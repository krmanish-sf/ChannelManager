package com.is.cm.core.domain;

import java.io.Serializable;
import java.util.Date;

import salesmachine.hibernatedb.OimShippingCarrier;

public class ShippingCarrier implements Serializable {
	private static final long serialVersionUID = 8939653121678037985L;
	private int id;
	private String name;
	private Date createdOn;
	private Supplier supplier;

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

	public static ShippingCarrier from(OimShippingCarrier oimShippingCarrier) {
		ShippingCarrier shippingCarrier = new ShippingCarrier();
		shippingCarrier.setId(oimShippingCarrier.getId());
		shippingCarrier.setCreatedOn(oimShippingCarrier.getCreatedOn());
		shippingCarrier.setName(oimShippingCarrier.getName());
		shippingCarrier.setSupplier(Supplier.from(oimShippingCarrier
				.getOimSupplier()));
		return shippingCarrier;
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}

	public OimShippingCarrier toOimShippingCarrier() {
		OimShippingCarrier carrier = new OimShippingCarrier();
		carrier.setCreatedOn(this.createdOn);
		carrier.setId(this.id);
		carrier.setName(this.name);
		return carrier;
	}
}
