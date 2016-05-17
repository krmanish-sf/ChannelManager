package salesmachine.oim.suppliers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderProcessingRule;
import salesmachine.hibernatedb.OimOrderStatuses;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSupplierMethodattrValues;
import salesmachine.hibernatedb.OimSupplierMethods;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.Reps;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.stores.impl.ChannelFactory;
import salesmachine.oim.suppliers.exception.SupplierCommunicationException;
import salesmachine.oim.suppliers.exception.SupplierConfigurationException;
import salesmachine.oim.suppliers.exception.SupplierOrderException;
import salesmachine.oim.suppliers.exception.SupplierOrderTrackingException;
import salesmachine.oim.suppliers.modal.OrderDetailResponse;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.util.FtpDetail;
import salesmachine.util.OimLogStream;
import salesmachine.util.StringHandle;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;
import com.enterprisedt.net.ftp.FTPTransferType;

public class BakerTayler extends Supplier implements HasTracking {

	private static final Logger log = LoggerFactory.getLogger(BakerTayler.class);
	private static final String ASCII = "ASCII";
	private static final byte[] FIRST_LINE = "000000;;ORDENT\n".getBytes();
	private static final byte[] NEW_LINE = new byte[] { '\n' };
	private static final byte[] SLASH = new byte[] { '/' };
	private static final byte[] SEMICOLON = new byte[] { ';' };
	private static final String[] BT_ACKNOWLEDGEMENT_HEADER = { "ORDER", "ENTDTE", "STATIM", "STS", "ORDQTY", "BKOQTY", "INVQTY", "SHPPNT", "TITLE",
			"ITEM", "BT_ORD", "RETAIL", "SHPVIA", "RELDTE", "UPC", "CUSLIN", "TRACKING NUMBER" };
	private static final String[] BT_SHIPMENT_HEADER = { "CUSNO", "CUSNAM", "ORDER_NO", "INV_NO", "ITEM", "TITLE", "INV_QTY", "SLS_AMT", "SHP_TYPE",
			"SHPVIA", "TRACKNO", "INVDTE", "CUSLIN" };
	private static final Map<String, String> BTORDERSTATUSCODES;
	static {
		BTORDERSTATUSCODES = new HashMap<String, String>();
		BTORDERSTATUSCODES.put("BO", "Backorder");
		BTORDERSTATUSCODES.put("CA", "Canceled");
		BTORDERSTATUSCODES.put("IN", "Invoiced");
		BTORDERSTATUSCODES.put("LS", "Lost sale/not available");
		BTORDERSTATUSCODES.put("NA", "Not available");
		BTORDERSTATUSCODES.put("OC", "Order created");
		BTORDERSTATUSCODES.put("PR", "Prebook");
		BTORDERSTATUSCODES.put("RE", "Return");
		BTORDERSTATUSCODES.put("SO", "Stock out — will convert to BO");
		BTORDERSTATUSCODES.put("60", "Automatically canceled");
	}

	@Override
	public OrderStatus getOrderStatus(OimVendorSuppliers ovs, Object trackingMeta, OimOrderDetails oimOrderDetails)
			throws SupplierOrderTrackingException {
		orderSkuPrefixMap = setSkuPrefixForOrders(ovs);
		if (!(trackingMeta instanceof String))
			throw new IllegalArgumentException("trackingMeta is expected to be a String value containing PONumber.");
		OrderStatus orderStatus = new OrderStatus();
		orderStatus.setStatus(oimOrderDetails.getSupplierOrderStatus());
		String skuPrefix = null;
		String sku = oimOrderDetails.getSku();
		if (!orderSkuPrefixMap.isEmpty()) {
			int channelId = oimOrderDetails.getOimOrders().getOimOrderBatches().getOimChannels().getChannelId();
			skuPrefix = orderSkuPrefixMap.get(channelId);
		}
		skuPrefix = StringHandle.removeNull(skuPrefix);
		if (sku.startsWith(skuPrefix)) {
			sku = sku.substring(skuPrefix.length());
		}

		String poNumber = (String) trackingMeta; // 641958-100
		FtpDetail ftpDetail = getFtpDetails(ovs);
		if (ftpDetail.getUrl() != null) {
			orderStatus = getOrderStatusFromFTP(orderStatus, ftpDetail, oimOrderDetails, poNumber, sku);
		}
		return orderStatus;
	}

