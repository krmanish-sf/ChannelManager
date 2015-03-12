package salesmachine.hibernatedb;

import java.io.Serializable;
import java.util.Date;

public class OimShippingCarrier implements Serializable {
	private static final long serialVersionUID = -2073584065000415560L;
	private int id;
	private String name;
	private Date createdOn;
	private OimSuppliers oimSupplier;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	public OimSuppliers getOimSupplier() {
		return oimSupplier;
	}
	public void setOimSupplier(OimSuppliers oimSupplier) {
		this.oimSupplier = oimSupplier;
	}
}
