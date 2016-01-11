package salesmachine.oim.stores.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.axis.encoding.Base64;
import org.apache.axis.utils.ByteArrayOutputStream;
import org.hibernate.HibernateException;
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

class AmazonOrderImport extends ChannelBase implements IOrderImport {
  private static final Logger log = LoggerFactory.getLogger(AmazonOrderImport.class);
  private String sellerId, mwsAuthToken;
  private List<String> marketPlaceIdList = null;
  private static final MarketplaceWebService feedMwsClient;
  private static final MarketplaceWebServiceOrdersClient orderMwsClient;
  private static JAXBContext jaxbContext;

  static {
    MarketplaceWebServiceConfig config = new MarketplaceWebServiceConfig();
    config.setServiceURL(ApplicationProperties.getProperty(ApplicationProperties.MWS_SERVICE_URL));
    feedMwsClient = new MarketplaceWebServiceClient(
        ApplicationProperties.getProperty(ApplicationProperties.MWS_ACCESS_KEY),
        ApplicationProperties.getProperty(ApplicationProperties.MWS_SECRET_KEY),
        ApplicationProperties.getProperty(ApplicationProperties.MWS_APP_NAME),
        ApplicationProperties.getProperty(ApplicationProperties.MWS_APP_VERSION), config);
    // Get a client connection. Make sure you've set the variables in
    // MarketplaceWebServiceOrdersClientConfig.
    orderMwsClient = MarketplaceWebServiceOrdersClientConfig.getClient();
    try {
      jaxbContext = JAXBContext.newInstance(OrderFulfillment.class, SubmitFeedRequest.class,
          OrderAcknowledgement.class, AmazonEnvelope.class);
    } catch (JAXBException e) {
      log.error(e.getMessage(), e);
    }
  }

