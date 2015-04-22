package salesmachine.oim.stores.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderProcessingRule;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.util.OimLogStream;

public abstract class ChannelBase implements IOrderImport {
	private static final Logger log = LoggerFactory
			.getLogger(ChannelBase.class);
	protected Session m_dbSession;
	protected OimChannels m_channel;
	protected OimOrderProcessingRule m_orderProcessingRule;
	protected OimLogStream logStream;

	@Override
	public boolean init(int channelID, Session dbSession, OimLogStream logStream) {
		m_dbSession = dbSession;
		if (logStream != null)
			this.logStream = logStream;
		else
			this.logStream = new OimLogStream();

		Transaction tx = m_dbSession.beginTransaction();
		Query query = m_dbSession
				.createQuery("from salesmachine.hibernatedb.OimChannels as c where c.channelId=:channelID");
		query.setInteger("channelID", channelID);
		tx.commit();
		if (!query.iterate().hasNext()) {
			log.error("No channel found with channel id: {}", channelID);
			return false;
		}

		m_channel = (OimChannels) query.iterate().next();
		log.info("Initializing Channel : {}", m_channel.getChannelName());
		query = m_dbSession
				.createQuery("select opr from salesmachine.hibernatedb.OimOrderProcessingRule opr where opr.deleteTm is null and opr.oimChannels=:chan");
		query.setEntity("chan", m_channel);
		Iterator iter = query.iterate();
		if (iter.hasNext()) {
			m_orderProcessingRule = (OimOrderProcessingRule) iter.next();
		}	
		return true;
	}

	protected List<String> getCurrentOrders() {
		List<String> orders = new ArrayList<String>();

		Query query = m_dbSession
				.createQuery("select o from salesmachine.hibernatedb.OimOrders o where o.oimOrderBatches.oimChannels=:chan");
		query.setEntity("chan", m_channel);
		Iterator iter = query.iterate();
		while (iter.hasNext()) {
			OimOrders o = (OimOrders) iter.next();
			orders.add(o.getStoreOrderId());
		}
		return orders;
	}

}
