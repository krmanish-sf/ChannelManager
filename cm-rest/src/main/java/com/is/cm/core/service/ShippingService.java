package com.is.cm.core.service;

import java.util.List;
import java.util.Map;

import com.is.cm.core.domain.ChannelShippingMap;
import com.is.cm.core.domain.OrderTracking;
import com.is.cm.core.domain.ShippingMethod;
import com.is.cm.core.event.CreateEvent;
import com.is.cm.core.event.CreatedEvent;
import com.is.cm.core.event.DeleteEvent;
import com.is.cm.core.event.DeletedEvent;
import com.is.cm.core.event.ReadCollectionEvent;
import com.is.cm.core.event.RequestReadEvent;

public interface ShippingService {

	List<ShippingMethod> getShippingMethods();

	ReadCollectionEvent<ChannelShippingMap> findShippingMethods(
			RequestReadEvent<Map<String, Integer>> requestReadEvent);

	CreatedEvent<ChannelShippingMap> createShippingMethods(
			CreateEvent<Map<String, Object>> createEvent);

	DeletedEvent<OrderTracking> deleteChannelShippingMapping(DeleteEvent<ChannelShippingMap> deleteEvent);

	
	
}
