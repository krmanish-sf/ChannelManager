package com.is.cm.core.domain;

import java.util.Date;

import org.springframework.beans.BeanUtils;

import salesmachine.hibernatedb.OimChannelAccessDetails;

public class ChannelAccessDetail extends DomainBase implements
		java.io.Serializable {
	private static final long serialVersionUID = -5744277386208384612L;
	private Integer accessDetailId;
	private Channel oimChannels;
	private ChannelAccessField oimChannelAccessFields;
	private Date insertionTm;
	private Date deleteTm;
	private String detailFieldValue;

	public ChannelAccessDetail() {
	}

	public Integer getAccessDetailId() {
		return this.accessDetailId;
	}

	public void setAccessDetailId(Integer accessDetailId) {
		this.accessDetailId = accessDetailId;
	}

	public Channel getOimChannels() {
		return this.oimChannels;
	}

	public void setOimChannels(Channel channel) {
		this.oimChannels = channel;
	}

	public ChannelAccessField getOimChannelAccessFields() {
		return this.oimChannelAccessFields;
	}

	public void setOimChannelAccessFields(
			ChannelAccessField oimChannelAccessFields) {
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

	public static ChannelAccessDetail from(
			OimChannelAccessDetails oimChannelAccessDetails) {
		ChannelAccessDetail channelAccessDetail = new ChannelAccessDetail();
		BeanUtils.copyProperties(oimChannelAccessDetails, channelAccessDetail,
				new String[] { "oimChannelAccessFields","oimChannels" });
		channelAccessDetail.setOimChannelAccessFields(ChannelAccessField
				.from(oimChannelAccessDetails.getOimChannelAccessFields()));
		return channelAccessDetail;
	}

}
