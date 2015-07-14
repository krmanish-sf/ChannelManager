package com.is.cm.core.domain;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeanUtils;

import salesmachine.hibernatedb.OimOrderStatuses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderStatus extends DomainBase implements java.io.Serializable {
	private static final long serialVersionUID = -8437752144951370396L;
	private Integer statusId;
	private String statusValue;

	private Set<OrderDetail> oimOrderDetailses = new HashSet<OrderDetail>(0);

	public OrderStatus() {
	}

	public OrderStatus(Integer statusId) {
		this.statusId = statusId;
	}

	public Integer getStatusId() {
		return this.statusId;
	}

	public void setStatusId(Integer statusId) {
		this.statusId = statusId;
	}

	public String getStatusValue() {
		return this.statusValue;
	}

	public void setStatusValue(String statusValue) {
		this.statusValue = statusValue;
	}

	public Set<OrderDetail> getOimOrderDetailses() {
		return this.oimOrderDetailses;
	}

	public void setOimOrderDetailses(Set<OrderDetail> oimOrderDetailses) {
		this.oimOrderDetailses = oimOrderDetailses;
	}

	public static OrderStatus from(OimOrderStatuses oimOrderStatuses) {
		if (oimOrderStatuses == null)
			return null;
		OrderStatus orderStatus = new OrderStatus();
		BeanUtils.copyProperties(oimOrderStatuses, orderStatus,
				new String[] { "oimOrderDetailses" });
		return orderStatus;
	}

	public OimOrderStatuses toOimOrderStatus() {
		return new OimOrderStatuses(this.getStatusId());
	}
}
