package salesmachine.oim.suppliers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;
import com.enterprisedt.net.ftp.FTPTransferType;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderProcessingRule;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSupplierShippingMethod;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.Reps;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.stores.impl.ChannelFactory;
import salesmachine.oim.suppliers.exception.SupplierCommunicationException;
import salesmachine.oim.suppliers.exception.SupplierConfigurationException;
import salesmachine.oim.suppliers.exception.SupplierOrderException;
import salesmachine.oim.suppliers.exception.SupplierOrderTrackingException;
import salesmachine.oim.suppliers.modal.OrderDetailResponse;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.oim.suppliers.modal.TrackingData;
import salesmachine.util.ApplicationProperties;
import salesmachine.util.FtpDetail;
import salesmachine.util.OimLogStream;
import salesmachine.util.StringHandle;

public class RSR extends Supplier implements HasTracking {

  private static final Logger log = LoggerFactory.getLogger(RSR.class);
  private static final byte[] NEW_LINE = new byte[] { '\n' };
  private static final byte[] SEMI_COLON = new byte[] { ';' };
  private static final String ASCII = "ASCII";

  private static final SimpleDateFormat df = new SimpleDateFormat("YYYYMMDD");

  @Override
  public void sendOrders(Integer vendorId, OimVendorSuppliers ovs, OimOrders order)
      throws SupplierConfigurationException, SupplierCommunicationException, SupplierOrderException,
      ChannelConfigurationException, ChannelCommunicationException, ChannelOrderFormatException {

    log.info("Sending orders of Account: {}", ovs.getAccountNumber());
    if (ovs.getTestMode().equals(1))
      return;

    Session session = SessionManager.currentSession();
    Query query;
    orderSkuPrefixMap = setSkuPrefixForOrders(ovs);
    int channelId = order.getOimOrderBatches().getOimChannels().getChannelId();
    OimChannels channel = order.getOimOrderBatches().getOimChannels();
    String sequenceNumber = null;
    if (channel.getSequenceNumber() == null) {
      sequenceNumber = "0001";
      channel.setSequenceNumber(sequenceNumber);
    } else {
      sequenceNumber = channel.getSequenceNumber();
      int sequenceNumInt = Integer.parseInt(sequenceNumber);
      sequenceNumInt += sequenceNumInt;
      sequenceNumber = String.format("%04d", sequenceNumInt);
      channel.setSequenceNumber(sequenceNumber);
    }
    Reps r = (Reps) session.createCriteria(Reps.class).add(Restrictions.eq("vendorId", vendorId))
        .uniqueResult();
    Vendors v = new Vendors();
    v.setVendorId(r.getVendorId());

    String poNum;
    query = session.createSQLQuery(
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
    }
    if (q != null) {
      poNum = (String) q;
      log.info("Reprocessing po - {}", poNum);
    } else {
      poNum = ovs.getVendors().getVendorId() + "-" + order.getStoreOrderId();
    }
    try {
      if (order.getOimShippingMethod() == null) {
        log.error("shipping method is missing");
        return;
      }
      OimSupplierShippingMethod oimSupplierShippingMethod = null;
      try {
        oimSupplierShippingMethod = Supplier.findShippingCodeFromUserMapping(
            Supplier.loadSupplierShippingMap(ovs.getOimSuppliers(), v),
            order.getOimShippingMethod());
      } catch (Exception e) {
        e.printStackTrace();
      }
      String shippingCode = oimSupplierShippingMethod.getOverride() == null
          ? oimSupplierShippingMethod.getName()
          : oimSupplierShippingMethod.getOverride().getShippingMethod();

      FtpDetail ftpDetails = getFtpDetails(ovs);
      StringBuffer errorString = new StringBuffer();
      if (!validateOrder(order, poNum, ovs.getAccountNumber(), shippingCode, errorString)) {
        log.error(errorString.toString());
        throw new SupplierOrderException(
            errorString.toString() + " for order id - " + order.getStoreOrderId());
      }
      String fileName = createOrderFile(order, ovs, poNum, shippingCode, sequenceNumber);
      System.out.println(fileName);
      sendToFTP(fileName, ftpDetails);
      sendEmail(ovs.getAccountNumber(), fileName);
      log.info("email sent to supplier with attachment");
      for (OimOrderDetails od : order.getOimOrderDetailses()) {

        successfulOrders.put(od.getDetailId(), new OrderDetailResponse(poNum,
            OimConstants.OIM_SUPPLER_ORDER_STATUS_SENT_TO_SUPPLIER, null));
      }

    } catch (RuntimeException e) {
      log.error(e.getMessage(), e);
    }
  }

