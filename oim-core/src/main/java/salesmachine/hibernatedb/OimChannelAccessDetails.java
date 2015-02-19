package salesmachine.hibernatedb;

// Generated 30 Mar, 2010 7:26:10 PM by Hibernate Tools 3.2.4.GA

import java.util.Date;

/**
 * OimChannelAccessDetails generated by hbm2java
 */
public class OimChannelAccessDetails implements java.io.Serializable {

	private Integer accessDetailId;
	private OimChannels oimChannels;
	private OimChannelAccessFields oimChannelAccessFields;
	private Date insertionTm;
	private Date deleteTm;
	private String detailFieldValue;

	public OimChannelAccessDetails() {
	}

	public OimChannelAccessDetails(OimChannels oimChannels,
			OimChannelAccessFields oimChannelAccessFields, Date insertionTm,
			Date deleteTm, String detailFieldValue) {
		this.oimChannels = oimChannels;
		this.oimChannelAccessFields = oimChannelAccessFields;
		this.insertionTm = insertionTm;
		this.deleteTm = deleteTm;
		this.detailFieldValue = detailFieldValue;
	}

	public Integer getAccessDetailId() {
		return this.accessDetailId;
	}

	public void setAccessDetailId(Integer accessDetailId) {
		this.accessDetailId = accessDetailId;
	}

	public OimChannels getOimChannels() {
		return this.oimChannels;
	}

	public void setOimChannels(OimChannels oimChannels) {
		this.oimChannels = oimChannels;
	}

	public OimChannelAccessFields getOimChannelAccessFields() {
		return this.oimChannelAccessFields;
	}

	public void setOimChannelAccessFields(
			OimChannelAccessFields oimChannelAccessFields) {
		this.oimChannelAccessFields = oimChannelAccessFields;
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

	public String getDetailFieldValue() {
		return this.detailFieldValue;
	}

	public void setDetailFieldValue(String detailFieldValue) {
		this.detailFieldValue = detailFieldValue;
	}

}
