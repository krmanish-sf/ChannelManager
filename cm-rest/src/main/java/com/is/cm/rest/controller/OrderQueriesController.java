package com.is.cm.rest.controller;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.is.cm.core.domain.DataTableCriterias;
import com.is.cm.core.domain.Order;
import com.is.cm.core.domain.OrderDetail;
import com.is.cm.core.domain.OrderDetailMod;
import com.is.cm.core.domain.PagedDataResult;
import com.is.cm.core.domain.VendorContext;
import com.is.cm.core.event.PagedDataResultEvent;
import com.is.cm.core.event.ReadCollectionEvent;
import com.is.cm.core.event.ReadEvent;
import com.is.cm.core.event.RequestReadEvent;
import com.is.cm.core.event.orders.AllOrdersEvent;
import com.is.cm.core.event.orders.RequestAllOrdersEvent;
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

	// @RequestMapping(method = RequestMethod.GET, value = "/{orderStatus}")
	// @ResponseStatus(HttpStatus.OK)
	// @ResponseBody
	// public Collection<Order> getAllOrders(@PathVariable String orderStatus) {
	// LOG.debug("Getting {} orders...", orderStatus);
	// ReadCollectionEvent<Order> details = orderService
	// .findOrderByStatus(new RequestReadEvent<String>(orderStatus));
	// return details.getEntity();
	// }

	@RequestMapping(method = RequestMethod.POST, value = "/{orderStatus}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public PagedDataResult<Order> getPostedOrders(
			@PathVariable String orderStatus,
			@RequestBody DataTableCriterias criteria) {

		LOG.debug("Getting {} orders...", orderStatus);
		PagedDataResultEvent<Order> details = orderService
				.findOrderByStatus(orderStatus,
						new RequestReadEvent<DataTableCriterias>(criteria));
		return details.getEntity();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/search")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public PagedDataResult<Order> searchOrders(
			@RequestBody DataTableCriterias criterias) {
		LOG.debug("Getting orders for vid: {}", VendorContext.get());
		PagedDataResultEvent<Order> details = orderService
				.find(new RequestReadEvent<DataTableCriterias>(criterias));
		details.getEntity().setDraw(criterias.getDraw());
		return details.getEntity();
	}

	@RequestMapping(method = { RequestMethod.GET }, value = "/orderdetails/{detailId}/modifications")
	public ResponseEntity<Collection<OrderDetailMod>> getOrderDetailModifications(
			@PathVariable int detailId) {
		ReadCollectionEvent<OrderDetailMod> event = orderService
				.getOrderDetailModifications(new ReadEvent<OrderDetailMod>(
						detailId));
		return new ResponseEntity<Collection<OrderDetailMod>>(
				event.getEntity(), HttpStatus.OK);
	}

	@RequestMapping(method = { RequestMethod.GET }, value = "/system/{orderId}/modifications")
	public ResponseEntity<Collection<OrderDetail>> getOrderDetailByOrderId(
			@PathVariable int orderId) {
		LOG.info("getOrderDetailByOrderId called------------------");
		ReadCollectionEvent<OrderDetail> event = orderService
				.getOrderDetailByOrderId(new ReadEvent<String>(orderId));
		return new ResponseEntity<Collection<OrderDetail>>(event.getEntity(),
				HttpStatus.OK);
	}
}
