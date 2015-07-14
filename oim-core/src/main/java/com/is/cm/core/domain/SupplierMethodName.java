package com.is.cm.core.domain;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeanUtils;

import salesmachine.hibernatedb.OimSupplierMethodNames;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class SupplierMethodName extends DomainBase implements
		java.io.Serializable {
	private static final long serialVersionUID = -7934521478543597242L;

	private Integer methodNameId;
	private String methodName;
	@JsonDeserialize(as = HashSet.class)
	private Set<SupplierMethod> oimSupplierMethodses;
	private static final String[] ignoreProperties = { "oimSupplierMethodses" };

	public SupplierMethodName() {
	}

	public Integer getMethodNameId() {
		return this.methodNameId;
	}

	public void setMethodNameId(Integer methodNameId) {
		this.methodNameId = methodNameId;
	}

	public String getMethodName() {
		return this.methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Set<SupplierMethod> getOimSupplierMethodses() {
		return this.oimSupplierMethodses;
	}

	public void setOimSupplierMethodses(Set<SupplierMethod> oimSupplierMethodses) {
		this.oimSupplierMethodses = oimSupplierMethodses;
	}

	public static SupplierMethodName from(
			OimSupplierMethodNames oimSupplierMethodNames) {
		SupplierMethodName methodName = new SupplierMethodName();
		BeanUtils.copyProperties(oimSupplierMethodNames, methodName,
				ignoreProperties);
		return methodName;
	}

	public OimSupplierMethodNames to() {
		OimSupplierMethodNames methodName = new OimSupplierMethodNames();
		BeanUtils.copyProperties(this, methodName, ignoreProperties);
		return methodName;
	}

}
