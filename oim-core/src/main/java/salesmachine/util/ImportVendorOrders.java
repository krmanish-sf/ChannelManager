package salesmachine.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import salesmachine.hibernatedb.OimChannelSupplierMap;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderBatchesTypes;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderProcessingRule;
import salesmachine.hibernatedb.OimOrderStatuses;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSuppliers;
import salesmachine.hibernatedb.Reps;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.PojoHelper;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.IOrderImport;
import HTTPClient.CookieModule;

import com.stevesoft.pat.Regex;

public class ImportVendorOrders {
	private static final Logger log = LoggerFactory
			.getLogger(ImportVendorOrders.class);
	private static OimOrderProcessingRule m_orderProcessingRule;

	public static void main(String arg[]) {
		Session dbSession = SessionManager.currentSession();
		Vector activeVendors = getActiveOIMVendor(dbSession);

		for (int i = 0; i < activeVendors.size(); i++) {
			Integer vid = (Integer) activeVendors.get(i);
			getChannels(dbSession, vid);
		}
	}

	public static Vector getActiveOIMVendor(Session dbSession) {
		Transaction tx = null;
		Reps r = null;
		Vector activeVendors = new Vector();

		try {
			tx = dbSession.beginTransaction();

			// Here is your db code
			Query query = dbSession
					.createQuery("from salesmachine.hibernatedb.Reps r where and r.cmAllowed=:cmAllowed");
			query.setString("cmAllowed", "1");
			Iterator it = query.iterate();
			while (it.hasNext()) {
				r = (salesmachine.hibernatedb.Reps) it.next();
				activeVendors.add(r.getVendorId());
				log.debug("Active Channel Manager VenderID : "
						+ r.getVendorId());
			}
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			e.printStackTrace();
		}
		return activeVendors;
	}

	private static void getChannels(Session session, Integer vid) {
		Vendors vendors = new Vendors(vid);
		// Get the channels for the logged in vendor
		String hql = "select ch from salesmachine.hibernatedb.OimChannels ch where ch.vendors=:v and ch.deleteTm is null";
		Query query = session.createQuery(hql);
		query.setEntity("v", vendors);
		Iterator iter = query.iterate();
		// HashMap channelMap = new HashMap();
		while (iter.hasNext()) {
			OimChannels channel = (OimChannels) iter.next();
			log.debug(vid + " :  " + channel.getChannelId());
			startOrderPullForChannel(session, channel);
			channel.getOimChannelSupplierMaps();
		}

	}

	private static void startOrderPullForChannel(Session session,
			OimChannels channel) {

		log.debug("Channel id : " + channel.getChannelId());
		int channelId = channel.getChannelId();
		Transaction tx = session.beginTransaction();
		Query query = session
				.createQuery("from salesmachine.hibernatedb.OimChannels as c where c.channelId=:channelID");
		query.setInteger("channelID", channelId);
		tx.commit();
		log.debug("Got the channel ");
		if (!query.iterate().hasNext()) {
			log.debug("No channel found for channel id: " + channelId);
		}

		// OimChannels channel = (OimChannels)query.iterate().next();
		String storeUrl = PojoHelper.getChannelAccessDetailValue(channel,
				OimConstants.CHANNEL_ACCESSDETAIL_CHANNEL_URL);

		log.debug("!! Supported channel : "
				+ channel.getOimSupportedChannels().getChannelName());
		String orderFetchBean = channel.getOimSupportedChannels()
				.getOrderFetchBean();
		IOrderImport coi = null;
		if (orderFetchBean != null && orderFetchBean.length() > 0) {
			try {
				Class theClass = Class.forName(orderFetchBean);
				coi = (IOrderImport) theClass.newInstance();
			} catch (Exception cnfe) {
				cnfe.printStackTrace();
				coi = null;
			}
		}

		if (coi != null) {
			log.debug("Created the iorderimport object");
			if (!coi.init(channelId, session, null)) {
				// log.debug("Failed initializing the channel.");
				System.out
						.println("Failed initializing the channel with channelId:"
								+ channelId);
			} else {
				log.debug("Pulling orders for channel id: " + channelId);
				coi.getVendorOrders();
				getVendorOrders(channel, session);
			}
		} else {
			log.error("ERROR - Could not find a bean to work with this market. ");
		}

		SessionManager.closeSession();
	}

