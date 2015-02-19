package com.is.cm.core.event.orders;

import java.util.Map;

import com.is.cm.core.domain.Order;
import com.is.cm.core.event.CreateEvent;

public class CreateOrderEvent extends CreateEvent<Order> {

	public CreateOrderEvent(final Order order) {
		this(null, order);
	}

	private final Map<String, String> orderData;

	public CreateOrderEvent(final Map<String, String> orderData, Order order) {
		super(order);
		this.orderData = orderData;
	}

	public Map<String, String> getOrderData() {
		return orderData;
	}

}
