package com.is.cm.core.domain;

import salesmachine.hibernatedb.OimVendorShippingMap;

@Deprecated
public class VendorShippingMap extends DomainBase {
	private static final long serialVersionUID = -4624607688135290928L;
	private Integer id;
	private Supplier oimSuppliers;
	private Vendor vendors;
	private SupplierShippingMethod oimShippingMethod;
	private String shippingText;

	public VendorShippingMap() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Supplier getOimSuppliers() {
		return this.oimSuppliers;
	}

	public void setOimSuppliers(Supplier oimSuppliers) {
		this.oimSuppliers = oimSuppliers;
	}

	public Vendor getVendors() {
		return this.vendors;
	}

	public void setVendors(Vendor vendors) {
		this.vendors = vendors;
	}

	public SupplierShippingMethod getOimShippingMethod() {
		return this.oimShippingMethod;
	}

	public void setOimShippingMethod(SupplierShippingMethod oimShippingMethod) {
		this.oimShippingMethod = oimShippingMethod;
	}

	public String getShippingText() {
		return this.shippingText;
	}

	public void setShippingText(String shippingText) {
		this.shippingText = shippingText;
	}

	public static VendorShippingMap from(OimVendorShippingMap map) {
		VendorShippingMap target = new VendorShippingMap();
		target.setId(map.getId());
		target.setShippingText(map.getShippingText());
		// target.oimSuppliers = Supplier.from(map.getOimSuppliers());
		// target.vendors = Vendor.from(map.getVendors());
		// target.oimShippingMethod =
		// SupplierShippingMethod.from(map.getOimShippingMethod());
		return target;
	}
}
