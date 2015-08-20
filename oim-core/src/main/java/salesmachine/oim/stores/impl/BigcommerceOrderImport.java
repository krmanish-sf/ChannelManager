package salesmachine.oim.stores.impl;

import static salesmachine.util.StringHandle.removeNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.util.ApplicationProperties;

public class BigcommerceOrderImport extends ChannelBase {

	private static final Logger log = LoggerFactory.getLogger(BigcommerceOrderImport.class);
	private static String clientID;
	private static String apiUrl;
	private String authToken;
	private String storeID;
	private static SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
	private static HashMap<String, Integer> orderStatusIDMap = new HashMap<String, Integer>(0);

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
	public boolean init(int channelID, Session dbSession) throws ChannelConfigurationException {
		super.init(channelID, dbSession);
		authToken = removeNull(PojoHelper.getChannelAccessDetailValue(m_channel, OimConstants.CHANNEL_ACCESSDETAIL_BIGCOMMERCE_AUTH_TOKEN));
		storeID = removeNull(PojoHelper.getChannelAccessDetailValue(m_channel, OimConstants.CHANNEL_ACCESSDETAIL_BIGCOMMERCE_STORE_ID));

		if (authToken.length() == 0 || storeID.length() == 0) {
			log.error("Channel setup is not correct. Please provide correct details.");
			return false;
		}
		return true;
	}

