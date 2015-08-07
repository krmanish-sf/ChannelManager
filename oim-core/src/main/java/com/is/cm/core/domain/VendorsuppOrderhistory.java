package com.is.cm.core.domain;

import java.util.Date;

import salesmachine.hibernatedb.OimVendorsuppOrderhistory;
import salesmachine.util.StringHandle;

public class VendorsuppOrderhistory extends DomainBase {
	private static final long serialVersionUID = 1416831662017531127L;
	private Integer vsoHistoryId;
	private Vendor vendors;
	private Supplier oimSuppliers;
	private Date processingTm;
	private Integer errorCode;
	private String description;
	private int detailId;
	private String storeOrderId = "N/A";

	public VendorsuppOrderhistory(OimVendorsuppOrderhistory source) {
		this.description = StringHandle.removeNull(source.getDescription());
		this.errorCode = source.getErrorCode();
		this.oimSuppliers = Supplier.from(source.getOimSuppliers());
		this.processingTm = source.getProcessingTm();
		this.vendors = Vendor.from(source.getVendors());
		this.vsoHistoryId = source.getVsoHistoryId();
		if (source.getOimOrderDetails() != null) {
			this.detailId = source.getOimOrderDetails().getDetailId();
			this.storeOrderId = source.getOimOrderDetails().getOimOrders()
					.getStoreOrderId();
		}
	}

	public VendorsuppOrderhistory() {
	}

	public Integer getVsoHistoryId() {
		return this.vsoHistoryId;
	}

	public void setVsoHistoryId(Integer vsoHistoryId) {
		this.vsoHistoryId = vsoHistoryId;
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

	public Date getProcessingTm() {
		return this.processingTm;
	}

	public void setProcessingTm(Date processingTm) {
		this.processingTm = processingTm;
	}

	public Integer getErrorCode() {
		return this.errorCode;
	}

	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getDetailId() {
		return detailId;
	}

	public void setDetailId(int detailId) {
		this.detailId = detailId;
	}

	public String getStoreOrderId() {
		return storeOrderId;
	}

	public void setStoreOrderId(String storeOrderId) {
		this.storeOrderId = storeOrderId;
	}

}
