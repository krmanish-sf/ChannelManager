package com.is.cm.core.service;

import java.util.List;
import java.util.Map;

import salesmachine.oim.stores.modal.shop.order.CCTRANSMISSION;
import salesmachine.oim.suppliers.exception.SupplierCommunicationException;
import salesmachine.oim.suppliers.exception.SupplierConfigurationException;
import salesmachine.oim.suppliers.exception.SupplierOrderException;

import com.is.cm.core.domain.DataTableCriterias;
import com.is.cm.core.domain.Order;
import com.is.cm.core.domain.OrderDetail;
import com.is.cm.core.domain.OrderDetailMod;
import com.is.cm.core.domain.OrderTracking;
import com.is.cm.core.event.CreateEvent;
import com.is.cm.core.event.CreatedEvent;
import com.is.cm.core.event.DeleteEvent;
import com.is.cm.core.event.DeletedEvent;
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

public interface OrderService {

    AllOrdersEvent getOrders(RequestAllOrdersEvent requestAllOrdersEvent);

    OrderDeletedEvent delete(DeleteOrderEvent deleteOrderEvent);

    OrderCreatedEvent createOrder(CreateOrderEvent createOrderEvent);

    OrderDetailUpdatedEvent update(UpdateOrderDetailEvent updateOrderDetailEvent);

    PagedDataResultEvent<Order> findOrderByStatus(String status,
	    RequestReadEvent<DataTableCriterias> requestReadEvent);

    UpdatedEvent<Order> processOrder(UpdateEvent<Order> event)
	    throws SupplierConfigurationException,
	    SupplierCommunicationException, SupplierOrderException;

    PagedDataResultEvent<Order> find(
	    RequestReadEvent<DataTableCriterias> requestReadEvent);

    UpdatedEvent<List<Order>> bulkProcessOrder(UpdateEvent<List<Order>> event)
	    throws SupplierConfigurationException,
	    SupplierCommunicationException, SupplierOrderException;

    UpdatedEvent<List<Order>> bulkProcessOrder1(String status,
	    UpdateEvent<List<Integer>> event);

    UpdatedEvent<String> trackOrderStatus(UpdateEvent<Integer> updateEvent);

    CreatedEvent<List<Order>> saveOrder(CreateEvent<CCTRANSMISSION> event);

    ReadCollectionEvent<OrderDetailMod> getOrderDetailModifications(
	    ReadEvent<OrderDetailMod> readEvent);

    ReadCollectionEvent<OrderDetail> getOrderDetailByOrderId(
	    ReadEvent<String> requestReadEvent);

    UpdatedEvent<String> updateTracking(
	    UpdateEvent<Map<String, String>> updateEvent);

    DeletedEvent<OrderTracking> deleteTracking(DeleteEvent<OrderTracking> deleteEvent);

}
