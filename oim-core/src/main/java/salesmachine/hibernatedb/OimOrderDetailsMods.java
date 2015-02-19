package salesmachine.hibernatedb;

// Generated 15 Feb, 2010 12:50:56 PM by Hibernate Tools 3.2.4.GA

import java.util.Date;

/**
 * OimOrderDetailsMods generated by hbm2java
 */
public class OimOrderDetailsMods implements java.io.Serializable {

	private Integer modId;
	private String operation;
	private Integer detailId;
	private Integer orderId;
	private String sku;
	private Double costPrice;
	private Double salePrice;
	private Integer supplierId;
	private Date processingTm;
	private Date insertionTm;
	private Integer statusId;
	private Integer quantity;

	public OimOrderDetailsMods() {
	}

	public OimOrderDetailsMods(Integer modId) {
		this.modId = modId;
	}

	public OimOrderDetailsMods(Integer modId, String operation,
			Integer detailId, Integer orderId, String sku, Double costPrice,
			Double salePrice, Integer supplierId, Date processingTm,
			Date insertionTm, Integer statusId, Integer quantity) {
		this.modId = modId;
		this.operation = operation;
		this.detailId = detailId;
		this.orderId = orderId;
		this.sku = sku;
		this.costPrice = costPrice;
		this.salePrice = salePrice;
		this.supplierId = supplierId;
		this.processingTm = processingTm;
		this.insertionTm = insertionTm;
		this.statusId = statusId;
		this.quantity = quantity;
	}

	public Integer getModId() {
		return this.modId;
	}

	public void setModId(Integer modId) {
		this.modId = modId;
	}

	public String getOperation() {
		return this.operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public Integer getDetailId() {
		return this.detailId;
	}

	public void setDetailId(Integer detailId) {
		this.detailId = detailId;
	}

	public Integer getOrderId() {
		return this.orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
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

	public Integer getSupplierId() {
		return this.supplierId;
	}

	public void setSupplierId(Integer supplierId) {
		this.supplierId = supplierId;
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

	public Integer getStatusId() {
		return this.statusId;
	}

	public void setStatusId(Integer statusId) {
		this.statusId = statusId;
	}

	public Integer getQuantity() {
		return this.quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

}
