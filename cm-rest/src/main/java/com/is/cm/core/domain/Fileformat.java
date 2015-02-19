package com.is.cm.core.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class Fileformat extends DomainBase implements java.io.Serializable {
	private static final long serialVersionUID = 4027968764446074268L;
	private Integer fileformatId;
	private String fileFormatName;
	private Date insertionTm;
	private Date deleteTm;
	@JsonDeserialize(as = HashSet.class)
	@JsonManagedReference("Filetype-Fileformat")
	private Set<Filetype> oimFiletypeses;

	public Fileformat() {
	}

	public Integer getFileformatId() {
		return this.fileformatId;
	}

	public void setFileformatId(Integer fileformatId) {
		this.fileformatId = fileformatId;
	}

	public String getFileFormatName() {
		return this.fileFormatName;
	}

	public void setFileFormatName(String fileFormatName) {
		this.fileFormatName = fileFormatName;
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

	public Set<Filetype> getOimFiletypeses() {
		return this.oimFiletypeses;
	}

	public void setOimFiletypeses(Set<Filetype> oimFiletypeses) {
		this.oimFiletypeses = oimFiletypeses;
	}

}
