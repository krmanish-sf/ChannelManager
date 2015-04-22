package salesmachine.oim.suppliers.modal;

import java.util.GregorianCalendar;

import salesmachine.util.StringHandle;

public class TrackingData {

	private String carrierCode;
	private String carrierName;
	private String shippingMethod;
	private String trackingNumber;
	private GregorianCalendar shipDate;

	public GregorianCalendar getShipDate() {
		return shipDate;
	}

	public String getCarrierCode() {
		return carrierCode;
	}

	public void setCarrierCode(String carrierCode) {
		this.carrierCode = carrierCode;
	}

	public String getCarrierName() {
		return carrierName;
	}

	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}

	public String getShippingMethod() {
		return shippingMethod;
	}

	public void setShippingMethod(String shippingMethod) {
		this.shippingMethod = shippingMethod;
	}

	public String getShipperTrackingNumber() {
		return trackingNumber;
	}

	public void setShipperTrackingNumber(String shipperTrackingNumber) {
		this.trackingNumber = shipperTrackingNumber;
	}

	public void setShipDate(GregorianCalendar shipDate) {
		this.shipDate = shipDate;
	}

	@Override
	public String toString() {
		return StringHandle.isNullOrEmpty(carrierCode) ? "" : String.format(
				"%s %s : %s", carrierCode, shippingMethod, trackingNumber);
	}
}
