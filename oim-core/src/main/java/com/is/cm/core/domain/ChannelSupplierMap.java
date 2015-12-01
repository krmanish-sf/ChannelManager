package com.is.cm.core.domain;

import java.util.Date;

import org.springframework.beans.BeanUtils;

import salesmachine.hibernatedb.OimChannelSupplierMap;

public class ChannelSupplierMap extends DomainBase implements
		java.io.Serializable {
	private static final long serialVersionUID = -6122877419112750510L;
	private Integer mapId;
	private Channel channel;
	private Supplier oimSuppliers;
	private String supplierPrefix;
	private Integer enableOrderAutomation;
	private Date insertionTm;
	private Date deleteTm;
	private String warehouseLocation;

  public ChannelSupplierMap() {
	}

	public ChannelSupplierMap(Channel channel, Supplier oimSuppliers,
			String supplierPrefix, Integer enableOrderAutomation,
			Date insertionTm, Date deleteTm) {
		this.channel = channel;
		this.oimSuppliers = oimSuppliers;
		this.supplierPrefix = supplierPrefix;
		this.enableOrderAutomation = enableOrderAutomation;
		this.insertionTm = insertionTm;
		this.deleteTm = deleteTm;
	}

	public Integer getMapId() {
		return this.mapId;
	}

	public void setMapId(Integer mapId) {
		this.mapId = mapId;
	}

	public Channel getOimChannels() {
		return this.channel;
	}

	public void setOimChannels(Channel channel) {
		this.channel = channel;
	}

	public Supplier getOimSuppliers() {
		return this.oimSuppliers;
	}

	public void setOimSuppliers(Supplier oimSuppliers) {
		this.oimSuppliers = oimSuppliers;
	}

	public String getSupplierPrefix() {
		return this.supplierPrefix;
	}

	public void setSupplierPrefix(String supplierPrefix) {
		this.supplierPrefix = supplierPrefix;
	}

	public Integer getEnableOrderAutomation() {
		return this.enableOrderAutomation;
	}

	public void setEnableOrderAutomation(Integer enableOrderAutomation) {
		this.enableOrderAutomation = enableOrderAutomation;
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
	
  public String getWarehouseLocation() {
    return warehouseLocation;
  }

  public void setWarehouseLocation(String warehouseLocation) {
    this.warehouseLocation = warehouseLocation;
  }

	public static ChannelSupplierMap from(OimChannelSupplierMap map) {
		ChannelSupplierMap target = new ChannelSupplierMap();
		BeanUtils.copyProperties(map, target, new String[] { "oimChannels",
				"oimSuppliers" });
		target.oimSuppliers = Supplier.from(map.getOimSuppliers());
		return target;
	}

}
