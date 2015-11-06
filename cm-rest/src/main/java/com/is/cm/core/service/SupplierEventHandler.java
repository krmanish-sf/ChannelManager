package com.is.cm.core.service;

import java.util.List;
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
import com.is.cm.core.persistance.SupplierRepository;

public class SupplierEventHandler implements SupplierService {
	private final SupplierRepository supplierRepository;

	public SupplierEventHandler(final SupplierRepository supplierRepository) {
		this.supplierRepository = supplierRepository;
	}

	@Override
	public AllSuppliersEvent getAll(
			RequestAllSuppliersEvent requestAllSuppliersEvent) {
		List<VendorSupplier> vendorSuppliers = supplierRepository.findAll();
		return new AllSuppliersEvent(vendorSuppliers);
	}

	@Override
	public UpdatedEvent<VendorSupplier> update(
			UpdateEvent<Map<String, String>> updateEvent) {
		VendorSupplier entity = supplierRepository.update(updateEvent.getId(),
				updateEvent.getEntity());
		return new UpdatedEvent<VendorSupplier>(entity.getVendorSupplierId(),
				entity);
	}

	@Override
	public DeletedEvent<VendorSupplier> removeSubscription(
			DeleteEvent<VendorSupplier> deleteEvent) {
		VendorSupplier vendorSupplier = supplierRepository
				.removeSubscription(deleteEvent.getId());
		if (null != vendorSupplier)
			return new DeletedEvent<VendorSupplier>(deleteEvent.getId(),
					vendorSupplier);
		else
			return new DeletedEvent<VendorSupplier>(deleteEvent.getId());
	}

	@Override
	public ReadCollectionEvent<Supplier> getUnsubscribed(
			RequestReadEvent<Supplier> requestReadEvent) {
		List<Supplier> suppliers = supplierRepository.findUnsubscribed();
		return new ReadCollectionEvent<Supplier>(suppliers);
	}

	@Override
	public CreatedEvent<VendorSupplier> subscribe(
			CreateEvent<Map<String, String>> createEvent) {
		VendorSupplier supplier;
		Integer supplierId = Integer.parseInt(createEvent.getEntity().get(
				"suppliername"));
		Integer testMode = Integer.parseInt(createEvent.getEntity().get(
				"testmode"));
		if (supplierId > 0) {
			if (supplierId == 1822) {
				supplier = supplierRepository.addSubscriptionHG(supplierId,
						createEvent.getEntity().get("phi-login"), createEvent
						.getEntity().get("phi-password"), createEvent
						.getEntity().get("phi-accountno"), createEvent
						.getEntity().get("phi-ftp"),
						createEvent.getEntity().get("hva-login"),
						createEvent.getEntity().get("hva-password"),
						createEvent.getEntity().get("hva-accountno"),
						createEvent.getEntity().get("hva-ftp"),
						testMode);
			}
			else if(supplierId == 221){
				supplier = supplierRepository.addSubscriptionWithFtpDetails(supplierId,createEvent.getEntity().get("moteng-ftp"),
						createEvent.getEntity().get("login"), createEvent
						.getEntity().get("password"), createEvent
						.getEntity().get("accountno"), testMode);
			}
			else{
				supplier = supplierRepository.addSubscription(supplierId,
						createEvent.getEntity().get("login"), createEvent
						.getEntity().get("password"), createEvent
						.getEntity().get("accountno"), createEvent
						.getEntity().get("defshippingmc"), testMode);
			}
		} else {
			supplier = supplierRepository.addCustomSubscription(createEvent
					.getEntity());

		}
		return new CreatedEvent<VendorSupplier>(supplier.getVendorSupplierId(),
				supplier);
	}

	@Override
	public UpdatedEvent<Map<String, String>> updateShippingMapping(
			UpdateEvent<Map<String, String>> updateEvent) {
		supplierRepository.editShippingMethodMapping(updateEvent.getEntity());
		return new UpdatedEvent<Map<String, String>>(0, updateEvent.getEntity());
	}

	@Override
	public ReadCollectionEvent<SupplierShippingMethod> findSupplierShippingMethods(
			RequestReadEvent<Integer> requestReadEvent) {
		List<SupplierShippingMethod> findSupplierShippingMapping = supplierRepository
				.findSupplierShippingMapping(requestReadEvent.getEntity());
		return new ReadCollectionEvent<SupplierShippingMethod>(
				findSupplierShippingMapping);
	}

	@Override
	public ReadCollectionEvent<VendorShippingMap> findVendorShippingMethods(
			RequestReadEvent<Integer> requestReadEvent) {
		List<VendorShippingMap> findVendorShippingMethods = supplierRepository
				.findVendorShippingMapping(requestReadEvent.getEntity());
		return new ReadCollectionEvent<VendorShippingMap>(
				findVendorShippingMethods);
	}

	@Override
	public ReadCollectionEvent<ShippingCarrier> findSupplierShippingCarriers(
			RequestReadEvent<Integer> requestReadEvent) {
		List<ShippingCarrier> findVendorShippingMethods = supplierRepository
				.findVendorShippingCarrier(requestReadEvent.getEntity());
		return new ReadCollectionEvent<ShippingCarrier>(
				findVendorShippingMethods);
	}

	@Override
	public ReadCollectionEvent<SupplierShippingMethod> findChannelShippingForSupplierCarrier(
			RequestReadEvent<Integer[]> requestReadEvent) {
		List<SupplierShippingMethod> findSupplierShippingMethod = supplierRepository
				.findChannelShippingForSupplierCarrier(
						requestReadEvent.getEntity()[0],
						requestReadEvent.getEntity()[1]);
		return new ReadCollectionEvent<SupplierShippingMethod>(
				findSupplierShippingMethod);
	}

	@Override
	public ReadCollectionEvent<SupplierShippingMethod> saveShippingOverrideForSupplierMethod(
			RequestReadEvent<Object[]> requestReadEvent) {
		List<SupplierShippingMethod> findSupplierShippingMethod = supplierRepository
				.saveShippingOverrideForSupplierMethod(
						(Integer) requestReadEvent.getEntity()[0],
						(String) requestReadEvent.getEntity()[1]);
		return new ReadCollectionEvent<SupplierShippingMethod>(
				findSupplierShippingMethod);
	}

	@Override
	public DeletedEvent<Integer> deleteShippingOverrideForSupplierMethod(
			DeleteEvent deleteEvent) {
		supplierRepository.deleteShippingOverrideForSupplierMethod(deleteEvent
				.getId());
		return new DeletedEvent<Integer>(deleteEvent.getId());
	}

	@Override
	public ReadCollectionEvent<ShippingMethod> getShippingMethods(
			RequestReadEvent<ShippingMethod> requestReadEvent) {
		List<ShippingMethod> entities = supplierRepository.getShippingMethods();

		return new ReadCollectionEvent<ShippingMethod>(entities);
	}
}
