package com.is.cm.core.event.supplier;

import java.util.List;

import com.is.cm.core.domain.VendorSupplier;
import com.is.cm.core.event.ReadCollectionEvent;

public class AllSuppliersEvent extends ReadCollectionEvent<VendorSupplier> {

	public AllSuppliersEvent(List<VendorSupplier> entities) {
		super(entities);
	}

}
