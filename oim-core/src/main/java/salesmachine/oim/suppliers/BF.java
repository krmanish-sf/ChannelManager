package salesmachine.oim.suppliers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.axis.utils.ByteArrayOutputStream;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimFields;
import salesmachine.hibernatedb.OimFileFieldMap;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderProcessingRule;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSupplierShippingMethod;
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
import salesmachine.oim.suppliers.modal.TrackingData;
import salesmachine.oim.suppliers.modal.bf.OrderXMLresp;
import salesmachine.oim.suppliers.modal.bf.OrderXMLresp.Items.Item;
import salesmachine.oim.suppliers.modal.bf.StatusResponse;
import salesmachine.oim.suppliers.modal.bf.StatusXML;
import salesmachine.oim.suppliers.modal.bf.StatusXML.AccessRequest;
import salesmachine.oim.suppliers.modal.bf.StatusXML.Status;
import salesmachine.oim.suppliers.modal.bf.Trackxml;
import salesmachine.oim.suppliers.modal.bf.Trackxml.Tracking;
import salesmachine.oim.suppliers.modal.bf.Trackxmlresp;
import salesmachine.util.StringHandle;

public class BF extends Supplier implements HasTracking {
	private static final Logger log = LoggerFactory.getLogger(BF.class);

	/*
	 * public enum OrderStatus { Pending("Pending"), In_Progress("In-Progress"),
	 * Shipped("Shipped"); private final String value;
	 * 
	 * OrderStatus(String status) { this.value = status; }
	 * 
	 * public String getValue() { return value; } }
	 */
	/***
	 * @Requirement for Integration 1) Username : login ID 2) Password :
	 *              password 3) Account : lincenceKey This method send orders to
	 *              BnF USA line supplier
	 * @param vendorId
	 *            VendorID
	 * @param ovs
	 *            Order vendor supplier
	 * @param orders
	 *            list of orders containing order info.
	 */
	private Reps r;

	@Override
	public void sendOrders(Integer vendorId, OimVendorSuppliers ovs, List orders)
			throws SupplierConfigurationException,
			SupplierCommunicationException, SupplierOrderException,
			ChannelConfigurationException, ChannelCommunicationException, ChannelOrderFormatException{
		log.info("Started sending orders to BnF USA");
		// populate orderSkuPrefixMap with channel id and the prefix to be used
		// for the given supplier.
		orderSkuPrefixMap = setSkuPrefixForOrders(ovs);
		Session session = SessionManager.currentSession();
		r = (Reps) session.createCriteria(Reps.class)
				.add(Restrictions.eq("vendorId", vendorId)).uniqueResult();
		Vendors v = new Vendors();
		v.setVendorId(r.getVendorId());
		try {
			createAndPostXMLRequest(orders, getFileFieldMap(),
					new StandardFileSpecificsProvider(session, ovs, v), ovs,
					vendorId, r);
		} catch (RuntimeException e1) {
			log.error("Error in sending order ", e1);
			throw new SupplierOrderException(e1.getMessage(), e1);
		}
	}

