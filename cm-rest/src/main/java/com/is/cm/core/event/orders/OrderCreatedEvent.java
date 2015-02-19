package com.is.cm.core.event.orders;

import com.is.cm.core.domain.Order;
import com.is.cm.core.event.CreatedEvent;

public class OrderCreatedEvent extends CreatedEvent<Order> {

	public OrderCreatedEvent(int newId, Order entity) {
		super(newId, entity);
	}
}
