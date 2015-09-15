package com.is.cm.core.domain;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.beans.BeanUtils;

import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderTracking;
import salesmachine.hibernatedb.OimOrders;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderTracking extends DomainBase implements Serializable {

  /**
     * 
     */
  private static final long serialVersionUID = -6678628436545729588L;
  private int orderTrackingId;
  private Date insertionTime;
  private OrderDetail detail;
  private String trackingNumber;
  private String shippingMethod;
  private String shippingCarrier;
  private int shipQuantity;
  private Date shipDate;
  SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss a");

  public int getOrderTrackingId() {
    return orderTrackingId;
  }

  public void setOrderTrackingId(int orderTrackingId) {
    this.orderTrackingId = orderTrackingId;
  }

  public Date getInsertionTime() {
    return insertionTime;
  }

  public void setInsertionTime(Date insertionTime) {
    this.insertionTime = insertionTime;
  }

  public OrderDetail getDetail() {
    return detail;
  }

  public void setDetail(OrderDetail detail) {
    this.detail = detail;
  }

  public String getTrackingNumber() {
    return trackingNumber;
  }

  public void setTrackingNumber(String trackingNumber) {
    this.trackingNumber = trackingNumber;
  }

  public String getShippingMethod() {
    return shippingMethod;
  }

  public void setShippingMethod(String shippingMethod) {
    this.shippingMethod = shippingMethod;
  }

  public String getShippingCarrier() {
    return shippingCarrier;
  }

  public void setShippingCarrier(String shippingCarrier) {
    this.shippingCarrier = shippingCarrier;
  }

  public int getShipQuantity() {
    return shipQuantity;
  }

  public void setShipQuantity(int shipQuantity) {
    this.shipQuantity = shipQuantity;
  }

  public Date getShipDate() {
    return shipDate;
  }

  public void setShipDate(Date shipDate) {
    this.shipDate = shipDate;
  }

  public String getShipDateString() {
    return df.format(getShipDate());
  }

  public static OrderTracking from(OimOrderTracking oimorderTracking) {
    if (oimorderTracking == null)
      return null;
    OrderTracking orderTracking = new OrderTracking();
    BeanUtils.copyProperties(oimorderTracking, orderTracking, new String[] { "detail" });
    orderTracking.detail = OrderDetail.from(oimorderTracking.getDetail());

    return orderTracking;
  }

  public OimOrderTracking toOimOrderTracking() {
    OimOrderTracking oimorderTracking = new OimOrderTracking();
    BeanUtils.copyProperties(this, oimorderTracking, new String[] { "detail" });
    oimorderTracking.setDetail(this.detail.toOimOrderDetails());
    return oimorderTracking;
  }
}
