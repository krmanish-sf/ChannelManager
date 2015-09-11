package salesmachine.oim.stores.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.axis.encoding.Base64;
import org.apache.axis.utils.ByteArrayOutputStream;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.mws.MarketplaceWebService;
import com.amazonaws.mws.MarketplaceWebServiceClient;
import com.amazonaws.mws.MarketplaceWebServiceConfig;
import com.amazonaws.mws.MarketplaceWebServiceException;
import com.amazonaws.mws.model.IdList;
import com.amazonaws.mws.model.SubmitFeedRequest;
import com.amazonaws.mws.model.SubmitFeedResponse;
import com.amazonservices.mws.client.MwsUtl;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersClient;
import com.amazonservices.mws.orders._2013_09_01.model.ListOrderItemsRequest;
import com.amazonservices.mws.orders._2013_09_01.model.ListOrderItemsResponse;
import com.amazonservices.mws.orders._2013_09_01.model.ListOrderItemsResult;
import com.amazonservices.mws.orders._2013_09_01.model.ListOrdersByNextTokenRequest;
import com.amazonservices.mws.orders._2013_09_01.model.ListOrdersByNextTokenResponse;
import com.amazonservices.mws.orders._2013_09_01.model.ListOrdersRequest;
import com.amazonservices.mws.orders._2013_09_01.model.ListOrdersResponse;
import com.amazonservices.mws.orders._2013_09_01.model.Order;
import com.amazonservices.mws.orders._2013_09_01.model.OrderItem;
import com.amazonservices.mws.orders._2013_09_01.model.ResponseHeaderMetadata;

import salesmachine.hibernatedb.OimChannelShippingMap;
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
import salesmachine.oim.stores.modal.amazon.AmazonEnvelope;
import salesmachine.oim.stores.modal.amazon.AmazonEnvelope.Message;
import salesmachine.oim.stores.modal.amazon.Header;
import salesmachine.oim.stores.modal.amazon.OrderAcknowledgement;
import salesmachine.oim.stores.modal.amazon.OrderFulfillment;
import salesmachine.oim.stores.modal.amazon.OrderFulfillment.FulfillmentData;
import salesmachine.oim.stores.modal.amazon.OrderFulfillment.Item;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.oim.suppliers.modal.TrackingData;
import salesmachine.util.ApplicationProperties;
import salesmachine.util.StringHandle;

public class AmazonOrderImport extends ChannelBase implements IOrderImport {
  private static final Logger log = LoggerFactory.getLogger(AmazonOrderImport.class);
  private String sellerId, mwsAuthToken;
  private List<String> marketPlaceIdList = null;
  private static final MarketplaceWebService service;
  private static final MarketplaceWebServiceOrdersClient client;
  private static JAXBContext jaxbContext;

  static {
    MarketplaceWebServiceConfig config = new MarketplaceWebServiceConfig();
    config.setServiceURL(ApplicationProperties.getProperty(ApplicationProperties.MWS_SERVICE_URL));
    service = new MarketplaceWebServiceClient(
        ApplicationProperties.getProperty(ApplicationProperties.MWS_ACCESS_KEY),
        ApplicationProperties.getProperty(ApplicationProperties.MWS_SECRET_KEY),
        ApplicationProperties.getProperty(ApplicationProperties.MWS_APP_NAME),
        ApplicationProperties.getProperty(ApplicationProperties.MWS_APP_VERSION), config);
    // Get a client connection. Make sure you've set the variables in
    // MarketplaceWebServiceOrdersClientConfig.
    client = MarketplaceWebServiceOrdersClientConfig.getClient();
    try {
      jaxbContext = JAXBContext.newInstance(OrderFulfillment.class, SubmitFeedRequest.class,
          OrderAcknowledgement.class, AmazonEnvelope.class);
    } catch (JAXBException e) {
      log.error(e.getMessage(), e);
    }
  }

