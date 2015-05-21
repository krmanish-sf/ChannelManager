package salesmachine.oim.stores.impl;

import org.hibernate.Session;

import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderBatchesTypes;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.oim.stores.api.ChannelBase;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.util.OimLogStream;

/***
 * Psudo code to explain Channel Integration.
 * 
 * @author amit-yadav
 *
 */

public class EbayOrderImport extends ChannelBase implements IOrderImport {

	@Override
	public boolean init(int channelID, Session dbSession, OimLogStream logStream) {
		super.init(channelID, dbSession, logStream);
		// Ebay Specific parameters init.
		return false;
	}

	@Override
	public OimOrderBatches getVendorOrders(OimOrderBatchesTypes batchesTypes) {
		// Provide the Order pulling logic form Ebay API.
		return null;
	}

	@Override
	public boolean updateStoreOrder(OimOrderDetails oimOrderDetails,
			OrderStatus orderStatus) {
		// Provide logic to update the Ebay store with Order status.
		return false;
	}

}
