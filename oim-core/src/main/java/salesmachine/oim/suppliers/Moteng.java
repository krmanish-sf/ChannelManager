package salesmachine.oim.suppliers;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.sftp.SftpFile;
import com.sshtools.j2ssh.sftp.SftpFileInputStream;
import com.sshtools.j2ssh.sftp.SftpSubsystemClient;
import com.sshtools.j2ssh.transport.HostKeyVerification;
import com.sshtools.j2ssh.transport.TransportProtocolException;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderProcessingRule;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSupplierMethodattrValues;
import salesmachine.hibernatedb.OimSupplierMethods;
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
import salesmachine.util.FtpDetail;
import salesmachine.util.MotengContryCode;
import salesmachine.util.OimLogStream;
import salesmachine.util.StringHandle;

public class Moteng extends Supplier implements HasTracking {

  private static final Logger log = LoggerFactory.getLogger(Moteng.class);
  private static final byte[] NEW_LINE = new byte[] { '\n' };
  private static final byte[] TAB = new byte[] { '\t' };
  private static final String ASCII = "ASCII";
  DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 2015-11-03 12:10:55

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
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      String shippingCode = oimSupplierShippingMethod.getOverride() == null
          ? oimSupplierShippingMethod.getName()
          : oimSupplierShippingMethod.getOverride().getShippingMethod();

