package com.is.cm.core.event.vendorsuppliers;

import com.is.cm.core.event.DeletedEvent;

@Deprecated
public class VendorSupplierDeletedEvent extends
		DeletedEvent<VendorSupplierDeletedEvent> {

	public VendorSupplierDeletedEvent(int id) {
		super(id);
	}

}
