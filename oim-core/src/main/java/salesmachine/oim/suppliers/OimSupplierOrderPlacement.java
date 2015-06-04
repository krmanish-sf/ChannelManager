package salesmachine.oim.suppliers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimFields;
import salesmachine.hibernatedb.OimFileFieldMap;
import salesmachine.hibernatedb.OimFiletypes;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderStatuses;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSupplierMethods;
import salesmachine.hibernatedb.OimSuppliers;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.OimVendorsuppOrderhistory;
import salesmachine.hibernatedb.Reps;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.PojoHelper;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.impl.OrderImportManager;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.orderfile.DatabaseFile;
import salesmachine.orderfile.DefaultCsvFile;
import salesmachine.orderfile.DefaultXlsFile;
import salesmachine.orderfile.OrderFile;
import salesmachine.util.FtpFileUploader;
import salesmachine.util.OimLogStream;
import salesmachine.util.StringHandle;

public class OimSupplierOrderPlacement {
	private static Logger log = LoggerFactory
			.getLogger(OimSupplierOrderPlacement.class);
	private final Session m_dbSession;
	private OimSuppliers m_supplier;
	private OimSupplierMethods m_supplierMethods;

	public static final int PCS = 1;
	public static final int MOBILELINE = 2;
	public static final int PETRA = 3;
	public static final int ICELLA = 61;
	public static final int PCI = 63;
	public static final int MOTENG = 221;
	public static final int DRBOTT = 161;
	public static final int RAX = 261;
	public static final int DROPSHIPDIRECT = 381;
	public static final int DROPSHIPDIRECTAUTOMOTIVES = 401;
	public static final int BnF = 481;
	public static final int DandH = 421;
	public static final int BRADLEYCALDWELL = 581;
	public static final int HONESTGREEN = 1822;
	public static final int GREENSUPPLY = 1981;

	// public boolean m_RunModePlaceOrders = true;
	// public boolean m_RunModeUpdateOrdersStatus = true;
	public float m_RunModeFailPct = 20;
	@Deprecated
	protected OimLogStream logStream;

	public static final Integer ERROR_NONE = 0;
	public static final Integer ERROR_UNCONFIGURED_SUPPLIER = 1;
	public static final Integer ERROR_PING_FAILURE = 2;
	public static final Integer ERROR_ORDER_PROCESSING = 3;

	public OimSupplierOrderPlacement(Session dbSession) {
		this.m_dbSession = dbSession;
		logStream = new OimLogStream();
	}

	@Deprecated
	public boolean init(int supplierId, Session dbSession, OimLogStream ols) {
		if (ols != null) {
			logStream = ols;
		}
		if (logStream == null) {
			logStream = new OimLogStream();
		}
		return init(supplierId);
	}

