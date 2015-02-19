package com.is.cm.core.event.orders;

import com.is.cm.core.domain.OrderDetail;
import com.is.cm.core.event.UpdatedEvent;

public class OrderDetailUpdatedEvent extends UpdatedEvent<OrderDetail> {

	public OrderDetailUpdatedEvent(final int id, final OrderDetail orderDetail) {
		super(id, orderDetail);
	}
}
