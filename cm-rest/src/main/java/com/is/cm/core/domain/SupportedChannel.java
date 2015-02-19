package com.is.cm.core.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeanUtils;

import salesmachine.hibernatedb.OimSupportedChannels;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class SupportedChannel extends DomainBase implements
		java.io.Serializable {
	private static final long serialVersionUID = -4376275407143041216L;
	private Integer supportedChannelId;
	private String channelName;
	private Date insertionTm;
	private Date deleteTm;
	private String orderFetchBean;
	@JsonDeserialize(as = HashSet.class)
	private Set<Channel> oimChannelses;

	public SupportedChannel() {
	}

	public Integer getSupportedChannelId() {
		return this.supportedChannelId;
	}

	public void setSupportedChannelId(Integer supportedChannelId) {
		this.supportedChannelId = supportedChannelId;
	}

	public String getChannelName() {
		return this.channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
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

	public String getOrderFetchBean() {
		return this.orderFetchBean;
	}

	public void setOrderFetchBean(String orderFetchBean) {
		this.orderFetchBean = orderFetchBean;
	}

	public Set<Channel> getOimChannelses() {
		return this.oimChannelses;
	}

	public void setOimChannelses(Set<Channel> oimChannelses) {
		this.oimChannelses = oimChannelses;
	}

	public static SupportedChannel from(
			OimSupportedChannels oimSupportedChannels) {
		if (oimSupportedChannels == null)
			return null;
		SupportedChannel supportedChannel = new SupportedChannel();
		BeanUtils.copyProperties(oimSupportedChannels, supportedChannel,
				new String[] { "oimChannelses" });
		return supportedChannel;
	}

}
