package salesmachine.oim.stores.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

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
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.ChannelBase;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.oim.suppliers.modal.TrackingData;
import salesmachine.util.ApplicationProperties;
import salesmachine.util.OAuthProvider;
import salesmachine.util.StringHandle;

public class DevHubOrderImport extends ChannelBase implements IOrderImport {

  private static final Logger log = LoggerFactory.getLogger(DevHubOrderImport.class);
  private String storeUrl;
  private String siteID;
  private String clientId;
  private String secrateKey;
  private static final String GET_REQUEST_METHOD = "GET";
  private static final String POST_REQUEST_METHOD = "POST";
  private static final String PUT_REQUEST_METHOD = "PUT";
  private static final String SPACE = " ";
  private int noOfApiRequests = 0;
  // new
  // inprocess
  // shipped
  // cancelled

  // End Point -- http://ibuyrite.cloudfrontend.net/api/v2/?format=json
  @Override
  public boolean init(OimChannels oimChannel, Session dbSession)
      throws ChannelConfigurationException {
    super.init(oimChannel, dbSession);
    storeUrl = StringHandle.removeNull(PojoHelper.getChannelAccessDetailValue(m_channel,
        OimConstants.CHANNEL_ACCESSDETAIL_CHANNEL_URL));
    if (storeUrl.endsWith("/"))
      storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
    clientId = ApplicationProperties.getProperty(ApplicationProperties.DEVHUB_CLIENT_ID);
    secrateKey = ApplicationProperties.getProperty(ApplicationProperties.DEVHUB_CLIENT_SECRET);
    siteID = StringHandle.removeNull(PojoHelper.getChannelAccessDetailValue(m_channel,
        OimConstants.CHANNEL_ACCESSDETAIL_DEVHUB_SITE_ID));
    if (storeUrl.length() == 0 || siteID.length() == 0) {
      log.error("Channel setup is not correct. Please provide correct details.");
      throw new ChannelConfigurationException(
          "Channel setup is not correct. Please provide correct details.");
    }
    return true;
  }

