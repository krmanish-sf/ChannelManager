package com.is.cm.core.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.beans.BeanUtils;

import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.util.StringHandle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
// @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include =
// JsonTypeInfo.As.PROPERTY, property = "@class")
public class Order extends DomainBase implements Serializable {
	private static final long serialVersionUID = 8734585796616539486L;
	private Integer orderId;
	private OrderBatch oimOrderBatches;
	private Date orderTm;
	private Date orderFetchTm;
	private Double orderTotalAmount;
	private Date insertionTm;
	private Date deleteTm;
	private String deliveryName;
	private String deliveryStreetAddress;
	private String deliverySuburb;
	private String deliveryCity;
	private String deliveryState;
	private String deliveryStateCode;
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
	private String orderComment;
	@JsonDeserialize(as = HashSet.class)
	private Set<OrderDetail> oimOrderDetailses = new HashSet<OrderDetail>(0);
	private ShippingMethod shippingMethod;

	public Order() {
	}

	public Integer getOrderId() {
		return this.orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	@JsonInclude
	public OrderBatch getOimOrderBatches() {
		return this.oimOrderBatches;
	}

	@JsonInclude
	public void setOimOrderBatches(OrderBatch oimOrderBatches) {
		this.oimOrderBatches = oimOrderBatches;
	}

	public Date getOrderTm() {
		return this.orderTm;
	}

	public void setOrderTm(Date orderTm) {
		this.orderTm = orderTm;
	}

	public Date getOrderFetchTm() {
		return this.orderFetchTm;
	}

	public void setOrderFetchTm(Date orderFetchTm) {
		this.orderFetchTm = orderFetchTm;
	}

	public Double getOrderTotalAmount() {
		return this.orderTotalAmount;
	}

	public void setOrderTotalAmount(Double orderTotalAmount) {
		this.orderTotalAmount = orderTotalAmount;
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

	public String getOrderComment() {
		return this.orderComment;
	}

	public void setOrderComment(String orderComment) {
		this.orderComment = orderComment;
	}

	public Set<OrderDetail> getOimOrderDetailses() {
		return this.oimOrderDetailses;
	}

	public void setOimOrderDetailses(Set<OrderDetail> oimOrderDetailses) {
		this.oimOrderDetailses = oimOrderDetailses;
	}

	public static Order from(OimOrders oimorder) {
		if (oimorder == null)
			return null;
		Order order = new Order();
		BeanUtils.copyProperties(oimorder, order, new String[] {
				"oimOrderBatches", "oimOrderDetailses" });
		order.oimOrderBatches = OrderBatch.from(oimorder.getOimOrderBatches());
		if (oimorder.getOimOrderDetailses() != null) {
			Iterator<OimOrderDetails> odIter = oimorder.getOimOrderDetailses()
					.iterator();
			Set<OrderDetail> details = new HashSet<OrderDetail>();
			while (odIter.hasNext()) {
				OimOrderDetails od = (OimOrderDetails) odIter.next();
				details.add(OrderDetail.from(od));
				od.getCostPrice();
			}
			order.setOimOrderDetailses(details);
		}
		if (oimorder.getOimShippingMethod() != null) {
			order.setShippingMethod(ShippingMethod.from(oimorder
					.getOimShippingMethod()));
		}
		return order;
	}

	public OimOrders toOimOrder() {
		OimOrders oimorder = new OimOrders();
		BeanUtils.copyProperties(this, oimorder, new String[] {
				"oimOrderBatches", "oimOrderDetailses", "shippingMethod" });
		oimorder.setOimOrderBatches(this.oimOrderBatches.toOimOrderBatches());
		if (this.shippingMethod != null)
			oimorder.setOimShippingMethod(this.shippingMethod
					.toOimShippingMethod());
		return oimorder;
	}

	public ShippingMethod getShippingMethod() {
		return shippingMethod;
	}

	public void setShippingMethod(ShippingMethod shippingMethod) {
		this.shippingMethod = shippingMethod;
	}

	public String getDeliveryStateCode() {
		return deliveryStateCode;
	}

	public void setDeliveryStateCode(String deliveryStateCode) {
		this.deliveryStateCode = deliveryStateCode;
	}

	public String getShippingAddress() {
		return String.format("%s %s %s %s %s %s",
				StringHandle.removeNull(deliveryStreetAddress),
				StringHandle.removeNull(deliverySuburb),
				StringHandle.removeNull(deliveryCity),
				StringHandle.removeNull(deliveryStateCode),
				StringHandle.removeNull(deliveryZip),
				StringHandle.removeNull(deliveryCountry));
	}

}
