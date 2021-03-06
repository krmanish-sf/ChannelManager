package salesmachine.oim.stores.impl;

import static salesmachine.util.StringHandle.removeNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.oim.suppliers.modal.TrackingData;
import salesmachine.util.ApplicationProperties;

public class BigcommerceOrderImport extends ChannelBase {

  private static final Logger log = LoggerFactory.getLogger(BigcommerceOrderImport.class);
  private static String clientID;
  private static String apiUrl;
  private String authToken;
  private String storeID;
  private static SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
  private static HashMap<String, Integer> orderStatusIDMap = new HashMap<String, Integer>(0);
  private static String storeBaseURL = "";
  private String confirmedOrderStatusJSON;
  private String cancelledOrderStatus;
  private Integer pullStatusID = null;
  private static final String GET_METHOD_TYPE = "GET";
  private static final String PUT_METHOD_TYPE = "PUT";
  private static final String POST_METHOD_TYPE = "POST";

  static {
    apiUrl = ApplicationProperties.getProperty(ApplicationProperties.BIGCOMMERCE_CLIENT_API_URL);
    clientID = ApplicationProperties.getProperty(ApplicationProperties.BIGCOMMERCE_CLIENT_ID);
    orderStatusIDMap.put("Incomplete", 0);
    orderStatusIDMap.put("Pending", 1);
    orderStatusIDMap.put("Shipped", 2);
    orderStatusIDMap.put("Partially Shipped", 3);
    orderStatusIDMap.put("Refunded", 4);
    orderStatusIDMap.put("Cancelled", 5);
    orderStatusIDMap.put("Declined", 6);
    orderStatusIDMap.put("Awaiting Payment", 7);
    orderStatusIDMap.put("Awaiting Pickup", 8);
    orderStatusIDMap.put("Awaiting Shipment", 9);
    orderStatusIDMap.put("Completed", 10);
    orderStatusIDMap.put("Awaiting Fulfillment", 11);
    orderStatusIDMap.put("Manual Verification Required", 12);
    orderStatusIDMap.put("Disputed", 13);
  }

  @Override
  public boolean init(OimChannels oimChannel, Session dbSession)
      throws ChannelConfigurationException {
    super.init(oimChannel, dbSession);
    authToken = removeNull(PojoHelper.getChannelAccessDetailValue(m_channel,
        OimConstants.CHANNEL_ACCESSDETAIL_AUTH_KEY));
    storeID = removeNull(PojoHelper.getChannelAccessDetailValue(m_channel,
        OimConstants.CHANNEL_ACCESSDETAIL_BIGCOMMERCE_STORE_ID));

    if (authToken.length() == 0 || storeID.length() == 0) {
      log.error("Channel setup is not correct. Please provide correct details.");
      return false;
    }
    storeBaseURL = apiUrl + storeID + "/v2";
    pullStatusID = orderStatusIDMap.get(m_orderProcessingRule.getPullWithStatus());
    if (pullStatusID == null) {
      throw new ChannelConfigurationException(
          "Error in channel Setup : Orders To Pull From Channel not correctly configured");
    }
    Integer orderConfirmationID = orderStatusIDMap.get(m_orderProcessingRule.getConfirmedStatus());
    if (orderConfirmationID == null) {
      throw new ChannelConfigurationException(
          "Error in channel Setup : Check '[Order Status When imported to Channel Manager] in channel setup step 3'");
    }
    JSONObject orderStatusJSON = new JSONObject();
    orderStatusJSON.put("status_id", orderConfirmationID);
    confirmedOrderStatusJSON = orderStatusJSON.toJSONString();

    Integer orderCanceledStatusId = orderStatusIDMap.get(m_orderProcessingRule.getFailedStatus());
    if (orderCanceledStatusId == null) {
      throw new ChannelConfigurationException(
          "Error in channel Setup : Check '[Order Status When Failed] in channel setup step 3'");
    }
    JSONObject cancelOrderStatusJSON = new JSONObject();
    cancelOrderStatusJSON.put("status_id", orderCanceledStatusId);
    cancelledOrderStatus = cancelOrderStatusJSON.toJSONString();

    return true;
  }

