package com.is.cm.core.domain;

import java.util.Date;

public class VwOimOrdersId extends DomainBase {
	private static final long serialVersionUID = -3993450501704072634L;
	private Integer vendorId;
	private Integer channelId;
	private Integer supplierId;
	private Integer batchTypeId;
	private String sku;
	private Integer statusId;
	private Date processingTm;
	private Integer quantity;
	private Double orderTotalAmount;
	private String deliveryName;
	private String deliveryStreetAddress;
	private String deliverySuburb;
	private String deliveryCity;
	private String deliveryState;
	private String deliveryCountry;
	private String deliveryZip;
	private String deliveryCompany;
	private String deliveryPhone;
	private String deliveryEmail;
	private String billingName;
	private String billingStreetAddress;
	private String billingSuburb;
	private String billingCity;
	private String billingState;
	private String billingCountry;
	private String billingZip;
	private String billingCompany;
	private String billingPhone;
	private String billingEmail;
	private String customerName;
	private String customerStreetAddress;
	private String customerSuburb;
	private String customerCity;
	private String customerState;
	private String customerCountry;
	private String customerZip;
	private String customerCompany;
	private String customerPhone;
	private String customerEmail;
	private String storeOrderId;
	private String shippingDetails;
	private String payMethod;
	private Double costPrice;
	private Double salePrice;
	private Date creationTm;
	private Integer batchId;
	private Integer orderId;
	private Integer detailId;

	public VwOimOrdersId() {
	}

	public Integer getVendorId() {
		return this.vendorId;
	}

	public void setVendorId(Integer vendorId) {
		this.vendorId = vendorId;
	}

	public Integer getChannelId() {
		return this.channelId;
	}

	public void setChannelId(Integer channelId) {
		this.channelId = channelId;
	}

	public Integer getSupplierId() {
		return this.supplierId;
	}

	public void setSupplierId(Integer supplierId) {
		this.supplierId = supplierId;
	}

	public Integer getBatchTypeId() {
		return this.batchTypeId;
	}

	public void setBatchTypeId(Integer batchTypeId) {
		this.batchTypeId = batchTypeId;
	}

