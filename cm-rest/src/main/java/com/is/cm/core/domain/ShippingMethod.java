package com.is.cm.core.domain;

import java.io.Serializable;
import java.util.Date;

import salesmachine.hibernatedb.OimShippingMethod;

public class ShippingMethod implements Serializable {
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
}