  @Override
  public boolean init(int channelID, Session dbSession) throws ChannelConfigurationException {
    super.init(channelID, dbSession);
    sellerId = StringHandle.removeNull(PojoHelper.getChannelAccessDetailValue(m_channel,
        OimConstants.CHANNEL_ACCESSDETAIL_AMAZON_SELLERID));
    mwsAuthToken = StringHandle.removeNull(PojoHelper.getChannelAccessDetailValue(m_channel,
        OimConstants.CHANNEL_ACCESSDETAIL_AMAZON_MWS_AUTH_TOKEN));
    String marketPlaceIds = StringHandle.removeNull(PojoHelper.getChannelAccessDetailValue(
        m_channel, OimConstants.CHANNEL_ACCESSDETAIL_AMAZON_MWS_MARKETPLACE_ID));
    String[] split = marketPlaceIds.split(",");
    marketPlaceIdList = new ArrayList<String>();
    for (int i = 0; i < split.length; i++) {
      marketPlaceIdList.add(split[i]);
    }
    if (sellerId.length() == 0 || mwsAuthToken.length() == 0) {
      log.error("Channel setup is not correct. Please provide correct details.");
      throw new ChannelConfigurationException(
          "Channel setup is not correct. Please provide correct details.");
    }
    return true;
  }

