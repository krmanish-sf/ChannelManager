package com.is.cm.core.domain;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeanUtils;

import salesmachine.hibernatedb.OimChannelAccessFields;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class ChannelAccessField extends DomainBase implements
		java.io.Serializable {
	private static final long serialVersionUID = 3009906698947418879L;
	private Integer fieldId;
	private String feildName;
	@JsonDeserialize(as = HashSet.class)
	private Set<ChannelAccessDetail> oimChannelAccessDetailses;

	public ChannelAccessField() {
	}

	public Integer getFieldId() {
		return this.fieldId;
	}

	public void setFieldId(Integer fieldId) {
		this.fieldId = fieldId;
	}

	public String getFeildName() {
		return this.feildName;
	}

	public void setFeildName(String feildName) {
		this.feildName = feildName;
	}

	public Set<ChannelAccessDetail> getOimChannelAccessDetailses() {
		return this.oimChannelAccessDetailses;
	}

	public void setOimChannelAccessDetailses(
			Set<ChannelAccessDetail> oimChannelAccessDetailses) {
		this.oimChannelAccessDetailses = oimChannelAccessDetailses;
	}

	public static ChannelAccessField from(
			OimChannelAccessFields oimChannelAccessFields) {
		ChannelAccessField channelAccessField = new ChannelAccessField();
		BeanUtils.copyProperties(oimChannelAccessFields, channelAccessField,
				new String[] { "oimChannelAccessDetailses" });
		return channelAccessField;
	}

}
