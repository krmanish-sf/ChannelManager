package salesmachine.oim.suppliers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderProcessingRule;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSupplierMethods;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.Reps;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.PojoHelper;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.stores.impl.ChannelFactory;
import salesmachine.oim.suppliers.exception.SupplierConfigurationException;
import salesmachine.oim.suppliers.exception.SupplierOrderException;
import salesmachine.oim.suppliers.modal.OrderDetailResponse;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.util.MotengContryCode;
import salesmachine.util.OimLogStream;
import salesmachine.util.StringHandle;

public class CustomSupplier extends Supplier {

  private static final Logger log = LoggerFactory.getLogger(Moteng.class);
  private static final byte[] NEW_LINE = new byte[] { '\n' };
  private static final byte[] TAB = new byte[] { '\t' };
  private static final String ASCII = "ASCII";
  private static final byte[] COMMA = new byte[] { ',' };

  public void sendOrders(Integer vendorId, OimVendorSuppliers ovs, OimOrders oimOrder)
      throws SupplierConfigurationException, SupplierOrderException, ChannelCommunicationException,
      ChannelOrderFormatException {

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
    query.setInteger("orderId", oimOrder.getOrderId());
    query.setInteger("supplierId", ovs.getOimSuppliers().getSupplierId());
    Object q = null;
    try {
      q = query.uniqueResult();
    } catch (NonUniqueResultException e) {
      log.error(
          "This order has more than one product having different PO number. Please make them unique. store order id is - {}",
          oimOrder.getStoreOrderId());
      throw new SupplierConfigurationException(
          "This order has more than one product having different PO number. Please make them unique.");
    }
    if (q != null) {
      poNum = (String) q;
      log.info("Reprocessing po - {}", poNum);
    } else {
      poNum = ovs.getVendors().getVendorId() + "-" + oimOrder.getStoreOrderId();
    }
    try {
      if (oimOrder.getOimShippingMethod() == null) {
        log.error("shipping method is missing");
        return;
      }

      query = session.createQuery(
          "from salesmachine.hibernatedb.OimSupplierMethods os where os.vendor.vendorId=:vid and os.oimSuppliers.supplierId=:sid and os.deleteTm is null");
      query.setInteger("vid", ovs.getVendors().getVendorId());
      query.setInteger("sid", ovs.getOimSuppliers().getSupplierId());
      OimSupplierMethods supplierMethod = (OimSupplierMethods) query.uniqueResult();
      String shippingCode = ovs.getDefShippingMethodCode();
      String tmp = PojoHelper.getSupplierMethodAttributeValue(supplierMethod,
          OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FILEFORMAT);
      String fileName = null;
      if (tmp.equals("1"))
        fileName = createOrderFile(oimOrder, ovs, poNum, shippingCode, COMMA);
      else if (tmp.equals("2"))
        fileName = createOrderFile(oimOrder, ovs, poNum, shippingCode, TAB);

      if (supplierMethod.getOimSupplierMethodNames().getMethodNameId()
          .intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_EMAILADDRESS) {
        String emailAddress = PojoHelper.getSupplierMethodAttributeValue(supplierMethod,
            OimConstants.SUPPLIER_METHOD_ATTRIBUTES_EMAILADDRESS);
        sendEmailToSupplier(emailAddress, fileName, ovs.getAccountNumber());
        sendEmailToSupplier("orders@inventorysource.com", fileName, ovs.getAccountNumber());
      }
      log.info("email sent to supplier with attachment");
      for (OimOrderDetails od : oimOrder.getOimOrderDetailses()) {

        successfulOrders.put(od.getDetailId(), new OrderDetailResponse(poNum,
            OimConstants.OIM_SUPPLER_ORDER_STATUS_SENT_TO_SUPPLIER, null));
      }

    } catch (RuntimeException e) {
      log.error(e.getMessage(), e);
    }

  }

  private void sendEmailToSupplier(String emailAddress, String fileName, String accountNo) {
    String emailBody = accountNo;
    String emailSubject = accountNo;
    EmailUtil.sendEmailWithAttachment(emailAddress, "support@inventorysource.com", "", emailSubject,
        emailBody, fileName);

  }

  private String createOrderFile(OimOrders order, OimVendorSuppliers ovs, String poNum,
      String shippingCode, byte[] seperater) throws SupplierOrderException,
          ChannelCommunicationException, ChannelOrderFormatException {

    String uploadfilename = "/tmp/" + ovs.getOimSuppliers().getSupplierName() + "_"
        + new Random().nextLong() + ".txt";
    File f = new File(uploadfilename);
    log.info("created file name for Moteng:{}", f.getName());
    try {
      order.setDeliveryCountryCode(MotengContryCode.getProperty(order.getDeliveryCountry()));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new SupplierOrderException(e.getMessage(), e);
    }
    try (FileOutputStream fOut = new FileOutputStream(f)) {
      for (OimOrderDetails od : order.getOimOrderDetailses()) {
        if (!od.getOimSuppliers().getSupplierId().equals(ovs.getOimSuppliers().getSupplierId()))
          continue;
        fOut.write(shippingCode.getBytes(ASCII));
        fOut.write(seperater);
        fOut.write(StringHandle.removeNull(poNum).getBytes(ASCII));
        fOut.write(seperater);
        fOut.write(StringHandle.removeNull(order.getDeliveryCompany()).getBytes(ASCII));
        fOut.write(seperater);
        fOut.write(StringHandle.removeNull(order.getDeliveryName()).getBytes(ASCII));
        fOut.write(seperater);
        fOut.write(StringHandle.removeNull(order.getDeliveryStreetAddress()).getBytes(ASCII));
        fOut.write(seperater);
        fOut.write(StringHandle.removeNull(order.getDeliverySuburb()).getBytes(ASCII));
        fOut.write(seperater);
        fOut.write(StringHandle.removeNull(order.getDeliveryCity()).getBytes(ASCII));
        fOut.write(seperater);

        fOut.write(StringHandle.removeNull(order.getDeliveryStateCode()).getBytes(ASCII));
        fOut.write(seperater);
        fOut.write(StringHandle.removeNull(order.getDeliveryZip()).getBytes(ASCII));
        fOut.write(seperater);
        fOut.write(StringHandle.removeNull(order.getDeliveryCountryCode()).getBytes(ASCII));
        fOut.write(seperater);
        String skuPrefix = null, sku = od.getSku();
        if (!orderSkuPrefixMap.isEmpty()) {
          skuPrefix = orderSkuPrefixMap.values().toArray()[0].toString();
        }
        skuPrefix = StringHandle.removeNull(skuPrefix);
        if (sku.startsWith(skuPrefix)) {
          sku = sku.substring(skuPrefix.length());
        }
        fOut.write(sku.getBytes(ASCII));
        fOut.write(seperater);
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

}
