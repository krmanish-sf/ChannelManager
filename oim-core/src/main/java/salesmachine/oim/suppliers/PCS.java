package salesmachine.oim.suppliers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.rpc.Call;
import javax.xml.rpc.ServiceException;

import org.hibernate.Query;
import org.hibernate.Session;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.Reps;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.util.StringHandle;

import com.suppliers.pcs.OrderAddress;
import com.suppliers.pcs.OrderLineItem;
import com.suppliers.pcs.OrderReturnInfo;
import com.suppliers.pcs.PCSDealersLocator;
import com.suppliers.pcs.PCSDealersSoap_PortType;

public class PCS extends Supplier {
	/***
	 * This method sends orders to PCS supplier.
	 * @param vendorId Vendor ID
	 * @param ovs Order vendor suppliers object
	 * @param orders list of orders containing orders info
	 */
	public void sendOrders(Integer vendorId, OimVendorSuppliers ovs,
			List orders) {
		logStream.println("Sending orders to Pacific Cellular Supply");

		Session session = SessionManager.currentSession();
		Query query = session
				.createQuery("select r from salesmachine.hibernatedb.Reps r where r.vendorId = "
						+ vendorId);
		Reps r = new Reps();
		Iterator repsIt = query.iterate();
		if (repsIt.hasNext()) {
			r = (Reps) repsIt.next();
		}

		Vendors v = new Vendors();v.setVendorId(r.getVendorId());
		HashMap shippingMethods = loadSupplierShippingMap(session,ovs.getOimSuppliers(),v);

		String name = StringHandle.removeNull(r.getFirstName()) + " "
				+ StringHandle.removeNull(r.getLastName());
		String emailContent = "Dear " + name + "<br>";
		emailContent += "<br>Following is the status of the orders processed for the supplier "
				+ ovs.getOimSuppliers().getSupplierName() + " : - <br>";
		boolean emailNotification = false;

		PCSDealersLocator serviceLocator = new PCSDealersLocator();

		PCSDealersSoap_PortType servicePort = null;
		try {
			servicePort = serviceLocator.getPCSDealersSoap();
		} catch (ServiceException e1) {
			// TODO Auto-generated catch block
			logStream.println(e1.getMessage());
			e1.printStackTrace();
		}		
		
		// Iterate over orders
		for (int i = 0; i < orders.size(); i++) {
			OimOrders order = (OimOrders) orders.get(i);
			// Create the address for the order
			OrderAddress address = new OrderAddress();
			address.setFirstName(order.getDeliveryName());
			address.setLastName("");
			address.setAddressLine1(order.getDeliveryStreetAddress());
			address.setAddressLine2(order.getDeliverySuburb());
			address.setCity(order.getDeliveryCity());

			String state = order.getDeliveryState();
			if (state.length() < 3) {
				String stateName = getUSStateFullName(state);
				if (stateName == null) {
					logStream
							.println("!! The state "
									+ state
									+ " is not an accepted state. Please enter a valid state");
					for (Iterator detailIt = order.getOimOrderDetailses()
							.iterator(); detailIt.hasNext();) {
						OimOrderDetails detail = (OimOrderDetails) detailIt
								.next();
						failedOrders.add(detail.getDetailId());
					}

					if (order.getOimOrderBatches().getOimChannels()
							.getEmailNotifications() == 1) {
						emailNotification = true;
						String orderStatus = "Failed to place order";
						emailContent += "<b>Store Order ID "
								+ order.getStoreOrderId() + "</b> -> "
								+ orderStatus + " ";
						emailContent += "(Error while placing : The delivery state is not an acceptable state)";
						emailContent += "<br>";
					}
					continue;
				}
				state = stateName;
				logStream.println("! Got the state from the state code : "
						+ state);
			}

			logStream.println("Setting the state to: " + state);
			address.setStateCode(state);
			address.setCountry(order.getDeliveryCountry());
			address.setZip(order.getDeliveryZip());
			String company = StringHandle
					.removeNull(order.getDeliveryCompany()).trim();
			if (company.length() == 0) {
				company = "N/A";
			}

			address.setCompany(company);
			address.setEmail(order.getDeliveryEmail());
			address.setPhoneNumber(order.getDeliveryPhone());

			// Get the order details
			ArrayList orderLineItemList = new ArrayList();
			for (Iterator detailIt = order.getOimOrderDetailses().iterator(); detailIt
					.hasNext();) {
				OimOrderDetails detail = (OimOrderDetails) detailIt.next();
				// for all the order details
				OrderLineItem item = new OrderLineItem();
				item.setItemno(detail.getSku());
				item.setPriceEach(detail.getSalePrice());
				item.setQuantity(detail.getQuantity());
				orderLineItemList.add(item);
			}

			// OrderLineItem[] orderLineItems =
			// (OrderLineItem[])(orderLineItemList.toArray());
			OrderLineItem[] orderLineItems = new OrderLineItem[orderLineItemList
					.size()];
			Iterator it = orderLineItemList.iterator();
			int j = 0;
			while (it.hasNext()) {
				orderLineItems[j] = (OrderLineItem) it.next();
				j++;
			}

			Calendar orderTime = Calendar.getInstance();
			orderTime.setTime(order.getOrderTm() == null ? order
					.getOrderFetchTm() : order.getOrderTm());
			String shippingDetails = order.getShippingDetails();
			String shippingMethodCode = findShippingCodeFromUserMapping(shippingMethods,shippingDetails);

			if (shippingMethodCode.length() == 0) {
				logStream
						.println("!! Couldnt find the shipping code as defined in the order shipping details so assigning the default shipping : "
								+ ovs.getDefShippingMethodCode());
				shippingMethodCode = ovs.getDefShippingMethodCode();
			}

			OrderReturnInfo response = null;
			Exception orderException = null;
			boolean orderSuccess = false;
			try {
				String note = "";
				if (vendorId == 79224 || vendorId == 89192 || vendorId == 1 || vendorId == 34871)
					note = "TEST ORDER - DO NOT SHIP";
				response = servicePort.insertPCSOrder(ovs.getAccountNumber(),
						ovs.getPassword(), order.getStoreOrderId(), address,
						orderLineItems, shippingMethodCode,
						note, orderTime);				
				if (response.isOrderCreated())
					orderSuccess = true;
			} catch (Exception e) {
				orderException = e;				
				logStream.println(e.getMessage());
				e.printStackTrace(logStream.getPrintStream());
			}

			String logEmailContent = "";
			//Print the XML SOAP REQUEST
			try {
				Call call = serviceLocator.createCall();
				//FIXME NULL ENTITY SET
				String soapRequest = "";//call.getMessageContext().getRequestMessage().getSOAPPartAsString();
				logEmailContent = "-------------- XML SOAP REQUEST SENT -------------\n";
				logEmailContent += soapRequest+"\n";
				logEmailContent += "--------------------------------------------------";
				
				logStream.println(logEmailContent);
//				logStream.println("-------------- XML SOAP REQUEST SENT -------------");
//				logStream.println(soapRequest);
//				logStream.println("--------------------------------------------------");
				
				if (orderException == null) {
					//FIXME NULL ENTITY SET
					String soapResponse ="";// call.getMessageContext().getResponseMessage().getSOAPPartAsString();
					logEmailContent += "\n\n-------------- XML SOAP RESPONSE RECEIVED -------------\n";
					logEmailContent += soapResponse+"\n";
					logEmailContent += "--------------------------------------------------";
					
					logStream.println("-------------- XML SOAP RESPONSE RECEIVED -------------");
					logStream.println(soapResponse);
					logStream.println("--------------------------------------------------");
				}
			} catch (Exception e) {
				logStream.println(e.getMessage());
			}
			
			if (response != null) {
				logStream.println("!! ---------------STORE ORDER ID : "
						+ order.getStoreOrderId() + "---------");
				logStream.println("!! ORDER PLACED : "
						+ response.isOrderCreated());
				logStream.println("!! GET RETURN VALUE : "
						+ response.getReturnValue());
				
				logEmailContent = "----------------Store ORDER ID : "+order.getStoreOrderId()+"---------\n Order Placed : "+response.isOrderCreated()+"\n GET RETURN VALUE :"+response.getReturnValue()+"\n\n"+logEmailContent;
			}

			if (orderSuccess) {
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
			}

			// Send Email Notifications if is set to true.
			if (order.getOimOrderBatches().getOimChannels()
					.getEmailNotifications() == 1) {
				emailNotification = true;
				String orderStatus = orderSuccess == true ? "Successfully Places"
						: "Failed to place order";
				emailContent += "<b>Store Order ID " + order.getStoreOrderId()
						+ "</b> -> " + orderStatus + " ";
				if (!orderSuccess) {
					if (response != null)
						emailContent += "(Error : "
								+ response.getReturnValue() + ")";
					else if (orderException != null){
						emailContent += "(Error : "
							+ orderException.getLocalizedMessage() + ")";
					}
				}
				emailContent += "<br>";
			}

			EmailUtil.sendEmail("oim@inventorysource.com","support@inventorysource.com","","Logs of order processing for order : "+order.getStoreOrderId(),logEmailContent);
			updateVendorSupplierOrderHistory(vendorId, ovs, response);
		}	// Iterate over all orders

		if (emailNotification) {
			emailContent += "<br>Thanks, <br>Inventory Source Team<br>";
			logStream.println("Sending email to " + r.getLogin());
			EmailUtil.sendEmail(r.getLogin(), "support@inventorysource.com",
					"", "Order Processing Results", emailContent, "text/html");
		}
	}
	
