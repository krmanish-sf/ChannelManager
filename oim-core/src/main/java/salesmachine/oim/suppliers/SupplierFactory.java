package salesmachine.oim.suppliers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import salesmachine.automation.AutomationManager;
import salesmachine.hibernatedb.OimChannelSupplierMap;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimFields;
import salesmachine.hibernatedb.OimFileFieldMap;
import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderBatchesTypes;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderStatuses;
import salesmachine.hibernatedb.OimOrderTracking;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSupplierMethods;
import salesmachine.hibernatedb.OimSuppliers;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatehelper.PojoHelper;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.ChannelBase;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.stores.impl.ChannelFactory;
import salesmachine.oim.suppliers.exception.InvalidAddressException;
import salesmachine.oim.suppliers.exception.SupplierCommunicationException;
import salesmachine.oim.suppliers.exception.SupplierConfigurationException;
import salesmachine.oim.suppliers.exception.SupplierOrderException;
import salesmachine.oim.suppliers.exception.SupplierOrderTrackingException;
import salesmachine.oim.suppliers.modal.OrderDetailResponse;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.oim.suppliers.modal.TrackingData;
import salesmachine.util.FtpFileUploader;
import salesmachine.util.OimLogStream;
import salesmachine.util.StringHandle;

public class SupplierFactory {
  private static Logger log = LoggerFactory.getLogger(SupplierFactory.class);
  private final Session m_dbSession;
  private OimSuppliers m_supplier;
  private OimSupplierMethods m_supplierMethods;

  // public boolean m_RunModePlaceOrders = true;
  // public boolean m_RunModeUpdateOrdersStatus = true;
  public float m_RunModeFailPct = 20;
  @Deprecated
  protected OimLogStream logStream;

  public SupplierFactory(Session dbSession) {
    this.m_dbSession = dbSession;
    logStream = new OimLogStream();
  }

  @Deprecated
  public boolean init(int supplierId, Session dbSession, OimLogStream ols) {
    if (ols != null) {
      logStream = ols;
    }
    if (logStream == null) {
      logStream = new OimLogStream();
    }
    return init(supplierId);
  }

  @Deprecated
  private boolean init(int supplierId) {
    try {

      // Here is your db code
      Query query = m_dbSession.createQuery(
          "from salesmachine.hibernatedb.OimSuppliers as s where s.supplierId=:id and s.deleteTm is null");
      query.setInteger("id", supplierId);
      if (!query.iterate().hasNext()) {
        log.debug("No supplier found for supplier id: " + supplierId);
        return false;
      } else {
        m_supplier = (OimSuppliers) query.iterate().next();

        query = m_dbSession.createQuery(
            "select m from OimSupplierMethods as m left join m.oimSupplierMethodattrValueses v where m.deleteTm is null and m.oimSuppliers=:supp and m.oimSupplierMethodTypes.methodTypeId=:methodTypeId and v.deleteTm is null");
        query.setEntity("supp", m_supplier);
        // query.setInteger("methodTypeId", OimConstants.SUPPLIER_METHOD_TYPE_ORDERPUSH.intValue());
        Iterator it = query.iterate();
        if (!it.hasNext()) {
          logStream.println("No method found for pushing orders to this supplier");
          return false;
        }
        m_supplierMethods = (OimSupplierMethods) it.next();
      }
    } catch (RuntimeException e) {
      e.printStackTrace();
    }
    return true;
  }

  public String reprocessVendorOrder(Integer vendorId, OimOrders oimOrders)
      throws SupplierConfigurationException, SupplierCommunicationException,
      SupplierOrderException {
    for (Iterator detailIt = oimOrders.getOimOrderDetailses().iterator(); detailIt.hasNext();) {
      OimOrderDetails detail = (OimOrderDetails) detailIt.next();
      detail.setOimOrderStatuses(new OimOrderStatuses(OimConstants.ORDER_STATUS_UNPROCESSED));
      detail.setSupplierOrderStatus("Re-processing");
      m_dbSession.persist(detail);
    }
    return processVendorOrder(vendorId, oimOrders,
        new OimOrderBatchesTypes(OimConstants.ORDERBATCH_TYPE_ID_MANUAL));
  }

  public String processVendorOrder(Integer vendorId, OimOrders oimOrders)
      throws SupplierConfigurationException, SupplierCommunicationException,
      SupplierOrderException {
    return processVendorOrder(vendorId, oimOrders,
        new OimOrderBatchesTypes(OimConstants.ORDERBATCH_TYPE_ID_MANUAL));
  }

