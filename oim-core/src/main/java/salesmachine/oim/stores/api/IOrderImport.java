package salesmachine.oim.stores.api;

import org.hibernate.Session;

import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.util.OimLogStream;

public interface IOrderImport {
	boolean init(int channelID, Session dbSession, OimLogStream log);

	boolean getVendorOrders();

	boolean updateStoreOrder(OimOrderDetails oimOrderDetails, OrderStatus orderStatus);
}
