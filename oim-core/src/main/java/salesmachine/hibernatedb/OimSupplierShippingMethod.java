package salesmachine.hibernatedb;

import java.io.Serializable;

import salesmachine.util.StringHandle;

public class OimSupplierShippingMethod implements Serializable {
	private static final long serialVersionUID = -3498347688014484119L;
	private int id;
	private OimSuppliers oimSupplier;
	private OimShippingCarrier oimShippingCarrier;
	private OimShippingMethod oimShippingMethod;
	private String name;
	private String carrierName;
	private OimSupplierShippingOverride override;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public OimSuppliers getOimSupplier() {
		return oimSupplier;
	}

	public void setOimSupplier(OimSuppliers oimSupplier) {
		this.oimSupplier = oimSupplier;
	}

	public OimShippingCarrier getOimShippingCarrier() {
		return oimShippingCarrier;
	}

	public void setOimShippingCarrier(OimShippingCarrier oimShippingCarrier) {
		this.oimShippingCarrier = oimShippingCarrier;
	}

	public OimShippingMethod getOimShippingMethod() {
		return oimShippingMethod;
	}

	public void setOimShippingMethod(OimShippingMethod oimShippingMethod) {
		this.oimShippingMethod = oimShippingMethod;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public OimSupplierShippingOverride getOverride() {
		return override;
	}

	public void setOverride(OimSupplierShippingOverride override) {
		this.override = override;
	}

	public String getCarrierName() {
		return carrierName;
	}

	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}

	@Override
	public String toString() {
		return (StringHandle.removeNull(this.carrierName) + " " + this.name).trim();
	}
}
