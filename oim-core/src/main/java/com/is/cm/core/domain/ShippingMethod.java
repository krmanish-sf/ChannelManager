package com.is.cm.core.domain;

import java.io.Serializable;
import java.util.Date;

import salesmachine.hibernatedb.OimShippingMethod;

public class ShippingMethod implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7453582791510031167L;
	private int id;
	private ShippingCarrier shippingCarrier;
	private String name;
	private Date createdOn;
	private String description;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ShippingCarrier getShippingCarrier() {
		return shippingCarrier;
	}

	public void setShippingCarrier(ShippingCarrier shippingCarrier) {
		this.shippingCarrier = shippingCarrier;
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

	public String getFullName() {
		return toString();
	}

	public void setFullName(String fullName) {
		// Left blank to support json parsing
	}

	public static ShippingMethod from(OimShippingMethod oimShippingMethod) {
		ShippingMethod shippingMethod = new ShippingMethod();
		shippingMethod.setCreatedOn(oimShippingMethod.getCreatedOn());
		shippingMethod.setDescription(oimShippingMethod.getDescription());
		shippingMethod.setId(oimShippingMethod.getId());
		shippingMethod.setName(oimShippingMethod.getName());
		shippingMethod.setShippingCarrier(ShippingCarrier
				.from(oimShippingMethod.getOimShippingCarrier()));
		return shippingMethod;
	}

	@Override
	public String toString() {
		return this.shippingCarrier.getName() + " " + this.name;
	}

	public OimShippingMethod toOimShippingMethod() {
		OimShippingMethod retVal = new OimShippingMethod();
		retVal.setCreatedOn(this.createdOn);
		retVal.setDescription(this.description);
		retVal.setId(this.id);
		retVal.setName(this.name);
		retVal.setOimShippingCarrier(this.shippingCarrier
				.toOimShippingCarrier());
		return retVal;
	}
}
