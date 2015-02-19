package com.is.cm.core.event.orders;

import com.is.cm.core.domain.Order;
import com.is.cm.core.event.ReadEvent;

public class OrderDetailsEvent extends ReadEvent<Order> {

	public OrderDetailsEvent(int id, Order entity) {
		super(id, entity);
	}
}
