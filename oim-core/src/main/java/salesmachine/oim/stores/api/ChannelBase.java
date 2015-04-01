package salesmachine.oim.stores.api;

import org.hibernate.Session;

import salesmachine.util.OimLogStream;

public abstract class ChannelBase implements IOrderImport {

	@Override
	public boolean init(int channelID, Session dbSession, OimLogStream log) {
		// TODO Auto-generated method stub
		return false;
	}

}