	private static Map<Integer, String> parseFileData(byte[] trackingFileData) {
		Map<Integer, String> fileData = null;
		try {
			fileData = new HashMap<Integer, String>();
			InputStream inputStream = new ByteArrayInputStream(trackingFileData);
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				fileData.put(i++, line);
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return fileData;
	}

	private Map<String, String> getLineItemAck(Map<Integer, String> ackDataMap, String poNumber, Integer detailId) {
		for (Iterator<String> iterator = ackDataMap.values().iterator(); iterator.hasNext();) {
			String ackLine = iterator.next();
			if (ackLine.startsWith(poNumber)) {
				Map<String, String> itemAck = parseLine(ackLine, BT_ACKNOWLEDGEMENT_HEADER);
				if (itemAck.get("CUSLIN").equals(String.valueOf(detailId))) {
					return itemAck;
				}
			}
		}
		return null;
	}

	private Map<String, String> getLineItemShip(Map<Integer, String> ackDataMap, String poNumber, Integer detailId) {
		for (Iterator<String> iterator = ackDataMap.values().iterator(); iterator.hasNext();) {
			String ackLine = iterator.next();
			Map<String, String> itemShip = parseLine(ackLine, BT_SHIPMENT_HEADER);
			if (itemShip.get("ORDER_NO").equals(String.valueOf(poNumber)) && itemShip.get("CUSLIN").equals(String.valueOf(detailId))) {
				return itemShip;
			}
		}
		return null;
	}

	private Map<String, String> parseLine(String line, String[] header) {
		Map<String, String> hash = new HashMap<String, String>();
		String[] lineTerms = line.split(",");
		for (int i = 0; i < lineTerms.length; i++) {
			hash.put(header[i], lineTerms[i].trim());
		}
		return hash;
	}

	public static void main(String[] args) throws Exception {
		FTPClient ftp = new FTPClient();
		ftp.setRemoteHost("ftp.baker-taylor.com");
		ftp.setDetectTransferMode(true);
		ftp.connect();
		ftp.login("11801890", "11801890");
		ftp.setTimeout(60 * 1000 * 60 * 7);
		FTPFile[] ftpFiles = ftp.dirDetails("/11801890/11801890.out/");
		List<FTPFile> ackFiles = new ArrayList<FTPFile>();
		List<FTPFile> shipFiles = new ArrayList<FTPFile>();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 7);
		Date boundaryDate = cal.getTime();
		for (FTPFile ftpFile : ftpFiles) {
			System.out.println(ftpFile.getName());
			System.out.println(ftpFile.lastModified());
			if (ftpFile.lastModified().compareTo(boundaryDate) > 0) {
				String trackingFile = ftpFile.getName();
				if (trackingFile.contains("ibuyrite" + "_ack_") && !trackingFile.contains("_since")) {
					ackFiles.add(ftpFile);
				} else if (trackingFile.contains("ibuyrite" + "_ship_")) {
					shipFiles.add(ftpFile);
				}
			}
		}
		Comparator<FTPFile> comp = new Comparator<FTPFile>() {
			public int compare(FTPFile f1, FTPFile f2) {
				return f2.lastModified().compareTo(f1.lastModified());
			}
		};
		Collections.sort(ackFiles, comp);
		Collections.sort(shipFiles, comp);
	}

