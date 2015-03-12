package salesmachine.hibernatedb;

import java.io.Serializable;

public class OimSupplierShippingOverride implements Serializable {
	private static final long serialVersionUID = -1421159164136846611L;
	private int id;
	private String shippingMethod;
	private int oimSupplierShippingMethod;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getShippingMethod() {
		return shippingMethod;
	}

	public void setShippingMethod(String shippingMethod) {
		this.shippingMethod = shippingMethod;
	}

	public Integer getOimSupplierShippingMethod() {
		return oimSupplierShippingMethod;
	}

	public void setOimSupplierShippingMethod(Integer oimSupplierShippingMethod) {
		this.oimSupplierShippingMethod = oimSupplierShippingMethod;
	}
}
