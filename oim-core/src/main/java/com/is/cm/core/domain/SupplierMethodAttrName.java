package com.is.cm.core.domain;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeanUtils;

import salesmachine.hibernatedb.OimSupplierMethodattrNames;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class SupplierMethodAttrName extends DomainBase implements
		java.io.Serializable {
	private static final long serialVersionUID = 1941371601411246041L;
	private Integer attrId;
	private String attrName;
	@JsonDeserialize(as = HashSet.class)
	private Set<SupplierMethodAttrValue> oimSupplierMethodattrValueses = new HashSet<SupplierMethodAttrValue>(
			0);

	public SupplierMethodAttrName() {
	}

	public Integer getAttrId() {
		return this.attrId;
	}

	public void setAttrId(Integer attrId) {
		this.attrId = attrId;
	}

	public String getAttrName() {
		return this.attrName;
	}

	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}

	public Set<SupplierMethodAttrValue> getOimSupplierMethodattrValueses() {
		return this.oimSupplierMethodattrValueses;
	}

	public void setOimSupplierMethodattrValueses(
			Set<SupplierMethodAttrValue> oimSupplierMethodattrValueses) {
		this.oimSupplierMethodattrValueses = oimSupplierMethodattrValueses;
	}

	public static SupplierMethodAttrName from(
			OimSupplierMethodattrNames oimSupplierMethodattrNames) {
		SupplierMethodAttrName target = new SupplierMethodAttrName();
		BeanUtils.copyProperties(oimSupplierMethodattrNames, target,
				new String[] { "oimSupplierMethodattrValueses" });
		return target;
	}

	public OimSupplierMethodattrNames to() {
		OimSupplierMethodattrNames target = new OimSupplierMethodattrNames();
		BeanUtils.copyProperties(this, target,
				new String[] { "oimSupplierMethodattrValueses" });
		return target;
	}

}