	public HashMap getDefaultShippingMapping() {
		HashMap finalMap = new HashMap();
				
		HashMap map = new HashMap();
		String fedexprefixes[] = {"Federal Express ","FEDEX "};
		map.put("Ground","5");
		map.put("Express Saver","3");
		map.put("2Day", "2");
		map.put("Standard Overnight","6");
		map.put("Priority Overnight","1");
		map.put("First Overnight","4" );	
		duplicateMapWithPrefixes(finalMap, map, fedexprefixes);
				
		map = new HashMap();
		String upsprefixes[] = {"United Parcel Service ","UPS "};
		map.put("Ground", "8");
		map.put("2nd Day Air", "7");
		map.put("Canada Standard", "9");
		map.put("Worldwide Express", "10");
		map.put("3 Day  Select","13");
		map.put("Second Day Air","7");
		map.put("Next Day Air","11");
		map.put("Next Day Air Early AM","12");
		map.put("Saturday","14");
		duplicateMapWithPrefixes(finalMap, map, upsprefixes);
		
		map = new HashMap();
		String uspsprefixes[] = {"United States Postal Service ","USPS ","US Mail "};
		map.put("First Class", "16");
		map.put("Priority Mail","17");
		map.put("Express Mail","15");		
		duplicateMapWithPrefixes(finalMap, map, uspsprefixes);

		return finalMap;
	}
	