	public String getSku() {
		return this.sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public Integer getStatusId() {
		return this.statusId;
	}

	public void setStatusId(Integer statusId) {
		this.statusId = statusId;
	}

	public Date getProcessingTm() {
		return this.processingTm;
	}

	public void setProcessingTm(Date processingTm) {
		this.processingTm = processingTm;
	}

	public Integer getQuantity() {
		return this.quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Double getOrderTotalAmount() {
		return this.orderTotalAmount;
	}

	public void setOrderTotalAmount(Double orderTotalAmount) {
		this.orderTotalAmount = orderTotalAmount;
	}

	public String getDeliveryName() {
		return this.deliveryName;
	}

	public void setDeliveryName(String deliveryName) {
		this.deliveryName = deliveryName;
	}

	public String getDeliveryStreetAddress() {
		return this.deliveryStreetAddress;
	}

	public void setDeliveryStreetAddress(String deliveryStreetAddress) {
		this.deliveryStreetAddress = deliveryStreetAddress;
	}

	public String getDeliverySuburb() {
		return this.deliverySuburb;
	}

	public void setDeliverySuburb(String deliverySuburb) {
		this.deliverySuburb = deliverySuburb;
	}

	public String getDeliveryCity() {
		return this.deliveryCity;
	}

	public void setDeliveryCity(String deliveryCity) {
		this.deliveryCity = deliveryCity;
	}

	public String getDeliveryState() {
		return this.deliveryState;
	}

	public void setDeliveryState(String deliveryState) {
		this.deliveryState = deliveryState;
	}

	public String getDeliveryCountry() {
		return this.deliveryCountry;
	}

	public void setDeliveryCountry(String deliveryCountry) {
		this.deliveryCountry = deliveryCountry;
	}

	public String getDeliveryZip() {
		return this.deliveryZip;
	}

	public void setDeliveryZip(String deliveryZip) {
		this.deliveryZip = deliveryZip;
	}

	public String getDeliveryCompany() {
		return this.deliveryCompany;
	}

	public void setDeliveryCompany(String deliveryCompany) {
		this.deliveryCompany = deliveryCompany;
	}

	public String getDeliveryPhone() {
		return this.deliveryPhone;
	}

	public void setDeliveryPhone(String deliveryPhone) {
		this.deliveryPhone = deliveryPhone;
	}

	public String getDeliveryEmail() {
		return this.deliveryEmail;
	}

	public void setDeliveryEmail(String deliveryEmail) {
		this.deliveryEmail = deliveryEmail;
	}

	public String getBillingName() {
		return this.billingName;
	}

	public void setBillingName(String billingName) {
		this.billingName = billingName;
	}

	public String getBillingStreetAddress() {
		return this.billingStreetAddress;
	}

	public void setBillingStreetAddress(String billingStreetAddress) {
		this.billingStreetAddress = billingStreetAddress;
	}

	public String getBillingSuburb() {
		return this.billingSuburb;
	}

	public void setBillingSuburb(String billingSuburb) {
		this.billingSuburb = billingSuburb;
	}

	public String getBillingCity() {
		return this.billingCity;
	}

	public void setBillingCity(String billingCity) {
		this.billingCity = billingCity;
	}

	public String getBillingState() {
		return this.billingState;
	}

	public void setBillingState(String billingState) {
		this.billingState = billingState;
	}

	public String getBillingCountry() {
		return this.billingCountry;
	}

	public void setBillingCountry(String billingCountry) {
		this.billingCountry = billingCountry;
	}

	public String getBillingZip() {
		return this.billingZip;
	}

	public void setBillingZip(String billingZip) {
		this.billingZip = billingZip;
	}

	public String getBillingCompany() {
		return this.billingCompany;
	}

	public void setBillingCompany(String billingCompany) {
		this.billingCompany = billingCompany;
	}

	public String getBillingPhone() {
		return this.billingPhone;
	}

	public void setBillingPhone(String billingPhone) {
		this.billingPhone = billingPhone;
	}

	public String getBillingEmail() {
		return this.billingEmail;
	}

	public void setBillingEmail(String billingEmail) {
		this.billingEmail = billingEmail;
	}

	public String getCustomerName() {
		return this.customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getCustomerStreetAddress() {
		return this.customerStreetAddress;
	}

	public void setCustomerStreetAddress(String customerStreetAddress) {
		this.customerStreetAddress = customerStreetAddress;
	}

	public String getCustomerSuburb() {
		return this.customerSuburb;
	}

	public void setCustomerSuburb(String customerSuburb) {
		this.customerSuburb = customerSuburb;
	}

	public String getCustomerCity() {
		return this.customerCity;
	}

	public void setCustomerCity(String customerCity) {
		this.customerCity = customerCity;
	}

	public String getCustomerState() {
		return this.customerState;
	}

	public void setCustomerState(String customerState) {
		this.customerState = customerState;
	}

	public String getCustomerCountry() {
		return this.customerCountry;
	}

	public void setCustomerCountry(String customerCountry) {
		this.customerCountry = customerCountry;
	}

	public String getCustomerZip() {
		return this.customerZip;
	}

	public void setCustomerZip(String customerZip) {
		this.customerZip = customerZip;
	}

	public String getCustomerCompany() {
		return this.customerCompany;
	}

	public void setCustomerCompany(String customerCompany) {
		this.customerCompany = customerCompany;
	}

	public String getCustomerPhone() {
		return this.customerPhone;
	}

	public void setCustomerPhone(String customerPhone) {
		this.customerPhone = customerPhone;
	}

	public String getCustomerEmail() {
		return this.customerEmail;
	}

	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}

	public String getStoreOrderId() {
		return this.storeOrderId;
	}

	public void setStoreOrderId(String storeOrderId) {
		this.storeOrderId = storeOrderId;
	}

	public String getShippingDetails() {
		return this.shippingDetails;
	}

	public void setShippingDetails(String shippingDetails) {
		this.shippingDetails = shippingDetails;
	}

	public String getPayMethod() {
		return this.payMethod;
	}

	public void setPayMethod(String payMethod) {
		this.payMethod = payMethod;
	}

	public Double getCostPrice() {
		return this.costPrice;
	}

	public void setCostPrice(Double costPrice) {
		this.costPrice = costPrice;
	}

	public Double getSalePrice() {
		return this.salePrice;
	}

	public void setSalePrice(Double salePrice) {
		this.salePrice = salePrice;
	}

	public Date getCreationTm() {
		return this.creationTm;
	}

	public void setCreationTm(Date creationTm) {
		this.creationTm = creationTm;
	}

	public Integer getBatchId() {
		return this.batchId;
	}

	public void setBatchId(Integer batchId) {
		this.batchId = batchId;
	}

	public Integer getOrderId() {
		return this.orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public Integer getDetailId() {
		return this.detailId;
	}

	public void setDetailId(Integer detailId) {
		this.detailId = detailId;
	}

	@Override
	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof VwOimOrdersId))
			return false;
		VwOimOrdersId castOther = (VwOimOrdersId) other;

		return ((this.getVendorId() == castOther.getVendorId()) || (this
				.getVendorId() != null && castOther.getVendorId() != null && this
				.getVendorId().equals(castOther.getVendorId())))
				&& ((this.getChannelId() == castOther.getChannelId()) || (this
						.getChannelId() != null
						&& castOther.getChannelId() != null && this
						.getChannelId().equals(castOther.getChannelId())))
				&& ((this.getSupplierId() == castOther.getSupplierId()) || (this
						.getSupplierId() != null
						&& castOther.getSupplierId() != null && this
						.getSupplierId().equals(castOther.getSupplierId())))
				&& ((this.getBatchTypeId() == castOther.getBatchTypeId()) || (this
						.getBatchTypeId() != null
						&& castOther.getBatchTypeId() != null && this
						.getBatchTypeId().equals(castOther.getBatchTypeId())))
				&& ((this.getSku() == castOther.getSku()) || (this.getSku() != null
						&& castOther.getSku() != null && this.getSku().equals(
						castOther.getSku())))
				&& ((this.getStatusId() == castOther.getStatusId()) || (this
						.getStatusId() != null
						&& castOther.getStatusId() != null && this
						.getStatusId().equals(castOther.getStatusId())))
				&& ((this.getProcessingTm() == castOther.getProcessingTm()) || (this
						.getProcessingTm() != null
						&& castOther.getProcessingTm() != null && this
						.getProcessingTm().equals(castOther.getProcessingTm())))
				&& ((this.getQuantity() == castOther.getQuantity()) || (this
						.getQuantity() != null
						&& castOther.getQuantity() != null && this
						.getQuantity().equals(castOther.getQuantity())))
				&& ((this.getOrderTotalAmount() == castOther
						.getOrderTotalAmount()) || (this.getOrderTotalAmount() != null
						&& castOther.getOrderTotalAmount() != null && this
						.getOrderTotalAmount().equals(
								castOther.getOrderTotalAmount())))
				&& ((this.getDeliveryName() == castOther.getDeliveryName()) || (this
						.getDeliveryName() != null
						&& castOther.getDeliveryName() != null && this
						.getDeliveryName().equals(castOther.getDeliveryName())))
				&& ((this.getDeliveryStreetAddress() == castOther
						.getDeliveryStreetAddress()) || (this
						.getDeliveryStreetAddress() != null
						&& castOther.getDeliveryStreetAddress() != null && this
						.getDeliveryStreetAddress().equals(
								castOther.getDeliveryStreetAddress())))
				&& ((this.getDeliverySuburb() == castOther.getDeliverySuburb()) || (this
						.getDeliverySuburb() != null
						&& castOther.getDeliverySuburb() != null && this
						.getDeliverySuburb().equals(
								castOther.getDeliverySuburb())))
				&& ((this.getDeliveryCity() == castOther.getDeliveryCity()) || (this
						.getDeliveryCity() != null
						&& castOther.getDeliveryCity() != null && this
						.getDeliveryCity().equals(castOther.getDeliveryCity())))
				&& ((this.getDeliveryState() == castOther.getDeliveryState()) || (this
						.getDeliveryState() != null
						&& castOther.getDeliveryState() != null && this
						.getDeliveryState()
						.equals(castOther.getDeliveryState())))
				&& ((this.getDeliveryCountry() == castOther
						.getDeliveryCountry()) || (this.getDeliveryCountry() != null
						&& castOther.getDeliveryCountry() != null && this
						.getDeliveryCountry().equals(
								castOther.getDeliveryCountry())))
				&& ((this.getDeliveryZip() == castOther.getDeliveryZip()) || (this
						.getDeliveryZip() != null
						&& castOther.getDeliveryZip() != null && this
						.getDeliveryZip().equals(castOther.getDeliveryZip())))
				&& ((this.getDeliveryCompany() == castOther
						.getDeliveryCompany()) || (this.getDeliveryCompany() != null
						&& castOther.getDeliveryCompany() != null && this
						.getDeliveryCompany().equals(
								castOther.getDeliveryCompany())))
				&& ((this.getDeliveryPhone() == castOther.getDeliveryPhone()) || (this
						.getDeliveryPhone() != null
						&& castOther.getDeliveryPhone() != null && this
						.getDeliveryPhone()
						.equals(castOther.getDeliveryPhone())))
				&& ((this.getDeliveryEmail() == castOther.getDeliveryEmail()) || (this
						.getDeliveryEmail() != null
						&& castOther.getDeliveryEmail() != null && this
						.getDeliveryEmail()
						.equals(castOther.getDeliveryEmail())))
				&& ((this.getBillingName() == castOther.getBillingName()) || (this
						.getBillingName() != null
						&& castOther.getBillingName() != null && this
						.getBillingName().equals(castOther.getBillingName())))
				&& ((this.getBillingStreetAddress() == castOther
						.getBillingStreetAddress()) || (this
						.getBillingStreetAddress() != null
						&& castOther.getBillingStreetAddress() != null && this
						.getBillingStreetAddress().equals(
								castOther.getBillingStreetAddress())))
				&& ((this.getBillingSuburb() == castOther.getBillingSuburb()) || (this
						.getBillingSuburb() != null
						&& castOther.getBillingSuburb() != null && this
						.getBillingSuburb()
						.equals(castOther.getBillingSuburb())))
				&& ((this.getBillingCity() == castOther.getBillingCity()) || (this
						.getBillingCity() != null
						&& castOther.getBillingCity() != null && this
						.getBillingCity().equals(castOther.getBillingCity())))
				&& ((this.getBillingState() == castOther.getBillingState()) || (this
						.getBillingState() != null
						&& castOther.getBillingState() != null && this
						.getBillingState().equals(castOther.getBillingState())))
				&& ((this.getBillingCountry() == castOther.getBillingCountry()) || (this
						.getBillingCountry() != null
						&& castOther.getBillingCountry() != null && this
						.getBillingCountry().equals(
								castOther.getBillingCountry())))
				&& ((this.getBillingZip() == castOther.getBillingZip()) || (this
						.getBillingZip() != null
						&& castOther.getBillingZip() != null && this
						.getBillingZip().equals(castOther.getBillingZip())))
				&& ((this.getBillingCompany() == castOther.getBillingCompany()) || (this
						.getBillingCompany() != null
						&& castOther.getBillingCompany() != null && this
						.getBillingCompany().equals(
								castOther.getBillingCompany())))
				&& ((this.getBillingPhone() == castOther.getBillingPhone()) || (this
						.getBillingPhone() != null
						&& castOther.getBillingPhone() != null && this
						.getBillingPhone().equals(castOther.getBillingPhone())))
				&& ((this.getBillingEmail() == castOther.getBillingEmail()) || (this
						.getBillingEmail() != null
						&& castOther.getBillingEmail() != null && this
						.getBillingEmail().equals(castOther.getBillingEmail())))
				&& ((this.getCustomerName() == castOther.getCustomerName()) || (this
						.getCustomerName() != null
						&& castOther.getCustomerName() != null && this
						.getCustomerName().equals(castOther.getCustomerName())))
				&& ((this.getCustomerStreetAddress() == castOther
						.getCustomerStreetAddress()) || (this
						.getCustomerStreetAddress() != null
						&& castOther.getCustomerStreetAddress() != null && this
						.getCustomerStreetAddress().equals(
								castOther.getCustomerStreetAddress())))
				&& ((this.getCustomerSuburb() == castOther.getCustomerSuburb()) || (this
						.getCustomerSuburb() != null
						&& castOther.getCustomerSuburb() != null && this
						.getCustomerSuburb().equals(
								castOther.getCustomerSuburb())))
				&& ((this.getCustomerCity() == castOther.getCustomerCity()) || (this
						.getCustomerCity() != null
						&& castOther.getCustomerCity() != null && this
						.getCustomerCity().equals(castOther.getCustomerCity())))
				&& ((this.getCustomerState() == castOther.getCustomerState()) || (this
						.getCustomerState() != null
						&& castOther.getCustomerState() != null && this
						.getCustomerState()
						.equals(castOther.getCustomerState())))
				&& ((this.getCustomerCountry() == castOther
						.getCustomerCountry()) || (this.getCustomerCountry() != null
						&& castOther.getCustomerCountry() != null && this
						.getCustomerCountry().equals(
								castOther.getCustomerCountry())))
				&& ((this.getCustomerZip() == castOther.getCustomerZip()) || (this
						.getCustomerZip() != null
						&& castOther.getCustomerZip() != null && this
						.getCustomerZip().equals(castOther.getCustomerZip())))
				&& ((this.getCustomerCompany() == castOther
						.getCustomerCompany()) || (this.getCustomerCompany() != null
						&& castOther.getCustomerCompany() != null && this
						.getCustomerCompany().equals(
								castOther.getCustomerCompany())))
				&& ((this.getCustomerPhone() == castOther.getCustomerPhone()) || (this
						.getCustomerPhone() != null
						&& castOther.getCustomerPhone() != null && this
						.getCustomerPhone()
						.equals(castOther.getCustomerPhone())))
				&& ((this.getCustomerEmail() == castOther.getCustomerEmail()) || (this
						.getCustomerEmail() != null
						&& castOther.getCustomerEmail() != null && this
						.getCustomerEmail()
						.equals(castOther.getCustomerEmail())))
				&& ((this.getStoreOrderId() == castOther.getStoreOrderId()) || (this
						.getStoreOrderId() != null
						&& castOther.getStoreOrderId() != null && this
						.getStoreOrderId().equals(castOther.getStoreOrderId())))
				&& ((this.getShippingDetails() == castOther
						.getShippingDetails()) || (this.getShippingDetails() != null
						&& castOther.getShippingDetails() != null && this
						.getShippingDetails().equals(
								castOther.getShippingDetails())))
				&& ((this.getPayMethod() == castOther.getPayMethod()) || (this
						.getPayMethod() != null
						&& castOther.getPayMethod() != null && this
						.getPayMethod().equals(castOther.getPayMethod())))
				&& ((this.getCostPrice() == castOther.getCostPrice()) || (this
						.getCostPrice() != null
						&& castOther.getCostPrice() != null && this
						.getCostPrice().equals(castOther.getCostPrice())))
				&& ((this.getSalePrice() == castOther.getSalePrice()) || (this
						.getSalePrice() != null
						&& castOther.getSalePrice() != null && this
						.getSalePrice().equals(castOther.getSalePrice())))
				&& ((this.getCreationTm() == castOther.getCreationTm()) || (this
						.getCreationTm() != null
						&& castOther.getCreationTm() != null && this
						.getCreationTm().equals(castOther.getCreationTm())))
				&& ((this.getBatchId() == castOther.getBatchId()) || (this
						.getBatchId() != null && castOther.getBatchId() != null && this
						.getBatchId().equals(castOther.getBatchId())))
				&& ((this.getOrderId() == castOther.getOrderId()) || (this
						.getOrderId() != null && castOther.getOrderId() != null && this
						.getOrderId().equals(castOther.getOrderId())))
				&& ((this.getDetailId() == castOther.getDetailId()) || (this
						.getDetailId() != null
						&& castOther.getDetailId() != null && this
						.getDetailId().equals(castOther.getDetailId())));
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getVendorId() == null ? 0 : this.getVendorId().hashCode());
		result = 37 * result
				+ (getChannelId() == null ? 0 : this.getChannelId().hashCode());
		result = 37
				* result
				+ (getSupplierId() == null ? 0 : this.getSupplierId()
						.hashCode());
		result = 37
				* result
				+ (getBatchTypeId() == null ? 0 : this.getBatchTypeId()
						.hashCode());
		result = 37 * result
				+ (getSku() == null ? 0 : this.getSku().hashCode());
		result = 37 * result
				+ (getStatusId() == null ? 0 : this.getStatusId().hashCode());
		result = 37
				* result
				+ (getProcessingTm() == null ? 0 : this.getProcessingTm()
						.hashCode());
		result = 37 * result
				+ (getQuantity() == null ? 0 : this.getQuantity().hashCode());
		result = 37
				* result
				+ (getOrderTotalAmount() == null ? 0 : this
						.getOrderTotalAmount().hashCode());
		result = 37
				* result
				+ (getDeliveryName() == null ? 0 : this.getDeliveryName()
						.hashCode());
		result = 37
				* result
				+ (getDeliveryStreetAddress() == null ? 0 : this
						.getDeliveryStreetAddress().hashCode());
		result = 37
				* result
				+ (getDeliverySuburb() == null ? 0 : this.getDeliverySuburb()
						.hashCode());
		result = 37
				* result
				+ (getDeliveryCity() == null ? 0 : this.getDeliveryCity()
						.hashCode());
		result = 37
				* result
				+ (getDeliveryState() == null ? 0 : this.getDeliveryState()
						.hashCode());
		result = 37
				* result
				+ (getDeliveryCountry() == null ? 0 : this.getDeliveryCountry()
						.hashCode());
		result = 37
				* result
				+ (getDeliveryZip() == null ? 0 : this.getDeliveryZip()
						.hashCode());
		result = 37
				* result
				+ (getDeliveryCompany() == null ? 0 : this.getDeliveryCompany()
						.hashCode());
		result = 37
				* result
				+ (getDeliveryPhone() == null ? 0 : this.getDeliveryPhone()
						.hashCode());
		result = 37
				* result
				+ (getDeliveryEmail() == null ? 0 : this.getDeliveryEmail()
						.hashCode());
		result = 37
				* result
				+ (getBillingName() == null ? 0 : this.getBillingName()
						.hashCode());
		result = 37
				* result
				+ (getBillingStreetAddress() == null ? 0 : this
						.getBillingStreetAddress().hashCode());
		result = 37
				* result
				+ (getBillingSuburb() == null ? 0 : this.getBillingSuburb()
						.hashCode());
		result = 37
				* result
				+ (getBillingCity() == null ? 0 : this.getBillingCity()
						.hashCode());
		result = 37
				* result
				+ (getBillingState() == null ? 0 : this.getBillingState()
						.hashCode());
		result = 37
				* result
				+ (getBillingCountry() == null ? 0 : this.getBillingCountry()
						.hashCode());
		result = 37
				* result
				+ (getBillingZip() == null ? 0 : this.getBillingZip()
						.hashCode());
		result = 37
				* result
				+ (getBillingCompany() == null ? 0 : this.getBillingCompany()
						.hashCode());
		result = 37
				* result
				+ (getBillingPhone() == null ? 0 : this.getBillingPhone()
						.hashCode());
		result = 37
				* result
				+ (getBillingEmail() == null ? 0 : this.getBillingEmail()
						.hashCode());
		result = 37
				* result
				+ (getCustomerName() == null ? 0 : this.getCustomerName()
						.hashCode());
		result = 37
				* result
				+ (getCustomerStreetAddress() == null ? 0 : this
						.getCustomerStreetAddress().hashCode());
		result = 37
				* result
				+ (getCustomerSuburb() == null ? 0 : this.getCustomerSuburb()
						.hashCode());
		result = 37
				* result
				+ (getCustomerCity() == null ? 0 : this.getCustomerCity()
						.hashCode());
		result = 37
				* result
				+ (getCustomerState() == null ? 0 : this.getCustomerState()
						.hashCode());
		result = 37
				* result
				+ (getCustomerCountry() == null ? 0 : this.getCustomerCountry()
						.hashCode());
		result = 37
				* result
				+ (getCustomerZip() == null ? 0 : this.getCustomerZip()
						.hashCode());
		result = 37
				* result
				+ (getCustomerCompany() == null ? 0 : this.getCustomerCompany()
						.hashCode());
		result = 37
				* result
				+ (getCustomerPhone() == null ? 0 : this.getCustomerPhone()
						.hashCode());
		result = 37
				* result
				+ (getCustomerEmail() == null ? 0 : this.getCustomerEmail()
						.hashCode());
		result = 37
				* result
				+ (getStoreOrderId() == null ? 0 : this.getStoreOrderId()
						.hashCode());
		result = 37
				* result
				+ (getShippingDetails() == null ? 0 : this.getShippingDetails()
						.hashCode());
		result = 37 * result
				+ (getPayMethod() == null ? 0 : this.getPayMethod().hashCode());
		result = 37 * result
				+ (getCostPrice() == null ? 0 : this.getCostPrice().hashCode());
		result = 37 * result
				+ (getSalePrice() == null ? 0 : this.getSalePrice().hashCode());
		result = 37
				* result
				+ (getCreationTm() == null ? 0 : this.getCreationTm()
						.hashCode());
		result = 37 * result
				+ (getBatchId() == null ? 0 : this.getBatchId().hashCode());
		result = 37 * result
				+ (getOrderId() == null ? 0 : this.getOrderId().hashCode());
		result = 37 * result
				+ (getDetailId() == null ? 0 : this.getDetailId().hashCode());
		return result;
	}

}
