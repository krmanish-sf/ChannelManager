package com.is.cm.core.domain;

import java.util.Date;

import org.springframework.beans.BeanUtils;

import salesmachine.hibernatedb.OimOrderDetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDetail extends DomainBase implements java.io.Serializable {
	private static final long serialVersionUID = -1340817164499200616L;
	private Integer detailId;
	private OrderStatus oimOrderStatuses;
	private Order oimOrders;
	private Supplier oimSuppliers;
	private String sku;
	private Double costPrice;
	private Double salePrice;
	private Date processingTm;
	private Date insertionTm;
	private Date deleteTm;
	private Integer quantity;
	private String productName;
	private String productDesc;

	public OrderDetail() {
	}

	public Integer getDetailId() {
		return this.detailId;
	}

	public void setDetailId(Integer detailId) {
		this.detailId = detailId;
	}

	public OrderStatus getOimOrderStatuses() {
		return this.oimOrderStatuses;
	}

	public void setOimOrderStatuses(OrderStatus oimOrderStatuses) {
		this.oimOrderStatuses = oimOrderStatuses;
	}

	public Order getOimOrders() {
		return this.oimOrders;
	}

	public void setOimOrders(Order oimOrders) {
		this.oimOrders = oimOrders;
	}

	public Supplier getOimSuppliers() {
		return this.oimSuppliers;
	}

	public void setOimSuppliers(Supplier oimSuppliers) {
		this.oimSuppliers = oimSuppliers;
	}

	public String getSku() {
		return this.sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
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

	public Date getProcessingTm() {
		return this.processingTm;
	}

	public void setProcessingTm(Date processingTm) {
		this.processingTm = processingTm;
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

	public Integer getQuantity() {
		return this.quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public String getProductName() {
		return this.productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductDesc() {
		return this.productDesc;
	}

	public void setProductDesc(String productDesc) {
		this.productDesc = productDesc;
	}

	public static OrderDetail from(OimOrderDetails oimOrderDetails) {
		OrderDetail detail = new OrderDetail();
		BeanUtils.copyProperties(oimOrderDetails, detail, new String[] {
				"oimSuppliers", "oimOrderStatuses", "oimOrders" });
		detail.oimOrderStatuses = OrderStatus.from(oimOrderDetails
				.getOimOrderStatuses());
		detail.oimSuppliers = Supplier.from(oimOrderDetails.getOimSuppliers());
		return detail;
	}

	public OimOrderDetails toOimOrderDetails() {
		OimOrderDetails target = new OimOrderDetails();
		BeanUtils.copyProperties(this, target, new String[] { "oimSuppliers",
				"oimOrderStatuses", "oimOrders" });
		target.setOimOrderStatuses(oimOrderStatuses.toOimOrderStatus());
		if (this.oimOrders != null)
			target.setOimOrders(this.oimOrders.toOimOrder());
		return target;
	}

}
