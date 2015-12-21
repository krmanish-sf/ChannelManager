package salesmachine.oim.suppliers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.Reps;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.suppliers.exception.SupplierCommunicationException;
import salesmachine.oim.suppliers.exception.SupplierConfigurationException;
import salesmachine.oim.suppliers.exception.SupplierOrderException;
import salesmachine.oim.suppliers.modal.OrderDetailResponse;
import salesmachine.util.StringHandle;

public class Mobileline extends Supplier {
	/***
	 * This method send orders to mobile line supplie
	 * 
	 * @param vendorId
	 *            VendorID
	 * @param ovs
	 *            Order vendor supplier
	 * @param orders
	 *            list of orders containing order info.
	 */
	public void sendOrders(Integer vendorId, OimVendorSuppliers ovs, List orders) {
		logStream.println("!!Started sending orders to MobileLine");
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
		HashMap shippingMethods = loadSupplierShippingMap(session,
				ovs.getOimSuppliers(), v);
		if (shippingMethods.size() == 0) {
			logStream
					.println("No shipping mapping set. Can not process orders. ");
		}

		String name = StringHandle.removeNull(r.getFirstName()) + " "
				+ StringHandle.removeNull(r.getLastName());
		String emailContent = "Dear " + name + "<br>";
		emailContent += "<br>Following is the status of the orders processed for the supplier "
				+ ovs.getOimSuppliers().getSupplierName() + " : - <br>";
		boolean emailNotification = false;

		// String requestHeader =
		// "apiuser="+StringHandle.removeNull(ovs.getLogin())+"&apikey="+StringHandle.removeNull(ovs.getPassword())+"&customer_number="+StringHandle.removeNull(ovs.getAccountNumber())+"&method=cconfile";
		String requestHeader = "apiuser="
				+ StringHandle.removeNull(ovs.getLogin()) + "&apikey="
				+ StringHandle.removeNull(ovs.getPassword())
				+ "&customer_number="
				+ StringHandle.removeNull(ovs.getAccountNumber())
				+ "&method=purchaseorder";
		String data = "";
		String answer = null;
		StringBuffer returnData = null;
		for (int i = 0; i < orders.size(); i++) {
			OimOrders order = (OimOrders) orders.get(i);
			String soapRequest = "", soapResponse = "";

			String shippingDetails = StringHandle.removeNull(order
					.getShippingDetails());
			String shippingMethodCode = findShippingCodeFromUserMapping(
					shippingMethods, shippingDetails);
			logStream.println("Shipping Method Code: " + shippingMethodCode);
			String customer_notes = StringHandle.removeNull(order
					.getOrderComment());
			if (customer_notes.length() == 0
					&& shippingMethodCode.toLowerCase().equalsIgnoreCase(
							"customeraccountmodule_customeraccount")) {
				logStream.println("Adding customer_notes: " + shippingDetails);
				customer_notes = shippingDetails;
			}
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
						.println("Shipping method code couldnt be found as defined in the order shipping details and also the default shipping code was not set.");
			}

			String countryId = order.getDeliveryCountry();
			if (countryId != null && countryId.trim().length() > 2) {
				countryId = StringHandle.removeNull((String) countryCodeMapping
						.get(countryId.trim()));
			}

			if (StringHandle.removeNull(countryId).length() == 0) {
				countryId = "US";
				logStream.println("No Country defined - Using US");
			}

			data = "";
			data += requestHeader;
			data += "&po_number="
					+ StringHandle.removeNull(order.getStoreOrderId())
					+ "&customer_note=" + URLEncoder.encode(customer_notes);

			data += "&shipping_method=" + URLEncoder.encode(shippingMethodCode);
			data += "&firstname="
					+ URLEncoder.encode(StringHandle.removeNull(order
							.getDeliveryName()))
					+ "&lastname=&company="
					+ URLEncoder.encode(StringHandle.removeNull(order
							.getDeliveryCompany()));
			data += "&street="
					+ URLEncoder.encode(StringHandle.removeNull(order
							.getDeliveryStreetAddress()))
					+ " "
					+ URLEncoder.encode(StringHandle.removeNull(order
							.getDeliverySuburb()));
			data += "&city="
					+ URLEncoder.encode(StringHandle.removeNull(order
							.getDeliveryCity())) + "&country_id="
					+ URLEncoder.encode(StringHandle.removeNull(countryId));
			data += "&region="
					+ URLEncoder.encode(StringHandle.removeNull(order
							.getDeliveryState()))
					+ "&postcode="
					+ URLEncoder.encode(StringHandle.removeNull(order
							.getDeliveryZip()));

			String telephone = StringHandle
					.removeNull(order.getDeliveryPhone());
			if (telephone.length() == 0)
				telephone = "1";
			data += "&telephone="
					+ URLEncoder.encode(telephone)
					+ "&email="
					+ URLEncoder.encode(StringHandle.removeNull(order
							.getDeliveryEmail()));

			String productsStr = "";

			String supplierPrefix = (String) orderSkuPrefixMap.get(order
					.getOimOrderBatches().getOimChannels().getChannelId());
			supplierPrefix = supplierPrefix == null ? "" : supplierPrefix;

			// Get the skus details and concat with 'data' variable.
			for (Iterator detailIt = order.getOimOrderDetailses().iterator(); detailIt
					.hasNext();) {
				OimOrderDetails detail = (OimOrderDetails) detailIt.next();
				String sku = detail.getSku();

				if (supplierPrefix.length() > 0) {

					if (sku.startsWith(supplierPrefix)) {
						sku = sku.substring(2, sku.length());
					}
				}
				productsStr += sku + "|" + detail.getQuantity() + ",";
			}
			productsStr += "FULFILLMENT WEB|1";
			data += "&products=" + URLEncoder.encode(productsStr);
			logStream.println(data);

			try {
				// Send the request
				URL url = new URL(
						"http://host2.authsafe.com/~bamadmin/inventorysource/oim/mobileline/MobileLineApi.php");
				URLConnection conn = url.openConnection();
				conn.setDoOutput(true);
				OutputStreamWriter writer = new OutputStreamWriter(
						conn.getOutputStream());

				// write parameters
				writer.write(data);
				writer.flush();

				// Get the response
				answer = "";
				returnData = new StringBuffer();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(conn.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					returnData.append(line);
				}

				writer.close();
				reader.close();

				logStream.println(returnData.toString());
				if (returnData != null
						&& returnData.toString().trim().length() > 84) { // length
																			// 84
																			// is
																			// to
																			// make
																			// sure
																			// all
																			// the
																			// tags
																			// '<request>','</request>','<response>'
																			// etc
																			// are
																			// coming
					soapRequest = returnData.substring(
							returnData.indexOf("<request>") + 9,
							returnData.indexOf("</request>"));
					logStream
							.println("-------------- XML SOAP REQUEST SENT -------------\n");
					logStream.println(soapRequest + "\n");

					soapResponse = returnData.substring(
							returnData.indexOf("<response>") + 10,
							returnData.indexOf("</response>"));
					logStream
							.println("-------------- XML SOAP RESPONSE -------------\n");
					logStream.println(soapResponse + "\n");

					answer = returnData.substring(
							returnData.indexOf("<result>") + 8,
							returnData.indexOf("</result>"));
				} else {
					logStream
							.println("!! Blank response came from the supplier side");
				}
				// Output the response
				if (answer.indexOf("Create New Order") != -1) {
					for (Iterator detailIt = order.getOimOrderDetailses()
							.iterator(); detailIt.hasNext();) {
						OimOrderDetails detail = (OimOrderDetails) detailIt
								.next();
						successfulOrders.put(detail.getDetailId(),new OrderDetailResponse()); //TODO get PO Number and add as value here
					}

				} else {
					for (Iterator detailIt = order.getOimOrderDetailses()
							.iterator(); detailIt.hasNext();) {
						OimOrderDetails detail = (OimOrderDetails) detailIt
								.next();
						failedOrders.put(detail.getDetailId(),"Fialed order processing for sku - "+detail.getSku());
					}
					updateVendorSupplierOrderHistory(vendorId,
							ovs.getOimSuppliers(), answer.toString(),
							ERROR_ORDER_PROCESSING);
				}

			} catch (MalformedURLException ex) {
				logStream.println("Malfarmed url exception came");
				for (Iterator detailIt = order.getOimOrderDetailses()
						.iterator(); detailIt.hasNext();) {
					OimOrderDetails detail = (OimOrderDetails) detailIt.next();
					failedOrders.put(detail.getDetailId(),"Fialed order processing for sku - "+detail.getSku());
				}
				logStream.println(ex.getMessage());
				ex.printStackTrace();
			} catch (IOException ex) {
				logStream.println("Malfarmed IOexception came");
				for (Iterator detailIt = order.getOimOrderDetailses()
						.iterator(); detailIt.hasNext();) {
					OimOrderDetails detail = (OimOrderDetails) detailIt.next();
					failedOrders.put(detail.getDetailId(),"Fialed order processing for sku - "+detail.getSku());
				}
				logStream.println(ex.getMessage());
				ex.printStackTrace();
			} catch (Exception e) {
				logStream.println("Exception came");
				for (Iterator detailIt = order.getOimOrderDetailses()
						.iterator(); detailIt.hasNext();) {
					OimOrderDetails detail = (OimOrderDetails) detailIt.next();
					failedOrders.put(detail.getDetailId(),"Fialed order processing for sku - "+detail.getSku());
				}
				logStream.println(e.getMessage());
				e.printStackTrace();
			}

			// Send Email Notifications if is set to true.
			if (order.getOimOrderBatches().getOimChannels()
					.getEmailNotifications() == 1) {
				emailNotification = true;
				String orderStatus = (answer.indexOf("Create New Order") != -1) == true ? "Successfully Places"
						: "Failed to place order";
				emailContent += "<b>Store Order ID " + order.getStoreOrderId()
						+ "</b> -> " + orderStatus + " ";
				emailContent += "<br>";
			}

			String logEmailContent = "";
			if (answer != null) {
				logStream.println("!! ---------------STORE ORDER ID : "
						+ order.getStoreOrderId() + "---------");
				logStream
						.println("!! ORDER PLACED : "
								+ ((answer.indexOf("Create New Order") != -1) == true ? "Yes"
										: "No"));
				logStream.println("!! GET RETURN VALUE : " + answer);
				logEmailContent = "-------------- XML SOAP REQUEST SENT -------------\n";
				logEmailContent += soapRequest + "\n";
				logEmailContent += "--------------------------------------------------";
				logEmailContent = "-------------- XML SOAP RESPONSE CAME -------------\n";
				logEmailContent += soapResponse + "\n";
				logEmailContent += "--------------------------------------------------";
				logEmailContent = "----------------Store ORDER ID : "
						+ order.getStoreOrderId()
						+ "---------\n Order Placed : "
						+ ((answer.indexOf("Create New Order") != -1) == true ? "Yes"
								: "No") + "\n GET RETURN VALUE :" + answer
						+ "\n\n" + logEmailContent;
			}

			EmailUtil.sendEmail(
					"oim@inventorysource.com",
					"support@inventorysource.com",
					"",
					"Logs of order processing for order : "
							+ order.getStoreOrderId(), logEmailContent);
		}
		if (emailNotification) {
			emailContent += "<br>Thanks, <br>Inventory Source Team<br>";
			logStream.println("Sending email to " + r.getLogin());
			EmailUtil.sendEmail(r.getLogin(), "support@inventorysource.com",
					"", "Order Processing Results", emailContent, "text/html");
		}

	}

	/***
	 * 
	 * @return Hashmap containing the shipping info for ML USPS First-Class
	 *         Mail: usps_First-Class Mail Package USPS Parcel Post: usps_Parcel
	 *         Post USPS Priority Mail: usps_Priority Mail USPS Express Mail:
	 *         usps_Express Mail
	 * 
	 *         Customer Account: customeraccountmodule_customeraccount (note:
	 *         you will need to add your shipping method in the comments if you
	 *         are shipping through Terry's UPS account)
	 * 
	 *         Federal Express Ground: fedex_FEDEXGROUND Federal Express Express
	 *         Saver: fedex_FEDEXEXPRESSSAVER Federal Express 2Day:
	 *         fedex_FEDEX2DAY Federal Express Standard Overnight:
	 *         fedex_STANDARDOVERNIGHT Federal Express Priority Overnight:
	 *         fedex_PRIORITYOVERNIGHT Federal Express First Overnight:
	 *         fedex_FIRSTOVERNIGHT
	 * 
	 *         UPS Ground: ups_03 UPS Three-Day Select: ups_12 UPS Second Day
	 *         Air: ups_02 UPS Second Day Air: A.M. ups_59 UPS Next Day Air:
	 *         Saver ups_13 UPS Next Day Air: ups_01 UPS Next Day Air: Early
	 *         A.M. ups_14
	 * 
	 */
	public HashMap getDefaultShippingMapping() {
		HashMap finalMap = new HashMap();

		HashMap map = new HashMap();
		String fedexprefixes[] = { "Federal Express ", "FEDEX " };
		map.put("Ground", "22");
		map.put("Express Saver", "19");
		map.put("2Day", "18");
		map.put("Standard Overnight", "24");
		map.put("Priority Overnight", "21");
		map.put("First Overnight", "20");
		map.put("Home Delivery", "23");
		duplicateMapWithPrefixes(finalMap, map, fedexprefixes);

		map = new HashMap();
		String upsprefixes[] = { "United Parcel Service ", "UPS " };
		map.put("Ground", "26");
		map.put("Three-Day Select", "29");
		map.put("Second Day Air", "25");
		map.put("Next Day Air", "27");
		map.put("Next Day Air", "28");
		map.put("Saturday", "30");
		duplicateMapWithPrefixes(finalMap, map, upsprefixes);

		map = new HashMap();
		String uspsprefixes[] = { "United States Postal Service ", "USPS ",
				"US Mail " };
		map.put("First-Class Mail", "34");
		map.put("APO", "34");
		map.put("FPO", "34");
		map.put("APO/FPO", "34");
		map.put("APO & FPO", "34");
		map.put("Parcel Post", "32");
		map.put("Priority Mail", "33");
		map.put("Express Mail", "31");
		duplicateMapWithPrefixes(finalMap, map, uspsprefixes);

		finalMap.put("Customer Account Fee", "35");
		finalMap.put("OnTrac Shipping", "36");

		return finalMap;
	}

	private HashMap createShippingMethods() {
		HashMap shippingMethods = new HashMap();

		HashMap fedex = new HashMap();
		fedex.put("fedex_FEDEXGROUND", "Federal Express Ground");
		fedex.put("fedex_FEDEXEXPRESSSAVER", "Federal Express Express Saver");
		fedex.put("fedex_FEDEX2DAY", "Federal Express 2Day");
		fedex.put("fedex_STANDARDOVERNIGHT",
				"Federal Express Standard Overnight");
		fedex.put("fedex_PRIORITYOVERNIGHT",
				"Federal Express Priority Overnight");
		fedex.put("fedex_FIRSTOVERNIGHT", "Federal Express First Overnight");
		fedex.put("fedex_GROUNDHOMEDELIVERY", "Federal Express Home Delivery");

		HashMap ups = new HashMap();
		ups.put("ups_03", "Ground");
		ups.put("ups_12", "Three-Day Select");
		ups.put("ups_02", "Second Day Air");
		ups.put("A.M. ups_59", "Second Day Air");
		ups.put("Saver ups_13", "Next Day Air");
		ups.put("ups_01", "Next Day Air");
		ups.put("Early A.M. ups_14", "Next Day Air");
		ups.put("UPSSAT", "Saturday");

		HashMap usps = new HashMap();
		usps.put("usps_First-Class Mail", "First-Class Mail");
		usps.put("usps_First-Class Mail", "APO");
		usps.put("usps_First-Class Mail", "FPO");
		usps.put("usps_First-Class Mail", "APO/FPO");
		usps.put("usps_First-Class Mail", "APO & FPO");
		usps.put("usps_Parcel Post", "Parcel Post");
		usps.put("usps_Priority Mail", "Priority Mail");
		usps.put("usps_Express Mail", "Express Mail");

		HashMap customerAccoutFee = new HashMap();
		customerAccoutFee.put("customeraccountmodule_customeraccount",
				"Customer Account Fee");

		HashMap onTrackShipping = new HashMap();
		onTrackShipping.put("regionalfreeshipping_regionalfreeshipping",
				"OnTrac Shipping");

		shippingMethods.put("FEDEX", fedex);
		shippingMethods.put("Federal Express", fedex);
		shippingMethods.put("United Parcel Service", ups);
		shippingMethods.put("UPS", ups);

		shippingMethods.put("United States Postal Service", usps);
		shippingMethods.put("US Mail", usps);
		shippingMethods.put("USPS", usps);

		shippingMethods.put("Customer Account Fee", customerAccoutFee);
		shippingMethods.put("CAF", customerAccoutFee);
		shippingMethods.put("Ontrac shipping", onTrackShipping);
		shippingMethods.put("OS", onTrackShipping);

		return shippingMethods;
	}

  @Override
  public void sendOrders(Integer vendorId, OimVendorSuppliers ovs, OimOrders orders)
      throws SupplierConfigurationException, SupplierCommunicationException, SupplierOrderException,
      ChannelConfigurationException, ChannelCommunicationException, ChannelOrderFormatException {
    // TODO Auto-generated method stub
    
  }
}
