package salesmachine.hibernatedb;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "OIM_ORDER_TRACKING")
public class OimOrderTracking implements java.io.Serializable {

  /**
     * 
     */
  private static final long serialVersionUID = 1L;
  private int orderTrackingId;
  private Date insertionTime;
  private OimOrderDetails detail;
  private String trackingNumber;
  private String shippingMethod;
  private String shippingCarrier;
  private int shipQuantity;
  private Date shipDate;

  @Id
  @SequenceGenerator(name = "oimOrderTrackingSequence", sequenceName = "OIM_ORDER_TRACKING_SEQUENCES", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oimOrderTrackingSequence")
  @Column(name = "ORDER_TRACKING_ID", nullable = false)
  public int getOrderTrackingId() {
    return orderTrackingId;
  }

  public void setOrderTrackingId(int orderTrackingId) {
    this.orderTrackingId = orderTrackingId;
  }

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "INSERTION_TM", nullable = true, length = 7)
  public Date getInsertionTime() {
    return insertionTime;
  }

  public void setInsertionTime(Date insertionTime) {
    this.insertionTime = insertionTime;
  }

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "DETAIL_ID", referencedColumnName = "DETAIL_ID")
  public OimOrderDetails getDetail() {
    return detail;
  }

  public OimOrderTracking() {
    super();
  }

  public void setDetail(OimOrderDetails detail) {
    this.detail = detail;
  }

  @Column(name = "TRACKING_NUMBER", nullable = true, length = 50)
  public String getTrackingNumber() {
    return trackingNumber;
  }

  public void setTrackingNumber(String trackingNumber) {
    this.trackingNumber = trackingNumber;
  }

  @Column(name = "SHIPPING_METHOD", nullable = true, length = 50)
  public String getShippingMethod() {
    return shippingMethod;
  }

  public void setShippingMethod(String shippingMethod) {
    this.shippingMethod = shippingMethod;
  }

  @Column(name = "SHIPPING_CARRIER", nullable = true, length = 50)
  public String getShippingCarrier() {
    return shippingCarrier;
  }

  public void setShippingCarrier(String shippingCarrier) {
    this.shippingCarrier = shippingCarrier;
  }

  @Column(name = "SHIP_QTY", nullable = true, precision = 12)
  public int getShipQuantity() {
    return shipQuantity;
  }

  public void setShipQuantity(int shipQuantity) {
    this.shipQuantity = shipQuantity;
  }

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "SHIP_DATE", nullable = true, length = 7)
  public Date getShipDate() {
    return shipDate;
  }

  public void setShipDate(Date shipDate) {
    this.shipDate = shipDate;
  }

}
