package com.is.cm.core.persistance;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import salesmachine.hibernatedb.OimChannelShippingMap;
import salesmachine.hibernatedb.OimShippingMethod;
import salesmachine.hibernatedb.OimSupportedChannels;
import salesmachine.hibernatehelper.SessionManager;

import com.is.cm.core.domain.ChannelShippingMap;
import com.is.cm.core.domain.ShippingMethod;

public class ShippingRepositoryDB extends RepositoryBase implements
		ShippingRepository {

	@Override
	public List<ShippingMethod> findShippingMethods() {
		Session dbSession = SessionManager.currentSession();
		List<ShippingMethod> shippingMethods = new ArrayList<ShippingMethod>();
		for (OimShippingMethod object : (List<OimShippingMethod>) dbSession
				.createCriteria(OimShippingMethod.class).list()) {
			shippingMethods.add(ShippingMethod.from(object));
		}
		return shippingMethods;
	}

	@Override
	public List<ChannelShippingMap> findRegExForChannel(int supportedChannelId,
			int shippingMethodId) {
		Session dbSession = SessionManager.currentSession();
		List<OimChannelShippingMap> list = dbSession
				.createCriteria(OimChannelShippingMap.class)
				.add(Restrictions.eq("oimSupportedChannel.supportedChannelId",
						supportedChannelId))
				.add(Restrictions.eq("oimShippingMethod.id", shippingMethodId))
				.list();
		List<ChannelShippingMap> map = new ArrayList<ChannelShippingMap>();
		for (OimChannelShippingMap oimChannelShippingMap : list) {
			map.add(ChannelShippingMap.from(oimChannelShippingMap));
		}
		return map;
	}

	@Override
	public ChannelShippingMap saveChannelShippingMap(int shippingMethodId,
			int supportedChannelId, String rexex) {
		Session dbSession = SessionManager.currentSession();
		OimSupportedChannels oimSupportedChannels = (OimSupportedChannels) dbSession
				.get(OimSupportedChannels.class, supportedChannelId);
		OimShippingMethod oimShippingMethod = (OimShippingMethod) dbSession
				.get(OimShippingMethod.class, shippingMethodId);
		OimChannelShippingMap channelShippingMap = new OimChannelShippingMap();
		channelShippingMap.setOimShippingMethod(oimShippingMethod);
		channelShippingMap.setOimSupportedChannel(oimSupportedChannels);
		channelShippingMap.setShippingRegEx(rexex);
		channelShippingMap.setOimShippingCarrier(oimShippingMethod
				.getOimShippingCarrier());
		dbSession.save(channelShippingMap);
		return ChannelShippingMap.from(channelShippingMap);
	}
}
