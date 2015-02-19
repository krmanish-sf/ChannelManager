package com.is.cm.core.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeanUtils;

import salesmachine.hibernatedb.Vendors;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class Vendor extends DomainBase implements java.io.Serializable {
	private static final long serialVersionUID = -1388260578533847229L;
	private Integer vendorId;
	private Integer vendorStatusId;
	private Integer primaryContactId;
	private String company;
	private Date nextChargeDate;
	private Double creditAvailable;
	private Double creditUsed;
	private Integer billingFailures;
	private String affiliateCustomerId;
	private Integer affiliateSiteId;
	private String istoreLogin;
	private String istorePassword;
	private Date istoreCreationDate;
	private Date istoreEmailDate;
	private Integer istoreCount;
	private Integer updateServer;
	private Integer updateDelaySeconds;
	private Double proratedBillingOverage;
	@JsonDeserialize(as = HashSet.class)
	private Set<Channel> oimChannelses;
	@JsonDeserialize(as = HashSet.class)
	private Set<VendorsuppOrderhistory> oimVendorsuppOrderhistories;
	@JsonDeserialize(as = HashSet.class)
	private Set<VendorSupplier> oimVendorSupplierses;

	private static final String[] excludeFromAutoCopy = { "oimChannelses",
			"oimVendorsuppOrderhistories", "oimVendorSupplierses" };

	public Vendor() {
	}

	public Integer getVendorId() {
		return this.vendorId;
	}

	public void setVendorId(Integer vendorId) {
		this.vendorId = vendorId;
	}

	public Integer getVendorStatusId() {
		return this.vendorStatusId;
	}

	public void setVendorStatusId(Integer vendorStatusId) {
		this.vendorStatusId = vendorStatusId;
	}

	public Integer getPrimaryContactId() {
		return this.primaryContactId;
	}

	public void setPrimaryContactId(Integer primaryContactId) {
		this.primaryContactId = primaryContactId;
	}

	public String getCompany() {
		return this.company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public Date getNextChargeDate() {
		return this.nextChargeDate;
	}

	public void setNextChargeDate(Date nextChargeDate) {
		this.nextChargeDate = nextChargeDate;
	}

	public Double getCreditAvailable() {
		return this.creditAvailable;
	}

	public void setCreditAvailable(Double creditAvailable) {
		this.creditAvailable = creditAvailable;
	}

	public Double getCreditUsed() {
		return this.creditUsed;
	}

	public void setCreditUsed(Double creditUsed) {
		this.creditUsed = creditUsed;
	}

	public Integer getBillingFailures() {
		return this.billingFailures;
	}

	public void setBillingFailures(Integer billingFailures) {
		this.billingFailures = billingFailures;
	}

	public String getAffiliateCustomerId() {
		return this.affiliateCustomerId;
	}

	public void setAffiliateCustomerId(String affiliateCustomerId) {
		this.affiliateCustomerId = affiliateCustomerId;
	}

	public Integer getAffiliateSiteId() {
		return this.affiliateSiteId;
	}

	public void setAffiliateSiteId(Integer affiliateSiteId) {
		this.affiliateSiteId = affiliateSiteId;
	}

	public String getIstoreLogin() {
		return this.istoreLogin;
	}

	public void setIstoreLogin(String istoreLogin) {
		this.istoreLogin = istoreLogin;
	}

	public String getIstorePassword() {
		return this.istorePassword;
	}

	public void setIstorePassword(String istorePassword) {
		this.istorePassword = istorePassword;
	}

	public Date getIstoreCreationDate() {
		return this.istoreCreationDate;
	}

	public void setIstoreCreationDate(Date istoreCreationDate) {
		this.istoreCreationDate = istoreCreationDate;
	}

	public Date getIstoreEmailDate() {
		return this.istoreEmailDate;
	}

	public void setIstoreEmailDate(Date istoreEmailDate) {
		this.istoreEmailDate = istoreEmailDate;
	}

	public Integer getIstoreCount() {
		return this.istoreCount;
	}

	public void setIstoreCount(Integer istoreCount) {
		this.istoreCount = istoreCount;
	}

	public Integer getUpdateServer() {
		return this.updateServer;
	}

	public void setUpdateServer(Integer updateServer) {
		this.updateServer = updateServer;
	}

	public Integer getUpdateDelaySeconds() {
		return this.updateDelaySeconds;
	}

	public void setUpdateDelaySeconds(Integer updateDelaySeconds) {
		this.updateDelaySeconds = updateDelaySeconds;
	}

	public Double getProratedBillingOverage() {
		return this.proratedBillingOverage;
	}

	public void setProratedBillingOverage(Double proratedBillingOverage) {
		this.proratedBillingOverage = proratedBillingOverage;
	}

	public Set<Channel> getOimChannelses() {
		return this.oimChannelses;
	}

	public void setOimChannelses(Set<Channel> oimChannelses) {
		this.oimChannelses = oimChannelses;
	}

	public Set<VendorsuppOrderhistory> getOimVendorsuppOrderhistories() {
		return this.oimVendorsuppOrderhistories;
	}

	public void setOimVendorsuppOrderhistories(
			Set<VendorsuppOrderhistory> oimVendorsuppOrderhistories) {
		this.oimVendorsuppOrderhistories = oimVendorsuppOrderhistories;
	}

	public Set<VendorSupplier> getOimVendorSupplierses() {
		return this.oimVendorSupplierses;
	}

	public void setOimVendorSupplierses(Set<VendorSupplier> oimVendorSupplierses) {
		this.oimVendorSupplierses = oimVendorSupplierses;
	}

	public static Vendor from(Vendors vendors) {
		Vendor vendor = new Vendor();
		BeanUtils.copyProperties(vendors, vendor, excludeFromAutoCopy);
		return vendor;
	}

	public Vendors toOimVendors() {
		Vendors vendors = new Vendors();
		BeanUtils.copyProperties(this, vendors, excludeFromAutoCopy);
		return vendors;
	}

}
