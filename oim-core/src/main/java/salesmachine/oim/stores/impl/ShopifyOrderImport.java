package salesmachine.oim.stores.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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

public final class ShopifyOrderImport extends ChannelBase implements IOrderImport {

  private static final Logger log = LoggerFactory.getLogger(ShopifyOrderImport.class);
  private String shopifyToken;
  private String storeUrl;
  private static final String GET_REQUEST_METHOD = "GET";
  private static final String POST_REQUEST_METHOD = "POST";
  private static final String PUT_REQUEST_METHOD = "PUT";
  private int noOfApiRequests = 0;

  @Override
  public boolean init(OimChannels oimChannel, Session dbSession)
      throws ChannelConfigurationException {
    super.init(oimChannel, dbSession);
    storeUrl = StringHandle.removeNull(PojoHelper.getChannelAccessDetailValue(m_channel,
        OimConstants.CHANNEL_ACCESSDETAIL_CHANNEL_URL));
    shopifyToken = StringHandle.removeNull(PojoHelper.getChannelAccessDetailValue(m_channel,
        OimConstants.CHANNEL_ACCESSDETAIL_SHOPIFY_ACCESS_CODE));

    if (storeUrl.length() == 0 || shopifyToken.length() == 0) {
      log.error("Channel setup is not correct. Please provide correct details.");
      throw new ChannelConfigurationException(
          "Channel setup is not correct. Please provide correct details.");
    }
    return true;
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
    String requestUrl = storeUrl + "/admin/orders/"
        + oimOrderDetails.getOimOrders().getStoreOrderId() + "/fulfillments.json";
    // if(orderStatus.getStatus().equalsIgnoreCase(m_orderProcessingRule.getFailedStatus())){
    // sendAcknowledgementToStore(requestUrl,
    // Integer.parseInt(oimOrderDetails.getOimOrders().getStoreOrderId()),
    // m_orderProcessingRule.getFailedStatus(),false,null);
    // return true;
    // }
    // post fullfillment

    JSONObject jsonObject = new JSONObject();
    JSONObject jsonObjVal = new JSONObject();
    JSONArray trackingNos = new JSONArray();
    JSONArray lineItemArray = new JSONArray();
    int qty = 0;
    for (TrackingData trackingData : orderStatus.getTrackingData()) {
      trackingNos.add(trackingData.getShipperTrackingNumber());
      qty += trackingData.getQuantity();
    }
    jsonObjVal.put("tracking_numbers", trackingNos);
    jsonObjVal.put("tracking_company", orderStatus.getTrackingData().get(0).getCarrierName());

    JSONObject lineItem = new JSONObject();
    lineItem.put("id", oimOrderDetails.getStoreOrderItemId());
    lineItem.put("quantity", qty);
    lineItemArray.add(lineItem);

    jsonObjVal.put("notify_customer", true);
    // jsonObjVal.put("line_items", lineItemArray);
    jsonObject.put("fulfillment", jsonObjVal);
    log.info("request for fullfillment : {}", jsonObject.toJSONString());
    StringRequestEntity requestEntity = null;
    try {
      requestEntity = new StringRequestEntity(jsonObject.toJSONString(), "application/json",
          "UTF-8");
    } catch (UnsupportedEncodingException e) {
      log.error("Error in parsing tracking request json {}", e);
      throw new ChannelOrderFormatException("Error in parsing tracking request json", e);
    }
    this.noOfApiRequests = 0;
    sendRequestOAuth(jsonObject.toString(), requestUrl, POST_REQUEST_METHOD,oimOrderDetails.getOimOrders().getStoreOrderId());
  }

