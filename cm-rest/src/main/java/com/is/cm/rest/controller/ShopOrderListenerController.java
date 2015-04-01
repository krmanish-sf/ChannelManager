package com.is.cm.rest.controller;

import java.io.StringReader;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import salesmachine.oim.stores.modal.shop.order.CCTRANSMISSION;
import salesmachine.oim.stores.modal.shop.order.CCTRANSMISSIONRESPONSE;
import salesmachine.oim.stores.modal.shop.order.ORDER;
import salesmachine.oim.stores.modal.shop.order.STATUS;
import salesmachine.oim.stores.modal.shop.order.status.OrderStatus;

import com.is.cm.core.domain.Order;
import com.is.cm.core.event.CreateEvent;
import com.is.cm.core.event.CreatedEvent;
import com.is.cm.core.service.OrderService;

@Controller
@RequestMapping("/shop-com-order-listener")
public class ShopOrderListenerController extends BaseController {

	private static final Logger log = LoggerFactory
			.getLogger(ShopOrderListenerController.class);
	@Autowired
	private OrderService orderService;

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<CCTRANSMISSIONRESPONSE> handleOrder(
			@RequestBody CCTRANSMISSION orderData) {
		log.info("Recieved new order from shop.com for catalogID: {}",
				orderData.getCATALOGID());
		CCTRANSMISSIONRESPONSE cc = new CCTRANSMISSIONRESPONSE();

		ORDER order = new ORDER();
		order.setALTURACATALOGID(orderData.getCATALOGID());
		order.setALTURAINVOICENO(orderData.getCCORDER().get(0).getINVOICENO());
		cc.setORDER(order);
		STATUS status = new STATUS();
		status.setMESSAGE("Invoice received successfully");
		status.setSTATUSCODE(OrderStatus.Order_received_by_seller.getValue());
		cc.setSTATUS(status);
		return new ResponseEntity<CCTRANSMISSIONRESPONSE>(cc, HttpStatus.OK);
	}

	@RequestMapping(value = "/SHOP/post_orders", method = RequestMethod.POST)
	public ResponseEntity<CCTRANSMISSIONRESPONSE> handleOrder2(
			@RequestParam String data) {
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(CCTRANSMISSION.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

			StringReader reader = new StringReader(data);
			CCTRANSMISSION orderData = (CCTRANSMISSION) unmarshaller
					.unmarshal(reader);
			log.info("Recieved new order from shop.com for catalogID: {}",
					orderData.getCATALOGID());

			CreatedEvent<List<Order>> saveOrder = orderService
					.saveOrder(new CreateEvent<CCTRANSMISSION>(orderData));
			CCTRANSMISSIONRESPONSE cc = new CCTRANSMISSIONRESPONSE();
			ORDER order = new ORDER();
			order.setALTURACATALOGID(orderData.getCATALOGID());
			order.setALTURAINVOICENO(orderData.getCCORDER().get(0)
					.getINVOICENO());
			cc.setORDER(order);
			STATUS status = new STATUS();
			status.setMESSAGE("Invoice received successfully");
			status.setSTATUSCODE("700");
			cc.setSTATUS(status);
			return new ResponseEntity<CCTRANSMISSIONRESPONSE>(cc, HttpStatus.OK);

		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
		}
		return new ResponseEntity<CCTRANSMISSIONRESPONSE>(
				new CCTRANSMISSIONRESPONSE(), HttpStatus.EXPECTATION_FAILED);
	}

	@RequestMapping(value = "/SHOP/post_orders", method = RequestMethod.GET)
	public ResponseEntity<CCTRANSMISSIONRESPONSE> handleOrder3(
			@RequestParam String data) {
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(CCTRANSMISSION.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

			StringReader reader = new StringReader(data);
			CCTRANSMISSION orderData = (CCTRANSMISSION) unmarshaller
					.unmarshal(reader);
			log.info("Recieved new order from shop.com for catalogID: {}",
					orderData.getCATALOGID());
			CCTRANSMISSIONRESPONSE cc = new CCTRANSMISSIONRESPONSE();
			ORDER order = new ORDER();
			order.setALTURACATALOGID(orderData.getCATALOGID());
			order.setALTURAINVOICENO(orderData.getCCORDER().get(0)
					.getINVOICENO());
			cc.setORDER(order);
			STATUS status = new STATUS();
			status.setMESSAGE("Invoice received successfully");
			status.setSTATUSCODE("700");
			cc.setSTATUS(status);
			return new ResponseEntity<CCTRANSMISSIONRESPONSE>(cc, HttpStatus.OK);

		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
		}
		return new ResponseEntity<CCTRANSMISSIONRESPONSE>(
				new CCTRANSMISSIONRESPONSE(), HttpStatus.EXPECTATION_FAILED);
	}
}
