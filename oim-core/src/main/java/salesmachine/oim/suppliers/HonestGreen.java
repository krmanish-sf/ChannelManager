package salesmachine.oim.suppliers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderProcessingRule;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.Reps;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.impl.OrderImportManager;
import salesmachine.oim.suppliers.exception.InvalidAddressException;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.oim.suppliers.modal.hg.TrackingData;
import salesmachine.util.OimLogStream;
import salesmachine.util.StringHandle;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;

public class HonestGreen extends Supplier implements HasTracking {
	private static final String UNFIORDERNO = "UNFIORDERNO";
	private static final String PONUM = "PONUM";
	private static final String ASCII = "ASCII";
	private static final Logger log = LoggerFactory
			.getLogger(HonestGreen.class);
	// private static final byte BLANK_SPACE = ' ';
	private static final byte[] NEW_LINE = new byte[] { '\r', '\n' };
	private static final byte[] HG_EOF = new byte[] { '*', '*', '*', 'E', 'O',
			'F', '*', '*', '*' };
	private static final byte[] FIL = new byte[] { 'F', 'I', 'L' };
	private static final byte[] COMMA = new byte[] { ',' };
	private static JAXBContext jaxbContext;

	static {
		try {
			jaxbContext = JAXBContext.newInstance(TrackingData.class);
		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void sendOrders(Integer vendorId, OimVendorSuppliers ovs, List orders) {
		log.info("Sending orders of Account: {}", ovs.getAccountNumber());
		if (ovs.getTestMode().equals(1))
			return;
		// populate orderSkuPrefixMap with channel id and the prefix to be used
		// for the given supplier.
		orderSkuPrefixMap = setSkuPrefixForOrders(ovs);
		Session session = SessionManager.currentSession();
		Reps r = (Reps) session.createCriteria(Reps.class)
				.add(Restrictions.eq("vendorId", vendorId)).uniqueResult();
		Vendors v = new Vendors();
		v.setVendorId(r.getVendorId());
		String name = StringHandle.removeNull(r.getFirstName()) + " "
				+ StringHandle.removeNull(r.getLastName());
		String emailContent = "Dear " + name + "<br>";
		emailContent += "<br>Following is the status of the orders file uploaded on FTP for the supplier "
				+ ovs.getOimSuppliers().getSupplierName() + " : - <br>";

		String accountNumber = ovs.getAccountNumber();

		for (Object object : orders) {
			boolean emailNotification = false;

			if (object instanceof OimOrders) {
				OimOrders order = (OimOrders) object;
				try {
					String fileName = createOrderFile(order, ovs);
					FTPClient ftp = new FTPClient();
					ftp.setRemoteHost("ftp1.unfi.com");
					ftp.setDetectTransferMode(true);
					ftp.connect();
					ftp.login(ovs.getLogin(), ovs.getPassword());
					ftp.setTimeout(60 * 1000 * 60 * 5);
					ftp.put(fileName, fileName);
					ftp.quit();

					String emailBody = "Account Number : "
							+ accountNumber
							+ "\n Find attached order file for the orders from my store.";
					String emailSubject = fileName;
					EmailUtil.sendEmailWithAttachment(
							"orders@inventorysource.com",
							"support@inventorysource.com", r.getLogin(),
							emailSubject, emailBody, fileName);

					if (order.getOimOrderBatches().getOimChannels()
							.getEmailNotifications() == 1) {
						emailNotification = true;
						String orderStatus = "Successfully Placed";
						emailContent += "<b>Store Order ID "
								+ order.getStoreOrderId() + "</b> -> "
								+ orderStatus + " ";
						emailContent += "<br>";
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				if (emailNotification) {
					emailContent += "<br>Thanks, <br>Inventorysource support<br>";
					logStream
							.println("Sending email to user about order processing");
					log.debug("Sending email to user about order processing");
					EmailUtil.sendEmail(r.getLogin(),
							"support@inventorysource.com", "",
							"Order processing update results", emailContent,
							"text/html");
				}
			}
		}
	}

	private Map<String, String> parseOrderConfirmation(
			Map<Integer, String> orderConfirmationMap) {
		String[] lineArray1 = orderConfirmationMap.get(1).split(",");
		Map<String, String> orderData = new HashMap<String, String>();
		orderData.put(PONUM, lineArray1[6]);
		orderData.put(UNFIORDERNO, lineArray1[0]);
		return orderData;
	}

	/**
	 * Parses and returns the file data in a Map with line number as keys.
	 * 
	 * @param confirmationFileData
	 * @return file data in a Map with line number as keys.
	 */
	private Map<Integer, String> parseFileData(byte[] confirmationFileData) {
		Map<Integer, String> fileData = null;
		try {
			fileData = new HashMap<Integer, String>();
			InputStream inputStream = new ByteArrayInputStream(
					confirmationFileData);
			Reader r = new InputStreamReader(inputStream, ASCII);

			int i = -1, lineNum = 1;
			char c;
			StringBuilder sb = new StringBuilder();
			while ((i = r.read()) != -1) {
				c = (char) i;
				if (c == '"')
					continue;
				if (c != '\n') {
					sb.append(c);
				} else {
					fileData.put(lineNum, sb.toString());
					lineNum++;
					sb = new StringBuilder();
				}
			}
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return fileData;
	}

	private String createOrderFile(OimOrders order, OimVendorSuppliers ovs) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
		String uploadfilename = "HG_" + ovs.getAccountNumber() + "_"
				+ sdf.format(new Date()) + ".txt";
		File f = new File(uploadfilename);
		log.info("created file name for HG:{}",f.getName());
		log.debug("Creating order file for OrderId:{}", order.getOrderId());
		try {
			FileOutputStream fOut = new FileOutputStream(f);
			// Integer orderSize = order.getOimOrderDetailses().size();
			fOut.write("1".getBytes(ASCII));
			fOut.write(NEW_LINE);
			fOut.write(ovs.getAccountNumber().getBytes(ASCII));
			fOut.write(COMMA);
			// fOut.write(BLANK_SPACE);
			fOut.write(COMMA);
			String poNum = ovs.getVendors().getVendorId() + "-"
					+ order.getStoreOrderId();
			fOut.write(poNum.getBytes(ASCII));
			fOut.write(COMMA);
			// fOut.write(BLANK_SPACE);
			fOut.write(COMMA);
			fOut.write(FIL);
			fOut.write(COMMA);
			// fOut.write(BLANK_SPACE);
			fOut.write(NEW_LINE);
			fOut.write(StringHandle.removeNull(order.getDeliveryName())
					.toUpperCase().getBytes(ASCII));
			fOut.write(COMMA);
			fOut.write(StringHandle
					.removeNull(order.getDeliveryStreetAddress()).toUpperCase()
					.getBytes(ASCII));
			fOut.write(COMMA);
			fOut.write(StringHandle.removeNull(order.getDeliverySuburb())
					.toUpperCase().getBytes(ASCII));
			fOut.write(COMMA);
			fOut.write(StringHandle.removeNull(order.getDeliveryCity())
					.toUpperCase().getBytes(ASCII));
			fOut.write(COMMA);
			fOut.write(StringHandle.removeNull(order.getDeliveryStateCode())
					.toUpperCase().getBytes(ASCII));
			fOut.write(COMMA);
			fOut.write(StringHandle.removeNull(order.getDeliveryZip())
					.toUpperCase().getBytes(ASCII));
			fOut.write(COMMA);
			fOut.write(StringHandle.removeNull(order.getDeliveryPhone())
					.toUpperCase().getBytes(ASCII));
			fOut.write(COMMA);
			fOut.write('A');
			fOut.write(COMMA);
			fOut.write("5001".getBytes(ASCII));
			fOut.write(NEW_LINE);
			for (OimOrderDetails od : ((Set<OimOrderDetails>) order
					.getOimOrderDetailses())) {
				if (!od.getOimSuppliers().getSupplierId()
						.equals(ovs.getOimSuppliers().getSupplierId()))
					continue;
				String skuPrefix = null, sku = od.getSku();
				if (!orderSkuPrefixMap.isEmpty()) {
					skuPrefix = orderSkuPrefixMap.values().toArray()[0]
							.toString();
				}
				skuPrefix = StringHandle.removeNull(skuPrefix);
				if (sku.startsWith(skuPrefix)) {
					sku = sku.substring(skuPrefix.length());
				}
				fOut.write(sku.getBytes(ASCII));
				fOut.write(COMMA);
				// fOut.write(BLANK_SPACE);
				fOut.write(COMMA);
				fOut.write(od.getQuantity().toString().getBytes(ASCII));
				fOut.write(COMMA);
				// fOut.write(BLANK_SPACE);
				fOut.write(COMMA);
				// fOut.write(BLANK_SPACE);
				fOut.write(COMMA);
				// fOut.write(BLANK_SPACE);
				// fOut.write(COMMA);
				fOut.write(NEW_LINE);
				od.setSupplierOrderNumber(poNum);
				od.setSupplierOrderStatus("Sent to supplier.");
				Session session = SessionManager.currentSession();
				session.update(od);
				successfulOrders.add(od.getDetailId());
				OimChannels oimChannels = order.getOimOrderBatches()
						.getOimChannels();
				Integer channelId = oimChannels.getChannelId();
				IOrderImport iOrderImport = OrderImportManager
						.getIOrderImport(channelId);
				OimLogStream stream = new OimLogStream();
				if (iOrderImport != null) {
					log.debug("Created the iorderimport object");
					if (!iOrderImport.init(channelId,
							SessionManager.currentSession(), stream)) {
						log.debug("Failed initializing the channel with Id:{}",
								channelId);
					} else {
						OrderStatus orderStatus = new OrderStatus();
						orderStatus
								.setStatus(((OimOrderProcessingRule) oimChannels
										.getOimOrderProcessingRules()
										.iterator().next())
										.getProcessedStatus());
						iOrderImport.updateStoreOrder(od, orderStatus);
					}
				} else {
					log.error("Could not find a bean to work with this Channel.");
					stream.println("This Channel type is not supported for pushing order updates.");
				}
			}
			fOut.write(HG_EOF);
			fOut.write(NEW_LINE);
			fOut.flush();
			fOut.close();
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return uploadfilename;
	}

	private static final String ORDER_CONFIRMATION_FILE_PATH_TEMPLATE = "confirmations/%s.O%sA.txt";
	private static final String ORDER_SHIPPING_FILE_PATH_TEMPLATE = "shipping/%s.S%sA.txt";
	private static final String ORDER_TRACKING_FILE_PATH_TEMPLATE = "%s.T%sX.txt";

	private String getShippingFilePath(String account, String unfiOrderNo) {
		return getFilePath(ORDER_SHIPPING_FILE_PATH_TEMPLATE, account,
				unfiOrderNo);
	}

	private String getConfirmationFilePath(String account, String unfiOrderNo) {
		return getFilePath(ORDER_CONFIRMATION_FILE_PATH_TEMPLATE, account,
				unfiOrderNo);
	}

	private String getTrackingFilePath(String account, String unfiOrderNo) {
		return getFilePath(ORDER_TRACKING_FILE_PATH_TEMPLATE, account,
				unfiOrderNo);
	}

	private String getFilePath(String template, String account,
			String unfiOrderNo) {
		return String.format(template, account, unfiOrderNo);
	}

	@Override
	public OrderStatus getOrderStatus(OimVendorSuppliers ovs,
			Object trackingMeta) {
		log.info("Tracking request for PONUM: {}", trackingMeta);
		if (!(trackingMeta instanceof String))
			throw new IllegalArgumentException(
					"trackingMeta is expected to be a String value containing UNFI Order number.");
		OrderStatus orderStatus = new OrderStatus();
		try {
			FTPClient ftp = new FTPClient();
			ftp.setRemoteHost("ftp1.unfi.com");
			ftp.setDetectTransferMode(true);
			ftp.connect();
			ftp.login(ovs.getLogin(), ovs.getPassword());
			ftp.setTimeout(60 * 1000 * 60 * 5);
			ftp.chdir("confirmations");
			for (String confirmationFile : ftp.dir()) {
				byte[] confirmationFileData = ftp.get(confirmationFile);
				Map<Integer, String> orderDataMap = parseFileData(confirmationFileData);
				Map<String, String> orderData = parseOrderConfirmation(orderDataMap);
				log.info("Order Confirmation details found for {}", orderData);
				if (trackingMeta.toString().equals(orderData.get(PONUM))) {
					log.debug("Order Invoice Data Found");
					String unfiOrderNo = orderData.get(UNFIORDERNO);
					// String shippingFilePath = getShippingFilePath(
					// ovs.getAccountNumber(), unfiOrderNo);
					ftp.cdup();
					ftp.chdir("tracking");
					String trackingFilePath = getTrackingFilePath(
							ovs.getAccountNumber(), unfiOrderNo);
					orderStatus.setStatus("In-Process");
					// byte[] shippingFileData = ftp.get(shippingFilePath);
					// Map<Integer, String> shippingDataMap =
					// parseFileData(shippingFileData);
					try {
						byte[] trackingFileData = ftp.get(trackingFilePath);
						Unmarshaller unmarshaller = jaxbContext
								.createUnmarshaller();
						String s = new String(trackingFileData);
						StringReader reader = new StringReader(s);
						log.info(s);
						TrackingData orderTrackingResponse = (TrackingData) unmarshaller
								.unmarshal(reader);
						orderStatus.setStatus("Shipped");
						salesmachine.oim.suppliers.modal.TrackingData trackingData = new salesmachine.oim.suppliers.modal.TrackingData();
						if ("A".equals(orderTrackingResponse.getPO()
								.getShipVia())) {
							trackingData.setCarrierCode("UPS");
							trackingData.setCarrierName("UPS");
							trackingData.setShippingMethod("Ground");

						} else {
							trackingData.setCarrierCode(orderTrackingResponse
									.getPO().getShipVia());
						}
						trackingData
								.setShipperTrackingNumber(orderTrackingResponse
										.getPOTracking().getTrackingNumber());
						orderStatus.setTrackingData(trackingData);
						log.info("Tracking details: {} {}",
								orderTrackingResponse.getPO().getShipVia(),
								orderTrackingResponse.getPOTracking()
										.getTrackingNumber());

					} catch (JAXBException e) {
						log.error(
								"Tracking XML recieved from server could not be parsed.",
								e);
					} catch (FTPException e) {
						log.error(e.getMessage());
						orderStatus.setStatus("Not shipped yet.");
					} finally {
						ftp.cdup();
						ftp.chdir("confirmations");
					}
					break;
				}
				orderStatus.setStatus("Not shipped yet.");
			}
			// byte[] confirmationFileData = ftp.get(getConfirmationFilePath(
			// ovs.getAccountNumber(), trackingMeta.toString()));
			// Map<Integer, String> confirmationDataMap =
			// parseFileData(confirmationFileData);
			// Map<String, String> orderConfirmationMap =
			// parseOrderConfirmation(confirmationDataMap);
			// log.info("Confirmation File Data Map: {}", confirmationDataMap);
			// log.info("Order Confirmation Data: {}", orderConfirmationMap);
			ftp.quit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return orderStatus;
	}

	public static void main(String[] args) {
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(TrackingData.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			File f = new File("/tmp/37984.T19559757X.txt");
			TrackingData orderTrackingResponse = (TrackingData) unmarshaller
					.unmarshal(f);
			String trackingInfo = orderTrackingResponse.getPO().getShipVia()
					+ ":"
					+ orderTrackingResponse.getPOTracking().getTrackingNumber();
			log.info("Tracking:{}", trackingInfo);

		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
		}
	}
}
