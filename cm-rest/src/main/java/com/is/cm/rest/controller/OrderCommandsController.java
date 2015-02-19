package com.is.cm.rest.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponentsBuilder;

import com.is.cm.core.domain.Order;
import com.is.cm.core.domain.OrderDetail;
import com.is.cm.core.event.UpdateEvent;
import com.is.cm.core.event.UpdatedEvent;
import com.is.cm.core.event.orders.CreateOrderEvent;
import com.is.cm.core.event.orders.DeleteOrderEvent;
import com.is.cm.core.event.orders.OrderCreatedEvent;
import com.is.cm.core.event.orders.OrderDeletedEvent;
import com.is.cm.core.event.orders.OrderDetailUpdatedEvent;
import com.is.cm.core.event.orders.UpdateOrderDetailEvent;
import com.is.cm.core.service.OrderService;

@Controller
@RequestMapping("/aggregators/orders")
public class OrderCommandsController {
	private static Logger LOG = LoggerFactory
			.getLogger(OrderCommandsController.class);
	@Autowired
	private OrderService orderService;

	@RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
	public ResponseEntity<Order> deleteOrder(@PathVariable String id) {
		OrderDeletedEvent deleteOrderEvent = orderService
				.delete(new DeleteOrderEvent(Integer.parseInt(id)));
		if (!deleteOrderEvent.isEntityFound()) {
			return new ResponseEntity<Order>(HttpStatus.NOT_FOUND);
		}
		if (deleteOrderEvent.isDeletionCompleted()) {
			return new ResponseEntity<Order>(deleteOrderEvent.getEntity(),
					HttpStatus.OK);
		}
		LOG.debug("No Order deleted.");
		return new ResponseEntity<Order>(deleteOrderEvent.getEntity(),
				HttpStatus.FORBIDDEN);
	}

	@RequestMapping(method = { RequestMethod.PUT })
	public ResponseEntity<Order> saveOrder(@RequestBody Order order,
			UriComponentsBuilder builder) {
		OrderCreatedEvent orderCreated = orderService
				.createOrder(new CreateOrderEvent(order));
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(builder.path("/aggregators/orders/{id}")
				.buildAndExpand(orderCreated.getNewId()).toUri());
		return new ResponseEntity<Order>(orderCreated.getEntity(), headers,
				HttpStatus.CREATED);
	}

	@RequestMapping(method = { RequestMethod.PUT }, value = "/orderdetails/{detailId}")
	public ResponseEntity<OrderDetail> updateOrderDetail(
			@RequestBody OrderDetail orderDetail, @PathVariable int detailId,
			UriComponentsBuilder builder) {
		OrderDetailUpdatedEvent event = orderService
				.update(new UpdateOrderDetailEvent(detailId, orderDetail));
		return new ResponseEntity<OrderDetail>(event.getEntity(), HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST }, value = "/processed/{orderId}")
	public ResponseEntity<Order> processOrder(@RequestBody Order order,
			UriComponentsBuilder builder, @PathVariable int orderId) {
		UpdatedEvent<Order> event = orderService
				.processOrder(new UpdateEvent<Order>(orderId, order));
		return new ResponseEntity<Order>(event.getEntity(), HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.POST }, value = "/processed/bulk/{action}")
	public ResponseEntity<List<Order>> processOrder(
			@PathVariable String action, @RequestBody List<Integer> orders) {
		UpdatedEvent<List<Order>> event = orderService.bulkProcessOrder1(
				action, new UpdateEvent<List<Integer>>(0, orders));
		return new ResponseEntity<List<Order>>(event.getEntity(), HttpStatus.OK);
	}
}