  public String processVendorOrder(Integer vendorId, OimOrders oimOrders,
      OimOrderBatchesTypes processingType) throws SupplierConfigurationException,
          SupplierCommunicationException, SupplierOrderException {
    log.debug("Processing orders for VendorId: {}", vendorId);

    if (oimOrders.getDeliveryStateCode() == null)
      throw new InvalidAddressException(
          "Please check Delivery State code for order id - " + oimOrders.getStoreOrderId());
    // Transaction tx = null;
    if (StringHandle.isNullOrEmpty(oimOrders.getDeliveryCountryCode())) {
      String countryCode = ChannelBase.validateAndGetCountryCode(oimOrders);
      if (StringHandle.isNullOrEmpty(countryCode)) {
        // throw new SupplierOrderException(
        // "Country Code is missing; please update the order with Country Code.");
        log.error("Country Code is missing; please update the order with Country Code.");
      }
      oimOrders.setDeliveryCountryCode(countryCode);
    }
    boolean ordersSent = false;
    String status = "";
    try {

      List orders = new ArrayList();
      Map<Integer, OrderDetailResponse> successfulOrders = new HashMap<Integer, OrderDetailResponse>();
      Map<Integer, String> failedOrders = new HashMap<Integer, String>();
      int countToProcess = 0;
      try {
        // tx = m_dbSession.beginTransaction();
        // Removed the order details which have been processed
        OimOrderBatches batches = oimOrders.getOimOrderBatches();
        OimChannels oimChannels = batches.getOimChannels();

        Set unprocessedDetails = new HashSet();
        for (Iterator detailIt = oimOrders.getOimOrderDetailses().iterator(); detailIt.hasNext();) {
          OimOrderDetails detail = (OimOrderDetails) detailIt.next();
          if (OimConstants.ORDER_STATUS_UNPROCESSED
              .equals(detail.getOimOrderStatuses().getStatusId())) {
            if (detail.getOimSuppliers() == null) {
              log.warn("Unresolved order found. Skipping to process it.");
              continue;
            }
            Criteria channelSupplierMapQuery = m_dbSession
                .createCriteria(OimChannelSupplierMap.class)
                .add(Restrictions.eq("oimChannels.channelId", oimChannels.getChannelId()))
                .add(Restrictions.eq("oimSuppliers.supplierId",
                    detail.getOimSuppliers().getSupplierId()));
            List<OimChannelSupplierMap> list = channelSupplierMapQuery.list();

            if (list.size() == 0) {
              log.warn("Order processing rule not found for Channel: {} Supplier: {}",
                  oimChannels.getChannelName(), detail.getOimSuppliers().getSupplierName());
              Supplier.updateVendorSupplierOrderHistory(vendorId, detail.getOimSuppliers(),
                  "Order processing rule not found for Channel: " + oimChannels.getChannelName()
                      + " Supplier: " + detail.getOimSuppliers().getSupplierName(),
                  Supplier.ERROR_UNCONFIGURED_SUPPLIER, detail);
              // return false;
              return "Supplier not configured for sku - " + detail.getSku() + " for channel : "
                  + oimChannels.getChannelName();
            } else {
              log.info("Channel Supplier Mapping : {}", list);
              for (OimChannelSupplierMap oimChannelSupplierMap : list) {
                if (processingType.getBatchTypeId().equals(OimConstants.ORDERBATCH_TYPE_ID_MANUAL)
                    || (batches.getOimOrderBatchesTypes().getBatchTypeId()
                        .equals(OimConstants.ORDERBATCH_TYPE_ID_AUTOMATED)
                        && detail.getSku().startsWith(oimChannelSupplierMap.getSupplierPrefix())
                        && oimChannelSupplierMap.getEnableOrderAutomation().equals(1))) {
                  unprocessedDetails.add(detail);
                  countToProcess++;
                }
              }
            }
          }
        }
        oimOrders.setOimOrderDetailses(unprocessedDetails);
        // tx.commit();
        orders.add(oimOrders);
      } catch (RuntimeException e) {
        log.error(e.getMessage(), e);
      }
      if (countToProcess == 0) {
        return "Order Processed successfully.";
      }
      log.debug("Number of order details to process: " + countToProcess);
      log.debug("Sending orders to the supplier");
      sendOrderToSupplier(vendorId, oimOrders, successfulOrders, failedOrders);
      updateOrderStatus(successfulOrders, OimConstants.ORDER_STATUS_PROCESSED_SUCCESS);
      updateFailedOrderStatus(failedOrders, OimConstants.ORDER_STATUS_PROCESSED_FAILED);
      if (failedOrders.size() == 0) {
        // updateVendorSupplierOrderHistory(vendorId, ERROR_NONE, "");
        ordersSent = true;
        status = "Order Processed successfully.";
      } else {
        ordersSent = false;
        String failedResion = "";
        for (Iterator<Integer> itr = failedOrders.keySet().iterator(); itr.hasNext();) {
          int detailId = itr.next();
          if (failedResion.length() > 0)
            failedResion += "<br>";

          OimOrderDetails failedDetail = (OimOrderDetails) m_dbSession.get(OimOrderDetails.class,
              detailId);
          // failedSkus += failedDetail.getSku();
          failedResion += failedOrders.get(detailId);
        }
        status = "Order processing failed for <br> " + failedResion;
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      status = e.getMessage();
      Supplier.updateVendorSupplierOrderHistory(vendorId, null, e.getMessage(),
          Supplier.ERROR_ORDER_PROCESSING);
      log.error(e.getMessage(), e);
      return "Order Processing failed. " + status;
    }
    return status;
  }

  private boolean updateFailedOrderStatus(Map<Integer, String> orders, Integer status) {

    if (orders == null || orders.size() == 0) {
      log.debug("No orders to update with status: " + status);
      return true;
    }
    Transaction tx = null;

    String orderDetails = "";
    for (Iterator<Integer> itr = orders.keySet().iterator(); itr.hasNext();) {
      int detailId = itr.next();
      if (orderDetails.length() > 0)
        orderDetails += ",";
      orderDetails += detailId;
      try {
        tx = m_dbSession.beginTransaction();
        String errorMsg = orders.get(detailId);
        Query q = m_dbSession.createQuery(
            "update salesmachine.hibernatedb.OimOrderDetails o set o.supplierOrderStatus='"
                + errorMsg + "' where o.detailId=" + detailId);
        log.info(q.getQueryString());
        int rows = q.executeUpdate();
        log.debug("Updated order details. Rows changed: " + rows);

        tx.commit();
      } catch (RuntimeException e) {
        tx.rollback();
        e.printStackTrace();
      }
    }

    String processTime = "";
    if (status == OimConstants.ORDER_STATUS_PROCESSED_SUCCESS) {
      processTime = " o.processingTm=sysdate, ";
    }
    System.out.println(
        "update salesmachine.hibernatedb.OimOrderDetails o set o.oimOrderStatuses.statusId="
            + status + " where o.detailId in (" + orderDetails + ")");

    try {
      tx = m_dbSession.beginTransaction();

      Query q = m_dbSession.createQuery("update salesmachine.hibernatedb.OimOrderDetails o set "
          + processTime + " o.oimOrderStatuses.statusId=" + status + " where o.detailId in ("
          + orderDetails + ")");
      int rows = q.executeUpdate();
      log.debug("Updated order details. Rows changed: " + rows);

      tx.commit();
    } catch (RuntimeException e) {
      tx.rollback();
      e.printStackTrace();
    }
    return true;

  }

  private boolean pingSupplier(Integer vendorId, OimVendorSuppliers ovs,
      StringBuffer errorMessage) {
    // init(ovs.getOimSuppliers().getSupplierId());
    Integer supplierMethodNameId = m_supplierMethods.getOimSupplierMethodNames().getMethodNameId();
    if (OimConstants.SUPPLIER_METHOD_NAME_EMAIL.equals(supplierMethodNameId)) {
      String emailAddress = PojoHelper.getSupplierMethodAttributeValue(m_supplierMethods,
          OimConstants.SUPPLIER_METHOD_ATTRIBUTES_EMAILADDRESS);
      // May need to handle the case of bad email in the future

      return true;
    }

    if (OimConstants.SUPPLIER_METHOD_NAME_FTP.equals(supplierMethodNameId)) {
      // Verify the ftp credentials
      String ftpServer = PojoHelper.getSupplierMethodAttributeValue(m_supplierMethods,
          OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPSERVER);
      String ftpLogin = PojoHelper.getSupplierMethodAttributeValue(m_supplierMethods,
          OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPLOGIN);
      if ("#accountspecific".equals(ftpLogin))
        ftpLogin = ovs.getLogin();
      String ftpPassword = PojoHelper.getSupplierMethodAttributeValue(m_supplierMethods,
          OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPPASSWORD);
      if ("#accountspecific".equals(ftpPassword))
        ftpPassword = ovs.getPassword();
      String ftpFolder = PojoHelper.getSupplierMethodAttributeValue(m_supplierMethods,
          OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPFOLDER);

      FtpFileUploader uploader = new FtpFileUploader(ftpServer, ftpLogin, ftpPassword, 5, 0);
      String txt = uploader.testConnection();

      if (txt != null) {
        errorMessage
            .append("Could not connect to the Supplier FTP Server with \nServer:" + ftpServer
                + "\nLogin:" + ftpLogin + "\nPassword:" + ftpPassword + "\n\nMessage:" + txt);
        return false;
      }

      return true;
    }

    if (OimConstants.SUPPLIER_METHOD_NAME_CUSTOM.equals(supplierMethodNameId)) {
      log.debug("!!! Ping custom supplier");
      return true;
    }

    return false;
  }

  private boolean sendOrderToSupplier(Integer vendorId, OimOrders oimOrder,
      Map<Integer, OrderDetailResponse> successfulOrders, Map<Integer, String> failedOrders)
          throws SupplierConfigurationException, SupplierCommunicationException,
          SupplierOrderException {

    Set<OimSuppliers> supplierSet = new HashSet<OimSuppliers>();

    for (OimOrderDetails oimOrderDetails : (Set<OimOrderDetails>) oimOrder.getOimOrderDetailses()) {
      // init(oimOrderDetails.getOimSuppliers().getSupplierId());
      supplierSet.add(oimOrderDetails.getOimSuppliers());
    }
    for (Iterator<OimSuppliers> itr = supplierSet.iterator(); itr.hasNext();) {
      OimSuppliers supplier = itr.next();
      OimVendorSuppliers ovs = null;
      boolean ping = false;
      StringBuffer errorMessage = new StringBuffer();
      Query query;
      try {
        query = m_dbSession.createQuery(
            "from OimVendorSuppliers s where s.oimSuppliers=:supp and s.vendors.vendorId=:vid and s.deleteTm is null");
        query.setEntity("supp", supplier);
        query.setInteger("vid", vendorId);
        Iterator it = query.iterate();
        if (it.hasNext()) {
          ovs = (OimVendorSuppliers) it.next();
          // ping = pingSupplier(vendorId, ovs, errorMessage);
        } else {
          return false;
        }
      } catch (RuntimeException e) {
        log.error(e.getMessage(), e);
      }
      //
      // Integer supplierMethodNameId = m_supplierMethods.getOimSupplierMethodNames()
      // .getMethodNameId();
      // log.debug("Supplier Method Id: " + supplierMethodNameId);
      // if (OimConstants.SUPPLIER_METHOD_NAME_CUSTOM.equals(supplierMethodNameId)) {
      // log.debug("!!! Custom method called");
      if (ovs.getOimSuppliers().getIsCustom().intValue() == 0) {
        Supplier s = null;
        String className = ovs.getOimSuppliers().getClassName();
        try {
          Class<?> clazz = Class.forName(className);
          s = (Supplier) clazz.newInstance();

          s.setLogStream(logStream);

          s.sendOrders(vendorId, ovs, oimOrder);
          successfulOrders.putAll(s.successfulOrders);
          failedOrders.putAll(s.failedOrders);
        } catch (SupplierConfigurationException e) {
          Supplier.updateVendorSupplierOrderHistory(vendorId, ovs.getOimSuppliers(), e.getMessage(),
              Supplier.ERROR_UNCONFIGURED_SUPPLIER);
          log.error(e.getMessage(), e);
          throw e;
        } catch (SupplierCommunicationException e) {
          Supplier.updateVendorSupplierOrderHistory(vendorId, ovs.getOimSuppliers(), e.getMessage(),
              Supplier.ERROR_PING_FAILURE);
          log.error(e.getMessage(), e);
          throw e;
        } catch (SupplierOrderException e) {
          Supplier.updateVendorSupplierOrderHistory(vendorId, ovs.getOimSuppliers(), e.getMessage(),
              Supplier.ERROR_ORDER_PROCESSING);
          log.error(e.getMessage(), e);
          throw e;
        } catch (ChannelConfigurationException e) {
          log.error(e.getMessage(), e);
          Supplier.updateVendorSupplierOrderHistory(vendorId, ovs.getOimSuppliers(),
              "Error occured in updating store order status due to ChannelConfiguration Error. "
                  + e.getMessage(),
              Supplier.ERROR_ORDER_PROCESSING);
        } catch (ChannelCommunicationException e) {
          log.error(e.getMessage(), e);
          Supplier.updateVendorSupplierOrderHistory(vendorId, ovs.getOimSuppliers(),
              "Error occured in updating store order status due to ChannelComunication Error. "
                  + e.getMessage(),
              Supplier.ERROR_ORDER_PROCESSING);
        } catch (ChannelOrderFormatException e) {
          log.error(e.getMessage(), e);
          Supplier.updateVendorSupplierOrderHistory(vendorId, ovs.getOimSuppliers(),
              "Error occured in updating store order status due to ChannelOrderFormat Error. "
                  + e.getMessage(),
              Supplier.ERROR_ORDER_PROCESSING);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
          Supplier.updateVendorSupplierOrderHistory(vendorId, ovs.getOimSuppliers(), e.getMessage(),
              Supplier.ERROR_UNCONFIGURED_SUPPLIER);
          throw new SupplierConfigurationException(e.getMessage(), e);
        }
      } else {
        String className = ovs.getOimSuppliers().getClassName();

        try {
          Class<?> clazz = Class.forName(className);
          CustomSupplier cs = (CustomSupplier) clazz.newInstance();

          cs.sendOrders(vendorId, ovs, oimOrder);
          successfulOrders.putAll(cs.successfulOrders);
          failedOrders.putAll(cs.failedOrders);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
          Supplier.updateVendorSupplierOrderHistory(vendorId, ovs.getOimSuppliers(), e.getMessage(),
              Supplier.ERROR_UNCONFIGURED_SUPPLIER);
        } catch (ChannelCommunicationException e) {
          log.error(e.getMessage(), e);
          Supplier.updateVendorSupplierOrderHistory(vendorId, ovs.getOimSuppliers(),
              "Error occured in updating store order status due to ChannelComunication Error. "
                  + e.getMessage(),
              Supplier.ERROR_ORDER_PROCESSING);
        } catch (ChannelOrderFormatException e) {
          log.error(e.getMessage(), e);
          Supplier.updateVendorSupplierOrderHistory(vendorId, ovs.getOimSuppliers(),
              "Error occured in updating store order status due to ChannelOrderFormat Error. "
                  + e.getMessage(),
              Supplier.ERROR_ORDER_PROCESSING);
        }
      }
      // else if (OimConstants.SUPPLIER_METHOD_NAME_EMAIL.equals(supplierMethodNameId)
      // || OimConstants.SUPPLIER_METHOD_NAME_FTP.equals(supplierMethodNameId)) {
      // String tmp = PojoHelper.getSupplierMethodAttributeValue(m_supplierMethods,
      // OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FILETYPEID);
      //
      // // Find if there is an associated file
      // OimFiletypes oimFile = null;
      // if (tmp != null && tmp.length() > 0) {
      // Integer fileid = Integer.valueOf(tmp);
      // log.debug("Using File Id: " + fileid + " for generating supplier order file");
      // query = m_dbSession.createQuery(
      // "select f from OimFiletypes f where f.fileTypeId=:fileId and f.deleteTm is null");
      // Iterator it = query.setInteger("fileId", fileid.intValue()).iterate();
      // if (!it.hasNext()) {
      // logStream.println("No file defined for this supplier file id: " + fileid);
      // } else {
      // oimFile = (OimFiletypes) it.next();
      // }
      // }
      //
      // int fileFormat = 1;
      // try {
      // fileFormat = Integer.parseInt(PojoHelper.getSupplierMethodAttributeValue(
      // m_supplierMethods, OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FILEFORMAT));
      // } catch (Exception e) {
      // e.printStackTrace();
      // fileFormat = 1;
      // }
      //
      // OrderFile ofile = null;
      // if (fileFormat == OimConstants.FILE_FORMAT_XLS) {
      // ofile = new DefaultXlsFile(m_dbSession);
      // } else {
      // ofile = new DefaultCsvFile(m_dbSession);
      // }
      // if (oimFile != null) {
      // ofile = new DatabaseFile(m_dbSession, oimFile);
      // }
      // ofile.build();
      //
      // String name = (String) ofile.getFileFormatParams()
      // .get(OimConstants.FILE_FORMAT_PARAMS_NAME);
      // if (name != null && name.length() > 0) {
      // if (name.indexOf("#SupplierAccountNumber") != -1) {
      // String accountNumber = ovs.getAccountNumber();
      // name = name.replaceAll("#SupplierAccountNumber", accountNumber);
      // }
      // }
      // String fileName = "";
      // if (fileFormat == OimConstants.FILE_FORMAT_XLS) {
      // fileName = "" + vendorId + "-" + m_supplier.getSupplierId() + ".xls";
      // log.debug("Generating file " + fileName);
      // try {
      // // FIXME generateXlsFile(orders, ofile.getFileFieldMaps(), fileName,
      // // ofile.getFileFormatParams(),
      // // ofile.getSpecificsProvider(ovs));
      // } catch (Exception e) {
      // log.error(e.getMessage(), e);
      // }
      // } else if (fileFormat == OimConstants.FILE_FORMAT_CSV) {
      // fileName = "" + vendorId + "-" + m_supplier.getSupplierId() + ".csv";
      // if (name != null && name.length() > 0)
      // fileName = name;
      // log.debug("Generating file " + fileName);
      // try {
      // /*
      // * generateCsvFile(orders, ofile.getFileFieldMaps(), fileName,
      // * ofile.getFileFormatParams(), ofile.getSpecificsProvider(ovs));
      // */
      // } catch (Exception e) {
      // log.debug(e.getMessage(), e);
      // }
      // }
      //
      // // Get the reps object to get the email id and name of the
      // // vendor to
      // // whom the order status email will go in case the
      // // emaiNotification
      // // is set for him.
      // Query q = m_dbSession.createQuery(
      // "select r from salesmachine.hibernatedb.Reps r where r.vendorId = " + vendorId);
      // Reps r = new Reps();
      // Iterator repsIt = q.iterate();
      // if (repsIt.hasNext()) {
      // r = (Reps) repsIt.next();
      // }
      //
      // if (OimConstants.SUPPLIER_METHOD_NAME_FTP.equals(supplierMethodNameId)) {
      // String ftpServer = PojoHelper.getSupplierMethodAttributeValue(m_supplierMethods,
      // OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPSERVER);
      // String ftpLogin = PojoHelper.getSupplierMethodAttributeValue(m_supplierMethods,
      // OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPLOGIN);
      // String ftpPassword = PojoHelper.getSupplierMethodAttributeValue(m_supplierMethods,
      // OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPPASSWORD);
      //
      // if ("#accountspecific".equals(ftpLogin))
      // ftpLogin = ovs.getLogin();
      // if ("#accountspecific".equals(ftpPassword))
      // ftpPassword = ovs.getPassword();
      //
      // String ftpFolder = PojoHelper.getSupplierMethodAttributeValue(m_supplierMethods,
      // OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPFOLDER);
      //
      // FtpFileUploader uploader = new FtpFileUploader(ftpServer, ftpLogin, ftpPassword, 5, 0);
      // try {
      // uploader.Upload(ftpFolder, fileName, fileName);
      // } catch (Exception e) {
      // log.error(e.getMessage(), e);
      // return false;
      // }
      //
      // } else if (OimConstants.SUPPLIER_METHOD_NAME_EMAIL.equals(supplierMethodNameId)) {
      // String emailAddress = PojoHelper.getSupplierMethodAttributeValue(m_supplierMethods,
      // OimConstants.SUPPLIER_METHOD_ATTRIBUTES_EMAILADDRESS);
      // if (fileFormat == OimConstants.FILE_FORMAT_SEND_PLAIN_TEXT_IN_EMAIL) {
      // try {
      // String vendor_name = StringHandle.removeNull(r.getFirstName()) + " "
      // + StringHandle.removeNull(r.getLastName());
      // String emailContent = "Dear " + vendor_name + "<br>";
      // emailContent += "<br>Following is the status of the orders processed for the supplier "
      // + ovs.getOimSuppliers().getSupplierName() + " : - <br>";
      // emailContent += ""/*
      // * //FIXME FgenerateMailBody(orders, ofile.getFileFieldMaps(), new
      // * StandardFileSpecificsProvider(m_dbSession, ovs, new
      // * Vendors(vendorId)))
      // */;
      // EmailUtil.sendEmail(emailAddress, "support@inventorysource.com", r.getLogin(),
      // "oim@inventorysource.com", m_supplier.getSupplierName() + " Orders", emailContent,
      // "text/html");
      // } catch (Exception e1) {
      // log.error(e1.getMessage(), e1);
      // }
      // } else {
      // EmailUtil.sendEmailWithAttachment(emailAddress, "support@inventorysource.com",
      // "oim@inventorysource.com," + r.getLogin(), m_supplier.getSupplierName() + " Orders",
      // "Find attached the orders from my store.", fileName, "");
      // }
      // }
      //
      // String nameEmail = StringHandle.removeNull(r.getFirstName()) + " "
      // + StringHandle.removeNull(r.getLastName());
      // String emailContent = "Dear " + nameEmail + "<br>";
      // emailContent += "<br>Following is the status of the orders processed for the supplier "
      // + ovs.getOimSuppliers().getSupplierName() + " : - <br>";
      // boolean emailNotification = false;
      //
      // // In both these cases i.e. Ftp File Upload and Email, orders
      // // can
      // // not fail at this stage
      // // So all of them need to be marked placed
      // /*
      // * //FIXME for (int i = 0; i < orders.size(); i++) { OimOrders order = (OimOrders)
      // * orders.get(i);
      // *
      // * // Send Email Notifications if is set to true. if
      // * (order.getOimOrderBatches().getOimChannels().getEmailNotifications() == 1) {
      // * emailNotification = true; String orderStatus = "Successfully Placed"; emailContent +=
      // * "<b>Store Order ID " + order.getStoreOrderId() + "</b> -> " + orderStatus + " ";
      // * emailContent += "<br>"; } }
      // */
      // if (emailNotification) {
      // emailContent += "<br>Thanks, <br>Inventorysource support<br>";
      // logStream.println("!! Sending email to user about order processing");
      // EmailUtil.sendEmail(r.getLogin(), "support@inventorysource.com", "",
      // "Order processing update results", emailContent, "text/html");
      // }
      // }
    }
    return true;
  }

  public static void generateCsvFile(List orders, List fileFieldMaps, String fileName,
      Hashtable fileFormatParams, IFileSpecificsProvider fileSpecifics) throws Exception {
    FileWriter outputFile = new FileWriter(fileName);

    boolean useHeader = "1"
        .equals((String) fileFormatParams.get(OimConstants.FILE_FORMAT_PARAMS_USEHEADER));
    String fieldDelimiter = (String) fileFormatParams
        .get(OimConstants.FILE_FORMAT_PARAMS_FIELD_DELIMITER);
    String textDelimiter = (String) fileFormatParams
        .get(OimConstants.FILE_FORMAT_PARAMS_TEXT_DELIMITER);
    if ("TAB".equals(fieldDelimiter)) {
      fieldDelimiter = "\t";
    }

    // Get the headers now
    if (useHeader) {
      // Write the header first
      String headerline = "";
      int i = 0;
      for (Iterator it = fileFieldMaps.iterator(); it.hasNext();) {
        OimFileFieldMap map = (OimFileFieldMap) it.next();
        if (i > 0)
          headerline += fieldDelimiter;
        headerline += textDelimiter + StringHandle.removeNull(map.getMappedFieldName())
            + textDelimiter;
        i++;
      }
      headerline += "\n";
      outputFile.write(headerline);
    }

    // Write the data now
    for (int i = 0; i < orders.size(); i++) {
      OimOrders order = (OimOrders) orders.get(i);
      for (Iterator detailIt = order.getOimOrderDetailses().iterator(); detailIt.hasNext();) {
        OimOrderDetails detail = (OimOrderDetails) detailIt.next();
        // for all the order details
        String dataline = "";
        int j = 0;

        for (Iterator it = fileFieldMaps.iterator(); it.hasNext();) {
          OimFileFieldMap map = (OimFileFieldMap) it.next();
          OimFields field = map.getOimFields();
          String writeModifier = StringHandle.removeNull(map.getMappedFieldModifierRuleWr());
          String fieldValue = StringHandle
              .removeNull(fileSpecifics.getFieldValueFromOrder(detail, map));
          // log.debug("Field Id: "+field.getFieldId()+"\t
          // ("+field.getFieldName()+")"+"\t:"+fieldValue);
          if (j > 0)
            dataline += fieldDelimiter;
          dataline += textDelimiter + fieldValue + textDelimiter;
          j++;
        }
        dataline += "\n";
        outputFile.write(dataline);
      }
    } // for (int i=0;i<orders.numRows();i++) {

    if (fileSpecifics.getLastFileLine() != null) {
      outputFile.write(fileSpecifics.getLastFileLine());
    }
    outputFile.close();
  }

  public static void generateXlsFile(List orders, List fileFieldMaps, String fileName,
      Hashtable fileFormatParams, IFileSpecificsProvider fileSpecifics) throws Exception {
    int row = 0;
    boolean useHeader = "1"
        .equals((String) fileFormatParams.get(OimConstants.FILE_FORMAT_PARAMS_USEHEADER));
    try {
      WritableWorkbook workbook = Workbook.createWorkbook(new File(fileName));
      WritableSheet sheet = workbook.createSheet("Sheet3", 0);
      Label label;
      if (useHeader) {
        int column = 0;
        // Write the header first
        String colval = "";
        for (Iterator it = fileFieldMaps.iterator(); it.hasNext();) {
          OimFileFieldMap map = (OimFileFieldMap) it.next();
          if ("".equals(StringHandle.removeNull(map.getMappedFieldName()))) {
            colval += StringHandle.removeNull(map.getMappedFieldName());
            continue;
          } else {
            colval += StringHandle.removeNull(map.getMappedFieldName());
          }
          label = new Label(column, row, colval);
          sheet.addCell(label);
          column++;
          colval = "";
        }
        row++;
      }

      // Write the data now
      for (int i = 0; i < orders.size(); i++) {
        OimOrders order = (OimOrders) orders.get(i);
        for (Iterator detailIt = order.getOimOrderDetailses().iterator(); detailIt.hasNext();) {
          OimOrderDetails detail = (OimOrderDetails) detailIt.next();
          // for all the order details
          int column = 0;
          String colval = "";
          for (Iterator it = fileFieldMaps.iterator(); it.hasNext();) {
            OimFileFieldMap map = (OimFileFieldMap) it.next();
            String fieldValue = StringHandle
                .removeNull(fileSpecifics.getFieldValueFromOrder(detail, map));
            if ("".equals(StringHandle.removeNull(map.getMappedFieldName()))) {
              colval += StringHandle.removeNull(fieldValue) + "\n";
              continue;
            } else {
              colval += StringHandle.removeNull(fieldValue);
            }
            label = new Label(column, row, colval);
            sheet.addCell(label);
            column++;
            colval = "";
          }
          row++;
        }
      } // for(int i=0;i<orders.size();i++) {
      workbook.write();
      workbook.close();
    } catch (RowsExceededException e) {
      e.printStackTrace();
    } catch (WriteException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String generateMailBody(List orders, List fileFieldMaps,
      IFileSpecificsProvider fileSpecifics) throws Exception {
    StringBuffer dataline = new StringBuffer("<br/><br/>");

    // Write the data now
    for (int i = 0; i < orders.size(); i++) {
      OimOrders order = (OimOrders) orders.get(i);
      for (Iterator detailIt = order.getOimOrderDetailses().iterator(); detailIt.hasNext();) {
        OimOrderDetails detail = (OimOrderDetails) detailIt.next();
        // for all the order details
        int j = 0;
        for (Iterator it = fileFieldMaps.iterator(); it.hasNext();) {
          OimFileFieldMap map = (OimFileFieldMap) it.next();
          String fieldValue = StringHandle
              .removeNull(fileSpecifics.getFieldValueFromOrder(detail, map));
          // log.debug("Field Id: "+field.getFieldId()+"\t
          // ("+field.getFieldName()+")"+"\t:"+fieldValue);
          if (j > 0)
            dataline.append(map.getMappedFieldName() + " : " + fieldValue + "<br/>");
          j++;
        }
        dataline.append("<br/><b>**********</b><br/>");
      }
    } // for (int i=0;i<orders.numRows();i++) {
    dataline.append("<br/><br/>End of Order List");

    return dataline.toString();
  }

  private boolean updateOrderStatus(Map<Integer, OrderDetailResponse> orders, Integer status) {
    if (orders == null || orders.size() == 0) {
      log.debug("No orders to update with status: " + status);
      return true;
    }
    Transaction tx = null;
    try {

      tx = m_dbSession.beginTransaction();
      for (Iterator<Integer> itr = orders.keySet().iterator(); itr.hasNext();) {
        Integer detailId = itr.next();
        OimOrderDetails detail = (OimOrderDetails) m_dbSession.get(OimOrderDetails.class, detailId);
        detail.setProcessingTm(new Date());
        detail.setOimOrderStatuses(new OimOrderStatuses(status));
        OrderDetailResponse res = orders.get(detailId);
        if (res.getPoNumber() != null)
          detail.setSupplierOrderNumber(res.getPoNumber());
        if (res.getStatus() != null)
          detail.setSupplierOrderStatus(res.getStatus());
        if (res.getWareHouseCode() != null)
          detail.setSupplierWareHouseCode(res.getWareHouseCode());
        m_dbSession.update(detail);
      }
      tx.commit();
    } catch (Exception e) {
      tx.rollback();
      log.error(e.getMessage(), e);
    }

    return true;
  }

  public String trackOrder(Integer vendorId, Integer orderDetailId) {
    Session session = m_dbSession;
    Transaction tx = session.getTransaction();
    OimLogStream stream = new OimLogStream();
    OrderStatus orderStatus = new OrderStatus();
    if (tx != null && tx.isActive())
      tx.commit();
    tx = session.beginTransaction();
    OimOrderDetails oimOrderDetails = (OimOrderDetails) session.get(OimOrderDetails.class,
        orderDetailId);
    int channelId = oimOrderDetails.getOimOrders().getOimOrderBatches().getOimChannels()
        .getChannelId();
    log.info("Tracking Status for Vendor#{} SKU# {}", vendorId, oimOrderDetails.getSku());
    HasTracking s = null;
    OimVendorSuppliers oimVendorSuppliers = null;
    Query query = session.createQuery(
        "from OimVendorSuppliers s where s.oimSuppliers=:supp and s.vendors.vendorId=:vid and s.deleteTm is null");
    query.setEntity("supp", oimOrderDetails.getOimSuppliers());
    query.setInteger("vid", vendorId);
    Object it = query.uniqueResult();
    if (it instanceof OimVendorSuppliers)
      oimVendorSuppliers = (OimVendorSuppliers) it;
    String className = oimOrderDetails.getOimSuppliers().getClassName();

    try {
      Class<?> clazz = Class.forName(className);
      Object o = clazz.newInstance();
      if (o instanceof HasTracking) {
        s = (HasTracking) o;
        orderStatus = s.getOrderStatus(oimVendorSuppliers, oimOrderDetails.getSupplierOrderNumber(),
            oimOrderDetails);

        oimOrderDetails.setSupplierOrderStatus(orderStatus.toString());
        if (orderStatus.isShipped()) {
          if (orderStatus.isPartialShipped())
            oimOrderDetails.setOimOrderStatuses(
                new OimOrderStatuses(OimConstants.ORDER_STATUS_PROCESSED_SUCCESS));
          else
            oimOrderDetails
                .setOimOrderStatuses(new OimOrderStatuses(OimConstants.ORDER_STATUS_SHIPPED));
          int trackCount = AutomationManager.orderTrackMap.get(channelId) != null
              ? AutomationManager.orderTrackMap.get(channelId) : 0;
          AutomationManager.orderTrackMap.put(channelId, trackCount++);
        }
      } else {

        orderStatus.setStatus("Tracking orders for "
            + oimOrderDetails.getOimSuppliers().getSupplierName() + " is not suported.");
      }
      session.update(oimOrderDetails);
    } catch (SupplierOrderTrackingException e1) {
      log.error(e1.getMessage(), e1);
      stream.println(e1.getMessage());
      Supplier.updateVendorSupplierOrderHistory(vendorId, oimVendorSuppliers.getOimSuppliers(),
          e1.getMessage(), Supplier.ERROR_ORDER_TRACKING);
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      Supplier.updateVendorSupplierOrderHistory(vendorId, oimVendorSuppliers.getOimSuppliers(),
          e.getMessage(), Supplier.ERROR_ORDER_TRACKING);
    }
    int existingQuantity = 0;
    query = session.createQuery("from OimOrderTracking t where t.detail=:od");
    query.setEntity("od", oimOrderDetails);

    List<OimOrderTracking> existingTrackins = query.list();
    log.info("existingTrackins size for order detail id - {} is {}", oimOrderDetails.getDetailId(),
        existingTrackins.size());
    for (OimOrderTracking ot : existingTrackins) {
      existingQuantity += ot.getShipQuantity();
    }
    if (orderStatus.isShipped()) {
      if (existingQuantity < oimOrderDetails.getQuantity()) {
        List<TrackingData> trackingDataList = orderStatus.getTrackingData();
        for (TrackingData td : trackingDataList) {
          OimOrderTracking oimOrderTracking = new OimOrderTracking();
          oimOrderTracking.setInsertionTime(new Date());
          oimOrderTracking.setShipDate(td.getShipDate().toGregorianCalendar().getTime());
          oimOrderTracking.setShippingCarrier(td.getCarrierName());
          oimOrderTracking.setShippingMethod(td.getShippingMethod());
          oimOrderTracking.setShipQuantity(td.getQuantity());
          oimOrderTracking.setTrackingNumber(td.getShipperTrackingNumber());
          oimOrderTracking.setDetail(oimOrderDetails);
          session.persist(oimOrderTracking);
        }
      }
    }
    if (tx != null && tx.isActive())
      tx.commit();
    // Update the store with tracking info
    OimOrders oimOrders = oimOrderDetails.getOimOrders();
    OimChannels oimChannels = oimOrders.getOimOrderBatches().getOimChannels();

    try {
      IOrderImport iOrderImport = ChannelFactory.getIOrderImport(oimChannels);

      if (orderStatus != null && oimChannels.getTestMode() == 0) {
        // if (existingQuantity < oimOrderDetails.getQuantity())
        iOrderImport.updateStoreOrder(oimOrderDetails, orderStatus);
        try {
          tx = session.getTransaction();
          if (tx != null && tx.isActive())
            tx.commit();
          tx = session.beginTransaction();
          if (orderStatus.isShipped())
            oimOrderDetails
                .setOimOrderStatuses(new OimOrderStatuses(OimConstants.ORDER_STATUS_COMPLETE));
          session.update(oimOrderDetails);
          tx.commit();
        } catch (Exception e) {
          if (tx != null && tx.isActive()) {
            tx.rollback();
          }
          log.error(e.getMessage(), e);
        }

      }

    } catch (ChannelConfigurationException e) {
      stream.println(e.getMessage());
      log.error(e.getMessage(), e);
      Supplier.updateVendorSupplierOrderHistory(vendorId, oimVendorSuppliers.getOimSuppliers(),
          "Error occured in updating store order status due to ChannelConfiguration Error. "
              + e.getMessage(),
          Supplier.ERROR_ORDER_TRACKING);
    } catch (ChannelCommunicationException e) {
      stream.println(e.getMessage());
      log.error(e.getMessage(), e);
      Supplier.updateVendorSupplierOrderHistory(vendorId, oimVendorSuppliers.getOimSuppliers(),
          "Error occured in updating store order status due to ChannelCommunication Error. "
              + e.getMessage(),
          Supplier.ERROR_ORDER_TRACKING);
    } catch (ChannelOrderFormatException e) {
      stream.println(e.getMessage());
      log.error(e.getMessage(), e);
      Supplier.updateVendorSupplierOrderHistory(vendorId, oimVendorSuppliers.getOimSuppliers(),
          "Error occured in updating store order status due to ChannelOrderFormat Error. "
              + e.getMessage(),
          Supplier.ERROR_ORDER_TRACKING);
    }
    if (orderStatus != null)
      stream.println(orderStatus.toString());
    return stream.toString();
  }
}
