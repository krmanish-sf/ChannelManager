package salesmachine.hibernatedb;

import java.io.Serializable;
import java.util.Date;

public class OimShippingMethod implements Serializable {
	private static final long serialVersionUID = 7956076903260268066L;
	private int id;
	private OimShippingCarrier oimShippingCarrier;
	private String name;
	private Date createdOn;
	private String description;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public OimShippingCarrier getOimShippingCarrier() {
		return oimShippingCarrier;
	}

	public void setOimShippingCarrier(OimShippingCarrier oimShippingCarrier) {
		this.oimShippingCarrier = oimShippingCarrier;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
