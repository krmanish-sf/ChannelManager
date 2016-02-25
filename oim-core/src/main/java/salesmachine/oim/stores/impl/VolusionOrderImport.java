package salesmachine.oim.stores.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimChannelShippingMap;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderBatchesTypes;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderStatuses;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSuppliers;
import salesmachine.hibernatehelper.PojoHelper;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.ChannelBase;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.stores.modal.volusion.order.Xmldata;
import salesmachine.oim.stores.modal.volusion.order.Xmldata.Orders;
import salesmachine.oim.stores.modal.volusion.order.Xmldata.Orders.OrderDetails;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.oim.suppliers.modal.el.XMLInputOrder;
import salesmachine.oim.suppliers.modal.el.XMLOrders;
import salesmachine.oim.suppliers.modal.el.tracking.ELTrackRequest;
import salesmachine.util.StringHandle;

public class VolusionOrderImport extends ChannelBase implements IOrderImport {

  private static final Logger log = LoggerFactory.getLogger(VolusionOrderImport.class);
  private String storeUrl;
  private String login;
  private String encriptedPassword;

  private static JAXBContext jaxbContext;

  static {
    try {
      jaxbContext = JAXBContext.newInstance(Xmldata.class);
    } catch (JAXBException e) {
      log.error("Error in initializing XML parsing context.");
    }
  }

  @Override
  public boolean init(OimChannels oimChannel, Session dbSession)
      throws ChannelConfigurationException {
    super.init(oimChannel, dbSession);
    storeUrl = null; // will read from setup TODO
    login = null; // TODO
    encriptedPassword = null; // TODO
    // if (storeUrl.length() == 0 || shopifyToken.length() == 0) {
    // log.error("Channel setup is not correct. Please provide correct details.");
    // throw new ChannelConfigurationException(
    // "Channel setup is not correct. Please provide correct details.");
    // }
    return true;

  }

