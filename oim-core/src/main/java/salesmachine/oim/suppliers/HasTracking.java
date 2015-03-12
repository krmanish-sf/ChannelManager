package salesmachine.oim.suppliers;

import salesmachine.hibernatedb.OimVendorSuppliers;

public interface HasTracking {

	String getOrderStatus(OimVendorSuppliers oimVendorSuppliers,
			Object trackingMeta);
}
