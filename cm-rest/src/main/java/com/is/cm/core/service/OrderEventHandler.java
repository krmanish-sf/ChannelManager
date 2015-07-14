package com.is.cm.core.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderStatuses;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.modal.shop.order.CCTRANSMISSION;

import com.is.cm.core.domain.Order;
import com.is.cm.core.domain.OrderDetailMod;
import com.is.cm.core.event.CreateEvent;
import com.is.cm.core.event.CreatedEvent;
import com.is.cm.core.event.ReadCollectionEvent;
import com.is.cm.core.event.ReadEvent;
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
import com.is.cm.core.persistance.OrderRepository;

public class OrderEventHandler implements OrderService {
	private static final Logger LOG = LoggerFactory
			.getLogger(OrderEventHandler.class);
	private final OrderRepository orderRepository;

	public OrderEventHandler(final OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	@Override
	public AllOrdersEvent getOrders(RequestAllOrdersEvent requestAllOrdersEvent) {
		List<Order> orders;
		if (requestAllOrdersEvent.getOrderStatus() == null) {
			orders = orderRepository.findAll();
		} else {
			String status = requestAllOrdersEvent.getOrderStatus();
			if ("processed".equalsIgnoreCase(status))
				orders = orderRepository.findAll(new String[] { "2" });
			else {
				orders = orderRepository.findAll(new String[] { "0", "3", "5",
						"6" });
			}
		}
		return new AllOrdersEvent(orders);
	}

	@Override
	public OrderDeletedEvent delete(DeleteOrderEvent deleteOrderEvent) {
		orderRepository.delete(deleteOrderEvent.getId());
		return new OrderDeletedEvent(deleteOrderEvent.getId(), null);
	}

	@Override
	public OrderCreatedEvent createOrder(CreateOrderEvent createOrderEvent) {
		Order order;
		if (createOrderEvent.getEntity() != null)
			order = orderRepository.save(createOrderEvent.getEntity());
		else
			order = orderRepository.saveOrder(createOrderEvent.getOrderData());
		return new OrderCreatedEvent(order.getOrderId(), order);
	}

	@Override
	public OrderDetailUpdatedEvent update(
			UpdateOrderDetailEvent updateOrderDetailEvent) {
		orderRepository.update(updateOrderDetailEvent.getEntity());
		return new OrderDetailUpdatedEvent(updateOrderDetailEvent.getEntity()
				.getDetailId(), updateOrderDetailEvent.getEntity());
	}

	@Override
	public ReadCollectionEvent<Order> findOrderByStatus(
			RequestReadEvent<String> event) {
		if (event.getEntity() == null) {
			return null;
		} else if ("unprocessed".equalsIgnoreCase(event.getEntity())) {
			return new ReadCollectionEvent<Order>(
					orderRepository.findUnprocessedOrders());
		} else if ("unresolved".equalsIgnoreCase(event.getEntity())) {
			return new ReadCollectionEvent<Order>(
					orderRepository.findUnresolvedOrders());
		} else if ("posted".equalsIgnoreCase(event.getEntity())) {
			return new ReadCollectionEvent<Order>(
					orderRepository.findProcessedOrders());
		}
		return null;
	}

	@Override
	public UpdatedEvent<Order> processOrder(UpdateEvent<Order> event) {
		Order order = event.getEntity();
		if (processOrderInternal(order)) {
			return new UpdatedEvent<Order>(event.getEntity().getOrderId(),
					event.getEntity());
		} else {
			return UpdatedEvent.updateForbidden(event.getId(), order);
		}
	}

	private boolean processOrderInternal(Order order) {
		return orderRepository.processOrders(order);
	}

	@Override
	public UpdatedEvent<List<Order>> bulkProcessOrder(
			UpdateEvent<List<Order>> event) {
		List<Order> orders = event.getEntity();
		LOG.debug("Order Size: {}", orders.size());
		Object order2 = orders.get(0);
		Order o = (Order) order2;
		LOG.debug(order2.getClass().getName());
		for (Order order : orders) {
			orderRepository.processOrders(order);
		}
		return new UpdatedEvent<List<Order>>(0, event.getEntity());
	}

	@Override
	public UpdatedEvent<List<Order>> bulkProcessOrder1(String status,
			UpdateEvent<List<Integer>> event) {
		List<Integer> orders = event.getEntity();
		if (orders == null && "process".equalsIgnoreCase(status)) {
			LOG.warn("No order submitted for {}", status);
			List<Order> findUnprocessedOrders = orderRepository
					.findUnprocessedOrders();
			orders = new ArrayList<Integer>();
			LOG.debug("Querying unprocessed orders..");
			for (Order order : findUnprocessedOrders) {
				orders.add(order.getOrderId());
			}
		}
		LOG.debug("Order Size: {}", orders.size());
		List<Order> list = new ArrayList<Order>();
		for (Integer id : orders) {
			OimOrders order = orderRepository.getById(id);
			if ("manually-processed".equalsIgnoreCase(status)) {
				for (Iterator dit = order.getOimOrderDetailses().iterator(); dit
						.hasNext();) {
					OimOrderDetails detail = (OimOrderDetails) dit.next();
					detail.setOimOrderStatuses(new OimOrderStatuses(
							OimConstants.ORDER_STATUS_MANUALLY_PROCESSED));
					orderRepository.update(detail);
					list.add(Order.from(order));
				}
			} else if ("delete".equalsIgnoreCase(status)) {
				for (Iterator dit = order.getOimOrderDetailses().iterator(); dit
						.hasNext();) {
					OimOrderDetails detail = (OimOrderDetails) dit.next();
					detail.setDeleteTm(new Date());
					orderRepository.update(detail);
					list.add(Order.from(order));
				}
			} else if ("process".equalsIgnoreCase(status)) {
				try {
					processOrderInternal(Order.from(order));
					list.add(Order.from(order));
				} catch (Exception e) {
					LOG.error("Error occured in placing order", e);
				}
			}

		}
		return new UpdatedEvent<List<Order>>(0, list);

	}

	@Override
	public ReadCollectionEvent<Order> find(
			RequestReadEvent<Map<String, String>> requestReadEvent) {
		List<Order> orders = orderRepository.find(requestReadEvent.getEntity());
		return new ReadCollectionEvent<Order>(orders);
	}

	@Override
	public UpdatedEvent<String> trackOrderStatus(
			UpdateEvent<Integer> updateEvent) {
		String trackOrderStatus = orderRepository.trackOrderStatus(updateEvent
				.getEntity());
		return new UpdatedEvent<String>(updateEvent.getId(), trackOrderStatus);
	}

	@Override
	public CreatedEvent<List<Order>> saveOrder(CreateEvent<CCTRANSMISSION> event) {
		List<Order> orders = orderRepository.save(event.getEntity());
		return new CreatedEvent<List<Order>>(0, orders);
	}

	@Override
	public ReadCollectionEvent<OrderDetailMod> getOrderDetailModifications(
			ReadEvent<OrderDetailMod> readEvent) {
		List<OrderDetailMod> findOrderDetailModifications = orderRepository
				.findOrderDetailModifications(readEvent.getId());

		return new ReadCollectionEvent<OrderDetailMod>(
				findOrderDetailModifications);
	}

}
