package com.is.cm.core.service;

import java.util.Map;

import com.is.cm.core.domain.ShippingCarrier;
import com.is.cm.core.domain.ShippingMethod;
import com.is.cm.core.domain.Supplier;
import com.is.cm.core.domain.SupplierShippingMethod;
import com.is.cm.core.domain.VendorShippingMap;
import com.is.cm.core.domain.VendorSupplier;
import com.is.cm.core.event.CreateEvent;
import com.is.cm.core.event.CreatedEvent;
import com.is.cm.core.event.DeleteEvent;
import com.is.cm.core.event.DeletedEvent;
import com.is.cm.core.event.ReadCollectionEvent;
import com.is.cm.core.event.RequestReadEvent;
import com.is.cm.core.event.UpdateEvent;
import com.is.cm.core.event.UpdatedEvent;
import com.is.cm.core.event.supplier.AllSuppliersEvent;
import com.is.cm.core.event.supplier.RequestAllSuppliersEvent;

public interface SupplierService {
	AllSuppliersEvent getAll(RequestAllSuppliersEvent requestAllSuppliersEvent);

	UpdatedEvent<VendorSupplier> update(
			UpdateEvent<Map<String, String>> updateVendorSupplierEvent);

	DeletedEvent<VendorSupplier> removeSubscription(
			DeleteEvent<VendorSupplier> deleteEvent);

	ReadCollectionEvent<Supplier> getUnsubscribed(
			RequestReadEvent<Supplier> requestReadEvent);

	CreatedEvent<VendorSupplier> subscribe(
			CreateEvent<Map<String, String>> createEvent);

	UpdatedEvent<Map<String, String>> updateShippingMapping(
			UpdateEvent<Map<String, String>> updateEvent);

	@Deprecated
	ReadCollectionEvent<VendorShippingMap> findVendorShippingMethods(
			RequestReadEvent<Integer> requestReadEvent);

	ReadCollectionEvent<SupplierShippingMethod> findSupplierShippingMethods(
			RequestReadEvent<Integer> requestReadEvent);

	ReadCollectionEvent<ShippingCarrier> findSupplierShippingCarriers(
			RequestReadEvent<Integer> requestReadEvent);

	ReadCollectionEvent<SupplierShippingMethod> findChannelShippingForSupplierCarrier(
			RequestReadEvent<Integer[]> requestReadEvent);

	ReadCollectionEvent<SupplierShippingMethod> saveShippingOverrideForSupplierMethod(
			RequestReadEvent<Object[]> requestReadEvent);

	DeletedEvent<Integer> deleteShippingOverrideForSupplierMethod(
			DeleteEvent deleteEvent);

	ReadCollectionEvent<ShippingMethod> getShippingMethods(
			RequestReadEvent<ShippingMethod> requestReadEvent);

}
