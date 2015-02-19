package com.is.cm.core.service;

import java.util.List;
import java.util.Map;

import com.is.cm.core.domain.Order;
import com.is.cm.core.event.ReadCollectionEvent;
import com.is.cm.core.event.RequestReadEvent;
import com.is.cm.core.event.UpdateEvent;
import com.is.cm.core.event.UpdatedEvent;
import com.is.cm.core.event.orders.AllOrdersEvent;
import com.is.cm.core.event.orders.CreateOrderEvent;
import com.is.cm.core.event.orders.DeleteOrderEvent;
import com.is.cm.core.event.orders.OrderCreatedEvent;
import com.is.cm.core.event.orders.OrderDeletedEvent;
import com.is.cm.core.event.orders.OrderDetailUpdatedEvent;
import com.is.cm.core.event.orders.RequestAllOrdersEvent;
import com.is.cm.core.event.orders.UpdateOrderDetailEvent;

public interface OrderService {

	AllOrdersEvent getOrders(RequestAllOrdersEvent requestAllOrdersEvent);

	OrderDeletedEvent delete(DeleteOrderEvent deleteOrderEvent);

	OrderCreatedEvent createOrder(CreateOrderEvent createOrderEvent);

	OrderDetailUpdatedEvent update(UpdateOrderDetailEvent updateOrderDetailEvent);

	ReadCollectionEvent<Order> findOrderByStatus(RequestReadEvent<String> event);

	UpdatedEvent<Order> processOrder(UpdateEvent<Order> event);

	ReadCollectionEvent<Order> find(
			RequestReadEvent<Map<String, String>> requestReadEvent);

	UpdatedEvent<List<Order>> bulkProcessOrder(UpdateEvent<List<Order>> event);

	UpdatedEvent<List<Order>> bulkProcessOrder1(String status,
			UpdateEvent<List<Integer>> event);

}
