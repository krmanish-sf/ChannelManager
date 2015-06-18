package salesmachine.oim.stores.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import salesmachine.hibernatedb.OimChannelShippingMap;
import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderBatchesTypes;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderProcessingRule;
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

import com.stevesoft.pat.Regex;

public class CREOrderImport extends ChannelBase implements IOrderImport {
	private static final Logger LOG = LoggerFactory
			.getLogger(CREOrderImport.class);
	private String m_storeURL;

	public boolean init(int channelID, Session dbSession, OimLogStream log) {
		super.init(channelID, dbSession, log);
		String scriptPath = PojoHelper.getChannelAccessDetailValue(m_channel,
				OimConstants.CHANNEL_ACCESSDETAIL_SCRIPT_PATH);
		LOG.info("Checking the script path");
		if (StringHandle.isNullOrEmpty(scriptPath)) {
			LOG.warn("Channel is not yet setup for automation.");
			log.println("Channel is not yet setup for automation. Script not found.");
			return false;
		}

		Regex scriptMatch = Regex.perlCode("/https?:\\/\\/(.+?)\\/(.+)/i");
		if (!scriptMatch.search(scriptPath)) {
			LOG.error("FAILED TO PARSE SCRIPT LOCATION");
			log.println("Failed to parse script location.");
			return false;
		}
		m_storeURL = scriptPath;
		return true;
	}

	@Override
	public OimOrderBatches getVendorOrders(OimOrderBatchesTypes batchesTypes) {
		OimOrderBatches batch = null;

		try {
			if (!pingTest()) {
				logStream.println("Channel ping failed.");
				return batch;
			}

			long start = System.currentTimeMillis();
			String response = sendGetOrdersRequest();
			LOG.debug(response);
			if (!"".equals(response)) {
				StringReader str = new StringReader(response);
				batch = parseGetProdResponse(str, batchesTypes);
			} else {
				LOG.error("FAILURE_GETPRODUCT_NULL_RESPONSE");
				logStream
						.println("Channel returned null in response to fetch Orders.");
				return batch;
			}

			long time = (System.currentTimeMillis() - start);
			LOG.info("Finished GetProduct step in {} seconds", time / 1000);

			if (batch.getOimOrderses().size() == 0) {
				logStream
						.println("Order Import Process Complete. No new orders found on the store.");
				return batch;
			}

			// Update status
			if (!StringHandle.isNullOrEmpty(m_orderProcessingRule
					.getConfirmedStatus())) {
				response = sendOrderStatusRequest(batch,
						m_orderProcessingRule.getConfirmedStatus());
			}

			if (!StringHandle.isNullOrEmpty(response)) {
				StringReader str = new StringReader(response);
				List updatedOrders = parseUpdateResponse(str);
				Set orders = batch.getOimOrderses();
				Set confirmedOrders = new HashSet();
				if (!StringHandle.isNullOrEmpty(m_orderProcessingRule
						.getConfirmedStatus())) {
					for (Iterator it = orders.iterator(); it.hasNext();) {
						OimOrders order = (OimOrders) it.next();
						if (updatedOrders.contains(order.getStoreOrderId())) {
							confirmedOrders.add(order);
						}
					}
					batch.setOimOrderses(confirmedOrders);
				}

				// Save everything
				Transaction tx = m_dbSession.beginTransaction();
				batch.setInsertionTm(new Date());
				batch.setCreationTm(new Date());
				m_dbSession.save(batch);

				LOG.debug("Saved batch id: {}", batch.getBatchId());

				// Get all the orders for the current channel
				List<String> currentOrders = getCurrentOrders();
				boolean ordersSaved = false;
				int importCount = 0;
				for (Iterator oit = batch.getOimOrderses().iterator(); oit
						.hasNext();) {
					OimOrders order = (OimOrders) oit.next();

					if (currentOrders.contains(order.getStoreOrderId())) {
						LOG.warn(
								"Order skipping as already exists. Store order id : {}",
								order.getStoreOrderId());
						continue;
					}

					order.setOimOrderBatches(batch);
					order.setOrderFetchTm(new Date());
					order.setInsertionTm(new Date());
					String shippingDetails = order.getShippingDetails();
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
						Pattern p = Pattern.compile(shippingRegEx,
								Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
						Matcher m = p.matcher(shippingDetails);
						if (m.find() && m.groupCount() >= 2) {
							order.setOimShippingMethod(entity
									.getOimShippingMethod());
							LOG.info("Shipping set to "
									+ entity.getOimShippingMethod());
							break;
						}
					}
					if (order.getOimShippingMethod() == null)
						LOG.warn("Shipping can't be mapped for order "
								+ order.getStoreOrderId());
					m_dbSession.save(order);
					LOG.info("Saved order id: " + order.getOrderId());

					for (Iterator dit = order.getOimOrderDetailses().iterator(); dit
							.hasNext();) {
						OimOrderDetails detail = (OimOrderDetails) dit.next();
						detail.setOimOrders(order);
						detail.setInsertionTm(new Date());
						detail.setOimOrderStatuses(new OimOrderStatuses(
								OimConstants.ORDER_STATUS_UNPROCESSED));

						m_dbSession.save(detail);
						LOG.info("Saved detail id: " + detail.getDetailId());
						ordersSaved = true;
					}
					importCount++;
				}
				if (ordersSaved) {
					tx.commit();
					logStream
							.println("Order Import Process Complete. Number of Orders imported from the store: "
									+ importCount);
				} else {
					logStream.println("No new order found on store.");
				}
			} else {
				LOG.error("FAILURE_GETPRODUCT_NULL_RESPONSE");
			}
		} catch (Exception e) {
			LOG.info(e.getMessage(), e);
		}
		return batch;
	}

