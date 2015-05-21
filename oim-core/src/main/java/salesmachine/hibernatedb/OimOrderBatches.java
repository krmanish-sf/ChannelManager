package salesmachine.hibernatedb;

// Generated 30 Mar, 2010 7:26:10 PM by Hibernate Tools 3.2.4.GA

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * OimOrderBatches generated by hbm2java
 */
public class OimOrderBatches implements java.io.Serializable {

	private Integer batchId;
	private OimOrderBatchesTypes oimOrderBatchesTypes;
	private OimChannels oimChannels;
	private Date creationTm;
	private Date insertionTm;
	private Date deleteTm;
	private String description;
	private Set oimUploadedFileses = new HashSet(0);
	private Set oimOrderses = new HashSet(0);

	public OimOrderBatches() {
	}

	public OimOrderBatches(OimOrderBatchesTypes oimOrderBatchesTypes,
			OimChannels oimChannels, Date creationTm, Date insertionTm,
			Date deleteTm, Set oimUploadedFileses, Set oimOrderses) {
		this.oimOrderBatchesTypes = oimOrderBatchesTypes;
		this.oimChannels = oimChannels;
		this.creationTm = creationTm;
		this.insertionTm = insertionTm;
		this.deleteTm = deleteTm;
		this.oimUploadedFileses = oimUploadedFileses;
		this.oimOrderses = oimOrderses;
	}

	public Integer getBatchId() {
		return this.batchId;
	}

	public void setBatchId(Integer batchId) {
		this.batchId = batchId;
	}

	public OimOrderBatchesTypes getOimOrderBatchesTypes() {
		return this.oimOrderBatchesTypes;
	}

	public void setOimOrderBatchesTypes(
			OimOrderBatchesTypes oimOrderBatchesTypes) {
		this.oimOrderBatchesTypes = oimOrderBatchesTypes;
	}

	public OimChannels getOimChannels() {
		return this.oimChannels;
	}

	public void setOimChannels(OimChannels oimChannels) {
		this.oimChannels = oimChannels;
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

	public Set getOimUploadedFileses() {
		return this.oimUploadedFileses;
	}

	public void setOimUploadedFileses(Set oimUploadedFileses) {
		this.oimUploadedFileses = oimUploadedFileses;
	}

	public Set getOimOrderses() {
		return this.oimOrderses;
	}

	public void setOimOrderses(Set oimOrderses) {
		this.oimOrderses = oimOrderses;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
