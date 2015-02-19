package com.is.cm.core.domain;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeanUtils;

import salesmachine.hibernatedb.OimOrderBatchesTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderBatchesType extends DomainBase implements
		java.io.Serializable {
	private static final long serialVersionUID = -154857916627665725L;
	private Integer batchTypeId;
	private String batchTypeName;
	@JsonDeserialize(as = HashSet.class)
	//@JsonManagedReference("OrderBatchesType-OrderBatch")
	private Set<OrderBatch> oimOrderBatcheses;

	public OrderBatchesType() {
	}

	public Integer getBatchTypeId() {
		return this.batchTypeId;
	}

	public void setBatchTypeId(Integer batchTypeId) {
		this.batchTypeId = batchTypeId;
	}

	public String getBatchTypeName() {
		return this.batchTypeName;
	}

	public void setBatchTypeName(String batchTypeName) {
		this.batchTypeName = batchTypeName;
	}

	public Set<OrderBatch> getOimOrderBatcheses() {
		return this.oimOrderBatcheses;
	}

	public void setOimOrderBatcheses(Set<OrderBatch> oimOrderBatcheses) {
		this.oimOrderBatcheses = oimOrderBatcheses;
	}

	public static OrderBatchesType from(
			OimOrderBatchesTypes oimOrderBatchesTypes) {
		if (oimOrderBatchesTypes == null)
			return null;
		OrderBatchesType orderBatchesType = new OrderBatchesType();
		BeanUtils.copyProperties(oimOrderBatchesTypes, orderBatchesType,
				new String[] { "oimOrderBatcheses" });
		return orderBatchesType;
	}

}
