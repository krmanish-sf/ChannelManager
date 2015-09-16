package com.is.cm.core.persistance;

import java.util.List;
import java.util.Map;

import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.oim.stores.modal.shop.order.CCTRANSMISSION;
import salesmachine.oim.suppliers.exception.SupplierCommunicationException;
import salesmachine.oim.suppliers.exception.SupplierConfigurationException;
import salesmachine.oim.suppliers.exception.SupplierOrderException;

import com.is.cm.core.domain.DataTableCriterias;
import com.is.cm.core.domain.Order;
import com.is.cm.core.domain.OrderDetail;
import com.is.cm.core.domain.OrderDetailMod;
import com.is.cm.core.domain.PagedDataResult;

public interface OrderRepository {

  void delete(int id);

  PagedDataResult<Order> find(DataTableCriterias criterias);

  List<Order> findAll();

  List<Order> findAll(String[] orderStatus);

  Order findById(int id);

  List<OrderDetailMod> findOrderDetailModifications(int orderDetailId);

  PagedDataResult<Order> findProcessedOrders(int firstResult, int pageSize, String storeOrderId);

  PagedDataResult<Order> findUnprocessedOrders(int firstResult, int pageSize, String storeOrderId);

  PagedDataResult<Order> findUnresolvedOrders(DataTableCriterias criterias);

  OimOrders getById(int id);

  boolean processOrders(Order order) throws SupplierConfigurationException,
      SupplierCommunicationException, SupplierOrderException;

  List<Order> save(CCTRANSMISSION entity);

  Order save(Order order);

  Order saveOrder(Map<String, String> orderData);

  String trackOrderStatus(Integer entity);

  void update(OimOrderDetails orderDetail);

  void update(OrderDetail orderDetail);

  String updateTracking(int detailId, Map<String, String> entity);
}
