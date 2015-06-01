package salesmachine.oim.stores.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
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
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.util.OimLogStream;
import salesmachine.util.StringHandle;

public class ShopifyOrderImport extends ChannelBase implements IOrderImport {

	private static final Logger log = LoggerFactory
			.getLogger(ShopifyOrderImport.class);
	private String shopifyToken;
	private String storeUrl;

	@Override
	public boolean init(int channelID, Session dbSession, OimLogStream logStream) {
		super.init(channelID, dbSession, logStream);
		storeUrl = StringHandle.removeNull(PojoHelper
				.getChannelAccessDetailValue(m_channel,
						OimConstants.CHANNEL_ACCESSDETAIL_CHANNEL_URL));
		shopifyToken = StringHandle.removeNull(PojoHelper
				.getChannelAccessDetailValue(m_channel,
						OimConstants.CHANNEL_ACCESSDETAIL_SHOPIFY_ACCESS_CODE));

		if (storeUrl.length() == 0 || shopifyToken.length() == 0) {
			log.error("Channel setup is not correct. Please provide correct details.");
			this.logStream
					.println("Channel setup is not correct. Please provide correct details.");
			return false;
		}
		return true;
	}

	@Override
	public boolean updateStoreOrder(OimOrderDetails oimOrderDetails,
			OrderStatus orderStatus) {
		// this method is implemented for tracking purpose
		//log.info("order status is - {}",orderStatus.g);
		log.info("order id is - {}",oimOrderDetails.getOimOrders().getOrderId());
		log.info("tracking number - {}",orderStatus.getTrackingData().getShipperTrackingNumber());
		log.info("tracking_company - {}",orderStatus.getTrackingData().getCarrierName());
		//		if (!orderStatus.isShipped()) {
		//			return true;
		//		}
		String requestUrl = storeUrl + "/admin/orders/"+oimOrderDetails.getOimOrders().getStoreOrderId()+"/fulfillments.json";
		HttpClient client = new HttpClient();
		//		GetMethod getOrderJson = new GetMethod(requestUrl);
		//		getOrderJson.addRequestHeader("X-Shopify-Access-Token", shopifyToken);
		//		int responseCode = 0;
		//		String jsonString=null;
		//		try {
		//			responseCode = client.executeMethod(getOrderJson);
		//			if(responseCode==200){
		//				jsonString = getOrderJson.getResponseBodyAsString();
		//				if(jsonString!=null)
		//					log.info("fullfillment json  - {}",jsonString);
		//			}
		//		} catch (HttpException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		PostMethod postMethod = new PostMethod(requestUrl);
		postMethod.addRequestHeader("X-Shopify-Access-Token", shopifyToken);
		JSONObject jsonObject = new JSONObject();
		JSONObject jsonObjVal = new JSONObject();
		jsonObjVal.put("tracking_number", orderStatus.getTrackingData().getShipperTrackingNumber());
		jsonObjVal.put("tracking_company",orderStatus.getTrackingData().getCarrierName());
		jsonObjVal.put("notify_customer", true);
		jsonObject.put("fulfillment", jsonObjVal);

		StringRequestEntity requestEntity=null;
		try {
			requestEntity = new StringRequestEntity(jsonObject.toJSONString(),"application/json","UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		postMethod.setRequestEntity(requestEntity);
		int statusCode=0;
		try {
			statusCode = client.executeMethod(postMethod);
			log.info("statusCode is - {}",statusCode);
			
				String responseString = postMethod.getResponseBodyAsString();
				log.info("response string - {}",responseString);
			
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public OimOrderBatches getVendorOrders(OimOrderBatchesTypes batchesTypes) {
		// on the basis of access token, we will pull orders from vendors
		OimOrderBatches batch = new OimOrderBatches();
		Transaction tx = null;
		HttpClient client = new HttpClient();
		String requestUrl = storeUrl + "/admin/orders.json";
		String jsonString = null;
		GetMethod getOrderJson = new GetMethod(requestUrl);
		getOrderJson.addRequestHeader("X-Shopify-Access-Token", shopifyToken);

		batch.setOimChannels(m_channel);
		batch.setOimOrderBatchesTypes(batchesTypes);

		tx = m_dbSession.beginTransaction();
		batch.setInsertionTm(new Date());
		batch.setCreationTm(new Date());
		m_dbSession.save(batch);
		tx.commit();

		tx = m_dbSession.beginTransaction();

		int responseCode = 0;
		try {
			responseCode = client.executeMethod(getOrderJson);
		} catch (IOException e) {
			log.error("Unable to get response from shopify. Please check the store url and access token");
			log.error(e.getMessage(), e);
			return null;
		}

		if (responseCode == 200) {
			List currentOrders = getCurrentOrders();
			try {
				jsonString = getOrderJson.getResponseBodyAsString();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
				return null;
			}
			JSONObject jsonObject = null;
			JSONParser parser = new JSONParser();
			try {
				jsonObject = (JSONObject) parser.parse(jsonString);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if (jsonObject == null)
				return null;
			JSONArray orderArr = (JSONArray) jsonObject.get("orders");
			int numOrdersSaved = 0;
			for (int i = 0; i < orderArr.size(); i++) {
				JSONObject orderObj = (JSONObject) orderArr.get(i);
				int storeOrderId = (int) (long) orderObj.get("id");
				if (currentOrders.contains(storeOrderId + ""))
					continue;
				OimOrders oimOrders = new OimOrders();
				oimOrders.setStoreOrderId(storeOrderId + "");
				// setting billing information
				JSONObject billingObj = (JSONObject) orderObj
						.get("billing_address");
				if (billingObj != null) {
					oimOrders.setBillingStreetAddress(StringHandle
							.removeNull((String) billingObj.get("address1")));
					oimOrders.setBillingSuburb(StringHandle
							.removeNull((String) billingObj.get("address2")));

					oimOrders.setBillingZip(StringHandle
							.removeNull((String) billingObj.get("zip")));
					oimOrders.setBillingCity(StringHandle
							.removeNull((String) billingObj.get("city")));
					oimOrders.setBillingCompany(StringHandle
							.removeNull((String) billingObj.get("company")));
					oimOrders.setBillingCountry(StringHandle
							.removeNull((String) billingObj.get("country")));
					oimOrders.setBillingName(StringHandle
							.removeNull((String) billingObj.get("first_name"))
							+ " "
							+ StringHandle.removeNull((String) billingObj
									.get("last_name")));
					oimOrders.setBillingPhone(StringHandle
							.removeNull((String) billingObj.get("phone")));
				}
				// setting delivery information
				JSONObject deliveryObj = (JSONObject) orderObj
						.get("shipping_address");
				if (deliveryObj != null) {
					oimOrders.setDeliveryCity(StringHandle
							.removeNull((String) deliveryObj.get("city")));
					oimOrders.setDeliveryCompany(StringHandle
							.removeNull((String) deliveryObj.get("company")));
					oimOrders.setDeliveryCountry(StringHandle
							.removeNull((String) deliveryObj.get("country")));
					oimOrders.setDeliveryName(StringHandle
							.removeNull((String) deliveryObj.get("first_name"))
							+ " "
							+ StringHandle.removeNull((String) deliveryObj
									.get("last_name")));
					oimOrders.setDeliveryPhone(StringHandle
							.removeNull((String) deliveryObj.get("phone")));
					oimOrders.setDeliveryStreetAddress(StringHandle
							.removeNull((String) deliveryObj.get("address1")));
					oimOrders.setDeliverySuburb(StringHandle
							.removeNull((String) deliveryObj.get("address2")));
					oimOrders.setDeliveryZip(StringHandle
							.removeNull((String) deliveryObj.get("zip")));
				}
				// setting customer information
				JSONObject custInfo = (JSONObject) orderObj.get("customer");
				oimOrders.setCustomerEmail(StringHandle
						.removeNull((String) custInfo.get("email")));
				if (custInfo != null) {
					JSONObject customerObj = (JSONObject) custInfo
							.get("default_address");
					if (customerObj != null) {
						oimOrders.setCustomerCity(StringHandle
								.removeNull((String) customerObj.get("city")));
						oimOrders
								.setCustomerCompany(StringHandle
										.removeNull((String) customerObj
												.get("company")));
						oimOrders
								.setCustomerCountry(StringHandle
										.removeNull((String) customerObj
												.get("country")));
						oimOrders.setCustomerName(StringHandle
								.removeNull((String) customerObj.get("name")));
						oimOrders.setCustomerPhone(StringHandle
								.removeNull((String) customerObj.get("phone")));
						oimOrders.setCustomerStreetAddress(StringHandle
								.removeNull((String) customerObj
										.get("address1")));
						oimOrders.setCustomerSuburb(StringHandle
								.removeNull((String) customerObj
										.get("address2")));
						oimOrders.setCustomerZip(StringHandle
								.removeNull((String) customerObj.get("zip")));
					}
				}

				oimOrders.setInsertionTm(new Date());
				oimOrders.setOimOrderBatches(batch);
				// oimOrders.setOrderComment(order2.);
				oimOrders.setOrderFetchTm(new Date());
				SimpleDateFormat df = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ssXXX"); // "2015-05-27T04:38:58-04:00
				Date orderTm = null;
				try {
					String orderTmString = ((String) orderObj.get("created_at"));
					orderTm = df.parse(orderTmString);
				} catch (java.text.ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				oimOrders.setOrderTm(orderTm);
				oimOrders.setOrderTotalAmount(Double
						.parseDouble((String) orderObj.get("total_price")));
				oimOrders.setPayMethod((String) orderObj.get("gateway"));
				String shippingDetails = (String) ((JSONObject) ((JSONArray) orderObj
						.get("shipping_lines")).get(0)).get("title");
				oimOrders.setShippingDetails(shippingDetails);

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

				m_dbSession.saveOrUpdate(oimOrders);
				// setting product information
				JSONArray itemArray = (JSONArray) orderObj.get("line_items");
				Set<OimOrderDetails> detailSet = new HashSet<OimOrderDetails>();
				for (int j = 0; j < itemArray.size(); j++) {
					OimOrderDetails details = new OimOrderDetails();
					JSONObject item = (JSONObject) itemArray.get(j);

					details.setCostPrice(Double.parseDouble(StringHandle
							.removeNull((String) item.get("price"))));
					details.setInsertionTm(new Date());
					details.setOimOrderStatuses(new OimOrderStatuses(
							OimConstants.ORDER_STATUS_UNPROCESSED));
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
					details.setSalePrice(Double.parseDouble(StringHandle
							.removeNull((String) item.get("price")))); // need
					// to
					// confirm
					details.setSku(sku);
					details.setStoreOrderItemId(((long) item.get("id")) + "");
					details.setOimOrders(oimOrders);

					m_dbSession.save(details);
					detailSet.add(details);
				}
				oimOrders.setOimOrderDetailses(detailSet);
				m_dbSession.saveOrUpdate(oimOrders);
				numOrdersSaved++;
			}
			log.info("Fetched {} order(s)", orderArr.size());
			tx.commit();
			logStream.println("Imported " + numOrdersSaved + " Orders");
			log.debug("Finished importing orders...");
		} else {
			log.error(
					"Got response code {} .Please check the request parameters",
					responseCode);
		}

		return batch;
	}

	protected List<String> getCurrentOrders() {
		List<String> orders = new ArrayList<String>();

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

}