	private List<OimFileFieldMap> getFileFieldMap() {
		List<OimFileFieldMap> fileFieldMaps = new ArrayList<OimFileFieldMap>();
		// For blank headers, header values will be append to next header value
		// which is not blank.
		// In this case headers after "Description" are all blank so they will
		// append in header "Address"
		String fields[] = { "order", "id", "shipping", "ship_to",
				"ship_contact", "ship_add", "ship_add2", "ship_city",
				"ship_state", "ship_zip", "/shipping", "processing", "po_num",
				"exp", "ship_via", "Ship_acct", "inv_notes", "/processing",
				"/order", "item", "vend_id", "item_id", "qty", "/item" };

		Integer mappedFieldIds[] = { 0, 2, 0, 3, 0, 4, 0, 5, 36, 7, 0, 0, 2, 0,
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

	private void createAndPostXMLRequest(List<OimOrders> orders,
			List fileFieldMaps, IFileSpecificsProvider fileSpecifics,
			OimVendorSuppliers ovs, Integer vendorId, Reps r)
			throws SupplierConfigurationException, SupplierOrderException,
			SupplierCommunicationException, ChannelConfigurationException, ChannelCommunicationException, ChannelOrderFormatException {
		String USERID = ovs.getLogin();
		String PASSWORD = ovs.getPassword();
		String lincenceKey = ovs.getAccountNumber();
		boolean emailNotification = false;
		String name = StringHandle.removeNull(r.getFirstName()) + " "
				+ StringHandle.removeNull(r.getLastName());
		String emailContent = "Dear " + name + "<br>";
		emailContent += "<br>Following is the status of the orders processed for the supplier "
				+ ovs.getOimSuppliers().getSupplierName() + " : - <br>";
		Vendors v = new Vendors();
		v.setVendorId(r.getVendorId());
		List<OimSupplierShippingMethod> shippingMethods = loadSupplierShippingMap(
				ovs.getOimSuppliers(), v);
		// Write the data now
		for (OimOrders order : orders) {
			String shippingDetails = StringHandle.removeNull(order
					.getShippingDetails());
			String shippingMethodCode;
			OimSupplierShippingMethod shippingMethod = findShippingCodeFromUserMapping(
					shippingMethods, order.getOimShippingMethod());
			if (shippingMethod == null) {
				log.warn(shippingDetails
						+ ": Couldnt find the shipping code | Assigning the default shipping : "
						+ ovs.getDefShippingMethodCode());
				shippingMethodCode = StringHandle.removeNull(ovs
						.getDefShippingMethodCode());
			} else {
				log.info("Shipping method code found : " + shippingMethod);
				shippingMethodCode = shippingMethod.toString();
			}
			// order.setShippingDetails(shippingMethodCode);

			boolean addShippingDetails = true;
			String val = "";
			for (Iterator detailIt = order.getOimOrderDetailses().iterator(); detailIt
					.hasNext();) {
				OimOrderDetails detail = (OimOrderDetails) detailIt.next();
				if (!detail.getOimSuppliers().getSupplierId()
						.equals(ovs.getOimSuppliers().getSupplierId()))
					continue;
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
						val += "<" + map.getMappedFieldName() + "><![CDATA["
								+ StringHandle.removeNull(fieldValue) + "]]></"
								+ map.getMappedFieldName() + ">\n";
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
			String action = ovs.getTestMode() == 1 ? "TEST" : "PROCESS";
			String xmlRequest = "<orderxml><AccessRequest>\n" + "<XMLlickey>"
					+ lincenceKey + "</XMLlickey>\n" + "<UserId>" + USERID
					+ "</UserId>\n" + "<Password>" + PASSWORD + "</Password>\n"
					+ "<Version>1.1</Version>\n" + "<xml_action>" + action
					+ "</xml_action>\n" + "</AccessRequest>" + val
					+ "</items></orderxml>";
			String xmlResponse = null;
			try {
				xmlResponse = postRequest(xmlRequest, r, "cart");

				// Output the response
				JAXBContext jaxbContext;
				jaxbContext = JAXBContext.newInstance(OrderXMLresp.class);
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				Marshaller marshaller = jaxbContext.createMarshaller();
				Object unmarshal = unmarshaller.unmarshal(new StringReader(
						xmlResponse));
				Set<String> failedStatus = new HashSet<String>();
				failedStatus.add("Not Available");
				failedStatus.add("Invalid Part Number");
				if (unmarshal instanceof OrderXMLresp) {
					OrderXMLresp orderXMLresp = (OrderXMLresp) unmarshal;
					if (orderXMLresp.getOrder() != null) {
						log.info("Recieved order submit reponse for Order# {}",
								orderXMLresp.getOrder().getId());
						for (Item item : orderXMLresp.getItems().getItem()) {
							String itemId = item.getItemId();
							String status = item.getAction();
							for (Iterator detailIt = order
									.getOimOrderDetailses().iterator(); detailIt
									.hasNext();) {
								OimOrderDetails detail = (OimOrderDetails) detailIt
										.next();
								if (detail.getSku().contains(itemId)) {
									detail.setSupplierOrderStatus(status);
									detail.setSupplierOrderNumber(String
											.valueOf(orderXMLresp.getOrder()
													.getProcessing()
													.getInvNum()));
									if (failedStatus.contains(status)) {
										failedOrders.add(detail.getDetailId());
									} else {
										successfulOrders.add(detail
												.getDetailId());
									}
									Session session = SessionManager
											.currentSession();
									session.update(detail);

									OimChannels oimChannels = order
											.getOimOrderBatches()
											.getOimChannels();
									Integer channelId = oimChannels
											.getChannelId();
									IOrderImport iOrderImport = OrderImportManager
											.getIOrderImport(channelId);
									if (iOrderImport != null) {
										log.debug("Created the iorderimport object");
										if (!iOrderImport.init(channelId,
												session)) {
											log.debug(
													"Failed initializing the channel with Id:{}",
													channelId);
										} else {
											salesmachine.oim.suppliers.modal.OrderStatus orderStatus = new salesmachine.oim.suppliers.modal.OrderStatus();
											orderStatus
													.setStatus(((OimOrderProcessingRule) oimChannels
															.getOimOrderProcessingRules()
															.iterator().next())
															.getProcessedStatus());
											iOrderImport.updateStoreOrder(
													detail, orderStatus);
										}
									} else {
										log.error("Could not find a bean to work with this Channel.");
									}
								}
							}

						}
					} else if (orderXMLresp.getErrorResponse() != null) {

						for (Iterator detailIt = order.getOimOrderDetailses()
								.iterator(); detailIt.hasNext();) {
							OimOrderDetails detail = (OimOrderDetails) detailIt
									.next();
							detail.setSupplierOrderStatus(orderXMLresp
									.getErrorResponse().getMSG().get(0));
							failedOrders.add(detail.getDetailId());
						}
						updateVendorSupplierOrderHistory(vendorId,
								ovs.getOimSuppliers(), orderXMLresp
										.getErrorResponse().getMSG().get(0),
								ERROR_ORDER_PROCESSING);
					}
				} else {
					for (Iterator detailIt = order.getOimOrderDetailses()
							.iterator(); detailIt.hasNext();) {
						OimOrderDetails detail = (OimOrderDetails) detailIt
								.next();
						failedOrders.add(detail.getDetailId());
					}
					updateVendorSupplierOrderHistory(vendorId,
							ovs.getOimSuppliers(), xmlResponse.toString(),
							ERROR_PING_FAILURE);
				}
			} catch (RuntimeException e) {
				log.error(e.getMessage(), e);
			} catch (JAXBException e) {
				log.error(e.getMessage(), e);
				throw new SupplierCommunicationException(
						"Response could not be parsed.");
			}
			// Send Email Notifications if is set to true.
			if (order.getOimOrderBatches().getOimChannels()
					.getEmailNotifications() == 1) {
				emailNotification = true;
				String orderStatus = (xmlResponse != null && xmlResponse
						.indexOf("<xml_action>" + action + "</xml_action>") != -1) == true ? "Successfully Places"
						: "Failed to place order";
				emailContent += "<b>Store Order ID " + order.getStoreOrderId()
						+ "</b> -> " + orderStatus + " ";
				emailContent += "<br>";
			}

			String logEmailContent = "";
			if (xmlResponse != null) {
				log.info("STORE ORDER ID : " + order.getStoreOrderId());
				log.info("ORDER PLACED : "
						+ ((xmlResponse
								.indexOf("<xml_action>PROCESS</xml_action>") != -1) == true ? "Yes"
								: "No"));
				log.info("GET RETURN VALUE : " + xmlResponse);
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
					"orders@inventorysource.com",
					"support@inventorysource.com",
					"",
					"Logs of order processing for order : "
							+ order.getStoreOrderId(), logEmailContent);
		}// END for(int i=0;i<orders.size();i++) {
		if (emailNotification) {
			emailContent += "<br>Thanks, <br>Inventory Source Team<br>";
			EmailUtil.sendEmail(r.getLogin(), "support@inventorysource.com",
					"", "Order Processing Results", emailContent, "text/html");
		}
	}

	private String postRequest(String request, Reps r, String service)
			throws SupplierConfigurationException, SupplierOrderException,
			SupplierCommunicationException {
		URL url;
		HttpsURLConnection connection = null;
		String response = "";
		try {
			if (StringHandle.isNullOrEmpty(service)) {
				service = "cart";
			}
			// Create connection
			url = new URL("https://www.bnfusa.com/xml_xchange/" + service
					+ "/post.lasso");
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
			log.info("Sending request with data{}", request);
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
			log.info("Response: {}", response);
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
			String logEmailContent = "-------------- Order failed with Exception -------------\n";
			logEmailContent += "-------------- XML SOAP REQUEST SENT -------------\n";
			logEmailContent += request + "\n";
			logEmailContent += "--------------------------------------------------";
			logEmailContent += "-------------- XML SOAP RESPONSE CAME -------------\n";
			logEmailContent += e + "\n";
			logEmailContent += "--------------------------------------------------";

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
		return response;
	}

	@Override
	public salesmachine.oim.suppliers.modal.OrderStatus getOrderStatus(
			OimVendorSuppliers oimVendorSuppliers, Object trackingMeta)  throws SupplierOrderTrackingException{
		salesmachine.oim.suppliers.modal.OrderStatus orderStatus = new salesmachine.oim.suppliers.modal.OrderStatus();
		if (!(trackingMeta instanceof String))
			throw new IllegalArgumentException(
					"trackingMeta is expected to be a String value containing PO number.");
		Session session = SessionManager.currentSession();
		r = (Reps) session
				.createCriteria(Reps.class)
				.add(Restrictions.eq("vendorId", oimVendorSuppliers
						.getVendors().getVendorId())).uniqueResult();

		StatusXML requestObject = new StatusXML();
		StatusResponse statusResponse;
		AccessRequest accessRequest = new AccessRequest();
		accessRequest.setUserId(oimVendorSuppliers.getLogin());
		accessRequest.setPassword(oimVendorSuppliers.getPassword());
		accessRequest.setXMLlickey(oimVendorSuppliers.getAccountNumber());
		accessRequest.setVersion(1.1F);
		requestObject.setAccessRequest(accessRequest);
		Status status = new Status();
		status.setWebId(trackingMeta.toString());
		requestObject.setStatus(status);
		String trackResponseData, trackRequestData, statusResponseData, statusRequestData;
		try {
			JAXBContext jaxbContext;
			jaxbContext = JAXBContext.newInstance(StatusXML.class,
					StatusResponse.class, Trackxml.class, Trackxmlresp.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			Marshaller marshaller = jaxbContext.createMarshaller();
			OutputStream os2 = new ByteArrayOutputStream();
			marshaller.marshal(requestObject, os2);
			statusRequestData = os2.toString();
			statusResponseData = postRequest(statusRequestData, r, "stat");
			Trackxmlresp trackResponse;
			Object unmarshal2 = unmarshaller.unmarshal(new StringReader(
					statusResponseData));
			String supplierStatus;
			if (unmarshal2 instanceof StatusResponse) {
				statusResponse = (StatusResponse) unmarshal2;
				if (statusResponse.getErrorResponse() != null) {
					supplierStatus = statusResponse.getErrorResponse().getMSG();
				} else
					supplierStatus = statusResponse.getStatusInfo().getOrder()
							.getStatus();
				switch (supplierStatus) {
				case "Pending":
				case "In-Progress":
				default:
					orderStatus.setStatus(supplierStatus);
					break;
				case "Shipped":
					orderStatus.setStatus(supplierStatus);
					Trackxml trackRequest = new Trackxml();
					salesmachine.oim.suppliers.modal.bf.Trackxml.AccessRequest value = new salesmachine.oim.suppliers.modal.bf.Trackxml.AccessRequest();
					value.setUserId(oimVendorSuppliers.getLogin());
					value.setPassword(oimVendorSuppliers.getPassword());
					value.setXMLlickey(oimVendorSuppliers.getAccountNumber());
					value.setVersion(1.1F);
					trackRequest.setAccessRequest(value);

					Tracking tracking = new Tracking();
					tracking.setUserPo(statusResponse.getStatusInfo()
							.getOrder().getUserPo());
					trackRequest.setTracking(tracking);

					OutputStream os = new ByteArrayOutputStream();
					marshaller.marshal(trackRequest, os);
					trackRequestData = os.toString();
					trackResponseData = postRequest(trackRequestData, r,
							"track");
					Object unmarshal = unmarshaller.unmarshal(new StringReader(
							trackResponseData));
					if (unmarshal instanceof Trackxmlresp) {
						trackResponse = (Trackxmlresp) unmarshal;
						TrackingData trackingData;
						if (trackResponse.getTrackingInfo() != null) {
							trackingData = new TrackingData();
							trackingData.setCarrierCode(trackResponse
									.getTrackingInfo().getCarrier());
							trackingData.setCarrierName(trackResponse
									.getTrackingInfo().getCarrier());
							trackingData.setShipperTrackingNumber(trackResponse
									.getTrackingInfo().getTrackingNumber());
							trackingData.setShippingMethod(trackResponse
									.getTrackingInfo().getService());
							String shipDate = trackResponse.getTrackingInfo()
									.getShipDate();// YYYYMMDD

							int year = Integer.parseInt(shipDate
									.substring(0, 4));
							int month = Integer.parseInt(shipDate.substring(4,
									6));
							int dayOfMonth = Integer.parseInt(shipDate
									.substring(6, 8));
							GregorianCalendar cal = new GregorianCalendar(year,
									month, dayOfMonth);
							trackingData.setShipDate(cal);
							orderStatus.addTrackingData(trackingData);
						} else
							orderStatus
									.setStatus("No tracking info available.");
						break;
					}

				}
			}
				if(orderStatus.getStatus()==null ){
					throw new SupplierOrderTrackingException("Error in getting order status from Supplier while tracking Tracking Id- "+trackingMeta);
				}
			if(orderStatus.getTrackingData()==null)
				throw new SupplierOrderTrackingException("Error in getting tracking details from Supplier while tracking Tracking Id- "+trackingMeta);
				
		} catch (JAXBException | SupplierConfigurationException
				| SupplierOrderException | SupplierCommunicationException e) {
			log.error(e.getMessage(), e);
		}
		return orderStatus;
	}
}
