package salesmachine.oim.suppliers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enterprisedt.net.ftp.FTPClient;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimFields;
import salesmachine.hibernatedb.OimFileFieldMap;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderProcessingRule;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.Reps;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.stores.impl.ChannelFactory;
import salesmachine.oim.suppliers.exception.SupplierCommunicationException;
import salesmachine.oim.suppliers.exception.SupplierConfigurationException;
import salesmachine.oim.suppliers.exception.SupplierOrderException;
import salesmachine.oim.suppliers.modal.OrderDetailResponse;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.util.OimLogStream;
import salesmachine.util.StringHandle;

public class GreenSupply extends Supplier {
  private static final Logger log = LoggerFactory.getLogger(GreenSupply.class);
  private static final String ORDERHEADER = "PO Number,ShipName,ShipAddr1,ShipAddr2,ShipCity,ShipState,ShipZip,ShipZip4,RushPack,Fulfillment,SKURcrd,Quantity,ShipMethod";
  private static final String NEWLINE = "\n";
  private static final String COMMA = ",";

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
    String accountNumber = ovs.getAccountNumber();
    // Transaction tx = session.beginTransaction();
    try {
      String fileName = createOrderFile(order, ovs, getFileFieldMap(),
          new StandardFileSpecificsProvider(session, ovs, v));
      System.out.println(fileName);
      FTPClient ftp = new FTPClient();
      ftp.setRemoteHost("datacenter.greensupply.com");
      ftp.setDetectTransferMode(true);
      ftp.connect();
      log.info("ovs.getLogin() : {}", ovs.getLogin());
      log.info("ovs.getPassword() : {}", ovs.getPassword());
      ftp.login(ovs.getLogin(), ovs.getPassword());
      ftp.setTimeout(60 * 1000 * 60 * 5);
      ftp.chdir("orders/");
      ftp.put(fileName, fileName);
      ftp.quit();
      // tx.commit();
      String emailBody = "Account Number : " + accountNumber
          + "\n Find attached order file for the orders from my store.";
      String emailSubject = fileName;
      EmailUtil.sendEmailWithAttachment("nilesh@inventorysource.com", "support@inventorysource.com",
          "aruppar@inventorysource.com", emailSubject, emailBody, fileName);
    } catch (Exception e) {
      // tx.rollback();
      log.error(e.getMessage(), e);
    }

    boolean emailNotification = false;

    // for (Object object : orders) {
    if (order.getOimOrderBatches().getOimChannels().getEmailNotifications() == 1) {
      emailNotification = true;
      String orderStatus = "Successfully Placed";
      emailContent += "<b>Store Order ID " + order.getStoreOrderId() + "</b> -> " + orderStatus
          + " ";
      emailContent += "<br>";
    }

    if (emailNotification) {
      emailContent += "<br>Thanks, <br>Inventorysource support<br>";
      logStream.println("Sending email to user about order processing");
      log.debug("Sending email to user about order processing");
      EmailUtil.sendEmail(r.getLogin(), "support@inventorysource.com", "",
          "Order processing update results", emailContent, "text/html");
    }
  }

  private List<OimFileFieldMap> getFileFieldMap() {
    List<OimFileFieldMap> fileFieldMaps = new ArrayList<OimFileFieldMap>();
    // For blank headers, header values will be append to next header value
    // which is not blank.
    // In this case headers after "Description" are all blank so they will
    // append in header "Address"
    String fields[] = ORDERHEADER.split(",");

    // Integer mappedFieldIds[] = { 2, 3, 4, 12, 5, 6, 7, 0, 0, 0, 1, 9, 10
    // };
    Integer mappedFieldIds[] = { 2, 3, 4, 12, 5, 36, 7, 0, 0, 0, 1, 9, 10 };

    for (int i = 0; i < fields.length; i++) {
      OimFields field = new OimFields(fields[i], fields[i], new Date(), null, null);
      field.setFieldId(mappedFieldIds[i]);
      OimFileFieldMap ffm = new OimFileFieldMap(null, field, fields[i], new Date(), null, "", "");
      fileFieldMaps.add(ffm);
    }
    return fileFieldMaps;
  }

  private String createOrderFile(OimOrders order, OimVendorSuppliers ovs,
      List<OimFileFieldMap> fileFieldMaps, IFileSpecificsProvider fileSpecifics)
          throws SupplierConfigurationException, SupplierCommunicationException,
          SupplierOrderException, ChannelCommunicationException, ChannelOrderFormatException {

    SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
    String uploadfilename = "GS_" + ovs.getAccountNumber() + "_" + sdf.format(new Date()) + ".csv";
    try (Writer fw = new FileWriter(uploadfilename)) {
      fw.write(ORDERHEADER);
      fw.write(NEWLINE);
      Session session = SessionManager.currentSession();
      // for (Object object : orders) {

      for (OimOrderDetails od : ((Set<OimOrderDetails>) order.getOimOrderDetailses())) {
        StringBuilder sb = new StringBuilder();
        // for (OimFileFieldMap map : fileFieldMaps) {
        if (!od.getOimSuppliers().getSupplierId().equals(ovs.getOimSuppliers().getSupplierId()))
          continue;
        String poNumber = null;
        for (int i = 0; i < fileFieldMaps.size(); i++) {
          OimFileFieldMap map = fileFieldMaps.get(i);
          String mappedFieldName = StringHandle.removeNull(map.getMappedFieldName());
          String fieldValue = "";
          if (mappedFieldName.equals("RushPack")) {
            fieldValue = "N";
          } else if (mappedFieldName.equals("Fulfillment")) {
            fieldValue = "Y";
          } else {
            fieldValue = StringHandle.removeNull(fileSpecifics.getFieldValueFromOrder(od, map));
          }
          sb.append(fieldValue.replaceAll(",", " "));
          if (mappedFieldName.equalsIgnoreCase("PO Number")) {
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
            }
            if (q != null) {
              poNumber = (String) q;
              log.info("Reprocessing PO NUmber - {}", poNumber);
            }
            else{
              poNumber=StringHandle.removeNull(fieldValue);
            }
           // poNumber = fieldValue;
          }
          if (i == fileFieldMaps.size() - 1) {
            sb.append(NEWLINE);
          } else {
            sb.append(COMMA);
          }
        }
        fw.write(sb.toString());
        // od.setSupplierOrderStatus("Sent to supplier.");
        // session.update(od);
        successfulOrders.put(od.getDetailId(),
            new OrderDetailResponse(poNumber, "Sent to supplier.", null));
        OimChannels oimChannels = order.getOimOrderBatches().getOimChannels();
        OimLogStream stream = new OimLogStream();
        try {
          {
            IOrderImport iOrderImport = ChannelFactory.getIOrderImport(oimChannels);
            OrderStatus orderStatus = new OrderStatus();
            orderStatus.setStatus(((OimOrderProcessingRule) oimChannels.getOimOrderProcessingRules()
                .iterator().next()).getProcessedStatus());
            if (oimChannels.getTestMode() == 0)
              iOrderImport.updateStoreOrder(od, orderStatus);
          }
        } catch (ChannelConfigurationException e) {
          stream.println(e.getMessage());
        }
      }
      // }
      // }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return uploadfilename;
  }
}
