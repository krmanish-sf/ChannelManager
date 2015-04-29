package com.is.cm.core.service;

import java.util.List;
import java.util.Map;

import com.is.cm.core.domain.ChannelShippingMap;
import com.is.cm.core.domain.ShippingMethod;
import com.is.cm.core.event.CreateEvent;
import com.is.cm.core.event.CreatedEvent;
import com.is.cm.core.event.ReadCollectionEvent;
import com.is.cm.core.event.RequestReadEvent;
import com.is.cm.core.persistance.ShippingRepository;

public class ShippingEventHandler implements ShippingService {
	private final ShippingRepository shippingRepository;

	public ShippingEventHandler(ShippingRepository shippingRepository) {
		this.shippingRepository = shippingRepository;
	}

	@Override
	public List<ShippingMethod> getShippingMethods() {
		return shippingRepository.findShippingMethods();
	}

	@Override
	public ReadCollectionEvent<ChannelShippingMap> findShippingMethods(
			RequestReadEvent<Map<String, Integer>> requestReadEvent) {
		int shippingMethodId, supportedChannelId;
		shippingMethodId = requestReadEvent.getEntity().get("SMID");
		supportedChannelId = requestReadEvent.getEntity().get("SCID");
		List<ChannelShippingMap> entities = shippingRepository
				.findRegExForChannel(supportedChannelId, shippingMethodId);
		return new ReadCollectionEvent<ChannelShippingMap>(entities);
	}

	@Override
	public CreatedEvent<ChannelShippingMap> createShippingMethods(
			CreateEvent<Map<String, Object>> createEvent) {
		int shippingMethodId, supportedChannelId;
		shippingMethodId = (Integer) createEvent.getEntity().get("SMID");
		supportedChannelId = (Integer) createEvent.getEntity().get("SCID");
		String rexex = (String) createEvent.getEntity().get("REGEX");
		ChannelShippingMap entity = shippingRepository.saveChannelShippingMap(
				shippingMethodId, supportedChannelId, rexex);

		return new CreatedEvent<ChannelShippingMap>(entity.getId(), entity);
	}

}
