package salesmachine.oim.suppliers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatedb.OimFields;
import salesmachine.hibernatedb.OimFileFieldMap;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.Reps;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.util.StringHandle;

public class DandH extends Supplier implements HasTracking {
	private static final String SERVICE_URL = "https://www.dandh.com/dhXML/xmlDispatch";
	private static final Logger log = LoggerFactory.getLogger(DandH.class);

	/***
	 * @Requirement for Integration 1) Username : login ID 2) Password :
	 *              password This method send orders to DandH
	 * @param vendorId
	 *            VendorID
	 * @param ovs
	 *            Order vendor supplier
	 * @param orders
	 *            list of orders containing order info.
	 */
	public void sendOrders(Integer vendorId, OimVendorSuppliers ovs, List orders) {
		logStream.println("Started sending orders to DandH");
		orderSkuPrefixMap = setSkuPrefixForOrders(ovs); // populate
														// orderSkuPrefixMap
														// with channel id and
														// the prefix to be used
														// for the given
														// supplier.

		Session session = SessionManager.currentSession();
		Query query = session
				.createQuery("select r from salesmachine.hibernatedb.Reps r where r.vendorId = "
						+ vendorId);
		Reps r = new Reps();
		Iterator repsIt = query.iterate();
		if (repsIt.hasNext()) {
			r = (Reps) repsIt.next();
		}
		Vendors v = new Vendors();
		v.setVendorId(r.getVendorId());
		try {
			createAndPostXMLRequest(orders, getFileFieldMap(),
					new StandardFileSpecificsProvider(session, ovs, v), ovs,
					vendorId, r);
		} catch (Exception e1) {
			log.error("Error in sending orders", e1);

			updateVendorSupplierOrderHistory(vendorId, ovs,
					"Error in sending orders");
		}
	}

