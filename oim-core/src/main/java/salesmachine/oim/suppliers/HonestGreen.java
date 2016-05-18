package salesmachine.oim.suppliers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.channels.FileLock;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.axis.encoding.Base64;
import org.apache.axis.utils.ByteArrayOutputStream;
import org.hibernate.HibernateError;
import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.mws.MarketplaceWebService;
import com.amazonaws.mws.MarketplaceWebServiceClient;
import com.amazonaws.mws.MarketplaceWebServiceConfig;
import com.amazonaws.mws.MarketplaceWebServiceException;
import com.amazonaws.mws.model.IdList;
import com.amazonaws.mws.model.SubmitFeedRequest;
import com.amazonaws.mws.model.SubmitFeedResponse;
import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;
import com.enterprisedt.net.ftp.FTPTransferType;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderProcessingRule;
import salesmachine.hibernatedb.OimOrderStatuses;
import salesmachine.hibernatedb.OimOrderTracking;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSupplierMethodattrValues;
import salesmachine.hibernatedb.OimSupplierMethods;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.Product;
import salesmachine.hibernatedb.Reps;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.stores.impl.ChannelFactory;
import salesmachine.oim.stores.modal.amazon.AmazonEnvelope;
import salesmachine.oim.stores.modal.amazon.AmazonEnvelope.Message;
import salesmachine.oim.stores.modal.amazon.Header;
import salesmachine.oim.stores.modal.amazon.OrderAcknowledgement;
import salesmachine.oim.stores.modal.amazon.OrderFulfillment;
import salesmachine.oim.stores.modal.amazon.OrderFulfillment.FulfillmentData;
import salesmachine.oim.stores.modal.amazon.OrderFulfillment.Item;
import salesmachine.oim.suppliers.exception.SupplierCommunicationException;
import salesmachine.oim.suppliers.exception.SupplierConfigurationException;
import salesmachine.oim.suppliers.exception.SupplierOrderException;
import salesmachine.oim.suppliers.exception.SupplierOrderTrackingException;
import salesmachine.oim.suppliers.modal.OrderDetailResponse;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.oim.suppliers.modal.hg.TrackingData;
import salesmachine.oim.suppliers.modal.hg.TrackingData.POTracking;
import salesmachine.util.ApplicationProperties;
import salesmachine.util.FtpDetail;
import salesmachine.util.FtpDetail.WareHouseType;
import salesmachine.util.OimLogStream;
import salesmachine.util.StringHandle;

public class HonestGreen extends Supplier implements HasTracking {

  private static final String CACHE_FILENAME = ApplicationProperties.getProperty("cm.cache.dir")
      + "HG-PO-Hashtable.ser";

  private static final Map<String, String> PONUM_UNFI_MAP = new Hashtable<String, String>();

  private static final String UNFIORDERNO = "UNFIORDERNO";
  private static final String PONUM = "PONUM";
  private static final String QTY_ORDERED = "QTY_ORDERED";
  private static final String QTY_SHIPPED = "QTY_SHIPPED";
  private static final String ASCII = "ASCII";
  private static final Logger log = LoggerFactory.getLogger(HonestGreen.class);
  // private static final byte BLANK_SPACE = ' ';
  private static final byte[] NEW_LINE = new byte[] { '\n' };
  private static final byte[] HG_EOF = new byte[] { '*', '*', '*', 'E', 'O', 'F', '*', '*', '*' };
  private static final byte[] FIL = new byte[] { 'F', 'I', 'L' };
  private static final byte[] COMMA = new byte[] { ',' };
  private static final String SHIP_DATE = "SHIP_DATE";
  private static JAXBContext jaxbContext;

  static {
    try {
      jaxbContext = JAXBContext.newInstance(TrackingData.class);
    } catch (JAXBException e) {
      log.error(e.getMessage(), e);
    }
    updateObjectFromCacheFile();
  }

  private static final synchronized void updateObjectFromCacheFile() {
    try {
      FileInputStream fis = new FileInputStream(CACHE_FILENAME);
      ObjectInputStream ois = new ObjectInputStream(fis);
      PONUM_UNFI_MAP.putAll((Hashtable<String, String>) ois.readObject());
      ois.close();
    } catch (Exception e) {
      log.warn("{} file is blank", CACHE_FILENAME);
    }
  }

  @Override
  public void sendOrders(Integer vendorId, OimVendorSuppliers ovs, OimOrders order)
      throws SupplierConfigurationException, SupplierCommunicationException, SupplierOrderException,
      ChannelConfigurationException, ChannelCommunicationException, ChannelOrderFormatException {
    log.info("Sending orders of Account: {}", ovs.getAccountNumber());
    if (ovs.getTestMode().equals(1))
      return;

    // populate orderSkuPrefixMap with channel id and the prefix to be used
    // for the given supplier.
    orderSkuPrefixMap = setSkuPrefixForOrders(ovs);
    String supplierDefaultPrefix = ovs.getOimSuppliers().getDefaultSkuPrefix();
    int channelId = order.getOimOrderBatches().getOimChannels().getChannelId();
    String configuredPrefix = orderSkuPrefixMap.get(channelId);
    Session session = SessionManager.currentSession();
    Reps r = (Reps) session.createCriteria(Reps.class).add(Restrictions.eq("vendorId", vendorId))
        .uniqueResult();
    Vendors v = new Vendors();
    v.setVendorId(r.getVendorId());
    String name = StringHandle.removeNull(r.getFirstName()) + " "
        + StringHandle.removeNull(r.getLastName());
    String emailContent = "Dear " + name + "<br>";
    emailContent += "<br>Following is the status of the orders file uploaded on FTP for the supplier "
        + ovs.getOimSuppliers().getSupplierName() + " : - <br>";

    boolean emailNotification = false;

    boolean isOrderFromAmazonStore = isOrderFromAmazonStore(order);
    String poNum;
    Query query = session.createSQLQuery(
        "select  distinct SUPPLIER_ORDER_NUMBER from kdyer.OIM_ORDER_DETAILS where ORDER_ID=:orderId and SUPPLIER_ID=:supplierId");
    query.setInteger("orderId", order.getOrderId());
    query.setInteger("supplierId", ovs.getOimSuppliers().getSupplierId());
    Object q = null;
    try {
      q = query.uniqueResult();
    } catch (NonUniqueResultException e) {
      log.error(
          "This order has more than one product having different PO number. Please make them unique. store order id is - {}",
          order.getStoreOrderId());
      throw new SupplierConfigurationException(
          "This order has more than one product having different PO number. Please make them unique.");
    } catch (HibernateException e) {
      throw new SupplierCommunicationException(SupplierCommunicationException.DB_CONNECTION_ISSUE
          + " for order id - " + order.getOrderId(), e);
    }

    if (q != null) {
      poNum = (String) q;
      log.info("Reprocessing po - {}", poNum);
    } else {
      if (isOrderFromAmazonStore)
        poNum = order.getStoreOrderId();
      else
        poNum = ovs.getVendors().getVendorId() + "-" + order.getStoreOrderId();
    }
    Set<OimOrderDetails> phiItems = new HashSet<OimOrderDetails>();
    Set<OimOrderDetails> hvaItems = new HashSet<OimOrderDetails>();
    FtpDetail hvaFtpDetail = FtpDetail.getFtpDetails(ovs, true);
    FtpDetail phiFtpDetail = FtpDetail.getFtpDetails(ovs, false);
    boolean isSingleWarehouseConfigured = false;
    boolean isHva = false;
    if (hvaFtpDetail.getUrl() != null && phiFtpDetail.getUrl() == null) {
      isSingleWarehouseConfigured = true;
      isHva = true;
    } else if (phiFtpDetail.getUrl() != null && hvaFtpDetail.getUrl() == null) {
      isSingleWarehouseConfigured = true;
      isHva = false;
    }
    if (!isSingleWarehouseConfigured) {
      query = session.createSQLQuery(
          "SELECT WAREHOUSE_LOCATION from OIM_CHANNEL_SUPPLIER_MAP where channel_id=:channelId and SUPPLIER_ID=:supplierId");
      query.setInteger("channelId", channelId);
      query.setInteger("supplierId", ovs.getOimSuppliers().getSupplierId());
      String warehouseLocations = null;
      try {
        warehouseLocations = (String) query.uniqueResult();
      } catch (NonUniqueResultException e) {
        log.error(e.getMessage(), e);
      }
      String phi = null;
      String hva = null;
      if (warehouseLocations != null) {
        if (warehouseLocations.contains("~")) {
          String[] warehouseArray = warehouseLocations.split("~");
          for (int i = 0; i < warehouseArray.length; i++) {
            if (warehouseArray[i].equalsIgnoreCase("PHI"))
              phi = warehouseArray[i];
            if (warehouseArray[i].equalsIgnoreCase("HVA"))
              hva = warehouseArray[i];
          }
        } else {
          isHva = warehouseLocations.equalsIgnoreCase("HVA");
          isSingleWarehouseConfigured = true;
        }
      }
      if (phi != null && hva == null)
        isHva = false;
      if (hva != null && phi == null)
        isHva = true;
    }
    for (OimOrderDetails orderDetail : ((Set<OimOrderDetails>) order.getOimOrderDetailses())) {
      String sku = orderDetail.getSku();
      if (configuredPrefix.equals(""))
        sku = supplierDefaultPrefix + sku;
      else if (!configuredPrefix.equalsIgnoreCase(supplierDefaultPrefix)) {
        sku = sku.replaceFirst(configuredPrefix, supplierDefaultPrefix);
      }
      Integer phiQty = getPhiQuantity(sku);
      int phiQtyInt = phiQty != null ? phiQty.intValue() : 0;
      Integer hvaQty = getHvaQuantity(sku, vendorId, phiQtyInt);
      int hvaQtyInt = hvaQty !=null ? hvaQty.intValue() :0;
      if (phiQty == null && hvaQty == null) {
        failedOrders.put(orderDetail.getDetailId(),
            orderDetail.getSku() + " is not processed because it is not in our system.");
        continue;
      }
      if (!isSingleWarehouseConfigured) {
        isHva = isRestricted(sku, vendorId, phiQtyInt, hvaQtyInt);
      }
      if (isHva) {
        hvaItems.add(orderDetail);

      } else {
        phiItems.add(orderDetail);
      }
    }
    if (order.getOimOrderBatches().getOimChannels().getEmailNotifications() == 1) {
      emailNotification = true;
      String orderStatus = "Successfully Placed";
      emailContent += "<b>Store Order ID " + order.getStoreOrderId() + "</b> -> " + orderStatus
          + " ";
      emailContent += "<br>";
    }

    if (hvaItems.size() > 0) {
      FtpDetail ftpDetails = FtpDetail.getFtpDetails(ovs, true);

      String fileName = createOrderFile(order, ovs, hvaItems, ftpDetails, poNum);

      sendToFTP(fileName, ftpDetails);
      for (OimOrderDetails od : hvaItems) {

        successfulOrders.put(od.getDetailId(), new OrderDetailResponse(poNum,
            OimConstants.OIM_SUPPLER_ORDER_STATUS_SENT_TO_SUPPLIER, "H"));
      }
      // if (emailNotification) {
      sendEmail(emailContent, ftpDetails, fileName, "");
      // }

    }
    if (phiItems.size() > 0) {
      // create order file and send order to PHI configured
      FtpDetail ftpDetails = FtpDetail.getFtpDetails(ovs, false);
      String fileName = createOrderFile(order, ovs, phiItems, ftpDetails, poNum);
      sendToFTP(fileName, ftpDetails);
      for (OimOrderDetails od : phiItems) {
        successfulOrders.put(od.getDetailId(), new OrderDetailResponse(poNum,
            OimConstants.OIM_SUPPLER_ORDER_STATUS_SENT_TO_SUPPLIER, "P"));
      }
      // if (emailNotification) {
      sendEmail(emailContent, ftpDetails, fileName, "");
      // }

    }
  }