	public boolean pingTest() {
		String ping_xml = "<xmlPopulate>"
				+ "<header>"
				+ "<requestType>Ping</requestType>"
				+ "<passkey>"
				+ PojoHelper.getChannelAccessDetailValue(m_channel,
						OimConstants.CHANNEL_ACCESSDETAIL_AUTH_KEY)
				+ "</passkey>" + "</header>" + "</xmlPopulate>";
		String ping_response = sendRequest(ping_xml);
		ping_response = ping_response.trim();

		StringReader str = new StringReader(ping_response);
		if (!"".equals(ping_response)) {
			if (!parsePingResponse(str, "xmlPopulateResponse")) {
				// Try once more
				ping_response = sendRequest(ping_xml);
				str = new StringReader(ping_response);
				if (!"".equals(ping_response)) {
					return parsePingResponse(str, "xmlPopulateResponse");
				}
			} else
				return true;
		}
		return false;
	}

	private String sendGetOrdersRequest() {
		String getprod_xml = "<xmlPopulate>"
				+ "<header>"
				+ "<requestType>GetOrders</requestType><orderStatus>"
				+ ((OimOrderProcessingRule) m_channel
						.getOimOrderProcessingRules().iterator().next())
						.getPullWithStatus()
				+ "</orderStatus>"
				+ "<passkey>"
				+ PojoHelper.getChannelAccessDetailValue(m_channel,
						OimConstants.CHANNEL_ACCESSDETAIL_AUTH_KEY)
				+ "</passkey>" + "</header></xmlPopulate>";
		String getprod_response = sendRequest(getprod_xml);
		return getprod_response.trim();
	}

