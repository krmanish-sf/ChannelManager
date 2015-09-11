package com.is.cm.core.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderStatuses;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.modal.shop.order.CCTRANSMISSION;
import salesmachine.oim.suppliers.OimSupplierOrderPlacement;
import salesmachine.oim.suppliers.exception.SupplierCommunicationException;
import salesmachine.oim.suppliers.exception.SupplierConfigurationException;
import salesmachine.oim.suppliers.exception.SupplierOrderException;

import com.is.cm.core.domain.DataTableCriterias;
import com.is.cm.core.domain.DataTableCriterias.SearchCriterias;
import com.is.cm.core.domain.Order;
import com.is.cm.core.domain.OrderDetail;
import com.is.cm.core.domain.OrderDetailMod;
import com.is.cm.core.domain.PagedDataResult;
import com.is.cm.core.domain.VendorContext;
import com.is.cm.core.event.CreateEvent;
import com.is.cm.core.event.CreatedEvent;
import com.is.cm.core.event.PagedDataResultEvent;
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
	public PagedDataResultEvent<Order> findOrderByStatus(String status,
			RequestReadEvent<DataTableCriterias> event) {
		PagedDataResult<Order> result = null;
		String storeOrderId = event.getEntity().getSearch()
				.get(SearchCriterias.value);
		if ("unprocessed".equalsIgnoreCase(status)) {
			result = orderRepository.findUnprocessedOrders(event.getEntity()
					.getStart(), event.getEntity().getLength(), storeOrderId);
		} else if ("unresolved".equalsIgnoreCase(status)) {
			result = orderRepository.findUnresolvedOrders(event.getEntity());
		} else if ("posted".equalsIgnoreCase(status)) {
			result = orderRepository.findProcessedOrders(event.getEntity()
					.getStart(), event.getEntity().getLength(), storeOrderId);
		} else
			result = new PagedDataResult<Order>(0, 0, new ArrayList<Order>(0));
		result.setDraw(event.getEntity().getDraw());
		return new PagedDataResultEvent<Order>(result);
	}

	@Override
	public UpdatedEvent<Order> processOrder(UpdateEvent<Order> event)
			throws SupplierConfigurationException,
			SupplierCommunicationException, SupplierOrderException {
		Order order = event.getEntity();
		if (processOrderInternal(order)) {
			return new UpdatedEvent<Order>(event.getEntity().getOrderId(),
					event.getEntity());
		} else {
			return UpdatedEvent.updateForbidden(event.getId(), order);
		}
	}

	private boolean processOrderInternal(Order order)
			throws SupplierConfigurationException,
			SupplierCommunicationException, SupplierOrderException {
		Session dbSession = SessionManager.currentSession();
		OimSupplierOrderPlacement osop = new OimSupplierOrderPlacement(
				dbSession);
		OimOrders oimOrders = orderRepository.getById(order.getOrderId());
		return osop.processVendorOrder(VendorContext.get(), oimOrders);
	}

	@Override
	public UpdatedEvent<List<Order>> bulkProcessOrder(
			UpdateEvent<List<Order>> event)
			throws SupplierConfigurationException,
			SupplierCommunicationException, SupplierOrderException {
		List<Order> orders = event.getEntity();
		LOG.debug("Order Size: {}", orders.size());
		Object order2 = orders.get(0);
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
			throw new RuntimeException("Order List must not be empty.");
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
					// detail.setDeleteTm(new Date());
					detail.setOimOrderStatuses(new OimOrderStatuses(
							OimConstants.ORDER_STATUS_CANCELED));
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
			} else if ("re-process".equalsIgnoreCase(status)) {
				try {
					Session dbSession = SessionManager.currentSession();
					OimSupplierOrderPlacement osop = new OimSupplierOrderPlacement(
							dbSession);
					OimOrders oimOrders = orderRepository.getById(order
							.getOrderId());
					osop.reprocessVendorOrder(VendorContext.get(), oimOrders);
					list.add(Order.from(order));
				} catch (Exception e) {
					LOG.error("Error occured in re-submitting orders", e);
				}
			} else if ("track".equalsIgnoreCase(status)) {
				try {
					Session dbSession = SessionManager.currentSession();
					OimSupplierOrderPlacement osop = new OimSupplierOrderPlacement(
							dbSession);

					for (Iterator dit = order.getOimOrderDetailses().iterator(); dit
							.hasNext();) {
						OimOrderDetails detail = (OimOrderDetails) dit.next();
						if (detail
								.getOimOrderStatuses()
								.getStatusId()
								.equals(OimConstants.ORDER_STATUS_PROCESSED_SUCCESS))
							osop.trackOrder(VendorContext.get(),
									detail.getDetailId());
					}
					list.add(Order.from(order));
				} catch (Exception e) {
					LOG.error("Error occured in tracking orders", e);
				}
			}

		}
		return new UpdatedEvent<List<Order>>(0, list);

	}

	@Override
	public PagedDataResultEvent<Order> find(
			RequestReadEvent<DataTableCriterias> requestReadEvent) {
		PagedDataResult<Order> orders = orderRepository.find(requestReadEvent
				.getEntity());
		return new PagedDataResultEvent<Order>(orders);
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

	@Override
	public ReadCollectionEvent<OrderDetail> getOrderDetailByOrderId(
			ReadEvent<String> requestReadEvent) {
		// Order order =
		// orderRepository.findById(Integer.parseInt(requestReadEvent.getEntity()));
		OimOrders order = orderRepository.getById(Integer
				.parseInt(requestReadEvent.getEntity()));
		List<OrderDetail> list = new ArrayList<OrderDetail>();
		for (Iterator<OimOrderDetails> itr = order.getOimOrderDetailses()
				.iterator(); itr.hasNext();) {
			OimOrderDetails details = itr.next();
			list.add(OrderDetail.from(details));
		}
		return new ReadCollectionEvent<OrderDetail>(list);
	}

}
