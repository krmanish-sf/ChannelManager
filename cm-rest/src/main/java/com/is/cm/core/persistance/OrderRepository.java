package com.is.cm.core.persistance;

import java.util.List;
import java.util.Map;

import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;

import com.is.cm.core.domain.Order;
import com.is.cm.core.domain.OrderDetail;
import com.is.cm.core.domain.shop.CCTRANSMISSION;

public interface OrderRepository {

	List<Order> findAll();

	Order save(Order order);

	void delete(int id);

	Order findById(int id);

	OimOrders getById(int id);

	void update(OrderDetail orderDetail);

	List<Order> findAll(String[] orderStatus);

	Order saveOrder(Map<String, String> orderData);

	List<Order> findUnprocessedOrders();

	List<Order> findUnresolvedOrders();

	boolean processOrders(Order order);

	List<Order> find(Map<String, String> map);

	void update(OimOrderDetails orderDetail);

	String trackOrderStatus(Integer entity);

	List<Order> findProcessedOrders();

	Order save(CCTRANSMISSION entity);
}