  private boolean isOrderFromAmazonStore(OimOrders order) {
    if (order.getOimOrderBatches().getOimChannels().getOimSupportedChannels()
        .getSupportedChannelId() == 4)
      return true;
    return false;
  }

  private void sendEmail(String emailContent, FtpDetail ftpDetails, String fileName, String login) {
    String emailBody = "Account Number : " + ftpDetails.getAccountNumber()
        + "\n Find attached order file for the orders from my store.";
    String emailSubject = fileName;
    EmailUtil.sendEmailWithAttachment("orders@inventorysource.com", "support@inventorysource.com",
        login, emailSubject, emailBody, fileName);

  }

  private void sendErrorReportEmail(String fileName, FtpDetail ftpDetails) {
    EmailUtil.sendEmail("support@inventorysource.com", "support@inventorysource.com", "",
        "Failed to put order file to " + ftpDetails.getUrl(),
        "Failed to put this order file " + fileName + " to " + ftpDetails.getUrl(), "text/html");
  }

  public Integer getHvaQuantity(String sku, Integer vendorId, int phiQuantity) {
    List<String> findSkuList = new ArrayList<String>();
    String prefix = sku.substring(0, 2);
    String tempSku = sku.substring(2, sku.length());
    findSkuList.add(sku);
    if (tempSku.startsWith("0")) {
      String tempSku2 = tempSku.substring(1, tempSku.length());
      findSkuList.add(prefix + tempSku2);
    } else
      findSkuList.add(prefix + "0" + tempSku);
    Session dbSession = SessionManager.currentSession();
    Query query = dbSession.createSQLQuery(
        "select QUANTITY from VENDOR_CUSTOM_FEEDS_PRODUCTS where sku IN (:skuList) and is_restricted=1 and VENDOR_CUSTOM_FEED_ID=(select VENDOR_CUSTOM_FEED_ID from VENDOR_CUSTOM_FEEDS where vendor_id=:vendorID AND IS_RESTRICTED=1)");
    query.setParameterList("skuList", findSkuList);
    query.setInteger("vendorID", vendorId);
    Object q = null;
    try {
      q = query.uniqueResult();
    } catch (NonUniqueResultException e) {
      log.warn("VENDOR_CUSTOM_FEEDS_PRODUCTS contains more than one entries for SKU {}", sku);

      List restrictedList = query.list();
      q = restrictedList.get(0);
    }
    int tempQuantity = 0;
    if (q != null) {
      log.debug("HVA Quantity {} for Sku {}", q.toString(), sku);
      tempQuantity = Integer.parseInt(q.toString());
      return tempQuantity - phiQuantity;
    } else
      return null;
  }

  public Integer getPhiQuantity(String sku) {
    List<String> findSkuList = new ArrayList<String>();
    String prefix = sku.substring(0, 2);
    String tempSku = sku.substring(2, sku.length());
    findSkuList.add(sku);
    if (tempSku.startsWith("0")) {
      String tempSku2 = tempSku.substring(1, tempSku.length());
      findSkuList.add(prefix + tempSku2);
    } else
      findSkuList.add(prefix + "0" + tempSku);
    Session dbSession = SessionManager.currentSession();
    Query query = dbSession.createSQLQuery("select QUANTITY from PRODUCT where SKU IN (:skuList)");
    query.setParameterList("skuList", findSkuList);
    Object q = null;
    try {
      q = query.uniqueResult();
    } catch (NonUniqueResultException e) {
      log.warn("PRODUCT table contains more than one entries for SKU {}", sku);

      List list = query.list();
      q = list.get(0);
    }
    int phiQuantity = 0;
    if (q != null) {
      log.debug("PHI Quantity {} for Sku {}", q.toString(), sku);
      phiQuantity = Integer.parseInt(q.toString());
      return phiQuantity;
    } else
      return null;
  }

  private void sendToFTP(String fileName, FtpDetail ftpDetails)
      throws SupplierCommunicationException, SupplierConfigurationException {
    FTPClient ftp = new FTPClient();
    File file = new File(fileName);
    try {
      ftp.setRemoteHost(ftpDetails.getUrl());
      ftp.setDetectTransferMode(false);
      ftp.connect();
      ftp.login(ftpDetails.getUserName(), ftpDetails.getPassword());
      ftp.setType(FTPTransferType.ASCII);
      ftp.setTimeout(60 * 1000 * 60 * 5);
      ftp.put(fileName, file.getName());
      if (ftp.get(file.getName()) == null) {
        sendErrorReportEmail(fileName, ftpDetails);
      }
      ftp.quit();
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      throw new SupplierCommunicationException(
          "Could not connect to FTP while sending orderfile to HG - " + e.getMessage(), e);
    } catch (FTPException e) {
      log.error(e.getMessage(), e);
      throw new SupplierConfigurationException(
          "Could not connect to FTP while sending orderfile to HG - " + e.getMessage(), e);
    }
  }

