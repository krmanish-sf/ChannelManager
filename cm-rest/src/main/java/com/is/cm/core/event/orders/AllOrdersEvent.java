package com.is.cm.core.event.orders;

import java.util.List;

import com.is.cm.core.domain.Order;
import com.is.cm.core.event.ReadCollectionEvent;

public class AllOrdersEvent extends ReadCollectionEvent<Order> {

	public AllOrdersEvent(List<Order> entities) {
		super(entities);
	}
}