	/***
	 * 
	 * @return Hashmap containing the shipping info for PCS
	 */
	private HashMap createShippingMethodsMap() {
		HashMap shippingMethods = new HashMap();

		HashMap fedex = new HashMap();
		fedex.put("FEDX1", "Priority Overnight");
		fedex.put("FEDX2", "2nd Day");
		fedex.put("FEDXE", "Express Saver");
		fedex.put("FEDXF", "First Overnight");
		fedex.put("FEDXG", "Ground");
		fedex.put("FEDXS", "Standard Overnight");

		HashMap ups = new HashMap();
		ups.put("UPSB", "2nd Day Air");
		ups.put("UPSG", "Ground");
		ups.put("UPSGC", "Canada Standard");
		ups.put("UPSI", "Worldwide Express");
		ups.put("UPSN", "Next Day Air");
		ups.put("UPSR", "Next Day Air Early AM");
		ups.put("UPSS", "3 Day Select");
		ups.put("UPSSAT", "Saturday");

		HashMap usps = new HashMap();
		usps.put("USPSE", "Express Mail");
		usps.put("USPSF", "First Class");
		usps.put("USPSP", "Priority Mail");

		shippingMethods.put("FEDEX", fedex);
		shippingMethods.put("Federal Express", fedex);
		shippingMethods.put("United Parcel Service", ups);
		shippingMethods.put("UPS", ups);
		shippingMethods.put("United States Postal Service", usps);
		shippingMethods.put("USPS", usps);

		return shippingMethods;
	}	
}