	private Object getBigcommerceJSON(String requestUrl) throws IOException, InterruptedException, ParseException {
		Object json = null;
		HttpsURLConnection connection = null;
		URL url = new URL(requestUrl);
		connection = (HttpsURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setDoOutput(true);

		connection.setRequestProperty("X-Auth-Client", clientID);
		connection.setRequestProperty("X-Auth-Token", authToken);
		connection.setRequestProperty("Accept", "*/*");
		connection.setRequestProperty("Content-type", "application/json");

		connection.connect();

		int responseCode = connection.getResponseCode();
		if (responseCode == 429) {
			int waitTime = Integer.parseInt(connection.getHeaderField("X-Retry-After"));
			System.out.println("API rate limit exceeded, waiting for " + waitTime + " seconds");
			connection.disconnect();
			Thread.sleep(waitTime * 1000);
			getBigcommerceJSON(requestUrl);
		}
		if (responseCode == 200) {
			String response = getStringFromStream(connection.getInputStream());
			JSONParser parser = new JSONParser();
			json = parser.parse(response);
			connection.disconnect();
		}
		return json;
	}

	@Override
	public void getVendorOrders(OimOrderBatchesTypes batchesTypes, OimOrderBatches batch) throws ChannelCommunicationException, ChannelOrderFormatException,
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
		try {
			String requestURL = apiUrl + storeID + "/v2/orders.json?" + orderStatusIDMap.get(m_orderProcessingRule.getProcessWithStatus()) + "&";

			JSONArray orderJsonArray = (JSONArray) getBigcommerceJSON(requestURL);
			if (orderJsonArray == null)
				return;
			System.out.println("count - " + orderJsonArray.size());
			tx = m_dbSession.beginTransaction();
			for (int i = 0; i < orderJsonArray.size(); i++) {
				JSONObject orderJsonObj = (JSONObject) orderJsonArray.get(i);
				String storeOrderId = orderJsonObj.get("id").toString();
				if (orderAlreadyImported(storeOrderId)) {
					log.info("Order#{} is already imported in the system, updating Order.", storeOrderId);
					continue;
				}
				OimOrders oimOrders = new OimOrders();
				oimOrders.setStoreOrderId(storeOrderId);
				String customer_id = removeNull(orderJsonObj.get("customer_id"));
				JSONObject customer = (JSONObject) getBigcommerceJSON(apiUrl + storeID + "/v2/customers/" + customer_id);
				oimOrders.setCustomerName(removeNull(customer.get("first_name") + " " + removeNull(customer.get("last_name"))));
				oimOrders.setCustomerEmail(removeNull(customer.get("email")));
				oimOrders.setCustomerPhone(removeNull(customer.get("phone")));
				JSONObject billingObj = (JSONObject) orderJsonObj.get("billing_address");
				if (billingObj != null) {
					oimOrders.setBillingName(removeNull(billingObj.get("first_name")) + " " + removeNull(billingObj.get("last_name")));
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
				String shippingAddressesUrl = (String) ((JSONObject) orderJsonObj.get("shipping_addresses")).get("url");
				if (!shippingAddressesUrl.endsWith(".json")) {
					shippingAddressesUrl += ".json";
				}
				JSONArray shippingAddresses = (JSONArray) getBigcommerceJSON(shippingAddressesUrl);
				String shipMethod = null;
				if (shippingAddresses.size() == 1) {
					JSONObject shippingAddObj = (JSONObject) shippingAddresses.get(0);
					oimOrders.setDeliveryName(removeNull(shippingAddObj.get("first_name") + " " + removeNull(shippingAddObj.get("last_name"))));
					oimOrders.setDeliveryCompany(removeNull(shippingAddObj.get("company")));
					oimOrders.setDeliveryStreetAddress(removeNull(shippingAddObj.get("street_1")));
					oimOrders.setDeliverySuburb(removeNull(shippingAddObj.get("street_2")));
					oimOrders.setDeliveryCity(removeNull(shippingAddObj.get("city")));
					oimOrders.setDeliveryZip(removeNull(shippingAddObj.get("zip")));
					oimOrders.setDeliveryCountry(removeNull(shippingAddObj.get("country")));
					oimOrders.setDeliveryState(removeNull(shippingAddObj.get("state")));
					oimOrders.setDeliveryEmail(removeNull(shippingAddObj.get("email")));
					oimOrders.setDeliveryPhone(removeNull(shippingAddObj.get("phone")));
					shipMethod = removeNull(shippingAddObj.get("shipping_method"));
					oimOrders.setShippingDetails(shipMethod);
				}
				oimOrders.setInsertionTm(new Date());
				oimOrders.setOimOrderBatches(batch);
				oimOrders.setOrderFetchTm(new Date());
				String orderCreatedTm = removeNull(orderJsonObj.get("date_created"));
				Date order_tm = null;
				order_tm = sdf.parse(orderCreatedTm);
				oimOrders.setOrderTm(order_tm);
				oimOrders.setOrderTotalAmount(Double.parseDouble(removeNull(orderJsonObj.get("total_inc_tax"))));
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

				m_dbSession.saveOrUpdate(oimOrders);

				String product_resource = removeNull(((JSONObject) orderJsonObj.get("products")).get("resource"));
				String product_resource_url = apiUrl + storeID + "/v2" + product_resource;
				if (!product_resource_url.endsWith(".json")) {
					product_resource_url += product_resource_url;
				}
				Set<OimOrderDetails> detailSet = new HashSet<OimOrderDetails>();
				JSONArray orderItems = (JSONArray) getBigcommerceJSON(product_resource_url);
				for (int x = 0; x < orderItems.size(); x++) {
					JSONObject orderItem = (JSONObject) orderItems.get(x);
					OimOrderDetails details = new OimOrderDetails();
					details.setCostPrice(Double.parseDouble(removeNull(orderItem.get("base_cost_price"))));
					details.setInsertionTm(new Date());
					details.setOimOrderStatuses(new OimOrderStatuses(OimConstants.ORDER_STATUS_UNPROCESSED));
					String sku = removeNull(orderItem.get("sku")).toUpperCase();
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
					details.setProductDesc(removeNull(orderItem.get("name")));
					details.setProductName(removeNull(orderItem.get("name")));
					details.setQuantity(Integer.parseInt(removeNull(orderItem.get("quantity"))));
					details.setSalePrice(Double.parseDouble(removeNull(orderItem.get("base_price"))));
					details.setSku(sku);
					details.setStoreOrderItemId(removeNull(orderItem.get("id")));
					details.setOimOrders(oimOrders);
					m_dbSession.save(details);
					detailSet.add(details);
				}
			}
			log.info("Fetched {} order(s)", orderJsonArray.size());
			tx.commit();
			log.debug("Finished importing orders...");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean updateStoreOrder(OimOrderDetails oimOrderDetails, OrderStatus orderStatus) {
		return false;
	}

	private static String getStringFromStream(InputStream is) throws IOException {
		StringBuffer streamBuffer = new StringBuffer();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		String inputLine;
		while ((inputLine = reader.readLine()) != null) {
			streamBuffer.append(inputLine + '\n');
		}
		reader.close();
		return streamBuffer.toString();
	}

}
