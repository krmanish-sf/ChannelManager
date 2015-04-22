package salesmachine.oim.suppliers.modal;

public class OrderStatus {

	private String status;
	private TrackingData trackingData;
	private boolean isShipped = false;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public TrackingData getTrackingData() {
		return trackingData;
	}

	public void setTrackingData(TrackingData trackingData) {
		this.trackingData = trackingData;
		this.isShipped = true;
	}

	public boolean isShipped() {
		return isShipped;
	}

	@Override
	public String toString() {
		return isShipped ? String.format("%s %s", status, trackingData)
				: String.format("%s ", status);
	}
}
