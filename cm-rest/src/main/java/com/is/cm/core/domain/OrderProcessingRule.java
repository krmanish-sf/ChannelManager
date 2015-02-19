package com.is.cm.core.domain;

import java.util.Date;

import org.springframework.beans.BeanUtils;

import salesmachine.hibernatedb.OimOrderProcessingRule;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderProcessingRule extends DomainBase implements
		java.io.Serializable {
	private static final long serialVersionUID = 7013230335041774518L;
	private Integer ruleId;
	@JsonBackReference("Channel-OrderProcessingRule")
	private Channel oimChannels;
	private Integer processAll;
	private String processWithStatus;
	private Integer updateStoreOrderStatus;
	private Date insertionTm;
	private Date deleteTm;
	private Integer supplierId;
	private String updateWithStatus;

	public OrderProcessingRule() {
	}

	public Integer getRuleId() {
		return this.ruleId;
	}

	public void setRuleId(Integer ruleId) {
		this.ruleId = ruleId;
	}

	public Channel getOimChannels() {
		return this.oimChannels;
	}

	public void setOimChannels(Channel oimChannels) {
		this.oimChannels = oimChannels;
	}

	public Integer getProcessAll() {
		return this.processAll;
	}

	public void setProcessAll(Integer processAll) {
		this.processAll = processAll;
	}

	public String getProcessWithStatus() {
		return this.processWithStatus;
	}

	public void setProcessWithStatus(String processWithStatus) {
		this.processWithStatus = processWithStatus;
	}

	public Integer getUpdateStoreOrderStatus() {
		return this.updateStoreOrderStatus;
	}

	public void setUpdateStoreOrderStatus(Integer updateStoreOrderStatus) {
		this.updateStoreOrderStatus = updateStoreOrderStatus;
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

	public Integer getSupplierId() {
		return this.supplierId;
	}

	public void setSupplierId(Integer supplierId) {
		this.supplierId = supplierId;
	}

	public String getUpdateWithStatus() {
		return this.updateWithStatus;
	}

	public void setUpdateWithStatus(String updateWithStatus) {
		this.updateWithStatus = updateWithStatus;
	}

	public static OrderProcessingRule from(OimOrderProcessingRule source) {
		OrderProcessingRule target = new OrderProcessingRule();
		BeanUtils
				.copyProperties(source, target, new String[] { "oimChannels" });
		return target;
	}

}
