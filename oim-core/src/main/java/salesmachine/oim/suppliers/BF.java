package salesmachine.oim.suppliers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

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

public class BF extends Supplier {
	/***
	 * @Requirement for Integration 1) Username : login ID 2) Password :
	 *              password 3) Account : lincenceKey This method send orders to
	 *              BnF USA line supplie
	 * @param vendorId
	 *            VendorID
	 * @param ovs
	 *            Order vendor supplier
	 * @param orders
	 *            list of orders containing order info.
	 */
	public void sendOrders(Integer vendorId, OimVendorSuppliers ovs, List orders) {
		logStream.println("!!Started sending orders to BnF USA");
		// populate orderSkuPrefixMap with channel id and the prefix to be used
		// for the given supplier.
		orderSkuPrefixMap = setSkuPrefixForOrders(ovs);
		Session session = SessionManager.currentSession();
		Reps r = (Reps) session.createCriteria(Reps.class)
				.add(Restrictions.eq("vendorId", vendorId)).uniqueResult();
		Vendors v = new Vendors();
		v.setVendorId(r.getVendorId());
		try {
			createAndPostXMLRequest(orders, getFileFieldMap(),
					new StandardFileSpecificsProvider(session, ovs, v), ovs,
					vendorId, r);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private List getFileFieldMap() {
		List fileFieldMaps = new ArrayList();
		// For blank headers, header values will be append to next header value
		// which is not blank.
		// In this case headers after "Description" are all blank so they will
		// append in header "Address"
		String fields[] = { "order", "id", "shipping", "ship_to",
				"ship_contact", "ship_add", "ship_add2", "ship_city",
				"ship_state", "ship_zip", "/shipping", "processing", "po_num",
				"exp", "ship_via", "Ship_acct", "inv_notes", "/processing",
				"/order", "item", "vend_id", "item_id", "qty", "/item" };

		Integer mappedFieldIds[] = { 0, 2, 0, 3, 0, 4, 0, 5, 6, 7, 0, 0, 2, 0,
				10, 0, 0, 0, 0, 0, 0, 1, 9, 0 };

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
		Session session = SessionManager.currentSession();
		Vendors v = new Vendors();
		v.setVendorId(r.getVendorId());
		HashMap shippingMethods = loadSupplierShippingMap(session,
				ovs.getOimSuppliers(), v);
		// Write the data now
		for (int i = 0; i < orders.size(); i++) {
			OimOrders order = (OimOrders) orders.get(i);
			String shippingDetails = StringHandle.removeNull(order
					.getShippingDetails());
			String shippingMethodCode = findShippingCodeFromUserMapping(
					shippingMethods, shippingDetails);
			if (shippingMethodCode.length() == 0) {
				logStream
						.println(shippingDetails
								+ ": Couldnt find the shipping code | Assigning the default shipping : "
								+ ovs.getDefShippingMethodCode());
				shippingMethodCode = StringHandle.removeNull(ovs
						.getDefShippingMethodCode());
			} else {
				logStream.println("!!! Shipping method code found : "
						+ shippingMethodCode);
			}

			if (shippingMethodCode == null
					|| shippingMethodCode.trim().length() == 0) {
				shippingMethodCode = "";
				logStream
						.println("Shipping method code could not be found as defined in the order shipping details and also the default shipping code was not set.");
			}
			order.setShippingDetails(shippingMethodCode);

			boolean addShippingDetails = true;
			String val = "";
			for (Iterator detailIt = order.getOimOrderDetailses().iterator(); detailIt
					.hasNext();) {
				OimOrderDetails detail = (OimOrderDetails) detailIt.next();
				// for all the order details
				for (Iterator it = fileFieldMaps.iterator(); it.hasNext();) {
					OimFileFieldMap map = (OimFileFieldMap) it.next();
					String fieldValue = StringHandle.removeNull(fileSpecifics
							.getFieldValueFromOrder(detail, map));
					if ("id".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && addShippingDetails) {
						val += "<order>\n" + "<" + map.getMappedFieldName()
								+ "><![CDATA["
								+ StringHandle.removeNull(fieldValue) + "]]></"
								+ map.getMappedFieldName() + ">\n";
					} else if ("shipping".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && addShippingDetails) {
						val += "<" + map.getMappedFieldName() + ">\n";
					} else if ("ship_to".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && addShippingDetails) {
						val += "<" + map.getMappedFieldName() + "><![CDATA["
								+ StringHandle.removeNull(fieldValue) + "]]></"
								+ map.getMappedFieldName() + ">\n";
					} else if ("ship_contact".equals(StringHandle
							.removeNull(map.getMappedFieldName()))
							&& addShippingDetails) {
						val += "<" + map.getMappedFieldName() + "><![CDATA["
								+ StringHandle.removeNull(fieldValue) + "]]></"
								+ map.getMappedFieldName() + ">\n";
					} else if ("ship_add".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && addShippingDetails) {
						val += "<" + map.getMappedFieldName() + "><![CDATA["
								+ StringHandle.removeNull(fieldValue) + "]]></"
								+ map.getMappedFieldName() + ">\n";
					} else if ("ship_add2".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && addShippingDetails) {
						val += "<" + map.getMappedFieldName() + "><![CDATA["
								+ StringHandle.removeNull(fieldValue) + "]]></"
								+ map.getMappedFieldName() + ">\n";
					} else if ("ship_city".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && addShippingDetails) {
						val += "<" + map.getMappedFieldName() + "><![CDATA["
								+ StringHandle.removeNull(fieldValue) + "]]></"
								+ map.getMappedFieldName() + ">\n";
					} else if ("ship_state".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && addShippingDetails) {
						val += "<" + map.getMappedFieldName() + "><![CDATA["
								+ StringHandle.removeNull(fieldValue) + "]]></"
								+ map.getMappedFieldName() + ">\n";
					} else if ("ship_zip".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && addShippingDetails) {
						val += "<" + map.getMappedFieldName() + "><![CDATA["
								+ StringHandle.removeNull(fieldValue) + "]]></"
								+ map.getMappedFieldName() + ">\n";
					} else if ("/shipping".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && addShippingDetails) {
						val += "<" + map.getMappedFieldName() + ">\n";
					} else if ("processing".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && addShippingDetails) {
						val += "<" + map.getMappedFieldName() + ">\n";
					} else if ("po_num".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && addShippingDetails) {
						val += "<" + map.getMappedFieldName() + "><![CDATA["
								+ StringHandle.removeNull(fieldValue) + "]]></"
								+ map.getMappedFieldName() + ">\n";
					} else if ("exp".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && addShippingDetails) {
						val += "<" + map.getMappedFieldName() + ">STD</"
								+ map.getMappedFieldName() + ">\n";
					} else if ("ship_via".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && addShippingDetails) {
						val += "<" + map.getMappedFieldName()
								+ "><![CDATA[1]]></" + map.getMappedFieldName()
								+ ">\n";
						// val +=
						// "<"+map.getMappedFieldName()+"><![CDATA["+StringHandle.removeNull(fieldValue)+"]]></"+map.getMappedFieldName()+">\n";
					} else if ("ship_acct".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && addShippingDetails) {
						val += "<" + map.getMappedFieldName() + "><![CDATA["
								+ StringHandle.removeNull(fieldValue) + "]]></"
								+ map.getMappedFieldName() + ">\n";
					} else if ("id".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && addShippingDetails) {
						val += "<" + map.getMappedFieldName() + "><![CDATA["
								+ StringHandle.removeNull(fieldValue) + "]]></"
								+ map.getMappedFieldName() + ">\n";
					} else if ("inv_notes".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && addShippingDetails) {
						val += "<" + map.getMappedFieldName() + "><![CDATA["
								+ StringHandle.removeNull(fieldValue) + "]]></"
								+ map.getMappedFieldName() + ">\n";
					} else if ("/processing".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && addShippingDetails) {
						val += "<" + map.getMappedFieldName() + ">\n";
					} else if ("/order".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && addShippingDetails) {
						val += "<" + map.getMappedFieldName() + ">\n<items>\n";
						addShippingDetails = false;
					} else if ("item".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && !addShippingDetails) {
						val += "<" + map.getMappedFieldName() + ">\n";
					} else if ("vend_id".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && !addShippingDetails) {
						val += "<" + map.getMappedFieldName() + "><![CDATA["
								+ StringHandle.removeNull(fieldValue) + "]]></"
								+ map.getMappedFieldName() + ">\n";
					} else if ("item_id".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && !addShippingDetails) {
						val += "<" + map.getMappedFieldName() + "><![CDATA["
								+ StringHandle.removeNull(fieldValue) + "]]></"
								+ map.getMappedFieldName() + ">\n";
					} else if ("qty".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && !addShippingDetails) {
						val += "<" + map.getMappedFieldName() + "><![CDATA["
								+ StringHandle.removeNull(fieldValue) + "]]></"
								+ map.getMappedFieldName() + ">\n";
					} else if ("/item".equals(StringHandle.removeNull(map
							.getMappedFieldName())) && !addShippingDetails) {
						val += "<" + map.getMappedFieldName() + ">\n";
					}
				}
			}// END for (Iterator
				// detailIt=order.getOimOrderDetailses().iterator();
				// detailIt.hasNext();) {
			String xmlRequest = "<orderxml>\n" + "<AccessRequest>\n"
					+ "<XMLlickey>" + lincenceKey + "</XMLlickey>\n"
					+ "<UserId>" + USERID + "</UserId>\n" + "<Password>"
					+ PASSWORD + "</Password>\n" + "<Version>1.1</Version>\n"
					+
					// "<xml_action>TEST</xml_action>\n" +
					"<xml_action>PROCESS</xml_action>\n" + "</AccessRequest>\n"
					+ val + "</items>\n" + "</orderxml>";
			// System.out.println("Post Request : "+xmlRequest);
			String xmlResponse = null;
			try {
				xmlResponse = postOrder(xmlRequest, r);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// Output the response
			if (xmlResponse != null
					&& xmlResponse.indexOf("<xml_action>PROCESS</xml_action>") != -1) {
				for (Iterator detailIt = order.getOimOrderDetailses()
						.iterator(); detailIt.hasNext();) {
					OimOrderDetails detail = (OimOrderDetails) detailIt.next();
					successfulOrders.add(detail.getDetailId());
				}
			} else {
				for (Iterator detailIt = order.getOimOrderDetailses()
						.iterator(); detailIt.hasNext();) {
					OimOrderDetails detail = (OimOrderDetails) detailIt.next();
					failedOrders.add(detail.getDetailId());
				}
				updateVendorSupplierOrderHistory(vendorId, ovs,
						xmlResponse.toString());
			}

			// Send Email Notifications if is set to true.
			if (order.getOimOrderBatches().getOimChannels()
					.getEmailNotifications() == 1) {
				emailNotification = true;
				String orderStatus = (xmlResponse != null && xmlResponse
						.indexOf("<xml_action>PROCESS</xml_action>") != -1) == true ? "Successfully Places"
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
						+ ((xmlResponse
								.indexOf("<xml_action>PROCESS</xml_action>") != -1) == true ? "Yes"
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
			url = new URL("https://www.bnfusa.com/xml_xchange/cart/post.lasso");
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
			// System.out.print(connection.getContentLength());

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line + '\n');
			}
			// System.out.println(sb.toString());
			response = sb.toString();

		} catch (Exception e) {
			e.printStackTrace();
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
}
