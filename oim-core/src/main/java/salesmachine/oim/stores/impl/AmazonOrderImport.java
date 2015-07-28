package salesmachine.oim.stores.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import salesmachine.oim.stores.modal.amazon.AmazonEnvelope;
import salesmachine.oim.stores.modal.amazon.AmazonEnvelope.Message;
import salesmachine.oim.stores.modal.amazon.Header;
import salesmachine.oim.stores.modal.amazon.OrderAcknowledgement;
import salesmachine.oim.stores.modal.amazon.OrderFulfillment;
import salesmachine.oim.stores.modal.amazon.OrderFulfillment.FulfillmentData;
import salesmachine.oim.stores.modal.amazon.OrderFulfillment.Item;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.util.ApplicationProperties;
import salesmachine.util.OimLogStream;
import salesmachine.util.StringHandle;

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
import com.amazonservices.mws.orders._2013_09_01.model.ListOrdersRequest;
import com.amazonservices.mws.orders._2013_09_01.model.ListOrdersResponse;
import com.amazonservices.mws.orders._2013_09_01.model.Order;
import com.amazonservices.mws.orders._2013_09_01.model.OrderItem;
import com.amazonservices.mws.orders._2013_09_01.model.ResponseHeaderMetadata;

public class AmazonOrderImport extends ChannelBase implements IOrderImport {
	private static final Logger log = LoggerFactory
			.getLogger(AmazonOrderImport.class);
	private String sellerId, mwsAuthToken;
	private List<String> marketPlaceIdList = null;
	private static final MarketplaceWebService service;
	private static JAXBContext jaxbContext;
	static {
		MarketplaceWebServiceConfig config = new MarketplaceWebServiceConfig();
		config.setServiceURL(ApplicationProperties
				.getProperty(ApplicationProperties.MWS_SERVICE_URL));
		service = new MarketplaceWebServiceClient(
				ApplicationProperties
						.getProperty(ApplicationProperties.MWS_ACCESS_KEY),
				ApplicationProperties
						.getProperty(ApplicationProperties.MWS_SECRET_KEY),
				ApplicationProperties
						.getProperty(ApplicationProperties.MWS_APP_NAME),
				ApplicationProperties
						.getProperty(ApplicationProperties.MWS_APP_VERSION),
				config);

		try {
			jaxbContext = JAXBContext.newInstance(OrderFulfillment.class,
					SubmitFeedRequest.class, OrderAcknowledgement.class,
					AmazonEnvelope.class);
		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public boolean init(int channelID, Session dbSession, OimLogStream logStream) {
		super.init(channelID, dbSession, logStream);
		sellerId = StringHandle.removeNull(PojoHelper
				.getChannelAccessDetailValue(m_channel,
						OimConstants.CHANNEL_ACCESSDETAIL_AMAZON_SELLERID));
		mwsAuthToken = StringHandle
				.removeNull(PojoHelper
						.getChannelAccessDetailValue(
								m_channel,
								OimConstants.CHANNEL_ACCESSDETAIL_AMAZON_MWS_AUTH_TOKEN));
		String marketPlaceIds = StringHandle
				.removeNull(PojoHelper
						.getChannelAccessDetailValue(
								m_channel,
								OimConstants.CHANNEL_ACCESSDETAIL_AMAZON_MWS_MARKETPLACE_ID));
		String[] split = marketPlaceIds.split(",");
		marketPlaceIdList = new ArrayList<String>();
		for (int i = 0; i < split.length; i++) {
			marketPlaceIdList.add(split[i]);
		}
		if (sellerId.length() == 0 || mwsAuthToken.length() == 0) {
			log.error("Channel setup is not correct. Please provide correct details.");
			this.logStream
					.println("Channel setup is not correct. Please provide correct details.");
			return false;
		}
		return true;
	}

	@Override
	public OimOrderBatches getVendorOrders(OimOrderBatchesTypes batchesTypes) {
		Transaction tx = m_dbSession.getTransaction();
		OimOrderBatches batch = new OimOrderBatches();
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

			// Get a client connection.
			// Make sure you've set the variables in
			// MarketplaceWebServiceOrdersClientConfig.
			MarketplaceWebServiceOrdersClient client = MarketplaceWebServiceOrdersClientConfig
					.getClient();

			// Create a request.
			ListOrdersRequest request = new ListOrdersRequest();
			request.setSellerId(sellerId);
			request.setMWSAuthToken(mwsAuthToken);
			request.setMarketplaceId(marketPlaceIdList);
			if (!StringHandle.isNullOrEmpty(m_orderProcessingRule
					.getPullWithStatus())) {
				List<String> pullWithStatus = new ArrayList<String>();
				String[] split = m_orderProcessingRule.getPullWithStatus()
						.split(",");
				for (String pullStatus : split) {
					pullWithStatus.add(pullStatus);
				}
				request.setOrderStatus(pullWithStatus);
			}
			SubmitFeedRequest submitFeedRequest = new SubmitFeedRequest();
			submitFeedRequest.setMerchant(sellerId);
			submitFeedRequest.setMWSAuthToken(mwsAuthToken);
			submitFeedRequest
					.setMarketplaceIdList(new IdList(marketPlaceIdList));
			submitFeedRequest.setFeedType("_POST_ORDER_ACKNOWLEDGEMENT_DATA_");
			Marshaller marshaller = jaxbContext.createMarshaller();
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.DATE, -14);
			Date cutoffDate = c.getTime();
			XMLGregorianCalendar createdAfter = MwsUtl.getDTF()
					.newXMLGregorianCalendar();
			createdAfter.setYear(cutoffDate.getYear());
			createdAfter.setMonth(cutoffDate.getMonth());
			createdAfter.setDay(cutoffDate.getDate());
			createdAfter.setHour(cutoffDate.getHours());
			createdAfter.setMinute(cutoffDate.getMinutes());
			createdAfter.setSecond(cutoffDate.getSeconds());
			request.setCreatedAfter(createdAfter);
			// Make the call.
			ListOrdersResponse response = client.listOrders(request);
			ResponseHeaderMetadata rhmd = response.getResponseHeaderMetadata();
			// We recommend logging every the request id and timestamp of every
			// call.
			log.debug("Response:");
			log.debug("RequestId: " + rhmd.getRequestId());
			log.debug("Timestamp: " + rhmd.getTimestamp());

			log.info("Total order(s) fetched: {}", response
					.getListOrdersResult().getOrders().size());

			boolean newOrder = false;
			int numOrdersSaved = 0;
			for (Order order2 : response.getListOrdersResult().getOrders()) {
				String amazonOrderId = order2.getAmazonOrderId();
				log.info("Order#{} fetched.", amazonOrderId);
				OimOrders oimOrders = null;
				if (orderAlreadyImported(amazonOrderId)) {
					log.info(
							"Order#{} is already imported in the system, updating Order.",
							amazonOrderId);
					Query query = m_dbSession
							.createQuery("select o from salesmachine.hibernatedb.OimOrders o where o.oimOrderBatches.oimChannels=:chan and o.storeOrderId=:storeOrderId");
					query.setEntity("chan", m_channel);
					query.setString("storeOrderId", amazonOrderId);
					Iterator iter = query.iterate();
					while (iter.hasNext()) {
						oimOrders = (OimOrders) iter.next();
					}
				}
				if (oimOrders == null) {
					oimOrders = new OimOrders();
					newOrder = true;
				}
				oimOrders.setStoreOrderId(amazonOrderId);
				if (order2.getShippingAddress() != null) {
					oimOrders.setBillingCity(order2.getShippingAddress()
							.getCity());
					oimOrders.setBillingCompany(order2.getShippingAddress()
							.getAddressLine3());
					oimOrders.setBillingCountry(order2.getShippingAddress()
							.getCounty());
					oimOrders.setBillingEmail(order2.getBuyerEmail());
					oimOrders.setBillingName(order2.getShippingAddress()
							.getName());
					oimOrders.setBillingPhone(order2.getShippingAddress()
							.getPhone());
					oimOrders.setBillingState(order2.getShippingAddress()
							.getStateOrRegion());
					oimOrders.setBillingStreetAddress(order2
							.getShippingAddress().getAddressLine1());
					oimOrders.setBillingSuburb(order2.getShippingAddress()
							.getAddressLine2());
					oimOrders.setBillingZip(order2.getShippingAddress()
							.getPostalCode());

					oimOrders.setCustomerCity(order2.getShippingAddress()
							.getCity());
					oimOrders.setCustomerCompany(order2.getShippingAddress()
							.getAddressLine3());
					oimOrders.setCustomerCountry(order2.getShippingAddress()
							.getCounty());
					oimOrders.setCustomerEmail(order2.getBuyerEmail());
					oimOrders.setCustomerName(order2.getShippingAddress()
							.getName());
					oimOrders.setCustomerPhone(order2.getShippingAddress()
							.getPhone());
					oimOrders.setCustomerState(order2.getShippingAddress()
							.getStateOrRegion());
					oimOrders.setCustomerStreetAddress(order2
							.getShippingAddress().getAddressLine1());
					oimOrders.setCustomerSuburb(order2.getShippingAddress()
							.getAddressLine2());
					oimOrders.setCustomerZip(order2.getShippingAddress()
							.getPostalCode());

					oimOrders.setDeliveryCity(order2.getShippingAddress()
							.getCity());
					oimOrders.setDeliveryCompany(order2.getShippingAddress()
							.getAddressLine3());
					oimOrders.setDeliveryCountry(order2.getShippingAddress()
							.getCounty());
					oimOrders.setDeliveryEmail(order2.getBuyerEmail());
					oimOrders.setDeliveryName(order2.getShippingAddress()
							.getName());
					oimOrders.setDeliveryPhone(order2.getShippingAddress()
							.getPhone());
					oimOrders.setDeliveryState(order2.getShippingAddress()
							.getStateOrRegion());
					oimOrders.setDeliveryStreetAddress(order2
							.getShippingAddress().getAddressLine1());
					oimOrders.setDeliverySuburb(order2.getShippingAddress()
							.getAddressLine2());
					oimOrders.setDeliveryZip(order2.getShippingAddress()
							.getPostalCode());
				}
				oimOrders.setInsertionTm(new Date());
				oimOrders.setOimOrderBatches(batch);
				// oimOrders.setOrderComment(order2.);
				oimOrders.setOrderFetchTm(new Date());
				oimOrders.setOrderTm(order2.getPurchaseDate()
						.toGregorianCalendar().getTime());
				oimOrders.setOrderTotalAmount(Double.parseDouble(order2
						.getOrderTotal().getAmount()));
				oimOrders.setPayMethod(order2.getPaymentMethod());
				oimOrders.setShippingDetails(order2.getShipServiceLevel());
				String shippingDetails = order2.getShipServiceLevel();
				Integer supportedChannelId = m_channel
						.getOimSupportedChannels().getSupportedChannelId();
				Criteria findCriteria = m_dbSession
						.createCriteria(OimChannelShippingMap.class);
				findCriteria.add(Restrictions.eq(
						"oimSupportedChannel.supportedChannelId",
						supportedChannelId));
				List<OimChannelShippingMap> list = findCriteria.list();
				for (OimChannelShippingMap entity : list) {
					String shippingRegEx = entity.getShippingRegEx();
					if (shippingDetails.equalsIgnoreCase(shippingRegEx)) {
						oimOrders.setOimShippingMethod(entity
								.getOimShippingMethod());
						log.info("Shipping set to "
								+ entity.getOimShippingMethod());
						break;
					}
				}

				if (oimOrders.getOimShippingMethod() == null)
					log.warn("Shipping can't be mapped for order "
							+ oimOrders.getStoreOrderId());
				// setting delivery state code 
				if (order2.getShippingAddress().getStateOrRegion().length() == 2) {
					oimOrders.setDeliveryStateCode(order2
							.getShippingAddress().getStateOrRegion());
				} else {
					String stateCode = validateAndGetStateCode(oimOrders);
					if (stateCode != "") 
						oimOrders.setDeliveryStateCode(stateCode); 
				}
				
				m_dbSession.saveOrUpdate(oimOrders);
				ListOrderItemsRequest itemsRequest = new ListOrderItemsRequest();
				itemsRequest.setSellerId(sellerId);
				itemsRequest.setMWSAuthToken(mwsAuthToken);

				itemsRequest.setAmazonOrderId(amazonOrderId);

				ListOrderItemsResponse listOrderResponse = client
						.listOrderItems(itemsRequest);
				ListOrderItemsResult listOrderItemsResult = listOrderResponse
						.getListOrderItemsResult();
				if (newOrder) {
					Set<OimOrderDetails> detailSet = new HashSet<OimOrderDetails>();
					for (OrderItem orderItem : listOrderItemsResult
							.getOrderItems()) {
						OimOrderDetails details = new OimOrderDetails();
						double itemPrice = Double.parseDouble(orderItem
								.getItemPrice().getAmount());
						// Amazon returns total price for this order item which
						// needs to be divided by quantity before saving.
						itemPrice = itemPrice / orderItem.getQuantityOrdered();
						details.setCostPrice(itemPrice);
						details.setInsertionTm(new Date());
						details.setOimOrderStatuses(new OimOrderStatuses(
								OimConstants.ORDER_STATUS_UNPROCESSED));
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
						details.setOimOrders(oimOrders);
						m_dbSession.save(details);
						detailSet.add(details);

					}
					oimOrders.setOimOrderDetailses(detailSet);
					m_dbSession.saveOrUpdate(oimOrders);
					numOrdersSaved++;

					AmazonEnvelope envelope = new AmazonEnvelope();
					Header header = new Header();
					header.setDocumentVersion("1.01");
					header.setMerchantIdentifier(sellerId);
					envelope.setHeader(header);
					envelope.setMessageType("OrderAcknowledgement");
					Message message = new Message();
					message.setMessageID(BigInteger.ONE);

					envelope.getMessage().add(message);
					OrderAcknowledgement acknowledgement = new OrderAcknowledgement();
					message.setOrderAcknowledgement(acknowledgement);
					acknowledgement.setAmazonOrderID(amazonOrderId);
					acknowledgement.setMerchantOrderID(oimOrders.getOrderId()
							.toString());
					acknowledgement.setStatusCode(m_orderProcessingRule
							.getConfirmedStatus());
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					marshaller.marshal(envelope, os);
					InputStream orderAcknowledgement = new ByteArrayInputStream(
							os.toByteArray());
					submitFeedRequest.setFeedContent(orderAcknowledgement);

					submitFeedRequest.setContentMD5(Base64
							.encode((MessageDigest.getInstance("MD5").digest(os
									.toByteArray()))));
					service.submitFeed(submitFeedRequest);
				}
			}
			m_dbSession.persist(batch);
			tx.commit();
			logStream.println("Imported " + numOrdersSaved + " Orders");
			log.debug("Finished importing orders...");
		} catch (Exception e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			log.error(e.getMessage(), e);
			logStream.println("Import Orders failed (" + e.getMessage() + ")");

		}
		log.info("Returning Order batch with size: {}", batch.getOimOrderses()
				.size());
		return batch;
	}