  private String sendRequest(String data, String requestUrl, String requestMethod)
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
      connection.setRequestProperty("X-Auth-Client", clientID);
      connection.setRequestProperty("X-Auth-Token", authToken);
      connection.setRequestProperty("Accept", "*/*");
      connection.setRequestProperty("Content-type", "application/json");
      connection.setRequestProperty("Content-Language", "en-US");
      connection.setRequestProperty("User-Agent", "BC API Client/1.0");
      connection.setDoOutput(true);
      if (data != null && (requestMethod.equalsIgnoreCase(POST_METHOD_TYPE)
          || requestMethod.equalsIgnoreCase(PUT_METHOD_TYPE))) {
        byte[] req = data.getBytes();
        OutputStream out = connection.getOutputStream();
        out.write(req);
        out.close();
      }
      connection.connect();
      responseCode = connection.getResponseCode();
      if (responseCode == 200 || responseCode == 201) {
        response = getStringFromStream(connection.getInputStream());
      } else if (responseCode == 204) {
        log.info("Request Successful, but no response, cause there may be no data");
        return response;
      } else if (responseCode == 429) {
        connection.disconnect();
        int waitTime = Integer.parseInt(connection.getHeaderField("X-Retry-After"));
        log.info("API rate limit exceeded, waiting for " + waitTime + " seconds");
        Thread.sleep(waitTime * 1000);
        return sendRequest(data, requestUrl, requestMethod);
      } else if (responseCode == 400) {
        throw new ChannelConfigurationException(
            "API returned response code 400 - probably a malformed url - " + requestUrl);
      } else if (responseCode == 401) {
        throw new ChannelConfigurationException(
            "inavild credentials - verfiy channel setup/auth-token");
      } else if (responseCode == 403) {
        throw new ChannelCommunicationException("verify app OAuth scopes");
      } else if (responseCode == 500) {
        throw new ChannelCommunicationException("error occured within the API");
      } else if (responseCode == 503) {
        throw new ChannelCommunicationException("Store is down for maintenance");
      } else {
        throw new ChannelCommunicationException(
            "Unable to pull Order Data from BigCommerce, got response code " + responseCode);
      }
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

