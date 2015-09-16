package salesmachine.oim.suppliers.modal;

import java.util.ArrayList;
import java.util.List;

public class OrderStatus {

  private String status;
  private final List<TrackingData> trackingData;
  private boolean isShipped = false;

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
}