  @Override
  public void getVendorOrders(OimOrderBatchesTypes batchesTypes, OimOrderBatches batch)
      throws ChannelCommunicationException, ChannelOrderFormatException,
      ChannelConfigurationException {
    Transaction tx = m_dbSession.getTransaction();
    try {
      batch.setOimChannels(m_channel);
      batch.setOimOrderBatchesTypes(batchesTypes);
      if (tx != null && tx.isActive())
        tx.commit();
      // Save Batch..
      tx = m_dbSession.beginTransaction();
      batch.setInsertionTm(new Date());
      batch.setCreationTm(new Date());
      m_dbSession.save(batch);
      tx.commit();

      tx = m_dbSession.beginTransaction();

      // Create a request.
      ListOrdersRequest listOrdersRequest = new ListOrdersRequest();
      listOrdersRequest.setSellerId(sellerId);
      listOrdersRequest.setMWSAuthToken(mwsAuthToken);
      listOrdersRequest.setMarketplaceId(marketPlaceIdList);

      if (!StringHandle.isNullOrEmpty(m_orderProcessingRule.getPullWithStatus())) {
        List<String> pullWithStatus = new ArrayList<String>();
        String[] split = m_orderProcessingRule.getPullWithStatus().split(",");
        for (String pullStatus : split) {
          pullWithStatus.add(pullStatus);
        }
        listOrdersRequest.setOrderStatus(pullWithStatus);
      }
      SubmitFeedRequest orderAckSubmitFeedRequest = new SubmitFeedRequest();
      orderAckSubmitFeedRequest.setMerchant(sellerId);
      orderAckSubmitFeedRequest.setMWSAuthToken(mwsAuthToken);
      orderAckSubmitFeedRequest.setMarketplaceIdList(new IdList(marketPlaceIdList));
      orderAckSubmitFeedRequest.setFeedType("_POST_ORDER_ACKNOWLEDGEMENT_DATA_");

      AmazonEnvelope ackAmazonEnvelope = new AmazonEnvelope();
      Header header = new Header();
      header.setDocumentVersion("1.01");
      header.setMerchantIdentifier(sellerId);
      ackAmazonEnvelope.setHeader(header);
      ackAmazonEnvelope.setMessageType("OrderAcknowledgement");

      Marshaller marshaller = null;
      try {
        marshaller = jaxbContext.createMarshaller();
      } catch (JAXBException e1) {
        log.error(e1.getMessage(), e1);
      }
      Calendar c = Calendar.getInstance();
      if (m_channel.getLastFetchTm() != null) {
        c.setTime(m_channel.getLastFetchTm());
        c.add(Calendar.HOUR, -2);
      } else {
        c.setTime(new Date());
        c.add(Calendar.DATE, -5);
      }

      log.info("Set to fetch Orders after {}", c.getTime());
      XMLGregorianCalendar createdAfter = MwsUtl.getDTF().newXMLGregorianCalendar();
      createdAfter.setYear(c.get(Calendar.YEAR));
      createdAfter.setMonth(c.get(Calendar.MONTH) + 1);
      createdAfter.setDay(c.get(Calendar.DAY_OF_MONTH));
      createdAfter.setHour(c.get(Calendar.HOUR_OF_DAY));
      createdAfter.setMinute(c.get(Calendar.MINUTE));
      createdAfter.setSecond(c.get(Calendar.SECOND));
      listOrdersRequest.setCreatedAfter(createdAfter);
      log.info(listOrdersRequest.toXML());
      int numOrdersSaved = 0;
      String nextToken = null;
      boolean lastPass = false;
      ListOrdersResponse response = client.listOrders(listOrdersRequest);
      List<Order> orderList = response.getListOrdersResult().getOrders();
      ResponseHeaderMetadata rhmd = response.getResponseHeaderMetadata();
      if (response.getListOrdersResult().isSetNextToken())
        nextToken = response.getListOrdersResult().getNextToken();

      do {
        log.info("Response: {}", rhmd.toString());

        log.info("Total order(s) fetched: {}", orderList.size());

        for (Order order : orderList) {
          String amazonOrderId = order.getAmazonOrderId();
          if (orderAlreadyImported(amazonOrderId)) {
            log.warn("Order#{} is already imported in the system, skipping it.", amazonOrderId);
            continue;
          }
          log.info("Order#{} fetched.", amazonOrderId);
          OimOrders oimOrder = new OimOrders();
          oimOrder.setStoreOrderId(amazonOrderId);
          if (order.getShippingAddress() != null) {
            oimOrder.setBillingCity(StringHandle.removeComma(order.getShippingAddress().getCity()));
            oimOrder.setBillingCompany(
                StringHandle.removeComma(order.getShippingAddress().getAddressLine3()));
            oimOrder.setBillingCountry(
                StringHandle.removeComma(order.getShippingAddress().getCounty()));
            oimOrder.setBillingEmail(StringHandle.removeComma(order.getBuyerEmail()));
            oimOrder.setBillingName(StringHandle.removeComma(order.getShippingAddress().getName()));
            oimOrder
                .setBillingPhone(StringHandle.removeComma(order.getShippingAddress().getPhone()));
            oimOrder.setBillingState(
                StringHandle.removeComma(order.getShippingAddress().getStateOrRegion()));
            oimOrder.setBillingStreetAddress(
                StringHandle.removeComma(order.getShippingAddress().getAddressLine1()));
            oimOrder.setBillingSuburb(
                StringHandle.removeComma(order.getShippingAddress().getAddressLine2()));
            oimOrder.setBillingZip(
                StringHandle.removeComma(order.getShippingAddress().getPostalCode()));

            oimOrder
                .setCustomerCity(StringHandle.removeComma(order.getShippingAddress().getCity()));
            oimOrder.setCustomerCompany(
                StringHandle.removeComma(order.getShippingAddress().getAddressLine3()));
            oimOrder.setCustomerCountry(
                StringHandle.removeComma(order.getShippingAddress().getCounty()));
            oimOrder.setCustomerEmail(StringHandle.removeComma(order.getBuyerEmail()));
            oimOrder
                .setCustomerName(StringHandle.removeComma(order.getShippingAddress().getName()));
            oimOrder
                .setCustomerPhone(StringHandle.removeComma(order.getShippingAddress().getPhone()));
            oimOrder.setCustomerState(
                StringHandle.removeComma(order.getShippingAddress().getStateOrRegion()));
            oimOrder.setCustomerStreetAddress(
                StringHandle.removeComma(order.getShippingAddress().getAddressLine1()));
            oimOrder.setCustomerSuburb(
                StringHandle.removeComma(order.getShippingAddress().getAddressLine2()));
            oimOrder.setCustomerZip(
                StringHandle.removeComma(order.getShippingAddress().getPostalCode()));

            oimOrder
                .setDeliveryCity(StringHandle.removeComma(order.getShippingAddress().getCity()));
            oimOrder.setDeliveryCompany(
                StringHandle.removeComma(order.getShippingAddress().getAddressLine3()));
            oimOrder.setDeliveryCountry(
                StringHandle.removeComma(order.getShippingAddress().getCounty()));
            oimOrder.setDeliveryEmail(StringHandle.removeComma(order.getBuyerEmail()));
            oimOrder
                .setDeliveryName(StringHandle.removeComma(order.getShippingAddress().getName()));
            oimOrder
                .setDeliveryPhone(StringHandle.removeComma(order.getShippingAddress().getPhone()));
            oimOrder.setDeliveryState(
                StringHandle.removeComma(order.getShippingAddress().getStateOrRegion()));
            oimOrder.setDeliveryStreetAddress(
                StringHandle.removeComma(order.getShippingAddress().getAddressLine1()));
            oimOrder.setDeliverySuburb(
                StringHandle.removeComma(order.getShippingAddress().getAddressLine2()));
            oimOrder.setDeliveryZip(
                StringHandle.removeComma(order.getShippingAddress().getPostalCode()));
          }
          oimOrder.setInsertionTm(new Date());
          oimOrder.setOimOrderBatches(batch);
          batch.getOimOrderses().add(oimOrder);
          // oimOrders.setOrderComment(order2.);
          oimOrder.setOrderFetchTm(new Date());
          oimOrder.setOrderTm(order.getPurchaseDate().toGregorianCalendar().getTime());
          oimOrder.setOrderTotalAmount(Double.parseDouble(order.getOrderTotal().getAmount()));
          oimOrder.setPayMethod(order.getPaymentMethod());
          oimOrder.setShippingDetails(order.getShipServiceLevel());
          String shippingDetails = order.getShipServiceLevel();
          for (OimChannelShippingMap entity : oimChannelShippingMapList) {
            String shippingRegEx = entity.getShippingRegEx();
            if (shippingDetails.equalsIgnoreCase(shippingRegEx)) {
              oimOrder.setOimShippingMethod(entity.getOimShippingMethod());
              log.info("Shipping set to " + entity.getOimShippingMethod());
              break;
            }
          }

          if (oimOrder.getOimShippingMethod() == null)
            log.warn("Shipping can't be mapped for order " + oimOrder.getStoreOrderId());
          // setting delivery state code
          if (order.getShippingAddress().getStateOrRegion().length() == 2) {
            oimOrder.setDeliveryStateCode(order.getShippingAddress().getStateOrRegion());
          } else {
            String stateCode = validateAndGetStateCode(oimOrder);
            if (stateCode != "")
              oimOrder.setDeliveryStateCode(stateCode);
          }

          m_dbSession.saveOrUpdate(oimOrder);
          Thread.currentThread().sleep(1000);
          ListOrderItemsRequest itemsRequest = new ListOrderItemsRequest();
          itemsRequest.setSellerId(sellerId);
          itemsRequest.setMWSAuthToken(mwsAuthToken);

          itemsRequest.setAmazonOrderId(amazonOrderId);

          ListOrderItemsResponse listOrderResponse = client.listOrderItems(itemsRequest);
          ListOrderItemsResult listOrderItemsResult = listOrderResponse.getListOrderItemsResult();
          Set<OimOrderDetails> detailSet = new HashSet<OimOrderDetails>();
          try {
            Thread.currentThread().sleep(1000);
          } catch (InterruptedException e) {
            log.warn(e.getMessage());
          }
          for (OrderItem orderItem : listOrderItemsResult.getOrderItems()) {
            OimOrderDetails details = new OimOrderDetails();
            double itemPrice = Double.parseDouble(orderItem.getItemPrice().getAmount());
            // Amazon returns total price for this order item
            // which needs to be divided by quantity before
            // saving.
            itemPrice = itemPrice / orderItem.getQuantityOrdered();
            details.setCostPrice(itemPrice);
            details.setInsertionTm(new Date());
            details
                .setOimOrderStatuses(new OimOrderStatuses(OimConstants.ORDER_STATUS_UNPROCESSED));
            String sku = orderItem.getSellerSKU();
            OimSuppliers oimSuppliers = null;
            for (String prefix : supplierMap.keySet()) {
              if (sku.startsWith(prefix)) {
                oimSuppliers = supplierMap.get(prefix);
                break;
              }
            }
            if (oimSuppliers != null) {
              details.setOimSuppliers(oimSuppliers);
            }
            details.setProductDesc(orderItem.getTitle());
            details.setProductName(orderItem.getTitle());
            details.setQuantity(orderItem.getQuantityOrdered());
            details.setSalePrice(itemPrice);
            details.setSku(orderItem.getSellerSKU());
            details.setStoreOrderItemId(orderItem.getOrderItemId());
            details.setOimOrders(oimOrder);
            m_dbSession.save(details);
            detailSet.add(details);

          }
          oimOrder.setOimOrderDetailses(detailSet);
          m_dbSession.saveOrUpdate(oimOrder);
          numOrdersSaved++;

          Message message = new Message();
          message.setMessageID(BigInteger.valueOf(numOrdersSaved));

          ackAmazonEnvelope.getMessage().add(message);
          OrderAcknowledgement acknowledgement = new OrderAcknowledgement();
          message.setOrderAcknowledgement(acknowledgement);
          acknowledgement.setAmazonOrderID(amazonOrderId);
          acknowledgement.setMerchantOrderID(oimOrder.getOrderId().toString());
          acknowledgement.setStatusCode(m_orderProcessingRule.getConfirmedStatus());

        }
        lastPass = false;
        if (nextToken != null) {
          Thread.currentThread().sleep(1000);
          ListOrdersByNextTokenRequest listOrderByNextTokenReq = new ListOrdersByNextTokenRequest(
              sellerId, mwsAuthToken, nextToken);
          ListOrdersByNextTokenResponse listOrdersByNextTokenResponse = client
              .listOrdersByNextToken(listOrderByNextTokenReq);
          rhmd = listOrdersByNextTokenResponse.getResponseHeaderMetadata();
          orderList = listOrdersByNextTokenResponse.getListOrdersByNextTokenResult().getOrders();
          if (listOrdersByNextTokenResponse.getListOrdersByNextTokenResult().isSetNextToken()) {
            nextToken = listOrdersByNextTokenResponse.getListOrdersByNextTokenResult()
                .getNextToken();
          } else {
            nextToken = null;
            lastPass = true;
          }
        }
      } while (nextToken != null || lastPass);

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      if (marshaller != null) {
        try {
          log.info("OrderAcknowledgemtFeed: {}", os.toString());
          marshaller.marshal(ackAmazonEnvelope, os);
        } catch (JAXBException e) {
          log.error(e.getMessage(), e);
          throw new ChannelOrderFormatException(
              "Error in parsing ackAmazonEnvelope - " + e.getMessage(), e);
        }
      }
      InputStream orderAcknowledgement = new ByteArrayInputStream(os.toByteArray());
      orderAckSubmitFeedRequest.setFeedContent(orderAcknowledgement);

      try {
        orderAckSubmitFeedRequest.setContentMD5(
            Base64.encode((MessageDigest.getInstance("MD5").digest(os.toByteArray()))));
      } catch (NoSuchAlgorithmException e) {
        log.error(e.getMessage(), e);
        throw new ChannelCommunicationException(
            "Error in sending order acknowledgement - " + e.getMessage(), e);
      }
      m_dbSession.persist(batch);
      m_channel.setLastFetchTm(new Date());
      m_dbSession.persist(m_channel);
      tx.commit();
      try {
        service.submitFeed(orderAckSubmitFeedRequest);
      } catch (MarketplaceWebServiceException e) {
        log.error(e.getMessage(), e);
        throw new ChannelCommunicationException(
            "Error in sending order acknoledgement - " + e.getMessage(), e);
      }
      log.debug("Finished importing orders...");
    } catch (RuntimeException e) {
      if (tx != null && tx.isActive())
        tx.rollback();
      log.error(e.getMessage(), e);
      throw new ChannelOrderFormatException(e.getMessage(), e);

    } catch (InterruptedException e1) {
      log.error(e1.getMessage());
    }
    log.info("Returning Order batch with size: {}", batch.getOimOrderses().size());
  }

