package com.is.cm.core.domain;

import java.util.Date;

import org.springframework.beans.BeanUtils;

import salesmachine.hibernatedb.OimSupplierMethodattrValues;

public class SupplierMethodAttrValue extends DomainBase implements
		java.io.Serializable, java.lang.Comparable<SupplierMethodAttrValue> {
	private static final long serialVersionUID = -6990163104005278588L;
	private Integer attrvalueId;
	private SupplierMethod oimSupplierMethods;
	private SupplierMethodAttrName oimSupplierMethodattrNames;
	private String attributeValue;
	private Date insertionTm;
	private Date deleteTm;
	private static final String[] IGNORED_PROPERTIES = { "oimSupplierMethods",
			"oimSupplierMethodattrNames" };

	public SupplierMethodAttrValue() {
	}

	public Integer getAttrvalueId() {
		return this.attrvalueId;
	}

	public void setAttrvalueId(Integer attrvalueId) {
		this.attrvalueId = attrvalueId;
	}

	public SupplierMethod getOimSupplierMethods() {
		return this.oimSupplierMethods;
	}

	public void setOimSupplierMethods(SupplierMethod oimSupplierMethods) {
		this.oimSupplierMethods = oimSupplierMethods;
	}

	public SupplierMethodAttrName getOimSupplierMethodattrNames() {
		return this.oimSupplierMethodattrNames;
	}

	public void setOimSupplierMethodattrNames(
			SupplierMethodAttrName oimSupplierMethodattrNames) {
		this.oimSupplierMethodattrNames = oimSupplierMethodattrNames;
	}

	public String getAttributeValue() {
		return this.attributeValue;
	}

	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
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

	public static SupplierMethodAttrValue from(
			OimSupplierMethodattrValues source) {
		SupplierMethodAttrValue target = new SupplierMethodAttrValue();
		BeanUtils.copyProperties(source, target, IGNORED_PROPERTIES);
		target.setOimSupplierMethodattrNames(SupplierMethodAttrName.from(source
				.getOimSupplierMethodattrNames()));
		return target;
	}

	public OimSupplierMethodattrValues to() {
		OimSupplierMethodattrValues target = new OimSupplierMethodattrValues();
		BeanUtils.copyProperties(this, target, IGNORED_PROPERTIES);
		target.setOimSupplierMethodattrNames(this
				.getOimSupplierMethodattrNames().to());
		return target;
	}

	@Override
	public int compareTo(SupplierMethodAttrValue o) {
		return this.oimSupplierMethodattrNames.getAttrName().compareTo(
				o.oimSupplierMethodattrNames.getAttrName());
	}

}
