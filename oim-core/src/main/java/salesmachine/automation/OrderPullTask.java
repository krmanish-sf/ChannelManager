package salesmachine.automation;

import java.util.TimerTask;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.oim.stores.api.IOrderImport;

import com.google.common.eventbus.EventBus;

public class OrderPullTask extends TimerTask {
	private static final Logger log = LoggerFactory
			.getLogger(OrderPullTask.class);
	private final EventBus eventBus;
	private final Session session;

	public OrderPullTask(EventBus eventBus, Session session) {
		this.eventBus = eventBus;
		this.session = session;
	}

	@Override
	public void run() {
		log.info("Order Pull Task Running...");
		// Query vendorQuery =
		// session.createQuery("from salesmachine.hibernatedb.Reps r where r.cmAllowed=:cmAllowed");

		Query channelQuery = session
				.createQuery("select distinct c from salesmachine.hibernatedb.OimChannels c "
						+ "inner join c.oimSupportedChannels "
						+ "inner join c.oimOrderProcessingRules r "
						+ "left join c.oimChannelAccessDetailses d "
						+ "where c.vendors.vendorId=:vid");
		channelQuery.setInteger("vid", 441325);
		for (Object object : channelQuery.list()) {
			OimChannels channel = (OimChannels) object;
			log.info("Channel Type: {}", channel.getOimSupportedChannels()
					.getChannelName());
			String orderFetchBean = channel.getOimSupportedChannels()
					.getOrderFetchBean();
			IOrderImport iOrderImport = null;
			if (orderFetchBean != null && orderFetchBean.length() > 0) {
				try {
					Class theClass = Class.forName(orderFetchBean);
					iOrderImport = (IOrderImport) theClass.newInstance();
				} catch (Exception e) {
					log.error("CONFIG ERROR: Error in initializing Channel Bean.");
					iOrderImport = null;
				}
			}
			if (iOrderImport != null) {
				log.debug("Created the orderimport object");
				if (!iOrderImport.init(channel.getChannelId(), session, null)) {
					log.error(
							"Failed initializing the channel with channelId {},",
							channel.getChannelId());
				} else {
					log.info("Pulling orders for channel id: {}",
							channel.getChannelId());
					OimOrderBatches vendorOrders = iOrderImport
							.getVendorOrders();
					if (vendorOrders != null)
						eventBus.post(vendorOrders);
				}
			} else {
				log.error("CONFIG ERROR : Could not find a bean to work with this market. ");
			}
		}
	}
}