  @Override
  public boolean updateStoreOrder(OimOrderDetails oimOrderDetails, OrderStatus orderStatus)
      throws ChannelCommunicationException, ChannelOrderFormatException {
    if (!orderStatus.isShipped()) {
      return true;
    }
    SubmitFeedRequest submitFeedRequest = new SubmitFeedRequest();
    submitFeedRequest.setMerchant(sellerId);
    submitFeedRequest.setMWSAuthToken(mwsAuthToken);
    submitFeedRequest.setMarketplaceIdList(new IdList(marketPlaceIdList));
    submitFeedRequest.setFeedType("_POST_ORDER_FULFILLMENT_DATA_");
    try {
      Marshaller marshaller = null;
      try {
        marshaller = jaxbContext.createMarshaller();
      } catch (JAXBException e) {
        log.error(e.getMessage(), e);
        throw new ChannelOrderFormatException(e.getMessage(), e);
      }
      AmazonEnvelope envelope = new AmazonEnvelope();
      Header header = new Header();
      header.setDocumentVersion("1.01");
      header.setMerchantIdentifier(sellerId);
      envelope.setHeader(header);
      envelope.setMessageType("OrderFulfillment");
      long msgId = 1L;
      for (TrackingData td : orderStatus.getTrackingData()) {
        Message message = new Message();
        message.setMessageID(BigInteger.valueOf(msgId++));
        envelope.getMessage().add(message);
        OrderFulfillment fulfillment = new OrderFulfillment();
        message.setOrderFulfillment(fulfillment);
        fulfillment.setAmazonOrderID(oimOrderDetails.getOimOrders().getStoreOrderId());
        fulfillment.setMerchantFulfillmentID(
            BigInteger.valueOf(oimOrderDetails.getOimOrders().getOrderId().longValue()));
        fulfillment.setFulfillmentDate(td.getShipDate());
        Item i = new Item();
        i.setAmazonOrderItemCode(oimOrderDetails.getStoreOrderItemId());
        i.setQuantity(BigInteger.valueOf(td.getQuantity()));
        i.setMerchantFulfillmentItemID(BigInteger.valueOf(oimOrderDetails.getDetailId()));
        FulfillmentData value = new FulfillmentData();
        // value.setCarrierCode(orderStatus.getTrackingData().getCarrierCode());
        value.setCarrierName(td.getCarrierName());
        value.setShipperTrackingNumber(td.getShipperTrackingNumber());
        value.setShippingMethod(td.getShippingMethod());
        fulfillment.getItem().add(i);
        fulfillment.setFulfillmentData(value);
      }

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      if (marshaller != null) {
        try {
          marshaller.marshal(envelope, os);
        } catch (JAXBException e) {
          log.error(e.getMessage(), e);
          throw new ChannelOrderFormatException("Error in Updating Store order - " + e.getMessage(),
              e);
        }
      }
      InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
      submitFeedRequest.setFeedContent(inputStream);
      try {
        submitFeedRequest.setContentMD5(
            Base64.encode((MessageDigest.getInstance("MD5").digest(os.toByteArray()))));
      } catch (NoSuchAlgorithmException e) {
        log.error(e.getMessage(), e);
        throw new ChannelCommunicationException(
            "Error in submiting feed request while updating order to store - " + e.getMessage(), e);
      }
      log.info("SubmitFeedRequest: {}", os.toString());
      SubmitFeedResponse submitFeed = null;
      try {
        Thread.sleep(60 * 1000);
        submitFeed = service.submitFeed(submitFeedRequest);
        log.info(submitFeed.toXML());
      } catch (MarketplaceWebServiceException e) {
        log.error(e.getMessage(), e);
        throw new ChannelCommunicationException(
            "Error in submiting feed request while updating order to store - " + e.getMessage(), e);
      } catch (InterruptedException e) {
        log.error(e.getMessage(), e);
      }

      return true;
    } catch (RuntimeException e) {
      log.error(e.getMessage(), e);
      throw new ChannelOrderFormatException(e.getMessage(), e);
    }
  }

  @Override
  public void cancelOrder(OimOrders oimOrder) {
    // TODO Auto-generated method stub

  }

  @Override
  public void cancelOrder(OimOrderDetails oimOrder) {
    // TODO Auto-generated method stub

  }
}
