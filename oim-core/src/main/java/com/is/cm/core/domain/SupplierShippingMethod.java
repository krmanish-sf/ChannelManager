package com.is.cm.core.domain;

import java.io.Serializable;

import salesmachine.hibernatedb.OimSupplierShippingMethod;
import salesmachine.util.StringHandle;

public class SupplierShippingMethod implements Serializable {
	private static final long serialVersionUID = -8448436767787388464L;
	private int id;
	private Supplier supplier;
	private ShippingCarrier shippingCarrier;
	private ShippingMethod shippingMethod;
	private String name;
	private String carrierName;
	private boolean isOverride;
	private String overrideMethod;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}

	public ShippingCarrier getShippingCarrier() {
		return shippingCarrier;
	}

	public void setShippingCarrier(ShippingCarrier oimShippingCarrier) {
		this.shippingCarrier = oimShippingCarrier;
	}

	public ShippingMethod getShippingMethod() {
		return shippingMethod;
	}

	public void setShippingMethod(ShippingMethod oimShippingMethod) {
		this.shippingMethod = oimShippingMethod;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static SupplierShippingMethod from(OimSupplierShippingMethod method) {
		SupplierShippingMethod supplierShippingMethod = new SupplierShippingMethod();
		supplierShippingMethod.setId(method.getId());
		supplierShippingMethod.setName(method.getName());
		supplierShippingMethod.setCarrierName(StringHandle.removeNull(method
				.getCarrierName()));
		supplierShippingMethod.setShippingCarrier(ShippingCarrier.from(method
				.getOimShippingCarrier()));
		supplierShippingMethod.setShippingMethod(ShippingMethod.from(method
				.getOimShippingMethod()));
		supplierShippingMethod.isOverride = method.getOverride() != null;
		if (supplierShippingMethod.isOverride)
			supplierShippingMethod.overrideMethod = method.getOverride()
					.getShippingMethod();
		return supplierShippingMethod;
	}

	public boolean isOverride() {
		return isOverride;
	}

	public void setOverride(boolean isOverride) {
		this.isOverride = isOverride;
	}

	public String getOverrideMethod() {
		return overrideMethod;
	}

	public void setOverrideMethod(String overrideMethod) {
		this.overrideMethod = overrideMethod;
	}

	public String getCarrierName() {
		return carrierName;
	}

	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}
}
