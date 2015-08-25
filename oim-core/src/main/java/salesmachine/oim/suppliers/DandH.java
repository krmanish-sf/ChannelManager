package salesmachine.oim.suppliers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimFields;
import salesmachine.hibernatedb.OimFileFieldMap;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderProcessingRule;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.Reps;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.stores.impl.OrderImportManager;
import salesmachine.oim.suppliers.exception.SupplierCommunicationException;
import salesmachine.oim.suppliers.exception.SupplierConfigurationException;
import salesmachine.oim.suppliers.exception.SupplierOrderException;
import salesmachine.oim.suppliers.exception.SupplierOrderTrackingException;
import salesmachine.oim.suppliers.modal.OrderDetailResponse;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.oim.suppliers.modal.TrackingData;
import salesmachine.oim.suppliers.modal.dh.XMLRESPONSE;
import salesmachine.oim.suppliers.modal.dh.XMLRESPONSE.ORDERSTATUS;
import salesmachine.oim.suppliers.modal.dh.XMLRESPONSE.ORDERSTATUS.PACKAGE;
import salesmachine.util.OimLogStream;
import salesmachine.util.StringHandle;

import com.amazonservices.mws.client.MwsUtl;

/***
 * 
 * @author amit-yadav
 *
 */
public class DandH extends Supplier implements HasTracking {
	private static final String SERVICE_URL = "https://www.dandh.com/dhXML/xmlDispatch";
	private static final Logger log = LoggerFactory.getLogger(DandH.class);
	private Reps r = null;

	/***
	 * @Requirement for Integration 1) Username : login ID 2) Password :
	 *              password This method send orders to DandH
	 * @param vendorId
	 *            VendorID
	 * @param ovs
	 *            Order vendor supplier
	 * @param orders
	 *            list of orders containing order info.
	 * @throws SupplierOrderException
	 * @throws SupplierCommunicationException
	 * @throws SupplierConfigurationException
	 * @throws ChannelOrderFormatException
	 * @throws ChannelCommunicationException
	 */

	public void sendOrders(Integer vendorId, OimVendorSuppliers ovs, List orders)
			throws SupplierConfigurationException,
			SupplierCommunicationException, SupplierOrderException,
			ChannelConfigurationException, ChannelCommunicationException,
			ChannelOrderFormatException {
		logStream.println("Started sending orders to DandH");

		// populate orderSkuPrefixMap with channel id and the prefix to be used
		// for the given supplier.
		orderSkuPrefixMap = setSkuPrefixForOrders(ovs);

		Session session = SessionManager.currentSession();

		try {
			r = (Reps) session.createCriteria(Reps.class)
					.add(Restrictions.eq("vendorId", vendorId)).uniqueResult();
			Vendors v = new Vendors();
			v.setVendorId(r.getVendorId());
			createAndPostXMLRequest(orders, getFileFieldMap(),
					new StandardFileSpecificsProvider(session, ovs, v), ovs,
					vendorId, r);
		} catch (RuntimeException e1) {
			log.error("Error in sending orders", e1);
			updateVendorSupplierOrderHistory(vendorId, ovs.getOimSuppliers(),
					"Error in sending orders", ERROR_ORDER_PROCESSING);
		}
	}

	private List<OimFileFieldMap> getFileFieldMap() {
		List<OimFileFieldMap> fileFieldMaps = new ArrayList<OimFileFieldMap>();
		// For blank headers, header values will be append to next header value
		// which is not blank.
		// In this case headers after "Description" are all blank so they will
		// append in header "Address"
		String fields[] = { "SHIPTONAME", "SHIPTOATTN", "SHIPTOADDRESS",
				"SHIPTOADDRESS2", "SHIPTOCITY", "SHIPTOSTATE", "SHIPTOZIP",
				"SHIPCARRIER", "SHIPSERVICE", "PONUM", "/ORDERHEADER",
				"ORDERITEMS", "ITEM", "PARTNUM", "QTY", "/ITEM" };

		Integer mappedFieldIds[] = { 3, 3, 4, 0, 5, 36, 7, 35, 10, 2, 0, 0, 0,
				1, 9, 0 };

		for (int i = 0; i < fields.length; i++) {
			OimFields field = new OimFields(fields[i], fields[i], new Date(),
					null, null);
			field.setFieldId(mappedFieldIds[i]);
			OimFileFieldMap ffm = new OimFileFieldMap(null, field, fields[i],
					new Date(), null, "", "");
			fileFieldMaps.add(ffm);
		}
		return fileFieldMaps;
	}

