package salesmachine.oim.stores.api;

import org.hibernate.Session;

import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderBatchesTypes;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.suppliers.modal.OrderStatus;

public interface IOrderImport {
	boolean init(int channelID, Session dbSession)
			throws ChannelConfigurationException;

	OimOrderBatches getVendorOrders(OimOrderBatchesTypes batchesTypes);

	boolean updateStoreOrder(OimOrderDetails oimOrderDetails,
			OrderStatus orderStatus);
}
