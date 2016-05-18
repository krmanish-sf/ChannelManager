/**
 * 
 */
package salesmachine.oim.stores.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.TransactionException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimChannelShippingMap;
import salesmachine.hibernatedb.OimChannelSupplierMap;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderBatchesTypes;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderStatuses;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSuppliers;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatehelper.PojoHelper;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.ChannelBase;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.oim.suppliers.modal.TrackingData;
import salesmachine.util.StringHandle;
import sun.misc.BASE64Encoder;

/**
 * @author manish-kumar
 *
 */
public class OrdoroOrderImport extends ChannelBase implements IOrderImport {

  private static final Logger log = LoggerFactory.getLogger(OrdoroOrderImport.class);
  private static String USER_NAME;
  private static String PASSWORD;
  private static final String GET_REQUEST_METHOD = "GET";
  private static final String POST_REQUEST_METHOD = "POST";
  private static final String PUT_REQUEST_METHOD = "PUT";
  private int noOfApiRequests = 0;
  private static final String orderEndPoint = "https://api.ordoro.com/order";
  private static final String trackingEndPoint = "https://api.ordoro.com/shipment";
  static int limit = 100;

  @Override
  public boolean init(OimChannels oimChannel, Session dbSession)
      throws ChannelConfigurationException {
    super.init(oimChannel, dbSession);
    USER_NAME = StringHandle.removeNull(PojoHelper.getChannelAccessDetailValue(m_channel,
        OimConstants.CHANNEL_ACCESSDETAIL_ADMIN_LOGIN));
    PASSWORD = StringHandle.removeNull(PojoHelper.getChannelAccessDetailValue(m_channel,
        OimConstants.CHANNEL_ACCESSDETAIL_ADMIN_PWD));
    return true;
  }