	private List getFileFieldMap() {
		List fileFieldMaps = new ArrayList();
		// For blank headers, header values will be append to next header value
		// which is not blank.
		// In this case headers after "Description" are all blank so they will
		// append in header "Address"
		String fields[] = { "SHIPTONAME", "SHIPTOATTN", "SHIPTOADDRESS",
				"SHIPTOADDRESS2", "SHIPTOCITY", "SHIPTOSTATE", "SHIPTOZIP",
				"SHIPCARRIER", "SHIPSERVICE", "PONUM", "/ORDERHEADER",
				"ORDERITEMS", "ITEM", "PARTNUM", "QTY", "/ITEM" };

		Integer mappedFieldIds[] = { 3, 3, 4, 0, 5, 6, 7, 35, 10, 2, 0, 0, 0,
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

	private void createAndPostXMLRequest(List orders, List fileFieldMaps,
			IFileSpecificsProvider fileSpecifics, OimVendorSuppliers ovs,
			Integer vendorId, Reps r) throws Exception {
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
			String val = "";

			for (Iterator detailIt = order.getOimOrderDetailses().iterator(); detailIt
					.hasNext();) {

				OimOrderDetails detail = (OimOrderDetails) detailIt.next();
				try { // for all the order details
					for (Iterator it = fileFieldMaps.iterator(); it.hasNext();) {
						OimFileFieldMap map = (OimFileFieldMap) it.next();
						String fieldValue = StringHandle
								.removeNull(fileSpecifics
										.getFieldValueFromOrder(detail, map));
						if ("SHIPTONAME".equals(StringHandle.removeNull(map
								.getMappedFieldName())) && addShippingDetails) {
							val += "<" + map.getMappedFieldName()
									+ "><![CDATA["
									+ StringHandle.removeNull(fieldValue)
									+ "]]></" + map.getMappedFieldName()
									+ ">\n";
						} else if ("SHIPTOATTN".equals(StringHandle
								.removeNull(map.getMappedFieldName()))
								&& addShippingDetails) {
							val += "<" + map.getMappedFieldName() + "></"
									+ map.getMappedFieldName() + ">\n";
						} else if ("SHIPTOADDRESS".equals(StringHandle
								.removeNull(map.getMappedFieldName()))
								&& addShippingDetails) {
							val += "<" + map.getMappedFieldName()
									+ "><![CDATA["
									+ StringHandle.removeNull(fieldValue)
									+ "]]></" + map.getMappedFieldName()
									+ ">\n";
						} else if ("SHIPTOADDRESS2".equals(StringHandle
								.removeNull(map.getMappedFieldName()))
								&& addShippingDetails) {
							val += "<" + map.getMappedFieldName()
									+ "><![CDATA["
									+ StringHandle.removeNull(fieldValue)
									+ "]]></" + map.getMappedFieldName()
									+ ">\n";
						} else if ("SHIPTOCITY".equals(StringHandle
								.removeNull(map.getMappedFieldName()))
								&& addShippingDetails) {
							val += "<" + map.getMappedFieldName()
									+ "><![CDATA["
									+ StringHandle.removeNull(fieldValue)
									+ "]]></" + map.getMappedFieldName()
									+ ">\n";
						} else if ("SHIPTOSTATE".equals(StringHandle
								.removeNull(map.getMappedFieldName()))
								&& addShippingDetails) {
							val += "<" + map.getMappedFieldName()
									+ "><![CDATA["
									+ StringHandle.removeNull(fieldValue)
									+ "]]></" + map.getMappedFieldName()
									+ ">\n";
						} else if ("SHIPTOZIP".equals(StringHandle
								.removeNull(map.getMappedFieldName()))
								&& addShippingDetails) {
							val += "<" + map.getMappedFieldName()
									+ "><![CDATA["
									+ StringHandle.removeNull(fieldValue)
									+ "]]></" + map.getMappedFieldName()
									+ ">\n";
						} else if ("SHIPCARRIER".equals(StringHandle
								.removeNull(map.getMappedFieldName()))
								&& addShippingDetails) {
							val += "<" + map.getMappedFieldName()
									+ "><![CDATA["
									+ StringHandle.removeNull(fieldValue)
									+ "]]></" + map.getMappedFieldName()
									+ ">\n";
							// val += "<" + map.getMappedFieldName()
							// + "><![CDATA[UPS]]></"
							// + map.getMappedFieldName() + ">\n";
						} else if ("SHIPSERVICE".equals(StringHandle
								.removeNull(map.getMappedFieldName()))
								&& addShippingDetails) {
							val += "<" + map.getMappedFieldName()
									+ "><![CDATA["
									+ StringHandle.removeNull(fieldValue)
									+ "]]></" + map.getMappedFieldName()
									+ ">\n";
							// val += "<" + map.getMappedFieldName()
							// + "><![CDATA[Ground]]></"
							// + map.getMappedFieldName() + ">\n";
						} else if ("PONUM".equals(StringHandle.removeNull(map
								.getMappedFieldName())) && addShippingDetails) {
							val += "<" + map.getMappedFieldName()
									+ "><![CDATA[DH"
									+ StringHandle.removeNull(fieldValue)
									+ "]]></" + map.getMappedFieldName()
									+ ">\n";
						} else if ("/ORDERHEADER".equals(StringHandle
								.removeNull(map.getMappedFieldName()))
								&& addShippingDetails) {
							val += "<" + map.getMappedFieldName() + ">\n";
						} else if ("ORDERITEMS".equals(StringHandle
								.removeNull(map.getMappedFieldName()))
								&& addShippingDetails) {
							val += "<" + map.getMappedFieldName() + ">\n";
							addShippingDetails = false;
						} else if ("ITEM".equals(StringHandle.removeNull(map
								.getMappedFieldName())) && !addShippingDetails) {
							val += "<" + map.getMappedFieldName() + ">\n";
						} else if ("PARTNUM".equals(StringHandle.removeNull(map
								.getMappedFieldName())) && !addShippingDetails) {
							val += "<" + map.getMappedFieldName()
									+ "><![CDATA["
									+ StringHandle.removeNull(fieldValue)
									+ "]]></" + map.getMappedFieldName()
									+ ">\n";
						} else if ("QTY".equals(StringHandle.removeNull(map
								.getMappedFieldName())) && !addShippingDetails) {
							val += "<" + map.getMappedFieldName()
									+ "><![CDATA["
									+ StringHandle.removeNull(fieldValue)
									+ "]]></" + map.getMappedFieldName()
									+ ">\n";
						} else if ("/ITEM".equals(StringHandle.removeNull(map
								.getMappedFieldName())) && !addShippingDetails) {
							val += "<" + map.getMappedFieldName() + ">\n";
						}
					}

				} catch (RuntimeException e) {
					String message = "Error in posting order";
					updateVendorSupplierOrderHistory(vendorId, ovs, message);
					failedOrders.add(detail.getDetailId());
					detail.setSupplierOrderStatus(message);
					Session session = SessionManager.currentSession();
					session.update(detail);
					val = "";
				}
			}
			if (val.length() == 0)
				return;
			String xmlRequest = "<XMLFORMPOST>\n"
					+ "<REQUEST>orderEntry</REQUEST>\n" + "<LOGIN>\n"
					+ "<USERID>" + USERID + "</USERID>\n" + "<PASSWORD>"
					+ PASSWORD + "</PASSWORD>\n" + "</LOGIN>\n"
					+ "<ORDERHEADER>\n"
					+ "<BACKORDERALLOW>N</BACKORDERALLOW>\n" + val
					+ "</ORDERITEMS>\n" + "</XMLFORMPOST>";

			log.debug("Post Request : {}", xmlRequest);
			String xmlResponse = null;
			try {
				xmlResponse = postOrder(xmlRequest, r);
			} catch (Exception e) {
				e.printStackTrace();
			}
			String orderMessage = "";
			if (xmlResponse == null) {
				orderMessage = "There was error in sending order to Supplier.";
			} else if (getXPath(xmlResponse, "/XMLRESPONSE/STATUS/text()")
					.equalsIgnoreCase("SUCCESS")) {
				for (Iterator detailIt = order.getOimOrderDetailses()
						.iterator(); detailIt.hasNext();) {
					OimOrderDetails detail = (OimOrderDetails) detailIt.next();
					orderMessage = getXPath(xmlResponse,
							"/XMLRESPONSE/ORDERNUM/text()");
					detail.setSupplierOrderNumber(orderMessage);
					detail.setSupplierOrderStatus("Sent to supplier.");
					Session session = SessionManager.currentSession();
					session.update(detail);
					successfulOrders.add(detail.getDetailId());
				}
			} else {
				for (Iterator detailIt = order.getOimOrderDetailses()
						.iterator(); detailIt.hasNext();) {
					OimOrderDetails detail = (OimOrderDetails) detailIt.next();
					// <?xml version="1.0" encoding="UTF-8" ?>
					// <XMLRESPONSE><STATUS>failure</STATUS><MESSAGE>Duplicate
					// PO Number</MESSAGE></XMLRESPONSE>
					orderMessage = getXPath(xmlResponse,
							"/XMLRESPONSE/MESSAGE/text()");
					detail.setSupplierOrderStatus(orderMessage);
					Session session = SessionManager.currentSession();
					session.update(detail);
					failedOrders.add(detail.getDetailId());
				}
				updateVendorSupplierOrderHistory(vendorId, ovs, orderMessage);
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

	public String postOrder(String request, Reps r) {
		URL url;
		HttpsURLConnection connection = null;
		String response = "";
		try {
			// Create connection
			url = new URL(SERVICE_URL);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");

			byte[] req = request.getBytes();

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
		} catch (Exception e) {
			log.error("Failed to post orders ...", e);
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
			EmailUtil.sendEmail("oim@inventorysource.com",
					"support@inventorysource.com", "", emailSubject,
					logEmailContent);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return response;
	}

	@Override
	public String getOrderStatus(OimVendorSuppliers oimVendorSuppliers,
			Object trackingMeta) {
		if (!(trackingMeta instanceof String))
			throw new IllegalArgumentException(
					"trackingMeta is expected to be a String value containing PO number.");
		URL url;
		HttpsURLConnection connection = null;
		String response = "";
		String requestData = "<XMLFORMPOST>" + "<REQUEST>orderStatus</REQUEST>"
				+ "<LOGIN><USERID>" + oimVendorSuppliers.getLogin()
				+ "</USERID>" + "<PASSWORD>" + oimVendorSuppliers.getPassword()
				+ "</PASSWORD></LOGIN>" + "<STATUSREQUEST><ORDERNUM>"
				+ trackingMeta + "</ORDERNUM></STATUSREQUEST></XMLFORMPOST>";
		try {
			// Create connection
			url = new URL(SERVICE_URL);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");

			byte[] req = requestData.getBytes();

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
			String invoice = getXPath(response,
					"/XMLRESPONSE/ORDERSTATUS/INVOICE/text()");
			if ("In Process".equalsIgnoreCase(invoice)) {
				response = invoice;
			} else {

				String carrier = getXPath(response,
						"/XMLRESPONSE/ORDERSTATUS/PACKAGE/CARRIER/text()");
				String service = getXPath(response,
						"/XMLRESPONSE/ORDERSTATUS/PACKAGE/SERVICE/text()");
				String shipped = getXPath(response,
						"/XMLRESPONSE/ORDERSTATUS/PACKAGE/SHIPPED/text()");
				String trackNum = getXPath(response,
						"/XMLRESPONSE/ORDERSTATUS/PACKAGE/TRACKNUM/text()");
				if ("no".equalsIgnoreCase(shipped)) {
					response = "Not shipped yet.";
				} else {
					response = carrier + service + ":" + trackNum;
				}
			}
		} catch (IOException e) {
			log.error("Error occured when cheking order status for PO No. :"
					+ trackingMeta);
		} finally {
			if (connection != null)
				connection.disconnect();
		}
		return response;
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