  public static void main(String[] args) throws ChannelCommunicationException,
      ChannelOrderFormatException, ChannelConfigurationException {

    new DevHubOrderImport().getVendorOrders(null, null);
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
    // String status = "new";
   // siteID = "1643737";
    String requestUrl = storeUrl;
    // String requestUrl = devHubEndPoint + "?site_id=" + siteID + "&status=" + status+"&limit=10";

    JSONParser parser = new JSONParser();
    int totalOrders = 0;
    JSONArray orderArr = new JSONArray();
    boolean isCompleted = false;
    String appendToUrl = null;
    do {
      String response = sendRequestOAuth(null, requestUrl, GET_REQUEST_METHOD, null, appendToUrl,
          status, true);
      JSONObject jsonObject;
      try {
        jsonObject = (JSONObject) parser.parse(response);
        // Object obj = parser.parse(new
        // FileReader("/home/manish-kumar/git/isource-cm/isource-cm/oim-core/src/main/resources/store-scripts/devhub-response.json"));
        // jsonObject = (JSONObject)obj;
      } catch (ParseException e1) {
        log.error("Error in parsing response from Devhub store - " + storeUrl, e1);
        throw new ChannelOrderFormatException(
            "Error in parsing response from Devhub storel - " + storeUrl, e1);
      }
      JSONObject metaInfo = (JSONObject) jsonObject.get("meta");
      if (metaInfo.get("next") != null && !"null".equalsIgnoreCase((String) metaInfo.get("next"))) {
        appendToUrl = StringHandle.removeNull((String) metaInfo.get("next"));
        // requestUrl = devHubEndPoint + appendToUrl;
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
          oimOrders.setInsertionTm(new Date());
          oimOrders.setOimOrderBatches(batch);
          batch.getOimOrderses().add(oimOrders);
          oimOrders.setOrderFetchTm(new Date());
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
          // oimOrders.setPayMethod((String) orderObj.get("gateway")); TODO
          String shippingDetails;
          try {
            shippingDetails = StringHandle.removeNull((String) orderObj.get("shipping_lines"));
          } catch (IndexOutOfBoundsException e) {
            shippingDetails = "Standard Shipping";
            log.warn("Order {} has no shipping method from store. Assigning default [{}] ",
                oimOrders.getStoreOrderId(), shippingDetails);
          }
          oimOrders.setShippingDetails(shippingDetails);
          if (oimChannelShippingMapList != null) {
            for (OimChannelShippingMap entity : oimChannelShippingMapList) {
              String shippingRegEx = entity.getShippingRegEx();
              if (shippingDetails.equalsIgnoreCase(shippingRegEx)) {
                oimOrders.setOimShippingMethod(entity.getOimShippingMethod());
                log.info("Shipping set to " + entity.getOimShippingMethod().getName());
                break;
              }
            }
          }
          if (oimOrders.getOimShippingMethod() == null)
            log.warn("Shipping can't be mapped for order " + oimOrders.getStoreOrderId());
          m_dbSession.saveOrUpdate(oimOrders);
          // setting product information
          String prodResponse = sendRequestOAuth(null, requestUrl, GET_REQUEST_METHOD, oimOrders.getStoreOrderId(), null,
              null, false);
          log.info("item response :- " + prodResponse);
          JSONObject itemJObject = new JSONObject();
          JSONParser itemJParser = new JSONParser();
          try {
            itemJObject = (JSONObject) itemJParser.parse(prodResponse);
          } catch (ParseException e1) {
            log.error("Error in parsing response from Devhub store - " + storeUrl, e1);
          }
          JSONArray itemArray = (JSONArray) itemJObject.get("cart_items");
          Set<OimOrderDetails> detailSet = new HashSet<OimOrderDetails>();
          for (int j = 0; j < itemArray.size(); j++) {
            OimOrderDetails details = new OimOrderDetails();
            JSONObject item = (JSONObject) itemArray.get(j);

            details.setInsertionTm(new Date());
            details
                .setOimOrderStatuses(new OimOrderStatuses(OimConstants.ORDER_STATUS_UNPROCESSED));
            String sku = (String) ((JSONObject) item.get("product")).get("sku");
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
                if (sku.toUpperCase().startsWith(prefix)) {
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
            m_dbSession.saveOrUpdate(details);
            detailSet.add(details);
          }
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
    // log.info("Returning Order batch with size: {}", batch.getOimOrderses().size());

  }

  private void sendAcknowledgementToStore(String storeUrl, String storeOrderId)
      throws ChannelConfigurationException, ChannelCommunicationException,
      ChannelOrderFormatException {
    String appendToUrl = "/api/v2/shoporders/" + storeOrderId + "/";
    JSONObject jObject = new JSONObject();
    jObject.put("status", m_orderProcessingRule.getConfirmedStatus());
    sendRequestOAuth(jObject.toString(), storeUrl, PUT_REQUEST_METHOD, storeOrderId, appendToUrl,
        null, true);
  }

  private String sendRequestOAuth(String data, String requestUrl, String requestMethod,
      String storeOrderId, String appendToUrl, String status, boolean isOrderRequest)
          throws ChannelConfigurationException, ChannelCommunicationException,
          ChannelOrderFormatException {

    String response = null;
    HttpsURLConnection connection = null;
    URL url;
    int responseCode = 0;
    try {
      String appendToRequest = null;
      if (appendToUrl == null && requestMethod.equalsIgnoreCase(GET_REQUEST_METHOD)) {
        if (isOrderRequest)
          appendToRequest = "/api/v2/shoporders/?site_id=" + siteID + "&status=" + status
              + "&limit=10&";
        else
          appendToRequest = "/api/v2/shoporders/" + storeOrderId + "/?"; // item request
        String signature = OAuthProvider.getSignature(requestUrl, secrateKey);
        appendToRequest += "oauth_consumer_key=" + clientId;
        appendToRequest += "&oauth_signature_method=HMAC-SHA1";
        appendToRequest += "&oauth_timestamp=" + (System.currentTimeMillis() / 1000);
        appendToRequest += "&oauth_nonce=" + (int) (Math.random() * 100000000);
        appendToRequest += "&oauth_version=1.0";
        appendToRequest += "&oauth_signature=" + signature;
        requestUrl = requestUrl + appendToRequest;

      } else {
        requestUrl = requestUrl + appendToUrl;
      }

      url = new URL(null, requestUrl, new sun.net.www.protocol.https.Handler());
      connection = (HttpsURLConnection) url.openConnection();
      connection.setRequestMethod(requestMethod);
      connection.setDoOutput(true);

      if (data != null && (requestMethod.equalsIgnoreCase(POST_REQUEST_METHOD)
          || requestMethod.equalsIgnoreCase(PUT_REQUEST_METHOD))) {
        // OAuth
        // oauth_consumer_key="kTC5SQfXBDA4kr9WN8UhWW3ESENTWLvt",oauth_signature_method="HMAC-SHA1",oauth_timestamp="1455527406",
        // oauth_nonce="rlJwSv",oauth_version="1.0",oauth_signature="6bsuoeRQgzdlgXBtTJo7HqHa4N4%3D"
        String signature = OAuthProvider.getSignature(requestUrl, secrateKey);
        String authorizationParam = "OAuth oauth_consumer_key=\"" + clientId+"\"";
        authorizationParam += ",oauth_signature_method=\"HMAC-SHA1\"";
        authorizationParam += ",oauth_timestamp=\"" + (System.currentTimeMillis() / 1000)+"\"";
        authorizationParam += ",oauth_nonce=\"" + (int) (Math.random() * 100000000)+"\"";
        authorizationParam += ",oauth_version=\"1.0\"";
        authorizationParam += ",oauth_signature=\"" + signature+"\"";
        connection.addRequestProperty("Content-type", "application/json");
        connection.addRequestProperty("Authorization", authorizationParam);
        byte[] req = data.getBytes();
        OutputStream out = connection.getOutputStream();
        out.write(req);
        out.close();
      }
      connection.connect();
      responseCode = connection.getResponseCode();
      if (responseCode == 200 || responseCode ==202) {
        response = getStringFromStream(connection.getInputStream());
        System.out.println(response);
      } else if (responseCode == 201 && requestMethod.equalsIgnoreCase(POST_REQUEST_METHOD)) {
        response = getStringFromStream(connection.getInputStream());
      } else if (responseCode == 429) {
        noOfApiRequests++;
        connection.disconnect();
        if (noOfApiRequests < 5) {
          return sendRequestOAuth(data, requestUrl, requestMethod, storeOrderId, appendToUrl,
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
            "401- API Request is not valid for this shop. You are either not using the right Access Token or the permission for that token has been revoked");
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
    } catch (InvalidKeyException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      if (connection != null)
        connection.disconnect();
    }
    return response;

  }

  public void updateStoreOrder(OimOrderDetails oimOrderDetails, OrderStatus orderStatus)
      throws ChannelCommunicationException, ChannelOrderFormatException,
      ChannelConfigurationException {
    // this method is implemented for tracking purpose
    log.info("order id is - {}", oimOrderDetails.getOimOrders().getOrderId());
    log.info("order status is - {}", orderStatus);

    if (!orderStatus.isShipped()) {
      return;
    }
    String requestUrl = storeUrl;
    String appendToUrl = "/api/v2/shoporders/" + oimOrderDetails.getOimOrders().getStoreOrderId()
        + "/";

    // { "id" : "Item ID here", "shipping_name" : "UPS GROUND", "shipping_tracking" :
    // "1234567890","quantity":"2" }
    JSONObject jsonObject = new JSONObject();
    JSONArray lineItemArray = new JSONArray();
    JSONArray trackingNos = new JSONArray();
    int qty = 0;
    for (TrackingData trackingData : orderStatus.getTrackingData()) {
      trackingNos.add(trackingData.getShipperTrackingNumber());
      qty += trackingData.getQuantity();
      jsonObject.put("id", oimOrderDetails.getStoreOrderItemId());
      jsonObject.put("shipping_name", orderStatus.getTrackingData().get(0).getCarrierName() + SPACE
          + orderStatus.getTrackingData().get(0).getShippingMethod());
      jsonObject.put("shipping_tracking", trackingData.getShipperTrackingNumber());
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
    this.noOfApiRequests = 0;
    // sendRequestOAuth(jsonObject.toString(), requestUrl,
    // POST_REQUEST_METHOD,oimOrderDetails.getOimOrders().getStoreOrderId());

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
