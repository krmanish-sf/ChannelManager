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
	public boolean init(int channelID, Session dbSession)
			throws ChannelConfigurationException {
		super.init(channelID, dbSession);
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
			throw new ChannelConfigurationException(
					"Channel setup is not correct. Please provide correct details.");
		}
		return true;
	}

	@Override
	public void getVendorOrders(OimOrderBatchesTypes batchesTypes,
			OimOrderBatches batch) throws ChannelCommunicationException,
			ChannelOrderFormatException, ChannelConfigurationException {
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

			// Get a client connection.
			// Make sure you've set the variables in
			// MarketplaceWebServiceOrdersClientConfig.
			MarketplaceWebServiceOrdersClient client = MarketplaceWebServiceOrdersClientConfig
					.getClient();

			// Create a request.
			ListOrdersRequest listOrdersRequest = new ListOrdersRequest();
			listOrdersRequest.setSellerId(sellerId);
			listOrdersRequest.setMWSAuthToken(mwsAuthToken);
			listOrdersRequest.setMarketplaceId(marketPlaceIdList);

			if (!StringHandle.isNullOrEmpty(m_orderProcessingRule
					.getPullWithStatus())) {
				List<String> pullWithStatus = new ArrayList<String>();
				String[] split = m_orderProcessingRule.getPullWithStatus()
						.split(",");
				for (String pullStatus : split) {
					pullWithStatus.add(pullStatus);
				}
				listOrdersRequest.setOrderStatus(pullWithStatus);
			}
			SubmitFeedRequest orderAckSubmitFeedRequest = new SubmitFeedRequest();
			orderAckSubmitFeedRequest.setMerchant(sellerId);
			orderAckSubmitFeedRequest.setMWSAuthToken(mwsAuthToken);
			orderAckSubmitFeedRequest.setMarketplaceIdList(new IdList(
					marketPlaceIdList));
			orderAckSubmitFeedRequest
					.setFeedType("_POST_ORDER_ACKNOWLEDGEMENT_DATA_");

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
			XMLGregorianCalendar createdAfter = MwsUtl.getDTF()
					.newXMLGregorianCalendar();
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

				for (Order order2 : orderList) {
					String amazonOrderId = order2.getAmazonOrderId();
					if (orderAlreadyImported(amazonOrderId)) {
						log.warn(
								"Order#{} is already imported in the system, skipping it.",
								amazonOrderId);
						continue;
					}
					log.info("Order#{} fetched.", amazonOrderId);
					OimOrders oimOrders = new OimOrders();
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
						oimOrders.setCustomerCompany(order2
								.getShippingAddress().getAddressLine3());
						oimOrders.setCustomerCountry(order2
								.getShippingAddress().getCounty());
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
						oimOrders.setDeliveryCompany(order2
								.getShippingAddress().getAddressLine3());
						oimOrders.setDeliveryCountry(order2
								.getShippingAddress().getCounty());
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
					batch.getOimOrderses().add(oimOrders);
					// oimOrders.setOrderComment(order2.);
					oimOrders.setOrderFetchTm(new Date());
					oimOrders.setOrderTm(order2.getPurchaseDate()
							.toGregorianCalendar().getTime());
					oimOrders.setOrderTotalAmount(Double.parseDouble(order2
							.getOrderTotal().getAmount()));
					oimOrders.setPayMethod(order2.getPaymentMethod());
					oimOrders.setShippingDetails(order2.getShipServiceLevel());
					String shippingDetails = order2.getShipServiceLevel();
					for (OimChannelShippingMap entity : oimChannelShippingMapList) {
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
					Thread.currentThread().sleep(1000);
					ListOrderItemsRequest itemsRequest = new ListOrderItemsRequest();
					itemsRequest.setSellerId(sellerId);
					itemsRequest.setMWSAuthToken(mwsAuthToken);

					itemsRequest.setAmazonOrderId(amazonOrderId);

					ListOrderItemsResponse listOrderResponse = client
							.listOrderItems(itemsRequest);
					ListOrderItemsResult listOrderItemsResult = listOrderResponse
							.getListOrderItemsResult();
					Set<OimOrderDetails> detailSet = new HashSet<OimOrderDetails>();
					try {
						Thread.currentThread().sleep(1000);
					} catch (InterruptedException e) {
						log.warn(e.getMessage());
					}
					for (OrderItem orderItem : listOrderItemsResult
							.getOrderItems()) {
						OimOrderDetails details = new OimOrderDetails();
						double itemPrice = Double.parseDouble(orderItem
								.getItemPrice().getAmount());
						// Amazon returns total price for this order item
						// which needs to be divided by quantity before
						// saving.
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

					Message message = new Message();
					message.setMessageID(BigInteger.valueOf(numOrdersSaved));

					ackAmazonEnvelope.getMessage().add(message);
					OrderAcknowledgement acknowledgement = new OrderAcknowledgement();
					message.setOrderAcknowledgement(acknowledgement);
					acknowledgement.setAmazonOrderID(amazonOrderId);
					acknowledgement.setMerchantOrderID(oimOrders.getOrderId()
							.toString());
					acknowledgement.setStatusCode(m_orderProcessingRule
							.getConfirmedStatus());

				}
				lastPass = false;
				if (nextToken != null) {
					Thread.currentThread().sleep(1000);
					ListOrdersByNextTokenRequest listOrderByNextTokenReq = new ListOrdersByNextTokenRequest(
							sellerId, mwsAuthToken, nextToken);
					ListOrdersByNextTokenResponse listOrdersByNextTokenResponse = client
							.listOrdersByNextToken(listOrderByNextTokenReq);
					rhmd = listOrdersByNextTokenResponse
							.getResponseHeaderMetadata();
					orderList = listOrdersByNextTokenResponse
							.getListOrdersByNextTokenResult().getOrders();
					if (listOrdersByNextTokenResponse
							.getListOrdersByNextTokenResult().isSetNextToken()) {
						nextToken = listOrdersByNextTokenResponse
								.getListOrdersByNextTokenResult()
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
					marshaller.marshal(ackAmazonEnvelope, os);
				} catch (JAXBException e) {
					log.error(e.getMessage(), e);
					throw new ChannelOrderFormatException(
							"Error in parsing ackAmazonEnvelope - "
									+ e.getMessage(), e);
				}
			}
			InputStream orderAcknowledgement = new ByteArrayInputStream(
					os.toByteArray());
			orderAckSubmitFeedRequest.setFeedContent(orderAcknowledgement);

			try {
				orderAckSubmitFeedRequest.setContentMD5(Base64
						.encode((MessageDigest.getInstance("MD5").digest(os
								.toByteArray()))));
			} catch (NoSuchAlgorithmException e) {
				log.error(e.getMessage(), e);
				throw new ChannelCommunicationException(
						"Error in sending order acknowledgement - "
								+ e.getMessage(), e);
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
						"Error in sending order acknoledgement - "
								+ e.getMessage(), e);
			}
			log.debug("Finished importing orders...");
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			log.error(e.getMessage(), e);
			throw new ChannelOrderFormatException(e.getMessage(), e);

		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		log.info("Returning Order batch with size: {}", batch.getOimOrderses()
				.size());
	}

	@Override
	public boolean updateStoreOrder(OimOrderDetails oimOrderDetails,
			OrderStatus orderStatus) throws ChannelCommunicationException,
			ChannelOrderFormatException {
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
				fulfillment.setAmazonOrderID(oimOrderDetails.getOimOrders()
						.getStoreOrderId());
				fulfillment.setMerchantFulfillmentID(BigInteger
						.valueOf(oimOrderDetails.getOimOrders().getOrderId()
								.longValue()));
				fulfillment.setFulfillmentDate(td.getShipDate());
				Item i = new Item();
				i.setAmazonOrderItemCode(oimOrderDetails.getStoreOrderItemId());
				i.setQuantity(BigInteger.valueOf(td.getQuantity()));
				i.setMerchantFulfillmentItemID(BigInteger
						.valueOf(oimOrderDetails.getDetailId()));
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
					throw new ChannelOrderFormatException(
							"Error in Updating Store order - " + e.getMessage(),
							e);
				}
			}
			InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
			submitFeedRequest.setFeedContent(inputStream);
			try {
				submitFeedRequest.setContentMD5(Base64.encode((MessageDigest
						.getInstance("MD5").digest(os.toByteArray()))));
			} catch (NoSuchAlgorithmException e) {
				log.error(e.getMessage(), e);
				throw new ChannelCommunicationException(
						"Error in submiting feed request while updating order to store - "
								+ e.getMessage(), e);
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
						"Error in submiting feed request while updating order to store - "
								+ e.getMessage(), e);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}

			return true;
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
			throw new ChannelOrderFormatException(e.getMessage(), e);
		}
	}
}