  public static void main(String[] args) {
    try {
      new VolusionOrderImport().getVendorOrders(null, null);
    } catch (ChannelCommunicationException | ChannelOrderFormatException
        | ChannelConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void getVendorOrders(OimOrderBatchesTypes batchesTypes, OimOrderBatches batch)
      throws ChannelCommunicationException, ChannelOrderFormatException,
      ChannelConfigurationException {
    Transaction tx=null;
//    Transaction tx = m_dbSession.getTransaction();
//    if (tx != null && tx.isActive())
//      tx.commit();
//    tx = m_dbSession.beginTransaction();
//    batch.setOimChannels(m_channel);
//    batch.setOimOrderBatchesTypes(batchesTypes);
//    batch.setInsertionTm(new Date());
//    batch.setCreationTm(new Date());
//    m_dbSession.save(batch);
//    tx.commit();
//    if (StringHandle.removeNull(m_orderProcessingRule.getPullWithStatus()).equals(""))
//      throw new ChannelConfigurationException(
//          "Error in channel Setup : Orders To Pull From Channel not correctly configured");
    //String status = m_orderProcessingRule.getPullWithStatus();
    String status = "New";
    storeUrl = "http://zduaf.fkywj.servertrust.com";
    login = "integrations@inventorysource.com";
    encriptedPassword = "F11EE7FA6F826A85B8B361F929B636F2A993C08EB7792E6ABF735513F2ADC92F";
    String requestParameters = "&SELECT_Columns=o.OrderID,o.BillingAddress1,o.BillingAddress2,o.BillingCity,"
        + "o.BillingCompanyName,o.BillingCountry,o.BillingFirstName,o.BillingLastName,o.BillingPhoneNumber,"
        + "o.BillingPostalCode,o.BillingState,o.LastModified,o.OrderDate,o.OrderStatus,o.PaymentAmount,o.PONum,"
        + "o.ShipAddress1,o.ShipAddress2,o.ShipCity,o.ShipCompanyName,o.ShipCountry,o.ShipDate,o.ShipFirstName,"
        + "o.ShipLastName,o.Shipped,o.ShipPhoneNumber,o.ShipPostalCode,o.ShipResidential,o.ShipState,o.sOrderID,"
        + "od.OrderDetailID,od.LastModified,od.OrderID,od.ProductID,od.ProductName,od.ProductPrice,od.QtyShipped,"
        + "od.Quantity,od.ShipDate,od.Shipped,od.TotalPrice&WHERE_Column=o.OrderStatus&WHERE_Value="
        + status;
    // &WHERE_Column=o.OrderStatus&WHERE_Value=Processing

    String requestUrl = storeUrl + "/net/WebService.aspx?Login=" + login + "&EncryptedPassword="
        + encriptedPassword + "&API_Name=Generic\\Orders" + requestParameters;

    HttpURLConnection connection = null;
    URL url;
    int responseCode = 0;
    try {
      // url = new URL(null, requestUrl, new sun.net.www.protocol.https.Handler());
      url = new URL(requestUrl);
      connection = (HttpURLConnection) url.openConnection();
      // sun.net.www.protocol.http.HttpURLConnection cannot be cast to
      // javax.net.ssl.HttpsURLConnection
      connection.setRequestMethod("GET");
      connection.setDoOutput(true);
      connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
      connection.setRequestProperty("Content-Action", "Volusion_API");
      connection.connect();
      responseCode = connection.getResponseCode();
      String response = getStringFromStream(connection.getInputStream());
      System.out.println(response);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      salesmachine.oim.stores.modal.volusion.order.Xmldata xmlResponse = (salesmachine.oim.stores.modal.volusion.order.Xmldata) unmarshaller
          .unmarshal(new StringReader(response));
      int totalOrders = 0;
      for (Orders order : xmlResponse.getOrders()) {
        String storeOrderId = Byte.toString(order.getSOrderID());
        if (orderAlreadyImported(storeOrderId)) {
          log.info("Order#{} is already imported in the system, updating Order.", storeOrderId);
          continue;
        }
        tx = m_dbSession.beginTransaction();
        OimOrders oimOrders = new OimOrders();
        oimOrders.setOrderNumber(Byte.toString(order.getOrderID()));
        oimOrders.setStoreOrderId(storeOrderId);
        // setting delivery information
        oimOrders.setBillingStreetAddress(StringHandle.removeNull(order.getBillingAddress1()));
        oimOrders.setBillingSuburb(StringHandle.removeNull(order.getBillingAddress2()));
        oimOrders.setBillingZip(StringHandle.removeNull(order.getBillingPostalCode()));
        oimOrders.setBillingCity(StringHandle.removeNull(order.getBillingCity()));
        oimOrders.setBillingCompany(StringHandle.removeNull(order.getBillingCompanyName()));
        oimOrders.setBillingCountry(StringHandle.removeNull(order.getBillingCountry()));
        oimOrders.setBillingName(StringHandle
            .removeNull(order.getBillingFirstName() + " " + order.getBillingLastName()));
        oimOrders.setBillingPhone(StringHandle.removeNull(order.getBillingPhoneNumber()));
        oimOrders.setBillingState(StringHandle.removeNull(order.getBillingPhoneNumber()));
        // setting shipping information

        oimOrders.setDeliveryCity(StringHandle.removeNull(order.getShipAddress1()));
        oimOrders.setDeliveryCompany(StringHandle.removeNull(order.getShipAddress2()));
        oimOrders.setDeliveryCountry(StringHandle.removeNull(order.getShipCountry()));
        String countryCode = validateAndGetCountryCode(oimOrders);
        if (!StringHandle.isNullOrEmpty(countryCode))
          oimOrders.setDeliveryCountryCode(countryCode);
        oimOrders.setDeliveryName(
            StringHandle.removeNull(order.getShipFirstName() + " " + order.getShipLastName()));
        oimOrders.setDeliveryPhone(StringHandle.removeNull(order.getShipPhoneNumber()));
        oimOrders.setDeliveryStreetAddress(StringHandle.removeNull(order.getShipAddress1()));
        oimOrders.setDeliverySuburb(StringHandle.removeNull(order.getShipAddress2()));
        oimOrders.setDeliveryZip(StringHandle.removeNull(order.getShipPostalCode()));
        oimOrders.setDeliveryState(StringHandle.removeNull(order.getShipState()));

        if (!StringHandle.isNullOrEmpty(order.getShipState())
            && order.getShipState().length() == 2) {
          oimOrders.setDeliveryStateCode(StringHandle.removeNull(order.getShipState()));
        } else {
          String stateCode = validateAndGetStateCode(oimOrders);
          if (!StringHandle.isNullOrEmpty(stateCode))
            oimOrders.setDeliveryStateCode(stateCode);
        }
        
        // setting customer information
        oimOrders.setCustomerCity(StringHandle.removeNull(order.getShipCity()));
        oimOrders
            .setCustomerCompany(StringHandle.removeNull(order.getShipCompanyName()));
        oimOrders
            .setCustomerCountry(StringHandle.removeNull(order.getShipCountry()));
        oimOrders.setCustomerName(StringHandle.removeNull(order.getShipFirstName()+" "+order.getShipLastName()));
        oimOrders
            .setCustomerPhone(StringHandle.removeNull(order.getShipPhoneNumber()));
        oimOrders.setCustomerStreetAddress(
            StringHandle.removeNull(order.getShipAddress1()));
        oimOrders
            .setCustomerSuburb(StringHandle.removeNull(order.getShipAddress2()));
        oimOrders.setCustomerZip(StringHandle.removeNull(order.getShipPostalCode()));
        oimOrders
            .setCustomerState(StringHandle.removeNull(order.getShipState()));
        
        //other oimorder informations
        oimOrders.setInsertionTm(new Date());
        oimOrders.setOimOrderBatches(batch);
        oimOrders.setOrderFetchTm(new Date());
        batch.getOimOrderses().add(oimOrders);
        
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy'T'HH:mm:ssXXX"); // 2/24/2016 4:21:00 AM
        Date orderTm = null;
        try {
          String orderTmString = (order.getOrderDate());
          orderTm = df.parse(orderTmString);
        } catch (java.text.ParseException e) {
          e.printStackTrace();
        }
        oimOrders.setOrderTm(orderTm);
        oimOrders.setOrderTotalAmount(new Double(order.getPaymentAmount()));
       if(order.getPaymentMethodID()==1)
        oimOrders.setPayMethod("Credit Card");
       else if(order.getPaymentMethodID()==2)
         oimOrders.setPayMethod("Check by Mail");
       String shippingDetails = Byte.toString(order.getShippingMethodID());
       oimOrders.setShippingDetails(shippingDetails);
       for (OimChannelShippingMap entity : oimChannelShippingMapList) {
         String shippingRegEx = entity.getShippingRegEx();
         if (shippingDetails.equalsIgnoreCase(shippingRegEx)) {
           oimOrders.setOimShippingMethod(entity.getOimShippingMethod());
           log.info("Shipping set to " + entity.getOimShippingMethod().getName());
           break;
         }
       }
       if (oimOrders.getOimShippingMethod() == null)
         log.warn("Shipping can't be mapped for order " + oimOrders.getStoreOrderId());
       
       //setting oimOrderDetails
       Set<OimOrderDetails> detailSet = new HashSet<OimOrderDetails>();
       for(OrderDetails item : order.getOrderDetails()){
         OimOrderDetails oimOrderDetail = new OimOrderDetails();
        
         oimOrderDetail.setCostPrice(
             Double.parseDouble(StringHandle.removeNull(item.getProductPrice())));
         oimOrderDetail.setInsertionTm(new Date());
         oimOrderDetail
             .setOimOrderStatuses(new OimOrderStatuses(OimConstants.ORDER_STATUS_UNPROCESSED));
         String sku = item.getProductCode();
         OimSuppliers oimSuppliers = null;
         String prefix = null;
         List<OimSuppliers> blankPrefixSupplierList = new ArrayList<OimSuppliers>();
         for (Iterator<OimSuppliers> itr = supplierMap.keySet().iterator(); itr.hasNext();) {
           OimSuppliers supplier = itr.next();
           prefix = supplierMap.get(supplier);
           if (prefix == null) {
             blankPrefixSupplierList.add(supplier);
             continue;
           }
           if (sku.toUpperCase().startsWith(prefix)) {
             oimSuppliers = supplier;
             break;
           }
         }
         if (oimSuppliers == null && blankPrefixSupplierList.size() == 1) {
           oimSuppliers = blankPrefixSupplierList.get(0);
         }
         if (oimSuppliers != null) {
           oimOrderDetail.setOimSuppliers(oimSuppliers);
         }
         oimOrderDetail.setProductDesc(item.getProductName());
         oimOrderDetail.setProductName(item.getProductName());
         oimOrderDetail.setQuantity(new Byte(item.getQuantity()).intValue());
         oimOrderDetail.setSalePrice(new Byte(item.getQuantity()).doubleValue()); // since no salePrice corresponding to order detail found in api
         oimOrderDetail.setSku(sku);
         oimOrderDetail.setStoreOrderItemId(new Byte(item.getOrderDetailID()).toString());
         oimOrderDetail.setOimOrders(oimOrders);
         m_dbSession.saveOrUpdate(oimOrderDetail);
         detailSet.add(oimOrderDetail);
       }
       oimOrders.setOimOrderDetailses(detailSet);
       m_dbSession.saveOrUpdate(oimOrders);
      // String acknowledgementURL = storeUrl + "/admin/orders/" + storeOrderId + ".json"; // 704264451
//       if (m_channel.getTestMode() == 0) {
//         sendAcknowledgementToStore(acknowledgementURL, storeOrderId, tags);
//       } else {
//         log.warn("Acknowledgement to channel was not sent as Channel is set to test mode.");
//       }
       tx.commit();
       totalOrders++;
       
      }
      log.info("Fetched {} order(s)", totalOrders);
      try {
        tx = m_dbSession.beginTransaction();
        m_channel.setLastFetchTm(new Date());
        m_dbSession.persist(m_channel);
        tx.commit();
      } catch (HibernateException e) {
        tx.rollback();
      }
      log.debug("Finished importing orders...");
      log.info("Returning Order batch with size: {}", batch.getOimOrderses().size());

    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ProtocolException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (JAXBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  @Override
  public void updateStoreOrder(OimOrderDetails oimOrderDetails, OrderStatus orderStatus)
      throws ChannelCommunicationException, ChannelOrderFormatException,
      ChannelConfigurationException {
    // TODO Auto-generated method stub

  }

  @Override
  public void cancelOrder(OimOrders oimOrder) throws ChannelOrderFormatException,
      ChannelCommunicationException, ChannelConfigurationException {
    // TODO Auto-generated method stub

  }

  @Override
  public void cancelOrder(OimOrderDetails oimOrder)
      throws ChannelOrderFormatException, ChannelCommunicationException {
    // TODO Auto-generated method stub

  }

}
