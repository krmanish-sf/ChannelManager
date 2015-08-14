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
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderProcessingRule;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSupplierMethodattrValues;
import salesmachine.hibernatedb.OimSupplierMethods;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.Reps;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.impl.OrderImportManager;
import salesmachine.oim.suppliers.exception.SupplierCommunicationException;
import salesmachine.oim.suppliers.exception.SupplierConfigurationException;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.oim.suppliers.modal.hg.TrackingData;
import salesmachine.util.OimLogStream;
import salesmachine.util.StringHandle;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;

public class HonestGreen extends Supplier implements HasTracking {
	private static final String UNFIORDERNO = "UNFIORDERNO";
	private static final String PONUM = "PONUM";
	private static final String QTY_ORDERED = "QTY_ORDERED";
	private static final String QTY_SHIPPED = "QTY_SHIPPED";
	private static final String ASCII = "ASCII";
	private static final Logger log = LoggerFactory
			.getLogger(HonestGreen.class);
	// private static final byte BLANK_SPACE = ' ';
	private static final byte[] NEW_LINE = new byte[] { '\r', '\n' };
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

	@Override
	public void sendOrders(Integer vendorId, OimVendorSuppliers ovs, List orders)
			throws SupplierCommunicationException,
			SupplierConfigurationException {
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
						for (Iterator<OimSupplierMethods> itr = ovs
								.getOimSuppliers().getOimSupplierMethodses()
								.iterator(); itr.hasNext();) {
							OimSupplierMethods oimSupplierMethods = itr.next();
							if (oimSupplierMethods.getOimSupplierMethodTypes()
									.getMethodTypeId().intValue() == OimConstants.SUPPLIER_METHOD_TYPE_HG_HVA) {
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
						try {
							sendToFTP(fileName, ovs, ftpDetails, true, false);
							if (emailNotification) {
								sendEmail(emailContent, ftpDetails, fileName,
										r.getLogin());
							}
						} catch (RuntimeException e) {
							log.error(
									"could not connect to ftp server {} for user {} for HVA.",
									ftpDetails.getUrl(),
									ftpDetails.getUserName());
						}

					}
					if (PhiMap.size() > 0) {
						// create order file and send order to PHI configured
						// ftp
						FtpDetails ftpDetails = new FtpDetails();
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
						try {
							sendToFTP(fileName, ovs, ftpDetails, false, true);
							if (emailNotification) {
								sendEmail(emailContent, ftpDetails, fileName,
										r.getLogin());
							}
						} catch (Exception e) {
							log.error(
									"could not connect to ftp server {} for user {} for HVA.",
									ftpDetails.getUrl(),
									ftpDetails.getUserName());
							e.printStackTrace();
						}
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
							try {
								sendToFTP(fileName, ovs, ftpDetails, true,
										false);
								if (emailNotification) {
									sendEmail(emailContent, ftpDetails,
											fileName, r.getLogin());
								}
							} catch (Exception e) {
								log.error(
										"could not connect to ftp server {} for user {} for HVA.",
										ftpDetails.getUrl(),
										ftpDetails.getUserName());
								e.printStackTrace();
							}
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
							try {
								sendToFTP(fileName, ovs, ftpDetails, false,
										true);

								if (emailNotification) {
									sendEmail(emailContent, ftpDetails,
											fileName, r.getLogin());
								}
							} catch (Exception e) {
								log.error(
										"could not connect to ftp server {} for user {} for HVA.",
										ftpDetails.getUrl(),
										ftpDetails.getUserName());
								e.printStackTrace();
							}
						}

					}
					// ***************************************************

				} catch (RuntimeException e) {
					log.error(e.getMessage(), e);
				}
				// if (emailNotification) {
				// emailContent +=
				// "<br>Thanks, <br>Inventorysource support<br>";
				// logStream
				// .println("Sending email to user about order processing");
				// log.debug("Sending email to user about order processing");
				// EmailUtil.sendEmail(r.getLogin(),
				// "support@inventorysource.com", "",
				// "Order processing update results", emailContent,
				// "text/html");
				// }
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

