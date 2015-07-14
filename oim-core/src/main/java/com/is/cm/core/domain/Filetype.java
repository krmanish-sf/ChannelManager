package com.is.cm.core.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import salesmachine.hibernatedb.OimFiletypes;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class Filetype extends DomainBase implements java.io.Serializable {
	private static final long serialVersionUID = -4675661337257668034L;
	private Integer fileTypeId;
	@JsonBackReference("Filetype-Fileformat")
	private Fileformat oimFileformats;
	private String fileTypeName;
	private Date insertionTm;
	private Date deleteTm;
	@JsonDeserialize(as = HashSet.class)
	private Set<UploadedFile> oimUploadedFileses;
	@JsonDeserialize(as = HashSet.class)
	private Set<FileformatParam> oimFileformatParamses;
	@JsonDeserialize(as = HashSet.class)
	private Set<FileFieldMap> oimFileFieldMaps;
	@JsonDeserialize(as = HashSet.class)
	private Set<ChannelFile> oimChannelFileses;

	public Filetype() {
	}

	public Filetype(Fileformat oimFileformats, String fileTypeName,
			Date insertionTm, Date deleteTm,
			Set<UploadedFile> oimUploadedFileses,
			Set<FileformatParam> oimFileformatParamses,
			Set<FileFieldMap> oimFileFieldMaps,
			Set<ChannelFile> oimChannelFileses) {
		this.oimFileformats = oimFileformats;
		this.fileTypeName = fileTypeName;
		this.insertionTm = insertionTm;
		this.deleteTm = deleteTm;
		this.oimUploadedFileses = oimUploadedFileses;
		this.oimFileformatParamses = oimFileformatParamses;
		this.oimFileFieldMaps = oimFileFieldMaps;
		this.oimChannelFileses = oimChannelFileses;
	}

	public Integer getFileTypeId() {
		return this.fileTypeId;
	}

	public void setFileTypeId(Integer fileTypeId) {
		this.fileTypeId = fileTypeId;
	}

	public Fileformat getOimFileformats() {
		return this.oimFileformats;
	}

	public void setOimFileformats(Fileformat oimFileformats) {
		this.oimFileformats = oimFileformats;
	}

	public String getFileTypeName() {
		return this.fileTypeName;
	}

	public void setFileTypeName(String fileTypeName) {
		this.fileTypeName = fileTypeName;
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

	public Set<UploadedFile> getOimUploadedFileses() {
		return this.oimUploadedFileses;
	}

	public void setOimUploadedFileses(Set<UploadedFile> oimUploadedFileses) {
		this.oimUploadedFileses = oimUploadedFileses;
	}

	public Set<FileformatParam> getOimFileformatParamses() {
		return this.oimFileformatParamses;
	}

	public void setOimFileformatParamses(
			Set<FileformatParam> oimFileformatParamses) {
		this.oimFileformatParamses = oimFileformatParamses;
	}

	public Set<FileFieldMap> getOimFileFieldMaps() {
		return this.oimFileFieldMaps;
	}

	public void setOimFileFieldMaps(Set<FileFieldMap> oimFileFieldMaps) {
		this.oimFileFieldMaps = oimFileFieldMaps;
	}

	public Set<ChannelFile> getOimChannelFileses() {
		return this.oimChannelFileses;
	}

	public void setOimChannelFileses(Set<ChannelFile> oimChannelFileses) {
		this.oimChannelFileses = oimChannelFileses;
	}

	public static Filetype from(OimFiletypes oimFiletypes) {
		Filetype f = new Filetype();
		f.setFileTypeId(oimFiletypes.getFileTypeId());
		f.setFileTypeName(oimFiletypes.getFileTypeName());
		return f;
	}

}
