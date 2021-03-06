package com.is.cm.core.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import salesmachine.hibernatedb.OimChannelAccessDetails;
import salesmachine.hibernatedb.OimChannelSupplierMap;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderProcessingRule;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Channel extends DomainBase implements Serializable {
  private static final long serialVersionUID = 2802712946146646361L;
  private Integer channelId;
  private Vendor vendors;
  private SupportedChannel oimSupportedChannels;
  private String channelName;
  private Integer emailNotifications;
  private Integer enableOrderAutomation;
  private Date insertionTm;
  private Date deleteTm;
  private Date lastFetchTm;
  private boolean testMode;
  private boolean onlyPullMatchingOrders;

  @JsonDeserialize(as = HashSet.class)
  private Set<ChannelAccessDetail> oimChannelAccessDetailses;
  @JsonDeserialize(as = HashSet.class)
  private Set<OrderBatch> oimOrderBatcheses;
  @JsonDeserialize(as = HashSet.class)
  private Set<OrderProcessingRule> oimOrderProcessingRules;
  @JsonDeserialize(as = HashSet.class)
  private Set<UploadedFile> oimUploadedFileses;
  @JsonDeserialize(as = HashSet.class)
  private Set<ChannelSupplierMap> oimChannelSupplierMaps;
  @JsonDeserialize(as = HashSet.class)
  private Set<ChannelFile> oimChannelFileses;

  public Channel() {
  }

  public Integer getChannelId() {
    return this.channelId;
  }

  public void setChannelId(Integer channelId) {
    this.channelId = channelId;
  }

  public Vendor getVendors() {
    return this.vendors;
  }

  public void setVendors(Vendor vendor) {
    this.vendors = vendor;
  }

  public SupportedChannel getOimSupportedChannels() {
    return this.oimSupportedChannels;
  }

  public void setOimSupportedChannels(SupportedChannel oimSupportedChannels) {
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

  public Set<ChannelAccessDetail> getOimChannelAccessDetailses() {
    return this.oimChannelAccessDetailses;
  }

  public void setOimChannelAccessDetailses(Set<ChannelAccessDetail> oimChannelAccessDetailses) {
    this.oimChannelAccessDetailses = oimChannelAccessDetailses;
  }

  public Set<OrderBatch> getOimOrderBatcheses() {
    return this.oimOrderBatcheses;
  }

  public void setOimOrderBatcheses(Set<OrderBatch> oimOrderBatcheses) {
    this.oimOrderBatcheses = oimOrderBatcheses;
  }

  public Set<OrderProcessingRule> getOimOrderProcessingRules() {
    return this.oimOrderProcessingRules;
  }

  public void setOimOrderProcessingRules(Set<OrderProcessingRule> oimOrderProcessingRules) {
    this.oimOrderProcessingRules = oimOrderProcessingRules;
  }

  public Set<UploadedFile> getOimUploadedFileses() {
    return this.oimUploadedFileses;
  }

  public void setOimUploadedFileses(Set<UploadedFile> oimUploadedFileses) {
    this.oimUploadedFileses = oimUploadedFileses;
  }

  public Set<ChannelSupplierMap> getOimChannelSupplierMaps() {
    return this.oimChannelSupplierMaps;
  }

  public void setOimChannelSupplierMaps(Set<ChannelSupplierMap> oimChannelSupplierMaps) {
    this.oimChannelSupplierMaps = oimChannelSupplierMaps;
  }

  public Set<ChannelFile> getOimChannelFileses() {
    return this.oimChannelFileses;
  }

  public void setOimChannelFileses(Set<ChannelFile> oimChannelFileses) {
    this.oimChannelFileses = oimChannelFileses;
  }
  public boolean isOnlyPullMatchingOrders() {
    return onlyPullMatchingOrders;
  }

  public void setOnlyPullMatchingOrders(boolean onlyPullMatchingOrders) {
    this.onlyPullMatchingOrders = onlyPullMatchingOrders;
  }

  public static Channel from(OimChannels oimChannel) {
    if (oimChannel == null)
      return null;
    Channel target = new Channel();
    target.setChannelId(oimChannel.getChannelId());
    target.setChannelName(oimChannel.getChannelName());
    target.setEmailNotifications(oimChannel.getEmailNotifications());
    target.setEnableOrderAutomation(oimChannel.getEnableOrderAutomation());
    target.setOimSupportedChannels(SupportedChannel.from(oimChannel.getOimSupportedChannels()));
    target.setLastFetchTm(oimChannel.getLastFetchTm());
    target.setInsertionTm(oimChannel.getInsertionTm());
    target.setTestMode(oimChannel.getTestMode() != null && oimChannel.getTestMode() == 1);
    target.setVendors(Vendor.from(oimChannel.getVendors()));
    target.setOnlyPullMatchingOrders(oimChannel.getOnlyPullMatchingOrders() !=null && oimChannel.getOnlyPullMatchingOrders() ==1);
    if (oimChannel.getOimChannelAccessDetailses() != null) {
      Set<ChannelAccessDetail> channelAccessDetails = new HashSet<ChannelAccessDetail>();
      for (OimChannelAccessDetails oimChannelAccessDetails : oimChannel
          .getOimChannelAccessDetailses()) {
        channelAccessDetails.add(ChannelAccessDetail.from(oimChannelAccessDetails));
      }
      target.setOimChannelAccessDetailses(channelAccessDetails);
    }
    Set<OimOrderProcessingRule> rules = oimChannel.getOimOrderProcessingRules();
    target.oimOrderProcessingRules = new HashSet<OrderProcessingRule>();
    if (rules != null) {
      for (OimOrderProcessingRule oimOrderProcessingRule : rules) {
        target.oimOrderProcessingRules.add(OrderProcessingRule.from(oimOrderProcessingRule));
      }
    }

    Set<OimChannelSupplierMap> maps = oimChannel.getOimChannelSupplierMaps();
    target.oimChannelSupplierMaps = new HashSet<ChannelSupplierMap>();
    if (maps != null) {
      for (OimChannelSupplierMap map : maps) {
        target.oimChannelSupplierMaps.add(ChannelSupplierMap.from(map));
      }
    }
    return target;
  }

  public Date getLastFetchTm() {
    return lastFetchTm;
  }

  public void setLastFetchTm(Date lastFetchTm) {
    this.lastFetchTm = lastFetchTm;
  }

  public String getLastFetch() {
    if (lastFetchTm == null)
      return null;
    Date now = new Date();
    String lapsedTm = "";
    long lapsedTime = now.getTime() - lastFetchTm.getTime();
    if (lapsedTime / (1000 * 60) < 60) {
      lapsedTm = (lapsedTime / (1000 * 60)) + " mins ago";
    } else {
      lapsedTm = (lapsedTime / (1000 * 60 * 60)) + " hours ago";
    }
    return lapsedTm;
  }

  public boolean isTestMode() {
    return testMode;
  }

  public void setTestMode(boolean testMode) {
    this.testMode = testMode;
  }
}