  private boolean validateOrder(OimOrders order, String poNum, String accountNum,
      String shippingMethod, StringBuffer errorString) {
    int errorCount = 0;
    if (poNum.length() > 22) {
      errorString.append("PO Number can not be greater than 22 characters. <BR/>");
      errorCount++;
    }
    if (StringHandle.removeNull(accountNum).length() > 5) {
      errorString.append("Account number can not be greater than 5 characters. <BR/>");
      errorCount++;
    }
    if (StringHandle.removeNull(order.getDeliveryName()).length() > 25) {
      errorString.append("Customer name can not be greater than 25 characters. <BR/>");
      errorCount++;
    }
    if (StringHandle.removeNull(order.getDeliveryStreetAddress()).length() > 27) {
      errorString.append("Street address can not be greater than 27 characters. <BR/>");
      errorCount++;
    }
    if (StringHandle.removeNull(order.getDeliverySuburb()).length() > 27) {
      errorString.append("Street address can not be greater than 27 characters. <BR/>");
      errorCount++;
    }
    if (StringHandle.removeNull(order.getDeliveryCity()).length() > 25) {
      errorString.append("City can not be greater than 25 characters. <BR/>");
      errorCount++;
    }
    if (StringHandle.removeNull(order.getDeliveryStateCode()).length() > 2) {
      errorString.append("Statecode not be greater than 2 characters. <BR/>");
      errorCount++;
    }
    if (!StringHandle.isNullOrEmpty(order.getDeliveryStateCode())
        && (order.getDeliveryStateCode().equals("PR") || order.getDeliveryStateCode().equals("GU")
            || order.getDeliveryStateCode().equals("VI"))) {
      errorString.append("RSR doesn't ship to this state. <BR/>");
    }
    if (StringHandle.removeNull(order.getDeliveryZip()).length() > 9) {
      errorString.append("city can not be greater than 2 characters. <BR/>");
      errorCount++;
    }
    if (StringHandle.removeNull(order.getDeliveryPhone()).length() > 10) {
      errorString.append("Phone number can not be greater than 10 characters. <BR/>");
      errorCount++;
    }
    if (!StringHandle.isNullOrEmpty(order.getDeliveryPhone())) {
      if (order.getDeliveryPhone().contains("-") || order.getDeliveryPhone().contains("{")
          || order.getDeliveryPhone().contains("}")) {
        errorString.append("Phone number can not contain '-','{' or '}' <BR/>");
      }
    }
    if (StringHandle.removeNull(order.getDeliveryEmail()).length() > 50) {
      errorString.append("Email can not be greater than 50 characters. <BR/>");
      errorCount++;
    }
    if (StringHandle.removeNull(shippingMethod).length() > 4) {
      errorString.append("Shipping method can not be greater than 4 characters. <BR/>");
      errorCount++;
    }
    if (StringHandle.removeNull(order.getOimShippingMethod().getOimShippingCarrier().getName())
        .length() > 5) {
      errorString.append("Shipping carrier can not be greater than 5 characters. <BR/>");
      errorCount++;
    }
    if (errorCount > 0)
      return false;
    return true;

  }

  private void writeSquenceNumber(int sequenceNumber) {

  }

  private void sendToFTP(String fileName, FtpDetail ftpDetails)
      throws SupplierCommunicationException, SupplierConfigurationException {
    File file = new File(fileName);
    FTPClient ftp = new FTPClient();
    try {
      ftp.setRemoteHost(ftpDetails.getUrl());
      ftp.setDetectTransferMode(false);
      ftp.connect();
      ftp.login(ftpDetails.getUserName(), ftpDetails.getPassword());
      ftp.setType(FTPTransferType.ASCII);
      ftp.setTimeout(60 * 1000 * 60 * 5);
      ftp.put(fileName, file.getName());
      // if (ftp.get(file.getName()) == null) {
      // sendErrorReportEmail(fileName, ftpDetails);
      // }
      ftp.quit();
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      throw new SupplierCommunicationException(
          "Could not connect to FTP while sending orderfile to RSR - " + e.getMessage(), e);
    } catch (FTPException e) {
      log.error(e.getMessage(), e);
      throw new SupplierConfigurationException(
          "Could not connect to FTP while sending orderfile to RSR - " + e.getMessage(), e);
    }

  }

