package salesmachine.oim.suppliers;

import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.oim.suppliers.modal.OrderStatus;

public interface HasTracking {

	OrderStatus getOrderStatus(OimVendorSuppliers oimVendorSuppliers,
			Object trackingMeta);
}
