package com.is.cm.rest.controller;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.is.cm.core.domain.Order;
import com.is.cm.core.event.ReadCollectionEvent;
import com.is.cm.core.event.RequestReadEvent;
import com.is.cm.core.event.orders.AllOrdersEvent;
import com.is.cm.core.event.orders.RequestAllOrdersEvent;
import com.is.cm.core.security.AuthenticationInterceptor.MyThreadLocal;
import com.is.cm.core.service.OrderService;

@Controller
@RequestMapping("/aggregators/orders")
public class OrderQueriesController {
	private static Logger LOG = LoggerFactory
			.getLogger(OrderQueriesController.class);

	@Autowired
	private OrderService orderService;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Collection<Order> getAllOrders() {
		LOG.debug("Getting all orders...");

		AllOrdersEvent details = orderService
				.getOrders(new RequestAllOrdersEvent());
		return details.getEntity();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{orderStatus}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Collection<Order> getAllOrders(@PathVariable String orderStatus) {
		LOG.debug("Getting {} orders...", orderStatus);
		LOG.info("Getting vendorId# {} from ThreadLocal", MyThreadLocal.get());
		ReadCollectionEvent<Order> details = orderService
				.findOrderByStatus(new RequestReadEvent<String>(orderStatus));
		return details.getEntity();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/search")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Collection<Order> searchOrders(
			@RequestBody Map<String, String> filters) {
		LOG.debug("Getting orders for vid: {}", MyThreadLocal.get());
		Map<String, String> o = (Map<String, String>) filters;
		ReadCollectionEvent<Order> details = orderService
				.find(new RequestReadEvent<Map<String, String>>(o));
		return details.getEntity();
	}
}