	private OimOrderBatches parseGetProdResponse(StringReader xml_toparse,
			OimOrderBatchesTypes batchesTypes) {
		try {
			DecimalFormat df = new DecimalFormat("#.##");
			OimOrderBatches batch = new OimOrderBatches();
			batch.setOimChannels(m_channel);
			batch.setOimOrderBatchesTypes(batchesTypes);

			ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
			DOMParser parser = new DOMParser();
			parser.setErrorStream(baos);
			parser.parse(xml_toparse);
			XMLDocument doc = parser.getDocument();
			doc.getDocumentElement().normalize();
			NodeList N_list = doc.getElementsByTagName("Order");
			for (int s = 0; s < N_list.getLength(); s++) {
				Node node = N_list.item(s);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					OimOrders order = new OimOrders();
					NodeList details = element
							.getElementsByTagName("deliverydetails");
					if (details != null && details.getLength() > 0) {
						Element e = (Element) details.item(0);
						order.setDeliveryName(getTagValue("name", e));
						order.setDeliveryStreetAddress(getTagValue(
								"streetaddress", e));
						order.setDeliverySuburb(getTagValue("suburb", e));
						order.setDeliveryCity(getTagValue("city", e));
						order.setDeliveryState(getTagValue("state", e));
						order.setDeliveryCountry(getTagValue("country", e));
						order.setDeliveryZip(getTagValue("zip", e));
						order.setDeliveryCompany(getTagValue("company", e));
						order.setDeliveryPhone(getTagValue("phone", e));
						order.setDeliveryEmail(getTagValue("email", e));
					}

					details = element.getElementsByTagName("billingdetails");
					if (details != null && details.getLength() > 0) {
						Element e = (Element) details.item(0);
						order.setBillingName(getTagValue("name", e));
						order.setBillingStreetAddress(getTagValue(
								"streetaddress", e));
						order.setBillingSuburb(getTagValue("suburb", e));
						order.setBillingCity(getTagValue("city", e));
						order.setBillingState(getTagValue("state", e));
						order.setBillingCountry(getTagValue("country", e));
						order.setBillingZip(getTagValue("zip", e));
						order.setBillingCompany(getTagValue("company", e));
						order.setBillingPhone(getTagValue("phone", e));
						order.setBillingEmail(getTagValue("email", e));
					}

					details = element.getElementsByTagName("customerdetails");
					if (details != null && details.getLength() > 0) {
						Element e = (Element) details.item(0);
						order.setCustomerName(getTagValue("name", e));
						order.setCustomerStreetAddress(getTagValue(
								"streetaddress", e));
						order.setCustomerSuburb(getTagValue("suburb", e));
						order.setCustomerCity(getTagValue("city", e));
						order.setCustomerState(getTagValue("state", e));
						order.setCustomerCountry(getTagValue("country", e));
						order.setCustomerZip(getTagValue("zip", e));
						order.setCustomerCompany(getTagValue("company", e));
						order.setCustomerPhone(getTagValue("phone", e));
						order.setCustomerEmail(getTagValue("email", e));
					}

					order.setStoreOrderId(getTagValue("o_id", element));
					order.setPayMethod(getTagValue("o_pay_method", element));
					double billAmt = Double.parseDouble(getTagValue(
							"p_bill_amount", element));
					String billAmtFormatted = df.format(billAmt);
					order.setOrderTotalAmount(new Double(billAmtFormatted));

					order.setShippingDetails(getTagValue("o_shipping", element));

					SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
					Date d1 = new Date();
					try {
						d1 = sdf.parse(getTagValue("o_time", element));
					} catch (ParseException e) {
						LOG.error(e.getMessage(), e);
					}
					order.setOrderTm(d1);

					details = ((Element) (element
							.getElementsByTagName("products").item(0)))
							.getElementsByTagName("product");
					if (details != null && details.getLength() > 0) {
						for (int i = 0; i < details.getLength(); i++) {
							Element e = (Element) details.item(i);
							String sku = getTagValue("p_model", e);
							String qty = getTagValue("p_quantity", e);
							String priceEach = getTagValue("p_price_each", e);
							String productName = getTagValue("p_name", e);
							String productCost = "0";
							try {
								productCost = getTagValue("p_price_cost", e);
							} catch (Exception ex) {
								productCost = "0";
							}
							if (sku.trim().length() == 0) {
								continue;
							}
							LOG.info("SKU: {} Qty: {} Price each: {}", sku,
									qty, priceEach);

							/*
							 * String prefix = ""; if (sku.length() > 2) {
							 * prefix = sku.substring(0, 2); }
							 */
							OimSuppliers supplier = null;
							for (String prefix : supplierMap.keySet()) {
								if (sku.startsWith(prefix)) {
									supplier = supplierMap.get(prefix);
									break;
								}
							}
							/*
							 * if (supplierMap.containsKey(prefix)) { supplier =
							 * (OimSuppliers) supplierMap .get(prefix); }
							 */
							double cost = 0;
							try {
								cost = Double.parseDouble(productCost);
							} catch (Exception ex) {
								ex.printStackTrace();
							}

							double saleprice = 0;
							int quantity = 1;
							try {
								saleprice = Double.parseDouble(priceEach);
								quantity = Integer.parseInt(qty);
							} catch (Exception ex) {
								LOG.error(ex.getMessage(), e);
							}

							OimOrderDetails detail = new OimOrderDetails();
							detail.setSalePrice(new Double(df.format(saleprice)));
							detail.setQuantity(new Integer(quantity));
							detail.setSku(sku);
							detail.setStoreOrderItemId(sku);
							detail.setOimSuppliers(supplier);
							if (cost > 0)
								detail.setCostPrice(cost);
							detail.setProductName(productName);
							order.getOimOrderDetailses().add(detail);
						}
					}

					String o_note = getTagValue("o_note", element);
					order.setOrderComment(o_note);

					LOG.info("Adding order in the batch with order id : "
							+ order.getStoreOrderId());
					batch.getOimOrderses().add(order);
				}
			}
			return batch;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	private String sendRequest(String pingXML) {
		LOG.info("Sending request to {}", m_storeURL);
		pingXML = "XML_INPUT_VALUE=" + pingXML;
		URL url;
		HttpsURLConnection connection = null;
		String response = "";
		try {
			// Create connection
			url = new URL(m_storeURL);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");

			byte[] req = pingXML.getBytes();
			LOG.info("Request: {}", pingXML);
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			OutputStream outputStream = connection.getOutputStream();
			outputStream.write(req);
			outputStream.close();
			connection.connect();
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line + '\n');
			}
			response = sb.toString();
			LOG.info("Response: {}", response);
		} catch (Exception e) {
			LOG.error("Failed to send request ...", e);

		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return response;

	}

