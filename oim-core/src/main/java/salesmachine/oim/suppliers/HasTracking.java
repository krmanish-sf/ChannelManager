package salesmachine.oim.suppliers;

import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.oim.suppliers.exception.SupplierOrderTrackingException;
import salesmachine.oim.suppliers.modal.OrderStatus;

public interface HasTracking {

	OrderStatus getOrderStatus(OimVendorSuppliers oimVendorSuppliers,
			Object trackingMeta,OimOrderDetails oimOrderDetails) throws SupplierOrderTrackingException;
}
