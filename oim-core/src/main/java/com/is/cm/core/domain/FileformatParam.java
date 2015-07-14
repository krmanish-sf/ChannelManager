package com.is.cm.core.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;

public class FileformatParam extends DomainBase implements java.io.Serializable {
	private static final long serialVersionUID = -2597506676323990064L;
	private Integer paramId;
	@JsonBackReference("FileformatParam-Filetype")
	private Filetype oimFiletypes;
	private String paramName;
	private String paramValue;
	private Date insertionTm;
	private Date deleteTm;

	public FileformatParam() {
	}

	public Integer getParamId() {
		return this.paramId;
	}

	public void setParamId(Integer paramId) {
		this.paramId = paramId;
	}

	public Filetype getOimFiletypes() {
		return this.oimFiletypes;
	}

	public void setOimFiletypes(Filetype oimFiletypes) {
		this.oimFiletypes = oimFiletypes;
	}

	public String getParamName() {
		return this.paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getParamValue() {
		return this.paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
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

}
