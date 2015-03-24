package salesmachine.oim.stores.impl;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.stores.api.IOrderImport;

public class OrderImportManager {
	public static final Logger log = LoggerFactory
			.getLogger(OrderImportManager.class);

	// Make extendable, avoid instantiation
	protected OrderImportManager() {

	}

	public static IOrderImport getIOrderImport(int channelId) {
		Session session = SessionManager.currentSession();
		OimChannels channel = (OimChannels) session.get(OimChannels.class,
				channelId);
		String channelName = channel.getChannelName();
		log.debug("Supported channel : " + channelName);
		String orderFetchBean = channel.getOimSupportedChannels()
				.getOrderFetchBean();
		IOrderImport coi = null;
		if (orderFetchBean != null && orderFetchBean.length() > 0) {
			try {
				Class<?> theClass = Class.forName(orderFetchBean);
				coi = (IOrderImport) theClass.newInstance();
			} catch (Exception cnfe) {
				log.error(cnfe.getMessage(), cnfe);
				coi = null;
			}
		}
		return coi;
	}
}
