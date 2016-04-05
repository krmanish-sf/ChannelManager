package salesmachine.oim.stores.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import salesmachine.hibernatedb.OimShippingMethod;
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
import salesmachine.util.ApplicationProperties;
import salesmachine.util.DevhubShippingMapping;
import salesmachine.util.OAuthProvider;
import salesmachine.util.StringHandle;
import salesmachine.util.VolusionShippingCodeMapWithIS;

public class DevHubOrderImport extends ChannelBase implements IOrderImport {

  private static final Logger log = LoggerFactory.getLogger(DevHubOrderImport.class);
  private String storeUrl;
  // private String siteID;
  private String clientId;
  private String secrateKey;
  private static final String GET_REQUEST_METHOD = "GET";
  private static final String PUT_REQUEST_METHOD = "PUT";
  private static final String SPACE = " ";
  private int noOfApiRequests = 0;
  private static final int limit = 10;
  private static final String HOST = "ibuyrite.cloudfrontend.net";
  private static final String PATH = "/api/v2/shoporders/";

  // End Point -- http://ibuyrite.cloudfrontend.net/api/v2/?format=json
  @Override
  public boolean init(OimChannels oimChannel, Session dbSession)
      throws ChannelConfigurationException {
    super.init(oimChannel, dbSession);
    storeUrl = "http://ibuyrite.cloudfrontend.net";
    clientId = "kTC5SQfXBDA4kr9WN8UhWW3ESENTWLvt";
    secrateKey = "wM4auTbJDzz9yDU36NAU8EFCrLXaDhm5";
    return true;
  }