	private boolean init(int supplierId) {
		Transaction tx = null;
		try {
			tx = m_dbSession.beginTransaction();

			// Here is your db code
			Query query = m_dbSession
					.createQuery("from salesmachine.hibernatedb.OimSuppliers as s where s.supplierId=:id and s.deleteTm is null");
			query.setInteger("id", supplierId);
			if (!query.iterate().hasNext()) {
				log.debug("No supplier found for supplier id: " + supplierId);
				return false;
			} else {
				m_supplier = (OimSuppliers) query.iterate().next();

				query = m_dbSession
						.createQuery("select m from OimSupplierMethods as m left join m.oimSupplierMethodattrValueses v where m.deleteTm is null and m.oimSuppliers=:supp and m.oimSupplierMethodTypes.methodTypeId=:methodTypeId and v.deleteTm is null");
				query.setEntity("supp", m_supplier);
				query.setInteger("methodTypeId",
						OimConstants.SUPPLIER_METHOD_TYPE_ORDERPUSH.intValue());
				Iterator it = query.iterate();
				if (!it.hasNext()) {
					logStream
							.println("No method found for pushing orders to this supplier");
					return false;
				}
				m_supplierMethods = (OimSupplierMethods) it.next();
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		} finally {
			tx.rollback();
		}
		return true;
	}

	public boolean placeOrders() {
		Transaction tx = null;
		List vendorIds = new ArrayList();
		try {
			tx = m_dbSession.beginTransaction();

			// Here is your db code
			Query query = m_dbSession
					.createQuery("select distinct c.vendors.vendorId from salesmachine.hibernatedb.OimChannels as c, "
							+ "salesmachine.hibernatedb.OimOrderBatches as b, "
							+ "salesmachine.hibernatedb.OimOrders as o, "
							+ "salesmachine.hibernatedb.OimOrderDetails as d "
							+ "where d.oimSuppliers=:supp and d.deleteTm is null and d.oimOrderStatuses=:status "
							+ "and b.oimChannels = c and o.oimOrderBatches = b and d.oimOrders = o");
			query.setEntity("supp", m_supplier);
			query.setEntity("status", new OimOrderStatuses(
					OimConstants.ORDER_STATUS_UNPROCESSED));

			for (Iterator it = query.iterate(); it.hasNext();) {
				Integer vendorId = (Integer) it.next();
				vendorIds.add(vendorId);
			}
			tx.commit();
		} catch (RuntimeException e) {
			tx.rollback();
			e.printStackTrace();
		}

		for (Iterator it = vendorIds.iterator(); it.hasNext();) {
			Integer vendorId = (Integer) it.next();
			log.debug("Processing orders for vendor: " + vendorId);
			processVendorOrders(vendorId);
		}

		return true;
	}

	private void updateVendorSupplierOrderHistory(Integer vendorId,
			Integer errorCode, String description) {
		Transaction tx = null;
		try {
			tx = m_dbSession.beginTransaction();

			Vendors vendors = new Vendors(vendorId);
			OimVendorsuppOrderhistory history = new OimVendorsuppOrderhistory();
			history.setDescription(description);
			history.setErrorCode(errorCode);
			history.setOimSuppliers(m_supplier);
			history.setVendors(vendors);
			history.setProcessingTm(new Date());
			m_dbSession.save(history);

			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		}
	}

	public boolean processVendorOrders(Integer vendorId) {
		return processVendorOrders(vendorId, "");
	}

	public boolean processVendorOrder(Integer vendorId, OimOrders oimOrders) {
		log.debug("Processing orders for vendor id: " + vendorId);
		// Transaction tx = null;
		boolean ordersSent = false;
		try {

			List orders = new ArrayList();
			List<Integer> successfulOrders = new ArrayList<Integer>();
			List<Integer> failedOrders = new ArrayList<Integer>();
			int countToProcess = 0;
			try {
				// tx = m_dbSession.beginTransaction();
				// Removed the order details which have been processed

				Set unprocessedDetails = new HashSet();
				for (Iterator detailIt = oimOrders.getOimOrderDetailses()
						.iterator(); detailIt.hasNext();) {
					OimOrderDetails detail = (OimOrderDetails) detailIt.next();
					if (OimConstants.ORDER_STATUS_UNPROCESSED.equals(detail
							.getOimOrderStatuses().getStatusId())) {
						unprocessedDetails.add(detail);
						countToProcess++;
					}
				}
				oimOrders.setOimOrderDetailses(unprocessedDetails);
				// tx.commit();
				orders.add(oimOrders);
			} catch (RuntimeException e) {
				// tx.rollback();
				e.printStackTrace();
			}
			if (countToProcess == 0) {
				return true;
			}
			log.debug("Number of order details to process: " + countToProcess);

			try {
				// tx = m_dbSession.beginTransaction();
				log.debug("Sending orders to the supplier");
				sendOrderToSupplier(vendorId, oimOrders, successfulOrders,
						failedOrders);
				// tx.commit();
			} catch (RuntimeException e) {
				log.error(e.getMessage(), e);
			}

			updateOrderStatus(successfulOrders,
					OimConstants.ORDER_STATUS_PROCESSED_SUCCESS);
			updateOrderStatus(failedOrders,
					OimConstants.ORDER_STATUS_PROCESSED_FAILED);
			if (failedOrders.size() == 0) {
				updateVendorSupplierOrderHistory(vendorId, ERROR_NONE, "");
				ordersSent = true;
			} else
				ordersSent = false;
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
			return false;
		}
		return ordersSent;
	}

	@Deprecated
	public boolean processVendorOrders(Integer vendorId, String orderIds) {
		log.debug("Processing orders for vendor id: " + vendorId);
		Transaction tx = null;
		try {
			OimVendorSuppliers ovs = null;
			boolean ping = false;
			StringBuffer errorMessage = new StringBuffer();
			Query query;
			try {
				tx = m_dbSession.beginTransaction();

				// Here is your db code
				query = m_dbSession
						.createQuery("from OimVendorSuppliers s where s.oimSuppliers=:supp and s.vendors.vendorId=:vid and s.deleteTm is null");
				query.setEntity("supp", m_supplier);
				query.setInteger("vid", vendorId.intValue());
				Iterator it = query.iterate();
				if (it.hasNext()) {
					ovs = (OimVendorSuppliers) it.next();
					ping = pingSupplier(vendorId, ovs, errorMessage);
				} else
					return false;
				tx.commit();
			} catch (RuntimeException e) {
				tx.rollback();
				e.printStackTrace();
			}

			if (ovs == null || ovs.getTestMode() > 0) {
				updateVendorSupplierOrderHistory(vendorId, ERROR_PING_FAILURE,
						"Supplier is not configured for this vendor");
				log.debug("Supplier is not configured for this vendor");
				return false;
			} else if (!ping) {
				updateVendorSupplierOrderHistory(vendorId, ERROR_PING_FAILURE,
						errorMessage.toString());
				log.debug("Could not connect to the supplier: "
						+ ovs.getOimSuppliers().getSupplierName());
				log.debug(errorMessage.toString());
				return false;
			}

			List successfulOrders = new ArrayList();
			List failedOrders = new ArrayList();
			List orders = new ArrayList();

			int countToProcess = 0;
			try {
				tx = m_dbSession.beginTransaction();

				String queryStr = "select distinct o from salesmachine.hibernatedb.OimOrders as o "
						+ "inner join o.oimOrderDetailses d where o.oimOrderBatches.oimChannels.vendors.vendorId=:vid "
						+ "and d.oimSuppliers=:supp and d.deleteTm is null and d.oimOrderStatuses=:status";
				if (orderIds != null && orderIds.length() > 0)
					queryStr += " and o.orderId in (" + orderIds + ")";

				query = m_dbSession.createQuery(queryStr);
				query.setEntity("supp", m_supplier);
				query.setEntity("status", new OimOrderStatuses(
						OimConstants.ORDER_STATUS_UNPROCESSED));
				query.setInteger("vid", vendorId.intValue());
				orders = query.list();

				// Removed the order details which have been processed
				for (int i = 0; i < orders.size(); i++) {
					OimOrders order = (OimOrders) orders.get(i);
					Set unprocessedDetails = new HashSet();
					for (Iterator detailIt = order.getOimOrderDetailses()
							.iterator(); detailIt.hasNext();) {
						OimOrderDetails detail = (OimOrderDetails) detailIt
								.next();
						if (OimConstants.ORDER_STATUS_UNPROCESSED.equals(detail
								.getOimOrderStatuses().getStatusId())
								&& detail.getOimSuppliers().getSupplierId()
										.equals(m_supplier.getSupplierId())) {
							unprocessedDetails.add(detail);
							countToProcess++;
						}
					}
					order.setOimOrderDetailses(unprocessedDetails);
				}

				tx.commit();
			} catch (RuntimeException e) {
				tx.rollback();
				e.printStackTrace();
			}

			log.debug("Number of order details to process: " + countToProcess);
			if (countToProcess == 0)
				return true;

			boolean ordersSent = true;
			try {
				tx = m_dbSession.beginTransaction();
				log.debug("Sending orders to the supplier");
				ordersSent = sendOrdersToSupplier(vendorId, ovs, orders,
						successfulOrders, failedOrders);
				tx.commit();
			} catch (RuntimeException e) {
				e.printStackTrace();
				if (tx != null)
					tx.rollback();
			}
			log.debug("Successful orders: " + successfulOrders.size());
			log.debug("Failed orders: " + failedOrders.size());

			if (ordersSent) {
				logStream
						.println("Updating orders status to placed in the database");
				updateOrderStatus(successfulOrders,
						OimConstants.ORDER_STATUS_PROCESSED_SUCCESS);
				updateOrderStatus(failedOrders,
						OimConstants.ORDER_STATUS_PROCESSED_FAILED);
			} else {
				logStream
						.println("Sending order failed. Not updating order statuses");
			}

			if (failedOrders.size() == 0) {
				updateVendorSupplierOrderHistory(vendorId, ERROR_NONE, "");
			} else
				return false;
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		return true;
	}

	private boolean pingSupplier(Integer vendorId, OimVendorSuppliers ovs,
			StringBuffer errorMessage) {
		// init(ovs.getOimSuppliers().getSupplierId());
		Integer supplierMethodNameId = m_supplierMethods
				.getOimSupplierMethodNames().getMethodNameId();
		if (OimConstants.SUPPLIER_METHOD_NAME_EMAIL
				.equals(supplierMethodNameId)) {
			String emailAddress = PojoHelper.getSupplierMethodAttributeValue(
					m_supplierMethods,
					OimConstants.SUPPLIER_METHOD_ATTRIBUTES_EMAILADDRESS);
			// May need to handle the case of bad email in the future

			return true;
		}

		if (OimConstants.SUPPLIER_METHOD_NAME_FTP.equals(supplierMethodNameId)) {
			// Verify the ftp credentials
			String ftpServer = PojoHelper.getSupplierMethodAttributeValue(
					m_supplierMethods,
					OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPSERVER);
			String ftpLogin = PojoHelper.getSupplierMethodAttributeValue(
					m_supplierMethods,
					OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPLOGIN);
			if ("#accountspecific".equals(ftpLogin))
				ftpLogin = ovs.getLogin();
			String ftpPassword = PojoHelper.getSupplierMethodAttributeValue(
					m_supplierMethods,
					OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPPASSWORD);
			if ("#accountspecific".equals(ftpPassword))
				ftpPassword = ovs.getPassword();
			String ftpFolder = PojoHelper.getSupplierMethodAttributeValue(
					m_supplierMethods,
					OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPFOLDER);

			FtpFileUploader uploader = new FtpFileUploader(ftpServer, ftpLogin,
					ftpPassword, 5, 0);
			String txt = uploader.testConnection();

			if (txt != null) {
				errorMessage
						.append("Could not connect to the Supplier FTP Server with \nServer:"
								+ ftpServer
								+ "\nLogin:"
								+ ftpLogin
								+ "\nPassword:"
								+ ftpPassword
								+ "\n\nMessage:"
								+ txt);
				return false;
			}

			return true;
		}

		if (OimConstants.SUPPLIER_METHOD_NAME_CUSTOM
				.equals(supplierMethodNameId)) {
			log.debug("!!! Ping custom supplier");
			return true;
		}

		return false;
	}

	private boolean sendOrderToSupplier(Integer vendorId, OimOrders oimOrders,
			List<Integer> successfulOrders, List<Integer> failedOrders) {
		List<OimOrders> orders = new ArrayList<OimOrders>();
		orders.add(oimOrders);
		for (OimOrderDetails oimOrderDetails : (Set<OimOrderDetails>) oimOrders
				.getOimOrderDetailses()) {
			init(oimOrderDetails.getOimSuppliers().getSupplierId());
			OimVendorSuppliers ovs = null;
			boolean ping = false;
			StringBuffer errorMessage = new StringBuffer();
			Query query;
			Transaction tx = null;
			try {
				tx = m_dbSession.beginTransaction();

				// Here is your db code
				query = m_dbSession
						.createQuery("from OimVendorSuppliers s where s.oimSuppliers=:supp and s.vendors.vendorId=:vid and s.deleteTm is null");
				query.setEntity("supp", m_supplier);
				query.setInteger("vid", vendorId);
				Iterator it = query.iterate();
				if (it.hasNext()) {
					ovs = (OimVendorSuppliers) it.next();
					ping = pingSupplier(vendorId, ovs, errorMessage);
				} else {
					return false;
				}
				tx.commit();
			} catch (RuntimeException e) {
				if (tx != null && tx.isActive())
					tx.rollback();
				e.printStackTrace();
			}

			Integer supplierMethodNameId = m_supplierMethods
					.getOimSupplierMethodNames().getMethodNameId();
			log.debug("Supplier Method Id: " + supplierMethodNameId);
			if (OimConstants.SUPPLIER_METHOD_NAME_CUSTOM
					.equals(supplierMethodNameId)) {
				log.debug("!!! Custom method called");
				Supplier s = null;
				int supplierId = ovs.getOimSuppliers().getSupplierId();
				switch (supplierId) {
				case PCS:
					log.debug("!!! SENDING ORDERS TO PCS");
					s = new PCS();
					break;
				case MOBILELINE:
					log.debug("!!! SENDING ORDERS TO Mobileline");
					s = new Mobileline();
					break;
				case PETRA:
					log.debug("!!! SENDING ORDERS TO Petra");
					s = new Petra();
					break;
				case ICELLA:
					log.debug("!!! SENDING ORDERS TO iCella");
					s = new Icella();
					break;
				case PCI:
					log.debug("!!! SENDING ORDERS TO Progressive Concepts");
					s = new ProgressiveConcepts();
					break;
				case MOTENG:
					log.debug("!!! SENDING ORDERS TO Moteng");
					s = new Moteng();
					break;
				case DRBOTT:
					log.debug("!!! SENDING ORDERS TO DrBott");
					s = new DrBott();
					break;
				case RAX:
					log.debug("!!! SENDING ORDERS TO Rax");
					s = new Rax();
					break;
				case DROPSHIPDIRECT:
					log.debug("!!! SENDING ORDERS TO Dropship Direct");
					s = new DropshipDirect();
					break;
				case DROPSHIPDIRECTAUTOMOTIVES:
					logStream
							.println("!!! SENDING ORDERS TO Dropship Direct Automotives");
					s = new DropshipDirect();
					break;
				case BnF:
					log.debug("!!! SENDING ORDERS TO BnF USA");
					s = new BF();
					break;
				case DandH:
					log.debug("!!! SENDING ORDERS TO DandH");
					s = new DandH();
					break;
				case BRADLEYCALDWELL:
					log.debug("!!! SENDING ORDERS TO BRADLEYCALDWELL");
					s = new BradleyCaldwell();
					break;
				case HONESTGREEN:
					log.debug("SENDING ORDERS TO HONEST GREEN");
					s = new HonestGreen();
					break;
				case GREENSUPPLY:
					log.debug("SENDING ORDERS TO GREENSUPPLY");
					s = new GreenSupply();
					break;
				}
				if (s != null) {
					s.setLogStream(logStream);
					s.sendOrders(vendorId, ovs, orders);
					successfulOrders.addAll(s.successfulOrders);
					failedOrders.addAll(s.failedOrders);
				} else {
					log.debug("Unknown Supplier Id: " + supplierId);
				}
			} else if (OimConstants.SUPPLIER_METHOD_NAME_EMAIL
					.equals(supplierMethodNameId)
					|| OimConstants.SUPPLIER_METHOD_NAME_FTP
							.equals(supplierMethodNameId)) {
				String tmp = PojoHelper.getSupplierMethodAttributeValue(
						m_supplierMethods,
						OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FILETYPEID);

				// Find if there is an associated file
				OimFiletypes oimFile = null;
				if (tmp != null && tmp.length() > 0) {
					Integer fileid = Integer.valueOf(tmp);
					log.debug("Using File Id: " + fileid
							+ " for generating supplier order file");
					query = m_dbSession
							.createQuery("select f from OimFiletypes f where f.fileTypeId=:fileId and f.deleteTm is null");
					Iterator it = query.setInteger("fileId", fileid.intValue())
							.iterate();
					if (!it.hasNext()) {
						logStream
								.println("No file defined for this supplier file id: "
										+ fileid);
					} else {
						oimFile = (OimFiletypes) it.next();
					}
				}

				int fileFormat = 1;
				try {
					fileFormat = Integer
							.parseInt(PojoHelper
									.getSupplierMethodAttributeValue(
											m_supplierMethods,
											OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FILEFORMAT));
				} catch (Exception e) {
					e.printStackTrace();
					fileFormat = 1;
				}

				OrderFile ofile = null;
				if (fileFormat == OimConstants.FILE_FORMAT_XLS) {
					ofile = new DefaultXlsFile(m_dbSession);
				} else {
					ofile = new DefaultCsvFile(m_dbSession);
				}
				if (oimFile != null) {
					ofile = new DatabaseFile(m_dbSession, oimFile);
				}
				ofile.build();

				String name = (String) ofile.getFileFormatParams().get(
						OimConstants.FILE_FORMAT_PARAMS_NAME);
				if (name != null && name.length() > 0) {
					if (name.indexOf("#SupplierAccountNumber") != -1) {
						String accountNumber = ovs.getAccountNumber();
						name = name.replaceAll("#SupplierAccountNumber",
								accountNumber);
					}
				}
				String fileName = "";
				if (fileFormat == OimConstants.FILE_FORMAT_XLS) {
					fileName = "" + vendorId + "-" + m_supplier.getSupplierId()
							+ ".xls";
					log.debug("Generating file " + fileName);
					try {
						generateXlsFile(orders, ofile.getFileFieldMaps(),
								fileName, ofile.getFileFormatParams(),
								ofile.getSpecificsProvider(ovs));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						log.debug(e.getMessage());
						e.printStackTrace();
					}
				} else if (fileFormat == OimConstants.FILE_FORMAT_CSV) {
					fileName = "" + vendorId + "-" + m_supplier.getSupplierId()
							+ ".csv";
					if (name != null && name.length() > 0)
						fileName = name;
					log.debug("Generating file " + fileName);
					try {
						generateCsvFile(orders, ofile.getFileFieldMaps(),
								fileName, ofile.getFileFormatParams(),
								ofile.getSpecificsProvider(ovs));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						log.debug(e.getMessage());
						e.printStackTrace();
					}
				}

				// Get the reps object to get the email id and name of the
				// vendor to
				// whom the order status email will go in case the
				// emaiNotification
				// is set for him.
				Query q = m_dbSession
						.createQuery("select r from salesmachine.hibernatedb.Reps r where r.vendorId = "
								+ vendorId);
				Reps r = new Reps();
				Iterator repsIt = q.iterate();
				if (repsIt.hasNext()) {
					r = (Reps) repsIt.next();
				}

				if (OimConstants.SUPPLIER_METHOD_NAME_FTP
						.equals(supplierMethodNameId)) {
					String ftpServer = PojoHelper
							.getSupplierMethodAttributeValue(
									m_supplierMethods,
									OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPSERVER);
					String ftpLogin = PojoHelper
							.getSupplierMethodAttributeValue(
									m_supplierMethods,
									OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPLOGIN);
					String ftpPassword = PojoHelper
							.getSupplierMethodAttributeValue(
									m_supplierMethods,
									OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPPASSWORD);

					if ("#accountspecific".equals(ftpLogin))
						ftpLogin = ovs.getLogin();
					if ("#accountspecific".equals(ftpPassword))
						ftpPassword = ovs.getPassword();

					String ftpFolder = PojoHelper
							.getSupplierMethodAttributeValue(
									m_supplierMethods,
									OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPFOLDER);

					FtpFileUploader uploader = new FtpFileUploader(ftpServer,
							ftpLogin, ftpPassword, 5, 0);
					try {
						uploader.Upload(ftpFolder, fileName, fileName);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						log.debug(e.getMessage());
						e.printStackTrace();
						return false;
					}

				} else if (OimConstants.SUPPLIER_METHOD_NAME_EMAIL
						.equals(supplierMethodNameId)) {
					String emailAddress = PojoHelper
							.getSupplierMethodAttributeValue(
									m_supplierMethods,
									OimConstants.SUPPLIER_METHOD_ATTRIBUTES_EMAILADDRESS);
					if (fileFormat == OimConstants.FILE_FORMAT_SEND_PLAIN_TEXT_IN_EMAIL) {
						try {
							String vendor_name = StringHandle.removeNull(r
									.getFirstName())
									+ " "
									+ StringHandle.removeNull(r.getLastName());
							String emailContent = "Dear " + vendor_name
									+ "<br>";
							emailContent += "<br>Following is the status of the orders processed for the supplier "
									+ ovs.getOimSuppliers().getSupplierName()
									+ " : - <br>";
							emailContent += generateMailBody(orders,
									ofile.getFileFieldMaps(),
									new StandardFileSpecificsProvider(
											m_dbSession, ovs, new Vendors(
													vendorId)));
							EmailUtil.sendEmail(emailAddress,
									"support@inventorysource.com",
									r.getLogin(), null,
									"oim@inventorysource.com," + r.getLogin(),
									m_supplier.getSupplierName() + " Orders",
									emailContent, "text/html");
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} else {
						EmailUtil.sendEmailWithAttachmentAndBCC(emailAddress,
								"support@inventorysource.com", r.getLogin(),
								null,
								"oim@inventorysource.com," + r.getLogin(),
								m_supplier.getSupplierName() + " Orders",
								"Find attached the orders from my store.",
								fileName, "");
						// EmailUtil.sendEmailWithAttachment(emailAddress,"support@inventorysource.com",
						// "mayank@inventorysource.com", "Orders",
						// "Find attached the orders from my store.",fileName);
					}
				}

				String nameEmail = StringHandle.removeNull(r.getFirstName())
						+ " " + StringHandle.removeNull(r.getLastName());
				String emailContent = "Dear " + nameEmail + "<br>";
				emailContent += "<br>Following is the status of the orders processed for the supplier "
						+ ovs.getOimSuppliers().getSupplierName() + " : - <br>";
				boolean emailNotification = false;

				// In both these cases i.e. Ftp File Upload and Email, orders
				// can
				// not fail at this stage
				// So all of them need to be marked placed
				for (int i = 0; i < orders.size(); i++) {
					OimOrders order = (OimOrders) orders.get(i);

					// Send Email Notifications if is set to true.
					if (order.getOimOrderBatches().getOimChannels()
							.getEmailNotifications() == 1) {
						emailNotification = true;
						String orderStatus = "Successfully Placed";
						emailContent += "<b>Store Order ID "
								+ order.getStoreOrderId() + "</b> -> "
								+ orderStatus + " ";
						emailContent += "<br>";
					}
				}
				if (emailNotification) {
					emailContent += "<br>Thanks, <br>Inventorysource support<br>";
					logStream
							.println("!! Sending email to user about order processing");
					EmailUtil.sendEmail(r.getLogin(),
							"support@inventorysource.com", "",
							"Order processing update results", emailContent,
							"text/html");
				}
			}
		}
		return true;
	}

	private boolean sendOrdersToSupplier(Integer vendorId,
			OimVendorSuppliers ovs, List orders, List successfulOrders,
			List failedOrders) {
		Integer supplierMethodNameId = m_supplierMethods
				.getOimSupplierMethodNames().getMethodNameId();
		log.debug("Supplier Method Id: " + supplierMethodNameId);
		if (OimConstants.SUPPLIER_METHOD_NAME_CUSTOM
				.equals(supplierMethodNameId)) {
			log.debug("!!! Custom method called");
			Supplier s = null;
			int supplierId = ovs.getOimSuppliers().getSupplierId();
			switch (supplierId) {
			case PCS:
				log.debug("!!! SENDING ORDERS TO PCS");
				s = new PCS();
				break;
			case MOBILELINE:
				log.debug("!!! SENDING ORDERS TO Mobileline");
				s = new Mobileline();
				break;
			case PETRA:
				log.debug("!!! SENDING ORDERS TO Petra");
				s = new Petra();
				break;
			case ICELLA:
				log.debug("!!! SENDING ORDERS TO iCella");
				s = new Icella();
				break;
			case PCI:
				log.debug("!!! SENDING ORDERS TO Progressive Concepts");
				s = new ProgressiveConcepts();
				break;
			case MOTENG:
				log.debug("!!! SENDING ORDERS TO Moteng");
				s = new Moteng();
				break;
			case DRBOTT:
				log.debug("!!! SENDING ORDERS TO DrBott");
				s = new DrBott();
				break;
			case RAX:
				log.debug("!!! SENDING ORDERS TO Rax");
				s = new Rax();
				break;
			case DROPSHIPDIRECT:
				log.debug("!!! SENDING ORDERS TO Dropship Direct");
				s = new DropshipDirect();
				break;
			case DROPSHIPDIRECTAUTOMOTIVES:
				logStream
						.println("!!! SENDING ORDERS TO Dropship Direct Automotives");
				s = new DropshipDirect();
				break;
			case BnF:
				log.debug("!!! SENDING ORDERS TO BnF USA");
				s = new BF();
				break;
			case DandH:
				log.debug("!!! SENDING ORDERS TO DandH");
				s = new DandH();
				break;
			case BRADLEYCALDWELL:
				log.debug("!!! SENDING ORDERS TO BRADLEYCALDWELL");
				s = new BradleyCaldwell();
				break;
			case HONESTGREEN:
				log.debug("SENDING ORDERS TO HONEST GREEN");
				s = new HonestGreen();
				break;
			}
			if (s != null) {
				s.setLogStream(logStream);
				s.sendOrders(vendorId, ovs, orders);
				successfulOrders.addAll(s.getSuccessfulOrders());
				failedOrders.addAll(s.getFailedOrders());
			} else {
				log.debug("Unknown Supplier Id: " + supplierId);
			}
		} else if (OimConstants.SUPPLIER_METHOD_NAME_EMAIL
				.equals(supplierMethodNameId)
				|| OimConstants.SUPPLIER_METHOD_NAME_FTP
						.equals(supplierMethodNameId)) {
			String tmp = PojoHelper.getSupplierMethodAttributeValue(
					m_supplierMethods,
					OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FILETYPEID);

			// Find if there is an associated file
			OimFiletypes oimFile = null;
			if (tmp != null && tmp.length() > 0) {
				Integer fileid = Integer.valueOf(tmp);
				log.debug("Using File Id: " + fileid
						+ " for generating supplier order file");
				Query query = m_dbSession
						.createQuery("select f from OimFiletypes f where f.fileTypeId=:fileId and f.deleteTm is null");
				Iterator it = query.setInteger("fileId", fileid.intValue())
						.iterate();
				if (!it.hasNext()) {
					logStream
							.println("No file defined for this supplier file id: "
									+ fileid);
				} else {
					oimFile = (OimFiletypes) it.next();
				}
			}

			int fileFormat = 1;
			try {
				fileFormat = Integer
						.parseInt(PojoHelper
								.getSupplierMethodAttributeValue(
										m_supplierMethods,
										OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FILEFORMAT));
			} catch (Exception e) {
				e.printStackTrace();
				fileFormat = 1;
			}

			OrderFile ofile = null;
			if (fileFormat == OimConstants.FILE_FORMAT_XLS) {
				ofile = new DefaultXlsFile(m_dbSession);
			} else {
				ofile = new DefaultCsvFile(m_dbSession);
			}
			if (oimFile != null) {
				ofile = new DatabaseFile(m_dbSession, oimFile);
			}
			ofile.build();

			String name = (String) ofile.getFileFormatParams().get(
					OimConstants.FILE_FORMAT_PARAMS_NAME);
			if (name != null && name.length() > 0) {
				if (name.indexOf("#SupplierAccountNumber") != -1) {
					String accountNumber = ovs.getAccountNumber();
					name = name.replaceAll("#SupplierAccountNumber",
							accountNumber);
				}
			}
			String fileName = "";
			if (fileFormat == OimConstants.FILE_FORMAT_XLS) {
				fileName = "" + vendorId + "-" + m_supplier.getSupplierId()
						+ ".xls";
				log.debug("Generating file " + fileName);
				try {
					generateXlsFile(orders, ofile.getFileFieldMaps(), fileName,
							ofile.getFileFormatParams(),
							ofile.getSpecificsProvider(ovs));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					log.debug(e.getMessage());
					e.printStackTrace();
				}
			} else if (fileFormat == OimConstants.FILE_FORMAT_CSV) {
				fileName = "" + vendorId + "-" + m_supplier.getSupplierId()
						+ ".csv";
				if (name != null && name.length() > 0)
					fileName = name;
				log.debug("Generating file " + fileName);
				try {
					generateCsvFile(orders, ofile.getFileFieldMaps(), fileName,
							ofile.getFileFormatParams(),
							ofile.getSpecificsProvider(ovs));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					log.debug(e.getMessage());
					e.printStackTrace();
				}
			}

			// Get the reps object to get the email id and name of the vendor to
			// whom the order status email will go in case the emaiNotification
			// is set for him.
			Query q = m_dbSession
					.createQuery("select r from salesmachine.hibernatedb.Reps r where r.vendorId = "
							+ vendorId);
			Reps r = new Reps();
			Iterator repsIt = q.iterate();
			if (repsIt.hasNext()) {
				r = (Reps) repsIt.next();
			}

			if (OimConstants.SUPPLIER_METHOD_NAME_FTP
					.equals(supplierMethodNameId)) {
				String ftpServer = PojoHelper.getSupplierMethodAttributeValue(
						m_supplierMethods,
						OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPSERVER);
				String ftpLogin = PojoHelper.getSupplierMethodAttributeValue(
						m_supplierMethods,
						OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPLOGIN);
				String ftpPassword = PojoHelper
						.getSupplierMethodAttributeValue(
								m_supplierMethods,
								OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPPASSWORD);

				if ("#accountspecific".equals(ftpLogin))
					ftpLogin = ovs.getLogin();
				if ("#accountspecific".equals(ftpPassword))
					ftpPassword = ovs.getPassword();

				String ftpFolder = PojoHelper.getSupplierMethodAttributeValue(
						m_supplierMethods,
						OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPFOLDER);

				FtpFileUploader uploader = new FtpFileUploader(ftpServer,
						ftpLogin, ftpPassword, 5, 0);
				try {
					uploader.Upload(ftpFolder, fileName, fileName);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					log.debug(e.getMessage());
					e.printStackTrace();
					return false;
				}

			} else if (OimConstants.SUPPLIER_METHOD_NAME_EMAIL
					.equals(supplierMethodNameId)) {
				String emailAddress = PojoHelper
						.getSupplierMethodAttributeValue(
								m_supplierMethods,
								OimConstants.SUPPLIER_METHOD_ATTRIBUTES_EMAILADDRESS);
				if (fileFormat == OimConstants.FILE_FORMAT_SEND_PLAIN_TEXT_IN_EMAIL) {
					try {
						String vendor_name = StringHandle.removeNull(r
								.getFirstName())
								+ " "
								+ StringHandle.removeNull(r.getLastName());
						String emailContent = "Dear " + vendor_name + "<br>";
						emailContent += "<br>Following is the status of the orders processed for the supplier "
								+ ovs.getOimSuppliers().getSupplierName()
								+ " : - <br>";
						emailContent += generateMailBody(orders,
								ofile.getFileFieldMaps(),
								new StandardFileSpecificsProvider(m_dbSession,
										ovs, new Vendors(vendorId)));
						EmailUtil.sendEmail(emailAddress,
								"support@inventorysource.com", r.getLogin(),
								null,
								"oim@inventorysource.com," + r.getLogin(),
								m_supplier.getSupplierName() + " Orders",
								emailContent, "text/html");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} else {
					EmailUtil.sendEmailWithAttachmentAndBCC(emailAddress,
							"support@inventorysource.com", r.getLogin(), null,
							"oim@inventorysource.com," + r.getLogin(),
							m_supplier.getSupplierName() + " Orders",
							"Find attached the orders from my store.",
							fileName, "");
					// EmailUtil.sendEmailWithAttachment(emailAddress,"support@inventorysource.com",
					// "mayank@inventorysource.com", "Orders",
					// "Find attached the orders from my store.",fileName);
				}
			}

			String nameEmail = StringHandle.removeNull(r.getFirstName()) + " "
					+ StringHandle.removeNull(r.getLastName());
			String emailContent = "Dear " + nameEmail + "<br>";
			emailContent += "<br>Following is the status of the orders processed for the supplier "
					+ ovs.getOimSuppliers().getSupplierName() + " : - <br>";
			boolean emailNotification = false;

			// In both these cases i.e. Ftp File Upload and Email, orders can
			// not fail at this stage
			// So all of them need to be marked placed
			for (int i = 0; i < orders.size(); i++) {
				OimOrders order = (OimOrders) orders.get(i);
				for (Iterator detailIt = order.getOimOrderDetailses()
						.iterator(); detailIt.hasNext();) {
					OimOrderDetails detail = (OimOrderDetails) detailIt.next();
					successfulOrders.add(detail.getDetailId());
				}

				// Send Email Notifications if is set to true.
				if (order.getOimOrderBatches().getOimChannels()
						.getEmailNotifications() == 1) {
					emailNotification = true;
					String orderStatus = "Successfully Placed";
					emailContent += "<b>Store Order ID "
							+ order.getStoreOrderId() + "</b> -> "
							+ orderStatus + " ";
					emailContent += "<br>";
				}
			}
			if (emailNotification) {
				emailContent += "<br>Thanks, <br>Inventorysource support<br>";
				logStream
						.println("!! Sending email to user about order processing");
				EmailUtil.sendEmail(r.getLogin(),
						"support@inventorysource.com", "",
						"Order processing update results", emailContent,
						"text/html");
			}
		}

		return true;
	}

	public static void generateCsvFile(List orders, List fileFieldMaps,
			String fileName, Hashtable fileFormatParams,
			IFileSpecificsProvider fileSpecifics) throws Exception {
		FileWriter outputFile = new FileWriter(fileName);

		boolean useHeader = "1".equals((String) fileFormatParams
				.get(OimConstants.FILE_FORMAT_PARAMS_USEHEADER));
		String fieldDelimiter = (String) fileFormatParams
				.get(OimConstants.FILE_FORMAT_PARAMS_FIELD_DELIMITER);
		String textDelimiter = (String) fileFormatParams
				.get(OimConstants.FILE_FORMAT_PARAMS_TEXT_DELIMITER);
		if ("TAB".equals(fieldDelimiter)) {
			fieldDelimiter = "\t";
		}

		// Get the headers now
		if (useHeader) {
			// Write the header first
			String headerline = "";
			int i = 0;
			for (Iterator it = fileFieldMaps.iterator(); it.hasNext();) {
				OimFileFieldMap map = (OimFileFieldMap) it.next();
				if (i > 0)
					headerline += fieldDelimiter;
				headerline += textDelimiter
						+ StringHandle.removeNull(map.getMappedFieldName())
						+ textDelimiter;
				i++;
			}
			headerline += "\n";
			outputFile.write(headerline);
		}

		// Write the data now
		for (int i = 0; i < orders.size(); i++) {
			OimOrders order = (OimOrders) orders.get(i);
			for (Iterator detailIt = order.getOimOrderDetailses().iterator(); detailIt
					.hasNext();) {
				OimOrderDetails detail = (OimOrderDetails) detailIt.next();
				// for all the order details
				String dataline = "";
				int j = 0;

				for (Iterator it = fileFieldMaps.iterator(); it.hasNext();) {
					OimFileFieldMap map = (OimFileFieldMap) it.next();
					OimFields field = map.getOimFields();
					String writeModifier = StringHandle.removeNull(map
							.getMappedFieldModifierRuleWr());
					String fieldValue = StringHandle.removeNull(fileSpecifics
							.getFieldValueFromOrder(detail, map));
					// log.debug("Field Id: "+field.getFieldId()+"\t ("+field.getFieldName()+")"+"\t:"+fieldValue);
					if (j > 0)
						dataline += fieldDelimiter;
					dataline += textDelimiter + fieldValue + textDelimiter;
					j++;
				}
				dataline += "\n";
				outputFile.write(dataline);
			}
		} // for (int i=0;i<orders.numRows();i++) {

		if (fileSpecifics.getLastFileLine() != null) {
			outputFile.write(fileSpecifics.getLastFileLine());
		}
		outputFile.close();
	}

	public static void generateXlsFile(List orders, List fileFieldMaps,
			String fileName, Hashtable fileFormatParams,
			IFileSpecificsProvider fileSpecifics) throws Exception {
		int row = 0;
		boolean useHeader = "1".equals((String) fileFormatParams
				.get(OimConstants.FILE_FORMAT_PARAMS_USEHEADER));
		try {
			WritableWorkbook workbook = Workbook.createWorkbook(new File(
					fileName));
			WritableSheet sheet = workbook.createSheet("Sheet3", 0);
			Label label;
			if (useHeader) {
				int column = 0;
				// Write the header first
				String colval = "";
				for (Iterator it = fileFieldMaps.iterator(); it.hasNext();) {
					OimFileFieldMap map = (OimFileFieldMap) it.next();
					if ("".equals(StringHandle.removeNull(map
							.getMappedFieldName()))) {
						colval += StringHandle.removeNull(map
								.getMappedFieldName());
						continue;
					} else {
						colval += StringHandle.removeNull(map
								.getMappedFieldName());
					}
					label = new Label(column, row, colval);
					sheet.addCell(label);
					column++;
					colval = "";
				}
				row++;
			}

			// Write the data now
			for (int i = 0; i < orders.size(); i++) {
				OimOrders order = (OimOrders) orders.get(i);
				for (Iterator detailIt = order.getOimOrderDetailses()
						.iterator(); detailIt.hasNext();) {
					OimOrderDetails detail = (OimOrderDetails) detailIt.next();
					// for all the order details
					int column = 0;
					String colval = "";
					for (Iterator it = fileFieldMaps.iterator(); it.hasNext();) {
						OimFileFieldMap map = (OimFileFieldMap) it.next();
						String fieldValue = StringHandle
								.removeNull(fileSpecifics
										.getFieldValueFromOrder(detail, map));
						if ("".equals(StringHandle.removeNull(map
								.getMappedFieldName()))) {
							colval += StringHandle.removeNull(fieldValue)
									+ "\n";
							continue;
						} else {
							colval += StringHandle.removeNull(fieldValue);
						}
						label = new Label(column, row, colval);
						sheet.addCell(label);
						column++;
						colval = "";
					}
					row++;
				}
			}// for(int i=0;i<orders.size();i++) {
			workbook.write();
			workbook.close();
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String generateMailBody(List orders, List fileFieldMaps,
			IFileSpecificsProvider fileSpecifics) throws Exception {
		StringBuffer dataline = new StringBuffer("<br/><br/>");

		// Write the data now
		for (int i = 0; i < orders.size(); i++) {
			OimOrders order = (OimOrders) orders.get(i);
			for (Iterator detailIt = order.getOimOrderDetailses().iterator(); detailIt
					.hasNext();) {
				OimOrderDetails detail = (OimOrderDetails) detailIt.next();
				// for all the order details
				int j = 0;
				for (Iterator it = fileFieldMaps.iterator(); it.hasNext();) {
					OimFileFieldMap map = (OimFileFieldMap) it.next();
					String fieldValue = StringHandle.removeNull(fileSpecifics
							.getFieldValueFromOrder(detail, map));
					// log.debug("Field Id: "+field.getFieldId()+"\t ("+field.getFieldName()+")"+"\t:"+fieldValue);
					if (j > 0)
						dataline.append(map.getMappedFieldName() + " : "
								+ fieldValue + "<br/>");
					j++;
				}
				dataline.append("<br/><b>**********</b><br/>");
			}
		}// for (int i=0;i<orders.numRows();i++) {
		dataline.append("<br/><br/>End of Order List");

		return dataline.toString();
	}

	private boolean updateOrderStatus(List orders, Integer status) {
		if (orders == null || orders.size() == 0) {
			log.debug("No orders to update with status: " + status);
			return true;
		}
		String orderDetails = "";
		for (int i = 0; i < orders.size(); i++) {
			if (orderDetails.length() > 0)
				orderDetails += ",";
			orderDetails += (Integer) orders.get(i);
		}

		String processTime = "";
		if (status == OimConstants.ORDER_STATUS_PROCESSED_SUCCESS) {
			processTime = " o.processingTm=sysdate, ";
		}
		System.out
				.println("update salesmachine.hibernatedb.OimOrderDetails o set o.oimOrderStatuses.statusId="
						+ status
						+ " where o.detailId in ("
						+ orderDetails
						+ ")");

		Transaction tx = null;
		try {
			tx = m_dbSession.beginTransaction();

			Query q = m_dbSession
					.createQuery("update salesmachine.hibernatedb.OimOrderDetails o set "
							+ processTime
							+ " o.oimOrderStatuses.statusId="
							+ status
							+ " where o.detailId in ("
							+ orderDetails
							+ ")");
			int rows = q.executeUpdate();
			log.debug("Updated order details. Rows changed: " + rows);

			tx.commit();
		} catch (RuntimeException e) {
			tx.rollback();
			e.printStackTrace();
		}
		return true;
	}

	public String trackOrder(Integer vendorId, Integer orderDetailId) {
		Session session = m_dbSession;
		Transaction tx = null;
		OrderStatus orderStatus;
		tx = session.beginTransaction();
		OimOrderDetails oimOrderDetails = (OimOrderDetails) session.get(
				OimOrderDetails.class, orderDetailId);
		HasTracking s = null;
		OimVendorSuppliers oimVendorSuppliers = null;
		Query query = session
				.createQuery("from OimVendorSuppliers s where s.oimSuppliers=:supp and s.vendors.vendorId=:vid and s.deleteTm is null");
		query.setEntity("supp", oimOrderDetails.getOimSuppliers());
		query.setInteger("vid", vendorId);
		Object it = query.uniqueResult();
		if (it instanceof OimVendorSuppliers)
			oimVendorSuppliers = (OimVendorSuppliers) it;
		switch (oimOrderDetails.getOimSuppliers().getSupplierId()) {
		case DandH:
			s = new DandH();
			orderStatus = s.getOrderStatus(oimVendorSuppliers,
					oimOrderDetails.getSupplierOrderNumber());
			oimOrderDetails.setSupplierOrderStatus(orderStatus.toString());
			if (orderStatus.isShipped())
				oimOrderDetails.setOimOrderStatuses(new OimOrderStatuses(
						OimConstants.ORDER_STATUS_SHIPPED));
			session.update(oimOrderDetails);
			break;
		case BnF:
			s = new BF();
			orderStatus = s.getOrderStatus(oimVendorSuppliers,
					oimOrderDetails.getSupplierOrderNumber());
			oimOrderDetails.setSupplierOrderStatus(orderStatus.toString());
			if (orderStatus.isShipped())
				oimOrderDetails.setOimOrderStatuses(new OimOrderStatuses(
						OimConstants.ORDER_STATUS_SHIPPED));
			session.update(oimOrderDetails);
			break;
		case HONESTGREEN:
			s = new HonestGreen();
			orderStatus = s.getOrderStatus(oimVendorSuppliers,
					oimOrderDetails.getSupplierOrderNumber());
			oimOrderDetails.setSupplierOrderStatus(orderStatus.toString());
			if (orderStatus.isShipped())
				oimOrderDetails.setOimOrderStatuses(new OimOrderStatuses(
						OimConstants.ORDER_STATUS_SHIPPED));
			session.update(oimOrderDetails);
			break;
		default:
			orderStatus = new OrderStatus();
			orderStatus.setStatus("Tracking orders for "
					+ oimOrderDetails.getOimSuppliers().getSupplierName()
					+ " is not suported.");
			break;
		}
		tx.commit();
		// Update the store with tracking info
		OimOrders oimOrders = oimOrderDetails.getOimOrders();
		OimChannels oimChannels = oimOrders.getOimOrderBatches()
				.getOimChannels();
		Integer channelId = oimChannels.getChannelId();
		IOrderImport iOrderImport = OrderImportManager
				.getIOrderImport(channelId);
		OimLogStream stream = new OimLogStream();
		if (iOrderImport != null) {
			log.debug("Created the iorderimport object");
			if (!iOrderImport.init(channelId, SessionManager.currentSession(),
					stream)) {
				log.debug("Failed initializing the channel with Id:{}",
						channelId);
			} else {
				iOrderImport.updateStoreOrder(oimOrderDetails, orderStatus);
			}
		} else {
			log.error("Could not find a bean to work with this Channel.");
			stream.println("This Channel type is not supported for pushing order updates.");
		}

		return orderStatus.toString();
	}
}
