package com.is.cm.core.domain;

import java.util.Date;

import org.springframework.beans.BeanUtils;

public class Reps extends DomainBase implements java.io.Serializable {
	private static final long serialVersionUID = 8887900875931158943L;
	private Integer repId;
	private String firstName;
	private String lastName;
	private Integer repTypeId;
	private Integer loginAttempts;
	private String login;
	private String password;
	private Integer repStatusId;
	private Integer vendorId;
	private Date lastLoginTm;
	private Date registrationTm;
	private Date verifyTm;
	private Integer referralTrackingId;
	private Integer emailListRemove;
	private Integer kbadminAllowed;
	private Integer cmAllowed;
	private Integer privateLabelDropshipperId;
	private Integer statId;
	private Integer supportCid;
	private Integer autoActivation;
	private Date activationTm;
	private String activationKey;
	private Integer promoterId;

	public Reps() {
	}

	public Integer getRepId() {
		return this.repId;
	}

	public void setRepId(Integer repId) {
		this.repId = repId;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Integer getRepTypeId() {
		return this.repTypeId;
	}

	public void setRepTypeId(Integer repTypeId) {
		this.repTypeId = repTypeId;
	}

	public Integer getLoginAttempts() {
		return this.loginAttempts;
	}

	public void setLoginAttempts(Integer loginAttempts) {
		this.loginAttempts = loginAttempts;
	}

	public String getLogin() {
		return this.login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getRepStatusId() {
		return this.repStatusId;
	}

	public void setRepStatusId(Integer repStatusId) {
		this.repStatusId = repStatusId;
	}

	public Integer getVendorId() {
		return this.vendorId;
	}

	public void setVendorId(Integer vendorId) {
		this.vendorId = vendorId;
	}

	public Date getLastLoginTm() {
		return this.lastLoginTm;
	}

	public void setLastLoginTm(Date lastLoginTm) {
		this.lastLoginTm = lastLoginTm;
	}

	public Date getRegistrationTm() {
		return this.registrationTm;
	}

	public void setRegistrationTm(Date registrationTm) {
		this.registrationTm = registrationTm;
	}

	public Date getVerifyTm() {
		return this.verifyTm;
	}

	public void setVerifyTm(Date verifyTm) {
		this.verifyTm = verifyTm;
	}

	public Integer getReferralTrackingId() {
		return this.referralTrackingId;
	}

	public void setReferralTrackingId(Integer referralTrackingId) {
		this.referralTrackingId = referralTrackingId;
	}

	public Integer getEmailListRemove() {
		return this.emailListRemove;
	}

	public void setEmailListRemove(Integer emailListRemove) {
		this.emailListRemove = emailListRemove;
	}

	public Integer getKbadminAllowed() {
		return this.kbadminAllowed;
	}

	public void setKbadminAllowed(Integer kbadminAllowed) {
		this.kbadminAllowed = kbadminAllowed;
	}

	public Integer getCmAllowed() {
		return this.cmAllowed;
	}

	public void setCmAllowed(Integer kbadminAllowed) {
		this.cmAllowed = kbadminAllowed;
	}

	public Integer getPrivateLabelDropshipperId() {
		return this.privateLabelDropshipperId;
	}

	public void setPrivateLabelDropshipperId(Integer privateLabelDropshipperId) {
		this.privateLabelDropshipperId = privateLabelDropshipperId;
	}

	public Integer getStatId() {
		return this.statId;
	}

	public void setStatId(Integer statId) {
		this.statId = statId;
	}

	public Integer getSupportCid() {
		return this.supportCid;
	}

	public void setSupportCid(Integer supportCid) {
		this.supportCid = supportCid;
	}

	public Integer getAutoActivation() {
		return this.autoActivation;
	}

	public void setAutoActivation(Integer autoActivation) {
		this.autoActivation = autoActivation;
	}

	public Date getActivationTm() {
		return this.activationTm;
	}

	public void setActivationTm(Date activationTm) {
		this.activationTm = activationTm;
	}

	public String getActivationKey() {
		return this.activationKey;
	}

	public void setActivationKey(String activationKey) {
		this.activationKey = activationKey;
	}

	public Integer getPromoterId() {
		return this.promoterId;
	}

	public void setPromoterId(Integer promoterId) {
		this.promoterId = promoterId;
	}

	public static Reps from(salesmachine.hibernatedb.Reps reps) {
		if (reps == null)
			return null;
		Reps target = new Reps();
		BeanUtils.copyProperties(reps, target);
		return target;
	}
}
