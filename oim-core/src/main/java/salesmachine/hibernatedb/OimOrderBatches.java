package salesmachine.hibernatedb;

// Generated 30 Mar, 2010 7:26:10 PM by Hibernate Tools 3.2.4.GA

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * OimOrderBatches generated by hbm2java
 */
public class OimOrderBatches implements java.io.Serializable {
  private static final long serialVersionUID = 1L;
  private Integer batchId;
  private OimOrderBatchesTypes oimOrderBatchesTypes;
  private OimChannels oimChannels;
  private Date creationTm;
  private Date insertionTm;
  private Date deleteTm;
  private String description;
  private Integer errorCode;
  private Set<OimUploadedFiles> oimUploadedFileses = new HashSet<OimUploadedFiles>(0);
  private Set<OimOrders> oimOrderses = new HashSet<OimOrders>(0);

  public OimOrderBatches() {
  }

  public Integer getBatchId() {
    return this.batchId;
  }

  public void setBatchId(Integer batchId) {
    this.batchId = batchId;
  }

  public OimOrderBatchesTypes getOimOrderBatchesTypes() {
    return this.oimOrderBatchesTypes;
  }

  public void setOimOrderBatchesTypes(OimOrderBatchesTypes oimOrderBatchesTypes) {
    this.oimOrderBatchesTypes = oimOrderBatchesTypes;
  }

  public OimChannels getOimChannels() {
    return this.oimChannels;
  }

  public void setOimChannels(OimChannels oimChannels) {
    this.oimChannels = oimChannels;
  }

  public Date getCreationTm() {
    return this.creationTm;
  }

  public void setCreationTm(Date creationTm) {
    this.creationTm = creationTm;
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

  public Set<OimUploadedFiles> getOimUploadedFileses() {
    return this.oimUploadedFileses;
  }

  public void setOimUploadedFileses(Set<OimUploadedFiles> oimUploadedFileses) {
    this.oimUploadedFileses = oimUploadedFileses;
  }

  public Set<OimOrders> getOimOrderses() {
    return this.oimOrderses;
  }

  public void setOimOrderses(Set<OimOrders> oimOrderses) {
    this.oimOrderses = oimOrderses;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(Integer errorCode) {
    this.errorCode = errorCode;
  }

}