  private boolean isRestricted(String sku, int vendorID, int phiQty, int hvaQty) {
    Session dbSession = SessionManager.currentSession();
    // tx = dbSession.beginTransaction();
    // if (configuredPrefix.equals(""))
    // sku = defaultPrefix + sku;
    // else if (!configuredPrefix.equalsIgnoreCase(defaultPrefix)) {
    // sku = sku.replaceFirst(configuredPrefix, defaultPrefix);
    // }
    Query query = dbSession.createQuery(
        "select p.isRestricted from salesmachine.hibernatedb.Product p where p.sku=:sku");
    query.setString("sku", sku);
    Object restrictedIntVal = query.uniqueResult();
    if (restrictedIntVal != null && ((Integer) restrictedIntVal).intValue() == 1) {
      log.debug("{} is restricted in product table", sku);
      return true;
    } else {
      query = dbSession.createSQLQuery(
          "select IS_RESTRICTED from VENDOR_CUSTOM_FEEDS_PRODUCTS where sku=:sku and VENDOR_CUSTOM_FEED_ID=(select VENDOR_CUSTOM_FEED_ID from VENDOR_CUSTOM_FEEDS where vendor_id=:vendorID)");
      query.setString("sku", sku);
      query.setInteger("vendorID", vendorID);
      Object restrictedVal = null;

      try {
        restrictedVal = query.uniqueResult();
      } catch (NonUniqueResultException e) {
        log.warn("VENDOR_CUSTOM_FEEDS_PRODUCTS contains more than one entries for SKU {}", sku);

        List restrictedList = query.list();
        restrictedVal = restrictedList.get(0);
      }

      if (restrictedVal != null && ((BigDecimal) restrictedVal).intValue() == 1) {
        log.debug("{} is restricted in VENDOR_CUSTOM_FEEDS_PRODUCTS table", sku);
        // int phiQuantity = getPhiQuantity(sku);
        // int hvaQuantity = getHvaQuantity(sku, vendorID, phiQuantity);
        if (hvaQty > phiQty) {
          return true;
        } else
          return false;

      } else {
        log.debug("{} is not restricted in both the tables", sku);
        return false;
      }
    }
  }

  private static Map<String, String> parseOrderConfirmation(
      Map<Integer, String> orderConfirmationMap, String tempTrackingMeta) {
    Map<String, String> orderData = new HashMap<String, String>();

    for (Iterator itr = orderConfirmationMap.values().iterator(); itr.hasNext();) {
      String line = (String) itr.next();
      String[] lineArray = line.split(",");
      if (lineArray.length == 9) {

        PONUM_UNFI_MAP.put(lineArray[6], lineArray[0]);
        if (lineArray[6].equals(tempTrackingMeta)) {
          orderData.put(PONUM, lineArray[6]);
          orderData.put(UNFIORDERNO, lineArray[0]);
          break;
        }
      }
    }
    // String[] lineArray1 = orderConfirmationMap.get(1).split(",");
    //
    // orderData.put(PONUM, lineArray1[6]);
    // orderData.put(UNFIORDERNO, lineArray1[0]);
    return orderData;
  }

  private Map<String, String> parseShippingConfirmation(
      Map<Integer, String> shippingConfirmationMap, String sku) {
    Map<String, String> orderData = new HashMap<String, String>();
    String[] lineArray1 = shippingConfirmationMap.get(1).split(",");
    orderData.put(PONUM, lineArray1[7]);
    orderData.put(UNFIORDERNO, lineArray1[9]);
    PONUM_UNFI_MAP.put(lineArray1[7], lineArray1[9]);
    orderData.put(SHIP_DATE, lineArray1[lineArray1.length - 1]);
    for (Iterator itr = shippingConfirmationMap.values().iterator(); itr.hasNext();) {
      String line = (String) itr.next();
      String[] lineArray = line.split(",");
      if (lineArray.length > 4 && sku.equals(lineArray[0])) {
        orderData.put(QTY_ORDERED, lineArray[3]);
        orderData.put(QTY_SHIPPED, lineArray[4]);
      }
    }
    return orderData;
  }

