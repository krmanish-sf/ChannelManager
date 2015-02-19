package salesmachine.hibernatedb;

public class OimVendorShippingMap {

	private Integer id;
	private OimSuppliers oimSuppliers;
	private Vendors vendors;
	private OimSupplierShippingMethods oimShippingMethod;
	private String shippingText;

	public OimVendorShippingMap() {
	}

	public OimVendorShippingMap(OimSuppliers oimSuppliers, Vendors vendors,  
			OimSupplierShippingMethods oimShippingMethod, String shippingText) {
		this.oimSuppliers = oimSuppliers;
		this.vendors = vendors;
		this.oimShippingMethod = oimShippingMethod;
		this.shippingText = shippingText;
	}

	public Integer getId() {
		return this.id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}

	public OimSuppliers getOimSuppliers() {
		return this.oimSuppliers;
	}

	public void setOimSuppliers(OimSuppliers oimSuppliers) {
		this.oimSuppliers = oimSuppliers;
	}

	public Vendors getVendors() {
		return this.vendors;
	}

	public void setVendors(Vendors vendors) {
		this.vendors = vendors;
	}

	public OimSupplierShippingMethods getOimShippingMethod() {
		return this.oimShippingMethod;
	}

	public void setOimShippingMethod(OimSupplierShippingMethods oimShippingMethod) {
		this.oimShippingMethod = oimShippingMethod;
	}	
	public String getShippingText() {
		return this.shippingText;
	}

	public void setShippingText(String shippingText) {
		this.shippingText = shippingText;
	}
}