  @Override
  public void getVendorOrders(OimOrderBatchesTypes batchesTypes, OimOrderBatches batch)
      throws ChannelCommunicationException, ChannelOrderFormatException,
      ChannelConfigurationException {
    // cartId = "64619"; -- manual cart
    // cartId = "64620"; // shopify cart
    Transaction tx = m_dbSession.getTransaction();
    batch.setOimChannels(m_channel);
    batch.setOimOrderBatchesTypes(batchesTypes);
    if (tx != null && tx.isActive())
      tx.commit();
    tx = m_dbSession.beginTransaction();
    batch.setInsertionTm(new Date());
    batch.setCreationTm(new Date());
    m_dbSession.save(batch);
    tx.commit();
    if (StringHandle.removeNull(m_orderProcessingRule.getPullWithStatus()).equals(""))
      throw new ChannelConfigurationException(
          "Error in channel Setup : Orders To Pull From Channel not correctly configured");
    HashMap<Integer, String> mappedSupplierMap = getMappedSupplierIds();
    Calendar c = Calendar.getInstance();
    c.setTime(new Date());
    c.add(Calendar.HOUR, -312);
    Date fetchOrdersAfter = c.getTime();
    log.info("Set to fetch Orders after {}", fetchOrdersAfter);

    String status = m_orderProcessingRule.getPullWithStatus();
    // String status = "in_process";
    String requestUrl = orderEndPoint + "/?status=" + status;
    try {
      SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss zZ");
      requestUrl += "&start_order_date=" + URLEncoder.encode(df.format(fetchOrdersAfter), "UTF-8");
    } catch (UnsupportedEncodingException e1) {
      log.warn("Encoding type [UTF-8] is invalid");
    }
    int offset = 0;
    boolean isNoMoreOrders = false;
    JSONParser parser = new JSONParser();
    do {
      requestUrl += "&limit=" + limit + "&offset=" + offset;
      String response = sendRequestOAuth(null, requestUrl, GET_REQUEST_METHOD, null);
      JSONObject jsonObject;
      try {
        jsonObject = (JSONObject) parser.parse(response);
      } catch (ParseException e1) {
        log.error("Error in parsing response from Ordoro store for channel- "
            + m_channel.getChannelName(), e1);
        throw new ChannelOrderFormatException(
            "Error in parsing response from Ordoro store for channel - "
                + m_channel.getChannelName(),
            e1);
      }
      int count = ((Long) jsonObject.get("count")).intValue();
      int orderLimit = ((Long) jsonObject.get("limit")).intValue();
      if (count < orderLimit)
        isNoMoreOrders = true;
      else {
        offset += limit;
      }
      JSONArray orderArr = (JSONArray) jsonObject.get("order");
      for (int i = 0; i < orderArr.size(); i++) {
        String storeOrderId = null;
        OimOrders oimOrders = null;
        try {
          JSONObject orderObj = (JSONObject) orderArr.get(i);
          storeOrderId = orderObj.get("order_id").toString();
          String shippability = (String)orderObj.get("shippability");
          if("unshippable".equalsIgnoreCase(shippability)){
            log.info("Store order id {} is unshippable.. ignoring it...",storeOrderId);
            continue;
          }
          if (orderAlreadyImported(storeOrderId)) {
            log.info("Order#{} is already imported in the system, updating Order.", storeOrderId);
            continue;
          }
          tx = m_dbSession.getTransaction();
          if (tx != null && tx.isActive())
            tx.commit();
          tx = m_dbSession.beginTransaction();
          oimOrders = new OimOrders();
          oimOrders.setStoreOrderId(storeOrderId);

          // setting billing information
          JSONObject billingObj = (JSONObject) orderObj.get("billing_address");
          if (billingObj != null) {
            oimOrders.setBillingStreetAddress(
                StringHandle.removeNull((String) billingObj.get("street1")));
            oimOrders.setBillingSuburb(StringHandle.removeNull((String) billingObj.get("street1")));

            oimOrders.setBillingZip(StringHandle.removeNull((String) billingObj.get("zip")));
            oimOrders.setBillingCity(StringHandle.removeNull((String) billingObj.get("city")));
            oimOrders
                .setBillingCompany(StringHandle.removeNull((String) billingObj.get("company")));
            oimOrders
                .setBillingCountry(StringHandle.removeNull((String) billingObj.get("country")));
            oimOrders.setBillingName(StringHandle.removeNull((String) billingObj.get("name")));
            oimOrders.setBillingPhone(StringHandle.removeNull((String) billingObj.get("phone")));
            oimOrders.setBillingState(StringHandle.removeNull((String) billingObj.get("state")));
          }
          // setting delivery information
          JSONObject deliveryObj = (JSONObject) orderObj.get("shipping_address");
          if (deliveryObj != null) {
            oimOrders.setDeliveryCity(StringHandle.removeNull((String) deliveryObj.get("city")));
            oimOrders
                .setDeliveryCompany(StringHandle.removeNull((String) deliveryObj.get("company")));
            oimOrders
                .setDeliveryCountry(StringHandle.removeNull((String) deliveryObj.get("country")));
            oimOrders.setDeliveryCountryCode(
                StringHandle.removeNull((String) deliveryObj.get("country")));
            oimOrders.setDeliveryName(StringHandle.removeNull((String) deliveryObj.get("name")));
            oimOrders.setDeliveryPhone(StringHandle.removeNull((String) deliveryObj.get("phone")));
            oimOrders.setDeliveryStreetAddress(
                StringHandle.removeNull((String) deliveryObj.get("street1")));
            oimOrders
                .setDeliverySuburb(StringHandle.removeNull((String) deliveryObj.get("street2")));
            oimOrders.setDeliveryZip(StringHandle.removeNull((String) deliveryObj.get("zip")));
            oimOrders.setDeliveryState(StringHandle.removeNull((String) deliveryObj.get("state")));
            if (deliveryObj.get("state") != null
                && ((String) deliveryObj.get("state")).length() == 2) {
              oimOrders.setDeliveryStateCode((String) deliveryObj.get("state"));
            } else {
              String stateCode = validateAndGetStateCode(oimOrders);
              if (stateCode != "")
                oimOrders.setDeliveryStateCode(stateCode);
            }
          }
          // setting customer information
          JSONObject customerObj = (JSONObject) orderObj.get("shipping_address");
          if (customerObj != null) {
            oimOrders.setCustomerEmail(StringHandle.removeNull((String) customerObj.get("email")));
            oimOrders.setCustomerCity(StringHandle.removeNull((String) customerObj.get("city")));
            oimOrders
                .setCustomerCompany(StringHandle.removeNull((String) customerObj.get("company")));
            oimOrders
                .setCustomerCountry(StringHandle.removeNull((String) customerObj.get("country")));
            oimOrders.setCustomerName(StringHandle.removeNull((String) customerObj.get("name")));
            oimOrders.setCustomerPhone(StringHandle.removeNull((String) customerObj.get("phone")));
            oimOrders.setCustomerStreetAddress(
                StringHandle.removeNull((String) customerObj.get("street1")));
            oimOrders
                .setCustomerSuburb(StringHandle.removeNull((String) customerObj.get("street2")));
            oimOrders.setCustomerZip(StringHandle.removeNull((String) customerObj.get("zip")));
            oimOrders.setCustomerState(StringHandle.removeNull((String) customerObj.get("state")));
          }

          oimOrders.setOrderFetchTm(new Date());
          SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"); // "2015-05-27T04:38:58-04:00
          // 2016-05-04T06:37:57-05:00
          Date orderTm = null;
          try {
            String orderTmString = ((String) orderObj.get("order_date"));
            orderTm = df.parse(orderTmString);
          } catch (java.text.ParseException e) {
            e.printStackTrace();
          }
          oimOrders.setOrderTm(orderTm);
          oimOrders.setOrderTotalAmount((double) orderObj.get("grand_total"));
          // oimOrders.setPayMethod((String) orderObj.get("gateway"));
          String shippingDetails = null;
          try {
            JSONArray shippingDetailsArray = (JSONArray) orderObj.get("shipments");
            for (int k = 0; k < shippingDetailsArray.size(); k++) {
              JSONObject obj = (JSONObject) shippingDetailsArray.get(0);
              if (null != obj.get("status")
                  && !((String) obj.get("status")).equalsIgnoreCase("deleted")) {
                shippingDetails = (String) obj.get("requested_shipping_method");
              }
            }
          } catch (IndexOutOfBoundsException e) {
            log.error(e.getMessage(), e);

          }
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
          // m_dbSession.saveOrUpdate(oimOrders);
          // setting product information
          JSONArray itemArray = (JSONArray) orderObj.get("lines");
          Set<OimOrderDetails> detailSet = new HashSet<OimOrderDetails>();
          for (int j = 0; j < itemArray.size(); j++) {
            OimOrderDetails detail = new OimOrderDetails();
            JSONObject item = (JSONObject) itemArray.get(j);
            detail.setCostPrice((double) item.get("item_price"));
            detail.setInsertionTm(new Date());
            detail.setOimOrderStatuses(
                new OimOrderStatuses(OimConstants.ORDER_STATUS_PROCESSED_SUCCESS));
            JSONObject product = (JSONObject) item.get("product");
            String sku = (String) product.get("sku");
            detail.setProductDesc((String) product.get("name"));
            detail.setProductName((String) product.get("name"));
            detail.setQuantity((int) (long) (item.get("quantity")));
            detail.setSalePrice((double) item.get("total_price"));
            detail.setSku(sku);
            detail.setStoreOrderItemId(((long) item.get("order_line_id")) + "");
            detail.setSupplierOrderStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_SENT_TO_SUPPLIER);
            detail.setOimOrders(oimOrders);
            detailSet.add(detail);
            detail = getShipmentAndSupplierForOrderDetail(detail, orderObj, mappedSupplierMap);
            if (detail.getSupplierOrderNumber() == null)
              continue;
            detailSet.add(detail);
          }
          if (detailSet.size() == 0)
            continue;

          for (Iterator<OimOrderDetails> dtl = detailSet.iterator(); dtl.hasNext();) {
            OimOrderDetails detailToSave = dtl.next();
            m_dbSession.saveOrUpdate(detailToSave);
          }
          oimOrders.setOimOrderDetailses(detailSet);
          oimOrders.setInsertionTm(new Date());
          oimOrders.setOimOrderBatches(batch);
          batch.getOimOrderses().add(oimOrders);
          m_dbSession.saveOrUpdate(oimOrders);
        } catch (HibernateException e) {
          log.error("Error occured during pull of store order id - " + storeOrderId, e);
          try {
            m_dbSession.clear();
            tx.rollback();
          } catch (RuntimeException e1) {
            log.error("Couldn’t roll back transaction", e1);
            e1.printStackTrace();
          }
          if (e instanceof TransactionException) {
            log.error("duplicate store order id - " + storeOrderId);
          } else
            throw new ChannelOrderFormatException(
                "Error occured during pull of store order id - " + storeOrderId, e);
        } catch (Exception e) {
          log.error("Error occured during pull of store order id - " + storeOrderId, e);
          tx.rollback();
          throw new ChannelOrderFormatException("Error occured during pull of store order id - "
              + storeOrderId + " cause - " + e.getMessage(), e);
        }
      }
    } while (isNoMoreOrders == false);
  }

  private HashMap<Integer, String> getMappedSupplierIds() {
    HashMap<Integer, String> returnMap = new HashMap<Integer, String>();
    Session session = SessionManager.currentSession();
    Query query;
    try {
      query = session.createQuery(
          "from OimChannelSupplierMap s where  s.oimChannels.channelId=:channelId and s.deleteTm is null");

      query.setInteger("channelId", m_channel.getChannelId());
      Iterator it = query.iterate();
      if (it.hasNext()) {
        OimChannelSupplierMap channelSupplierMap = (OimChannelSupplierMap) it.next();
        if (channelSupplierMap != null && channelSupplierMap.getChannelSupplierId() != null)
          returnMap.put(channelSupplierMap.getMapId(), channelSupplierMap.getChannelSupplierId()
              + "~" + channelSupplierMap.getOimSuppliers().getSupplierId());
      }
    } catch (Exception e) {
    }
    return returnMap;
  }

  private OimOrderDetails getShipmentAndSupplierForOrderDetail(OimOrderDetails detail,
      JSONObject orderObj, HashMap<Integer, String> mappedSupplierMap) {
    JSONArray shipmentArray = (JSONArray) orderObj.get("shipments");
    Session dbSession = SessionManager.currentSession();
    for (int i = 0; i < shipmentArray.size(); i++) {
      JSONObject shipmentObj = (JSONObject) shipmentArray.get(i);
      String status = (String) shipmentObj.get("status");
      if (!StringHandle.isNullOrEmpty(status) && status.equalsIgnoreCase("deleted"))
        continue;
      JSONArray itemArray = (JSONArray) shipmentObj.get("lines");
      if (!isMatchingProduct(itemArray, detail.getSku(), detail.getQuantity()))
        continue;
      String shipmentId = (String) shipmentObj.get("shipment_id");
      JSONObject ship_from = (JSONObject) shipmentObj.get("ship_from");
      String dropshipperId = String.valueOf((long) ship_from.get("dropshipper_id"));
      if (dropshipperId == null)
        continue;
      for (Iterator<Integer> itr = mappedSupplierMap.keySet().iterator(); itr.hasNext();) {
        int supplierMapId = itr.next();
        String val = mappedSupplierMap.get(supplierMapId);
        String[] valArray = val.split("~");
        String channelSupplierId = valArray[0];
        String supplierIdStr = valArray[1];
        if (dropshipperId.equals(String.valueOf(channelSupplierId))) {
          int supplierId = Integer.parseInt(supplierIdStr);
          OimSuppliers oimSuppliers = (OimSuppliers) dbSession.get(OimSuppliers.class, supplierId);
          detail.setOimSuppliers(oimSuppliers);
          detail.setSupplierOrderNumber(shipmentId);
        }
      }
    }
    return detail;
  }

  private boolean isMatchingProduct(JSONArray itemArray, String sku, Integer quantity) {
    for (int i = 0; i < itemArray.size(); i++) {
      JSONObject item = (JSONObject) itemArray.get(i);
      JSONObject product = (JSONObject) item.get("product");
      String prod_sku = (String) product.get("sku");
      if (!sku.equalsIgnoreCase(prod_sku))
        continue;
      int prod_qty = (int) (long) item.get("quantity");
      int prod_ordered_quantity = (int) (long) item.get("ordered_quantity");
      if (prod_qty == prod_ordered_quantity)
        return true;
    }
    return false;
  }

  private String sendRequestOAuth(String data, String requestUrl, String requestMethod,
      String storeOrderId) throws ChannelConfigurationException, ChannelCommunicationException,
          ChannelOrderFormatException {
    String response = null;
    HttpsURLConnection connection = null;
    URL url;
    int responseCode = 0;
    try {

      url = new URL(requestUrl);
      connection = (HttpsURLConnection) url.openConnection();
      connection.setRequestMethod(requestMethod);
      connection.setDoOutput(true);
      BASE64Encoder enc = new sun.misc.BASE64Encoder();
      String userpassword = USER_NAME + ":" + PASSWORD;
      String encodedAuthorization = enc.encode(userpassword.getBytes());
      encodedAuthorization = encodedAuthorization.replaceAll("\n", "");

      if (USER_NAME != null && USER_NAME.trim().length() > 0 && PASSWORD != null
          && PASSWORD.trim().length() > 0) {
        connection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
      }
      connection.setRequestProperty("Content-type", "application/json");

      if (data != null && (requestMethod.equalsIgnoreCase(POST_REQUEST_METHOD)
          || requestMethod.equalsIgnoreCase(PUT_REQUEST_METHOD))) {
        byte[] req = data.getBytes();
        OutputStream out = connection.getOutputStream();
        out.write(req);
        out.close();
      }
      connection.connect();
      responseCode = connection.getResponseCode();
      if (responseCode == 200) {
        response = getStringFromStream(connection.getInputStream());
      } else if (responseCode == 201 && requestMethod.equalsIgnoreCase(POST_REQUEST_METHOD)) {
        response = getStringFromStream(connection.getInputStream());
      } else if (responseCode == 429) {
        noOfApiRequests++;
        connection.disconnect();
        if (noOfApiRequests < 5) {
          return sendRequestOAuth(data, requestUrl, requestMethod, storeOrderId);
        } else {
          throw new ChannelCommunicationException(
              "Response Code : 429 - API Call Limit/Bucket Overflow");
        }
      } else if (responseCode == 404) {
        throw new ChannelConfigurationException(
            "404 - The resource does not exist" + " URL - " + requestUrl);
      } else if (responseCode == 406) {
        throw new ChannelCommunicationException(
            "406 Not acceptable - Possibly a put/post to the requested URL is not acceptable - "
                + requestUrl);
      } else if (responseCode == 401) {
        throw new ChannelConfigurationException(
            "401- API Request is not valid for this shop. You are either not using the right Access Token or the permission for that token has been revoked");
      } else if (responseCode == 422) {
        if (storeOrderId != null)
          log.error(
              "422 - There was a problem with the body of your Request. Inspect the response body for the errors for store order id - {}",
              storeOrderId);
        log.error(
            "422 - There was a problem with the body of your Request. Inspect the response body for the errors");
      } else if (responseCode == 403) {
        if (storeOrderId != null)
          throw new ChannelConfigurationException("Error occured for store order id - "
              + storeOrderId + " response code 403 - Forbidden access - verify app OAuth scopes");
        throw new ChannelConfigurationException(
            "response code 403 - Forbidden access - verify app OAuth scopes");
      } else if (String.valueOf(responseCode).startsWith("5")) {
        throw new ChannelCommunicationException(
            "500 series - Either Shopify is down or you sent something that caused our code to error out.");
      } else
        throw new ChannelCommunicationException("response code - " + responseCode);
    } catch (MalformedURLException e) {
      throw new ChannelConfigurationException("MalformedURLException - " + requestUrl);
    } catch (IOException e) {
      throw new ChannelCommunicationException(e.getMessage());
    } finally {
      if (connection != null)
        connection.disconnect();
    }
    return response;
  }

  @Override
  public void updateStoreOrder(OimOrderDetails oimOrderDetails, OrderStatus orderStatus)
      throws ChannelCommunicationException, ChannelOrderFormatException,
      ChannelConfigurationException {
    // this method is implemented for tracking purpose
    log.info("order id is - {}", oimOrderDetails.getOimOrders().getOrderId());
    log.info("order status is - {}", orderStatus);
    if (!orderStatus.isShipped()) {
      return;
    }
    if (orderStatus.getTrackingData().isEmpty())
      return;
    // https://api.ordoro.com/shipment/1-1007-1/tracking/
    String poNumber = oimOrderDetails.getSupplierOrderNumber();
    SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss zZ");
    String shipDate = df.format(orderStatus.getTrackingData().get(0).getShipDate().toGregorianCalendar().getTime());
    String carrierName = !StringHandle
        .isNullOrEmpty(orderStatus.getTrackingData().get(0).getCarrierCode())
            ? orderStatus.getTrackingData().get(0).getCarrierCode()
            : orderStatus.getTrackingData().get(0).getCarrierName();
    String trackingNumber = orderStatus.getTrackingData().get(0).getShipperTrackingNumber();
    String shippingMethod = orderStatus.getTrackingData().get(0).getShippingMethod();
    String requestUrl = trackingEndPoint + "/" + poNumber + "/tracking/";
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("notify_cart", true);
    jsonObject.put("ship_date", shipDate);
    JSONObject trackingObject = new JSONObject();
    trackingObject.put("vendor", carrierName);
    trackingObject.put("tracking", trackingNumber);
    trackingObject.put("shipping_method", shippingMethod);
    jsonObject.put("tracking", trackingObject);

    log.info("request for fullfillment : {}", jsonObject.toJSONString());
    StringRequestEntity requestEntity = null;
    try {
      requestEntity = new StringRequestEntity(jsonObject.toJSONString(), "application/json",
          "UTF-8");
    } catch (UnsupportedEncodingException e) {
      log.error("Error in parsing tracking request json {}", e);
      throw new ChannelOrderFormatException("Error in parsing tracking request json", e);
    }
    sendRequestOAuth(jsonObject.toString(), requestUrl, POST_REQUEST_METHOD,
        oimOrderDetails.getOimOrders().getStoreOrderId());
    log.info("Updated Store with tracking information");

  }

  // public static void main(String[] args) {
  // try {
  // new OrdoroOrderImport().getVendorOrders(null, null);
  // } catch (ChannelCommunicationException | ChannelOrderFormatException
  // | ChannelConfigurationException e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  // }

  public static void main(String[] args) throws ChannelCommunicationException,
      ChannelOrderFormatException, ChannelConfigurationException {
    Session session = SessionManager.currentSession();
    OimOrderDetails oimOrderDetails = (OimOrderDetails) session.get(OimOrderDetails.class, 10323821);
    OrderStatus orderStatus = new OrderStatus();
    TrackingData td = new TrackingData();
    td.setCarrierName("UPS");
    td.setShippingMethod("GROUND");
    td.setShipperTrackingNumber("TESTTRACK9999");
    GregorianCalendar c = new GregorianCalendar();
    c.setTime(new Date());
    XMLGregorianCalendar date2 = null;
    try {
      date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
    } catch (DatatypeConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    td.setShipDate(date2);
    orderStatus.addTrackingData(td);
    OrdoroOrderImport orderImport = new OrdoroOrderImport();
    orderImport.USER_NAME = "integrations@inventorysource.com";
    orderImport.PASSWORD = "Aut0Inventory!";
    orderImport.updateStoreOrder(oimOrderDetails, orderStatus);
  }

  @Override
  public void cancelOrder(OimOrders oimOrder) throws ChannelOrderFormatException,
      ChannelCommunicationException, ChannelConfigurationException {

  }

  @Override
  public void cancelOrder(OimOrderDetails oimOrder)
      throws ChannelOrderFormatException, ChannelCommunicationException {

  }

}
