package salesmachine.oim.stores.api;

import org.hibernate.Session;

import salesmachine.util.OimLogStream;

public interface IOrderImport {
	public boolean init(int channelID, Session dbSession, OimLogStream log);
	public boolean getVendorOrders();
}
