package com.is.cm.core.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeanUtils;

import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimUploadedFiles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderBatch extends DomainBase implements java.io.Serializable {
	private static final long serialVersionUID = -2700022421732310952L;
	private Integer batchId;
	// @JsonBackReference("OrderBatchesType-OrderBatch")
	private OrderBatchesType oimOrderBatchesTypes;
	private Channel oimChannels;
	private Date creationTm;
	private Date insertionTm;
	private Date deleteTm;
	private String description;
	private Integer errorCode;

	@JsonDeserialize(as = HashSet.class)
	// @JsonManagedReference("UploadedFile-OrderBatch")
	private Set<UploadedFile> oimUploadedFileses;
	@JsonDeserialize(as = HashSet.class)
	// @JsonManagedReference("OrderBatch-Order")
	private Set<Order> oimOrderses;

	public static OrderBatch from(OimOrderBatches oimOrderBatches) {
		if (oimOrderBatches == null)
			return null;
		OrderBatch batch = new OrderBatch();
		batch.errorCode = oimOrderBatches.getErrorCode() == null ? 0
				: oimOrderBatches.getErrorCode();
		BeanUtils.copyProperties(oimOrderBatches, batch, new String[] {
				"oimOrderBatchesTypes", "oimChannels", "oimUploadedFileses",
				"oimOrderses", "errorCode" });
		batch.oimOrderBatchesTypes = OrderBatchesType.from(oimOrderBatches
				.getOimOrderBatchesTypes());
		batch.oimChannels = Channel.from(oimOrderBatches.getOimChannels());

		batch.oimUploadedFileses = new HashSet<UploadedFile>();
		if (oimOrderBatches.getOimUploadedFileses() != null) {
			for (OimUploadedFiles file : (Set<OimUploadedFiles>) oimOrderBatches
					.getOimUploadedFileses()) {
				batch.oimUploadedFileses.add(UploadedFile.from(file));
			}
		}
		return batch;
	}

	public OrderBatch() {
	}

	public Integer getBatchId() {
		return this.batchId;
	}

	public void setBatchId(Integer batchId) {
		this.batchId = batchId;
	}

	@JsonInclude
	public OrderBatchesType getOimOrderBatchesTypes() {
		return this.oimOrderBatchesTypes;
	}

	public void setOimOrderBatchesTypes(OrderBatchesType oimOrderBatchesTypes) {
		this.oimOrderBatchesTypes = oimOrderBatchesTypes;
	}

	public Channel getOimChannels() {
		return this.oimChannels;
	}

	public void setOimChannels(Channel channel) {
		this.oimChannels = channel;
	}

	public Date getCreationTm() {
		return this.creationTm;
	}

	public void setCreationTm(Date creationTm) {
		this.creationTm = creationTm;
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

	public Set<Order> getOimOrderses() {
		return this.oimOrderses;
	}

	public void setOimOrderses(Set<Order> oimOrderses) {
		this.oimOrderses = oimOrderses;
	}

	public OimOrderBatches toOimOrderBatches() {
		OimOrderBatches target = new OimOrderBatches();
		BeanUtils.copyProperties(this, target, new String[] {
				"oimOrderBatchesTypes", "oimChannels", "oimUploadedFileses",
				"oimOrderses" });
		return target;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public final String getErrorDesc() {
		String msg;
		switch (errorCode) {
		case 1:
			msg = "Channel Configuration Error";
			break;
		case 2:
			msg = "Channel Communication Error";
			break;
		case 3:
			msg = "Order Format Error";
			break;
		default:
			msg = "";
			break;
		}
		return msg;
	}
}
