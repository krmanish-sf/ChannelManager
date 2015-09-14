package salesmachine.oim.stores.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
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
      throws ChannelCommunicationException, ChannelOrderFormatException {
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

    HttpClient client = new HttpClient();
    PostMethod postMethod = new PostMethod(requestUrl);
    postMethod.addRequestHeader("X-Shopify-Access-Token", shopifyToken);
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
    jsonObject.put("fulfillments", jsonObjVal);
    jsonObject.put("line_items", lineItemArray);
    StringRequestEntity requestEntity = null;
    try {
      requestEntity = new StringRequestEntity(jsonObject.toJSONString(), "application/json",
          "UTF-8");
    } catch (UnsupportedEncodingException e) {
      log.error("Error in parsing tracking request json {}", e);
      throw new ChannelOrderFormatException("Error in parsing tracking request json", e);
    }
    postMethod.setRequestEntity(requestEntity);
    int statusCode = 0;
    try {
      statusCode = client.executeMethod(postMethod);
      log.info("fullfilment statusCode is - {}", statusCode);

    } catch (HttpException e) {
      log.error("error in posting request for fullfillment {}", e);
      throw new ChannelCommunicationException("Error in posting request for fullfillment", e);
      // return false;
    } catch (IOException e) {
      log.error("error in parsing json response payload {}", e);
      throw new ChannelCommunicationException("Error in parsing json response payload", e);
      // return false;
    }

    // closing the order
    if (statusCode == 200 || statusCode == 201) {
      // send acknoledgement that order has been processed.
      requestUrl = storeUrl + "/admin/orders/" + oimOrderDetails.getOimOrders().getStoreOrderId()
          + ".json";
      if (sendAcknowledgementToStore(requestUrl, oimOrderDetails.getOimOrders().getStoreOrderId(),
          m_orderProcessingRule.getProcessedStatus(), true, orderStatus))
        closeOrder(oimOrderDetails);
    }
  }

  @Override
  public void getVendorOrders(OimOrderBatchesTypes batchesTypes, OimOrderBatches batch)
      throws ChannelCommunicationException, ChannelOrderFormatException,
      ChannelConfigurationException {
    // on the basis of access token, we will pull orders from vendors
    // OimOrderBatches batch = new OimOrderBatches();
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
    HttpClient client = new HttpClient();
    String requestUrl = storeUrl + "/admin/orders.json";
    Date lstFetchTime = m_channel.getLastFetchTm();
    // FIXME API didn't respond as per the specification, still getting all
    // the orders.
    if (lstFetchTime != null) {
      SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd hh:mm");
      log.info("Cutoff time {} ", lstFetchTime);
      try {
        requestUrl += "?" + URLEncoder.encode("created_at_min=" + df.format(lstFetchTime), "UTF-8");
      } catch (UnsupportedEncodingException e1) {
        log.warn("Encoding type [UTF-8] is invalid");
        throw new ChannelConfigurationException("Encoding type [UTF-8] is invalid", e1);
      }
    }
    String jsonString = null;
    GetMethod getOrderJson = new GetMethod(requestUrl);
    getOrderJson.addRequestHeader("X-Shopify-Access-Token", shopifyToken);
    tx = m_dbSession.beginTransaction();
    int responseCode = 0;
    try {
      responseCode = client.executeMethod(getOrderJson);
    } catch (IOException e) {
      log.error("Unable to get response from shopify. Please check the store url and access token");
      log.error(e.getMessage(), e);
      throw new ChannelConfigurationException(
          "Unable to get response from shopify. Please check the store url and access token "
              + e.getMessage(),
          e);
      // return null;
    }

    if (responseCode == 200) {
      try {
        jsonString = getOrderJson.getResponseBodyAsString();
        log.info("order json --- {}", jsonString);
      } catch (IOException e) {
        log.error(e.getMessage(), e);
        throw new ChannelCommunicationException(e.getMessage(), e);

      }
      JSONObject jsonObject = null;
      JSONParser parser = new JSONParser();
      try {
        jsonObject = (JSONObject) parser.parse(jsonString);
      } catch (ParseException e) {
        e.printStackTrace();
      }
      if (jsonObject == null)
        throw new ChannelCommunicationException(
            "Error in parsing response string for order pulling");

      JSONArray orderArr = (JSONArray) jsonObject.get("orders");

      for (int i = 0; i < orderArr.size(); i++) {
        JSONObject orderObj = (JSONObject) orderArr.get(i);
        String storeOrderId = orderObj.get("id").toString();
        if (orderAlreadyImported(storeOrderId)) {
          log.info("Order#{} is already imported in the system, updating Order.", storeOrderId);
          continue;
        }
        OimOrders oimOrders = new OimOrders();
        oimOrders.setStoreOrderId(storeOrderId);
        // setting billing information
        JSONObject billingObj = (JSONObject) orderObj.get("billing_address");
        if (billingObj != null) {
          oimOrders.setBillingStreetAddress(
              StringHandle.removeNull((String) billingObj.get("address1")));
          oimOrders.setBillingSuburb(StringHandle.removeNull((String) billingObj.get("address2")));

          oimOrders.setBillingZip(StringHandle.removeNull((String) billingObj.get("zip")));
          oimOrders.setBillingCity(StringHandle.removeNull((String) billingObj.get("city")));
          oimOrders.setBillingCompany(StringHandle.removeNull((String) billingObj.get("company")));
          oimOrders.setBillingCountry(StringHandle.removeNull((String) billingObj.get("country")));
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
          oimOrders.setDeliveryName(StringHandle.removeNull((String) deliveryObj.get("first_name"))
              + " " + StringHandle.removeNull((String) deliveryObj.get("last_name")));
          oimOrders.setDeliveryPhone(StringHandle.removeNull((String) deliveryObj.get("phone")));
          oimOrders.setDeliveryStreetAddress(
              StringHandle.removeNull((String) deliveryObj.get("address1")));
          oimOrders
              .setDeliverySuburb(StringHandle.removeNull((String) deliveryObj.get("address2")));
          oimOrders.setDeliveryZip(StringHandle.removeNull((String) deliveryObj.get("zip")));
          oimOrders.setDeliveryState(StringHandle.removeNull((String) deliveryObj.get("province")));
          // oimOrders.setDeliveryStateCode(StringHandle
          // .removeNull((String) deliveryObj.get("province_code")));

          if (((String) deliveryObj.get("province")).length() == 2) {
            oimOrders.setDeliveryStateCode((String) deliveryObj.get("province"));
          } else {
            String stateCode = validateAndGetStateCode(oimOrders);
            if (stateCode != "")
              oimOrders.setDeliveryStateCode(stateCode);
          }
        }
        // setting customer information
        JSONObject custInfo = (JSONObject) orderObj.get("customer");
        oimOrders.setCustomerEmail(StringHandle.removeNull((String) custInfo.get("email")));
        if (custInfo != null) {
          JSONObject customerObj = (JSONObject) custInfo.get("default_address");
          if (customerObj != null) {
            oimOrders.setCustomerCity(StringHandle.removeNull((String) customerObj.get("city")));
            oimOrders
                .setCustomerCompany(StringHandle.removeNull((String) customerObj.get("company")));
            oimOrders
                .setCustomerCountry(StringHandle.removeNull((String) customerObj.get("country")));
            oimOrders.setCustomerName(StringHandle.removeNull((String) customerObj.get("name")));
            oimOrders.setCustomerPhone(StringHandle.removeNull((String) customerObj.get("phone")));
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
        String shippingDetails = (String) ((JSONObject) ((JSONArray) orderObj.get("shipping_lines"))
            .get(0)).get("title");
        oimOrders.setShippingDetails(shippingDetails);
        for (OimChannelShippingMap entity : oimChannelShippingMapList) {
          String shippingRegEx = entity.getShippingRegEx();
          if (shippingDetails.equalsIgnoreCase(shippingRegEx)) {
            oimOrders.setOimShippingMethod(entity.getOimShippingMethod());
            log.info("Shipping set to " + entity.getOimShippingMethod());
            break;
          }
        }

        if (oimOrders.getOimShippingMethod() == null)
          log.warn("Shipping can't be mapped for order " + oimOrders.getStoreOrderId());

        m_dbSession.saveOrUpdate(oimOrders);
        // setting product information
        JSONArray itemArray = (JSONArray) orderObj.get("line_items");
        Set<OimOrderDetails> detailSet = new HashSet<OimOrderDetails>();
        for (int j = 0; j < itemArray.size(); j++) {
          OimOrderDetails details = new OimOrderDetails();
          JSONObject item = (JSONObject) itemArray.get(j);

          details.setCostPrice(
              Double.parseDouble(StringHandle.removeNull((String) item.get("price"))));
          details.setInsertionTm(new Date());
          details.setOimOrderStatuses(new OimOrderStatuses(OimConstants.ORDER_STATUS_UNPROCESSED));
          String sku = (String) item.get("sku");
          OimSuppliers oimSuppliers = null;
          for (String prefix : supplierMap.keySet()) {
            if (sku.toUpperCase().startsWith(prefix)) {
              oimSuppliers = supplierMap.get(prefix);
              break;
            }
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

          m_dbSession.save(details);
          detailSet.add(details);
        }
        oimOrders.setOimOrderDetailses(detailSet);
        m_dbSession.saveOrUpdate(oimOrders);

        // sending acknowledgement to shopify that we recived the order.
        requestUrl = storeUrl + "/admin/orders/" + storeOrderId + ".json"; // 704264451
        sendAcknowledgementToStore(requestUrl, storeOrderId,
            m_orderProcessingRule.getConfirmedStatus(), false, null);
      }
      log.info("Fetched {} order(s)", orderArr.size());
      m_channel.setLastFetchTm(new Date());
      m_dbSession.persist(m_channel);
      tx.commit();
      log.debug("Finished importing orders...");
    } else {
      log.error("Got response code {} .Please check the request parameters", responseCode);
      throw new ChannelConfigurationException(
          "Please check the request parameters. Got response code - " + responseCode);

    }
    log.info("Returning Order batch with size: {}", batch.getOimOrderses().size());
  }

  private boolean sendAcknowledgementToStore(String requestUrl, String storeOrderId, String status,
      boolean isSendTrackingDetails, OrderStatus orderStatus)
          throws ChannelCommunicationException, ChannelOrderFormatException {
    HttpClient httpclient = new HttpClient();
    PutMethod postMethod = new PutMethod(requestUrl);
    postMethod.addRequestHeader("X-Shopify-Access-Token", shopifyToken);
    JSONObject jObject = new JSONObject();
    JSONObject jsonObjVal = new JSONObject();
    JSONArray attributeArray = new JSONArray();
    JSONObject attributeObj1 = new JSONObject();
    jsonObjVal.put("id", storeOrderId);
    attributeObj1.put("name", "Order Acknowledgement");
    attributeObj1.put("value", status);
    attributeArray.add(attributeObj1);
    if (isSendTrackingDetails) {
      JSONObject attributeObj2 = new JSONObject();
      attributeObj2.put("name", "Tracking Status");
      StringBuilder trackingDetail = new StringBuilder();
      for (TrackingData td : orderStatus.getTrackingData()) {
        trackingDetail.append(td.toString());
        trackingDetail.append("\n");
      }
      attributeObj2.put("value", trackingDetail.toString());
      attributeArray.add(attributeObj2);
    }
    jsonObjVal.put("note_attributes", attributeArray);
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
    postMethod.setRequestEntity(requestEntity);
    int statusCode = 0;
    try {
      statusCode = httpclient.executeMethod(postMethod);
      log.info("acknowledgement/tracking statusCode is - {}", statusCode);
      String responseString = postMethod.getResponseBodyAsString();
      log.info("acknowledgement/tracking response string - {}", responseString);
      log.info("acknowledgement/tracking sent to store.");

    } catch (HttpException e) {
      log.error("error in posting acknowledgement/tracking {}", e);
      throw new ChannelCommunicationException("Error in posting acknowledgement/tracking ", e);
    } catch (IOException e) {
      log.error("error in parsing json response payload {}", e);
      throw new ChannelCommunicationException("Error in posting acknowledgement/tracking ", e);
      // return false;
    }
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
      log.error("error in posting request for fullfillment {}", e);
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
  public void cancelOrder(OimOrders oimOrder) throws ChannelCommunicationException {

    // POST /admin/orders/#{id}/close.json
    String requestCloseUrl = storeUrl + "/admin/orders/" + oimOrder.getStoreOrderId()
        + "/cancel.json";
    HttpClient client = new HttpClient();
    PostMethod postMethod = new PostMethod(requestCloseUrl);
    postMethod.addRequestHeader("X-Shopify-Access-Token", shopifyToken);
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
    postMethod.setRequestEntity(requestEntity);
    int statusCode = 0;
    try {
      statusCode = client.executeMethod(postMethod);
      log.info("Fullfilment statusCode is - {}", statusCode);

    } catch (IOException e) {
      log.error("error in posting request for fullfillment {}", e);
      throw new ChannelCommunicationException(
          "Error in posting request for fullfillment for store order id "
              + oimOrder.getStoreOrderId(),
          e);
      // return false;
    }
  }

  @Override
  public void cancelOrder(OimOrderDetails oimOrder) throws ChannelOrderFormatException {
    throw new ChannelOrderFormatException("Store does not allow partial cancellatoins.");
  }

}