  private void sendEmail(String accountNo, String fileName) {
    if (StringHandle.isNullOrEmpty(accountNo))
      accountNo = "TestAccount";
    String emailBody = "Account Number : " + accountNo
        + "\n Find attached order file for the orders from my store.";
    String emailSubject = fileName;
    EmailUtil.sendEmailWithAttachment("manish@inventorysource.com", "support@inventorysource.com",
        "", emailSubject, emailBody, fileName);

  }

  private FtpDetail getFtpDetails(OimVendorSuppliers ovs) {
    FtpDetail ftpDetail = new FtpDetail();
    // TODO - get configured ftp detail and set to ftpDetail object
    // ftpDetail.setAccountNumber("TestAccount");
    // ftpDetail.setUrl("ftp.rsrgroup.com");
    // ftpDetail.setUserName("99999");
    // ftpDetail.setPassword("Vt1234X");
    return ftpDetail;
  }

  private String createOrderFile(OimOrders order, OimVendorSuppliers ovs, String poNum,
      String shippingMethodCode, String sequenceNumber) {
    String date = df.format(new Date());
    ovs.setAccountNumber("TestAccount");
    String filename = ovs.getAccountNumber() + "-" + date + "-" + sequenceNumber + ".txt";
    File file = new File(filename);
    try {
      file.createNewFile();
      System.out.println(file.getAbsolutePath());
      System.out.println(file.getCanonicalPath());
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    try (FileOutputStream fOut = new FileOutputStream(filename)) {
      // file header start
      fOut.write(getBytes(poNum));
      fOut.write(SEMI_COLON);
      fOut.write(getBytes("00"));
      fOut.write(SEMI_COLON);
      fOut.write(getBytes(ovs.getAccountNumber()));
      fOut.write(SEMI_COLON);
      fOut.write(getBytes(date));
      fOut.write(SEMI_COLON);
      fOut.write(getBytes(sequenceNumber));
      fOut.write(NEW_LINE);
      // file header end
      // order header start
      fOut.write(getBytes(poNum));
      fOut.write(SEMI_COLON);
      fOut.write(getBytes("10"));
      fOut.write(SEMI_COLON);
      fOut.write(getBytes(order.getDeliveryName()));
      fOut.write(SEMI_COLON);
      fOut.write("".getBytes(ASCII));
      fOut.write(SEMI_COLON);
      fOut.write(getBytes(order.getDeliveryStreetAddress()));
      fOut.write(SEMI_COLON);
      fOut.write(getBytes(order.getDeliverySuburb()));
      fOut.write(SEMI_COLON);
      fOut.write(getBytes(order.getDeliveryCity()));
      fOut.write(SEMI_COLON);
      fOut.write(getBytes(order.getDeliveryStateCode()));
      fOut.write(SEMI_COLON);
      fOut.write(getBytes(order.getDeliveryZip()));
      fOut.write(SEMI_COLON);
      if (StringHandle.isNullOrEmpty(order.getDeliveryPhone()))
        fOut.write(getBytes("0"));
      else
        fOut.write(getBytes(order.getDeliveryPhone()));
      fOut.write(SEMI_COLON);
      if (!StringHandle.isNullOrEmpty(order.getDeliveryEmail())) {
        fOut.write(getBytes("Y"));
        fOut.write(SEMI_COLON);
        fOut.write(getBytes(order.getDeliveryEmail()));
        fOut.write(SEMI_COLON);
      } else {
        fOut.write(getBytes("N"));
        fOut.write(SEMI_COLON);
        fOut.write(getBytes(""));
        fOut.write(SEMI_COLON);
      }
      fOut.write(getBytes(""));
      fOut.write(NEW_LINE);
      // order header end
      // FFL DEALER SECTION -- skipping it now. once we have a way to recognize a firearm order,
      // then we will implement it.
      // order detail section started
      int totalQuantity = 0;
      for (OimOrderDetails detail : order.getOimOrderDetailses()) {
        if (!detail.getOimSuppliers().getSupplierId().equals(ovs.getOimSuppliers().getSupplierId()))
          continue;
        totalQuantity += detail.getQuantity();
        String skuPrefix = null, sku = detail.getSku();
        if (!orderSkuPrefixMap.isEmpty()) {
          skuPrefix = orderSkuPrefixMap.values().toArray()[0].toString();
        }
        skuPrefix = StringHandle.removeNull(skuPrefix);
        if (sku.startsWith(skuPrefix))
          sku = sku.substring(skuPrefix.length());
        fOut.write(getBytes(poNum));
        fOut.write(SEMI_COLON);
        fOut.write(getBytes("20"));
        fOut.write(SEMI_COLON);
        fOut.write(getBytes(sku));
        fOut.write(SEMI_COLON);
        fOut.write(getBytes(detail.getQuantity().toString()));
        fOut.write(SEMI_COLON);
        fOut.write(getBytes(order.getOimShippingMethod().getOimShippingCarrier().getName()));
        fOut.write(SEMI_COLON);
        fOut.write(getBytes(shippingMethodCode));
        fOut.write(NEW_LINE);

      }
      // order detail section end
      // order trailer section started
      fOut.write(getBytes(poNum));
      fOut.write(SEMI_COLON);
      fOut.write(getBytes("90"));
      fOut.write(SEMI_COLON);
      fOut.write(getBytes(String.valueOf(totalQuantity)));
      fOut.write(NEW_LINE);
      // order trailer section end
      // file trailer section started
      fOut.write(getBytes(poNum));
      fOut.write(SEMI_COLON);
      fOut.write(getBytes("99"));
      fOut.write(SEMI_COLON);
      fOut.write(getBytes(String.valueOf(order.getOimOrderDetailses().size())));
      fOut.write(NEW_LINE);

      fOut.flush();
      fOut.close();
    } catch (Exception e) {
      // TODO: handle exception
    }
    return filename;
  }

  @Override
  public OrderStatus getOrderStatus(OimVendorSuppliers ovs, Object trackingMeta,
      OimOrderDetails oimOrderDetails) throws SupplierOrderTrackingException {

    orderSkuPrefixMap = setSkuPrefixForOrders(ovs);
    if (!(trackingMeta instanceof String))
      throw new IllegalArgumentException(
          "trackingMeta is expected to be a String value containing PONumber.");
    OrderStatus orderStatus = new OrderStatus();
    orderStatus.setStatus(oimOrderDetails.getSupplierOrderStatus());
    String skuPrefix = null;
    String sku = oimOrderDetails.getSku();
    if (!orderSkuPrefixMap.isEmpty()) {
      int channelId = oimOrderDetails.getOimOrders().getOimOrderBatches().getOimChannels()
          .getChannelId();
      skuPrefix = orderSkuPrefixMap.get(channelId);
    }
    skuPrefix = StringHandle.removeNull(skuPrefix);
    if (sku.startsWith(skuPrefix)) {
      sku = sku.substring(skuPrefix.length());
    }

    String poNumber = (String) trackingMeta; // 641958-100
    FtpDetail ftpDetail = getFtpDetails(ovs);
    if (ftpDetail.getUrl() != null) {
      if (isOrderConfirmed(ftpDetail, ovs.getAccountNumber(), poNumber, oimOrderDetails)) {
        orderStatus.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_IN_PROCESS);
        // get the shipment status
        getTrackingInfo(ftpDetail, orderStatus, poNumber, ovs.getAccountNumber(),
            oimOrderDetails, sku);
      } else {
        // get the failure reason from error file.
        
      }
    }
    return orderStatus;

  }

