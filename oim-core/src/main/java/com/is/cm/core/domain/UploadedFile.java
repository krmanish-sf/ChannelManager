package com.is.cm.core.domain;

import java.util.Date;

import salesmachine.hibernatedb.OimUploadedFiles;

public class UploadedFile extends DomainBase implements java.io.Serializable {
	private static final long serialVersionUID = -2699983949351501924L;
	private Integer fileId;
	// @JsonBackReference("Filetype-UploadedFile")
	private Filetype oimFiletypes;
	// @JsonBackReference("UploadedFile-Channel")
	private Channel oimChannels;
	// @JsonBackReference("UploadedFile-OrderBatch")
	private OrderBatch oimOrderBatches;
	private String fileName;
	private Double fileSz;
	private Date insertionTm;
	private Date deleteTm;

	public UploadedFile() {
	}

	public Integer getFileId() {
		return this.fileId;
	}

	public void setFileId(Integer fileId) {
		this.fileId = fileId;
	}

	public Filetype getOimFiletypes() {
		return this.oimFiletypes;
	}

	public void setOimFiletypes(Filetype oimFiletypes) {
		this.oimFiletypes = oimFiletypes;
	}

	public Channel getOimChannels() {
		return this.oimChannels;
	}

	public void setOimChannels(Channel oimChannels) {
		this.oimChannels = oimChannels;
	}

	public OrderBatch getOimOrderBatches() {
		return this.oimOrderBatches;
	}

	public void setOimOrderBatches(OrderBatch oimOrderBatches) {
		this.oimOrderBatches = oimOrderBatches;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Double getFileSz() {
		return this.fileSz;
	}

	public void setFileSz(Double fileSz) {
		this.fileSz = fileSz;
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

	public static UploadedFile from(OimUploadedFiles s) {
		UploadedFile t = new UploadedFile();
		t.setFileId(s.getFileId());
		t.setFileName(s.getFileName());
		t.setFileSz(s.getFileSz());
		return t;
	}

}
