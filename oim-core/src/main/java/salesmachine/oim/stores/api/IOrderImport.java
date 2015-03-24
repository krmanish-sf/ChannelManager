package salesmachine.oim.stores.api;

import org.hibernate.Session;

import salesmachine.util.OimLogStream;

public interface IOrderImport {
	boolean init(int channelID, Session dbSession, OimLogStream log);

	boolean getVendorOrders();

	boolean updateStoreOrder(String storeOrderId, String orderStatus,
			String trackingDetail);
}
