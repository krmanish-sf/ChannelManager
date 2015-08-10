package com.is.cm.core.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.BeanUtils;

import salesmachine.hibernatedb.OimSupplierMethodattrValues;
import salesmachine.hibernatedb.OimSupplierMethods;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class SupplierMethod extends DomainBase implements java.io.Serializable,
		java.lang.Comparable<SupplierMethod> {
	private static final long serialVersionUID = 6605007085133085139L;
	private Integer supplierMethodId;
	private SupplierMethodName oimSupplierMethodNames;
	private SupplierMethodType oimSupplierMethodTypes;
	private Supplier oimSuppliers;
	private Date insertionTm;
	private Date deleteTm;
	private Vendor oimVendors;
	public Vendor getOimVendors() {
		return oimVendors;
	}

	public void setOimVendors(Vendor oimVendors) {
		this.oimVendors = oimVendors;
	}

	@JsonDeserialize(as = TreeSet.class)
	private Set<SupplierMethodAttrValue> oimSupplierMethodattrValueses = new TreeSet<SupplierMethodAttrValue>();
	private static final String[] IGNORE_PROPERTIES = {
			"oimSupplierMethodNames", "oimSupplierMethodTypes", "oimSuppliers",
			"oimSupplierMethodattrValueses" };

	public SupplierMethod() {
	}

	public Integer getSupplierMethodId() {
		return this.supplierMethodId;
	}

	public void setSupplierMethodId(Integer supplierMethodId) {
		this.supplierMethodId = supplierMethodId;
	}

	public SupplierMethodName getOimSupplierMethodNames() {
		return this.oimSupplierMethodNames;
	}

	public void setOimSupplierMethodNames(
			SupplierMethodName oimSupplierMethodNames) {
		this.oimSupplierMethodNames = oimSupplierMethodNames;
	}

	public SupplierMethodType getOimSupplierMethodTypes() {
		return this.oimSupplierMethodTypes;
	}

	public void setOimSupplierMethodTypes(
			SupplierMethodType oimSupplierMethodTypes) {
		this.oimSupplierMethodTypes = oimSupplierMethodTypes;
	}

	public Supplier getOimSuppliers() {
		return this.oimSuppliers;
	}

	public void setOimSuppliers(Supplier oimSuppliers) {
		this.oimSuppliers = oimSuppliers;
	}

	public Date getInsertionTm() {
		return this.insertionTm;
	}

	public void setInsertionTm(Date insertionTm) {
		this.insertionTm = insertionTm;
	}

	public Date getDeleteTm() {
		return this.deleteTm;
	}

	public void setDeleteTm(Date deleteTm) {
		this.deleteTm = deleteTm;
	}

	public Set<SupplierMethodAttrValue> getOimSupplierMethodattrValueses() {
		return this.oimSupplierMethodattrValueses;
	}

	public void setOimSupplierMethodattrValueses(
			Set<SupplierMethodAttrValue> oimSupplierMethodattrValueses) {
		this.oimSupplierMethodattrValueses = oimSupplierMethodattrValueses;
	}

	public static SupplierMethod from(OimSupplierMethods methods) {
		SupplierMethod supplierMethod = new SupplierMethod();
		BeanUtils.copyProperties(methods, supplierMethod, IGNORE_PROPERTIES);
		supplierMethod.setOimSupplierMethodNames(SupplierMethodName
				.from(methods.getOimSupplierMethodNames()));
		supplierMethod.setOimSupplierMethodTypes(SupplierMethodType
				.from(methods.getOimSupplierMethodTypes()));
		Set<SupplierMethodAttrValue> attrValues = new TreeSet<SupplierMethodAttrValue>();
		for (OimSupplierMethodattrValues it : (Set<OimSupplierMethodattrValues>) methods
				.getOimSupplierMethodattrValueses()) {
			attrValues.add(SupplierMethodAttrValue.from(it));
		}
		supplierMethod.setOimSupplierMethodattrValueses(attrValues);
		supplierMethod.setOimVendors(Vendor.from(methods.getVendor()));
		return supplierMethod;
	}

	public OimSupplierMethods to() {
		OimSupplierMethods supplierMethod = new OimSupplierMethods();
		BeanUtils.copyProperties(this, supplierMethod, IGNORE_PROPERTIES);
		supplierMethod.setOimSupplierMethodNames(this
				.getOimSupplierMethodNames().to());
		supplierMethod.setOimSupplierMethodTypes(this
				.getOimSupplierMethodTypes().to());
		Set<OimSupplierMethodattrValues> attrValues = new HashSet<OimSupplierMethodattrValues>();
		for (SupplierMethodAttrValue it : this
				.getOimSupplierMethodattrValueses()) {
			attrValues.add(it.to());
		}
		supplierMethod.setOimSupplierMethodattrValueses(attrValues);
		return supplierMethod;
	}

	@Override
	public int compareTo(SupplierMethod arg0) {
		return this.oimSupplierMethodNames.getMethodName().compareTo(
				((SupplierMethod) arg0).oimSupplierMethodNames.getMethodName());
	}
}
