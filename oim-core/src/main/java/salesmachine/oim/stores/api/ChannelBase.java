package salesmachine.oim.stores.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimChannelSupplierMap;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderProcessingRule;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSuppliers;
import salesmachine.util.OimLogStream;

public abstract class ChannelBase implements IOrderImport {
	private static final Logger log = LoggerFactory
			.getLogger(ChannelBase.class);
	protected Session m_dbSession;
	protected OimChannels m_channel;
	protected OimOrderProcessingRule m_orderProcessingRule;
	protected Map<String, OimSuppliers> supplierMap;
	@Deprecated
	protected OimLogStream logStream;

	@Override
	public boolean init(int channelID, Session dbSession, OimLogStream logStream) {
		m_dbSession = dbSession;
		if (logStream != null)
			this.logStream = logStream;
		else
			this.logStream = new OimLogStream();

		m_channel = (OimChannels) m_dbSession.get(OimChannels.class, channelID);

		if (m_channel == null) {
			log.error("No channel found with channel id: {}", channelID);
			return false;
		}
		log.info("Initializing Channel : {}", m_channel.getChannelName());
		Query query = m_dbSession
				.createQuery("select opr from salesmachine.hibernatedb.OimOrderProcessingRule opr where opr.deleteTm is null and opr.oimChannels=:chan");
		query.setEntity("chan", m_channel);
		Iterator iter = query.iterate();
		if (iter.hasNext()) {
			m_orderProcessingRule = (OimOrderProcessingRule) iter.next();
		}
		Set suppliers = m_channel.getOimChannelSupplierMaps();
		supplierMap = new HashMap<String, OimSuppliers>();
		Iterator itr = suppliers.iterator();
		while (itr.hasNext()) {
			OimChannelSupplierMap map = (OimChannelSupplierMap) itr.next();
			if (map.getDeleteTm() != null)
				continue;

			String prefix = map.getSupplierPrefix();
			OimSuppliers supplier = map.getOimSuppliers();
			log.info("Supplier Prefix: {} ID: {}", prefix,
					supplier.getSupplierId());
			supplierMap.put(prefix, supplier);
		}
		return true;
	}

	@Deprecated
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

	protected boolean orderAlreadyImported(String storeOrderId) {
		Query query = m_dbSession
				.createQuery("select o from salesmachine.hibernatedb.OimOrders o where o.oimOrderBatches.oimChannels=:chan and o.storeOrderId=:storeOrderId");
		query.setEntity("chan", m_channel);
		query.setString("storeOrderId", storeOrderId);
		Object obj = query.uniqueResult();
		return obj instanceof OimOrders;

	}

}
