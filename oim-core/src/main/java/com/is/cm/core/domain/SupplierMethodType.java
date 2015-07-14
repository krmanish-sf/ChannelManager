package com.is.cm.core.domain;

import java.util.HashSet;
import java.util.Set;

import salesmachine.hibernatedb.OimSupplierMethodTypes;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class SupplierMethodType extends DomainBase implements
		java.io.Serializable {
	private static final long serialVersionUID = -2135747222246497378L;
	private Integer methodTypeId;
	private String methodTypeName;
	@JsonDeserialize(as = HashSet.class)
	private Set<SupplierMethod> oimSupplierMethodses;

	public SupplierMethodType() {
	}

	public SupplierMethodType(Integer methodTypeId) {
		this.methodTypeId = methodTypeId;
	}

	public SupplierMethodType(Integer methodTypeId, String methodTypeName,
			Set<SupplierMethod> oimSupplierMethodses) {
		this.methodTypeId = methodTypeId;
		this.methodTypeName = methodTypeName;
		this.oimSupplierMethodses = oimSupplierMethodses;
	}

	public Integer getMethodTypeId() {
		return this.methodTypeId;
	}

	public void setMethodTypeId(Integer methodTypeId) {
		this.methodTypeId = methodTypeId;
	}

	public String getMethodTypeName() {
		return this.methodTypeName;
	}

	public void setMethodTypeName(String methodTypeName) {
		this.methodTypeName = methodTypeName;
	}

	public Set<SupplierMethod> getOimSupplierMethodses() {
		return this.oimSupplierMethodses;
	}

	public void setOimSupplierMethodses(Set<SupplierMethod> oimSupplierMethodses) {
		this.oimSupplierMethodses = oimSupplierMethodses;
	}

	public static SupplierMethodType from(
			OimSupplierMethodTypes oimSupplierMethodTypes) {
		SupplierMethodType methodType = new SupplierMethodType(
				oimSupplierMethodTypes.getMethodTypeId(),
				oimSupplierMethodTypes.getMethodTypeName(), null);
		return methodType;
	}

	public OimSupplierMethodTypes to() {
		OimSupplierMethodTypes methodType = new OimSupplierMethodTypes(
				this.getMethodTypeId(), this.getMethodTypeName(), null);
		return methodType;
	}

}