  /**
   * Parses and returns the file data in a Map with line number as keys.
   * 
   * @param confirmationFileData
   * @return file data in a Map with line number as keys.
   */
  private static Map<Integer, String> parseFileData(byte[] confirmationFileData) {
    Map<Integer, String> fileData = null;
    try {
      fileData = new HashMap<Integer, String>();
      InputStream inputStream = new ByteArrayInputStream(confirmationFileData);
      Reader r = new InputStreamReader(inputStream, ASCII);

      int i = -1, lineNum = 1;
      char c;
      StringBuilder sb = new StringBuilder();
      while ((i = r.read()) != -1) {
        c = (char) i;
        if (c == '"')
          continue;
        if (c != '\n') {
          sb.append(c);
        } else {
          fileData.put(lineNum, sb.toString());
          lineNum++;
          sb = new StringBuilder();
        }
      }
    } catch (UnsupportedEncodingException e) {
      log.error(e.getMessage(), e);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    return fileData;
  }

  private String createOrderFile(OimOrders order, OimVendorSuppliers ovs,
      Set<OimOrderDetails> detailSet, FtpDetail ftpDetails, String poNum)
          throws ChannelCommunicationException, ChannelOrderFormatException,
          SupplierOrderException {

    String uploadfilename = "/tmp/" + "HG_" + ftpDetails.getAccountNumber() + "_"
        + new Random().nextLong() + ".txt";
    File f = new File(uploadfilename);
    log.info("created file name for HG:{}", f.getName());
    log.debug("Creating order file for PO:{}", order.getOrderId());
    try (FileOutputStream fOut = new FileOutputStream(f)) {

      // Integer orderSize = order.getOimOrderDetailses().size();
      fOut.write("1".getBytes(ASCII));
      fOut.write(NEW_LINE);
      fOut.write(ftpDetails.getAccountNumber().getBytes(ASCII));
      fOut.write(COMMA);
      // fOut.write(BLANK_SPACE);
      fOut.write(COMMA);

      fOut.write(poNum.getBytes(ASCII));
      fOut.write(COMMA);
      // fOut.write(BLANK_SPACE);
      fOut.write(COMMA);
      fOut.write(FIL);
      fOut.write(COMMA);
      // fOut.write(BLANK_SPACE);
      fOut.write(NEW_LINE);
      fOut.write(
          StringHandle.removeComma(StringHandle.removeNull(order.getDeliveryName()).toUpperCase())
              .getBytes(ASCII));
      fOut.write(COMMA);
      String address = null;
      String addressLine1 = StringHandle
          .removeComma(StringHandle.removeNull(order.getDeliveryStreetAddress()).toUpperCase());
      String addressLine2 = StringHandle
          .removeComma(StringHandle.removeNull(order.getDeliverySuburb()).toUpperCase());
      address = addressLine1 + " " + addressLine2;
      if (addressLine1.length() > 25 && addressLine2.length() > 25)
        throw new SupplierOrderException(
            "Error: Address Line 1 &2 are both more than the 25 characters Honest Green allows. Please update the shipping address lines if possible and try again, or manually process.");
      else if (addressLine1.length() > 25)
        throw new SupplierOrderException(
            "Error: Street Address line 1 is longer than the Honest Green allows (25 max characters). Please edit and resubmit if possible or process manually.");

      else if (addressLine2.length() > 25)
        throw new SupplierOrderException(
            "Error: Street Address line 2 is longer than the Honest Green allows (25 max characters). Please edit and resubmit if possible or process manually.");
      // if (address.length() > 50) {
      // throw new SupplierOrderException(
      // "Street Address length is more than 25 characters, which can't fit the given Honest green
      // validation rules. Please edit the order and resubmit. Store Order id is - "
      // + order.getStoreOrderId());
      // } else {
      if (address.length() > 24) {
        addressLine1 = address.substring(0, 24);
        addressLine2 = address.substring(24, address.length());
      }
      // }
      fOut.write(addressLine1.getBytes(ASCII));
      fOut.write(COMMA);
      fOut.write(addressLine2.getBytes(ASCII));
      fOut.write(COMMA);
      fOut.write(getBytes(order.getDeliveryCity()));
      fOut.write(COMMA);
      fOut.write(getBytes(order.getDeliveryStateCode()));
      fOut.write(COMMA);
      fOut.write(getBytes(order.getDeliveryZip()));
      fOut.write(COMMA);
      fOut.write(COMMA);
      fOut.write('A');
      fOut.write(COMMA);
      fOut.write("5001".getBytes(ASCII));
      fOut.write(NEW_LINE);
      for (OimOrderDetails od : detailSet) {
        if (!od.getOimSuppliers().getSupplierId().equals(ovs.getOimSuppliers().getSupplierId()))
          continue;
        String skuPrefix = null, sku = od.getSku();
        if (!orderSkuPrefixMap.isEmpty()) {
          skuPrefix = orderSkuPrefixMap.values().toArray()[0].toString();
        }
        skuPrefix = StringHandle.removeNull(skuPrefix);
        if (sku.startsWith(skuPrefix)) {
          sku = sku.substring(skuPrefix.length());
        }
        fOut.write(sku.getBytes(ASCII));
        fOut.write(COMMA);
        fOut.write(COMMA);
        fOut.write(od.getQuantity().toString().getBytes(ASCII));
        fOut.write(COMMA);
        fOut.write(COMMA);
        fOut.write(COMMA);
        fOut.write(NEW_LINE);

//        OimChannels oimChannels = order.getOimOrderBatches().getOimChannels();
//
//        OimLogStream stream = new OimLogStream();
//
//        try {
//          IOrderImport iOrderImport = ChannelFactory.getIOrderImport(oimChannels);
//          OrderStatus orderStatus = new OrderStatus();
//          orderStatus.setStatus(
//              ((OimOrderProcessingRule) oimChannels.getOimOrderProcessingRules().iterator().next())
//                  .getProcessedStatus());
//          if (oimChannels.getTestMode() == 0)
//            iOrderImport.updateStoreOrder(od, orderStatus);
//
//        } catch (ChannelConfigurationException e) {
//          log.error(e.getMessage(), e);
//          stream.println(e.getMessage());
//        }
      }

      fOut.write(HG_EOF);
      fOut.write(NEW_LINE);
      fOut.flush();
      fOut.close();
    } catch (FileNotFoundException e) {
      log.error(e.getMessage(), e);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    return uploadfilename;
  }

  private byte[] getBytes(String str) throws UnsupportedEncodingException {
    return StringHandle.removeComma(StringHandle.removeNull(str).toUpperCase()).getBytes(ASCII);
  }

  private static final String ORDER_CONFIRMATION_FILE_PATH_TEMPLATE = "confirmations/%s.O%sA.txt";
  private static final String ORDER_SHIPPING_FILE_PATH_TEMPLATE = "shipping/%s.S%sA.txt";
  private static final String ORDER_TRACKING_FILE_PATH_TEMPLATE = "tracking/%s.T%sX.txt";
  private static final String ORDER_TRACKING_FILE_PATH_ARCHIVE_TEMPLATE = "archive/%s.T%sX.txt";

  private String getShippingFilePath(String account, String unfiOrderNo) {
    return getFilePath(ORDER_SHIPPING_FILE_PATH_TEMPLATE, account, unfiOrderNo);
  }

  private static String getConfirmationFilePath(String account, String unfiOrderNo) {
    return getFilePath(ORDER_CONFIRMATION_FILE_PATH_TEMPLATE, account, unfiOrderNo);
  }

  private static String getTrackingFilePath(String account, String unfiOrderNo) {
    return getFilePath(ORDER_TRACKING_FILE_PATH_TEMPLATE, account, unfiOrderNo);
  }

  private static String getTrackingFilePathInArchive(String account, String unfiOrderNo) {
    return getFilePath(ORDER_TRACKING_FILE_PATH_ARCHIVE_TEMPLATE, account, unfiOrderNo);
  }

  private static String getFilePath(String template, String account, String unfiOrderNo) {
    return String.format(template, account, unfiOrderNo);
  }

  @Override
  public OrderStatus getOrderStatus(OimVendorSuppliers ovs, final Object trackingMeta1,
      OimOrderDetails oimOrderDetails) throws SupplierOrderTrackingException {
    orderSkuPrefixMap = setSkuPrefixForOrders(ovs);
    if (!(trackingMeta1 instanceof String))
      throw new IllegalArgumentException(
          "trackingMeta is expected to be a String value containing UNFI Order number.");
    String trackingMeta = (String) trackingMeta1;
    log.info("Tracking request for PONUM: {}", trackingMeta);
    OrderStatus orderStatus = new OrderStatus();
    orderStatus.setStatus(oimOrderDetails.getSupplierOrderStatus());
    String supplierDefaultPrefix = ovs.getOimSuppliers().getDefaultSkuPrefix();
    int channelId = oimOrderDetails.getOimOrders().getOimOrderBatches().getOimChannels()
        .getChannelId();
    String configuredPrefix = orderSkuPrefixMap.get(channelId);
    FtpDetail ftpDetails = null;
    // if (trackingMeta.startsWith("H"))
    // ftpDetails = getFtpDetails(ovs, true);
    // else if (trackingMeta.startsWith("P"))
    // ftpDetails = getFtpDetails(ovs, false);
    if (oimOrderDetails.getSupplierWareHouseCode() != null
        && oimOrderDetails.getSupplierWareHouseCode().equals("H"))
      ftpDetails = FtpDetail.getFtpDetails(ovs, true);
    else if (oimOrderDetails.getSupplierWareHouseCode() != null
        && oimOrderDetails.getSupplierWareHouseCode().equals("P"))
      ftpDetails = FtpDetail.getFtpDetails(ovs, false);
    // else {
    // FtpDetail hvaFtpDetail = getFtpDetails(ovs, true);
    // FtpDetail phiFtpDetail = getFtpDetails(ovs, false);
    // boolean isSingleWarehouseConfigured = false;
    // boolean isHva = false;
    // if (hvaFtpDetail.getUrl() != null && phiFtpDetail.getUrl() == null) {
    // isSingleWarehouseConfigured = true;
    // isHva = true;
    // } else if (phiFtpDetail.getUrl() != null && hvaFtpDetail.getUrl() == null) {
    // isSingleWarehouseConfigured = true;
    // isHva = false;
    // }
    // if (!isSingleWarehouseConfigured)
    // isHva = isRestricted(oimOrderDetails.getSku(), ovs.getVendors().getVendorId(),
    // supplierDefaultPrefix, configuredPrefix);
    // if (isHva) {
    // ftpDetails = hvaFtpDetail;
    // } else {
    // ftpDetails = phiFtpDetail;
    // }
    // }
    if (ftpDetails != null) {
      FTPClient ftp = new FTPClient();
      try {
        ftp.setRemoteHost(ftpDetails.getUrl());
        ftp.setDetectTransferMode(true);
        ftp.connect();
        ftp.login(ftpDetails.getUserName(), ftpDetails.getPassword());
        ftp.setTimeout(60 * 1000 * 60 * 7);
        String unfiNumber = findUNFIFromConfirmations(ftp, oimOrderDetails, trackingMeta,
            orderStatus);
        getTrackingInfo(ftpDetails, ftp, unfiNumber, orderStatus, oimOrderDetails, trackingMeta);
      } catch (IOException | FTPException | ParseException | JAXBException e) {
        log.error(e.getMessage(), e);

      }

    }
    serializeMap();
    return orderStatus;
  }

  private static synchronized void serializeMap() {
    FileLock lock = null;
    try {
      updateObjectFromCacheFile();
      FileOutputStream fs = new FileOutputStream(CACHE_FILENAME);
      lock = fs.getChannel().lock();
      ObjectOutputStream os = new ObjectOutputStream(fs);
      os.writeObject(PONUM_UNFI_MAP);
      os.close();
    } catch (Exception e) {
      log.warn("Error occured while serializing HG PO map to {}", CACHE_FILENAME);
    } finally {
      if (lock != null && lock.isValid())
        try {
          lock.release();
        } catch (IOException e) {
          log.warn("Error in releasing lock", e);
        }
    }
  }

  private void getTrackingInfo(FtpDetail ftpDetails, FTPClient ftp, String unfiOrderNo,
      OrderStatus orderStatus, OimOrderDetails detail, String poNum)
          throws FTPException, IOException, ParseException, JAXBException {
    byte[] trackingFileData = null;

    String skuPrefix = null;
    String sku = detail.getSku();
    if (!orderSkuPrefixMap.isEmpty()) {
      // skuPrefix = orderSkuPrefixMap.values().toArray()[0].toString();
      int channelId = detail.getOimOrders().getOimOrderBatches().getOimChannels().getChannelId();
      skuPrefix = orderSkuPrefixMap.get(channelId);
    }
    skuPrefix = StringHandle.removeNull(skuPrefix);
    if (sku.startsWith(skuPrefix)) {
      sku = sku.substring(skuPrefix.length());
    }

    Map<String, String> parseShippingConfirmation = new HashMap<String, String>();
    if (unfiOrderNo != null) {
      String trackingFilePath = getTrackingFilePath(ftpDetails.getAccountNumber(), unfiOrderNo);
      orderStatus.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_IN_PROCESS);
      String shippingFilePath = getShippingFilePath(ftpDetails.getAccountNumber(), unfiOrderNo);
      try {
        byte[] shippingFileData = ftp.get(shippingFilePath);
        Map<Integer, String> shippingDataMap = parseFileData(shippingFileData);
        parseShippingConfirmation = parseShippingConfirmation(shippingDataMap, sku);
      } catch (FTPException e) {
        log.warn("Shipping {}", e.getMessage());

      }
      try {
        trackingFileData = ftp.get(trackingFilePath);
      } catch (FTPException e) {
        log.warn("Tracking {}", e.getMessage());
        trackingFileData = null;
      }
    } else {

      FTPFile[] ftpFiles = ftp.dirDetails("shipping");
      Arrays.sort(ftpFiles, new Comparator<FTPFile>() {
        public int compare(FTPFile f1, FTPFile f2) {
          return f2.lastModified().compareTo(f1.lastModified());
        }
      });
      for (FTPFile ftpFile : ftpFiles) {
        String shippingFileName = ftpFile.getName();
        if (shippingFileName.equals("..") || shippingFileName.equals("."))
          continue;

        if (ftpFile.lastModified().before(detail.getProcessingTm()))
          break;
        byte[] sippingFileData = ftp.get("shipping/" + shippingFileName);
        if (sippingFileData != null) {
          Map<Integer, String> shippingDataMap = parseFileData(sippingFileData);
          parseShippingConfirmation = parseShippingConfirmation(shippingDataMap, sku);
          log.debug("Shipping {}", parseShippingConfirmation);
          if (poNum.equals(parseShippingConfirmation.get(PONUM))) {
            unfiOrderNo = parseShippingConfirmation.get(UNFIORDERNO);

            if (unfiOrderNo != null) {
              orderStatus.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_IN_PROCESS);

              String trackingFilePath = getTrackingFilePath(ftpDetails.getAccountNumber(),
                  unfiOrderNo);
              try {
                trackingFileData = ftp.get(trackingFilePath);
              } catch (FTPException e) {
                log.warn("Tracking file not found in Tracking folder .. going to archive..");
                try {
                  trackingFileData = ftp.get(
                      getTrackingFilePathInArchive(ftpDetails.getAccountNumber(), unfiOrderNo));
                } catch (FTPException e1) {
                  log.warn("Tracking file not found in Archive folder ..");
                }
                log.info("Found under Archive folder...");
              }
            }
            break;
          }
        }
      }
    }

    if (trackingFileData != null) {
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      String s = new String(trackingFileData);
      StringReader reader = new StringReader(s);
      log.info("Tracking file data for {} is --  {}", poNum, s);
      TrackingData orderTrackingResponse = (TrackingData) unmarshaller.unmarshal(reader);
      orderStatus.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_SHIPPED);
      int qtyShipped = detail.getQuantity();
      if (parseShippingConfirmation != null & parseShippingConfirmation.containsKey(QTY_SHIPPED)) {
        qtyShipped = Integer.parseInt(parseShippingConfirmation.get(QTY_SHIPPED));
      }
      int perBoxQty = 1;
      if (detail.getQuantity() == orderTrackingResponse.getPOTracking().size()) {
        perBoxQty = 1;
      } else {
        if (detail.getQuantity() % orderTrackingResponse.getPOTracking().size() == 0) {
          perBoxQty = detail.getQuantity() / orderTrackingResponse.getPOTracking().size();
        } else if (orderTrackingResponse.getPOTracking().size() % detail.getQuantity() == 0)
          perBoxQty = orderTrackingResponse.getPOTracking().size() / detail.getQuantity();
      }
      for (POTracking poTracking : orderTrackingResponse.getPOTracking()) {
        salesmachine.oim.suppliers.modal.TrackingData trackingData = new salesmachine.oim.suppliers.modal.TrackingData();
        String trackingNum = poTracking.getTrackingNumber();
        if ("A".equals(orderTrackingResponse.getPO().getShipVia())) {
          if (trackingNum != null && trackingNum.startsWith("1")) {
            trackingData.setCarrierCode("UPS");
            trackingData.setCarrierName("UPS");
            trackingData.setShippingMethod("Ground");
          } else if (trackingNum != null && trackingNum.startsWith("9")) {
            trackingData.setCarrierCode("USPS");
            trackingData.setCarrierName("USPS");
            trackingData.setShippingMethod("PRIORITY");
          } else {
            trackingData.setCarrierCode("UPS");
            trackingData.setCarrierName("UPS");
            trackingData.setShippingMethod("Ground");
          }
        } else {
          trackingData.setCarrierName(orderTrackingResponse.getPO().getShipVia());
        }
        trackingData.setShipperTrackingNumber(poTracking.getTrackingNumber());
        trackingData.setQuantity(qtyShipped);
        trackingData.setShipDate(orderTrackingResponse.getPO().getDeliveryDate());
        orderStatus.addTrackingData(trackingData);
      }
      log.info("Tracking details: {}", orderStatus);
    } else {
      log.info("Tracking Details not updated for - {}", poNum);
    }
  }

  private String findUNFIFromConfirmations(FTPClient ftp, OimOrderDetails detail,
      String tempTrackingMeta, OrderStatus orderStatus)
          throws IOException, FTPException, ParseException {
    log.info("UNFI MAP Size: {}", PONUM_UNFI_MAP.size());
    if (PONUM_UNFI_MAP.containsKey(tempTrackingMeta)) {
      log.info("UNFI Order Id found in MAP {}", PONUM_UNFI_MAP.get(tempTrackingMeta));
      orderStatus.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_IN_PROCESS);
      return PONUM_UNFI_MAP.get(tempTrackingMeta);
    }
    FTPFile[] ftpFiles = ftp.dirDetails("confirmations");
    Arrays.sort(ftpFiles, new Comparator<FTPFile>() {
      public int compare(FTPFile f1, FTPFile f2) {
        return f2.lastModified().compareTo(f1.lastModified());
      }
    });
    for (FTPFile ftpFile : ftpFiles) {
      String confirmationFile = ftpFile.getName();
      if (confirmationFile.equals("..") || confirmationFile.equals("."))
        continue;

      // log.info(
      // "Confirmation file path: {} Last Modified: {} Order Processing Time: {}",
      // ftpFile.getName(), ftpFile.lastModified(),
      // detail.getProcessingTm());
      if (ftpFile.lastModified().before(detail.getProcessingTm()))
        break;
      byte[] confirmationFileData = ftp.get("confirmations/" + confirmationFile);
      Map<Integer, String> orderDataMap = parseFileData(confirmationFileData);
      Map<String, String> orderData = parseOrderConfirmation(orderDataMap, tempTrackingMeta);
      if (tempTrackingMeta.toString().equals(orderData.get(PONUM))) {
        log.info("Order Confirmation details found for {}", orderData);
        orderStatus.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_IN_PROCESS);

        return orderData.get(UNFIORDERNO);
      }
    }
    return null;
  }

  private static String sellerId = "A2V8R85K60KD3B",
      mwsAuthToken = "amzn.mws.c8a76813-c733-c66e-5215-2ef9bcecff4b";
  private static List<String> marketPlaceIdList = new ArrayList<String>();
  private static final MarketplaceWebService service;
  private static JAXBContext jaxbContext2;

  static {
    MarketplaceWebServiceConfig config = new MarketplaceWebServiceConfig();
    config.setServiceURL(ApplicationProperties.getProperty(ApplicationProperties.MWS_SERVICE_URL));
    service = new MarketplaceWebServiceClient(
        ApplicationProperties.getProperty(ApplicationProperties.MWS_ACCESS_KEY),
        ApplicationProperties.getProperty(ApplicationProperties.MWS_SECRET_KEY),
        ApplicationProperties.getProperty(ApplicationProperties.MWS_APP_NAME),
        ApplicationProperties.getProperty(ApplicationProperties.MWS_APP_VERSION), config);

    try {
      jaxbContext2 = JAXBContext.newInstance(OrderFulfillment.class, SubmitFeedRequest.class,
          OrderAcknowledgement.class, AmazonEnvelope.class);
      marketPlaceIdList.add("ATVPDKIKX0DER");
    } catch (JAXBException e) {
      log.error(e.getMessage(), e);
    }
  }