      String fileName = createOrderFile(order, ovs, poNum, shippingCode);
      sendEmailToSupplier(ovs.getAccountNumber(), fileName);
      sendEmailToSupport(ovs.getAccountNumber(), fileName);
      log.info("email sent to supplier with attachment");
      for (OimOrderDetails od : order.getOimOrderDetailses()) {

        successfulOrders.put(od.getDetailId(), new OrderDetailResponse(poNum,
            OimConstants.OIM_SUPPLER_ORDER_STATUS_SENT_TO_SUPPLIER, null));
      }

    } catch (RuntimeException e) {
      log.error(e.getMessage(), e);
    }
  }

  private String createOrderFile(OimOrders order, OimVendorSuppliers ovs, String poNum,
      String shippingCode) throws ChannelCommunicationException, ChannelOrderFormatException,
          SupplierOrderException {

    String uploadfilename = "/tmp/" + "MO_" + ovs.getAccountNumber() + "_" + new Random().nextLong()
        + ".txt";
    File f = new File(uploadfilename);
    log.info("created file name for Moteng:{}", f.getName());
    try {
      order.setDeliveryCountryCode(MotengContryCode.getProperty(order.getDeliveryCountry()));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new SupplierOrderException(e.getMessage(), e);
    }
    try (FileOutputStream fOut = new FileOutputStream(f)) {

      // String fields[] = { "ShipVia", "PO", "Company", "Name", "Address1", "Address2", "City",
      // "State",
      // "Zip", "Country", "SKU", "Qty", };
      for (OimOrderDetails od : order.getOimOrderDetailses()) {
        if (!od.getOimSuppliers().getSupplierId().equals(ovs.getOimSuppliers().getSupplierId()))
          continue;
        fOut.write(shippingCode.getBytes(ASCII));
        fOut.write(TAB);
        fOut.write(StringHandle.removeNull(poNum).getBytes(ASCII));
        fOut.write(TAB);
        fOut.write(StringHandle.removeNull(order.getDeliveryCompany()).getBytes(ASCII));
        fOut.write(TAB);
        fOut.write(StringHandle.removeNull(order.getDeliveryName()).getBytes(ASCII));
        fOut.write(TAB);
        fOut.write(StringHandle.removeNull(order.getDeliveryStreetAddress()).getBytes(ASCII));
        fOut.write(TAB);
        fOut.write(StringHandle.removeNull(order.getDeliverySuburb()).getBytes(ASCII));
        fOut.write(TAB);
        fOut.write(StringHandle.removeNull(order.getDeliveryCity()).getBytes(ASCII));
        fOut.write(TAB);

        fOut.write(StringHandle.removeNull(order.getDeliveryStateCode()).getBytes(ASCII));
        fOut.write(TAB);
        fOut.write(StringHandle.removeNull(order.getDeliveryZip()).getBytes(ASCII));
        fOut.write(TAB);
        fOut.write(StringHandle.removeNull(order.getDeliveryCountryCode()).getBytes(ASCII));
        fOut.write(TAB);
        String skuPrefix = null, sku = od.getSku();
        if (!orderSkuPrefixMap.isEmpty()) {
          skuPrefix = orderSkuPrefixMap.values().toArray()[0].toString();
        }
        skuPrefix = StringHandle.removeNull(skuPrefix);
        if (sku.startsWith(skuPrefix)) {
          sku = sku.substring(skuPrefix.length());
        }
        fOut.write(sku.getBytes(ASCII));
        fOut.write(TAB);
        fOut.write(StringHandle.removeNull(od.getQuantity().toString()).getBytes(ASCII));
        fOut.write(NEW_LINE);
        OimChannels oimChannels = order.getOimOrderBatches().getOimChannels();
        OimLogStream stream = new OimLogStream();

        try {
          IOrderImport iOrderImport = ChannelFactory.getIOrderImport(oimChannels);
          OrderStatus orderStatus = new OrderStatus();
          orderStatus.setStatus(
              ((OimOrderProcessingRule) oimChannels.getOimOrderProcessingRules().iterator().next())
                  .getProcessedStatus());
          if (oimChannels.getTestMode() == 0)
            iOrderImport.updateStoreOrder(od, orderStatus);

        } catch (ChannelConfigurationException e) {
          log.error(e.getMessage(), e);
          stream.println(e.getMessage());
        }
      }

      fOut.flush();
      fOut.close();
    } catch (FileNotFoundException e) {
      log.error(e.getMessage(), e);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    return uploadfilename;
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

      if (ftpDetail.getFtpType() != null && ftpDetail.getFtpType().equalsIgnoreCase("SFTP"))
        orderStatus = getOrderStatusFromSFTP(orderStatus, ftpDetail, oimOrderDetails, poNumber,
            sku);
      else
        orderStatus = getOrderStatusFromFTP(orderStatus, ftpDetail, oimOrderDetails, poNumber, sku);
    }
    return orderStatus;
  }

  private OrderStatus getOrderStatusFromFTP(OrderStatus orderStatus, FtpDetail ftpDetail,
      OimOrderDetails oimOrderDetails, String poNumber, String sku)
          throws SupplierOrderTrackingException {
    FTPClient ftp = new FTPClient();
    try {
      ftp.setRemoteHost(ftpDetail.getUrl());
      ftp.setDetectTransferMode(true);
      ftp.connect();
      ftp.login(ftpDetail.getUserName(), ftpDetail.getPassword());
      ftp.setTimeout(60 * 1000 * 60 * 7);
      FTPFile[] ftpFiles = ftp.dirDetails("/");
      Arrays.sort(ftpFiles, new Comparator<FTPFile>() {
        public int compare(FTPFile f1, FTPFile f2) {
          return f2.lastModified().compareTo(f1.lastModified());
        }
      });
      for (FTPFile ftpFile : ftpFiles) {
        String trackingFile = ftpFile.getName();
        if (!trackingFile.equals("ord.txt"))
          continue;
        byte[] trackingFileData = ftp.get("/" + trackingFile);
        Map<Integer, String> orderDataMap = parseFileData(trackingFileData);
        for (Iterator itr = orderDataMap.values().iterator(); itr.hasNext();) {
          String line = (String) itr.next();
          String[] lineArray = line.split(",");
          String shippingMethod;
          String shipDateString;
          int qty;
          String trackingNo;
          String headerStatus;
          try {
            String trackingPO = StringHandle.removeNull(lineArray[6]);
            String trackingSku = StringHandle.removeNull(lineArray[8]);
            if (!trackingPO.equals(poNumber) || !trackingSku.equalsIgnoreCase(sku))
              continue;
            shippingMethod = StringHandle.removeNull(lineArray[3]);
            String qtyOrdered = StringHandle.removeNull(lineArray[9]);
            String qtyShipped = StringHandle.removeNull(lineArray[10]);
            shipDateString = StringHandle.removeNull(lineArray[11]);
            qty = 0;
            try {
              qty = Integer.parseInt(qtyShipped);
            } catch (NumberFormatException e) {
              log.error(e.getMessage(), e);
            }

            trackingNo = StringHandle.removeNull(lineArray[13]);
            headerStatus = StringHandle.removeNull(lineArray[15]);
          } catch (ArrayIndexOutOfBoundsException e1) {
            log.error("This file is not appropreate as the documantation of Moteng");
            break;
          }
          if (headerStatus.equalsIgnoreCase("O")) {
            log.info("This is an open order pending shipment from Moteng warehouse.");
            orderStatus.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_IN_PROCESS);
            return orderStatus;
          }
          if (headerStatus.equalsIgnoreCase("P")) {
            orderStatus.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_IN_PROCESS);
            orderStatus.setPartialShipped(true);
          } else if (orderStatus.getStatus() == null) {
            orderStatus.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_SHIPPED);
          } else if (headerStatus.equalsIgnoreCase("C")) {
            orderStatus.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_SHIPPED);
            orderStatus.setPartialShipped(false);
          }
          salesmachine.oim.suppliers.modal.TrackingData trackingData = new salesmachine.oim.suppliers.modal.TrackingData();
          trackingData.setCarrierCode(oimOrderDetails.getOimOrders().getOimShippingMethod()
              .getOimShippingCarrier().getName());
          trackingData.setCarrierName(oimOrderDetails.getOimOrders().getOimShippingMethod()
              .getOimShippingCarrier().getName());
          trackingData.setQuantity(qty);
          trackingData.setShipperTrackingNumber(trackingNo);
          Date shipDate1 = df.parse(shipDateString);
          GregorianCalendar c = new GregorianCalendar();
          c.setTime(shipDate1);
          XMLGregorianCalendar shipDate = null;
          try {
            shipDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
          } catch (DatatypeConfigurationException e) {
            log.error(e.getMessage(), e);
          }
          if (shipDate != null)
            trackingData.setShipDate(shipDate);
          trackingData.setShippingMethod(shippingMethod);
          orderStatus.addTrackingData(trackingData);
        }
      }

    } catch (IOException | FTPException | ParseException e) {
      log.error(e.getMessage(), e);
      throw new SupplierOrderTrackingException("Unable to connect to supplier ftp", e);
    }

    return orderStatus;
  }

  private OrderStatus getOrderStatusFromSFTP(OrderStatus orderStatus, FtpDetail ftpDetail,
      OimOrderDetails oimOrderDetails, String poNumber, String sku)
          throws SupplierOrderTrackingException {

    try {

      SshClient con = new SshClient();
      String SFTPURL = ftpDetail.getUrl();
      System.out.println("Connecting to " + SFTPURL);
      int result = 0;
      try {
        con.connect(SFTPURL, 22, new HostKeyVerification() {
          public boolean verifyHost(String host, SshPublicKey pk)
              throws TransportProtocolException {
            return true;
          }
        });

        System.out.println("SFTP connected :");
        System.out.println("Going to authenticate Password and User Name :");
        com.sshtools.j2ssh.authentication.SshAuthenticationClient authClient = getAuthenticationClient(
            ftpDetail.getUserName(), ftpDetail.getPassword());
        result = con.authenticate(authClient);
        if (result != AuthenticationProtocolState.COMPLETE) {
          System.out.println("Login failed.");
        } else {
          System.out.println("Login successful.");
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      SessionChannelClient session = con.openSessionChannel();
      SftpSubsystemClient sftp = new SftpSubsystemClient();
      session.startSubsystem(sftp);
      SftpFile roortDir = sftp.openDirectory("/");// ("/", SftpSubsystemClient.OPEN_READ);
      List l = new ArrayList();
      sftp.listChildren(roortDir, l);
      for (Iterator itr1 = l.iterator(); itr1.hasNext();) {
        SftpFile sftpFile = (SftpFile) itr1.next();
        String trackingFileName = sftpFile.getFilename();
        System.out.println(trackingFileName);
        if (!trackingFileName.equals("ord.txt"))
          continue;
          BufferedInputStream in = new BufferedInputStream(new SftpFileInputStream(sftpFile));
          byte[] trackingFileData = new byte[in.available()];
          Map<Integer, String> orderDataMap = parseFileData(trackingFileData);
          for (Iterator itr = orderDataMap.values().iterator(); itr.hasNext();) {
            String line = (String) itr.next();
            String[] lineArray = line.split("\t");
            String shippingMethod;
            String shipDateString;
            int qty;
            String trackingNo;
            String headerStatus;
            try {
              String trackingPO = StringHandle.removeNull(lineArray[6]);
              String trackingSku = StringHandle.removeNull(lineArray[8]);
              if (!trackingPO.equals(poNumber) || !trackingSku.equalsIgnoreCase(sku))
                continue;
              shippingMethod = StringHandle.removeNull(lineArray[3]);
              String qtyOrdered = StringHandle.removeNull(lineArray[9]);
              String qtyShipped = StringHandle.removeNull(lineArray[10]);
              shipDateString = StringHandle.removeNull(lineArray[11]);
              qty = 0;
              try {
                qty = Integer.parseInt(qtyShipped);
              } catch (NumberFormatException e) {
                log.error(e.getMessage(), e);
              }

              trackingNo = StringHandle.removeNull(lineArray[13]);
              headerStatus = StringHandle.removeNull(lineArray[15]);
            } catch (ArrayIndexOutOfBoundsException e1) {
              log.error("This file is not appropreate as the documantation of Moteng");
              break;
            }
            if (headerStatus.equalsIgnoreCase("O")) {
              log.info("This is an open order pending shipment from Moteng warehouse.");
              return orderStatus;
            }
            if (headerStatus.equalsIgnoreCase("P")) {
              orderStatus.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_IN_PROCESS);
              orderStatus.setPartialShipped(true);
            } else if (orderStatus.getStatus() == null) {
              orderStatus.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_SHIPPED);
            } else if (headerStatus.equalsIgnoreCase("C")) {
              orderStatus.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_SHIPPED);
              orderStatus.setPartialShipped(false);
            }
            salesmachine.oim.suppliers.modal.TrackingData trackingData = new salesmachine.oim.suppliers.modal.TrackingData();
            trackingData.setCarrierCode(oimOrderDetails.getOimOrders().getOimShippingMethod()
                .getOimShippingCarrier().getName());
            trackingData.setCarrierName(oimOrderDetails.getOimOrders().getOimShippingMethod()
                .getOimShippingCarrier().getName());
            trackingData.setQuantity(qty);
            trackingData.setShipperTrackingNumber(trackingNo);
            Date shipDate1 = df.parse(shipDateString);
            GregorianCalendar c = new GregorianCalendar();
            c.setTime(shipDate1);
            XMLGregorianCalendar shipDate = null;
            try {
              shipDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
            } catch (DatatypeConfigurationException e) {
              log.error(e.getMessage(), e);
            }
            if (shipDate != null)
              trackingData.setShipDate(shipDate);
            trackingData.setShippingMethod(shippingMethod);
            orderStatus.addTrackingData(trackingData);
          }

      }

    } catch (IOException | ParseException e) {
      e.printStackTrace();
    }

    return orderStatus;
  }

  private static com.sshtools.j2ssh.authentication.SshAuthenticationClient getAuthenticationClient(
      String user, String password) throws Exception {
    com.sshtools.j2ssh.authentication.SshAuthenticationClient authClient = null;
    PasswordAuthenticationClient pac = new PasswordAuthenticationClient();
    pac.setUsername(user);
    pac.setPassword(password);
    authClient = pac;
    return authClient;
  }

  private FtpDetail getFtpDetails(OimVendorSuppliers ovs) {
    FtpDetail ftpDetail = new FtpDetail();
    for (Iterator<OimSupplierMethods> itr = ovs.getOimSuppliers().getOimSupplierMethodses()
        .iterator(); itr.hasNext();) {
      OimSupplierMethods oimSupplierMethods = itr.next();
      if (oimSupplierMethods.getDeleteTm() != null)
        continue;
      if (oimSupplierMethods.getOimSupplierMethodTypes().getMethodTypeId().intValue() == 1) {

        if (oimSupplierMethods.getVendor() != null && oimSupplierMethods.getVendor().getVendorId()
            .intValue() == ovs.getVendors().getVendorId().intValue()) {
          for (Iterator<OimSupplierMethodattrValues> iterator = oimSupplierMethods
              .getOimSupplierMethodattrValueses().iterator(); iterator.hasNext();) {
            OimSupplierMethodattrValues oimSupplierMethodattrValues = iterator.next();

            if (oimSupplierMethodattrValues.getOimSupplierMethodattrNames().getAttrId()
                .intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPACCOUNT)
              ftpDetail.setAccountNumber(oimSupplierMethodattrValues.getAttributeValue());
            if (oimSupplierMethodattrValues.getOimSupplierMethodattrNames().getAttrId()
                .intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPSERVER)
              ftpDetail.setUrl(oimSupplierMethodattrValues.getAttributeValue());
            if (oimSupplierMethodattrValues.getOimSupplierMethodattrNames().getAttrId()
                .intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPLOGIN)
              ftpDetail.setUserName(oimSupplierMethodattrValues.getAttributeValue());
            if (oimSupplierMethodattrValues.getOimSupplierMethodattrNames().getAttrId()
                .intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPPASSWORD)
              ftpDetail.setPassword(oimSupplierMethodattrValues.getAttributeValue());
            if (oimSupplierMethodattrValues.getOimSupplierMethodattrNames().getAttrId()
                .intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPTYPE)
              ftpDetail.setFtpType(oimSupplierMethodattrValues.getAttributeValue());
          }
          break;
        }
      }
    }
    return ftpDetail;
  }

  private static Map<Integer, String> parseFileData(byte[] trackingFileData) {
    Map<Integer, String> fileData = null;
    try {
      fileData = new HashMap<Integer, String>();
      InputStream inputStream = new ByteArrayInputStream(trackingFileData);
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

  // private String replaceWithTabIfNullOrBlank(String str) {
  // str = StringHandle.removeNull(str);
  // if ("".equals(str))
  // return new String(TAB);
  // return str;
  // }

  private void sendEmailToSupplier(String accountNumber, String fileName) {
    String emailBody = accountNumber;
    String emailSubject = accountNumber;
    EmailUtil.sendEmailWithAttachment("zapship2lineorders@moteng.com",
        "support@inventorysource.com", "gsmith@moteng.com", emailSubject, emailBody, fileName);

  }

  // sendEmailToSupport
  private void sendEmailToSupport(String accountNumber, String fileName) {
    String emailBody = accountNumber;
    String emailSubject = accountNumber;
    EmailUtil.sendEmailWithAttachment("orders@inventorysource.com", "support@inventorysource.com",
        "", emailSubject, emailBody, fileName);

  }

}
