package com.is.cm.core.event.orders;

import com.is.cm.core.domain.Order;
import com.is.cm.core.event.RequestReadEvent;

public class RequestAllOrdersEvent extends RequestReadEvent<Order> {
	private final String orderStatus;

	public RequestAllOrdersEvent() {
		this(null);
	}

	public RequestAllOrdersEvent(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	public String getOrderStatus() {
		return orderStatus;
	}
}
