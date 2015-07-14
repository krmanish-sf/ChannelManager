package com.is.cm.core.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

public class Field extends DomainBase implements java.io.Serializable {
	private static final long serialVersionUID = -6030500442672006276L;
	private Integer fieldId;
	private String fieldName;
	private String fieldDesc;
	private Date insertionTm;
	private Date deleteTm;
	@JsonManagedReference("FileFieldMap-Field")
	private Set<FileFieldMap> oimFileFieldMaps = new HashSet<FileFieldMap>(0);

	public Field() {
	}

	public Field(String fieldName, String fieldDesc, Date insertionTm,
			Date deleteTm, Set<FileFieldMap> oimFileFieldMaps) {
		this.fieldName = fieldName;
		this.fieldDesc = fieldDesc;
		this.insertionTm = insertionTm;
		this.deleteTm = deleteTm;
		this.oimFileFieldMaps = oimFileFieldMaps;
	}

	public Integer getFieldId() {
		return this.fieldId;
	}

	public void setFieldId(Integer fieldId) {
		this.fieldId = fieldId;
	}

	public String getFieldName() {
		return this.fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldDesc() {
		return this.fieldDesc;
	}

	public void setFieldDesc(String fieldDesc) {
		this.fieldDesc = fieldDesc;
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

	public Set<FileFieldMap> getOimFileFieldMaps() {
		return this.oimFileFieldMaps;
	}

	public void setOimFileFieldMaps(Set<FileFieldMap> oimFileFieldMaps) {
		this.oimFileFieldMaps = oimFileFieldMaps;
	}

}
