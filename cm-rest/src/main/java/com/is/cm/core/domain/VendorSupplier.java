package com.is.cm.core.domain;

import java.util.Date;

import org.springframework.beans.BeanUtils;

import salesmachine.hibernatedb.OimVendorSuppliers;

public class VendorSupplier extends DomainBase {
	private static final long serialVersionUID = -8314764679810090637L;
	private Integer vendorSupplierId;
	private Vendor vendors;
	private Supplier oimSuppliers;
	private String accountNumber;
	private String login;
	private String password;
	private String defShippingMethodCode;
	private Date insertionTm;
	private Date deleteTm;
	private Integer testMode;

	public VendorSupplier() {
	}

	public Integer getVendorSupplierId() {
		return this.vendorSupplierId;
	}

	public void setVendorSupplierId(Integer vendorSupplierId) {
		this.vendorSupplierId = vendorSupplierId;
	}

	public Vendor getVendors() {
		return this.vendors;
	}

	public void setVendors(Vendor vendors) {
		this.vendors = vendors;
	}

	public Supplier getOimSuppliers() {
		return this.oimSuppliers;
	}

	public void setOimSuppliers(Supplier oimSuppliers) {
		this.oimSuppliers = oimSuppliers;
	}

	public String getAccountNumber() {
		return this.accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getLogin() {
		return this.login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDefShippingMethodCode() {
		return this.defShippingMethodCode;
	}

	public void setDefShippingMethodCode(String defShippingMethodCode) {
		this.defShippingMethodCode = defShippingMethodCode;
	}

	public Date getInsertionTm() {
		return this.insertionTm;
	}

	public void setInsertionTm(Date insertionTm) {
		this.insertionTm = insertionTm;
	}

	public Date getDeleteTm() {
		return this.deleteTm;
	}

	public void setDeleteTm(Date deleteTm) {
		this.deleteTm = deleteTm;
	}

	public Integer getTestMode() {
		return this.testMode;
	}

	public void setTestMode(Integer testMode) {
		this.testMode = testMode;
	}

	public static VendorSupplier from(OimVendorSuppliers oimVendorSupplier) {
		VendorSupplier vendorSupplier = new VendorSupplier();
		BeanUtils.copyProperties(oimVendorSupplier, vendorSupplier,
				new String[] { "vendors", "oimSuppliers" });
		if (oimVendorSupplier.getVendors() != null)
			vendorSupplier.setVendors(Vendor.from(oimVendorSupplier
					.getVendors()));
		if (oimVendorSupplier.getOimSuppliers() != null) {
			vendorSupplier.setOimSuppliers(Supplier.from(oimVendorSupplier
					.getOimSuppliers()));
		}
		return vendorSupplier;
	}

	public OimVendorSuppliers toOimVendorSupplier() {
		OimVendorSuppliers oimVendorSuppliers = new OimVendorSuppliers();
		BeanUtils.copyProperties(this, oimVendorSuppliers, new String[] {
				"vendors", "oimSuppliers" });
		if (this.vendors != null) {
			oimVendorSuppliers.setVendors(vendors.toOimVendors());
		}
		if (this.oimSuppliers != null) {
			oimVendorSuppliers.setOimSuppliers(oimSuppliers.toOimSupplier());
		}
		return oimVendorSuppliers;
	}

}
