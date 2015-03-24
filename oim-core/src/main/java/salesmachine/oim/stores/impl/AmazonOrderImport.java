package salesmachine.oim.stores.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimChannelSupplierMap;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderBatchesTypes;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderProcessingRule;
import salesmachine.hibernatedb.OimOrderStatuses;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSuppliers;
import salesmachine.hibernatehelper.PojoHelper;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.util.OimLogStream;
import salesmachine.util.StringHandle;

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

public class AmazonOrderImport implements IOrderImport {
	private static final Logger log = LoggerFactory
			.getLogger(AmazonOrderImport.class);
	private String sellerId, mwsAuthToken;
	private List<String> marketPlaceIdList = null;
	private Session m_dbSession;
	private OimChannels m_channel;
	private OimOrderProcessingRule m_orderProcessingRule;
	private OimLogStream logStream;

	public boolean getVendorOrders() {
		Transaction tx = null;

		try {
			DecimalFormat df = new DecimalFormat("#.##");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Set suppliers = m_channel.getOimChannelSupplierMaps();
			Map supplierMap = new HashMap();
			Iterator itr = suppliers.iterator();
			while (itr.hasNext()) {
				OimChannelSupplierMap map = (OimChannelSupplierMap) itr.next();
				if (map.getDeleteTm() != null)
					continue;

				String prefix = map.getSupplierPrefix();
				OimSuppliers supplier = map.getOimSuppliers();
				System.out.println("prefix :: " + prefix + "supplierID :: "
						+ supplier.getSupplierId());
				supplierMap.put(prefix, supplier);
			}

			boolean ordersSaved = false;

			OimOrderBatches batch = new OimOrderBatches();
			batch.setOimChannels(m_channel);
			batch.setOimOrderBatchesTypes(new OimOrderBatchesTypes(
					OimConstants.ORDERBATCH_TYPE_ID_AUTOMATED));

			// Save Batch..
			tx = m_dbSession.beginTransaction();
			batch.setInsertionTm(new Date());
			batch.setCreationTm(new Date());
			m_dbSession.save(batch);
			tx.commit();

			tx = m_dbSession.beginTransaction();

			long start = System.currentTimeMillis();

			// Get a client connection.
			// Make sure you've set the variables in
			// MarketplaceWebServiceOrdersSampleConfig.
			MarketplaceWebServiceOrdersClient client = MarketplaceWebServiceOrdersClientConfig
					.getClient();

			// Create a request.
			ListOrdersRequest request = new ListOrdersRequest();

			request.setSellerId(sellerId);
			request.setMWSAuthToken(mwsAuthToken);
			request.setMarketplaceId(marketPlaceIdList);
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

			// Get all the orders for the current channel
			List currentOrders = getCurrentOrders();

			int numOrdersSaved = 0;
			for (Order order2 : response.getListOrdersResult().getOrders()) {
				if (currentOrders.contains(order2.getSellerOrderId())) {
					log.debug("Order is already imported in the system, skipping to next Order.");
					continue;
				}
				OimOrders oimOrders = new OimOrders();
				oimOrders.setBillingCity(order2.getShippingAddress().getCity());
				oimOrders.setBillingCompany(order2.getShippingAddress()
						.getAddressLine3());
				oimOrders.setBillingCountry(order2.getShippingAddress()
						.getCounty());
				oimOrders.setBillingEmail(order2.getBuyerEmail());
				oimOrders.setBillingName(order2.getShippingAddress().getName());
				oimOrders.setBillingPhone(order2.getShippingAddress()
						.getPhone());
				oimOrders.setBillingState(order2.getShippingAddress()
						.getStateOrRegion());
				oimOrders.setBillingStreetAddress(order2.getShippingAddress()
						.getAddressLine1());
				oimOrders.setBillingSuburb(order2.getShippingAddress()
						.getAddressLine2());
				oimOrders.setBillingZip(order2.getShippingAddress()
						.getPostalCode());

				oimOrders
						.setCustomerCity(order2.getShippingAddress().getCity());
				oimOrders.setCustomerCompany(order2.getShippingAddress()
						.getAddressLine3());
				oimOrders.setCustomerCountry(order2.getShippingAddress()
						.getCounty());
				oimOrders.setCustomerEmail(order2.getBuyerEmail());
				oimOrders
						.setCustomerName(order2.getShippingAddress().getName());
				oimOrders.setCustomerPhone(order2.getShippingAddress()
						.getPhone());
				oimOrders.setCustomerState(order2.getShippingAddress()
						.getStateOrRegion());
				oimOrders.setCustomerStreetAddress(order2.getShippingAddress()
						.getAddressLine1());
				oimOrders.setCustomerSuburb(order2.getShippingAddress()
						.getAddressLine2());
				oimOrders.setCustomerZip(order2.getShippingAddress()
						.getPostalCode());

				oimOrders
						.setDeliveryCity(order2.getShippingAddress().getCity());
				oimOrders.setDeliveryCompany(order2.getShippingAddress()
						.getAddressLine3());
				oimOrders.setDeliveryCountry(order2.getShippingAddress()
						.getCounty());
				oimOrders.setDeliveryEmail(order2.getBuyerEmail());
				oimOrders
						.setDeliveryName(order2.getShippingAddress().getName());
				oimOrders.setDeliveryPhone(order2.getShippingAddress()
						.getPhone());
				oimOrders.setDeliveryState(order2.getShippingAddress()
						.getStateOrRegion());
				oimOrders.setDeliveryStreetAddress(order2.getShippingAddress()
						.getAddressLine1());
				oimOrders.setDeliverySuburb(order2.getShippingAddress()
						.getAddressLine2());
				oimOrders.setDeliveryZip(order2.getShippingAddress()
						.getPostalCode());

				oimOrders.setInsertionTm(new Date());
				oimOrders.setOimOrderBatches(batch);
				// oimOrders.setOrderComment(order2);
				oimOrders.setOrderFetchTm(new Date());
				oimOrders.setOrderTm(order2.getPurchaseDate()
						.toGregorianCalendar().getTime());
				oimOrders.setOrderTotalAmount(Double.parseDouble(order2
						.getOrderTotal().getAmount()));
				oimOrders.setPayMethod(order2.getPaymentMethod());
				oimOrders.setShippingDetails(order2.getShipServiceLevel());
				oimOrders.setStoreOrderId(order2.getSellerOrderId());
				m_dbSession.save(oimOrders);
				ListOrderItemsRequest itemsRequest = new ListOrderItemsRequest();
				itemsRequest.setSellerId(sellerId);
				itemsRequest.setMWSAuthToken(mwsAuthToken);
				String amazonOrderId = order2.getAmazonOrderId();
				itemsRequest.setAmazonOrderId(amazonOrderId);

				ListOrderItemsResponse listOrderResponse = client
						.listOrderItems(itemsRequest);
				ListOrderItemsResult listOrderItemsResult = listOrderResponse
						.getListOrderItemsResult();
				Set<OimOrderDetails> detailSet = new HashSet<OimOrderDetails>();
				for (OrderItem orderItem : listOrderItemsResult.getOrderItems()) {
					OimOrderDetails details = new OimOrderDetails();
					details.setCostPrice(Double.parseDouble(orderItem
							.getItemPrice().getAmount()));
					details.setInsertionTm(new Date());
					details.setOimOrderStatuses(new OimOrderStatuses(
							OimConstants.ORDER_STATUS_UNPROCESSED));
					String skuPrefix = orderItem.getSellerSKU().substring(0, 2);
					OimSuppliers oimSuppliers = (OimSuppliers) supplierMap
							.get(skuPrefix);
					if (oimSuppliers != null) {
						details.setOimSuppliers(oimSuppliers);
					}
					details.setProductDesc(orderItem.getTitle());
					details.setProductName(orderItem.getTitle());
					details.setQuantity(orderItem.getQuantityOrdered());
					details.setSalePrice(Double.parseDouble(orderItem
							.getItemPrice().getAmount()));
					details.setSku(orderItem.getSellerSKU());
					details.setOimOrders(oimOrders);
					m_dbSession.save(details);
					detailSet.add(details);
				}
				oimOrders.setOimOrderDetailses(detailSet);
				m_dbSession.saveOrUpdate(oimOrders);
				numOrdersSaved++;
			}
			tx.commit();
			logStream.println("Imported " + numOrdersSaved + " Orders");
			log.debug("Finished importing orders...");
			return true;
		} catch (Exception e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			log.error(e.getMessage(), e);
			logStream.println("Import Orders failed (" + e.getMessage() + ")");
			return false;
		}

	}