  // public static void main(String[] args) {
  // RSR rsr = new RSR();
  // Session session = SessionManager.currentSession();
  // OimVendorSuppliers ovs = (OimVendorSuppliers) session.get(OimVendorSuppliers.class, 9961);
  // OimOrders order = (OimOrders) session.get(OimOrders.class, 450567);
  // try {
  // rsr.sendOrders(641958, ovs, order);
  // } catch (SupplierConfigurationException e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // } catch (SupplierCommunicationException e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // } catch (SupplierOrderException e) {
  // System.out.println(e.getMessage());
  // e.printStackTrace();
  // } catch (ChannelConfigurationException e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // } catch (ChannelCommunicationException e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // } catch (ChannelOrderFormatException e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  // }

  private void getTrackingInfo(FtpDetail ftpDetail, OrderStatus orderStatus, String poNumber,
      String accountNumber, OimOrderDetails detail, String sku) {

    FTPClient ftp = new FTPClient();
    try {
      ftp.setRemoteHost(ftpDetail.getUrl());
      ftp.setDetectTransferMode(true);
      ftp.connect();
      ftp.login(ftpDetail.getUserName(), ftpDetail.getPassword());
      ftp.setTimeout(60 * 1000 * 60 * 7);
      FTPFile[] ftpFiles = ftp.dirDetails("outgoing");
      Arrays.sort(ftpFiles, new Comparator<FTPFile>() {
        public int compare(FTPFile f1, FTPFile f2) {
          return f2.lastModified().compareTo(f1.lastModified());
        }
      });
      boolean trackingFound = false;
      for (FTPFile ftpFile : ftpFiles) {
        if(trackingFound)
          break;
        String shippingFile = ftpFile.getName();
        if (shippingFile.equals("..") || shippingFile.equals("."))
          continue;
        if (ftpFile.lastModified().before(detail.getProcessingTm()))
          break;
        if (shippingFile.startsWith("ESHIP") && shippingFile.contains("-" + accountNumber + "-")) {
          log.info("shipping file found for order - {}",poNumber);
          byte[] shippingFileData = ftp.get(shippingFile);
          Map<Integer, String> orderDataMap = parseFileData(shippingFileData);
          String[] lineArray1 = orderDataMap.get(0).split(";");
          if (lineArray1 != null && poNumber.equalsIgnoreCase(lineArray1[0])) {
            String trackingNo = StringHandle.removeNull(lineArray1[3]);
            String shippingMethod = StringHandle.removeNull(lineArray1[9]);
            String carrier = StringHandle.removeNull(lineArray1[8]);
            String shipdateStr = StringHandle.removeNull(lineArray1[5]);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date shipDate1 = df.parse(shipdateStr);
            GregorianCalendar c = new GregorianCalendar();
            c.setTime(shipDate1);
            XMLGregorianCalendar shipDate = null;
            try {
              shipDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
            } catch (DatatypeConfigurationException e) {
              log.error(e.getMessage(), e);
            }
            int qty=0;
            for (Iterator<Integer> itr = orderDataMap.keySet().iterator(); itr.hasNext();) {
              Integer lineNo = itr.next();
              if (lineNo == 0)
                continue;
              String lineItem = orderDataMap.get(lineNo);
              String[] lineArray = lineItem.split(";");
              if (lineArray.length > 3 && lineArray[2].equalsIgnoreCase(sku)) {
                String qtyStr = StringHandle.removeNull(lineArray[4]);
                qty = Integer.parseInt(qtyStr);
                if(qty<detail.getQuantity())
                  orderStatus.setPartialShipped(true);
                else
                  orderStatus.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_SHIPPED);
                TrackingData trackingData = new TrackingData();
                trackingData.setCarrierCode(carrier);
                trackingData.setCarrierName(carrier);
                trackingData.setQuantity(qty);
                trackingData.setShipperTrackingNumber(trackingNo);
                trackingData.setShippingMethod(shippingMethod);
                orderStatus.addTrackingData(trackingData);
                trackingFound=true;
                break;
              }
            }
          }

        }
      }
    } catch (IOException | FTPException | ParseException e) {
      log.error(e.getMessage(), e);
    } finally {
      try {
        ftp.quit();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (FTPException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  private boolean isOrderConfirmed(FtpDetail ftpDetail, String accountNumber, String poNumber,
      OimOrderDetails detail) {
    FTPClient ftp = new FTPClient();
    try {
      ftp.setRemoteHost(ftpDetail.getUrl());
      ftp.setDetectTransferMode(true);
      ftp.connect();
      ftp.login(ftpDetail.getUserName(), ftpDetail.getPassword());
      ftp.setTimeout(60 * 1000 * 60 * 7);
      FTPFile[] ftpFiles = ftp.dirDetails("outgoing");
      Arrays.sort(ftpFiles, new Comparator<FTPFile>() {
        public int compare(FTPFile f1, FTPFile f2) {
          return f2.lastModified().compareTo(f1.lastModified());
        }
      });
      for (FTPFile ftpFile : ftpFiles) {
        String confirmationFile = ftpFile.getName();
        if (confirmationFile.equals("..") || confirmationFile.equals("."))
          continue;
        if (ftpFile.lastModified().before(detail.getProcessingTm()))
          break;
        if (confirmationFile.startsWith("ECONF")
            && confirmationFile.contains("-" + accountNumber + "-")) {
          byte[] confirmationFileData = ftp.get(confirmationFile);
          Map<Integer, String> orderDataMap = parseFileData(confirmationFileData);
          String[] lineArray1 = orderDataMap.get(0).split(";");
          if (lineArray1 != null && poNumber.equalsIgnoreCase(lineArray1[0]))
            return true;

        }
      }
    } catch (IOException | FTPException | ParseException e) {
      log.error(e.getMessage(), e);
      return false;
    } finally {
      try {
        ftp.quit();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (FTPException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return false;
  }

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

  private byte[] getBytes(String str) throws UnsupportedEncodingException {
    return StringHandle.removeComma(StringHandle.removeNull(str)).getBytes(ASCII);
  }

}