  @Deprecated
  private boolean updateBigCommerceOrderData(String data, String requestUrl, String requestMethod)
      throws ChannelConfigurationException, ChannelCommunicationException,
      ChannelOrderFormatException {
    boolean status = false;
    HttpsURLConnection connection = null;
    URL url;
    int responseCode = 0;
    try {
      byte[] req = data.getBytes();
      url = new URL(requestUrl);
      connection = (HttpsURLConnection) url.openConnection();
      connection.setRequestMethod(requestMethod);
      connection.setRequestProperty("X-Auth-Client", clientID);
      connection.setRequestProperty("X-Auth-Token", authToken);
      connection.setRequestProperty("Content-Language", "en-US");
      connection.setRequestProperty("User-Agent", "BC API Client/1.0");
      connection.setRequestProperty("Accept", "*/*");
      connection.setDoOutput(true);
      OutputStream out = connection.getOutputStream();
      out.write(req);
      out.close();
      connection.connect();
      responseCode = connection.getResponseCode();
      if (responseCode == 200) {
        status = true;
      } else if (responseCode == 201) { // content posted successfully
        status = true;
      } else if (responseCode == 429) {
        int waitTime = Integer.parseInt(connection.getHeaderField("X-Retry-After"));
        log.info("API rate limit exceeded, waiting for " + waitTime + " seconds");
        connection.disconnect();
        Thread.sleep(waitTime * 1000);
        updateBigCommerceOrderData(data, requestUrl, requestMethod);
      } else if (responseCode == 400) {
        throw new ChannelConfigurationException(
            "API returned response code 400 - probably a malformed url - " + requestUrl);
      } else if (responseCode == 401) {
        throw new ChannelConfigurationException(
            "inavild credentials - verfiy channel setup/auth-token");
      } else if (responseCode == 403) {
        throw new ChannelCommunicationException("verify app OAuth scopes");
      } else if (responseCode == 500) {
        throw new ChannelCommunicationException("error occured within the API");
      } else if (responseCode == 503) {
        throw new ChannelCommunicationException("Store is down for maintenance");
      } else {
        throw new ChannelCommunicationException(
            "failed to update Bigcommerce order status, API returned response code - "
                + responseCode);
      }
    } catch (InterruptedException e) {
      throw new ChannelCommunicationException("Interrupted waiting for API bandwidth");
    } catch (MalformedURLException e) {
      throw new ChannelConfigurationException("MalformedURLException - " + requestUrl);
    } catch (IOException e) {
      throw new ChannelCommunicationException(e.getMessage());
    } finally {
      connection.disconnect();
    }
    return status;
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
    JSONParser parser = new JSONParser();
    int totalOrders = 0;
    int batchPullCount = 0;
    int page = 1;
    try {
      do {
        batchPullCount = 0;
        String requestURL = storeBaseURL + "/orders.json?status_id=" + pullStatusID
            + "&limit=250&page=" + page++;
        String responseOrders = sendRequest(null, requestURL, GET_METHOD_TYPE);
        if (responseOrders == null || responseOrders.equalsIgnoreCase("null")) {
          log.info("No orders found to be pulled " + m_channel.getChannelId());
          return;
        }
        JSONArray orderJsonArray = (JSONArray) parser.parse(responseOrders);
        batchPullCount = orderJsonArray.size();
        totalOrders += batchPullCount;
        String storeOrderId = null;
        for (int i = 0; i < orderJsonArray.size(); i++) {
          try {
           
            JSONObject orderJsonObj = (JSONObject) orderJsonArray.get(i);
            storeOrderId = orderJsonObj.get("id").toString();
            if (orderAlreadyImported(storeOrderId)) {
              log.info("Order#{} is already imported in the system, updating Order.", storeOrderId);
              continue;
            }
            if (tx != null && tx.isActive())
              tx = m_dbSession.getTransaction();
            else
              tx = m_dbSession.beginTransaction();
            OimOrders oimOrders = new OimOrders();
            oimOrders.setStoreOrderId(storeOrderId);
            String customer_id = removeNull(orderJsonObj.get("customer_id"));
            String customerDataUrl = storeBaseURL + "/customers.json?min_id=" + customer_id
                + "&max_id=" + customer_id;
            JSONArray customers = (JSONArray) parser
                .parse(sendRequest(null, customerDataUrl, GET_METHOD_TYPE));
            JSONObject customer = (JSONObject) customers.get(0);
            oimOrders.setCustomerName(removeNull(
                customer.get("first_name") + " " + removeNull(customer.get("last_name"))));
            oimOrders.setCustomerEmail(removeNull(customer.get("email")));
            oimOrders.setCustomerPhone(removeNull(customer.get("phone")));
            JSONObject billingObj = (JSONObject) orderJsonObj.get("billing_address");
            if (billingObj != null) {
              oimOrders.setBillingName(removeNull(billingObj.get("first_name")) + " "
                  + removeNull(billingObj.get("last_name")));
              oimOrders.setBillingCompany(removeNull(billingObj.get("company")));
              oimOrders.setBillingStreetAddress(removeNull(billingObj.get("street_1")));
              oimOrders.setBillingSuburb(removeNull(billingObj.get("street_2")));
              oimOrders.setBillingCity(removeNull(billingObj.get("city")));
              oimOrders.setBillingState(removeNull(billingObj.get("state")));
              oimOrders.setBillingZip(removeNull(billingObj.get("zip")));
              oimOrders.setBillingCountry(removeNull(billingObj.get("country")));
              oimOrders.setBillingPhone(removeNull(billingObj.get("phone")));
              oimOrders.setBillingEmail(removeNull(billingObj.get("email")));

              // set customer address same as billing address
              oimOrders.setCustomerCompany(removeNull(billingObj.get("company")));
              oimOrders.setCustomerStreetAddress(removeNull(billingObj.get("street_1")));
              oimOrders.setCustomerSuburb(removeNull(billingObj.get("street_2")));
              oimOrders.setCustomerCity(removeNull(billingObj.get("city")));
              oimOrders.setCustomerState(removeNull(billingObj.get("state")));
              oimOrders.setCustomerZip(removeNull(billingObj.get("zip")));
              oimOrders.setCustomerCountry(removeNull(billingObj.get("country")));
            }
            String shippingAddressesUrl = (String) ((JSONObject) orderJsonObj
                .get("shipping_addresses")).get("resource");
            shippingAddressesUrl = storeBaseURL + shippingAddressesUrl + ".json";
            JSONArray shippingAddresses = (JSONArray) parser
                .parse(sendRequest(null, shippingAddressesUrl, GET_METHOD_TYPE));
            String shipMethod = null;
            if (shippingAddresses.size() == 1) {
              JSONObject shippingAddObj = (JSONObject) shippingAddresses.get(0);
              oimOrders.setDeliveryName(removeNull(shippingAddObj.get("first_name") + " "
                  + removeNull(shippingAddObj.get("last_name"))));
              oimOrders.setDeliveryCompany(removeNull(shippingAddObj.get("company")));
              oimOrders.setDeliveryStreetAddress(removeNull(shippingAddObj.get("street_1")));
              oimOrders.setDeliverySuburb(removeNull(shippingAddObj.get("street_2")));
              oimOrders.setDeliveryCity(removeNull(shippingAddObj.get("city")));
              oimOrders.setDeliveryZip(removeNull(shippingAddObj.get("zip")));
              oimOrders.setDeliveryCountry(removeNull(shippingAddObj.get("country")));
              oimOrders.setDeliveryState(removeNull(shippingAddObj.get("state")));
              oimOrders.setDeliveryStateCode(validateAndGetStateCode(oimOrders));
              oimOrders.setDeliveryEmail(removeNull(shippingAddObj.get("email")));
              oimOrders.setDeliveryPhone(removeNull(shippingAddObj.get("phone")));
              shipMethod = removeNull(shippingAddObj.get("shipping_method"));
              oimOrders.setShippingDetails(shipMethod);
            }
            
            String orderCreatedTm = removeNull(orderJsonObj.get("date_created"));
            Date order_tm = null;
            try {
              order_tm = sdf.parse(orderCreatedTm);
            } catch (java.text.ParseException e) {
              throw new ChannelOrderFormatException(
                  "Verify Bigcommerce API date format" + e.getMessage());
            }
            oimOrders.setOrderTm(order_tm);
            oimOrders.setOrderTotalAmount(
                Double.parseDouble(removeNull(orderJsonObj.get("total_inc_tax"))));
            oimOrders.setPayMethod(removeNull(orderJsonObj.get("payment_method")));
            for (OimChannelShippingMap entity : oimChannelShippingMapList) {
              String shippingRegEx = entity.getShippingRegEx();
              if (shipMethod.equalsIgnoreCase(shippingRegEx)) {
                oimOrders.setOimShippingMethod(entity.getOimShippingMethod());
                log.info("Shipping set to " + entity.getOimShippingMethod());
                break;
              }
            }
            if (oimOrders.getOimShippingMethod() == null)
              log.warn("Shipping can't be mapped for order " + oimOrders.getStoreOrderId());

            String product_resource = removeNull(
                ((JSONObject) orderJsonObj.get("products")).get("resource"));
            String product_resource_url = storeBaseURL + product_resource + ".json";
            Set<OimOrderDetails> detailSet = new HashSet<OimOrderDetails>();
            JSONArray orderItems = (JSONArray) parser
                .parse(sendRequest(null, product_resource_url, GET_METHOD_TYPE));
            Set<String> notMatchingSkuSet = new HashSet<String>();
            for (int x = 0; x < orderItems.size(); x++) {
              JSONObject orderItem = (JSONObject) orderItems.get(x);
              OimOrderDetails details = new OimOrderDetails();
              details
                  .setCostPrice(Double.parseDouble(removeNull(orderItem.get("base_cost_price"))));
              details.setInsertionTm(new Date());
              details
                  .setOimOrderStatuses(new OimOrderStatuses(OimConstants.ORDER_STATUS_UNPROCESSED));
              String sku = removeNull(orderItem.get("sku")).toUpperCase();
              if (m_channel.getOnlyPullMatchingOrders() == 1) {
                if (!isProductMatchesWithSupplier(sku)) {
                  notMatchingSkuSet.add(sku);
                }
              }
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
              // setting order_address_id in product description to be
              // used later while updating order.
              details.setProductDesc(removeNull(orderItem.get("order_address_id")));
              details.setProductName(removeNull(orderItem.get("name")));
              details.setQuantity(Integer.parseInt(removeNull(orderItem.get("quantity"))));
              details.setSalePrice(Double.parseDouble(removeNull(orderItem.get("base_price"))));
              details.setSku(sku);
              details.setStoreOrderItemId(removeNull(orderItem.get("id")));
              details.setOimOrders(oimOrders);
             // m_dbSession.save(details);
              detailSet.add(details);
            }
            if (orderItems.size() == notMatchingSkuSet.size()) {
              StringBuffer sb = new StringBuffer();
              for (Iterator<String> it = notMatchingSkuSet.iterator(); it.hasNext();) {
                String sku = it.next();
                sb.append(sku);
                if (it.hasNext())
                  sb.append(",");
              }
              log.info(
                  "order id {} skipped because it has all the skus which are not starting with any of the configured supplier's prefix - {}",
                  storeOrderId, sb.toString());
              continue;
            }
            for (Iterator<OimOrderDetails> dtl = detailSet.iterator(); dtl.hasNext();) {
              OimOrderDetails detailToSave = dtl.next();
              m_dbSession.saveOrUpdate(detailToSave);
            }
            oimOrders.setInsertionTm(new Date());
            oimOrders.setOimOrderBatches(batch);
            batch.getOimOrderses().add(oimOrders);
            oimOrders.setOrderFetchTm(new Date());
            m_dbSession.saveOrUpdate(oimOrders);
            // Check if store is not in test mode.
            if (m_channel.getTestMode() == 0) {
              // update order status on store to acknowledge order has been received by CM
              String orderStatusUpdateUrl = storeBaseURL + "/orders/" + storeOrderId;
              sendRequest(confirmedOrderStatusJSON, orderStatusUpdateUrl, PUT_METHOD_TYPE);
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
          }
        }
      } while (batchPullCount == 250);

    } catch (org.json.simple.parser.ParseException e) {
      throw new ChannelOrderFormatException(e.getMessage());
    }
    log.info("Fetched {} order(s)", totalOrders);
    try {
      tx = m_dbSession.beginTransaction();
      m_channel.setLastFetchTm(new Date());
      tx.commit();
    } catch (HibernateException e) {
      tx.rollback();
    }
    log.debug("Finished importing orders...");
    log.info("Returning Order batch with size: {}", batch.getOimOrderses().size());
  }

  @Override
  public void updateStoreOrder(OimOrderDetails oimOrderDetails, OrderStatus orderStatus)
      throws ChannelCommunicationException, ChannelOrderFormatException,
      ChannelConfigurationException {
    log.info("order id is - {}", oimOrderDetails.getOimOrders().getOrderId());
    log.info("order status is - {}", orderStatus);

    if (!orderStatus.isShipped()) {
      return;
    }
    String addShipmentURL = storeBaseURL + "/orders/"
        + oimOrderDetails.getOimOrders().getStoreOrderId() + "/shipments";
    for (TrackingData trackingData : orderStatus.getTrackingData()) {
      JSONObject shipmentJSON = new JSONObject();
      shipmentJSON.put("tracking_number", trackingData.getShipperTrackingNumber());
      shipmentJSON.put("order_address_id", oimOrderDetails.getProductDesc());
      shipmentJSON.put("comments", "carrier_name : " + trackingData.getCarrierName());
      JSONArray items = new JSONArray();
      JSONObject item = new JSONObject();
      item.put("order_product_id", oimOrderDetails.getStoreOrderItemId());
      item.put("quantity", trackingData.getQuantity());
      items.add(item);
      shipmentJSON.put("items", items);
      sendRequest(shipmentJSON.toString(), addShipmentURL, POST_METHOD_TYPE);
    }
    return;
  }


  @Override
  public void cancelOrder(OimOrders oimOrder) throws ChannelOrderFormatException,
      ChannelConfigurationException, ChannelCommunicationException {
    String requestUrl = storeBaseURL + "/orders/" + oimOrder.getStoreOrderId();
    sendRequest(cancelledOrderStatus, requestUrl, PUT_METHOD_TYPE);
  }

  @Override
  public void cancelOrder(OimOrderDetails oimOrder) throws ChannelOrderFormatException {
    throw new ChannelOrderFormatException("Store does not allow partial order cancellatoins.");
  }

}
