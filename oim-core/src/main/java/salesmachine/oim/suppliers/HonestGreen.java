package salesmachine.oim.suppliers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.axis.encoding.Base64;
import org.apache.axis.utils.ByteArrayOutputStream;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
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
import salesmachine.hibernatedb.Product;
import salesmachine.hibernatedb.Reps;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.stores.impl.OrderImportManager;
import salesmachine.oim.stores.modal.amazon.AmazonEnvelope;
import salesmachine.oim.stores.modal.amazon.AmazonEnvelope.Message;
import salesmachine.oim.stores.modal.amazon.Header;
import salesmachine.oim.stores.modal.amazon.OrderAcknowledgement;
import salesmachine.oim.stores.modal.amazon.OrderFulfillment;
import salesmachine.oim.stores.modal.amazon.OrderFulfillment.FulfillmentData;
import salesmachine.oim.stores.modal.amazon.OrderFulfillment.Item;
import salesmachine.oim.suppliers.FtpDetails.WhareHouseType;
import salesmachine.oim.suppliers.exception.SupplierCommunicationException;
import salesmachine.oim.suppliers.exception.SupplierConfigurationException;
import salesmachine.oim.suppliers.exception.SupplierOrderException;
import salesmachine.oim.suppliers.exception.SupplierOrderTrackingException;
import salesmachine.oim.suppliers.modal.OrderDetailResponse;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.oim.suppliers.modal.hg.TrackingData;
import salesmachine.oim.suppliers.modal.hg.TrackingData.POTracking;
import salesmachine.util.ApplicationProperties;
import salesmachine.util.OimLogStream;
import salesmachine.util.StringHandle;

import com.amazonaws.mws.MarketplaceWebService;
import com.amazonaws.mws.MarketplaceWebServiceClient;
import com.amazonaws.mws.MarketplaceWebServiceConfig;
import com.amazonaws.mws.MarketplaceWebServiceException;
import com.amazonaws.mws.model.IdList;
import com.amazonaws.mws.model.SubmitFeedRequest;
import com.amazonaws.mws.model.SubmitFeedResponse;
import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;
import com.enterprisedt.net.ftp.FTPTransferType;

public class HonestGreen extends Supplier implements HasTracking {

	private static final Map<String, String> PONUM_UNFI_MAP = new Hashtable<String, String>();

	private static final String UNFIORDERNO = "UNFIORDERNO";
	private static final String PONUM = "PONUM";
	private static final String QTY_ORDERED = "QTY_ORDERED";
	private static final String QTY_SHIPPED = "QTY_SHIPPED";
	private static final String ASCII = "ASCII";
	private static final Logger log = LoggerFactory
			.getLogger(HonestGreen.class);
	// private static final byte BLANK_SPACE = ' ';
	private static final byte[] NEW_LINE = new byte[] { '\n' };
	private static final byte[] HG_EOF = new byte[] { '*', '*', '*', 'E', 'O',
			'F', '*', '*', '*' };
	private static final byte[] FIL = new byte[] { 'F', 'I', 'L' };
	private static final byte[] COMMA = new byte[] { ',' };
	private static final String SHIP_DATE = "SHIP_DATE";
	private static JAXBContext jaxbContext;
	protected Map<String, OimOrderDetails> HvaMap = new HashMap<String, OimOrderDetails>();
	protected Map<String, OimOrderDetails> PhiMap = new HashMap<String, OimOrderDetails>();
	protected Map<String, OimOrderDetails> HVAPhiMap = new HashMap<String, OimOrderDetails>();

	static {
		try {
			jaxbContext = JAXBContext.newInstance(TrackingData.class);
		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
		}
	}

