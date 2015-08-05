package com.is.cm.core.persistance;

import java.util.List;
import java.util.Map;

import com.is.cm.core.domain.ShippingCarrier;
import com.is.cm.core.domain.ShippingMethod;
import com.is.cm.core.domain.Supplier;
import com.is.cm.core.domain.SupplierShippingMethod;
import com.is.cm.core.domain.VendorShippingMap;
import com.is.cm.core.domain.VendorSupplier;

public interface SupplierRepository {
	List<VendorSupplier> findAll();

	VendorSupplier delete(int id);

	VendorSupplier findById(int id);

	VendorSupplier update(int vendorSupplierId,
			Map<String, String> vendorSupplierData);

	VendorSupplier removeSubscription(Integer vendorSupplierId);

	List<Supplier> findUnsubscribed();

	VendorSupplier addSubscription(Integer supplierId, String login,
			String password, String accountno, String defShippingMc,
			Integer testmode);

	VendorSupplier addSubscriptionHG(Integer supplierId, String phi_login,
			String phi_password, String phi_accountno, String phi_ftp,
			String hva_login, String hva_password, String hva_accountno, String hva_ftp, Integer testmode);

	VendorSupplier addCustomSubscription(Map<String, String> map);

	void editShippingMethodMapping(Map<String, String> map);

	@Deprecated
	List<VendorShippingMap> findVendorShippingMapping(int supplierId);

	List<SupplierShippingMethod> findSupplierShippingMapping(int supplierId);

	List<ShippingCarrier> findVendorShippingCarrier(Integer entity);

	List<SupplierShippingMethod> findChannelShippingForSupplierCarrier(
			Integer supplierId, Integer shippingCarrierId);

	void deleteShippingOverrideForSupplierMethod(int id);

	List<SupplierShippingMethod> saveShippingOverrideForSupplierMethod(
			Integer integer, String string);

	List<ShippingMethod> getShippingMethods();

}
