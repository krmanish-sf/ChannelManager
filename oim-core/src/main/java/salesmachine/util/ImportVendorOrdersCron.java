package salesmachine.util;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderBatchesTypes;
import salesmachine.hibernatedb.Reps;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.IOrderImport;

@Deprecated
public class ImportVendorOrdersCron {

	public static void main(String arg[]) {
		Session dbSession = SessionManager.currentSession();
		Vector activeVendors = getActiveOIMVendor(dbSession);
		for (int i = 0; i < activeVendors.size(); i++) {
			Integer vid = (Integer) activeVendors.get(i);
			getChannels(vid);
		}
	}

	public static Vector getActiveOIMVendor(Session dbSession) {
		Transaction tx = null;
		Reps r = null;
		Vector activeVendors = new Vector();

		try {
			tx = dbSession.beginTransaction();
			Query query = dbSession
					.createQuery("from salesmachine.hibernatedb.Reps r where r.cmAllowed=:cmAllowed");
			query.setString("cmAllowed", "1");
			Iterator it = query.iterate();
			while (it.hasNext()) {
				r = (salesmachine.hibernatedb.Reps) it.next();
				activeVendors.add(r.getVendorId());
				System.out.println("Active Channel Manager VenderID : "
						+ r.getVendorId());
			}
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			e.printStackTrace();
		}
		return activeVendors;
	}

	private static void getChannels(Integer vid) {
		Session session = SessionManager.currentSession();

		Vendors vendors = new Vendors(vid);
		Query query = session
				.createQuery("select distinct c from salesmachine.hibernatedb.OimChannels c "
						+ "inner join fetch c.oimSupportedChannels "
						+ "inner join fetch c.oimOrderProcessingRules r "
						+ "left join fetch c.oimChannelAccessDetailses d "
						+ "where c.vendors.vendorId=:vid");
		List channels = query.setInteger("vid",
				vendors.getVendorId().intValue()).list();
		for (int i = 0; i < channels.size(); i++) {
			OimChannels channel = (OimChannels) channels.get(i);
			System.out.println("Active VID : " + vid + " with Channel ID :  "
					+ channel.getChannelId() + "   Channel Name : "
					+ channel.getOimSupportedChannels().getChannelName());
			startOrderPullForChannel(channel.getChannelId().intValue());
		}
	}

	private static void startOrderPullForChannel(int channelId) {
		Session session = SessionManager.currentSession();
		Query query = session
				.createQuery("from salesmachine.hibernatedb.OimChannels as c where c.channelId=:channelID");
		query.setInteger("channelID", channelId);

		if (!query.iterate().hasNext()) {
			System.out.println("No channel found for channel id: " + channelId);
			return;
		}

		OimChannels channel = (OimChannels) query.iterate().next();
		System.out.println("Supported channel : "
				+ channel.getOimSupportedChannels().getChannelName());
		String orderFetchBean = channel.getOimSupportedChannels()
				.getOrderFetchBean();
		IOrderImport coi = null;
		if (orderFetchBean != null && orderFetchBean.length() > 0) {
			try {
				Class theClass = Class.forName(orderFetchBean);
				coi = (IOrderImport) theClass.newInstance();
			} catch (Exception cnfe) {
				cnfe.printStackTrace();
				coi = null;
			}
		}

		if (coi != null) {
			System.out.println("Created the orderimport object");
			if (!coi.init(channelId, session, null)) {
				System.out
						.println("Failed initializing the channel with channelId:"
								+ channelId);
			} else {
				System.out.println("Pulling orders for channel id: "
						+ channelId);
				coi.getVendorOrders(new OimOrderBatchesTypes(
						OimConstants.ORDERBATCH_TYPE_ID_AUTOMATED));
			}
		} else {
			System.out
					.println("ERROR - Could not find a bean to work with this market. ");
		}
	}
}
