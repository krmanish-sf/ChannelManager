package com.is.cm.core.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import salesmachine.hibernatedb.OimSupplierMethods;
import salesmachine.hibernatedb.OimSuppliers;

public class Supplier extends DomainBase {
	private static final Logger log = LoggerFactory.getLogger(Supplier.class);
	private static final String[] IGNORE_PROPERTIES = new String[] {
			"oimSupplierMethodses", "oimVendorsuppOrderhistories",
			"oimOrderDetailses", "oimVendorSupplierses",
			"oimChannelSupplierMaps" };
	private static final long serialVersionUID = 3689061364663948510L;
	private Integer supplierId;
	private String supplierName;
	private Integer isCustom;
	private Date insertionTm;
	private Date deleteTm;
	private String description;
	private String defaultSkuPrefix;
	@JsonDeserialize(as = HashSet.class)
	private Set<SupplierMethod> oimSupplierMethodses = new HashSet<SupplierMethod>();
	@JsonDeserialize(as = HashSet.class)
	private Set<VendorsuppOrderhistory> oimVendorsuppOrderhistories = new HashSet<VendorsuppOrderhistory>(
			0);
	@JsonDeserialize(as = HashSet.class)
	private Set<OrderDetail> oimOrderDetailses = new HashSet<OrderDetail>(0);
	@JsonDeserialize(as = HashSet.class)
	private Set<VendorSupplier> oimVendorSupplierses = new HashSet<VendorSupplier>(
			0);
	@JsonDeserialize(as = HashSet.class)
	private Set<ChannelSupplierMap> oimChannelSupplierMaps = new HashSet<ChannelSupplierMap>(
			0);

	public static Supplier from(OimSuppliers oimSuppliers) {
		if (oimSuppliers == null)
			return null;
		Supplier supplier = new Supplier();
		BeanUtils.copyProperties(oimSuppliers, supplier, IGNORE_PROPERTIES);
		Set<OimSupplierMethods> iterator = oimSuppliers
				.getOimSupplierMethodses();
		try {
			for (OimSupplierMethods oimSupplierMethods : iterator) {
				supplier.oimSupplierMethodses.add(SupplierMethod
						.from(oimSupplierMethods));
			}
		} catch (HibernateException e) {
			log.warn(e.getMessage());
		}
		return supplier;
	}

	public OimSuppliers toOimSupplier() {
		OimSuppliers oimSuppliers = new OimSuppliers();
		BeanUtils.copyProperties(this, oimSuppliers, IGNORE_PROPERTIES);
		Iterator<SupplierMethod> iterator = this.getOimSupplierMethodses()
				.iterator();
		while (iterator.hasNext()) {
			SupplierMethod methods = iterator.next();
			oimSuppliers.getOimSupplierMethodses().add(methods.to());
		}
		return oimSuppliers;
	}

	public Supplier() {
	}

	public Integer getSupplierId() {
		return this.supplierId;
	}

	public void setSupplierId(Integer supplierId) {
		this.supplierId = supplierId;
	}

	public String getSupplierName() {
		return this.supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public Integer getIsCustom() {
		return this.isCustom;
	}

	public void setIsCustom(Integer isCustom) {
		this.isCustom = isCustom;
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

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<SupplierMethod> getOimSupplierMethodses() {
		return this.oimSupplierMethodses;
	}

	public void setOimSupplierMethodses(Set<SupplierMethod> oimSupplierMethodses) {
		this.oimSupplierMethodses = oimSupplierMethodses;
	}

	public Set<VendorsuppOrderhistory> getOimVendorsuppOrderhistories() {
		return this.oimVendorsuppOrderhistories;
	}

	public void setOimVendorsuppOrderhistories(
			Set<VendorsuppOrderhistory> oimVendorsuppOrderhistories) {
		this.oimVendorsuppOrderhistories = oimVendorsuppOrderhistories;
	}

	public Set<OrderDetail> getOimOrderDetailses() {
		return this.oimOrderDetailses;
	}

	public void setOimOrderDetailses(Set<OrderDetail> oimOrderDetailses) {
		this.oimOrderDetailses = oimOrderDetailses;
	}

	public Set<VendorSupplier> getOimVendorSupplierses() {
		return this.oimVendorSupplierses;
	}

	public void setOimVendorSupplierses(Set<VendorSupplier> oimVendorSupplierses) {
		this.oimVendorSupplierses = oimVendorSupplierses;
	}

	public Set<ChannelSupplierMap> getOimChannelSupplierMaps() {
		return this.oimChannelSupplierMaps;
	}

	public void setOimChannelSupplierMaps(
			Set<ChannelSupplierMap> oimChannelSupplierMaps) {
		this.oimChannelSupplierMaps = oimChannelSupplierMaps;
	}

  public String getDefaultSkuPrefix() {
    return defaultSkuPrefix;
  }

  public void setDefaultSkuPrefix(String defaultSkuPrefix) {
    this.defaultSkuPrefix = defaultSkuPrefix;
  }
}
