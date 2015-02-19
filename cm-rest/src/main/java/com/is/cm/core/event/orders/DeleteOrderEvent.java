package com.is.cm.core.event.orders;

import com.is.cm.core.domain.Order;
import com.is.cm.core.event.DeleteEvent;

public class DeleteOrderEvent extends DeleteEvent<Order> {

	public DeleteOrderEvent(int id) {
		super(id);
	}

}