	@Override
	public boolean updateStoreOrder(OimOrderDetails oimOrderDetails,
			OrderStatus orderStatus) {
		if (!orderStatus.isShipped()) {
			return true;
		}
		SubmitFeedRequest submitFeedRequest = new SubmitFeedRequest();
		submitFeedRequest.setMerchant(sellerId);
		submitFeedRequest.setMWSAuthToken(mwsAuthToken);
		submitFeedRequest.setMarketplaceIdList(new IdList(marketPlaceIdList));
		submitFeedRequest.setFeedType("_POST_ORDER_FULFILLMENT_DATA_");
		try {
			Marshaller marshaller = jaxbContext.createMarshaller();
			AmazonEnvelope envelope = new AmazonEnvelope();
			Header header = new Header();
			header.setDocumentVersion("1.01");
			header.setMerchantIdentifier(sellerId);
			envelope.setHeader(header);
			envelope.setMessageType("OrderFulfillment");
			Message message = new Message();
			message.setMessageID(BigInteger.ONE);

			envelope.getMessage().add(message);
			OrderFulfillment fulfillment = new OrderFulfillment();
			message.setOrderFulfillment(fulfillment);
			fulfillment.setAmazonOrderID(oimOrderDetails.getOimOrders()
					.getStoreOrderId());
			fulfillment.setMerchantFulfillmentID(BigInteger
					.valueOf(oimOrderDetails.getOimOrders().getOrderId()
							.longValue()));
			XMLGregorianCalendar cal = MwsUtl.getDTF().newXMLGregorianCalendar(
					orderStatus.getTrackingData().getShipDate());
			GregorianCalendar shipDate = orderStatus.getTrackingData()
					.getShipDate();
			cal.setYear(shipDate.get(GregorianCalendar.YEAR));
			cal.setMonth(shipDate.get(GregorianCalendar.MONTH));
			cal.setDay(shipDate.get(GregorianCalendar.DATE));
			fulfillment.setFulfillmentDate(cal);
			Item i = new Item();
			i.setAmazonOrderItemCode(oimOrderDetails.getStoreOrderItemId());
			i.setQuantity(BigInteger.valueOf(orderStatus.getTrackingData()
					.getQuantity()));
			i.setMerchantFulfillmentItemID(BigInteger.valueOf(oimOrderDetails
					.getDetailId()));
			fulfillment.getItem().add(i);
			FulfillmentData value = new FulfillmentData();
			// value.setCarrierCode(orderStatus.getTrackingData().getCarrierCode());
			value.setCarrierName(orderStatus.getTrackingData().getCarrierName());
			value.setShipperTrackingNumber(orderStatus.getTrackingData()
					.getShipperTrackingNumber());
			value.setShippingMethod(orderStatus.getTrackingData()
					.getShippingMethod());

			fulfillment.setFulfillmentData(value);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			marshaller.marshal(envelope, os);
			InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
			submitFeedRequest.setFeedContent(inputStream);
			submitFeedRequest.setContentMD5(Base64.encode((MessageDigest
					.getInstance("MD5").digest(os.toByteArray()))));
			log.info("SubmitFeedRequest: {}", os.toString());
			SubmitFeedResponse submitFeed = service
					.submitFeed(submitFeedRequest);
			log.info(submitFeed.toXML());
			return true;
		} catch (JAXBException | NoSuchAlgorithmException e) {
			log.error(e.getMessage(), e);
		} catch (MarketplaceWebServiceException e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

}