	private boolean parsePingResponse(StringReader xml_toparse, String tag_name) {
		boolean ping_success = false;
		String heartbeat = "";
		DOMParser parser = new DOMParser();
		XMLDocument doc;
		Node node;
		Element element;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
			parser.setErrorStream(baos);
			parser.parse(xml_toparse);
			doc = parser.getDocument();
			doc.getDocumentElement().normalize();
			NodeList N_list = doc.getElementsByTagName(tag_name);
			for (int s = 0; s < N_list.getLength(); s++) {
				node = N_list.item(s);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					element = (Element) node;
					heartbeat = getTagValue("heartbeat", element);
					LOG.info("HEARTBEAT :" + heartbeat);
				}
			}
			if ("alive".equals(heartbeat.toLowerCase())) {
				ping_success = true;
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return ping_success;
	}

	private static String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
				.getChildNodes();
		Node nValue = (Node) nlList.item(0);
		if (nValue != null) {
			return nValue.getNodeValue();
		} else {
			return "";
		}
	}

	public String sendOrderStatusRequest(OimOrderBatches batch, String status) {
		StringBuffer xmlrequest = new StringBuffer("<xmlPopulate>"
				+ "<header>"
				+ "<requestType>updateorders</requestType><orderStatus>"
				+ status
				+ "</orderStatus>"
				+ "<passkey>"
				+ PojoHelper.getChannelAccessDetailValue(m_channel,
						OimConstants.CHANNEL_ACCESSDETAIL_AUTH_KEY)
				+ "</passkey>\n" + "</header>\n");

		Set orders = batch.getOimOrderses();
		for (Iterator it = orders.iterator(); it.hasNext();) {
			OimOrders order = (OimOrders) it.next();
			xmlrequest.append("<xml_order>\n");
			xmlrequest.append("<order_id>" + order.getStoreOrderId()
					+ "</order_id>");
			xmlrequest.append("<order_status>" + status + "</order_status>");
			xmlrequest
					.append("<order_tracking>Imported to InventorySource Channel Manager</order_tracking>");
			xmlrequest.append("</xml_order>\n");
		}
		xmlrequest.append("</xmlPopulate>");

		String getprod_response = sendRequest(xmlrequest.toString());
		return getprod_response.trim();
	}

	private List parseUpdateResponse(StringReader xmlToParse) {
		List updatedOrders = new ArrayList();
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
			DOMParser parser = new DOMParser();
			parser.setErrorStream(baos);
			parser.parse(xmlToParse);
			XMLDocument doc = parser.getDocument();
			doc.getDocumentElement().normalize();
			NodeList N_list = doc.getElementsByTagName("UpdatedOrder");
			for (int s = 0; s < N_list.getLength(); s++) {
				Node node = N_list.item(s);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					NodeList nlList = element.getChildNodes();
					Node nValue = (Node) nlList.item(0);
					if (nValue != null) {
						LOG.debug("Updated order: {}", nValue.getNodeValue());
						updatedOrders.add(nValue.getNodeValue());
					}
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return updatedOrders;
	}

	public void updateProcessedOrders() {
		Transaction tx = m_dbSession.beginTransaction();
		Query query = m_dbSession
				.createQuery("select distinct o.storeOrderId from salesmachine.hibernatedb.OimOrders as o "
						+ "where not exists (from salesmachine.hibernatedb.OimOrderDetails as d where d.oimOrders=o and d.processingTm is null)"
						+ "and exists (from salesmachine.hibernatedb.OimOrderDetails as d1 where d1.oimOrders=o and d1.processingTm>trunc(sysdate-1))");
		Iterator it = query.iterate();
		if (!it.hasNext()) {
			LOG.warn("No orders to update");
			return;
		}

		StringBuffer xmlrequest = new StringBuffer("<xmlPopulate>\n"
				+ "<header>\n"
				+ "<requestType>updateorders</requestType>\n"
				+ "<passkey>"
				+ PojoHelper.getChannelAccessDetailValue(m_channel,
						OimConstants.CHANNEL_ACCESSDETAIL_AUTH_KEY)
				+ "</passkey>\n" + "</header>\n");
		while (it.hasNext()) {
			String storeOrderId = (String) it.next();
			LOG.info("Updating order: " + storeOrderId);
			xmlrequest.append("<xml_order>\n");
			xmlrequest.append("<order_id>" + storeOrderId + "</order_id>");
			xmlrequest.append("<order_status>" + 3 + "</order_status>");
			xmlrequest.append("</xml_order>\n");
		}

		xmlrequest.append("</xmlPopulate>");
		LOG.info(xmlrequest.toString());
		String getprod_response = sendRequest(xmlrequest.toString());
	}

	@Override
	public boolean updateStoreOrder(OimOrderDetails oimOrderDetails,
			OrderStatus orderStatus) {
		if (!orderStatus.isShipped()) {
			return true;
		}
		StringBuffer xmlrequest = new StringBuffer("<xmlPopulate>"
				+ "<header>"
				+ "<requestType>updateorders</requestType><orderStatus>"
				+ orderStatus.getStatus()
				+ "</orderStatus>"
				+ "<passkey>"
				+ PojoHelper.getChannelAccessDetailValue(m_channel,
						OimConstants.CHANNEL_ACCESSDETAIL_AUTH_KEY)
				+ "</passkey></header>");

		xmlrequest.append("<xml_order>\n");
		xmlrequest.append("<order_id>"
				+ oimOrderDetails.getOimOrders().getStoreOrderId()
				+ "</order_id>");
		xmlrequest.append("<order_status>" + orderStatus.getStatus()
				+ "</order_status>");
		xmlrequest.append("<order_tracking>"
				+ (orderStatus.isShipped() ? orderStatus.getTrackingData()
						.toString() : "Order not shipped.")
				+ "</order_tracking>");
		xmlrequest.append("</xml_order>\n");

		xmlrequest.append("</xmlPopulate>");

		String getprod_response = sendRequest(xmlrequest.toString());
		LOG.debug("Update Store order complete.");
		LOG.debug(getprod_response.trim());

		return false;
	}

}
