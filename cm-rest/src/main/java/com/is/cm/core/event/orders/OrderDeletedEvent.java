package com.is.cm.core.event.orders;

import com.is.cm.core.domain.Order;
import com.is.cm.core.event.DeletedEvent;

public class OrderDeletedEvent extends DeletedEvent<Order> {

	public OrderDeletedEvent(int id, Order entity) {
		super(id, entity);
	}
}
