package salesmachine.oim.stores.impl;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderBatchesTypes;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.oim.stores.api.ChannelBase;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.util.OimLogStream;

public class ShopifyOrderImport extends ChannelBase implements IOrderImport {

	private static final Logger log = LoggerFactory
			.getLogger(ShopifyOrderImport.class);
	private String shopifyToken;

	@Override
	public boolean init(int channelID, Session dbSession, OimLogStream logStream) {
		// TODO Auto-generated method stub
		return super.init(channelID, dbSession, logStream);

	}

	@Override
	public boolean updateStoreOrder(OimOrderDetails oimOrderDetails,
			OrderStatus orderStatus) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public OimOrderBatches getVendorOrders(OimOrderBatchesTypes batchesTypes) {
		// TODO Auto-generated method stub
		return null;
	}

}