//  public static void main(String[] args) {
//    // updateFromConfirmation();
//    // updateFromTracking();
//    Session session = SessionManager.currentSession();
//    OimVendorSuppliers ovs = (OimVendorSuppliers) session.get(OimVendorSuppliers.class, 9681);
//    Query query = session.createSQLQuery(
//        "select order_id from kdyer.oim_orders where store_order_id in ('002-5991298-7306626')");
//    List list = query.list();
//
//    for (int i = 0; i < list.size(); i++) {
//      int orderId = ((BigDecimal) list.get(i)).intValue();
//      System.out.println(orderId);
//      try {
//        OimOrders order = (OimOrders) session.get(OimOrders.class, orderId);
//        new HonestGreen().sendOrders(735585, ovs, order);
//      } catch (SupplierConfigurationException | SupplierCommunicationException
//          | SupplierOrderException | ChannelConfigurationException | ChannelCommunicationException
//          | ChannelOrderFormatException e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//      }
//    }
//    // OimOrders order = (OimOrders) session.get(OimOrders.class, 477569);
//
//  }

  public static void main2(String[] args) {
    Marshaller marshaller = null;
    try {
      marshaller = jaxbContext2.createMarshaller();
    } catch (JAXBException e) {
      log.error(e.getMessage(), e);

    }
    AmazonEnvelope envelope = new AmazonEnvelope();
    Header header = new Header();
    header.setDocumentVersion("1.01");
    header.setMerchantIdentifier(sellerId);
    envelope.setHeader(header);
    envelope.setMessageType("OrderFulfillment");
    long msgId = 1L;

    SubmitFeedRequest submitFeedRequest = new SubmitFeedRequest();
    submitFeedRequest.setMerchant(sellerId);
    submitFeedRequest.setMWSAuthToken(mwsAuthToken);
    submitFeedRequest.setMarketplaceIdList(new IdList(marketPlaceIdList));
    submitFeedRequest.setFeedType("_POST_ORDER_FULFILLMENT_DATA_");
    Session session = SessionManager.currentSession();
    SQLQuery sqlQuery = session.createSQLQuery(
        "select * from kdyer.oim_order_tracking t where t.detail_id in (select d.detail_id from KDYER.OIM_ORDER_DETAILS d where"
            + " d.status_id=7 and d.PROCESSING_TM>to_date('01-MAR-16','DD-Mon-YY'))");
    sqlQuery.addEntity(OimOrderTracking.class);
    List<OimOrderTracking> trackingList = sqlQuery.list();
    Transaction tx = session.beginTransaction();
    for (OimOrderTracking td : trackingList) {
      OimOrderDetails detail = td.getDetail();
      System.out.println("updating order detail --" + detail.getDetailId());
      detail.setSupplierOrderStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_COMPLETED);
      detail.setOimOrderStatuses(new OimOrderStatuses(OimConstants.ORDER_STATUS_COMPLETE));
      session.saveOrUpdate(detail);
      Message message = new Message();
      message.setMessageID(BigInteger.valueOf(msgId++));
      envelope.getMessage().add(message);
      OrderFulfillment fulfillment = new OrderFulfillment();
      message.setOrderFulfillment(fulfillment);
      fulfillment.setAmazonOrderID(detail.getOimOrders().getStoreOrderId());
      fulfillment.setMerchantFulfillmentID(
          BigInteger.valueOf(detail.getOimOrders().getOrderId().longValue()));
      GregorianCalendar c = new GregorianCalendar();
      c.setTime(td.getShipDate());
      XMLGregorianCalendar shipDate = null;
      try {
        shipDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
      } catch (DatatypeConfigurationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      fulfillment.setFulfillmentDate(shipDate);
      Item i = new Item();
      i.setAmazonOrderItemCode(detail.getStoreOrderItemId());
      i.setQuantity(BigInteger.valueOf(td.getShipQuantity()));
      i.setMerchantFulfillmentItemID(BigInteger.valueOf(detail.getDetailId()));
      FulfillmentData value = new FulfillmentData();
      // value.setCarrierCode(orderStatus.getTrackingData().getCarrierCode());
      value.setCarrierName(td.getShippingCarrier());
      value.setShipperTrackingNumber(td.getTrackingNumber());
      value.setShippingMethod(td.getShippingMethod());
      fulfillment.getItem().add(i);
      fulfillment.setFulfillmentData(value);
    }

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    if (marshaller != null) {
      try {
        marshaller.marshal(envelope, os);
      } catch (JAXBException e) {
        log.error(e.getMessage(), e);
        // throw new ChannelOrderFormatException(
        // "Error in Updating Store order - " + e.getMessage(), e);
      }
    }
    InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
    submitFeedRequest.setFeedContent(inputStream);
    try {
      submitFeedRequest.setContentMD5(
          Base64.encode((MessageDigest.getInstance("MD5").digest(os.toByteArray()))));
    } catch (NoSuchAlgorithmException e) {
      log.error(e.getMessage(), e);
      // throw new ChannelCommunicationException(
      // "Error in submiting feed request while updating order to store - " + e.getMessage(),
      // e);
    }
    log.info("SubmitFeedRequest: {}", os.toString());
    SubmitFeedResponse submitFeed = null;
    try {
      if (envelope.getMessage().size() > 0) {
        submitFeed = service.submitFeed(submitFeedRequest);
        log.info(submitFeed.toXML());
      } else {
        log.info("Feed not submitted, Evnelop is empty.");
      }

    } catch (MarketplaceWebServiceException e) {
      log.error(e.getMessage(), e);
      // throw new ChannelCommunicationException(
      // "Error in submiting feed request while updating order to store - " + e.getMessage(),
      // e);
    }
    tx.commit();
  }

  public static Integer updateFromConfirmation() {
    int totalValidPO = 0, shippedPO = 0, orderTrackCount = 0;

    List<FtpDetail> ftpList = new ArrayList<FtpDetail>(2);

    FtpDetail ftpDetails2 = new FtpDetail();
    ftpDetails2.setUrl("ftp1.unfi.com");
    ftpDetails2.setAccountNumber("40968");
    ftpDetails2.setUserName("evox");
    ftpDetails2.setPassword("evoftp093!");
    ftpList.add(ftpDetails2);

    FtpDetail ftpDetails1 = new FtpDetail();
    ftpDetails1.setUrl("ftp1.unfi.com");
    ftpDetails1.setAccountNumber("70757");
    ftpDetails1.setUserName("70757");
    ftpDetails1.setPassword("vU!6akAB");
    ftpList.add(ftpDetails1);
    for (FtpDetail ftpDetails : ftpList) {

      FTPClient ftp = new FTPClient();
      Session session = SessionManager.currentSession();
      Transaction tx = null;
      SubmitFeedRequest submitFeedRequest = new SubmitFeedRequest();
      submitFeedRequest.setMerchant(sellerId);
      submitFeedRequest.setMWSAuthToken(mwsAuthToken);
      submitFeedRequest.setMarketplaceIdList(new IdList(marketPlaceIdList));
      submitFeedRequest.setFeedType("_POST_ORDER_FULFILLMENT_DATA_");

      Marshaller marshaller = null;
      try {
        marshaller = jaxbContext2.createMarshaller();
      } catch (JAXBException e) {
        log.error(e.getMessage(), e);

      }
      AmazonEnvelope envelope = new AmazonEnvelope();
      Header header = new Header();
      header.setDocumentVersion("1.01");
      header.setMerchantIdentifier(sellerId);
      envelope.setHeader(header);
      envelope.setMessageType("OrderFulfillment");
      long msgId = 1L;
      try {

        ftp.setRemoteHost(ftpDetails.getUrl());
        ftp.setDetectTransferMode(true);
        ftp.connect();
        ftp.login(ftpDetails.getUserName(), ftpDetails.getPassword());
        ftp.setTimeout(60 * 1000 * 60 * 7);

        int channelId = 2941;
        IOrderImport iOrderImport = ChannelFactory.getIOrderImport(channelId);

        FTPFile[] ftpFiles = ftp.dirDetails("confirmations");
        Arrays.sort(ftpFiles, new Comparator<FTPFile>() {
          public int compare(FTPFile f1, FTPFile f2) {
            return f2.lastModified().compareTo(f1.lastModified());
          }
        });
        Date cutoff = new Date(115, 7, 16);
        log.info("Cutoff: {}", cutoff);
        for (FTPFile ftpFile : ftpFiles) {
          try {
            log.trace("File: {} Modified@: {}", ftpFile.getName(), ftpFile.lastModified());
            if (ftpFile.getName().equals(".") || ftpFile.getName().equals("..")
                || ftpFile.getName().endsWith("S.txt"))
              continue;

            byte[] confirmationFileData = ftp.get("confirmations/" + ftpFile.getName());

            Map<Integer, String> parseFileData = parseFileData(confirmationFileData);

            parseOrderConfirmation(parseFileData, "");
            if (ftpFile.lastModified().before(cutoff))
              break;
          } catch (Exception e) {
            if (tx != null && tx.isActive()) {
              tx.rollback();
            }
            log.error(e.getMessage(), e);
          }
        }
        log.info("Found {} order confirmations till {}", PONUM_UNFI_MAP.size(), cutoff);
        for (String purchaseOrder : PONUM_UNFI_MAP.keySet()) {
          try {
            OimOrderDetails detail = (OimOrderDetails) session.createCriteria(OimOrderDetails.class)
                .add(Restrictions.eq("supplierOrderNumber", purchaseOrder)).uniqueResult();

            if (detail == null
                || detail.getOimOrderStatuses().getStatusId()
                    .intValue() == OimConstants.ORDER_STATUS_SHIPPED.intValue()
                || detail.getOimOrderStatuses().getStatusId()
                    .intValue() == OimConstants.ORDER_STATUS_MANUALLY_PROCESSED.intValue())
              continue;
            session.refresh(detail);
            tx = session.beginTransaction();
            String unfiOrderNo = PONUM_UNFI_MAP.get(purchaseOrder);
            OrderStatus orderStatus = new OrderStatus();
            orderStatus.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_IN_PROCESS);
            totalValidPO++;
            try {
              byte[] bs;
              try {
                bs = ftp.get(getTrackingFilePath(ftpDetails.getAccountNumber(), unfiOrderNo));
              } catch (FTPException e) {
                log.warn("Tracking not found PO: {}  UNFI: {}", purchaseOrder, unfiOrderNo);
                bs = ftp
                    .get(getTrackingFilePathInArchive(ftpDetails.getAccountNumber(), unfiOrderNo));
              }
              Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
              String s = new String(bs);
              StringReader reader = new StringReader(s);
              // log.info(s);
              TrackingData orderTrackingResponse = (TrackingData) unmarshaller.unmarshal(reader);

              orderStatus.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_SHIPPED);

              log.info("PONUM# {}", purchaseOrder);

              int perBoxQty = 1;
              if (detail.getQuantity() == orderTrackingResponse.getPOTracking().size()) {
                perBoxQty = 1;
              } else {
                if (detail.getQuantity() % orderTrackingResponse.getPOTracking().size() == 0) {
                  perBoxQty = detail.getQuantity() / orderTrackingResponse.getPOTracking().size();
                } else if (orderTrackingResponse.getPOTracking().size() % detail.getQuantity() == 0)
                  perBoxQty = orderTrackingResponse.getPOTracking().size() / detail.getQuantity();
              }
              for (POTracking poTracking : orderTrackingResponse.getPOTracking()) {
                salesmachine.oim.suppliers.modal.TrackingData trackingData = new salesmachine.oim.suppliers.modal.TrackingData();
                if ("A".equals(orderTrackingResponse.getPO().getShipVia())) {
                  trackingData.setCarrierCode("UPS");
                  trackingData.setCarrierName("UPS");
                  trackingData.setShippingMethod("Ground");

                } else {
                  trackingData.setCarrierName(orderTrackingResponse.getPO().getShipVia());
                }
                trackingData.setShipperTrackingNumber(poTracking.getTrackingNumber());

                trackingData.setQuantity(perBoxQty);
                trackingData.setShipDate(orderTrackingResponse.getPO().getDeliveryDate());
                orderStatus.addTrackingData(trackingData);
                shippedPO++;
              }
              if (orderStatus.isShipped()) {
                List<salesmachine.oim.suppliers.modal.TrackingData> trackingDataList = orderStatus
                    .getTrackingData();
                for (salesmachine.oim.suppliers.modal.TrackingData td : trackingDataList) {
                  OimOrderTracking oimOrderTracking = new OimOrderTracking();
                  oimOrderTracking.setInsertionTime(new Date());
                  oimOrderTracking.setShipDate(td.getShipDate().toGregorianCalendar().getTime());
                  oimOrderTracking.setShippingCarrier(td.getCarrierName());
                  oimOrderTracking.setShippingMethod(td.getShippingMethod());
                  oimOrderTracking.setShipQuantity(td.getQuantity());
                  oimOrderTracking.setTrackingNumber(td.getShipperTrackingNumber());
                  oimOrderTracking.setDetail(detail);
                  session.save(oimOrderTracking);
                }
              }
            } catch (FTPException e) {
              log.warn("Tracking not found PO: {}  UNFI: {}", purchaseOrder, unfiOrderNo);
            }
            log.info("Tracking details: {}", orderStatus);

            detail.setSupplierOrderStatus(orderStatus.toString());
            if (orderStatus.isShipped()) {
              detail.setOimOrderStatuses(new OimOrderStatuses(OimConstants.ORDER_STATUS_SHIPPED));
              orderTrackCount++;
              for (salesmachine.oim.suppliers.modal.TrackingData td : orderStatus
                  .getTrackingData()) {
                Message message = new Message();
                message.setMessageID(BigInteger.valueOf(msgId++));
                envelope.getMessage().add(message);
                OrderFulfillment fulfillment = new OrderFulfillment();
                message.setOrderFulfillment(fulfillment);
                fulfillment.setAmazonOrderID(detail.getOimOrders().getStoreOrderId());
                fulfillment.setMerchantFulfillmentID(
                    BigInteger.valueOf(detail.getOimOrders().getOrderId().longValue()));
                fulfillment.setFulfillmentDate(td.getShipDate());
                Item i = new Item();
                i.setAmazonOrderItemCode(detail.getStoreOrderItemId());
                i.setQuantity(BigInteger.valueOf(td.getQuantity()));
                i.setMerchantFulfillmentItemID(BigInteger.valueOf(detail.getDetailId()));
                FulfillmentData value = new FulfillmentData();
                // value.setCarrierCode(orderStatus.getTrackingData().getCarrierCode());
                value.setCarrierName(td.getCarrierName());
                value.setShipperTrackingNumber(td.getShipperTrackingNumber());
                value.setShippingMethod(td.getShippingMethod());
                fulfillment.getItem().add(i);
                fulfillment.setFulfillmentData(value);
              }
            }
            session.update(detail);
            tx.commit();

          } catch (Exception e) {
            if (tx != null && tx.isActive()) {
              tx.rollback();
            }
          }
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        if (marshaller != null) {
          try {
            marshaller.marshal(envelope, os);
          } catch (JAXBException e) {
            log.error(e.getMessage(), e);
            throw new ChannelOrderFormatException(
                "Error in Updating Store order - " + e.getMessage(), e);
          }
        }
        InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
        submitFeedRequest.setFeedContent(inputStream);
        try {
          submitFeedRequest.setContentMD5(
              Base64.encode((MessageDigest.getInstance("MD5").digest(os.toByteArray()))));
        } catch (NoSuchAlgorithmException e) {
          log.error(e.getMessage(), e);
          throw new ChannelCommunicationException(
              "Error in submiting feed request while updating order to store - " + e.getMessage(),
              e);
        }
        log.info("SubmitFeedRequest: {}", os.toString());
        SubmitFeedResponse submitFeed = null;
        try {
          if (envelope.getMessage().size() > 0) {
            submitFeed = service.submitFeed(submitFeedRequest);
            log.info(submitFeed.toXML());
          } else {
            log.info("Feed not submitted, Evnelop is empty.");
          }
        } catch (MarketplaceWebServiceException e) {
          log.error(e.getMessage(), e);
          throw new ChannelCommunicationException(
              "Error in submiting feed request while updating order to store - " + e.getMessage(),
              e);
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }
    log.info("Total PO Found {}, Shipped PO{}", totalValidPO, shippedPO);
    serializeMap();
    return orderTrackCount;
  }

  public static Integer updateFromTracking() {
    int orderTrackCount = 0;
    List<FtpDetail> ftpList = new ArrayList<FtpDetail>(2);
    FtpDetail ftpDetails2 = new FtpDetail();
    ftpDetails2.setUrl("ftp1.unfi.com");
    ftpDetails2.setAccountNumber("40968");
    ftpDetails2.setUserName("evox");
    ftpDetails2.setPassword("evoftp093!");
    ftpList.add(ftpDetails2);

    FtpDetail ftpDetails1 = new FtpDetail();
    ftpDetails1.setUrl("ftp1.unfi.com");
    ftpDetails1.setAccountNumber("70757");
    ftpDetails1.setUserName("70757");
    ftpDetails1.setPassword("vU!6akAB");
    ftpList.add(ftpDetails1);

    for (FtpDetail ftpDetails : ftpList) {

      FTPClient ftp = new FTPClient();
      Session session = SessionManager.currentSession();

      SubmitFeedRequest submitFeedRequest = new SubmitFeedRequest();
      submitFeedRequest.setMerchant(sellerId);
      submitFeedRequest.setMWSAuthToken(mwsAuthToken);
      submitFeedRequest.setMarketplaceIdList(new IdList(marketPlaceIdList));
      submitFeedRequest.setFeedType("_POST_ORDER_FULFILLMENT_DATA_");

      Marshaller marshaller = null;
      try {
        marshaller = jaxbContext2.createMarshaller();
      } catch (JAXBException e) {
        log.error(e.getMessage(), e);

      }
      AmazonEnvelope envelope = new AmazonEnvelope();
      Header header = new Header();
      header.setDocumentVersion("1.01");
      header.setMerchantIdentifier(sellerId);
      envelope.setHeader(header);
      envelope.setMessageType("OrderFulfillment");
      long msgId = 1L;

      Transaction tx = null;

      try {

        int channelId = 2941;
        IOrderImport iOrderImport = ChannelFactory.getIOrderImport(channelId);

        ftp.setRemoteHost(ftpDetails.getUrl());
        ftp.setDetectTransferMode(true);
        ftp.connect();
        ftp.login(ftpDetails.getUserName(), ftpDetails.getPassword());
        ftp.setTimeout(60 * 1000 * 60 * 7);
        Date cutoff = new Date(115, 7, 10);
        log.info("Cutoff: {}", cutoff);
        String dirname = "tracking";
        FTPFile[] ftpFiles = ftp.dirDetails(dirname);
        Arrays.sort(ftpFiles, new Comparator<FTPFile>() {
          public int compare(FTPFile f1, FTPFile f2) {
            return f2.lastModified().compareTo(f1.lastModified());
          }
        });
        for (FTPFile ftpFile : ftpFiles) {
          try {

            log.trace("Tracking File:{} Modified@", ftpFile.getName(), ftpFile.lastModified());
            if (ftpFile.getName().equals(".") || ftpFile.getName().equals("..")
                || ftpFile.getName().endsWith("S.txt"))
              continue;
            if (ftpFile.lastModified().before(cutoff))
              break;
            byte[] bs = ftp.get(dirname + "/" + ftpFile.getName());
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            String s = new String(bs);
            StringReader reader = new StringReader(s);
            // log.info(s);
            TrackingData orderTrackingResponse = (TrackingData) unmarshaller.unmarshal(reader);
            OrderStatus orderStatus = new OrderStatus();
            orderStatus.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_SHIPPED);
            String purchaseOrder = orderTrackingResponse.getPO().getPurchaseOrder();
            String[] poArray = { purchaseOrder, purchaseOrder.replaceFirst("P", ""),
                purchaseOrder.replaceFirst("H", "") };
            List<OimOrderDetails> detailList = session.createCriteria(OimOrderDetails.class)
                .add(Restrictions.in("supplierOrderNumber", poArray)).list();
            for (OimOrderDetails detail : detailList) {
              // setSkuPrefixForOrders(ovs);
              if (!detail.getSku().startsWith("HG"))
                continue;
              if (detail == null
                  || detail.getOimOrderStatuses().getStatusId()
                      .intValue() == OimConstants.ORDER_STATUS_SHIPPED.intValue()
                  || detail.getOimOrderStatuses().getStatusId()
                      .intValue() == OimConstants.ORDER_STATUS_MANUALLY_PROCESSED.intValue())
                continue;
              session.refresh(detail);
              log.debug("PONUM# {}", purchaseOrder);
              tx = session.beginTransaction();
              int perBoxQty = 1;
              if (detail.getQuantity() == orderTrackingResponse.getPOTracking().size()) {
                perBoxQty = 1;
              } else {
                if (detail.getQuantity() % orderTrackingResponse.getPOTracking().size() == 0) {
                  perBoxQty = detail.getQuantity() / orderTrackingResponse.getPOTracking().size();
                } else if (orderTrackingResponse.getPOTracking().size() % detail.getQuantity() == 0)
                  perBoxQty = orderTrackingResponse.getPOTracking().size() / detail.getQuantity();
              }
              for (POTracking poTracking : orderTrackingResponse.getPOTracking()) {
                salesmachine.oim.suppliers.modal.TrackingData trackingData = new salesmachine.oim.suppliers.modal.TrackingData();
                if ("A".equals(orderTrackingResponse.getPO().getShipVia())) {
                  trackingData.setCarrierCode("UPS");
                  trackingData.setCarrierName("UPS");
                  trackingData.setShippingMethod("Ground");

                } else {
                  trackingData.setCarrierName(orderTrackingResponse.getPO().getShipVia());
                }
                trackingData.setShipperTrackingNumber(poTracking.getTrackingNumber());

                trackingData.setQuantity(perBoxQty);
                trackingData.setShipDate(orderTrackingResponse.getPO().getDeliveryDate());
                orderStatus.addTrackingData(trackingData);

              }
              if (orderStatus.isShipped()) {
                List<salesmachine.oim.suppliers.modal.TrackingData> trackingDataList = orderStatus
                    .getTrackingData();
                for (salesmachine.oim.suppliers.modal.TrackingData td : trackingDataList) {
                  OimOrderTracking oimOrderTracking = new OimOrderTracking();
                  oimOrderTracking.setInsertionTime(new Date());
                  oimOrderTracking.setShipDate(td.getShipDate().toGregorianCalendar().getTime());
                  oimOrderTracking.setShippingCarrier(td.getCarrierName());
                  oimOrderTracking.setShippingMethod(td.getShippingMethod());
                  oimOrderTracking.setShipQuantity(td.getQuantity());
                  oimOrderTracking.setTrackingNumber(td.getShipperTrackingNumber());
                  oimOrderTracking.setDetail(detail);
                  session.save(oimOrderTracking);
                }
              }
              log.info("Tracking details: {}", orderStatus);
              detail.setSupplierOrderStatus(orderStatus.toString());
              if (orderStatus.isShipped()) {
                detail.setOimOrderStatuses(new OimOrderStatuses(OimConstants.ORDER_STATUS_SHIPPED));
                orderTrackCount++;
              }
              session.update(detail);

              /*
               * synchronized (iOrderImport) { iOrderImport.updateStoreOrder(detail, orderStatus); }
               */
              tx.commit();
              for (salesmachine.oim.suppliers.modal.TrackingData td : orderStatus
                  .getTrackingData()) {
                Message message = new Message();
                message.setMessageID(BigInteger.valueOf(msgId++));
                envelope.getMessage().add(message);
                OrderFulfillment fulfillment = new OrderFulfillment();
                message.setOrderFulfillment(fulfillment);
                fulfillment.setAmazonOrderID(detail.getOimOrders().getStoreOrderId());
                fulfillment.setMerchantFulfillmentID(
                    BigInteger.valueOf(detail.getOimOrders().getOrderId().longValue()));
                fulfillment.setFulfillmentDate(td.getShipDate());
                Item i = new Item();
                i.setAmazonOrderItemCode(detail.getStoreOrderItemId());
                i.setQuantity(BigInteger.valueOf(td.getQuantity()));
                i.setMerchantFulfillmentItemID(BigInteger.valueOf(detail.getDetailId()));
                FulfillmentData value = new FulfillmentData();
                // value.setCarrierCode(orderStatus.getTrackingData().getCarrierCode());
                value.setCarrierName(td.getCarrierName());
                value.setShipperTrackingNumber(td.getShipperTrackingNumber());
                value.setShippingMethod(td.getShippingMethod());
                fulfillment.getItem().add(i);
                fulfillment.setFulfillmentData(value);
              }
            }
          } catch (Exception e) {
            if (tx != null && tx.isActive()) {
              tx.rollback();
            }
            log.error(e.getMessage(), e);
          }

        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        if (marshaller != null) {
          try {
            marshaller.marshal(envelope, os);
          } catch (JAXBException e) {
            log.error(e.getMessage(), e);
            throw new ChannelOrderFormatException(
                "Error in Updating Store order - " + e.getMessage(), e);
          }
        }
        InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
        submitFeedRequest.setFeedContent(inputStream);
        try {
          submitFeedRequest.setContentMD5(
              Base64.encode((MessageDigest.getInstance("MD5").digest(os.toByteArray()))));
        } catch (NoSuchAlgorithmException e) {
          log.error(e.getMessage(), e);
          throw new ChannelCommunicationException(
              "Error in submiting feed request while updating order to store - " + e.getMessage(),
              e);
        }
        log.info("SubmitFeedRequest: {}", os.toString());
        SubmitFeedResponse submitFeed = null;
        try {
          if (envelope.getMessage().size() > 0) {
            submitFeed = service.submitFeed(submitFeedRequest);
            log.info(submitFeed.toXML());
          } else {
            log.info("Feed not submitted, Evnelop is empty.");
          }

        } catch (MarketplaceWebServiceException e) {
          log.error(e.getMessage(), e);
          throw new ChannelCommunicationException(
              "Error in submiting feed request while updating order to store - " + e.getMessage(),
              e);
        }
      } catch (Exception e) {
        log.error(e.getMessage());
      }
    }
    serializeMap();
    return orderTrackCount;
  }
}
