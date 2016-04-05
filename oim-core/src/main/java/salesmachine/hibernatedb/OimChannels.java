package salesmachine.hibernatedb;

// Generated 30 Mar, 2010 7:26:10 PM by Hibernate Tools 3.2.4.GA

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * OimChannels generated by hbm2java
 */
public class OimChannels implements java.io.Serializable {
  private static final long serialVersionUID = 1L;
  private Integer channelId;
  private Vendors vendors;
  private OimSupportedChannels oimSupportedChannels;
  private String channelName;
  private Integer emailNotifications;
  private Integer enableOrderAutomation;
  private Date insertionTm;
  private Date deleteTm;
  private Date lastFetchTm;
  private Integer testMode = 0;
  private Integer onlyPullMatchingOrders = 0;

  private String sequenceNumber;
  private Set<OimChannelAccessDetails> oimChannelAccessDetailses = new HashSet<>(0);
  private Set oimOrderBatcheses = new HashSet(0);
  private Set<OimOrderProcessingRule> oimOrderProcessingRules = new HashSet<>(0);
  private Set oimUploadedFileses = new HashSet(0);
  private Set<OimChannelSupplierMap> oimChannelSupplierMaps = new HashSet<>(0);
  private Set oimChannelFileses = new HashSet(0);
  private Set<OimChannelShippingMap> oimChannelShippingMap = new HashSet<>();

  public OimChannels() {
  }

  public Integer getChannelId() {
    return this.channelId;
  }

  public void setChannelId(Integer channelId) {
    this.channelId = channelId;
  }

  public Vendors getVendors() {
    return this.vendors;
  }

  public void setVendors(Vendors vendors) {
    this.vendors = vendors;
  }

  public OimSupportedChannels getOimSupportedChannels() {
    return this.oimSupportedChannels;
  }

  public void setOimSupportedChannels(OimSupportedChannels oimSupportedChannels) {
    this.oimSupportedChannels = oimSupportedChannels;
  }

  public String getChannelName() {
    return this.channelName;
  }

  public void setChannelName(String channelName) {
    this.channelName = channelName;
  }

  public Integer getEmailNotifications() {
    return this.emailNotifications;
  }

  public void setEmailNotifications(Integer emailNotifications) {
    this.emailNotifications = emailNotifications;
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

  public Set<OimChannelAccessDetails> getOimChannelAccessDetailses() {
    return this.oimChannelAccessDetailses;
  }

  public void setOimChannelAccessDetailses(Set<OimChannelAccessDetails> oimChannelAccessDetailses) {
    this.oimChannelAccessDetailses = oimChannelAccessDetailses;
  }

  public Set getOimOrderBatcheses() {
    return this.oimOrderBatcheses;
  }

  public void setOimOrderBatcheses(Set oimOrderBatcheses) {
    this.oimOrderBatcheses = oimOrderBatcheses;
  }

  public Set<OimOrderProcessingRule> getOimOrderProcessingRules() {
    return this.oimOrderProcessingRules;
  }

  public void setOimOrderProcessingRules(Set<OimOrderProcessingRule> oimOrderProcessingRules) {
    this.oimOrderProcessingRules = oimOrderProcessingRules;
  }

  public Set getOimUploadedFileses() {
    return this.oimUploadedFileses;
  }

  public void setOimUploadedFileses(Set oimUploadedFileses) {
    this.oimUploadedFileses = oimUploadedFileses;
  }

  public Set<OimChannelSupplierMap> getOimChannelSupplierMaps() {
    return this.oimChannelSupplierMaps;
  }

  public void setOimChannelSupplierMaps(Set<OimChannelSupplierMap> oimChannelSupplierMaps) {
    this.oimChannelSupplierMaps = oimChannelSupplierMaps;
  }

  public Set getOimChannelFileses() {
    return this.oimChannelFileses;
  }

  public void setOimChannelFileses(Set oimChannelFileses) {
    this.oimChannelFileses = oimChannelFileses;
  }

  public Date getLastFetchTm() {
    return lastFetchTm;
  }

  public void setLastFetchTm(Date lastFetchTm) {
    this.lastFetchTm = lastFetchTm;
  }

  public Integer getTestMode() {
    return testMode;
  }

  public void setTestMode(Integer testMode) {
    this.testMode = testMode;
  }

  public Set<OimChannelShippingMap> getOimChannelShippingMap() {
    return oimChannelShippingMap;
  }

  public void setOimChannelShippingMap(Set<OimChannelShippingMap> oimChannelShippingMap) {
    this.oimChannelShippingMap = oimChannelShippingMap;
  }

  public String getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(String sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }
  
  public Integer getOnlyPullMatchingOrders() {
    return onlyPullMatchingOrders;
  }

  public void setOnlyPullMatchingOrders(Integer onlyPullMatchingOrders) {
    this.onlyPullMatchingOrders = onlyPullMatchingOrders;
  }

}
