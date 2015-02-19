package com.is.cm.core.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;

public class FileFieldMap extends DomainBase implements java.io.Serializable {
	private static final long serialVersionUID = -8592253061888832087L;
	private Integer fieldMapId;
	@JsonBackReference("FileFieldMap-Filetype")
	private Filetype oimFiletypes;
	@JsonBackReference("FileFieldMap-Field")
	private Field oimFields;
	private String mappedFieldName;
	private Date insertionTm;
	private Date deleteTm;
	private String mappedFieldModifierRuleRd;
	private String mappedFieldModifierRuleWr;

	public FileFieldMap() {
	}

	public Integer getFieldMapId() {
		return this.fieldMapId;
	}

	public void setFieldMapId(Integer fieldMapId) {
		this.fieldMapId = fieldMapId;
	}

	public Filetype getOimFiletypes() {
		return this.oimFiletypes;
	}

	public void setOimFiletypes(Filetype oimFiletypes) {
		this.oimFiletypes = oimFiletypes;
	}

	public Field getOimFields() {
		return this.oimFields;
	}

	public void setOimFields(Field oimFields) {
		this.oimFields = oimFields;
	}

	public String getMappedFieldName() {
		return this.mappedFieldName;
	}

	public void setMappedFieldName(String mappedFieldName) {
		this.mappedFieldName = mappedFieldName;
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

	public String getMappedFieldModifierRuleRd() {
		return this.mappedFieldModifierRuleRd;
	}

	public void setMappedFieldModifierRuleRd(String mappedFieldModifierRuleRd) {
		this.mappedFieldModifierRuleRd = mappedFieldModifierRuleRd;
	}

	public String getMappedFieldModifierRuleWr() {
		return this.mappedFieldModifierRuleWr;
	}

	public void setMappedFieldModifierRuleWr(String mappedFieldModifierRuleWr) {
		this.mappedFieldModifierRuleWr = mappedFieldModifierRuleWr;
	}

}