	private void createAndPostXMLRequest(List orders,
			List<OimFileFieldMap> fileFieldMaps,
			IFileSpecificsProvider fileSpecifics, OimVendorSuppliers ovs,
			Integer vendorId, Reps r) throws SupplierConfigurationException,
			SupplierCommunicationException, SupplierOrderException,
			ChannelCommunicationException, ChannelOrderFormatException {
		String USERID = ovs.getLogin();
		String PASSWORD = ovs.getPassword();
		String lincenceKey = ovs.getAccountNumber();
		boolean emailNotification = false;
		String name = StringHandle.removeNull(r.getFirstName()) + " "
				+ StringHandle.removeNull(r.getLastName());
		String emailContent = "Dear " + name + "<br>";
		emailContent += "<br>Following is the status of the orders processed for the supplier "
				+ ovs.getOimSuppliers().getSupplierName() + " : - <br>";
		// Write the data now
		for (int i = 0; i < orders.size(); i++) {
			OimOrders order = (OimOrders) orders.get(i);
			boolean addShippingDetails = true;
			StringBuilder xmlOrder = new StringBuilder();

			for (Iterator detailIt = order.getOimOrderDetailses().iterator(); detailIt
					.hasNext();) {
				OimOrderDetails detail = (OimOrderDetails) detailIt.next();
				if (!detail.getOimSuppliers().getSupplierId()
						.equals(ovs.getOimSuppliers().getSupplierId()))
					continue;
				try { // for all the order details
					for (OimFileFieldMap map : fileFieldMaps) {
						String fieldValue = StringHandle
								.removeNull(fileSpecifics
										.getFieldValueFromOrder(detail, map));
						String mappedFieldName = StringHandle.removeNull(map
								.getMappedFieldName());
						if (addShippingDetails) {
							switch (mappedFieldName) {
							case "SHIPTONAME":
								xmlOrder.append("<SHIPTONAME><![CDATA[")
										.append(fieldValue)
										.append("]]></SHIPTONAME>");
								break;
							case "SHIPTOATTN":
								xmlOrder.append("<SHIPTOATTN></SHIPTOATTN>");
								break;
							case "SHIPTOADDRESS":
								xmlOrder.append("<SHIPTOADDRESS><![CDATA[")
										.append(fieldValue)
										.append("]]></SHIPTOADDRESS>");
								break;
							case "SHIPTOADDRESS2":
								xmlOrder.append("<SHIPTOADDRESS2><![CDATA[")
										.append(fieldValue)
										.append("]]></SHIPTOADDRESS2>");
								break;
							case "SHIPTOCITY":
								xmlOrder.append("<SHIPTOCITY><![CDATA[")
										.append(fieldValue)
										.append("]]></SHIPTOCITY>");
								break;
							case "SHIPTOSTATE":
								xmlOrder.append("<SHIPTOSTATE><![CDATA[")
										.append(fieldValue)
										.append("]]></SHIPTOSTATE>");
								break;
							case "SHIPTOZIP":
								xmlOrder.append("<SHIPTOZIP><![CDATA[")
										.append(fieldValue)
										.append("]]></SHIPTOZIP>");
								break;
							case "SHIPCARRIER":
								xmlOrder.append("<SHIPCARRIER><![CDATA[")
										.append(fieldValue)
										.append("]]></SHIPCARRIER>");
								break;
							case "SHIPSERVICE":
								xmlOrder.append("<SHIPSERVICE><![CDATA[")
										.append(fieldValue)
										.append("]]></SHIPSERVICE>");
								break;
							case "PONUM":
								xmlOrder.append("<PONUM><![CDATA[DH-")
										.append(fieldValue)
										.append("]]></PONUM>");
								break;
							case "/ORDERHEADER":
								xmlOrder.append("</ORDERHEADER>");
								break;
							case "ORDERITEMS":
								xmlOrder.append("<ORDERITEMS>");
								addShippingDetails = false;
								break;
							}
						} else {
							switch (mappedFieldName) {
							case "ITEM":
								xmlOrder.append("<ITEM>");
								break;
							case "PARTNUM":
								xmlOrder.append("<PARTNUM><![CDATA[")
										.append(fieldValue)
										.append("]]></PARTNUM>");
								break;
							case "QTY":
								xmlOrder.append("<QTY><![CDATA[")
										.append(fieldValue).append("]]></QTY>");
								break;
							case "/ITEM":
								xmlOrder.append("</ITEM>");
								break;
							}
						}
					}
				} catch (RuntimeException e) {
					log.error(e.getMessage(), e);
					String message = "Error in posting order";
					updateVendorSupplierOrderHistory(vendorId,
							ovs.getOimSuppliers(),
							message + ": " + e.getMessage(),
							ERROR_ORDER_PROCESSING);
					failedOrders.add(detail.getDetailId());
					detail.setSupplierOrderStatus(message);
					Session session = SessionManager.currentSession();
					session.update(detail);
					xmlOrder = new StringBuilder();
				}
			}
			if (xmlOrder.length() == 0)
				return;
			String xmlRequest = "<XMLFORMPOST>\n"
					+ "<REQUEST>orderEntry</REQUEST>\n" + "<LOGIN>\n"
					+ "<USERID>" + USERID + "</USERID>\n" + "<PASSWORD>"
					+ PASSWORD + "</PASSWORD>\n" + "</LOGIN>\n"
					+ "<ORDERHEADER>\n"
					+ "<BACKORDERALLOW>N</BACKORDERALLOW>\n" + xmlOrder
					+ "</ORDERITEMS>\n" + "</XMLFORMPOST>";

			log.debug("Post Request : {}", xmlRequest);
			String xmlResponse = null;
			try {
				xmlResponse = postRequest(xmlRequest, r);
			} catch (RuntimeException e) {
				log.error(e.getMessage(), e);
			}
			String orderMessage = "";
			if (xmlResponse == null) {
				orderMessage = "There was error in sending order to Supplier.";
			} else if (getXPath(xmlResponse, "/XMLRESPONSE/STATUS/text()")
					.equalsIgnoreCase("SUCCESS")) {
				for (Iterator detailIt = order.getOimOrderDetailses()
						.iterator(); detailIt.hasNext();) {
					OimOrderDetails detail = (OimOrderDetails) detailIt.next();
					if (!detail.getOimSuppliers().getSupplierId()
							.equals(ovs.getOimSuppliers().getSupplierId()))
						continue;
					orderMessage = getXPath(xmlResponse,
							"/XMLRESPONSE/ORDERNUM/text()");
					// detail.setSupplierOrderNumber(orderMessage);
					// detail.setSupplierOrderStatus("Sent to supplier.");
					// Session session = SessionManager.currentSession();
					// session.update(detail);
					successfulOrders.put(detail.getDetailId(),
							new OrderDetailResponse(orderMessage,
									"Sent to supplier."));

					OimChannels oimChannels = order.getOimOrderBatches()
							.getOimChannels();
					Integer channelId = oimChannels.getChannelId();
					IOrderImport iOrderImport = OrderImportManager
							.getIOrderImport(channelId);
					OimLogStream stream = new OimLogStream();
					if (iOrderImport != null) {
						log.debug("Created the iorderimport object");
						try {
							if (!iOrderImport.init(channelId,
									SessionManager.currentSession())) {
								log.debug(
										"Failed initializing the channel with Id:{}",
										channelId);
							} else {
								OrderStatus orderStatus = new OrderStatus();
								orderStatus
										.setStatus(((OimOrderProcessingRule) oimChannels
												.getOimOrderProcessingRules()
												.iterator().next())
												.getProcessedStatus());
								iOrderImport.updateStoreOrder(detail,
										orderStatus);
							}
						} catch (ChannelConfigurationException e) {
							stream.println(e.getMessage());
						}
					} else {
						log.error("Could not find a bean to work with this Channel.");
						stream.println("This Channel type is not supported for pushing order updates.");
					}
				}
			} else {
				for (Iterator detailIt = order.getOimOrderDetailses()
						.iterator(); detailIt.hasNext();) {
					OimOrderDetails detail = (OimOrderDetails) detailIt.next();
					if (!detail.getOimSuppliers().getSupplierId()
							.equals(ovs.getOimSuppliers().getSupplierId()))
						continue;
					// <?xml version="1.0" encoding="UTF-8" ?>
					// <XMLRESPONSE><STATUS>failure</STATUS><MESSAGE>Duplicate
					// PO Number</MESSAGE></XMLRESPONSE>
					orderMessage = getXPath(xmlResponse,
							"/XMLRESPONSE/MESSAGE/text()");
					detail.setSupplierOrderStatus(orderMessage);
					Session session = SessionManager.currentSession();
					session.update(detail);
					failedOrders.add(detail.getDetailId());

					OimChannels oimChannels = order.getOimOrderBatches()
							.getOimChannels();
					Integer channelId = oimChannels.getChannelId();
					IOrderImport iOrderImport = OrderImportManager
							.getIOrderImport(channelId);
					OimLogStream stream = new OimLogStream();
					if (iOrderImport != null) {
						log.debug("Created the iorderimport object");
						try {
							if (!iOrderImport.init(channelId,
									SessionManager.currentSession())) {
								log.debug(
										"Failed initializing the channel with Id:{}",
										channelId);
							} else {
								OrderStatus orderStatus = new OrderStatus();
								orderStatus
										.setStatus(((OimOrderProcessingRule) oimChannels
												.getOimOrderProcessingRules()
												.iterator().next())
												.getFailedStatus());
								iOrderImport.updateStoreOrder(detail,
										orderStatus);
							}
						} catch (ChannelConfigurationException e) {
							stream.println(e.getMessage());
						}
					} else {
						log.error("Could not find a bean to work with this Channel.");
						stream.println("This Channel type is not supported for pushing order updates.");
					}
				}
				updateVendorSupplierOrderHistory(
						vendorId,
						ovs.getOimSuppliers(),
						orderMessage,
						orderMessage.contains("login") ? ERROR_UNCONFIGURED_SUPPLIER
								: ERROR_ORDER_PROCESSING);
			}

			// Send Email Notifications if is set to true.
			if (order.getOimOrderBatches().getOimChannels()
					.getEmailNotifications() == 1) {
				emailNotification = true;
				String orderStatus = (xmlResponse != null && xmlResponse
						.indexOf("<STATUS>success</STATUS>") != -1) == true ? "Successfully Placed"
						: "Failed to place order";
				emailContent += "<b>Store Order ID " + order.getStoreOrderId()
						+ "</b> -> " + orderStatus + " ";
				emailContent += "<br>";
			}

			String logEmailContent = "";
			if (xmlResponse != null) {
				logStream.println("!! ---------------STORE ORDER ID : "
						+ order.getStoreOrderId() + "---------");
				logStream
						.println("!! ORDER PLACED : "
								+ ((xmlResponse
										.indexOf("<xml_action>PROCESS</xml_action>") != -1) == true ? "Yes"
										: "No"));
				logStream.println("!! GET RETURN VALUE : " + xmlResponse);
				logEmailContent = "----------------Store ORDER ID : "
						+ order.getStoreOrderId() + "---------\n\n";
				logEmailContent += "Order Placed : "
						+ ((xmlResponse.indexOf("<STATUS>success</STATUS>") != -1) == true ? "Yes"
								: "No") + "\n\n";
				logEmailContent += "-------------- XML SOAP REQUEST SENT -------------\n";
				logEmailContent += xmlRequest + "\n";
				logEmailContent += "--------------------------------------------------";
				logEmailContent += "-------------- XML SOAP RESPONSE CAME -------------\n";
				logEmailContent += xmlResponse + "\n";
				logEmailContent += "--------------------------------------------------";
			}
			EmailUtil.sendEmail(
					"oim@inventorysource.com",
					"support@inventorysource.com",
					"",
					"Logs of order processing for order : "
							+ order.getStoreOrderId(), logEmailContent);

		}// END for(int i=0;i<orders.size();i++) {
		if (emailNotification) {
			emailContent += "<br>Thanks, <br>Inventory Source Team<br>";
			logStream.println("Sending email to " + r.getLogin());
			EmailUtil.sendEmail(r.getLogin(), "support@inventorysource.com",
					"", "Order Processing Results", emailContent, "text/html");
		}
	}