	public boolean init(int channelID, Session dbSession, OimLogStream logStream) {
		m_dbSession = dbSession;
		if (logStream != null)
			this.logStream = logStream;
		else
			this.logStream = new OimLogStream();

		Transaction tx = m_dbSession.beginTransaction();
		Query query = m_dbSession
				.createQuery("from salesmachine.hibernatedb.OimChannels as c where c.channelId=:channelID");
		query.setInteger("channelID", channelID);
		tx.commit();
		if (!query.iterate().hasNext()) {
			System.out.println("No channel found for channel id: " + channelID);
			return false;
		}

		m_channel = (OimChannels) query.iterate().next();
		System.out.println("Channel name : " + m_channel.getChannelName());
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
			log.error("Channel setup is not correct. Please provide this details.");
			this.logStream
					.println("Channel setup is not correct. Please provide this details.");
			return false;
		}
		return true;
	}

	public ArrayList getCurrentOrders() {
		ArrayList orders = new ArrayList();

		Query query = m_dbSession
				.createQuery("select o from salesmachine.hibernatedb.OimOrders o where o.oimOrderBatches.oimChannels=:chan");
		query.setEntity("chan", m_channel);
		Iterator iter = query.iterate();
		while (iter.hasNext()) {
			OimOrders o = (OimOrders) iter.next();
			orders.add(o.getStoreOrderId());
		}
		return orders;
	}

	@Override
	public boolean updateStoreOrder(String storeOrderId, String orderStatus,
			String trackingDetail) {
		// TODO Auto-generated method stub
		return false;
	}
}