  @Override
  public void getVendorOrders(OimOrderBatchesTypes batchesTypes, OimOrderBatches batch)
      throws ChannelCommunicationException, ChannelOrderFormatException,
      ChannelConfigurationException {
    Transaction tx = m_dbSession.getTransaction();
    if (tx != null && tx.isActive())
      tx.commit();
    tx = m_dbSession.beginTransaction();
    batch.setOimChannels(m_channel);
    batch.setOimOrderBatchesTypes(batchesTypes);
    batch.setInsertionTm(new Date());
    batch.setCreationTm(new Date());
    m_dbSession.save(batch);
    tx.commit();
    if (StringHandle.removeNull(m_orderProcessingRule.getPullWithStatus()).equals(""))
      throw new ChannelConfigurationException(
          "Error in channel Setup : Orders To Pull From Channel not correctly configured");
    if (StringHandle.removeNull(m_orderProcessingRule.getConfirmedStatus()).equals(""))
      throw new ChannelConfigurationException(
          "Error in channel Setup : Order Status When imported to Channel Manager is not correctly configured");

    String status = m_orderProcessingRule.getPullWithStatus();
    String requestUrl = storeUrl;
    JSONParser parser = new JSONParser();
    int totalOrders = 0;
    JSONArray orderArr = new JSONArray();
    boolean isCompleted = false;
    String appendToUrl = null;
    do {
      String response = sendOAuthRequest(null, requestUrl, GET_REQUEST_METHOD, null, appendToUrl,
          status, true);
      JSONObject jsonObject;
      try {
        jsonObject = (JSONObject) parser.parse(response);
      } catch (ParseException e1) {
        log.error("Error in parsing response from Devhub store - " + storeUrl, e1);
        throw new ChannelOrderFormatException(
            "Error in parsing response from Devhub storel - " + storeUrl, e1);
      }
      JSONObject metaInfo = (JSONObject) jsonObject.get("meta");
      if (metaInfo.get("next") != null && !"null".equalsIgnoreCase((String) metaInfo.get("next"))) {
        appendToUrl = StringHandle.removeNull((String) metaInfo.get("next"));
      } else {
        isCompleted = true;
      }
      orderArr = (JSONArray) jsonObject.get("objects");
      for (int i = 0; i < orderArr.size(); i++) {
        String storeOrderId = null;
        OimOrders oimOrders = null;
        try {
          JSONObject orderObj = (JSONObject) orderArr.get(i);
          storeOrderId = orderObj.get("id").toString();
          if (orderAlreadyImported(storeOrderId)) {
            log.info("Order#{} is already imported in the system, updating Order.", storeOrderId);
            continue;
          }
          if (tx != null && tx.isActive())
            tx.commit();
          tx = m_dbSession.beginTransaction();
          oimOrders = new OimOrders();

          oimOrders.setStoreOrderId(storeOrderId);
          // setting billing information

          oimOrders.setBillingName(StringHandle.removeNull((String) orderObj.get("buyer_name")));
          oimOrders.setBillingEmail(StringHandle.removeNull((String) orderObj.get("buyer_email")));
          oimOrders.setBillingCity(StringHandle.removeNull((String) orderObj.get("ship_to_city")));
          oimOrders.setBillingStreetAddress(
              StringHandle.removeNull((String) orderObj.get("ship_to_address")));
          oimOrders
              .setBillingState(StringHandle.removeNull((String) orderObj.get("ship_to_state")));
          oimOrders
              .setBillingZip(StringHandle.removeNull((String) orderObj.get("ship_to_postal_code")));
          oimOrders
              .setBillingCountry(StringHandle.removeNull((String) orderObj.get("ship_to_country")));
          // setting delivery information
          oimOrders.setDeliveryName(StringHandle.removeNull((String) orderObj.get("buyer_name")));
          oimOrders.setDeliveryEmail(StringHandle.removeNull((String) orderObj.get("buyer_email")));
          oimOrders.setDeliveryCity(StringHandle.removeNull((String) orderObj.get("ship_to_city")));
          oimOrders.setDeliveryStreetAddress(
              StringHandle.removeNull((String) orderObj.get("ship_to_address")));
          oimOrders
              .setDeliveryState(StringHandle.removeNull((String) orderObj.get("ship_to_state")));
          oimOrders.setDeliveryStateCode(
              StringHandle.removeNull((String) orderObj.get("ship_to_state")));
          oimOrders.setDeliveryZip(
              StringHandle.removeNull((String) orderObj.get("ship_to_postal_code")));
          oimOrders.setDeliveryCountry(
              StringHandle.removeNull((String) orderObj.get("ship_to_country")));
          oimOrders.setDeliveryCountryCode(
              StringHandle.removeNull((String) orderObj.get("ship_to_country")));

          // setting customer information

          oimOrders.setCustomerName(StringHandle.removeNull((String) orderObj.get("buyer_name")));
          oimOrders.setCustomerEmail(StringHandle.removeNull((String) orderObj.get("buyer_email")));
          oimOrders.setCustomerCity(StringHandle.removeNull((String) orderObj.get("ship_to_city")));
          oimOrders.setCustomerStreetAddress(
              StringHandle.removeNull((String) orderObj.get("ship_to_address")));
          oimOrders
              .setCustomerState(StringHandle.removeNull((String) orderObj.get("ship_to_state")));
          oimOrders.setCustomerZip(
              StringHandle.removeNull((String) orderObj.get("ship_to_postal_code")));
          oimOrders.setCustomerCountry(
              StringHandle.removeNull((String) orderObj.get("ship_to_country")));
          SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
          Date orderTm = null;
          try {
            String orderTmString = ((String) orderObj.get("timestamp"));
            orderTm = df.parse(orderTmString);
          } catch (java.text.ParseException e) {
            e.printStackTrace();
          }
          oimOrders.setOrderTm(orderTm);
          if (orderObj.get("price") instanceof Long) {
            oimOrders.setOrderTotalAmount(((Number) orderObj.get("price")).doubleValue());
          } else if (orderObj.get("price") instanceof String)
            oimOrders.setOrderTotalAmount(Double.valueOf(((String) orderObj.get("price"))));
          oimOrders.setPayMethod("Credit Card");

          String prodResponse = sendOAuthRequest(null, requestUrl, GET_REQUEST_METHOD,
              oimOrders.getStoreOrderId(), null, null, false);
          log.info("item response :- " + prodResponse);
          if (prodResponse == null) {
            log.error("Got null response for order id - " + oimOrders.getStoreOrderId());
            continue;
          }
//          oimOrders.setInsertionTm(new Date());
//          oimOrders.setOimOrderBatches(batch);
//          batch.getOimOrderses().add(oimOrders);
          oimOrders.setOrderFetchTm(new Date());
          JSONObject itemJObject = new JSONObject();
          JSONParser itemJParser = new JSONParser();
          try {
            itemJObject = (JSONObject) itemJParser.parse(prodResponse);
          } catch (ParseException e1) {
            log.error("Error in parsing response from Devhub store - " + storeUrl, e1);
          }
          String shippingDetails = null;

          JSONArray itemArray = (JSONArray) itemJObject.get("cart_items");
          if (itemArray.size() > 0) {
            JSONObject productJson = (JSONObject) itemArray.get(0);
            shippingDetails = StringHandle.removeNull((String) productJson.get("shipping_name"));
            oimOrders.setShippingDetails(shippingDetails);
          }
          if (oimChannelShippingMapList != null && shippingDetails != null) {
            for (OimChannelShippingMap entity : oimChannelShippingMapList) {
              String shippingRegEx = entity.getShippingRegEx();
              if (shippingDetails.equalsIgnoreCase(shippingRegEx)) {
                oimOrders.setOimShippingMethod(entity.getOimShippingMethod());
                log.info("Shipping set to " + entity.getOimShippingMethod().getName());
                break;
              }
            }
          }
          if (oimOrders.getOimShippingMethod() == null && shippingDetails != null) {
            String methodIdStr = DevhubShippingMapping.getProperty(shippingDetails);
            if (methodIdStr != null) {
              int methodId = Integer.parseInt(methodIdStr);
              OimShippingMethod oimShippingMethod = (OimShippingMethod) m_dbSession
                  .get(OimShippingMethod.class, methodId);
              oimOrders.setOimShippingMethod(oimShippingMethod);
            }

          }
          if (oimOrders.getOimShippingMethod() == null)
            log.warn("Shipping can't be mapped for order " + oimOrders.getStoreOrderId());
          m_dbSession.saveOrUpdate(oimOrders);
          // setting product information
          Set<OimOrderDetails> detailSet = new HashSet<OimOrderDetails>();
          Set<String> notMatchingSkuSet = new HashSet<String>();
          for (int j = 0; j < itemArray.size(); j++) {
            OimOrderDetails details = new OimOrderDetails();
            JSONObject item = (JSONObject) itemArray.get(j);
            details.setInsertionTm(new Date());
            details
                .setOimOrderStatuses(new OimOrderStatuses(OimConstants.ORDER_STATUS_UNPROCESSED));
            String sku = (String) ((JSONObject) item.get("product")).get("sku");
            if (m_channel.getOnlyPullMatchingOrders() == 1) {
              if (!isProductMatchesWithSupplier(sku)) {
                notMatchingSkuSet.add(sku);
              }
            }
            details.setCostPrice(Double.parseDouble(StringHandle
                .removeNull((String) ((JSONObject) item.get("product")).get("wholesale_price"))));
            OimSuppliers oimSuppliers = null;
            String prefix = null;
            List<OimSuppliers> blankPrefixSupplierList = new ArrayList<OimSuppliers>();
            if (supplierMap != null) {
              for (Iterator<OimSuppliers> itr = supplierMap.keySet().iterator(); itr.hasNext();) {
                OimSuppliers supplier = itr.next();
                prefix = supplierMap.get(supplier);
                if (prefix == null) {
                  blankPrefixSupplierList.add(supplier);
                  continue;
                }
                if (sku.toUpperCase().startsWith(prefix.toUpperCase())) {
                  oimSuppliers = supplier;
                  break;
                }
              }
            }
            if (oimSuppliers == null && blankPrefixSupplierList.size() == 1) {
              oimSuppliers = blankPrefixSupplierList.get(0);
            }
            if (oimSuppliers != null) {
              details.setOimSuppliers(oimSuppliers);
            }
            details.setProductDesc((String) item.get("full_name"));
            details.setProductName((String) item.get("base_name"));
            details.setQuantity((int) (long) (item.get("quantity")));
            details.setSalePrice(
                Double.parseDouble(StringHandle.removeNull((String) item.get("unit_price"))));
            details.setSku(sku);
            details.setStoreOrderItemId(((long) item.get("id")) + "");
            details.setOimOrders(oimOrders);
           // m_dbSession.saveOrUpdate(details);
            detailSet.add(details);
          }
          if (itemArray.size() == notMatchingSkuSet.size()) {
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
          oimOrders.setOimOrderDetailses(detailSet);
          m_dbSession.saveOrUpdate(oimOrders);
          if (m_channel.getTestMode() == 0) {
            sendAcknowledgementToStore(storeUrl, storeOrderId);
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
          // tx.rollback();
          throw new ChannelOrderFormatException("Error occured during pull of store order id - "
              + storeOrderId + " cause - " + e.getMessage(), e);
        }
        totalOrders++;
      }
    } while (isCompleted == false);
    log.info("Fetched {} order(s)", totalOrders);
    try {
      if (tx != null && tx.isActive())
        tx.commit();
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

  private void sendAcknowledgementToStore(String storeUrl, String storeOrderId)
      throws ChannelConfigurationException, ChannelCommunicationException,
      ChannelOrderFormatException {
    String appendToUrl = PATH + storeOrderId + "/";
    JSONObject jObject = new JSONObject();
    jObject.put("status", m_orderProcessingRule.getConfirmedStatus());
    sendOAuthRequest(jObject.toString(), storeUrl, PUT_REQUEST_METHOD, storeOrderId, appendToUrl,
        null, true);
  }

  private String sendOAuthRequest(String data, String requestUrl, String requestMethod,
      String storeOrderId, String appendToUrl, String status, boolean isOrderRequest)
          throws ChannelConfigurationException, ChannelCommunicationException,
          ChannelOrderFormatException {

    String response = null;
    HttpURLConnection connection = null;
    URL url;
    int responseCode = 0;
    try {
      String appendToRequest = null;
      OAuthProvider oAuthProvider = null;
      String host = HOST;
      String path = PATH;
      List<String> additionalParameters = new ArrayList<String>();
      if (appendToUrl == null && requestMethod.equalsIgnoreCase(GET_REQUEST_METHOD)) {

        if (isOrderRequest) { // get all products
          appendToRequest = "/api/v2/shoporders/?status=" + status + "&limit=10";
          additionalParameters.add("status=" + status);
          additionalParameters.add("limit=" + limit);
        } else { // get item description based on order ID
          appendToRequest = PATH + storeOrderId + "/"; // item request
          path = PATH + storeOrderId;
          oAuthProvider = new OAuthProvider(host, path, clientId, secrateKey);
        }
        requestUrl = requestUrl + appendToRequest;

      } else { // put method or next order fetch call
        requestUrl = requestUrl + appendToUrl;
        if (requestMethod.equalsIgnoreCase(GET_REQUEST_METHOD)) {
          String parameter = appendToUrl.substring(appendToUrl.indexOf("?"), appendToUrl.length());
          String[] parameters = parameter.split("&");
          for (int i = 0; i < parameters.length; i++) {
            additionalParameters.add(parameters[i]);
          }
        }
      }
      oAuthProvider = new OAuthProvider(host, path, clientId, secrateKey);
      String oauthHeader = oAuthProvider.generateOauthHeader(requestMethod, additionalParameters);
      url = new URL(requestUrl);
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod(requestMethod);
      connection.setDoOutput(true);
      connection.addRequestProperty("Authorization", oauthHeader);
      if (data != null) {
        connection.addRequestProperty("Content-type", "application/json");
        connection.addRequestProperty("Accept", "application/json");
        byte[] req = data.getBytes();
        OutputStream out = connection.getOutputStream();
        out.write(req);
        out.close();
      }
      connection.connect();
      responseCode = connection.getResponseCode();
      if (responseCode == 200 || responseCode == 202) {
        InputStream is = connection.getInputStream();
        if (is != null) {
          response = getStringFromStream(connection.getInputStream());
          log.info(response);
        }
      } else if (requestMethod.equalsIgnoreCase(GET_REQUEST_METHOD) && !isOrderRequest
          && responseCode == 400) {
        log.error("Error occered because this order has no items in the cart..");
      } else if (responseCode == 429) {
        noOfApiRequests++;
        connection.disconnect();
        if (noOfApiRequests < 5) {
          return sendOAuthRequest(data, requestUrl, requestMethod, storeOrderId, appendToUrl,
              status, isOrderRequest);
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
            "401- API Request is not valid for this shop. Please check the request parameters.");
      } else if (responseCode == 422) {
        if (storeOrderId != null)
          throw new ChannelCommunicationException(
              "422 - There was a problem with the body of your Request. Inspect the response body for the errors for store order id - "
                  + storeOrderId);
        throw new ChannelCommunicationException(
            "422 - There was a problem with the body of your Request. Inspect the response body for the errors");
      } else if (responseCode == 403) {
        if (storeOrderId != null)
          throw new ChannelConfigurationException("Error occured for store order id - "
              + storeOrderId + " response code 403 - Forbidden access");
        throw new ChannelConfigurationException("response code 403 - Forbidden access");
      } else if (String.valueOf(responseCode).startsWith("5")) {
        throw new ChannelCommunicationException(
            "500 series - Either site is down or you sent something that caused our code to error out.");
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

  public void updateStoreOrder(OimOrderDetails oimOrderDetails, OrderStatus orderStatus)
      throws ChannelCommunicationException, ChannelOrderFormatException,
      ChannelConfigurationException {
    log.info("order id is - {}", oimOrderDetails.getOimOrders().getOrderId());
    log.info("order status is - {}", orderStatus);
    if (!orderStatus.isShipped()) {
      return;
    }
    clientId = ApplicationProperties.getProperty(ApplicationProperties.DEVHUB_CLIENT_ID);
    secrateKey = ApplicationProperties.getProperty(ApplicationProperties.DEVHUB_CLIENT_SECRET);
    String requestUrl = storeUrl;
    String appendToUrl = "/api/v2/shoporders/" + oimOrderDetails.getOimOrders().getStoreOrderId()
        + "/";
    JSONObject jsonObject = new JSONObject();
    int count = 0;
    JSONArray jArray = new JSONArray();

    OimOrders oimOrders = oimOrderDetails.getOimOrders();
    for (OimOrderDetails detail : oimOrders.getOimOrderDetailses()) {
      JSONObject itemObject = new JSONObject();
      itemObject.put("id", Integer.parseInt(oimOrderDetails.getStoreOrderItemId()));
      itemObject.put("order_id",
          Integer.parseInt(oimOrderDetails.getOimOrders().getStoreOrderId()));
      itemObject.put("quantity", oimOrderDetails.getQuantity());
      JSONObject productJson = new JSONObject();
      productJson.put("sku", oimOrderDetails.getSku());
      itemObject.put("product", productJson);
      if (detail.getDetailId().intValue() == oimOrderDetails.getDetailId().intValue()) {
        itemObject.put("shipping_name", orderStatus.getTrackingData().get(0).getCarrierName()
            + SPACE + orderStatus.getTrackingData().get(0).getShippingMethod());
        itemObject.put("shipping_tracking",
            orderStatus.getTrackingData().get(0).getShipperTrackingNumber());
        count++;
      } else {
        if (detail.getOimOrderStatuses().getStatusId() == OimConstants.ORDER_STATUS_SHIPPED
            || detail.getOimOrderStatuses().getStatusId() == OimConstants.ORDER_STATUS_COMPLETE) {
          count++;
        }
      }
      jArray.add(itemObject);
    }
    jsonObject.put("cart_items", jArray);
    if (count == oimOrders.getOimOrderDetailses().size())
      jsonObject.put("status", "shipped");
    else {
      jsonObject.put("status", "inprocess");
    }

    log.info("request for fullfillment : {}", jsonObject.toJSONString());
    StringRequestEntity requestEntity = null;
    try {
      requestEntity = new StringRequestEntity(jsonObject.toJSONString(), "application/json",
          "UTF-8");
    } catch (UnsupportedEncodingException e) {
      log.error("Error in parsing tracking request json {}", e);
      throw new ChannelOrderFormatException("Error in parsing tracking request json", e);
    }
    String response = sendOAuthRequest(jsonObject.toString(), requestUrl, PUT_REQUEST_METHOD,
        oimOrderDetails.getOimOrders().getStoreOrderId(), appendToUrl, null, false);

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

  public static void main(String[] args) throws ChannelCommunicationException,
      ChannelOrderFormatException, ChannelConfigurationException {
    Session session = SessionManager.currentSession();
    OimOrderDetails oimOrderDetails = (OimOrderDetails) session.get(OimOrderDetails.class, 9846406);
    OrderStatus orderStatus = new OrderStatus();
    TrackingData td = new TrackingData();
    td.setCarrierName("UPS");
    td.setShippingMethod("GROUND");
    td.setShipperTrackingNumber("TESTTRACK9999");
    orderStatus.addTrackingData(td);
    DevHubOrderImport devHubOrderImport  = new DevHubOrderImport();
    devHubOrderImport.storeUrl = "http://ibuyrite.cloudfrontend.net";
    devHubOrderImport.clientId = "kTC5SQfXBDA4kr9WN8UhWW3ESENTWLvt";
    devHubOrderImport.secrateKey = "wM4auTbJDzz9yDU36NAU8EFCrLXaDhm5";
    devHubOrderImport .updateStoreOrder(oimOrderDetails, orderStatus);
  }
}
