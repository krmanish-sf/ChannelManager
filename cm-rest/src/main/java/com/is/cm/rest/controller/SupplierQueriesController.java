package com.is.cm.rest.controller;

import java.util.Collection;
import java.util.List;

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

import com.is.cm.core.domain.ShippingCarrier;
import com.is.cm.core.domain.Supplier;
import com.is.cm.core.domain.SupplierShippingMethod;
import com.is.cm.core.domain.VendorSupplier;
import com.is.cm.core.event.DeleteEvent;
import com.is.cm.core.event.DeletedEvent;
import com.is.cm.core.event.ReadCollectionEvent;
import com.is.cm.core.event.RequestReadEvent;
import com.is.cm.core.event.supplier.AllSuppliersEvent;
import com.is.cm.core.event.supplier.RequestAllSuppliersEvent;
import com.is.cm.core.service.SupplierService;

@Controller
@RequestMapping("/aggregators/suppliers")
public class SupplierQueriesController {
	private static Logger LOG = LoggerFactory
			.getLogger(SupplierQueriesController.class);

	@Autowired
	private SupplierService supplierService;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Collection<VendorSupplier> getAllVendorSuppliers() {
		LOG.debug("Getting all channels...");
		AllSuppliersEvent event = supplierService
				.getAll(new RequestAllSuppliersEvent());
		return event.getEntity();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/unsubscribed")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Collection<Supplier> getUnsubscribedSuppliers() {
		LOG.debug("Getting Unsubscribed suppliers ...");
		ReadCollectionEvent<Supplier> event = supplierService
				.getUnsubscribed(new RequestReadEvent<Supplier>());
		return event.getEntity();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{supplierId}/shippingmapping")
	public ResponseEntity<List<SupplierShippingMethod>> getShippingMap(
			@PathVariable int supplierId) {
		ReadCollectionEvent<SupplierShippingMethod> event = supplierService
				.findSupplierShippingMethods(new RequestReadEvent<Integer>(
						supplierId));
		return new ResponseEntity<List<SupplierShippingMethod>>(
				event.getEntity(), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{supplierId}/shippingcarriers")
	public ResponseEntity<List<ShippingCarrier>> getShippingCarriers(
			@PathVariable int supplierId) {
		ReadCollectionEvent<ShippingCarrier> event = supplierService
				.findSupplierShippingCarriers(new RequestReadEvent<Integer>(
						supplierId));
		return new ResponseEntity<List<ShippingCarrier>>(event.getEntity(),
				HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{supplierId}/shippingcarriers/{carrierId}")
	public ResponseEntity<List<SupplierShippingMethod>> getChannelShippingForSupplierCarrier(
			@PathVariable int supplierId, @PathVariable int carrierId) {
		ReadCollectionEvent<SupplierShippingMethod> event = supplierService
				.findChannelShippingForSupplierCarrier(new RequestReadEvent<Integer[]>(
						new Integer[] { supplierId, carrierId }));
		return new ResponseEntity<List<SupplierShippingMethod>>(
				event.getEntity(), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{supplierId}/shippingcarriers/overrides/{supplierMethodId}")
	public ResponseEntity<List<SupplierShippingMethod>> saveShippingOverrideForSupplierMethod(
			@PathVariable int supplierMethodId, @RequestBody String shippingText) {
		ReadCollectionEvent<SupplierShippingMethod> event = supplierService
				.saveShippingOverrideForSupplierMethod(new RequestReadEvent<Object[]>(
						new Object[] { supplierMethodId, shippingText }));
		return new ResponseEntity<List<SupplierShippingMethod>>(
				event.getEntity(), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/{supplierId}/shippingcarriers/overrides/{supplierMethodId}")
	public ResponseEntity<Integer> deleteShippingOverrideForSupplierMethod(
			@PathVariable int supplierMethodId) {
		DeletedEvent<Integer> event = supplierService
				.deleteShippingOverrideForSupplierMethod(new DeleteEvent(
						supplierMethodId));
		return new ResponseEntity<Integer>(event.getEntity(), HttpStatus.OK);
	}
}