  @Override
  public boolean init(OimChannels oimChannel, Session dbSession)
      throws ChannelConfigurationException {
    super.init(oimChannel, dbSession);
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
    String amazonOrderId=null;
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
      List<Message> msgList = new ArrayList<Message>();
      Calendar c = Calendar.getInstance();
//      if (m_channel.getLastFetchTm() != null) {
//        c.setTime(m_channel.getLastFetchTm());
//        c.add(Calendar.HOUR, -2);
//      } else {
//        c.setTime(new Date());
//        c.add(Calendar.DATE, -5);
//      }
      c.setTime(new Date());
      c.add(Calendar.HOUR, -24);
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
      ListOrdersResponse response = orderMwsClient.listOrders(listOrdersRequest);
      List<Order> orderList = response.getListOrdersResult().getOrders();
      ResponseHeaderMetadata rhmd = response.getResponseHeaderMetadata();
      if (response.getListOrdersResult().isSetNextToken())
        nextToken = response.getListOrdersResult().getNextToken();

      do {
        log.info("Response: {}", rhmd.toString());

        log.info("Total order(s) fetched: {}", orderList.size());
        for (Order order : orderList) {
          amazonOrderId = order.getAmazonOrderId();
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

          oimOrder.setDeliveryCountryCode(order.getShippingAddress().getCountryCode());
          m_dbSession.saveOrUpdate(oimOrder);
          Thread.sleep(1000);
          ListOrderItemsRequest itemsRequest = new ListOrderItemsRequest();
          itemsRequest.setSellerId(sellerId);
          itemsRequest.setMWSAuthToken(mwsAuthToken);

          itemsRequest.setAmazonOrderId(amazonOrderId);

          ListOrderItemsResponse listOrderResponse = orderMwsClient.listOrderItems(itemsRequest);
          ListOrderItemsResult listOrderItemsResult = listOrderResponse.getListOrderItemsResult();
          Set<OimOrderDetails> detailSet = new HashSet<OimOrderDetails>();
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            log.warn(e.getMessage());
          }
          for (OrderItem orderItem : listOrderItemsResult.getOrderItems()) {
            OimOrderDetails details = new OimOrderDetails();
            double itemPrice = Double.parseDouble(orderItem.getItemPrice().getAmount());
            // Amazon returns total price for this order item which needs to be divided by quantity
            // before saving.
            itemPrice = itemPrice / orderItem.getQuantityOrdered();
            details.setCostPrice(itemPrice);
            details.setInsertionTm(new Date());
            details
                .setOimOrderStatuses(new OimOrderStatuses(OimConstants.ORDER_STATUS_UNPROCESSED));
            String sku = orderItem.getSellerSKU();
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

          msgList.add(message);
          OrderAcknowledgement acknowledgement = new OrderAcknowledgement();
          message.setOrderAcknowledgement(acknowledgement);
          acknowledgement.setAmazonOrderID(amazonOrderId);
          acknowledgement.setMerchantOrderID(oimOrder.getOrderId().toString());
          acknowledgement.setStatusCode(m_orderProcessingRule.getConfirmedStatus());
          for (OimOrderDetails oimOrderDetails : oimOrder.getOimOrderDetailses()) {
            OrderAcknowledgement.Item item = new OrderAcknowledgement.Item();
            item.setAmazonOrderItemCode(oimOrderDetails.getStoreOrderItemId());
            item.setMerchantOrderItemID(oimOrderDetails.getDetailId().toString());
            acknowledgement.getItem().add(item);
          }
        }
        lastPass = false;
        if (nextToken != null) {
          Thread.sleep(1000);
          ListOrdersByNextTokenRequest listOrderByNextTokenReq = new ListOrdersByNextTokenRequest(
              sellerId, mwsAuthToken, nextToken);
          ListOrdersByNextTokenResponse listOrdersByNextTokenResponse = orderMwsClient
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

      m_dbSession.persist(batch);
      m_channel.setLastFetchTm(new Date());
      m_dbSession.persist(m_channel);
      tx.commit();

      if (msgList.isEmpty()) {
        log.debug("Acknowledegement message list is empty.");
      } else if (m_channel.getTestMode() == 1) {
        log.warn("Acknowledgement to channel was not sent as Channel is set to test mode.");
      } else {
        // Need not to send acknowledgement request to amazon.
        createAndSendAmazonEnvelopeFor("_POST_ORDER_ACKNOWLEDGEMENT_DATA_", "OrderAcknowledgement",
            msgList);
      }
      log.debug("Finished importing orders...");
    } 
    catch(HibernateException e){
      log.error("Error occured during saving the pulled order. store order id - " + amazonOrderId, e);
      try {
        m_dbSession.clear();
        tx.rollback();
      } catch (RuntimeException e1) {
        log.error("Couldn’t roll back transaction", e1);
        e1.printStackTrace();
      }
      throw new ChannelOrderFormatException(
          "Error occured during pull of store order id - " + amazonOrderId , e);
    }
    catch (RuntimeException e) {
      if (tx != null && tx.isActive())
        tx.rollback();
      log.error(e.getMessage(), e);
      throw new ChannelOrderFormatException(e.getMessage(), e);

    } catch (InterruptedException e1) {
      log.error(e1.getMessage());
      throw new ChannelOrderFormatException(e1.getMessage(), e1);
    }
    catch (Exception e1) {
      log.error(e1.getMessage());
      throw new ChannelOrderFormatException(e1.getMessage(), e1);
    }
    log.info("Returning Order batch with size: {}", batch.getOimOrderses().size());
  }

  @Override
  public void updateStoreOrder(OimOrderDetails oimOrderDetails, OrderStatus orderStatus)
      throws ChannelCommunicationException, ChannelOrderFormatException {
    if (!orderStatus.isShipped()) {
      return;
    }

    try {

      List<Message> msgList = new ArrayList<Message>();
      long msgId = 1L;
      for (TrackingData td : orderStatus.getTrackingData()) {
        Message message = new Message();
        message.setMessageID(BigInteger.valueOf(msgId++));
        msgList.add(message);
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

      if (msgList.isEmpty())
        return;// Need not to send a request to amazon.
      createAndSendAmazonEnvelopeFor("_POST_ORDER_FULFILLMENT_DATA_", "OrderFulfillment", msgList);
    } catch (RuntimeException e) {
      log.error(e.getMessage(), e);
      throw new ChannelOrderFormatException(e.getMessage(), e);
    }
  }

  private void createAndSendAmazonEnvelopeFor(String feedType, String messageType,
      List<Message> msgs) throws ChannelOrderFormatException, ChannelCommunicationException {

    SubmitFeedRequest submitFeedRequest = new SubmitFeedRequest();
    submitFeedRequest.setMerchant(sellerId);
    submitFeedRequest.setMWSAuthToken(mwsAuthToken);
    submitFeedRequest.setMarketplaceIdList(new IdList(marketPlaceIdList));
    submitFeedRequest.setFeedType(feedType);

    AmazonEnvelope envelope = new AmazonEnvelope();
    Header header = new Header();
    header.setDocumentVersion("1.01");
    header.setMerchantIdentifier(sellerId);
    envelope.setHeader(header);
    envelope.setMessageType(messageType);
    envelope.getMessage().addAll(msgs);

    Marshaller marshaller = null;
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      marshaller = jaxbContext.createMarshaller();
      marshaller.marshal(envelope, os);
      InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
      submitFeedRequest.setFeedContent(inputStream);
      submitFeedRequest.setContentMD5(
          Base64.encode((MessageDigest.getInstance("MD5").digest(os.toByteArray()))));
      log.info("SubmitFeedRequest: {}", os.toString());
      SubmitFeedResponse submitFeed = null;

      Thread.sleep(60 * 1000);
      submitFeed = feedMwsClient.submitFeed(submitFeedRequest);
      log.info(submitFeed.toXML());
    } catch (NoSuchAlgorithmException e) {
      log.error(e.getMessage(), e);
      throw new ChannelCommunicationException(
          "Error in submiting feed request while updating order to store - " + e.getMessage(), e);
    } catch (JAXBException | IOException e) {
      log.error(e.getMessage(), e);
      throw new ChannelOrderFormatException("Error in Updating Store order - " + e.getMessage(), e);
    } catch (MarketplaceWebServiceException e) {
      log.error(e.getMessage(), e);
      throw new ChannelCommunicationException(
          "Error in submiting feed request while updating order to store - " + e.getMessage(), e);
    } catch (InterruptedException e) {
      log.error(e.getMessage(), e);
    }
  }

  @Override
  public void cancelOrder(OimOrders oimOrder)
      throws ChannelOrderFormatException, ChannelCommunicationException {
    List<Message> msgList = new ArrayList<>();
    int msgId = 1;
    Message message = new Message();
    message.setMessageID(BigInteger.valueOf(msgId++));

    msgList.add(message);
    OrderAcknowledgement acknowledgement = new OrderAcknowledgement();
    message.setOrderAcknowledgement(acknowledgement);
    acknowledgement.setAmazonOrderID(oimOrder.getStoreOrderId());
    acknowledgement.setMerchantOrderID(oimOrder.getOrderId().toString());
    acknowledgement.setStatusCode(m_orderProcessingRule.getFailedStatus());

    for (OimOrderDetails oimOrderDetails : oimOrder.getOimOrderDetailses()) {
      OrderAcknowledgement.Item item = new OrderAcknowledgement.Item();
      item.setAmazonOrderItemCode(oimOrderDetails.getStoreOrderItemId());
      item.setMerchantOrderItemID(oimOrderDetails.getDetailId().toString());
      item.setCancelReason("NoInventory");
      acknowledgement.getItem().add(item);
    }
    createAndSendAmazonEnvelopeFor("_POST_ORDER_ACKNOWLEDGEMENT_DATA_", "OrderAcknowledgement",
        msgList);
  }

  @Override
  public void cancelOrder(OimOrderDetails oimOrderDetails)
      throws ChannelOrderFormatException, ChannelCommunicationException {
    List<Message> msgList = new ArrayList<>();
    int msgId = 1;
    Message message = new Message();
    message.setMessageID(BigInteger.valueOf(msgId++));

    msgList.add(message);
    OrderAcknowledgement acknowledgement = new OrderAcknowledgement();
    message.setOrderAcknowledgement(acknowledgement);
    acknowledgement.setAmazonOrderID(oimOrderDetails.getOimOrders().getStoreOrderId());
    acknowledgement.setMerchantOrderID(oimOrderDetails.getOimOrders().getOrderId().toString());
    acknowledgement.setStatusCode(m_orderProcessingRule.getFailedStatus());

    OrderAcknowledgement.Item item = new OrderAcknowledgement.Item();
    item.setAmazonOrderItemCode(oimOrderDetails.getStoreOrderItemId());
    item.setMerchantOrderItemID(oimOrderDetails.getDetailId().toString());
    item.setCancelReason("NoInventory");
    acknowledgement.getItem().add(item);
    createAndSendAmazonEnvelopeFor("_POST_ORDER_ACKNOWLEDGEMENT_DATA_", "OrderAcknowledgement",
        msgList);

  }
}
