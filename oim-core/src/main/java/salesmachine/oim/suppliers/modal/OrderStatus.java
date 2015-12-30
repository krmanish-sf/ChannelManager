package salesmachine.oim.suppliers.modal;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import salesmachine.hibernatedb.OimOrderTracking;

public class OrderStatus {

  private String status;
  private final List<TrackingData> trackingData;
  private boolean isShipped = false;
  private boolean isPartialShipped = false;

  public boolean isPartialShipped() {
    return isPartialShipped;
  }

  public void setPartialShipped(boolean isPartialShipped) {
    this.isPartialShipped = isPartialShipped;
  }

  public OrderStatus() {
    trackingData = new ArrayList<TrackingData>();
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public List<TrackingData> getTrackingData() {
    return trackingData;
  }

  public void addTrackingData(TrackingData trackingData) {
    this.trackingData.add(trackingData);
    this.isShipped = true;
  }

  public boolean isShipped() {
    return isShipped;
  }

  @Override
  public String toString() {
    return isShipped ? String.format("%s %s", status, trackingData) : String.format("%s", status);
  }
  
  public static OrderStatus getOrderStatusFromOimOrderTracking(OimOrderTracking oimOrderTracking){
    TrackingData td = new TrackingData();
    GregorianCalendar c = new GregorianCalendar();
    c.setTime(oimOrderTracking.getShipDate());
    XMLGregorianCalendar date2 = null;
    try {
      date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
    } catch (DatatypeConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    td.setShipDate(date2);
    td.setCarrierName(oimOrderTracking.getShippingCarrier());
    td.setCarrierCode(oimOrderTracking.getShippingCarrier());
    td.setQuantity(oimOrderTracking.getShipQuantity());
    td.setShipperTrackingNumber(oimOrderTracking.getTrackingNumber());
    td.setShippingMethod(oimOrderTracking.getShippingMethod());
    OrderStatus status = new OrderStatus();
    status.addTrackingData(td);
    status.setStatus(oimOrderTracking.getDetail().getSupplierOrderStatus());
    return status;
  }
}