		emailContent += "<br>Thanks, <br>Inventorysource support<br>";
		logStream.println("Sending email to user about order processing");
		log.debug("Sending email to user about order processing");
		EmailUtil.sendEmail(login, "support@inventorysource.com", "",
				"Order processing update results", emailContent, "text/html");

	}

	private int getPhiQuantity(String sku, Integer vendorId, int hvaQuantity) {
		Session dbSession = SessionManager.currentSession();
		Query query = dbSession
				.createSQLQuery("select QUANTITY from VENDOR_CUSTOM_FEEDS_PRODUCTS where sku=:sku and VENDOR_CUSTOM_FEED_ID=(select VENDOR_CUSTOM_FEED_ID from VENDOR_CUSTOM_FEEDS where vendor_id=:vendorID AND IS_RESTRICTED=1)");
		query.setString("sku", sku);
		query.setInteger("vendorID", vendorId);
		Object q = query.uniqueResult();
		int tempQuantity = 0;
		if (q != null)
			tempQuantity = ((Integer) q).intValue();

		return tempQuantity - hvaQuantity;
	}

	private int getHvaQuantity(String sku) {
		Session dbSession = SessionManager.currentSession();
		Query query = dbSession
				.createQuery("select p.quantity from salesmachine.hibernatedb.Product p where p.sku=:sku");
		query.setString("sku", sku);
		System.out.println(query.list());
		Object quantity = query.uniqueResult();
		dbSession.close();
		return ((Integer) quantity).intValue();
	}

	private void sendToFTP(String fileName, OimVendorSuppliers ovs,
			FtpDetails ftpDetails, boolean isHva, boolean isPhi)
			throws SupplierCommunicationException,
			SupplierConfigurationException {
		FTPClient ftp = new FTPClient();
		File file = new File(fileName);
		try {
			ftp.setRemoteHost(ftpDetails.getUrl());
			ftp.setDetectTransferMode(true);
			ftp.connect();
			ftp.login(ftpDetails.getUserName(), ftpDetails.getPassword());
			ftp.setTimeout(60 * 1000 * 60 * 5);
			ftp.put(fileName, file.getName());
			ftp.quit();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new SupplierCommunicationException(e.getMessage(), e);
		} catch (FTPException e) {
			log.error(e.getMessage(), e);
			throw new SupplierConfigurationException(e.getMessage(), e);
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
		System.out.println(query.list());
		Object restrictedIntVal = query.uniqueResult();
		if (restrictedIntVal != null
				&& ((Integer) restrictedIntVal).intValue() == 1) {
			log.info("{} is restricted in product table", sku);
			HvaMap.put(sku, orderDetail);
		} else {
			query = dbSession
					.createSQLQuery("select IS_RESTRICTED from VENDOR_CUSTOM_FEEDS_PRODUCTS where sku=:sku and VENDOR_CUSTOM_FEED_ID=(select VENDOR_CUSTOM_FEED_ID from VENDOR_CUSTOM_FEEDS where vendor_id=:vendorID)");
			query.setString("sku", sku);
			query.setInteger("vendorID", vendorID);
			Object restrictedVal = query.uniqueResult();
			if (restrictedVal != null
					&& ((BigDecimal) restrictedVal).intValue() == 1) {
				log.info(
						"{} is restricted in VENDOR_CUSTOM_FEEDS_PRODUCTS table",
						sku);
				HVAPhiMap.put(sku, orderDetail);
			} else {
				log.info("{} is not restricted in both the tables", sku);
				PhiMap.put(sku, orderDetail);
			}
		}
		// return ((Integer) restrictedIntVal).intValue() == 1 ? true : false;
		return HvaMap.size() > 0 || HVAPhiMap.size() > 0;
	}

	private Map<String, String> parseOrderConfirmation(
			Map<Integer, String> orderConfirmationMap) {
		String[] lineArray1 = orderConfirmationMap.get(1).split(",");
		Map<String, String> orderData = new HashMap<String, String>();
		orderData.put(PONUM, lineArray1[6]);
		orderData.put(UNFIORDERNO, lineArray1[0]);
		return orderData;
	}

	private Map<String, String> parseShippingConfirmation(
			Map<Integer, String> shippingConfirmationMap) {
		String[] lineArray4 = shippingConfirmationMap.get(4).split(",");
		String[] lineArray1 = shippingConfirmationMap.get(1).split(",");
		Map<String, String> orderData = new HashMap<String, String>();
		orderData.put(QTY_ORDERED, lineArray4[3]);
		orderData.put(QTY_SHIPPED, lineArray4[4]);
		orderData.put(SHIP_DATE, lineArray1[lineArray1.length - 1]);
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
		log.info("created file name for HG:{}", f.getName());
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

	private String createOrderFile(OimOrders order, OimVendorSuppliers ovs,
			Map<Integer, OimOrderDetails> detailMap, FtpDetails ftpDetails,
			boolean isAmazon) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
		String uploadfilename = "HG_" + ftpDetails.getAccountNumber() + "_"
				+ sdf.format(new Date()) + ".txt";
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
				poNum = order.getStoreOrderId();
			else
				poNum = ovs.getVendors().getVendorId() + "-"
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
		clearCache();
		log.info("Tracking request for PONUM: {}", trackingMeta);
		if (!(trackingMeta instanceof String))
			throw new IllegalArgumentException(
					"trackingMeta is expected to be a String value containing UNFI Order number.");
		OrderStatus orderStatus = new OrderStatus();
		orderStatus.setStatus("Sent to supplier.");
		// getting item sku by which we can distinguish that this item can be
		// track from hva or phi location.
		Session session = SessionManager.currentSession();

		OimOrderDetails detail = (OimOrderDetails) session
				.createCriteria(OimOrderDetails.class)
				.add(Restrictions.eq("supplierOrderNumber",
						trackingMeta.toString())).uniqueResult();

		if (detail != null) {
			List<OimOrders> oimOrderList = new ArrayList<OimOrders>();
			oimOrderList.add(detail.getOimOrders());
			boolean isAmazon = isOrderFromAmazonStore(oimOrderList);
			String tempTrackingMeta = isAmazon ? new String(
					(String) trackingMeta) : ovs.getVendors().getVendorId()
					+ "-" + new String((String) trackingMeta);

			Map<String, FtpDetails> ftpDetailMap = new HashMap<String, FtpDetails>();
			isRestricted(detail.getSku(), detail, ovs.getVendors()
					.getVendorId());
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

			if (ftpDetailMap != null && ftpDetailMap.size() > 0) {
				for (Iterator<FtpDetails> itr = ftpDetailMap.values()
						.iterator(); itr.hasNext();) {
					FtpDetails ftpDetails = itr.next();
					try {
						FTPClient ftp = new FTPClient();
						ftp.setRemoteHost(ftpDetails.getUrl());
						ftp.setDetectTransferMode(true);
						ftp.connect();
						ftp.login(ftpDetails.getUserName(),
								ftpDetails.getPassword());
						ftp.setTimeout(60 * 1000 * 60 * 7);
						FTPFile[] ftpFiles = ftp.dirDetails("confirmations");
						Arrays.sort(ftpFiles, new Comparator<FTPFile>() {
							public int compare(FTPFile f1, FTPFile f2) {
								return f2.lastModified().compareTo(
										f1.lastModified());
							}
						});
						for (FTPFile ftpFile : ftpFiles) {
							String confirmationFile = ftpFile.getName();
							if (confirmationFile.equals("..")
									|| confirmationFile.equals("."))
								continue;

							log.info(
									"Confirmation file path: {} Last Modified: {} Order Processing Time: {}",
									ftpFile.getName(), ftpFile.lastModified(),
									detail.getProcessingTm());
							if (ftpFile.lastModified().before(
									detail.getProcessingTm()))
								break;
							byte[] confirmationFileData = ftp
									.get("confirmations/" + confirmationFile);
							Map<Integer, String> orderDataMap = parseFileData(confirmationFileData);
							Map<String, String> orderData = parseOrderConfirmation(orderDataMap);
							log.info("Order Confirmation details found for {}",
									orderData);
							if (tempTrackingMeta.toString().equals(
									orderData.get(PONUM))) {
								log.debug("Order Invoice Data Found");
								String unfiOrderNo = orderData.get(UNFIORDERNO);
								String trackingFilePath = getTrackingFilePath(
										ftpDetails.getAccountNumber(),
										unfiOrderNo);
								orderStatus.setStatus("In-Process");
								String shippingFilePath = getShippingFilePath(
										ftpDetails.getAccountNumber(),
										unfiOrderNo);
								byte[] shippingFileData = ftp
										.get(shippingFilePath);
								Map<Integer, String> shippingDataMap = parseFileData(shippingFileData);
								Map<String, String> parseShippingConfirmation = parseShippingConfirmation(shippingDataMap);
								try {
									byte[] trackingFileData;
									try {
										trackingFileData = ftp
												.get(trackingFilePath);
									} catch (FTPException e) {
										log.error(e.getMessage(), e);
										trackingFileData = null;
									}
									if (trackingFileData != null) {
										Unmarshaller unmarshaller = jaxbContext
												.createUnmarshaller();
										String s = new String(trackingFileData);
										StringReader reader = new StringReader(
												s);
										log.info(s);
										TrackingData orderTrackingResponse = (TrackingData) unmarshaller
												.unmarshal(reader);
										orderStatus.setStatus("Shipped");
										salesmachine.oim.suppliers.modal.TrackingData trackingData = new salesmachine.oim.suppliers.modal.TrackingData();
										if ("A".equals(orderTrackingResponse
												.getPO().getShipVia())) {
											trackingData.setCarrierCode("UPS");
											trackingData.setCarrierName("UPS");
											trackingData
													.setShippingMethod("Ground");

										} else {
											trackingData
													.setCarrierName(orderTrackingResponse
															.getPO()
															.getShipVia());
										}
										trackingData
												.setShipperTrackingNumber(orderTrackingResponse
														.getPOTracking()
														.getTrackingNumber());

										trackingData
												.setQuantity(Integer
														.parseInt(parseShippingConfirmation
																.get(QTY_SHIPPED)));
										String dateshipped = parseShippingConfirmation
												.get(SHIP_DATE);
										// FORMAT 08/11/2015 MM/DD/YYYY
										int year = Integer.parseInt(dateshipped
												.substring(6, 10));
										int month = Integer
												.parseInt(dateshipped
														.substring(0, 2));
										int dayOfMonth = Integer
												.parseInt(dateshipped
														.substring(3, 5));
										trackingData
												.setShipDate(new GregorianCalendar(
														year, month, dayOfMonth));
										orderStatus
												.addTrackingData(trackingData);
										log.info("Tracking details: {} {}",
												orderTrackingResponse.getPO()
														.getShipVia(),
												orderTrackingResponse
														.getPOTracking()
														.getTrackingNumber());
									} else {
										log.info(
												"Tracking Details not updated for - {}",
												trackingMeta.toString());
									}
								} catch (JAXBException e) {
									log.error(
											"Tracking XML recieved from server could not be parsed.",
											e);
								}
								break;
							}
						}
						ftp.quit();
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		}
		return orderStatus;
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
					log.info("found configured HVA Location ");
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
					log.info("found configured PHI Location ");
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
}

class FtpDetails {
	String accountNumber;
	String url;
	String userName;
	String password;

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

}
