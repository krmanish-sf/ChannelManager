package salesmachine.hibernatedb;

// Generated 30 Mar, 2010 7:26:10 PM by Hibernate Tools 3.2.4.GA

import java.util.Date;

/**
 * OimOrderDetails generated by hbm2java
 */
public class OimOrderDetails implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private Integer detailId;
	private OimOrderStatuses oimOrderStatuses;
	private OimOrders oimOrders;
	private OimSuppliers oimSuppliers;
	private String sku;
	private Double costPrice;
	private Double salePrice;
	private Date processingTm;
	private Date insertionTm;
	private Date deleteTm;
	private Integer quantity;
	private String productName;
	private String productDesc;
	private String supplierOrderStatus;
	private String supplierOrderNumber;
	private String storeOrderItemId;

	public OimOrderDetails() {
	}

	public OimOrderDetails(OimOrderStatuses oimOrderStatuses,
			OimOrders oimOrders, OimSuppliers oimSuppliers, String sku,
			Double costPrice, Double salePrice, Date processingTm,
			Date insertionTm, Date deleteTm, Integer quantity,
			String productName, String productDesc) {
		this.oimOrderStatuses = oimOrderStatuses;
		this.oimOrders = oimOrders;
		this.oimSuppliers = oimSuppliers;
		this.sku = sku;
		this.costPrice = costPrice;
		this.salePrice = salePrice;
		this.processingTm = processingTm;
		this.insertionTm = insertionTm;
		this.deleteTm = deleteTm;
		this.quantity = quantity;
		this.productName = productName;
		this.productDesc = productDesc;
	}

	public Integer getDetailId() {
		return this.detailId;
	}

	public void setDetailId(Integer detailId) {
		this.detailId = detailId;
	}

	public OimOrderStatuses getOimOrderStatuses() {
		return this.oimOrderStatuses;
	}

	public void setOimOrderStatuses(OimOrderStatuses oimOrderStatuses) {
		this.oimOrderStatuses = oimOrderStatuses;
	}

	public OimOrders getOimOrders() {
		return this.oimOrders;
	}

	public void setOimOrders(OimOrders oimOrders) {
		this.oimOrders = oimOrders;
	}

	public OimSuppliers getOimSuppliers() {
		return this.oimSuppliers;
	}

	public void setOimSuppliers(OimSuppliers oimSuppliers) {
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
		if (productName != null && productName.length() > 100)
			productName = productName.substring(0, 99);
		this.productName = productName;
	}

	public String getProductDesc() {
		return this.productDesc;
	}

	public void setProductDesc(String productDesc) {
		if (productDesc != null && productDesc.length() > 500)
			productDesc = productDesc.substring(0, 499);
		this.productDesc = productDesc;
	}

	public String getSupplierOrderStatus() {
		return supplierOrderStatus;
	}

	public void setSupplierOrderStatus(String supplierOrderStatus) {
		this.supplierOrderStatus = supplierOrderStatus;
	}

	public String getSupplierOrderNumber() {
		return supplierOrderNumber;
	}

	public void setSupplierOrderNumber(String supplierOrderNumber) {
		this.supplierOrderNumber = supplierOrderNumber;
	}

	public String getStoreOrderItemId() {
		return storeOrderItemId;
	}

	public void setStoreOrderItemId(String storeOrderItemId) {
		this.storeOrderItemId = storeOrderItemId;
	}

}