	static {
		try {
			FileInputStream fis = new FileInputStream("orders.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			PONUM_UNFI_MAP.putAll((Hashtable<String, String>) ois.readObject());
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
			log.warn("orders.ser file is blank");
		}
	}

	@Override
	public void sendOrders(Integer vendorId, OimVendorSuppliers ovs, List orders)
			throws SupplierConfigurationException,
			SupplierCommunicationException, SupplierOrderException,
			ChannelConfigurationException, ChannelCommunicationException,
			ChannelOrderFormatException {
		log.info("Sending orders of Account: {}", ovs.getAccountNumber());
		if (ovs.getTestMode().equals(1))
			return;
		boolean isOrderFromAmazonStore = isOrderFromAmazonStore(orders);
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
					getProdTypeMap(order, vendorId);
					for (OimOrderDetails orderDetail : ((Set<OimOrderDetails>) order
							.getOimOrderDetailses())) {
						String sku = orderDetail.getSku();
						isRestricted(sku, orderDetail, vendorId);
					}
					// ***************************************************
					if (order.getOimOrderBatches().getOimChannels()
							.getEmailNotifications() == 1) {
						emailNotification = true;
						String orderStatus = "Successfully Placed";
						emailContent += "<b>Store Order ID "
								+ order.getStoreOrderId() + "</b> -> "
								+ orderStatus + " ";
						emailContent += "<br>";
					}
					if (HvaMap.size() > 0) {
						// create order file and send order to Hva configured
						// ftp
						Map<Integer, OimOrderDetails> orderDetailMap = new HashMap<Integer, OimOrderDetails>();
						for (Iterator<OimOrderDetails> itr = HvaMap.values()
								.iterator(); itr.hasNext();) {
							OimOrderDetails detail = itr.next();
							orderDetailMap.put(detail.getDetailId(), detail);
						}
						FtpDetails ftpDetails = new FtpDetails();
						ftpDetails.setWhareHouseType(WhareHouseType.HVA);
						for (Iterator<OimSupplierMethods> itr = ovs
								.getOimSuppliers().getOimSupplierMethodses()
								.iterator(); itr.hasNext();) {
							OimSupplierMethods oimSupplierMethods = itr.next();
							if (oimSupplierMethods.getOimSupplierMethodTypes()
									.getMethodTypeId().intValue() == OimConstants.SUPPLIER_METHOD_TYPE_HG_HVA) {
								ftpDetails
										.setWhareHouseType(WhareHouseType.HVA);
								log.info("found configured HVA Location ");
								if (oimSupplierMethods.getVendor() != null
										&& oimSupplierMethods.getVendor()
												.getVendorId().intValue() == ovs
												.getVendors().getVendorId()
												.intValue()) {
									for (Iterator<OimSupplierMethodattrValues> iterator = oimSupplierMethods
											.getOimSupplierMethodattrValueses()
											.iterator(); iterator.hasNext();) {

										OimSupplierMethodattrValues oimSupplierMethodattrValues = iterator
												.next();
										if (oimSupplierMethodattrValues
												.getOimSupplierMethodattrNames()
												.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPACCOUNT)
											ftpDetails
													.setAccountNumber(oimSupplierMethodattrValues
															.getAttributeValue());
										if (oimSupplierMethodattrValues
												.getOimSupplierMethodattrNames()
												.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPSERVER)
											ftpDetails
													.setUrl(oimSupplierMethodattrValues
															.getAttributeValue());
										if (oimSupplierMethodattrValues
												.getOimSupplierMethodattrNames()
												.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPLOGIN)
											ftpDetails
													.setUserName(oimSupplierMethodattrValues
															.getAttributeValue());
										if (oimSupplierMethodattrValues
												.getOimSupplierMethodattrNames()
												.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPPASSWORD)
											ftpDetails
													.setPassword(oimSupplierMethodattrValues
															.getAttributeValue());
									}
								}
							}
						}
						String fileName = createOrderFile(order, ovs,
								orderDetailMap, ftpDetails,
								isOrderFromAmazonStore);
						sendToFTP(fileName, ftpDetails);
						// if (emailNotification) {
						sendEmail(emailContent, ftpDetails, fileName, "");
						// }

					}
					if (PhiMap.size() > 0) {
						// create order file and send order to PHI configured
						// ftp
						FtpDetails ftpDetails = new FtpDetails();
						ftpDetails.setWhareHouseType(WhareHouseType.PHI);
						Map<Integer, OimOrderDetails> orderDetailMap = new HashMap<Integer, OimOrderDetails>();
						for (Iterator<OimOrderDetails> itr = PhiMap.values()
								.iterator(); itr.hasNext();) {
							OimOrderDetails detail = itr.next();
							orderDetailMap.put(detail.getDetailId(), detail);
						}
						for (Iterator<OimSupplierMethods> itr = ovs
								.getOimSuppliers().getOimSupplierMethodses()
								.iterator(); itr.hasNext();) {
							OimSupplierMethods oimSupplierMethods = itr.next();
							if (oimSupplierMethods.getOimSupplierMethodTypes()
									.getMethodTypeId().intValue() == OimConstants.SUPPLIER_METHOD_TYPE_HG_PHI) {
								ftpDetails
										.setWhareHouseType(WhareHouseType.PHI);
								log.info("found configured PHI Location ");
								if (oimSupplierMethods.getVendor() != null
										&& oimSupplierMethods.getVendor()
												.getVendorId().intValue() == ovs
												.getVendors().getVendorId()
												.intValue()) {
									for (Iterator<OimSupplierMethodattrValues> iterator = oimSupplierMethods
											.getOimSupplierMethodattrValueses()
											.iterator(); iterator.hasNext();) {

										OimSupplierMethodattrValues oimSupplierMethodattrValues = iterator
												.next();
										if (oimSupplierMethodattrValues
												.getOimSupplierMethodattrNames()
												.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPACCOUNT)
											ftpDetails
													.setAccountNumber(oimSupplierMethodattrValues
															.getAttributeValue());
										if (oimSupplierMethodattrValues
												.getOimSupplierMethodattrNames()
												.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPSERVER)
											ftpDetails
													.setUrl(oimSupplierMethodattrValues
															.getAttributeValue());
										if (oimSupplierMethodattrValues
												.getOimSupplierMethodattrNames()
												.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPLOGIN)
											ftpDetails
													.setUserName(oimSupplierMethodattrValues
															.getAttributeValue());
										if (oimSupplierMethodattrValues
												.getOimSupplierMethodattrNames()
												.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPPASSWORD)
											ftpDetails
													.setPassword(oimSupplierMethodattrValues
															.getAttributeValue());
									}
								}
							}
						}
						String fileName = createOrderFile(order, ovs,
								orderDetailMap, ftpDetails,
								isOrderFromAmazonStore);
						sendToFTP(fileName, ftpDetails);
						// if (emailNotification) {
						sendEmail(emailContent, ftpDetails, fileName, "");
						// }

					}
					if (HVAPhiMap.size() > 0) {
						// check quantity of hva and phi. based on that send
						// order file to particular location.
						Map<Integer, OimOrderDetails> orderDetailHVAMap = new HashMap<Integer, OimOrderDetails>();
						Map<Integer, OimOrderDetails> orderDetailPHIMap = new HashMap<Integer, OimOrderDetails>();
						for (Iterator<OimOrderDetails> itr = HVAPhiMap.values()
								.iterator(); itr.hasNext();) {
							OimOrderDetails detail = itr.next();
							int hvaQuantity = getHvaQuantity(detail.getSku());
							int phiQuantity = getPhiQuantity(detail.getSku(),
									vendorId, hvaQuantity);
							if (hvaQuantity > phiQuantity) {
								orderDetailHVAMap.put(detail.getDetailId(),
										detail);
							} else
								orderDetailPHIMap.put(detail.getDetailId(),
										detail);
						}
						String fileName = "";
						if (orderDetailHVAMap.size() > 0) {
							FtpDetails ftpDetails = new FtpDetails();
							for (Iterator<OimSupplierMethods> itr = ovs
									.getOimSuppliers()
									.getOimSupplierMethodses().iterator(); itr
									.hasNext();) {
								OimSupplierMethods oimSupplierMethods = itr
										.next();
								if (oimSupplierMethods
										.getOimSupplierMethodTypes()
										.getMethodTypeId().intValue() == OimConstants.SUPPLIER_METHOD_TYPE_HG_HVA) {
									ftpDetails
											.setWhareHouseType(WhareHouseType.HVA);
									log.info("found configured HVA Location ");
									if (oimSupplierMethods.getVendor() != null
											&& oimSupplierMethods.getVendor()
													.getVendorId().intValue() == ovs
													.getVendors().getVendorId()
													.intValue()) {
										for (Iterator<OimSupplierMethodattrValues> iterator = oimSupplierMethods
												.getOimSupplierMethodattrValueses()
												.iterator(); iterator.hasNext();) {

											OimSupplierMethodattrValues oimSupplierMethodattrValues = iterator
													.next();
											if (oimSupplierMethodattrValues
													.getOimSupplierMethodattrNames()
													.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPACCOUNT)
												ftpDetails
														.setAccountNumber(oimSupplierMethodattrValues
																.getAttributeValue());
											if (oimSupplierMethodattrValues
													.getOimSupplierMethodattrNames()
													.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPSERVER)
												ftpDetails
														.setUrl(oimSupplierMethodattrValues
																.getAttributeValue());
											if (oimSupplierMethodattrValues
													.getOimSupplierMethodattrNames()
													.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPLOGIN)
												ftpDetails
														.setUserName(oimSupplierMethodattrValues
																.getAttributeValue());
											if (oimSupplierMethodattrValues
													.getOimSupplierMethodattrNames()
													.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPPASSWORD)
												ftpDetails
														.setPassword(oimSupplierMethodattrValues
																.getAttributeValue());
										}
									}
								}
							}
							fileName = createOrderFile(order, ovs,
									orderDetailHVAMap, ftpDetails,
									isOrderFromAmazonStore);
							sendToFTP(fileName, ftpDetails);
							// if (emailNotification) {
							sendEmail(emailContent, ftpDetails, fileName, "");
							// }

						}
						if (orderDetailPHIMap.size() > 0) {
							FtpDetails ftpDetails = new FtpDetails();
							for (Iterator<OimSupplierMethods> itr = ovs
									.getOimSuppliers()
									.getOimSupplierMethodses().iterator(); itr
									.hasNext();) {
								OimSupplierMethods oimSupplierMethods = itr
										.next();
								if (oimSupplierMethods
										.getOimSupplierMethodTypes()
										.getMethodTypeId().intValue() == OimConstants.SUPPLIER_METHOD_TYPE_HG_PHI) {
									ftpDetails
											.setWhareHouseType(WhareHouseType.PHI);
									log.info("found configured PHI Location ");
									if (oimSupplierMethods.getVendor() != null
											&& oimSupplierMethods.getVendor()
													.getVendorId().intValue() == ovs
													.getVendors().getVendorId()
													.intValue()) {
										for (Iterator<OimSupplierMethodattrValues> iterator = oimSupplierMethods
												.getOimSupplierMethodattrValueses()
												.iterator(); iterator.hasNext();) {

											OimSupplierMethodattrValues oimSupplierMethodattrValues = iterator
													.next();
											if (oimSupplierMethodattrValues
													.getOimSupplierMethodattrNames()
													.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPACCOUNT)
												ftpDetails
														.setAccountNumber(oimSupplierMethodattrValues
																.getAttributeValue());
											if (oimSupplierMethodattrValues
													.getOimSupplierMethodattrNames()
													.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPSERVER)
												ftpDetails
														.setUrl(oimSupplierMethodattrValues
																.getAttributeValue());
											if (oimSupplierMethodattrValues
													.getOimSupplierMethodattrNames()
													.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPLOGIN)
												ftpDetails
														.setUserName(oimSupplierMethodattrValues
																.getAttributeValue());
											if (oimSupplierMethodattrValues
													.getOimSupplierMethodattrNames()
													.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPPASSWORD)
												ftpDetails
														.setPassword(oimSupplierMethodattrValues
																.getAttributeValue());
										}
									}
								}
							}
							fileName = createOrderFile(order, ovs,
									orderDetailPHIMap, ftpDetails,
									isOrderFromAmazonStore);
							sendToFTP(fileName, ftpDetails);

							// if (emailNotification) {
							sendEmail(emailContent, ftpDetails, fileName, "");
							// }
						}

					}
					// ***************************************************

				} catch (RuntimeException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}

	private boolean isOrderFromAmazonStore(List orders) {
		for (Object object : orders) {
			if (object instanceof OimOrders) {
				OimOrders order = (OimOrders) object;
				if (order.getOimOrderBatches().getOimChannels()
						.getOimSupportedChannels().getSupportedChannelId() == 4)
					return true;
			}
		}
		return false;
	}

	private void sendEmail(String emailContent, FtpDetails ftpDetails,
			String fileName, String login) {
		String emailBody = "Account Number : " + ftpDetails.getAccountNumber()
				+ "\n Find attached order file for the orders from my store.";
		String emailSubject = fileName;
		EmailUtil.sendEmailWithAttachment("orders@inventorysource.com",
				"support@inventorysource.com", login, emailSubject, emailBody,
				fileName);

	}

	private void sendErrorReportEmail(String fileName, FtpDetails ftpDetails) {
		EmailUtil.sendEmail("support@inventorysource.com",
				"support@inventorysource.com", "",
				"Failed to put order file to " + ftpDetails.getUrl(),
				"Failed to put this order file " + fileName + " to "
						+ ftpDetails.getUrl(), "text/html");
	}

	private int getPhiQuantity(String sku, Integer vendorId, int hvaQuantity) {
		Session dbSession = SessionManager.currentSession();
		Query query = dbSession
				.createSQLQuery("select QUANTITY from VENDOR_CUSTOM_FEEDS_PRODUCTS where sku=:sku and VENDOR_CUSTOM_FEED_ID=(select VENDOR_CUSTOM_FEED_ID from VENDOR_CUSTOM_FEEDS where vendor_id=:vendorID AND IS_RESTRICTED=1)");
		query.setString("sku", sku);
		query.setInteger("vendorID", vendorId);
		Object q = query.uniqueResult();
		log.info("PHI Quantity: {}", q.toString());
		int tempQuantity = 0;
		if (q != null)
			tempQuantity = Integer.parseInt(q.toString());

		return tempQuantity - hvaQuantity;
	}

	private int getHvaQuantity(String sku) {
		Session dbSession = SessionManager.currentSession();
		Query query = dbSession
				.createQuery("select p from salesmachine.hibernatedb.Product p where p.sku=:sku");

		query.setString("sku", sku);
		System.out.println(query.list());
		Product p = (Product) query.uniqueResult();
		return p.getQuantity();
	}

	private void sendToFTP(String fileName, FtpDetails ftpDetails)
			throws SupplierCommunicationException,
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
			ftp.put(fileName, file.getName());
			if (ftp.get(file.getName()) == null) {
				sendErrorReportEmail(fileName, ftpDetails);
			}
			ftp.quit();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new SupplierCommunicationException(
					"Could not connect to FTP while sending orderfile to HG - "
							+ e.getMessage(), e);
		} catch (FTPException e) {
			log.error(e.getMessage(), e);
			throw new SupplierConfigurationException(
					"Could not connect to FTP while sending orderfile to HG - "
							+ e.getMessage(), e);
		}
	}

	private void getProdTypeMap(OimOrders order, int vendorId) {
		// for (OimOrderDetails orderDetail : ((Set<OimOrderDetails>) order
		// .getOimOrderDetailses())) {
		// String sku = orderDetail.getSku();
		// if (isHVAAndPHIBoth(sku,vendorId))
		// HVAPhiMap.put(sku,sku);
		// else if(isHVA(sku,vendorId))
		// HvaMap.put(sku,sku);
		// else if(isPHI(sku,vendorId))
		// PhiMap.put(sku, sku);
		// }

	}

	private boolean isRestricted(String sku, OimOrderDetails orderDetail,
			int vendorID) {
		// if is restricted value is 0 in product table and 1 in vendor custom
		// product
		// or if is restricted value is 1 in product table
		// Transaction tx = null;
		Session dbSession = SessionManager.currentSession();
		// tx = dbSession.beginTransaction();
		Query query = dbSession
				.createQuery("select p.isRestricted from salesmachine.hibernatedb.Product p where p.sku=:sku");
		query.setString("sku", sku);
		Object restrictedIntVal = query.uniqueResult();
		if (restrictedIntVal != null
				&& ((Integer) restrictedIntVal).intValue() == 1) {
			log.debug("{} is restricted in product table", sku);
			HvaMap.put(sku, orderDetail);
		} else {
			query = dbSession
					.createSQLQuery("select IS_RESTRICTED from VENDOR_CUSTOM_FEEDS_PRODUCTS where sku=:sku and VENDOR_CUSTOM_FEED_ID=(select VENDOR_CUSTOM_FEED_ID from VENDOR_CUSTOM_FEEDS where vendor_id=:vendorID)");
			query.setString("sku", sku);
			query.setInteger("vendorID", vendorID);
			Object restrictedVal = query.uniqueResult();
			if (restrictedVal != null
					&& ((BigDecimal) restrictedVal).intValue() == 1) {
				log.debug(
						"{} is restricted in VENDOR_CUSTOM_FEEDS_PRODUCTS table",
						sku);
				HVAPhiMap.put(sku, orderDetail);
			} else {
				log.debug("{} is not restricted in both the tables", sku);
				PhiMap.put(sku, orderDetail);
			}
		}
		// return ((Integer) restrictedIntVal).intValue() == 1 ? true : false;
		return HvaMap.size() > 0 || HVAPhiMap.size() > 0;
	}

	private static Map<String, String> parseOrderConfirmation(
			Map<Integer, String> orderConfirmationMap, String tempTrackingMeta) {
		Map<String, String> orderData = new HashMap<String, String>();

		for (Iterator itr = orderConfirmationMap.values().iterator(); itr
				.hasNext();) {
			String line = (String) itr.next();
			String[] lineArray = line.split(",");
			if (lineArray.length == 9) {

				PONUM_UNFI_MAP.put(lineArray[6], lineArray[0]);
				if (lineArray[6].equals(tempTrackingMeta)) {
					orderData.put(PONUM, lineArray[6]);
					orderData.put(UNFIORDERNO, lineArray[0]);
					break;
				}
			}
		}
		// String[] lineArray1 = orderConfirmationMap.get(1).split(",");
		//
		// orderData.put(PONUM, lineArray1[6]);
		// orderData.put(UNFIORDERNO, lineArray1[0]);
		return orderData;
	}

	private Map<String, String> parseShippingConfirmation(
			Map<Integer, String> shippingConfirmationMap, String sku) {
		Map<String, String> orderData = new HashMap<String, String>();
		String[] lineArray1 = shippingConfirmationMap.get(1).split(",");
		orderData.put(PONUM, lineArray1[7]);
		orderData.put(UNFIORDERNO, lineArray1[9]);
		PONUM_UNFI_MAP.put(lineArray1[7], lineArray1[9]);
		orderData.put(SHIP_DATE, lineArray1[lineArray1.length - 1]);
		for (Iterator itr = shippingConfirmationMap.values().iterator(); itr
				.hasNext();) {
			String line = (String) itr.next();
			String[] lineArray = line.split(",");
			if (lineArray.length > 4 && lineArray[0] == sku) {
				orderData.put(QTY_ORDERED, lineArray[3]);
				orderData.put(QTY_SHIPPED, lineArray[4]);
			}
		}
		return orderData;
	}

	/**
	 * Parses and returns the file data in a Map with line number as keys.
	 * 
	 * @param confirmationFileData
	 * @return file data in a Map with line number as keys.
	 */
	private static Map<Integer, String> parseFileData(
			byte[] confirmationFileData) {
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

	private String createOrderFile(OimOrders order, OimVendorSuppliers ovs,
			Map<Integer, OimOrderDetails> detailMap, FtpDetails ftpDetails,
			boolean isAmazon) throws ChannelCommunicationException,
			ChannelOrderFormatException {

		String uploadfilename = "HG_" + ftpDetails.getAccountNumber() + "_"
				+ new Random().nextLong() + ".txt";
		File f = new File(uploadfilename);
		log.info("created file name for HG:{}", f.getName());
		log.debug("Creating order file for OrderId:{}", order.getOrderId());
		try {
			FileOutputStream fOut = new FileOutputStream(f);
			String poNum = null;
			// Integer orderSize = order.getOimOrderDetailses().size();
			fOut.write("1".getBytes(ASCII));
			fOut.write(NEW_LINE);
			fOut.write(ftpDetails.getAccountNumber().getBytes(ASCII));
			fOut.write(COMMA);
			// fOut.write(BLANK_SPACE);
			fOut.write(COMMA);
			if (isAmazon)
				poNum = ftpDetails.getWhareHouseType().getWharehouseType() == OimConstants.SUPPLIER_METHOD_TYPE_HG_HVA
						.intValue() ? "H" + order.getStoreOrderId() : "P"
						+ order.getStoreOrderId();
			else
				poNum = ftpDetails.getWhareHouseType().getWharehouseType() == OimConstants.SUPPLIER_METHOD_TYPE_HG_HVA
						.intValue() ? "H" + ovs.getVendors().getVendorId()
						+ "-" + order.getStoreOrderId() : "P"
						+ ovs.getVendors().getVendorId() + "-"
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
			// fOut.write(StringHandle.removeNull(order.getDeliveryPhone())
			// .toUpperCase().getBytes(ASCII));
			fOut.write(COMMA);
			fOut.write('A');
			fOut.write(COMMA);
			fOut.write("5001".getBytes(ASCII));
			fOut.write(NEW_LINE);
			// for (OimOrderDetails od : ((Set<OimOrderDetails>) order
			// .getOimOrderDetailses())) {
			for (Iterator<OimOrderDetails> itr = detailMap.values().iterator(); itr
					.hasNext();) {
				OimOrderDetails od = itr.next();
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
				// od.setSupplierOrderNumber(poNum);
				// od.setSupplierOrderStatus("Sent to supplier.");
				// Session session = SessionManager.currentSession();
				// session.update(od);
				successfulOrders.put(od.getDetailId(), new OrderDetailResponse(
						poNum, "Sent to supplier."));
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
							iOrderImport.updateStoreOrder(od, orderStatus);
						}
					} catch (ChannelConfigurationException e) {
						log.error(e.getMessage(), e);
						stream.println(e.getMessage());
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
	private static final String ORDER_TRACKING_FILE_PATH_TEMPLATE = "tracking/%s.T%sX.txt";
	private static final String ORDER_TRACKING_FILE_PATH_ARCHIVE_TEMPLATE = "archive/%s.T%sX.txt";

	private String getShippingFilePath(String account, String unfiOrderNo) {
		return getFilePath(ORDER_SHIPPING_FILE_PATH_TEMPLATE, account,
				unfiOrderNo);
	}

	private static String getConfirmationFilePath(String account,
			String unfiOrderNo) {
		return getFilePath(ORDER_CONFIRMATION_FILE_PATH_TEMPLATE, account,
				unfiOrderNo);
	}

	private static String getTrackingFilePath(String account, String unfiOrderNo) {
		return getFilePath(ORDER_TRACKING_FILE_PATH_TEMPLATE, account,
				unfiOrderNo);
	}

	private static String getTrackingFilePathInArchive(String account,
			String unfiOrderNo) {
		return getFilePath(ORDER_TRACKING_FILE_PATH_ARCHIVE_TEMPLATE, account,
				unfiOrderNo);
	}

	private static String getFilePath(String template, String account,
			String unfiOrderNo) {
		return String.format(template, account, unfiOrderNo);
	}

	@Override
	public OrderStatus getOrderStatus(OimVendorSuppliers ovs,
			Object trackingMeta, OimOrderDetails oimOrderDetails)
			throws SupplierOrderTrackingException {
		clearCache();
		log.info("Tracking request for PONUM: {}", trackingMeta);
		orderSkuPrefixMap = setSkuPrefixForOrders(ovs);
		if (!(trackingMeta instanceof String))
			throw new IllegalArgumentException(
					"trackingMeta is expected to be a String value containing UNFI Order number.");
		OrderStatus orderStatus = new OrderStatus();
		orderStatus.setStatus("Sent to supplier.");
		Session session = SessionManager.currentSession();

		List<OimOrders> oimOrderList = new ArrayList<OimOrders>();
		oimOrderList.add(oimOrderDetails.getOimOrders());
		boolean isAmazon = isOrderFromAmazonStore(oimOrderList);
		String tempTrackingMeta = isAmazon ? new String((String) trackingMeta)
				: ovs.getVendors().getVendorId() + "-"
						+ new String((String) trackingMeta);

		Map<String, FtpDetails> ftpDetailMap = new HashMap<String, FtpDetails>();

		if (tempTrackingMeta.startsWith("H")) {
			FtpDetails ftpDetails = getFtpDetails(ovs, false, true);
			ftpDetailMap.put("FromHvaMap", ftpDetails);
		} else if (tempTrackingMeta.startsWith("P")) {
			FtpDetails ftpDetails = getFtpDetails(ovs, true, false);
			ftpDetailMap.put("FromPhiMap", ftpDetails);
		} else {
			isRestricted(oimOrderDetails.getSku(), oimOrderDetails, ovs
					.getVendors().getVendorId());
			if (HvaMap.size() > 0) {
				FtpDetails ftpDetails = getFtpDetails(ovs, false, true);
				ftpDetailMap.put("FromHvaMap", ftpDetails);
			} else if (PhiMap.size() > 0) {
				FtpDetails ftpDetails = getFtpDetails(ovs, true, false);
				ftpDetailMap.put("FromPhiMap", ftpDetails);
			} else if (HVAPhiMap.size() > 0) {
				Map<String, FtpDetails> ftpDetailMaps = getFtpdDetailsForHVAAndPHI(
						ovs.getVendors().getVendorId(), ovs);
				ftpDetailMap.putAll(ftpDetailMaps);
			}
		}

		if (ftpDetailMap != null && ftpDetailMap.size() > 0) {
			for (Iterator<FtpDetails> itr1 = ftpDetailMap.values().iterator(); itr1
					.hasNext();) {
				FtpDetails ftpDetails = itr1.next();
				FTPClient ftp = new FTPClient();
				try {
					ftp.setRemoteHost(ftpDetails.getUrl());
					ftp.setDetectTransferMode(true);
					ftp.connect();
					ftp.login(ftpDetails.getUserName(),
							ftpDetails.getPassword());
					ftp.setTimeout(60 * 1000 * 60 * 7);
					String unfiNumber = findUNFIFromConfirmations(ftp,
							oimOrderDetails, tempTrackingMeta, orderStatus);
					// if(unfiNumber==null){
					// EmailUtil.sendEmail("support@inventorysource.com",
					// "support@inventorysource.com", "",
					// "Order confirmation failed for OrderID"+tempTrackingMeta,
					// "Order confirmation file for order id - "+tempTrackingMeta+" is not found at HG's ftp for account - "+ftpDetails.getAccountNumber(),
					// "text/html");
					// orderStatus.setStatus("Order Confirmation Failed");
					//
					// return orderStatus;
					// }

					getTrackingInfo(ftpDetails, ftp, unfiNumber, orderStatus,
							tempTrackingMeta, oimOrderDetails, trackingMeta);
				} catch (IOException | FTPException | ParseException
						| JAXBException e) {
					log.error(e.getMessage(), e);

				}
			}
		}
		serializeMap();
		return orderStatus;
	}

	private static synchronized void serializeMap() {
		try {
			FileOutputStream fs = new FileOutputStream("orders.ser");
			ObjectOutputStream os = new ObjectOutputStream(fs);
			os.writeObject(PONUM_UNFI_MAP);
			os.close();
		} catch (Exception e) {
			log.warn("error occure while serializing PONUM_UNFI_MAP to orders.ser");
		}
	}

	private void getTrackingInfo(FtpDetails ftpDetails, FTPClient ftp,
			String unfiOrderNo, OrderStatus orderStatus,
			String tempTrackingMeta, OimOrderDetails detail, Object trackingMeta)
			throws FTPException, IOException, ParseException, JAXBException {
		byte[] trackingFileData = null;

		String skuPrefix = null;
		String sku = detail.getSku();
		if (!orderSkuPrefixMap.isEmpty()) {
			skuPrefix = orderSkuPrefixMap.values().toArray()[0].toString();
		}
		skuPrefix = StringHandle.removeNull(skuPrefix);
		if (sku.startsWith(skuPrefix)) {
			sku = sku.substring(skuPrefix.length());
		}

		Map<String, String> parseShippingConfirmation = new HashMap<String, String>();
		if (unfiOrderNo != null) {
			String trackingFilePath = getTrackingFilePath(
					ftpDetails.getAccountNumber(), unfiOrderNo);
			orderStatus.setStatus("In-Process");
			String shippingFilePath = getShippingFilePath(
					ftpDetails.getAccountNumber(), unfiOrderNo);
			byte[] shippingFileData = ftp.get(shippingFilePath);
			Map<Integer, String> shippingDataMap = parseFileData(shippingFileData);
			parseShippingConfirmation = parseShippingConfirmation(
					shippingDataMap, sku);
			try {
				trackingFileData = ftp.get(trackingFilePath);
			} catch (FTPException e) {
				log.error(e.getMessage(), e);
				trackingFileData = null;
			}
		} else {

			FTPFile[] ftpFiles = ftp.dirDetails("shipping");
			Arrays.sort(ftpFiles, new Comparator<FTPFile>() {
				public int compare(FTPFile f1, FTPFile f2) {
					return f2.lastModified().compareTo(f1.lastModified());
				}
			});
			for (FTPFile ftpFile : ftpFiles) {
				String shippingFileName = ftpFile.getName();
				if (shippingFileName.equals("..")
						|| shippingFileName.equals("."))
					continue;

				/*
				 * log.info(
				 * "shipping file path: {} Last Modified: {} Order Processing Time: {}"
				 * , ftpFile.getName(), ftpFile.lastModified(),
				 * detail.getProcessingTm());
				 */
				if (ftpFile.lastModified().before(detail.getProcessingTm()))
					break;
				byte[] sippingFileData = ftp
						.get("shipping/" + shippingFileName);
				if (sippingFileData != null) {
					Map<Integer, String> shippingDataMap = parseFileData(sippingFileData);
					parseShippingConfirmation = parseShippingConfirmation(
							shippingDataMap, sku);
					log.info("Shipping {}", parseShippingConfirmation);
					if (tempTrackingMeta.toString().equals(
							parseShippingConfirmation.get(PONUM))) {
						unfiOrderNo = parseShippingConfirmation
								.get(UNFIORDERNO);

						if (unfiOrderNo != null) {
							orderStatus.setStatus("In-Process");

							String trackingFilePath = getTrackingFilePath(
									ftpDetails.getAccountNumber(), unfiOrderNo);
							try {
								trackingFileData = ftp.get(trackingFilePath);
							} catch (FTPException e) {
								log.warn("Tracking file not found in Tracking folder .. going to archive..");
								trackingFileData = ftp
										.get(getTrackingFilePathInArchive(
												ftpDetails.getAccountNumber(),
												unfiOrderNo));
								log.info("Found under Archive folder...");
							}
						}
						break;
					}
				}
			}
		}

		if (trackingFileData != null) {
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			String s = new String(trackingFileData);
			StringReader reader = new StringReader(s);
			log.info(s);
			TrackingData orderTrackingResponse = (TrackingData) unmarshaller
					.unmarshal(reader);
			orderStatus.setStatus("Shipped");
			int perBoxQty = 1;
			if (detail.getQuantity() == orderTrackingResponse.getPOTracking()
					.size()) {
				perBoxQty = 1;
			} else {
				if (detail.getQuantity()
						% orderTrackingResponse.getPOTracking().size() == 0) {
					perBoxQty = detail.getQuantity()
							/ orderTrackingResponse.getPOTracking().size();
				} else if (orderTrackingResponse.getPOTracking().size()
						% detail.getQuantity() == 0)
					perBoxQty = orderTrackingResponse.getPOTracking().size()
							/ detail.getQuantity();
			}
			for (POTracking poTracking : orderTrackingResponse.getPOTracking()) {
				salesmachine.oim.suppliers.modal.TrackingData trackingData = new salesmachine.oim.suppliers.modal.TrackingData();
				if ("A".equals(orderTrackingResponse.getPO().getShipVia())) {
					trackingData.setCarrierCode("UPS");
					trackingData.setCarrierName("UPS");
					trackingData.setShippingMethod("Ground");

				} else {
					trackingData.setCarrierName(orderTrackingResponse.getPO()
							.getShipVia());
				}
				trackingData.setShipperTrackingNumber(poTracking
						.getTrackingNumber());

				trackingData.setQuantity(perBoxQty);
				// String dateshipped =
				// parseShippingConfirmation.get(SHIP_DATE);
				// FORMAT 08/11/2015 MM/DD/YYYY
				// int year = Integer.parseInt(dateshipped.substring(6, 10));
				// int month = Integer.parseInt(dateshipped.substring(0, 2));
				// int dayOfMonth = Integer.parseInt(dateshipped.substring(3,
				// 5));
				trackingData.setShipDate(orderTrackingResponse.getPO()
						.getDeliveryDate());
				orderStatus.addTrackingData(trackingData);
			}
			log.info("Tracking details: {}", orderStatus);
		} else {
			log.info("Tracking Details not updated for - {}",
					trackingMeta.toString());
		}
	}

	private String findUNFIFromConfirmations(FTPClient ftp,
			OimOrderDetails detail, String tempTrackingMeta,
			OrderStatus orderStatus) throws IOException, FTPException,
			ParseException {
		log.info("UNFI MAP Size: {}", PONUM_UNFI_MAP.size());
		if (PONUM_UNFI_MAP.containsKey(tempTrackingMeta)) {
			log.info("UNFI Order Id found in MAP {}",
					PONUM_UNFI_MAP.get(tempTrackingMeta));
			orderStatus.setStatus("In-Process");
			return PONUM_UNFI_MAP.get(tempTrackingMeta);
		}
		FTPFile[] ftpFiles = ftp.dirDetails("confirmations");
		Arrays.sort(ftpFiles, new Comparator<FTPFile>() {
			public int compare(FTPFile f1, FTPFile f2) {
				return f2.lastModified().compareTo(f1.lastModified());
			}
		});
		for (FTPFile ftpFile : ftpFiles) {
			String confirmationFile = ftpFile.getName();
			if (confirmationFile.equals("..") || confirmationFile.equals("."))
				continue;

			// log.info(
			// "Confirmation file path: {} Last Modified: {} Order Processing Time: {}",
			// ftpFile.getName(), ftpFile.lastModified(),
			// detail.getProcessingTm());
			if (ftpFile.lastModified().before(detail.getProcessingTm()))
				break;
			byte[] confirmationFileData = ftp.get("confirmations/"
					+ confirmationFile);
			Map<Integer, String> orderDataMap = parseFileData(confirmationFileData);
			Map<String, String> orderData = parseOrderConfirmation(
					orderDataMap, tempTrackingMeta);
			if (tempTrackingMeta.toString().equals(orderData.get(PONUM))) {
				log.info("Order Confirmation details found for {}", orderData);
				orderStatus.setStatus("In-Process");

				return orderData.get(UNFIORDERNO);
			}
		}
		return null;
	}

	private Map<String, FtpDetails> getFtpdDetailsForHVAAndPHI(int vendorId,
			OimVendorSuppliers ovs) {

		// check quantity of hva and phi. based on that send
		// order file to particular location.
		Map<Integer, OimOrderDetails> orderDetailHVAMap = new HashMap<Integer, OimOrderDetails>();
		Map<Integer, OimOrderDetails> orderDetailPHIMap = new HashMap<Integer, OimOrderDetails>();
		Map<String, FtpDetails> ftpDetailsMap = new HashMap<String, FtpDetails>();
		for (Iterator<OimOrderDetails> itr = HVAPhiMap.values().iterator(); itr
				.hasNext();) {
			OimOrderDetails detail = itr.next();
			int hvaQuantity = getHvaQuantity(detail.getSku());
			int phiQuantity = getPhiQuantity(detail.getSku(), vendorId,
					hvaQuantity);
			if (hvaQuantity > phiQuantity) {
				orderDetailHVAMap.put(detail.getDetailId(), detail);
			} else
				orderDetailPHIMap.put(detail.getDetailId(), detail);
		}
		if (orderDetailHVAMap.size() > 0) {
			FtpDetails ftpDetails = new FtpDetails();
			for (Iterator<OimSupplierMethods> itr = ovs.getOimSuppliers()
					.getOimSupplierMethodses().iterator(); itr.hasNext();) {
				OimSupplierMethods oimSupplierMethods = itr.next();
				if (oimSupplierMethods.getOimSupplierMethodTypes()
						.getMethodTypeId().intValue() == OimConstants.SUPPLIER_METHOD_TYPE_HG_HVA) {
					ftpDetails.setWhareHouseType(WhareHouseType.HVA);
					if (oimSupplierMethods.getVendor() != null
							&& oimSupplierMethods.getVendor().getVendorId()
									.intValue() == ovs.getVendors()
									.getVendorId().intValue()) {
						for (Iterator<OimSupplierMethodattrValues> iterator = oimSupplierMethods
								.getOimSupplierMethodattrValueses().iterator(); iterator
								.hasNext();) {

							OimSupplierMethodattrValues oimSupplierMethodattrValues = iterator
									.next();
							if (oimSupplierMethodattrValues
									.getOimSupplierMethodattrNames()
									.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPACCOUNT)
								ftpDetails
										.setAccountNumber(oimSupplierMethodattrValues
												.getAttributeValue());
							if (oimSupplierMethodattrValues
									.getOimSupplierMethodattrNames()
									.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPSERVER)
								ftpDetails.setUrl(oimSupplierMethodattrValues
										.getAttributeValue());
							if (oimSupplierMethodattrValues
									.getOimSupplierMethodattrNames()
									.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPLOGIN)
								ftpDetails
										.setUserName(oimSupplierMethodattrValues
												.getAttributeValue());
							if (oimSupplierMethodattrValues
									.getOimSupplierMethodattrNames()
									.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPPASSWORD)
								ftpDetails
										.setPassword(oimSupplierMethodattrValues
												.getAttributeValue());
						}
					}
				}
			}
			ftpDetailsMap.put("HVA", ftpDetails);
		}
		if (orderDetailPHIMap.size() > 0) {
			FtpDetails ftpDetails = new FtpDetails();
			for (Iterator<OimSupplierMethods> itr = ovs.getOimSuppliers()
					.getOimSupplierMethodses().iterator(); itr.hasNext();) {
				OimSupplierMethods oimSupplierMethods = itr.next();
				if (oimSupplierMethods.getOimSupplierMethodTypes()
						.getMethodTypeId().intValue() == OimConstants.SUPPLIER_METHOD_TYPE_HG_PHI) {
					ftpDetails.setWhareHouseType(WhareHouseType.PHI);
					if (oimSupplierMethods.getVendor() != null
							&& oimSupplierMethods.getVendor().getVendorId()
									.intValue() == ovs.getVendors()
									.getVendorId().intValue()) {
						for (Iterator<OimSupplierMethodattrValues> iterator = oimSupplierMethods
								.getOimSupplierMethodattrValueses().iterator(); iterator
								.hasNext();) {

							OimSupplierMethodattrValues oimSupplierMethodattrValues = iterator
									.next();
							if (oimSupplierMethodattrValues
									.getOimSupplierMethodattrNames()
									.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPACCOUNT)
								ftpDetails
										.setAccountNumber(oimSupplierMethodattrValues
												.getAttributeValue());
							if (oimSupplierMethodattrValues
									.getOimSupplierMethodattrNames()
									.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPSERVER)
								ftpDetails.setUrl(oimSupplierMethodattrValues
										.getAttributeValue());
							if (oimSupplierMethodattrValues
									.getOimSupplierMethodattrNames()
									.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPLOGIN)
								ftpDetails
										.setUserName(oimSupplierMethodattrValues
												.getAttributeValue());
							if (oimSupplierMethodattrValues
									.getOimSupplierMethodattrNames()
									.getAttrId().intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPPASSWORD)
								ftpDetails
										.setPassword(oimSupplierMethodattrValues
												.getAttributeValue());
						}
					}
				}
			}
			ftpDetailsMap.put("PHI", ftpDetails);
		}

		return ftpDetailsMap;
	}

	private FtpDetails getFtpDetails(OimVendorSuppliers ovs, boolean isPHI,
			boolean isHVA) {
		FtpDetails ftpDetails = new FtpDetails();
		for (Iterator<OimSupplierMethods> itr = ovs.getOimSuppliers()
				.getOimSupplierMethodses().iterator(); itr.hasNext();) {
			OimSupplierMethods oimSupplierMethods = itr.next();
			int compareValue = isHVA ? OimConstants.SUPPLIER_METHOD_TYPE_HG_HVA
					: OimConstants.SUPPLIER_METHOD_TYPE_HG_PHI;
			if (isHVA)
				ftpDetails.setWhareHouseType(WhareHouseType.HVA);
			else
				ftpDetails.setWhareHouseType(WhareHouseType.PHI);
			if (oimSupplierMethods.getOimSupplierMethodTypes()
					.getMethodTypeId().intValue() == compareValue) {
				log.info("found configured Location for {}", isPHI ? "PHI"
						: "HVA");
				if (oimSupplierMethods.getVendor() != null
						&& oimSupplierMethods.getVendor().getVendorId()
								.intValue() == ovs.getVendors().getVendorId()
								.intValue()) {
					for (Iterator<OimSupplierMethodattrValues> iterator = oimSupplierMethods
							.getOimSupplierMethodattrValueses().iterator(); iterator
							.hasNext();) {
						OimSupplierMethodattrValues oimSupplierMethodattrValues = iterator
								.next();

						if (oimSupplierMethodattrValues
								.getOimSupplierMethodattrNames().getAttrId()
								.intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPACCOUNT)
							ftpDetails
									.setAccountNumber(oimSupplierMethodattrValues
											.getAttributeValue());
						if (oimSupplierMethodattrValues
								.getOimSupplierMethodattrNames().getAttrId()
								.intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPSERVER)
							ftpDetails.setUrl(oimSupplierMethodattrValues
									.getAttributeValue());
						if (oimSupplierMethodattrValues
								.getOimSupplierMethodattrNames().getAttrId()
								.intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPLOGIN)
							ftpDetails.setUserName(oimSupplierMethodattrValues
									.getAttributeValue());
						if (oimSupplierMethodattrValues
								.getOimSupplierMethodattrNames().getAttrId()
								.intValue() == OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPPASSWORD)
							ftpDetails.setPassword(oimSupplierMethodattrValues
									.getAttributeValue());
					}
				}
			}
		}
		return ftpDetails;
	}

	private void clearCache() {
		HvaMap = new HashMap<String, OimOrderDetails>();
		PhiMap = new HashMap<String, OimOrderDetails>();
		HVAPhiMap = new HashMap<String, OimOrderDetails>();
	}

	private static String sellerId = "A2V8R85K60KD3B",
			mwsAuthToken = "amzn.mws.c8a76813-c733-c66e-5215-2ef9bcecff4b";
	private static List<String> marketPlaceIdList = new ArrayList<String>();
	private static final MarketplaceWebService service;
	private static JAXBContext jaxbContext2;
	static {
		MarketplaceWebServiceConfig config = new MarketplaceWebServiceConfig();
		config.setServiceURL(ApplicationProperties
				.getProperty(ApplicationProperties.MWS_SERVICE_URL));
		service = new MarketplaceWebServiceClient(
				ApplicationProperties
						.getProperty(ApplicationProperties.MWS_ACCESS_KEY),
				ApplicationProperties
						.getProperty(ApplicationProperties.MWS_SECRET_KEY),
				ApplicationProperties
						.getProperty(ApplicationProperties.MWS_APP_NAME),
				ApplicationProperties
						.getProperty(ApplicationProperties.MWS_APP_VERSION),
				config);

		try {
			jaxbContext2 = JAXBContext.newInstance(OrderFulfillment.class,
					SubmitFeedRequest.class, OrderAcknowledgement.class,
					AmazonEnvelope.class);
			marketPlaceIdList.add("ATVPDKIKX0DER");
		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
		}
	}

	public static void main(String[] args) {
		updateFromConfirmation();
		updateFromTracking();
	}

	public static void updateFromConfirmation() {
		int totalValidPO = 0, shippedPO = 0;

		List<FtpDetails> ftpList = new ArrayList<FtpDetails>(2);

		FtpDetails ftpDetails2 = new FtpDetails();
		ftpDetails2.setUrl("ftp1.unfi.com");
		ftpDetails2.setAccountNumber("40968");
		ftpDetails2.setUserName("evox");
		ftpDetails2.setPassword("evoftp093!");
		ftpList.add(ftpDetails2);

		FtpDetails ftpDetails1 = new FtpDetails();
		ftpDetails1.setUrl("ftp1.unfi.com");
		ftpDetails1.setAccountNumber("70757");
		ftpDetails1.setUserName("70757");
		ftpDetails1.setPassword("vU!6akAB");
		ftpList.add(ftpDetails1);
		for (FtpDetails ftpDetails : ftpList) {

			FTPClient ftp = new FTPClient();
			Session session = SessionManager.currentSession();
			Transaction tx = null;
			SubmitFeedRequest submitFeedRequest = new SubmitFeedRequest();
			submitFeedRequest.setMerchant(sellerId);
			submitFeedRequest.setMWSAuthToken(mwsAuthToken);
			submitFeedRequest
					.setMarketplaceIdList(new IdList(marketPlaceIdList));
			submitFeedRequest.setFeedType("_POST_ORDER_FULFILLMENT_DATA_");

			Marshaller marshaller = null;
			try {
				marshaller = jaxbContext2.createMarshaller();
			} catch (JAXBException e) {
				log.error(e.getMessage(), e);

			}
			AmazonEnvelope envelope = new AmazonEnvelope();
			Header header = new Header();
			header.setDocumentVersion("1.01");
			header.setMerchantIdentifier(sellerId);
			envelope.setHeader(header);
			envelope.setMessageType("OrderFulfillment");
			long msgId = 1L;
			try {

				ftp.setRemoteHost(ftpDetails.getUrl());
				ftp.setDetectTransferMode(true);
				ftp.connect();
				ftp.login(ftpDetails.getUserName(), ftpDetails.getPassword());
				ftp.setTimeout(60 * 1000 * 60 * 7);

				int channelId = 2941;
				IOrderImport iOrderImport = OrderImportManager
						.getIOrderImport(channelId);

				if (!iOrderImport.init(channelId,
						SessionManager.currentSession())) {
					log.debug("Failed initializing the channel with Id:{}",
							channelId);
				}

				FTPFile[] ftpFiles = ftp.dirDetails("confirmations");
				Arrays.sort(ftpFiles, new Comparator<FTPFile>() {
					public int compare(FTPFile f1, FTPFile f2) {
						return f2.lastModified().compareTo(f1.lastModified());
					}
				});
				Date cutoff = new Date(115, 7, 19);
				log.info("Cutoff: {}", cutoff);
				for (FTPFile ftpFile : ftpFiles) {
					try {
						log.info("{} {}", ftpFile.getName(),
								ftpFile.lastModified());
						if (ftpFile.getName().equals(".")
								|| ftpFile.getName().equals("..")
								|| ftpFile.getName().endsWith("S.txt"))
							continue;

						byte[] confirmationFileData = ftp.get("confirmations/"
								+ ftpFile.getName());

						Map<Integer, String> parseFileData = parseFileData(confirmationFileData);

						parseOrderConfirmation(parseFileData, "");
						if (ftpFile.lastModified().before(cutoff))
							break;
					} catch (Exception e) {
						if (tx != null && tx.isActive()) {
							tx.rollback();
						}
						log.error(e.getMessage(), e);
					}
				}
				log.info("Found {} order confirmations till {}",
						PONUM_UNFI_MAP.size(), cutoff);
				for (String purchaseOrder : PONUM_UNFI_MAP.keySet()) {
					try {
						OimOrderDetails detail = (OimOrderDetails) session
								.createCriteria(OimOrderDetails.class)
								.add(Restrictions.eq("supplierOrderNumber",
										purchaseOrder)).uniqueResult();

						if (detail == null
								|| detail.getOimOrderStatuses().getStatusId()
										.intValue() == OimConstants.ORDER_STATUS_SHIPPED
										.intValue()
								|| detail.getOimOrderStatuses().getStatusId()
										.intValue() == OimConstants.ORDER_STATUS_MANUALLY_PROCESSED
										.intValue())
							continue;

						tx = session.beginTransaction();
						String unfiOrderNo = PONUM_UNFI_MAP.get(purchaseOrder);
						OrderStatus orderStatus = new OrderStatus();
						orderStatus.setStatus("In-Process");
						totalValidPO++;
						try {
							byte[] bs;
							try {
								bs = ftp.get(getTrackingFilePath(
										ftpDetails.getAccountNumber(),
										unfiOrderNo));
							} catch (FTPException e) {
								log.warn("Tracking not found PO: {}  UNFI: {}",
										purchaseOrder, unfiOrderNo);
								bs = ftp.get(getTrackingFilePathInArchive(
										ftpDetails.getAccountNumber(),
										unfiOrderNo));
							}
							Unmarshaller unmarshaller = jaxbContext
									.createUnmarshaller();
							String s = new String(bs);
							StringReader reader = new StringReader(s);
							// log.info(s);
							TrackingData orderTrackingResponse = (TrackingData) unmarshaller
									.unmarshal(reader);

							orderStatus.setStatus("Shipped");

							log.info("PONUM# {}", purchaseOrder);

							int perBoxQty = 1;
							if (detail.getQuantity() == orderTrackingResponse
									.getPOTracking().size()) {
								perBoxQty = 1;
							} else {
								if (detail.getQuantity()
										% orderTrackingResponse.getPOTracking()
												.size() == 0) {
									perBoxQty = detail.getQuantity()
											/ orderTrackingResponse
													.getPOTracking().size();
								} else if (orderTrackingResponse
										.getPOTracking().size()
										% detail.getQuantity() == 0)
									perBoxQty = orderTrackingResponse
											.getPOTracking().size()
											/ detail.getQuantity();
							}
							for (POTracking poTracking : orderTrackingResponse
									.getPOTracking()) {
								salesmachine.oim.suppliers.modal.TrackingData trackingData = new salesmachine.oim.suppliers.modal.TrackingData();
								if ("A".equals(orderTrackingResponse.getPO()
										.getShipVia())) {
									trackingData.setCarrierCode("UPS");
									trackingData.setCarrierName("UPS");
									trackingData.setShippingMethod("Ground");

								} else {
									trackingData
											.setCarrierName(orderTrackingResponse
													.getPO().getShipVia());
								}
								trackingData
										.setShipperTrackingNumber(poTracking
												.getTrackingNumber());

								trackingData.setQuantity(perBoxQty);
								trackingData.setShipDate(orderTrackingResponse
										.getPO().getDeliveryDate());
								orderStatus.addTrackingData(trackingData);
								shippedPO++;
							}
						} catch (FTPException e) {
							log.warn("Tracking not found PO: {}  UNFI: {}",
									purchaseOrder, unfiOrderNo);
						}
						log.info("Tracking details: {}", orderStatus);

						detail.setSupplierOrderStatus(orderStatus.toString());
						if (orderStatus.isShipped()) {
							detail.setOimOrderStatuses(new OimOrderStatuses(
									OimConstants.ORDER_STATUS_SHIPPED));
							for (salesmachine.oim.suppliers.modal.TrackingData td : orderStatus
									.getTrackingData()) {
								Message message = new Message();
								message.setMessageID(BigInteger
										.valueOf(msgId++));
								envelope.getMessage().add(message);
								OrderFulfillment fulfillment = new OrderFulfillment();
								message.setOrderFulfillment(fulfillment);
								fulfillment.setAmazonOrderID(detail
										.getOimOrders().getStoreOrderId());
								fulfillment.setMerchantFulfillmentID(BigInteger
										.valueOf(detail.getOimOrders()
												.getOrderId().longValue()));
								fulfillment
										.setFulfillmentDate(td.getShipDate());
								Item i = new Item();
								i.setAmazonOrderItemCode(detail
										.getStoreOrderItemId());
								i.setQuantity(BigInteger.valueOf(td
										.getQuantity()));
								i.setMerchantFulfillmentItemID(BigInteger
										.valueOf(detail.getDetailId()));
								FulfillmentData value = new FulfillmentData();
								// value.setCarrierCode(orderStatus.getTrackingData().getCarrierCode());
								value.setCarrierName(td.getCarrierName());
								value.setShipperTrackingNumber(td
										.getShipperTrackingNumber());
								value.setShippingMethod(td.getShippingMethod());
								fulfillment.getItem().add(i);
								fulfillment.setFulfillmentData(value);
							}
						}
						session.update(detail);
						tx.commit();

					} catch (Exception e) {
						if (tx != null && tx.isActive()) {
							tx.rollback();
						}
					}
				}
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				if (marshaller != null) {
					try {
						marshaller.marshal(envelope, os);
					} catch (JAXBException e) {
						log.error(e.getMessage(), e);
						throw new ChannelOrderFormatException(
								"Error in Updating Store order - "
										+ e.getMessage(), e);
					}
				}
				InputStream inputStream = new ByteArrayInputStream(
						os.toByteArray());
				submitFeedRequest.setFeedContent(inputStream);
				try {
					submitFeedRequest.setContentMD5(Base64
							.encode((MessageDigest.getInstance("MD5").digest(os
									.toByteArray()))));
				} catch (NoSuchAlgorithmException e) {
					log.error(e.getMessage(), e);
					throw new ChannelCommunicationException(
							"Error in submiting feed request while updating order to store - "
									+ e.getMessage(), e);
				}
				log.info("SubmitFeedRequest: {}", os.toString());
				SubmitFeedResponse submitFeed = null;
				try {
					submitFeed = service.submitFeed(submitFeedRequest);
					log.info(submitFeed.toXML());

				} catch (MarketplaceWebServiceException e) {
					log.error(e.getMessage(), e);
					throw new ChannelCommunicationException(
							"Error in submiting feed request while updating order to store - "
									+ e.getMessage(), e);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		log.info("Total PO Found {}, Shipped PO{}", totalValidPO, shippedPO);
		serializeMap();
	}

	public static void updateFromTracking() {
		List<FtpDetails> ftpList = new ArrayList<FtpDetails>(2);

		FtpDetails ftpDetails2 = new FtpDetails();
		ftpDetails2.setUrl("ftp1.unfi.com");
		ftpDetails2.setAccountNumber("40968");
		ftpDetails2.setUserName("evox");
		ftpDetails2.setPassword("evoftp093!");
		ftpList.add(ftpDetails2);

		FtpDetails ftpDetails1 = new FtpDetails();
		ftpDetails1.setUrl("ftp1.unfi.com");
		ftpDetails1.setAccountNumber("70757");
		ftpDetails1.setUserName("70757");
		ftpDetails1.setPassword("vU!6akAB");
		ftpList.add(ftpDetails1);
		for (FtpDetails ftpDetails : ftpList) {

			FTPClient ftp = new FTPClient();
			Session session = SessionManager.currentSession();

			SubmitFeedRequest submitFeedRequest = new SubmitFeedRequest();
			submitFeedRequest.setMerchant(sellerId);
			submitFeedRequest.setMWSAuthToken(mwsAuthToken);
			submitFeedRequest
					.setMarketplaceIdList(new IdList(marketPlaceIdList));
			submitFeedRequest.setFeedType("_POST_ORDER_FULFILLMENT_DATA_");

			Marshaller marshaller = null;
			try {
				marshaller = jaxbContext2.createMarshaller();
			} catch (JAXBException e) {
				log.error(e.getMessage(), e);

			}
			AmazonEnvelope envelope = new AmazonEnvelope();
			Header header = new Header();
			header.setDocumentVersion("1.01");
			header.setMerchantIdentifier(sellerId);
			envelope.setHeader(header);
			envelope.setMessageType("OrderFulfillment");
			long msgId = 1L;

			Transaction tx = null;

			try {

				int channelId = 2941;
				IOrderImport iOrderImport = OrderImportManager
						.getIOrderImport(channelId);

				if (!iOrderImport.init(channelId,
						SessionManager.currentSession())) {
					log.debug("Failed initializing the channel with Id:{}",
							channelId);
				}

				ftp.setRemoteHost(ftpDetails.getUrl());
				ftp.setDetectTransferMode(true);
				ftp.connect();
				ftp.login(ftpDetails.getUserName(), ftpDetails.getPassword());
				ftp.setTimeout(60 * 1000 * 60 * 7);
				FTPFile[] ftpFiles = ftp.dirDetails("tracking");
				Arrays.sort(ftpFiles, new Comparator<FTPFile>() {
					public int compare(FTPFile f1, FTPFile f2) {
						return f2.lastModified().compareTo(f1.lastModified());
					}
				});
				for (FTPFile ftpFile : ftpFiles) {
					try {

						log.info(ftpFile.getName());
						if (ftpFile.getName().equals(".")
								|| ftpFile.getName().equals("..")
								|| ftpFile.getName().endsWith("S.txt"))
							continue;

						byte[] bs = ftp.get("tracking/" + ftpFile.getName());
						Unmarshaller unmarshaller = jaxbContext
								.createUnmarshaller();
						String s = new String(bs);
						StringReader reader = new StringReader(s);
						// log.info(s);
						TrackingData orderTrackingResponse = (TrackingData) unmarshaller
								.unmarshal(reader);
						OrderStatus orderStatus = new OrderStatus();
						orderStatus.setStatus("Shipped");
						String purchaseOrder = orderTrackingResponse.getPO()
								.getPurchaseOrder();
						OimOrderDetails detail = (OimOrderDetails) session
								.createCriteria(OimOrderDetails.class)
								.add(Restrictions.eq("supplierOrderNumber",
										purchaseOrder)).uniqueResult();

						if (detail == null
								|| detail.getOimOrderStatuses().getStatusId()
										.intValue() == OimConstants.ORDER_STATUS_SHIPPED
										.intValue()
								|| detail.getOimOrderStatuses().getStatusId()
										.intValue() == OimConstants.ORDER_STATUS_MANUALLY_PROCESSED
										.intValue())
							continue;
						log.info("PONUM# {}", purchaseOrder);
						tx = session.beginTransaction();
						int perBoxQty = 1;
						if (detail.getQuantity() == orderTrackingResponse
								.getPOTracking().size()) {
							perBoxQty = 1;
						} else {
							if (detail.getQuantity()
									% orderTrackingResponse.getPOTracking()
											.size() == 0) {
								perBoxQty = detail.getQuantity()
										/ orderTrackingResponse.getPOTracking()
												.size();
							} else if (orderTrackingResponse.getPOTracking()
									.size() % detail.getQuantity() == 0)
								perBoxQty = orderTrackingResponse
										.getPOTracking().size()
										/ detail.getQuantity();
						}
						for (POTracking poTracking : orderTrackingResponse
								.getPOTracking()) {
							salesmachine.oim.suppliers.modal.TrackingData trackingData = new salesmachine.oim.suppliers.modal.TrackingData();
							if ("A".equals(orderTrackingResponse.getPO()
									.getShipVia())) {
								trackingData.setCarrierCode("UPS");
								trackingData.setCarrierName("UPS");
								trackingData.setShippingMethod("Ground");

							} else {
								trackingData
										.setCarrierName(orderTrackingResponse
												.getPO().getShipVia());
							}
							trackingData.setShipperTrackingNumber(poTracking
									.getTrackingNumber());

							trackingData.setQuantity(perBoxQty);
							// String dateshipped =
							// parseShippingConfirmation.get(SHIP_DATE);
							// FORMAT 08/11/2015 MM/DD/YYYY
							// int year =
							// Integer.parseInt(dateshipped.substring(6,
							// 10));
							// int month =
							// Integer.parseInt(dateshipped.substring(0,
							// 2));
							// int dayOfMonth =
							// Integer.parseInt(dateshipped.substring(3,
							// 5));
							trackingData.setShipDate(orderTrackingResponse
									.getPO().getDeliveryDate());
							orderStatus.addTrackingData(trackingData);
						}
						log.info("Tracking details: {}", orderStatus);
						detail.setSupplierOrderStatus(orderStatus.toString());
						if (orderStatus.isShipped())
							detail.setOimOrderStatuses(new OimOrderStatuses(
									OimConstants.ORDER_STATUS_SHIPPED));
						session.update(detail);
						/*
						 * synchronized (iOrderImport) {
						 * iOrderImport.updateStoreOrder(detail, orderStatus); }
						 */
						tx.commit();
						for (salesmachine.oim.suppliers.modal.TrackingData td : orderStatus
								.getTrackingData()) {
							Message message = new Message();
							message.setMessageID(BigInteger.valueOf(msgId++));
							envelope.getMessage().add(message);
							OrderFulfillment fulfillment = new OrderFulfillment();
							message.setOrderFulfillment(fulfillment);
							fulfillment.setAmazonOrderID(detail.getOimOrders()
									.getStoreOrderId());
							fulfillment.setMerchantFulfillmentID(BigInteger
									.valueOf(detail.getOimOrders().getOrderId()
											.longValue()));
							fulfillment.setFulfillmentDate(td.getShipDate());
							Item i = new Item();
							i.setAmazonOrderItemCode(detail
									.getStoreOrderItemId());
							i.setQuantity(BigInteger.valueOf(td.getQuantity()));
							i.setMerchantFulfillmentItemID(BigInteger
									.valueOf(detail.getDetailId()));
							FulfillmentData value = new FulfillmentData();
							// value.setCarrierCode(orderStatus.getTrackingData().getCarrierCode());
							value.setCarrierName(td.getCarrierName());
							value.setShipperTrackingNumber(td
									.getShipperTrackingNumber());
							value.setShippingMethod(td.getShippingMethod());
							fulfillment.getItem().add(i);
							fulfillment.setFulfillmentData(value);
						}
					} catch (Exception e) {
						if (tx != null && tx.isActive()) {
							tx.rollback();
						}
						log.error(e.getMessage(), e);
					}

				}
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				if (marshaller != null) {
					try {
						marshaller.marshal(envelope, os);
					} catch (JAXBException e) {
						log.error(e.getMessage(), e);
						throw new ChannelOrderFormatException(
								"Error in Updating Store order - "
										+ e.getMessage(), e);
					}
				}
				InputStream inputStream = new ByteArrayInputStream(
						os.toByteArray());
				submitFeedRequest.setFeedContent(inputStream);
				try {
					submitFeedRequest.setContentMD5(Base64
							.encode((MessageDigest.getInstance("MD5").digest(os
									.toByteArray()))));
				} catch (NoSuchAlgorithmException e) {
					log.error(e.getMessage(), e);
					throw new ChannelCommunicationException(
							"Error in submiting feed request while updating order to store - "
									+ e.getMessage(), e);
				}
				log.info("SubmitFeedRequest: {}", os.toString());
				SubmitFeedResponse submitFeed = null;
				try {
					submitFeed = service.submitFeed(submitFeedRequest);
					log.info(submitFeed.toXML());

				} catch (MarketplaceWebServiceException e) {
					log.error(e.getMessage(), e);
					throw new ChannelCommunicationException(
							"Error in submiting feed request while updating order to store - "
									+ e.getMessage(), e);
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		serializeMap();
	}
}

class FtpDetails {
	String accountNumber;
	String url;
	String userName;
	String password;
	WhareHouseType whareHouseType;

	public static enum WhareHouseType {
		PHI(OimConstants.SUPPLIER_METHOD_TYPE_HG_PHI), HVA(
				OimConstants.SUPPLIER_METHOD_TYPE_HG_HVA);
		private final int wharehouseType;

		WhareHouseType(int wharehouseType) {
			this.wharehouseType = wharehouseType;
		}

		public int getWharehouseType() {
			return wharehouseType;
		}
	}

	public FtpDetails() {
		super();
	}

	public FtpDetails(String accountNumber, String url, String userName,
			String password) {
		this.accountNumber = accountNumber;
		this.url = url;
		this.userName = userName;
		this.password = password;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public WhareHouseType getWhareHouseType() {
		return whareHouseType;
	}

	public void setWhareHouseType(WhareHouseType whareHouseType) {
		this.whareHouseType = whareHouseType;
	}

}