  @Override
  public void getVendorOrders(OimOrderBatchesTypes batchesTypes, OimOrderBatches batch)
      throws ChannelCommunicationException, ChannelOrderFormatException,
      ChannelConfigurationException {
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
    String status = m_orderProcessingRule.getPullWithStatus();
    String requestUrl = storeUrl + "/admin/orders.json?fulfillment_status=" + status;
    Calendar c = Calendar.getInstance();
    c.setTime(new Date());
    c.add(Calendar.HOUR, -24);
    Date fetchOrdersAfter = c.getTime();
    log.info("Set to fetch Orders after {}", fetchOrdersAfter);
    try {
      SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss zZ");
      requestUrl += "?updated_at_min=" + URLEncoder.encode(df.format(fetchOrdersAfter), "UTF-8");
    } catch (UnsupportedEncodingException e1) {
      log.warn("Encoding type [UTF-8] is invalid");
    }
    // requestUrl += "&limit=250&page=";

    JSONParser parser = new JSONParser();
    int totalOrders = 0;
    int pageNo = 0;
    JSONArray orderArr = new JSONArray();
    do {
      String orderRequestUrl = requestUrl + "&limit=250&page=" + ++pageNo;
      this.noOfApiRequests = 0;
      String response = sendRequestOAuth(null, orderRequestUrl, GET_REQUEST_METHOD,null);
      JSONObject jsonObject;
      try {
        jsonObject = (JSONObject) parser.parse(response);
      } catch (ParseException e1) {
        log.error(
            "Error in parsing response from shopify store for request url - " + orderRequestUrl,
            e1);
        throw new ChannelOrderFormatException(
            "Error in parsing response from shopify store for request url - " + orderRequestUrl,
            e1);
      }
      orderArr = (JSONArray) jsonObject.get("orders");
      for (int i = 0; i < orderArr.size(); i++) {
        String storeOrderId = null;
        OimOrders oimOrders = null;
        try {
          JSONObject orderObj = (JSONObject) orderArr.get(i);
          storeOrderId = orderObj.get("id").toString();
          String tags = StringHandle.removeComma(orderObj.get("tags").toString());
          if (tags.length() > 0) {
            tags = tags + ",";
          }
          if (orderAlreadyImported(storeOrderId)) {
            log.info("Order#{} is already imported in the system, updating Order.", storeOrderId);
            continue;
          }
          tx = m_dbSession.beginTransaction();
          oimOrders = new OimOrders();
          if(orderObj.get("order_number")!=null){
            String orderNumber = Long.toString((long)orderObj.get("order_number"));
            oimOrders.setOrderNumber(orderNumber);
          }
          
          oimOrders.setStoreOrderId(storeOrderId);
          // setting billing information
          JSONObject billingObj = (JSONObject) orderObj.get("billing_address");
          if (billingObj != null) {
            oimOrders.setBillingStreetAddress(
                StringHandle.removeNull((String) billingObj.get("address1")));
            oimOrders
                .setBillingSuburb(StringHandle.removeNull((String) billingObj.get("address2")));

            oimOrders.setBillingZip(StringHandle.removeNull((String) billingObj.get("zip")));
            oimOrders.setBillingCity(StringHandle.removeNull((String) billingObj.get("city")));
            oimOrders
                .setBillingCompany(StringHandle.removeNull((String) billingObj.get("company")));
            oimOrders
                .setBillingCountry(StringHandle.removeNull((String) billingObj.get("country")));
            oimOrders.setBillingName(StringHandle.removeNull((String) billingObj.get("first_name"))
                + " " + StringHandle.removeNull((String) billingObj.get("last_name")));
            oimOrders.setBillingPhone(StringHandle.removeNull((String) billingObj.get("phone")));
            oimOrders.setBillingState(StringHandle.removeNull((String) billingObj.get("province")));
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
                StringHandle.removeNull((String) deliveryObj.get("country_code")));
            oimOrders
                .setDeliveryName(StringHandle.removeNull((String) deliveryObj.get("first_name"))
                    + " " + StringHandle.removeNull((String) deliveryObj.get("last_name")));
            oimOrders.setDeliveryPhone(StringHandle.removeNull((String) deliveryObj.get("phone")));
            oimOrders.setDeliveryStreetAddress(
                StringHandle.removeNull((String) deliveryObj.get("address1")));
            oimOrders
                .setDeliverySuburb(StringHandle.removeNull((String) deliveryObj.get("address2")));
            oimOrders.setDeliveryZip(StringHandle.removeNull((String) deliveryObj.get("zip")));
            oimOrders
                .setDeliveryState(StringHandle.removeNull((String) deliveryObj.get("province")));
            // oimOrders.setDeliveryStateCode(StringHandle
            // .removeNull((String)
            // deliveryObj.get("province_code")));

            if (deliveryObj.get("province")!=null && ((String) deliveryObj.get("province")).length() == 2) {
              oimOrders.setDeliveryStateCode((String) deliveryObj.get("province"));
            } else {
              String stateCode = validateAndGetStateCode(oimOrders);
              if (stateCode != "")
                oimOrders.setDeliveryStateCode(stateCode);
            }
          }
          // setting customer information
          JSONObject custInfo = (JSONObject) orderObj.get("customer");
          if (custInfo != null) {
            oimOrders.setCustomerEmail(StringHandle.removeNull((String) custInfo.get("email")));
            JSONObject customerObj = (JSONObject) custInfo.get("default_address");
            if (customerObj != null) {
              oimOrders.setCustomerCity(StringHandle.removeNull((String) customerObj.get("city")));
              oimOrders
                  .setCustomerCompany(StringHandle.removeNull((String) customerObj.get("company")));
              oimOrders
                  .setCustomerCountry(StringHandle.removeNull((String) customerObj.get("country")));
              oimOrders.setCustomerName(StringHandle.removeNull((String) customerObj.get("name")));
              oimOrders
                  .setCustomerPhone(StringHandle.removeNull((String) customerObj.get("phone")));
              oimOrders.setCustomerStreetAddress(
                  StringHandle.removeNull((String) customerObj.get("address1")));
              oimOrders
                  .setCustomerSuburb(StringHandle.removeNull((String) customerObj.get("address2")));
              oimOrders.setCustomerZip(StringHandle.removeNull((String) customerObj.get("zip")));
              oimOrders
                  .setCustomerState(StringHandle.removeNull((String) customerObj.get("province")));
            }
          }

          oimOrders.setInsertionTm(new Date());
          oimOrders.setOimOrderBatches(batch);
          batch.getOimOrderses().add(oimOrders);
          // oimOrders.setOrderComment(order2.);
          oimOrders.setOrderFetchTm(new Date());
          SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"); // "2015-05-27T04:38:58-04:00
          Date orderTm = null;
          try {
            String orderTmString = ((String) orderObj.get("created_at"));
            orderTm = df.parse(orderTmString);
          } catch (java.text.ParseException e) {
            e.printStackTrace();
          }
          oimOrders.setOrderTm(orderTm);
          oimOrders.setOrderTotalAmount(Double.parseDouble((String) orderObj.get("total_price")));
          oimOrders.setPayMethod((String) orderObj.get("gateway"));
          String shippingDetails;
          try {
            shippingDetails = (String) ((JSONObject) ((JSONArray) orderObj.get("shipping_lines"))
                .get(0)).get("title");
          } catch (IndexOutOfBoundsException e) {
            shippingDetails = "Standard Shipping";
            log.warn("Order {} has no shipping method from store. Assigning default [{}] ",
                oimOrders.getStoreOrderId(), shippingDetails);
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
          JSONArray itemArray = (JSONArray) orderObj.get("line_items");
          Set<OimOrderDetails> detailSet = new HashSet<OimOrderDetails>();
          for (int j = 0; j < itemArray.size(); j++) {
            OimOrderDetails details = new OimOrderDetails();
            JSONObject item = (JSONObject) itemArray.get(j);
            details.setCostPrice(
                Double.parseDouble(StringHandle.removeNull((String) item.get("price"))));
            details.setInsertionTm(new Date());
            details
                .setOimOrderStatuses(new OimOrderStatuses(OimConstants.ORDER_STATUS_UNPROCESSED));
            String sku = (String) item.get("sku");
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
              details.setOimSuppliers(oimSuppliers);
            }
            details.setProductDesc((String) item.get("title"));
            details.setProductName((String) item.get("name"));
            details.setQuantity((int) (long) (item.get("quantity")));
            details.setSalePrice(
                Double.parseDouble(StringHandle.removeNull((String) item.get("price"))));
            details.setSku(sku);
            details.setStoreOrderItemId(((long) item.get("id")) + "");
            details.setOimOrders(oimOrders);
            m_dbSession.saveOrUpdate(details);
            detailSet.add(details);
          }
          oimOrders.setOimOrderDetailses(detailSet);
          m_dbSession.saveOrUpdate(oimOrders);
          String acknowledgementURL = storeUrl + "/admin/orders/" + storeOrderId + ".json"; // 704264451
          if (m_channel.getTestMode() == 0) {
            sendAcknowledgementToStore(acknowledgementURL, storeOrderId, tags);
          } else {
            log.warn("Acknowledgement to channel was not sent as Channel is set to test mode.");
          }
          tx.commit();
        } catch (HibernateException e) {
          log.error("Error occured during pull of store order id - " + storeOrderId, e);
          try {
            m_dbSession.clear();
            tx.rollback();
          } catch (RuntimeException e1) {
            log.error("Couldn’t roll back transaction", e1);
            e1.printStackTrace();
          }
          throw new ChannelOrderFormatException(
              "Error occured during pull of store order id - " + storeOrderId, e);
        } catch (Exception e) {
          log.error("Error occured during pull of store order id - " + storeOrderId, e);
          tx.rollback();
          throw new ChannelOrderFormatException("Error occured during pull of store order id - "
              + storeOrderId + " cause - " + e.getMessage(), e);
        }
        totalOrders++;
      }
    } while (orderArr.size() > 0);
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
  }

  private boolean sendAcknowledgementToStore(String requestUrl, String storeOrderId, String tags)
      throws ChannelCommunicationException, ChannelOrderFormatException,
      ChannelConfigurationException {
    JSONObject jObject = new JSONObject();
    JSONObject jsonObjVal = new JSONObject();
    jsonObjVal.put("id", storeOrderId);
    if (tags.length() > 0)
      jsonObjVal.put("tags", tags + m_orderProcessingRule.getConfirmedStatus());
    else
      jsonObjVal.put("tags", m_orderProcessingRule.getConfirmedStatus());
    jObject.put("order", jsonObjVal);
    StringRequestEntity requestEntity = null;
    try {
      requestEntity = new StringRequestEntity(jObject.toJSONString(), "application/json", "UTF-8");
    } catch (UnsupportedEncodingException e) {
      log.error("Error in creating json request on sending acknowledgement {}", e);
      throw new ChannelCommunicationException(
          "Error in creating json request on sending acknowledgement for store order id -"
              + storeOrderId,
          e);
      // return false;
    }
    this.noOfApiRequests = 0;
    sendRequestOAuth(jObject.toString(), requestUrl, PUT_REQUEST_METHOD,storeOrderId);
    return true;
  }

  private boolean closeOrder(OimOrderDetails oimOrderDetails) throws ChannelCommunicationException {

    // POST /admin/orders/#{id}/close.json
    String requestCloseUrl = storeUrl + "/admin/orders/"
        + oimOrderDetails.getOimOrders().getStoreOrderId() + "/close.json";
    HttpClient client = new HttpClient();
    PostMethod postMethod = new PostMethod(requestCloseUrl);
    postMethod.addRequestHeader("X-Shopify-Access-Token", shopifyToken);
    JSONObject orderCloseObject = new JSONObject();

    StringRequestEntity requestEntity = null;
    try {
      requestEntity = new StringRequestEntity(orderCloseObject.toJSONString(), "application/json",
          "UTF-8");
    } catch (UnsupportedEncodingException e) {
      log.error("error in parsing json request for order close {}", e);
    }
    postMethod.setRequestEntity(requestEntity);
    int statusCode = 0;
    try {
      statusCode = client.executeMethod(postMethod);
      log.info("fullfilment statusCode is - {}", statusCode);

    } catch (HttpException e) {
      log.error("error in posting request for fullfillment", e);
      throw new ChannelCommunicationException(
          "Error in posting request for fullfillment for store order id "
              + oimOrderDetails.getOimOrders().getStoreOrderId(),
          e);
      // return false;
    } catch (IOException e) {
      log.error("error in parsing json response payload {}", e);
      throw new ChannelCommunicationException(
          "Error in posting request for fullfillment for store order id "
              + oimOrderDetails.getOimOrders().getStoreOrderId(),
          e);
    }
    return true;
  }

  @Override
  public void cancelOrder(OimOrders oimOrder) throws ChannelCommunicationException,
      ChannelOrderFormatException, ChannelConfigurationException {
    // POST /admin/orders/#{id}/close.json
    String requestCloseUrl = storeUrl + "/admin/orders/" + oimOrder.getStoreOrderId()
        + "/cancel.json";
    JSONObject orderCancelObject = new JSONObject();
    orderCancelObject.put("reason", "inventory");
    orderCancelObject.put("email", true);

    StringRequestEntity requestEntity = null;
    try {
      requestEntity = new StringRequestEntity(orderCancelObject.toJSONString(), "application/json",
          "UTF-8");
    } catch (UnsupportedEncodingException e) {
      log.error("Error in parsing json request for order cancellation {}", e);
    }
    this.noOfApiRequests = 0;
    sendRequestOAuth(orderCancelObject.toString(), requestCloseUrl, POST_REQUEST_METHOD,oimOrder.getStoreOrderId());
  }

  private String sendRequestOAuth(String data, String requestUrl, String requestMethod, String storeOrderId)
      throws ChannelConfigurationException, ChannelCommunicationException,
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
      connection.setRequestProperty("X-Shopify-Access-Token", this.shopifyToken);
      connection.setRequestProperty("Content-type", "application/json");

      if (data != null && (requestMethod.equalsIgnoreCase(POST_REQUEST_METHOD)
          || requestMethod.equalsIgnoreCase(PUT_REQUEST_METHOD))) {
        byte[] req = data.getBytes();
        OutputStream out = connection.getOutputStream();
        out.write(req);
        out.close();
      }
      connection.connect();
      String limit = connection.getHeaderField("HTTP_X_SHOPIFY_SHOP_API_CALL_LIMIT");
      if (limit != null && !limit.equalsIgnoreCase("")) {
        String[] limitArr = limit.split("/");
        int currentValue = Integer.parseInt(limitArr[0]);
        if (currentValue > 36) {
          log.info(" About to reach API call limit - " + limit);
          log.info(" Pausing for 10 seconds.. ");
          Thread.sleep(10 * 1000);
        }
      }
      responseCode = connection.getResponseCode();
      if (responseCode == 200) {
        response = getStringFromStream(connection.getInputStream());
      } else if (responseCode == 201 && requestMethod.equalsIgnoreCase(POST_REQUEST_METHOD)) {
        response = getStringFromStream(connection.getInputStream());
      } else if (responseCode == 429) {
        noOfApiRequests++;
        connection.disconnect();
        if (noOfApiRequests < 5) {
          return sendRequestOAuth(data, requestUrl, requestMethod,storeOrderId);
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
      } 
      else if (responseCode == 422) {
        if(storeOrderId!=null)
          log.error("422 - There was a problem with the body of your Request. Inspect the response body for the errors for store order id - {}",storeOrderId);
        log.error("422 - There was a problem with the body of your Request. Inspect the response body for the errors");
      }
      else if (responseCode == 403) {
        if(storeOrderId!=null)
        throw new ChannelConfigurationException(
            "Error occured for store order id - "+storeOrderId+" response code 403 - Forbidden access - verify app OAuth scopes");
        throw new ChannelConfigurationException(
            "response code 403 - Forbidden access - verify app OAuth scopes");
      } else if (String.valueOf(responseCode).startsWith("5")) {
        throw new ChannelCommunicationException(
            "500 series - Either Shopify is down or you sent something that caused our code to error out.");
      } else
        throw new ChannelCommunicationException("response code - " + responseCode);
    } catch (InterruptedException e) {
      throw new ChannelCommunicationException("Interrupted waiting for API bandwidth");
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
  public void cancelOrder(OimOrderDetails oimOrder) throws ChannelOrderFormatException {
    throw new ChannelOrderFormatException("Store does not allow partial cancellatoins.");
  }

}