	private String postRequest(String request, Reps r)
			throws SupplierConfigurationException,
			SupplierCommunicationException, SupplierOrderException {
		URL url;
		HttpsURLConnection connection = null;
		String response = "";
		try {
			// Create connection
			url = new URL(SERVICE_URL);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");

			byte[] req = request.getBytes();
			log.info("Request: {}", request);
			connection.setRequestProperty("Content-Type", "text/xml");
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			OutputStream out = connection.getOutputStream();
			out.write(req);
			out.close();
			connection.connect();
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line + '\n');
			}
			response = sb.toString();
			log.info("Response: {}", response);
			return response;
		} catch (RuntimeException e) {
			log.error("Failed to send request ...", e);
			String logEmailContent = "-------------- Order failed with Exception -------------\n";
			logEmailContent += "-------------- XML SOAP REQUEST SENT -------------\n";
			logEmailContent += request + "\n";
			logEmailContent += "--------------------------------------------------";
			logEmailContent += "-------------- XML SOAP RESPONSE CAME -------------\n";
			logEmailContent += e + "\n";
			logEmailContent += "--------------------------------------------------";
			logStream.println(logEmailContent);
			String emailSubject = "Order failed for Vendor : "
					+ r.getFirstName() + " " + r.getLastName() + " VID : "
					+ r.getVendorId();
			EmailUtil.sendEmail("orders@inventorysource.com",
					"support@inventorysource.com", "", emailSubject,
					logEmailContent);
			throw new SupplierOrderException();
		} catch (MalformedURLException e) {
			log.error(e.getMessage());
			throw new SupplierConfigurationException(
					"Supplier Communication URL is invalid.", e);
		} catch (IOException e) {
			log.error(e.getMessage());
			throw new SupplierCommunicationException();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	@Override
	public OrderStatus getOrderStatus(OimVendorSuppliers oimVendorSuppliers,
			Object trackingMeta) throws SupplierOrderTrackingException {
		if (!(trackingMeta instanceof String))
			throw new IllegalArgumentException(
					"trackingMeta is expected to be a String value containing D&H ORDERNUM.");
		OrderStatus orderStatus = new OrderStatus();
		Session session = SessionManager.currentSession();
		r = (Reps) session
				.createCriteria(Reps.class)
				.add(Restrictions.eq("vendorId", oimVendorSuppliers
						.getVendors().getVendorId())).uniqueResult();
		String response = null;
		String requestData = "<XMLFORMPOST>" + "<REQUEST>orderStatus</REQUEST>"
				+ "<LOGIN><USERID>" + oimVendorSuppliers.getLogin()
				+ "</USERID>" + "<PASSWORD>" + oimVendorSuppliers.getPassword()
				+ "</PASSWORD></LOGIN>" + "<STATUSREQUEST><ORDERNUM>"
				+ trackingMeta + "</ORDERNUM></STATUSREQUEST></XMLFORMPOST>";
		try {
			response = postRequest(requestData, r);

			JAXBContext jaxbContext;
			jaxbContext = JAXBContext.newInstance(XMLRESPONSE.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

			StringReader reader = new StringReader(response);
			XMLRESPONSE orderStatusResponse = (XMLRESPONSE) unmarshaller
					.unmarshal(reader);
			log.info(orderStatusResponse.getSTATUS());
			if ("success".equalsIgnoreCase(orderStatusResponse.getSTATUS())) {

				for (ORDERSTATUS orderstatus2 : orderStatusResponse
						.getORDERSTATUS()) {
					log.info("Invoice:{}", orderstatus2.getINVOICE());
					if (!trackingMeta.toString().equals(
							orderstatus2.getORDERNUM()))
						continue;
					if (!StringHandle.isNullOrEmpty(orderstatus2.getMessage())) {
						orderStatus.setStatus(orderstatus2.getMessage());
						break;
					}
					String invoice = orderstatus2.getINVOICE();
					if ("In Process".equalsIgnoreCase(invoice)) {
						response = invoice;
						orderStatus.setStatus(response);
					} else {
						int perBoxQty = 1;
						int shipQty = 0;
						int detailSize = orderstatus2.getORDERDETAIL()
								.getDETAILITEM().size();
						int packageSize = orderstatus2.getPACKAGE().size();
						log.debug(
								"DETAILITEM Size in tracking response is {}.",
								detailSize);
						log.debug("Package Size: {}", packageSize);
						if (detailSize == 0) {
							log.error("Error in tracking data format.");
						} else {
							shipQty = orderstatus2.getORDERDETAIL()
									.getDETAILITEM().get(0).getQUANTITY();
						}

						if (packageSize == shipQty)
							perBoxQty = 1;
						else if (packageSize > shipQty) {
							perBoxQty = packageSize / shipQty;
						} else {
							perBoxQty = shipQty / packageSize;
						}
						for (PACKAGE package1 : orderstatus2.getPACKAGE()) {
							String shipped = package1.getSHIPPED();
							if ("no".equalsIgnoreCase(shipped)) {
								response = "UnShipped";
								orderStatus.setStatus(response);
							} else {
								orderStatus.setStatus("Shipped");
								TrackingData trackingData = new TrackingData();
								trackingData.setCarrierCode(package1
										.getCARRIER().split(" ")[0]);
								trackingData.setCarrierName(package1
										.getCARRIER());
								trackingData.setShippingMethod(package1
										.getSERVICE());
								trackingData.setShipperTrackingNumber(package1
										.getTRACKNUM());
								trackingData.setQuantity(perBoxQty);
								String dateshipped = package1.getDateshipped();
								XMLGregorianCalendar cal = MwsUtl.getDTF()
										.newXMLGregorianCalendar();
								if (!StringHandle.isNullOrEmpty(dateshipped)) {
									// Date Format: 04/16/15
									int year = 2000 + Integer
											.parseInt(dateshipped.substring(6,
													8));
									int month = Integer.parseInt(dateshipped
											.substring(0, 2));
									int dayOfMonth = Integer
											.parseInt(dateshipped.substring(3,
													5));
									cal.setYear(year);
									cal.setMonth(month);
									cal.setDay(dayOfMonth);
								}
								trackingData.setShipDate(cal);
								orderStatus.addTrackingData(trackingData);
							}

						}
					}
				}
			} else {
				orderStatus.setStatus(orderStatusResponse.getMESSAGE());
			}

		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
		} catch (SupplierConfigurationException e) {
			log.error(e.getMessage(), e);
		} catch (SupplierCommunicationException e) {
			log.error(e.getMessage(), e);
		} catch (SupplierOrderException e) {
			log.error(e.getMessage(), e);
		}

		if (orderStatus.getStatus() == null) {
			throw new SupplierOrderTrackingException(
					"Error in getting order status from Supplier while tracking Tracking Id- "
							+ trackingMeta);
		} else if (orderStatus.getTrackingData() == null)
			throw new SupplierOrderTrackingException(
					"Error in getting order tracking details from Supplier while tracking Tracking Id- "
							+ trackingMeta);
		return orderStatus;
	}

	public static String getXPath(String xmlString, String xPath) {
		String retVal = null;
		try {
			InputStream is = new ByteArrayInputStream(
					xmlString.getBytes("UTF-8"));
			DocumentBuilderFactory domFactory = DocumentBuilderFactory
					.newInstance();
			domFactory.setNamespaceAware(true);
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(is);
			XPath xpath = XPathFactory.newInstance().newXPath();
			// XPath Query for showing all nodes value

			javax.xml.xpath.XPathExpression expr = xpath.compile(xPath);
			Object result = expr.evaluate(doc, XPathConstants.NODE);
			retVal = ((org.w3c.dom.Node) result).getNodeValue();

		} catch (XPathExpressionException e) {
			log.error(e.getMessage(), e);
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		} catch (ParserConfigurationException e) {
			log.error(e.getMessage(), e);
		} catch (SAXException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return retVal;
	}
}