	private OrderStatus getOrderStatusFromFTP(OrderStatus orderStatus, FtpDetail ftpDetail, OimOrderDetails oimOrderDetails, String poNumber,
			String sku) throws SupplierOrderTrackingException {
		FTPClient ftp = new FTPClient();
		try {
			ftp.setRemoteHost(ftpDetail.getUrl());
			ftp.setDetectTransferMode(true);
			ftp.connect();
			ftp.login(ftpDetail.getUserName(), ftpDetail.getPassword());
			ftp.setTimeout(60 * 1000 * 60 * 7);
			FTPFile[] ftpFiles = ftp.dirDetails("/" + ftpDetail.getAccountNumber() + "/" + ftpDetail.getAccountNumber() + ".out/");
			List<FTPFile> ackFiles = new ArrayList<FTPFile>();
			List<FTPFile> shipFiles = new ArrayList<FTPFile>();
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 7);
			Date boundaryDate = cal.getTime();
			for (FTPFile ftpFile : ftpFiles) {
				if (ftpFile.lastModified().compareTo(boundaryDate) > 0) {
					String trackingFile = ftpFile.getName();
					if (trackingFile.contains(ftpDetail.getAccountName() + "_ack_") && !trackingFile.contains("_since")) {
						ackFiles.add(ftpFile);
					} else if (trackingFile.contains(ftpDetail.getAccountName() + "_ship_")) {
						shipFiles.add(ftpFile);
					}
				}
			}
			Comparator<FTPFile> comp = new Comparator<FTPFile>() {
				public int compare(FTPFile f1, FTPFile f2) {
					return f2.lastModified().compareTo(f1.lastModified());
				}
			};
			Collections.sort(ackFiles, comp);
			Collections.sort(shipFiles, comp);
			if (orderStatus.getStatus().equalsIgnoreCase(OimConstants.OIM_SUPPLER_ORDER_STATUS_SENT_TO_SUPPLIER)) {
				for (FTPFile ftpFile : ackFiles) {
					byte[] trackingFileData = ftp.get("/" + ftpDetail.getAccountNumber() + "/" + ftpDetail.getAccountNumber() + ".out/"
							+ ftpFile.getName());
					Map<Integer, String> ackDataMap = parseFileData(trackingFileData);
					Map<String, String> itemAckMap = getLineItemAck(ackDataMap, poNumber, oimOrderDetails.getDetailId());
					if (itemAckMap == null) {
						continue;
					}
					String trackingPO = itemAckMap.get("ORDER");
					String trackingSku = itemAckMap.get("ITEM");
					if (!trackingSku.equalsIgnoreCase(sku)) {
						// this condition wont hold true
						log.warn("Improbable case found - detail id - {} matched but not sku {}", oimOrderDetails.getDetailId(),
								oimOrderDetails.getSku());
						continue;
					}
					String shippingMethod = itemAckMap.get("SHPVIA");
					String qtyOrdered = itemAckMap.get("ORDQTY");
					String qtyShipped = itemAckMap.get("INVQTY");
					int qty = 0;
					try {
						qty = Integer.parseInt(qtyShipped);
					} catch (NumberFormatException e) {
						log.error(e.getMessage(), e);
					}
					String trackingNo = itemAckMap.get("TRACKING NUMBER");

					String headerStatus = itemAckMap.get("STS");
					log.info("Status of Order at BakerTayler : {} - {}", headerStatus, BTORDERSTATUSCODES.get(headerStatus));
					orderStatus.setStatus(BTORDERSTATUSCODES.get(headerStatus));

					if (headerStatus.equalsIgnoreCase("OC")) {
						orderStatus.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_IN_PROCESS);
						oimOrderDetails.setOimOrderStatuses(new OimOrderStatuses(OimConstants.ORDER_STATUS_PROCESSED_SUCCESS));
					} else if (headerStatus.equalsIgnoreCase("BO") || headerStatus.equalsIgnoreCase("LS") || headerStatus.equalsIgnoreCase("NA")
							|| headerStatus.equalsIgnoreCase("PR") || headerStatus.equalsIgnoreCase("RE") || headerStatus.equalsIgnoreCase("SO")
							|| headerStatus.equalsIgnoreCase("60")) {
						oimOrderDetails.setOimOrderStatuses(new OimOrderStatuses(OimConstants.ORDER_STATUS_PROCESSED_FAILED));
					} else if (headerStatus.equalsIgnoreCase("CA")) {
						oimOrderDetails.setOimOrderStatuses(new OimOrderStatuses(OimConstants.ORDER_STATUS_CANCELED));
					}
				}
			}
			if (orderStatus.getStatus().equalsIgnoreCase(OimConstants.OIM_SUPPLER_ORDER_STATUS_IN_PROCESS)) {
				for (FTPFile ftpFile : shipFiles) {
					byte[] fileData = ftp.get("/" + ftpDetail.getAccountNumber() + "/" + ftpDetail.getAccountNumber() + ".out/" + ftpFile.getName());
					Map<Integer, String> shipDataMap = parseFileData(fileData);
					Map<String, String> itemShipMap = getLineItemShip(shipDataMap, poNumber, oimOrderDetails.getDetailId());
					if (itemShipMap == null) {
						continue;
					}
					String trackingSku = itemShipMap.get("ITEM");
					if (!trackingSku.equalsIgnoreCase(sku)) {
						// this condition wont hold true
						log.warn("Improbable case found - detail id - {} matched but not sku {}", oimOrderDetails.getDetailId(),
								oimOrderDetails.getSku());
						continue;
					}
					String shippingMethod = itemShipMap.get("SHPVIA");
					String shipType = itemShipMap.get("SHP_TYPE");
					String qtyShipped = itemShipMap.get("INV_QTY");
					int qty = 0;
					try {
						qty = Integer.parseInt(qtyShipped);
					} catch (NumberFormatException e) {
						log.error(e.getMessage(), e);
					}
					String trackingNo = itemShipMap.get("TRACKNO");
					if (!trackingNo.isEmpty()) {
						String invDate = itemShipMap.get("INVDTE");
						SimpleDateFormat sdt = new SimpleDateFormat("YYYYMMDD");
						Date invShipDate = sdt.parse(invDate);
						oimOrderDetails.setOimOrderStatuses(new OimOrderStatuses(OimConstants.ORDER_STATUS_SHIPPED));
						orderStatus.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_SHIPPED);
						salesmachine.oim.suppliers.modal.TrackingData trackingData = new salesmachine.oim.suppliers.modal.TrackingData();
						trackingData.setCarrierCode("DHL");
						trackingData.setCarrierName("DHL");
						trackingData.setShippingMethod("Express");
						trackingData.setQuantity(qty);
						trackingData.setShipperTrackingNumber(trackingNo);
						XMLGregorianCalendar shipDate = null;
						try {
							GregorianCalendar c = new GregorianCalendar();
							c.setTime(invShipDate);
							shipDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
						trackingData.setShipDate(shipDate);
						orderStatus.addTrackingData(trackingData);
					}
				}
			}
		} catch (IOException | FTPException | ParseException e) {
			log.error(e.getMessage(), e);
			throw new SupplierOrderTrackingException("Unable to connect to supplier ftp", e);
		}

		return orderStatus;
	}

	private FtpDetail getFtpDetails(OimVendorSuppliers ovs) {
		FtpDetail ftpDetail = new FtpDetail();
		for (Iterator<OimSupplierMethods> itr = ovs.getOimSuppliers().getOimSupplierMethodses().iterator(); itr.hasNext();) {
			OimSupplierMethods oimSupplierMethods = itr.next();
			if (oimSupplierMethods.getDeleteTm() != null)
				continue;
			if (oimSupplierMethods.getOimSupplierMethodTypes().getMethodTypeId().intValue() == 1) {

				if (oimSupplierMethods.getVendor() != null
						&& oimSupplierMethods.getVendor().getVendorId().intValue() == ovs.getVendors().getVendorId().intValue()) {
					for (Iterator<OimSupplierMethodattrValues> iterator = oimSupplierMethods.getOimSupplierMethodattrValueses().iterator(); iterator
							.hasNext();) {
						OimSupplierMethodattrValues oimSupplierMethodattrValues = iterator.next();

						if (oimSupplierMethodattrValues.getOimSupplierMethodattrNames().getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPACCOUNT)
							ftpDetail.setAccountNumber(oimSupplierMethodattrValues.getAttributeValue());
						if (oimSupplierMethodattrValues.getOimSupplierMethodattrNames().getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPSERVER)
							ftpDetail.setUrl(oimSupplierMethodattrValues.getAttributeValue());
						if (oimSupplierMethodattrValues.getOimSupplierMethodattrNames().getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPLOGIN)
							ftpDetail.setUserName(oimSupplierMethodattrValues.getAttributeValue());
						if (oimSupplierMethodattrValues.getOimSupplierMethodattrNames().getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPPASSWORD)
							ftpDetail.setPassword(oimSupplierMethodattrValues.getAttributeValue());
						if (oimSupplierMethodattrValues.getOimSupplierMethodattrNames().getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPTYPE)
							ftpDetail.setFtpType(oimSupplierMethodattrValues.getAttributeValue());
						if (oimSupplierMethodattrValues.getOimSupplierMethodattrNames().getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_ACCOUNTNAME) {
							ftpDetail.setAccountName(oimSupplierMethodattrValues.getAttributeValue());
						}
					}
					break;
				}
			}
		}
		return ftpDetail;
	}

	@Override
	public void sendOrders(Integer vendorId, OimVendorSuppliers ovs, OimOrders order) throws SupplierConfigurationException,
			SupplierCommunicationException, SupplierOrderException, ChannelConfigurationException, ChannelCommunicationException,
			ChannelOrderFormatException {
		log.info("Sending orders of Account: {}", ovs.getAccountNumber());
		if (ovs.getTestMode().equals(1))
			return;

		Session session = SessionManager.currentSession();
		Query query;
		orderSkuPrefixMap = setSkuPrefixForOrders(ovs);
		Reps r = (Reps) session.createCriteria(Reps.class).add(Restrictions.eq("vendorId", vendorId)).uniqueResult();
		Vendors v = new Vendors();
		v.setVendorId(r.getVendorId());
		String poNum;
		query = session
				.createSQLQuery("select  distinct SUPPLIER_ORDER_NUMBER from kdyer.OIM_ORDER_DETAILS where ORDER_ID=:orderId and SUPPLIER_ID=:supplierId");
		query.setInteger("orderId", order.getOrderId());
		query.setInteger("supplierId", ovs.getOimSuppliers().getSupplierId());
		Object q = null;
		try {
			q = query.uniqueResult();
		} catch (NonUniqueResultException e) {
			log.error("This order has more than one product having different PO number. Please make them same. store order id is - {}",
					order.getStoreOrderId());
			throw new SupplierConfigurationException("This order has more than one product having different PO number. Please make them same.");
		}
		if (q != null) {
			poNum = (String) q;
			log.info("Reprocessing po - {}", poNum);
		} else {
			poNum = ovs.getVendors().getVendorId() + "-" + order.getStoreOrderId();
		}
		try {
			if (order.getOimShippingMethod() == null) {
				log.error("shipping method is missing");
				return;
			}
			String shippingCode = "@71";
			for (Iterator<OimOrderDetails> iterator = order.getOimOrderDetailses().iterator(); iterator.hasNext();) {
				OimOrderDetails oimOrderDetail = iterator.next();
				String sku = oimOrderDetail.getSku();
				boolean isEntertainmentProduct = isEntertainmentProduct(sku);
				if (isEntertainmentProduct) {
					shippingCode = "@72";
					break;
				}
			}
			String fileName = createOrderFile(order, ovs, poNum, shippingCode);
			FtpDetail ftpDetails = getFtpDetails(ovs);
			sendToFTP(fileName, ftpDetails, "/" + ftpDetails.getAccountNumber() + "/" + ftpDetails.getAccountNumber() + ".in/");
			log.info("order file sent to ftp");
			for (OimOrderDetails od : order.getOimOrderDetailses()) {

				successfulOrders.put(od.getDetailId(), new OrderDetailResponse(poNum, OimConstants.OIM_SUPPLER_ORDER_STATUS_SENT_TO_SUPPLIER, null));
			}

		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
		}

	}

	private void sendToFTP(String fileName, FtpDetail ftpDetails, String remoteDir) throws SupplierCommunicationException,
			SupplierConfigurationException {
		FTPClient ftp = new FTPClient();
		File file = new File(fileName);
		try {
			ftp.setRemoteHost(ftpDetails.getUrl());
			ftp.setDetectTransferMode(false);
			ftp.connect();
			ftp.login(ftpDetails.getUserName(), ftpDetails.getPassword());
			ftp.setType(FTPTransferType.ASCII);
			ftp.setTimeout(60 * 1000 * 60 * 5);
			ftp.put(fileName, remoteDir + file.getName());
			/*if (ftp.get(file.getName()) == null) {
				sendErrorReportEmail(fileName, ftpDetails);
			}*/
			ftp.quit();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new SupplierCommunicationException("Could not connect to FTP while sending orderfile to BK - " + e.getMessage(), e);
		} catch (FTPException e) {
			log.error(e.getMessage(), e);
			throw new SupplierConfigurationException("Could not connect to FTP while sending orderfile to BK - " + e.getMessage(), e);
		}
	}

	private void sendErrorReportEmail(String fileName, FtpDetail ftpDetails) {
		EmailUtil.sendEmail("support@inventorysource.com", "support@inventorysource.com", "", "Failed to put order file to " + ftpDetails.getUrl(),
				"Failed to put this order file " + fileName + " to " + ftpDetails.getUrl(), "text/html");
	}

	private String createOrderFile(OimOrders order, OimVendorSuppliers ovs, String poNum, String shippingCode) throws ChannelCommunicationException,
			ChannelOrderFormatException, SupplierOrderException {
		String uploadfilename = "/tmp/" + "BK_" + ovs.getAccountNumber() + "_" + order.getStoreOrderId() + ".done";
		File f = new File(uploadfilename);
		log.info("created file name for Baker Tayler:{}", f.getName());
		/*try {
			order.setDeliveryCountryCode(MotengContryCode.getProperty(order.getDeliveryCountry()));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new SupplierOrderException(e.getMessage(), e);
		}*/
		try (FileOutputStream fOut = new FileOutputStream(f)) {
			// first line
			fOut.write(FIRST_LINE);
			// second line - order header information
			String country = order.getDeliveryCountry().toLowerCase();
			if ((country.length() <= 3 && country.startsWith("us")) || (country.contains("united") && country.contains("state"))) {
			} else {
				log.error("Order can only be processed in United States. Order id is - {}", order.getStoreOrderId());
				EmailUtil.sendEmail("nilesh@inventorysource.com", "support@inventorysource.com", "", "Warning!! Urgent - Different Country found",
						"Country Expected USA, received - " + country);
				throw new ChannelOrderFormatException("Order can only be processed in United States.");
			}
			String headerInfo = "000001;~EDIID=" + ovs.getAccountNumber() + "/EDIORD=Y/BO=N/" + "SHPVIA=" + shippingCode + "/CHGVIA=" + shippingCode
					+ "/EDIADR=Y/PONUM=" + poNum + "/EDIINT=N/SHPCPL=Y/SPLIT=Y/EDICOU=" + order.getDeliveryCountry();
			fOut.write(headerInfo.getBytes());
			fOut.write(NEW_LINE);
			// line 2 consumers name
			fOut.write("000002;EDINAM=".getBytes());
			fOut.write(order.getDeliveryName().getBytes());
			fOut.write(NEW_LINE);
			String deliveryAddress = order.getDeliveryStreetAddress();
			String ediAd1 = "";
			String ediAd2 = "";
			String ediAd3 = "";
			ediAd1 = deliveryAddress;
			if (deliveryAddress.length() <= 30) {
				ediAd1 = deliveryAddress.substring(0, deliveryAddress.length());
			}
			if (deliveryAddress.length() > 30 && deliveryAddress.length() <= 60) {
				ediAd2 = deliveryAddress.substring(30, deliveryAddress.length());
			}
			if (deliveryAddress.length() > 60 && deliveryAddress.length() <= 90) {
				ediAd3 = deliveryAddress.substring(60);
			} 
			if(deliveryAddress.length() > 90){
				throw new SupplierOrderException("Delivery Street address can not be greater than 90 characters.");
			}
			// line 3 street address
			// upto 30 chars only else continue in next EDIAD2
			fOut.write("000003;EDIAD1=".getBytes());
			fOut.write(ediAd1.getBytes());
			fOut.write(NEW_LINE);
			fOut.write("000004;EDIAD2=".getBytes());
			fOut.write(ediAd2.getBytes());
			fOut.write(NEW_LINE);
			fOut.write("000005;EDIAD3=".getBytes());
			fOut.write(ediAd3.getBytes());
			fOut.write(NEW_LINE);
			// line 4 city
			fOut.write("000006;EDICIT=".getBytes());
			fOut.write(order.getDeliveryCity().getBytes());
			fOut.write(NEW_LINE);
			// line 5 state
			fOut.write("000007;EDISTA=".getBytes());
			fOut.write(order.getDeliveryState().getBytes());
			fOut.write(NEW_LINE);
			// line 6 zipcode
			fOut.write("000008;EDIZIP=".getBytes());
			fOut.write(order.getDeliveryZip().getBytes());
			fOut.write(NEW_LINE);
			// line 7 phone
			fOut.write("000009;EDIFON=".getBytes());
			if (order.getDeliveryPhone() != null) {
				fOut.write(order.getDeliveryPhone().getBytes());
			}
			fOut.write(NEW_LINE);
			String supplierDefaultPrefix = ovs.getOimSuppliers().getDefaultSkuPrefix();
			int currentLine = 9;
			for (OimOrderDetails od : order.getOimOrderDetailses()) {
				if (!od.getOimSuppliers().getSupplierId().equals(ovs.getOimSuppliers().getSupplierId()))
					continue;
				String skuPrefix = null, sku = od.getSku();
				if (!orderSkuPrefixMap.isEmpty()) {
					skuPrefix = orderSkuPrefixMap.values().toArray()[0].toString();
				}
				skuPrefix = StringHandle.removeNull(skuPrefix);
				if (sku.startsWith(skuPrefix)) {
					sku = sku.substring(skuPrefix.length());
				}
				String prodSku = supplierDefaultPrefix + sku;
				boolean isEntertainmentProduct = isEntertainmentProduct(prodSku);
				String itemType = isEntertainmentProduct ? "UPC" : "IB";
				fOut.write((String.format("%06d", ++currentLine) + ";" + itemType + "=" + sku).getBytes());
				fOut.write(SLASH);
				fOut.write(("QTY=" + od.getQuantity()).getBytes());
				fOut.write(SLASH);
				fOut.write(("CUSLIN=" + od.getDetailId()).getBytes());
				fOut.write(NEW_LINE);
				OimChannels oimChannels = order.getOimOrderBatches().getOimChannels();
				OimLogStream stream = new OimLogStream();
				try {
					IOrderImport iOrderImport = ChannelFactory.getIOrderImport(oimChannels);
					OrderStatus orderStatus = new OrderStatus();
					orderStatus.setStatus(((OimOrderProcessingRule) oimChannels.getOimOrderProcessingRules().iterator().next()).getProcessedStatus());
					if (oimChannels.getTestMode() == 0)
						iOrderImport.updateStoreOrder(od, orderStatus);
				} catch (ChannelConfigurationException e) {
					log.error(e.getMessage(), e);
					stream.println(e.getMessage());
				}
			}
			fOut.write((String.format("%06d", ++currentLine) + ";GFTMSG=").getBytes());
			fOut.write(NEW_LINE);
			fOut.write(("999999;" + String.format("%06d", currentLine)).getBytes());
			fOut.flush();
			fOut.close();
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return uploadfilename;
	}

	public boolean isEntertainmentProduct(String sku) {
		Session dbSession = SessionManager.currentSession();
		Query query = dbSession.createSQLQuery("select count(*) from PRODUCT where SKU = :sku and UPC is not null");
		query.setParameter("sku", sku);
		Object q = null;
		int count = 0;
		try {
			count = ((BigDecimal) query.uniqueResult()).intValue();
		} catch (NonUniqueResultException e) {
			log.error(e.getMessage(), e);
		}
		return count == 1 ? true : false;
	}
}
