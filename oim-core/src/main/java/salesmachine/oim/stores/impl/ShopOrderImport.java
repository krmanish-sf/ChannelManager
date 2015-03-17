package salesmachine.oim.stores.impl;

import org.hibernate.Session;

import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.util.OimLogStream;

public class ShopOrderImport implements IOrderImport {

	@Override
	public boolean init(int channelID, Session dbSession, OimLogStream log) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getVendorOrders() {
		// TODO Auto-generated method stub
		return false;
	}

}
