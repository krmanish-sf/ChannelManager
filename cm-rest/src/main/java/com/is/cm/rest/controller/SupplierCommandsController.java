package com.is.cm.rest.controller;

import java.util.Map;

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

import com.is.cm.core.domain.VendorSupplier;
import com.is.cm.core.event.CreateEvent;
import com.is.cm.core.event.CreatedEvent;
import com.is.cm.core.event.DeleteEvent;
import com.is.cm.core.event.DeletedEvent;
import com.is.cm.core.event.UpdateEvent;
import com.is.cm.core.event.UpdatedEvent;
import com.is.cm.core.service.SupplierService;

@Controller
@RequestMapping("/aggregators/suppliers")
public class SupplierCommandsController extends BaseController {
	private static Logger LOG = LoggerFactory.getLogger(SupplierCommandsController.class);

	@Autowired
	private SupplierService supplierService;

	@RequestMapping(method = RequestMethod.PUT, value = "/{vendorSupplierId}")
	public ResponseEntity<VendorSupplier> update(@PathVariable int vendorSupplierId,
			@RequestBody Map<String, String> vendorSupplierData) {
		LOG.debug("Recieved request to update {}", vendorSupplierId);
		UpdatedEvent<VendorSupplier> event = supplierService
				.update(new UpdateEvent<Map<String, String>>(vendorSupplierId, vendorSupplierData));
		return new ResponseEntity<VendorSupplier>(event.getEntity(), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/updateHG/{vendorSupplierId}")
	public ResponseEntity<VendorSupplier> updateHG(@PathVariable int vendorSupplierId,
			@RequestBody Map<String, String> vendorSupplierData) {
		LOG.debug("Recieved request to update {}", vendorSupplierId);
		UpdatedEvent<VendorSupplier> event = supplierService
				.update(new UpdateEvent<Map<String, String>>(vendorSupplierId, vendorSupplierData));
		return new ResponseEntity<VendorSupplier>(event.getEntity(), HttpStatus.OK);
	}


	@RequestMapping(method = RequestMethod.DELETE, value = "/subscriptions/{vendorSupplierId}")
	public ResponseEntity<VendorSupplier> removeSubscription(@PathVariable String vendorSupplierId) {
		LOG.debug("Recieved request to delete subscription for {}", vendorSupplierId);
		DeletedEvent<VendorSupplier> deletedEvent = supplierService
				.removeSubscription(new DeleteEvent<VendorSupplier>(Integer.parseInt(vendorSupplierId)));
		return createResponseBody(deletedEvent);
	}

	@RequestMapping(method = RequestMethod.PUT)
	public ResponseEntity<VendorSupplier> addSubscription(@RequestBody Map<String, String> supplierMap) {
		CreatedEvent<VendorSupplier> createdEvent = supplierService
				.subscribe(new CreateEvent<Map<String, String>>(supplierMap));
		if (createdEvent.getNewId() > 0) {
			return new ResponseEntity<VendorSupplier>(createdEvent.getEntity(), HttpStatus.CREATED);
		} else
			return new ResponseEntity<VendorSupplier>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/{supplierId}/shippingmapping")
	public ResponseEntity<Map<String, String>> saveShippingMap(@PathVariable int supplierId,
			@RequestBody Map<String, String> map) {
		map.put("supplierId", String.valueOf(supplierId));
		UpdatedEvent<Map<String, String>> event = supplierService
				.updateShippingMapping(new UpdateEvent<Map<String, String>>(supplierId, map));
		return new ResponseEntity<Map<String, String>>(event.getEntity(), HttpStatus.CREATED);
	}
}