	public static void getVendorOrders(OimChannels channel, Session session) {
		boolean success = true;
		try {
			Set suppliers = channel.getOimChannelSupplierMaps();
			Map supplierMap = new HashMap();
			Iterator itr = suppliers.iterator();
			while (itr.hasNext()) {
				OimChannelSupplierMap map = (OimChannelSupplierMap) itr.next();
				if (map.getDeleteTm() != null)
					continue;

				String prefix = map.getSupplierPrefix();
				OimSuppliers supplier = map.getOimSuppliers();
				log.debug("prefix :: " + prefix + "supplierID :: "
						+ supplier.getSupplierId());
				supplierMap.put(prefix, supplier);
			}

			OimOrderBatches batch = null;
			long start = System.currentTimeMillis();
			String response = sendGetOrdersRequest(channel, session);

			if (!"".equals(response)) {
				StringReader str = new StringReader(response);
				batch = parseGetProdResponse(str, supplierMap, channel);
			} else {
				System.out
						.println("FAILURE_GETPRODUCT_NULL_RESPONSE FAILURE_GETPRODUCT_NULL_RESPONSE");
				// return false;
			}

			long time = (System.currentTimeMillis() - start);
			log.debug("Finished GetProduct step in " + (time / 1000)
					+ " seconds ("
					+ NumberFormat.roundDouble((time / 1000 / 60))
					+ " minutes)" + "\n");

			if (batch.getOimOrderses().size() == 0) {
				log.info("nOrder Import Process Complete. No new orders found on the store.");
				// return true;
			}

			if (m_orderProcessingRule.getUpdateStoreOrderStatus().intValue() > 0) {
				// Update status
				if (m_orderProcessingRule.getUpdateWithStatus() != null
						&& m_orderProcessingRule.getUpdateWithStatus().trim()
								.length() > 0) {
					response = sendOrderStatusRequest(batch,
							m_orderProcessingRule.getUpdateWithStatus(),
							channel, session);
				}
			} else {
				// Don't update status
			}

			if (!"".equals(response)) {
				StringReader str = new StringReader(response);
				List updatedOrders = parseUpdateResponse(str);

				Set orders = batch.getOimOrderses();

				Set confirmedOrders = new HashSet();
				if (m_orderProcessingRule.getUpdateStoreOrderStatus()
						.intValue() > 0) {
					for (Iterator it = orders.iterator(); it.hasNext();) {
						OimOrders order = (OimOrders) it.next();
						if (updatedOrders.contains(order.getStoreOrderId())) {
							confirmedOrders.add(order);
						}
					}
					batch.setOimOrderses(confirmedOrders);
				}

				// Save everything
				Transaction tx = session.beginTransaction();
				batch.setInsertionTm(new Date());
				batch.setCreationTm(new Date());
				session.save(batch);

				log.debug("Saved batch id: " + batch.getBatchId());

				// Get all the orders for the current channel
				ArrayList currentOrders = getCurrentOrders(channel, session);
				boolean ordersSaved = false;
				for (Iterator oit = batch.getOimOrderses().iterator(); oit
						.hasNext();) {
					OimOrders order = (OimOrders) oit.next();

					if (currentOrders.contains(order.getStoreOrderId())) {
						System.out
								.println("!!! Order skiiping as already exists. Store order id : "
										+ order.getStoreOrderId());
						continue;
					}

					order.setOimOrderBatches(batch);
					order.setOrderFetchTm(new Date());
					order.setInsertionTm(new Date());
					String shippingDetails = order.getShippingDetails();
					Integer supportedChannelId = channel
							.getOimSupportedChannels().getSupportedChannelId();
					Criteria findCriteria = session
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
							log.info("Shipping set to "
									+ entity.getOimShippingMethod());
							break;
						}
					}
					log.warn("Shipping can't be mapped for order "
							+ order.getOrderId());
					session.save(order);
					log.debug("Saved order id: " + order.getOrderId());
					for (Iterator dit = order.getOimOrderDetailses().iterator(); dit
							.hasNext();) {
						OimOrderDetails detail = (OimOrderDetails) dit.next();
						detail.setOimOrders(order);
						detail.setInsertionTm(new Date());
						detail.setOimOrderStatuses(new OimOrderStatuses(
								OimConstants.ORDER_STATUS_UNPROCESSED));
						session.save(detail);
						log.debug("Saved detail id: " + detail.getDetailId());
						ordersSaved = true;
					}
				}
				if (ordersSaved) {
					tx.commit();
					log.info("Order Import Process Complete. Number of Orders imported from the store: "
							+ batch.getOimOrderses().size());
				} else {
					log.debug("!! No order saved.");
				}
			} else {
				log.error("FAILURE_GETPRODUCT_NULL_RESPONSE");
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public static String sendRequest(String pingXML, Session session,
			OimChannels channel) {

		String scriptPath = PojoHelper.getChannelAccessDetailValue(channel,
				OimConstants.CHANNEL_ACCESSDETAIL_SCRIPT_PATH);
		log.debug("Checking the script path");
		if (scriptPath == null || scriptPath.equalsIgnoreCase("")) {
			log.warn("Channel is not yet setup for automation.");
			// return false;
		}

		Regex scriptMatch = Regex.perlCode("/http:\\/\\/(.+?)\\/(.+)/i");
		if (scriptMatch.search(scriptPath)) {
			// m_storeURL = scriptMatch.stringMatched(1);
			// m_filePath = "/" + scriptMatch.stringMatched(2);
		} else {
			log.error("FAILED TO PARSE SCRIPT LOCATION!");
			log.error("FAILURE_PARSE_MARKETURL");
			// return false;
		}

		// log.debug("Sending request to " + m_storeURL + m_filePath);

		PrintWriter out = new PrintWriter(System.out);
		FormObject formObj = new FormObject("", "", "", "", false, false,
				false, out, "");
		// FormObject formObj = new FormObject(m_storeURL, m_filePath, "", "",
		// false, false, false, out, "");

		// Find which orders to pull from the store if the pingXML is for order
		// pulling
		Hashtable formData = new Hashtable();
		formData.put("XML_INPUT_VALUE", pingXML);

		if (pingXML.indexOf("<requestType>GetOrders</requestType>") != -1) {
			Query query = session
					.createQuery("select opr from salesmachine.hibernatedb.OimOrderProcessingRule opr where opr.deleteTm is null and opr.oimChannels=:chan");
			query.setEntity("chan", channel);
			Iterator iter = query.iterate();
			if (iter.hasNext()) {
				m_orderProcessingRule = (OimOrderProcessingRule) iter.next();

				if (m_orderProcessingRule.getProcessAll().intValue() == 0) {
					String status = m_orderProcessingRule
							.getProcessWithStatus();
					log.debug("Status to pull :{}", status);

					formData.put("orderpulltype", status);
				}
			}
		}

		formObj.addData(formData);
		formObj.handleRedirects();
		formObj.setTimeOut(60 * 1000 * 30);
		CookieModule.setCookiePolicyHandler(null);
		try {
			formObj.hitForm("Post", null);
		} catch (Exception e) {
			StringBuffer buffer = new StringBuffer(1024);
			buffer.append("Exception occurred during sending XML Request for VID '");
			// buffer.append(market.vendor_id);
			buffer.append("' & VMMID '");
			// buffer.append(market.vendor_market_map_id);
			buffer.append("'\nRequest: ----------> ");
			buffer.append(pingXML);
			buffer.append("\nException during hitting the FO ------------> ");
			buffer.append(e.getMessage());
			log.debug(buffer.toString());
			ExcHandle.printStackTraceToErr(e);
		}

		if (formObj.okay) {
			return formObj.page;
		} else {
			log.debug("Ping Failure. Page Contents:\n" + formObj.page);
		}
		return "";
	}

	public static String sendGetOrdersRequest(OimChannels channel,
			Session session) {
		String getprod_xml = "<xmlPopulate>"
				+ "<header>"
				+ "<requestType>GetOrders</requestType>"
				+ "<passkey>"
				+ PojoHelper.getChannelAccessDetailValue(channel,
						OimConstants.CHANNEL_ACCESSDETAIL_AUTH_KEY)
				+ "</passkey>" + "</header>" + "</xmlPopulate>";
		String getprod_response = sendRequest(getprod_xml, session, channel);
		return getprod_response.trim();
	}

	private static OimOrderBatches parseGetProdResponse(
			StringReader xml_toparse, Map supplierMap, OimChannels channel) {
		try {
			DecimalFormat df = new DecimalFormat("#.##");
			OimOrderBatches batch = new OimOrderBatches();
			batch.setOimChannels(channel);
			batch.setOimOrderBatchesTypes(new OimOrderBatchesTypes(
					OimConstants.ORDERBATCH_TYPE_ID_AUTOMATED));

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
						// TODO Auto-generated catch block
						e.printStackTrace();
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
							if (sku.trim().length() == 0) {
								continue;
							}
							log.debug("SKU: {} Qty: {} Price each: {}", sku,
									qty, priceEach);

							String prefix = "";
							if (sku.length() > 2) {
								prefix = sku.substring(0, 2);
							}
							OimSuppliers supplier = null;
							if (supplierMap.containsKey(prefix)) {
								supplier = (OimSuppliers) supplierMap
										.get(prefix);
							}

							// double cost = 0;
							double saleprice = 0;
							int quantity = 1;
							try {
								saleprice = Double.parseDouble(priceEach);
								quantity = Integer.parseInt(qty);
							} catch (Exception ex) {
								ex.printStackTrace();
							}

							OimOrderDetails detail = new OimOrderDetails();
							detail.setSalePrice(new Double(df.format(saleprice)));
							detail.setQuantity(new Integer(quantity));
							detail.setSku(sku);
							detail.setOimSuppliers(supplier);
							detail.setProductName(productName);
							order.getOimOrderDetailses().add(detail);
						}
					}

					String o_note = getTagValue("o_note", element);
					order.setOrderComment(o_note);
					System.out
							.println("Adding order in the batch with order id : "
									+ order.getStoreOrderId());
					batch.getOimOrderses().add(order);
				}
			}
			return batch;
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return null;
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

	public static String sendOrderStatusRequest(OimOrderBatches batch,
			String status, OimChannels channel, Session session) {
		StringBuffer xmlrequest = new StringBuffer("<xmlPopulate>\n"
				+ "<header>\n"
				+ "<requestType>updateorders</requestType>\n"
				+ "<passkey>"
				+ PojoHelper.getChannelAccessDetailValue(channel,
						OimConstants.CHANNEL_ACCESSDETAIL_AUTH_KEY)
				+ "</passkey>\n" + "</header>\n");

		Set orders = batch.getOimOrderses();
		for (Iterator it = orders.iterator(); it.hasNext();) {
			OimOrders order = (OimOrders) it.next();
			xmlrequest.append("<xml_order>\n");
			xmlrequest.append("<order_id>" + order.getStoreOrderId()
					+ "</order_id>");
			xmlrequest.append("<order_status>" + status + "</order_status>");
			xmlrequest.append("</xml_order>\n");
		}
		xmlrequest.append("</xmlPopulate>");

		String getprod_response = sendRequest(xmlrequest.toString(), session,
				channel);
		return getprod_response.trim();
	}

	private static List parseUpdateResponse(StringReader xmlToParse) {
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
						log.debug("Updated order: {}", nValue.getNodeValue());
						updatedOrders.add(nValue.getNodeValue());
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return updatedOrders;
	}

	public static ArrayList getCurrentOrders(OimChannels channel,
			Session session) {
		ArrayList orders = new ArrayList();

		Query query = session
				.createQuery("select o from salesmachine.hibernatedb.OimOrders o where o.oimOrderBatches.oimChannels=:chan");
		query.setEntity("chan", channel);
		Iterator iter = query.iterate();
		while (iter.hasNext()) {
			OimOrders o = (OimOrders) iter.next();
			orders.add(o.getStoreOrderId());
		}

		return orders;
	}
}
