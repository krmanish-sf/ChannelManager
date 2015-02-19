package salesmachine.oim.suppliers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import salesmachine.hibernatedb.OimFileFieldMap;
import salesmachine.hibernatedb.OimFileformatParams;
import salesmachine.hibernatedb.OimFiletypes;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderStatuses;
import salesmachine.hibernatedb.OimSupplierMethods;
import salesmachine.hibernatedb.OimSuppliers;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatehelper.PojoHelper;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.util.ExcHandle;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPTransferType;

public class OimSupplierOrderStatus {
	Session 			m_dbSession;
	OimSuppliers		m_supplier;
	OimSupplierMethods	m_supplierMethods;
	
	int m_MethodNameId;
	Hashtable m_Attributes;

	public boolean init(int supplierId, Session dbSession) {
		m_dbSession = dbSession;
		
		Transaction tx = m_dbSession.beginTransaction();
		Query query = m_dbSession.createQuery("from salesmachine.hibernatedb.OimSuppliers as s where s.supplierId=:id and s.deleteTm is null");
		query.setInteger("id", supplierId);
		tx.commit();
		if (! query.iterate().hasNext()) {
			System.out.println("No supplier found for supplier id: "+supplierId);
			return false;
		}
		
		m_supplier = (OimSuppliers)query.iterate().next();
		return true;
	}	
	
	public boolean fetchOrderStatus() {
		Transaction tx = m_dbSession.beginTransaction();
		
		Query query = m_dbSession.createQuery("select m from OimSupplierMethods as m inner join m.oimSupplierMethodattrValueses v where m.deleteTm is null and m.oimSuppliers=:supp and m.oimSupplierMethodTypes.methodTypeId=:methodTypeId and v.deleteTm is null");
		query.setEntity("supp", m_supplier);
		query.setInteger("methodTypeId", OimConstants.SUPPLIER_METHOD_TYPE_STATUSPULL.intValue());
		Iterator it = query.iterate();
		if (!it.hasNext()) {
			System.out.println("No method found for pushing orders to this supplier");
			return false;
		}
		m_supplierMethods = (OimSupplierMethods)it.next();
				
		query = m_dbSession.createQuery("select distinct c.vendorId from salesmachine.hibernatedb.OimChannels as c, " +
				"salesmachine.hibernatedb.OimOrderBatches as b, " +
				"salesmachine.hibernatedb.OimOrders as o, " +
				"salesmachine.hibernatedb.OimOrderDetails as d " +
				"where d.oimSuppliers=:supp and d.deleteTm is null and d.oimOrderStatuses=:status " +
				"and b.oimChannels = c and o.oimOrderBatches = b and d.oimOrders = o");		
		query.setEntity("supp", m_supplier);
		query.setEntity("status", new OimOrderStatuses(OimConstants.ORDER_STATUS_PLACED));
		tx.commit();
		
		it = query.iterate();
		if (! it.hasNext()) {
			System.out.println("No order details to be processed");
			return true;
		}

		while (it.hasNext()) {
			Integer vendorId = (Integer)it.next();
			System.out.println("Processing orders for vendor: "+vendorId);
			fetchVendorOrderStatus(vendorId);
		}
		
		return true;
	}

	private boolean fetchVendorOrderStatus(Integer vendorId) {
		System.out.println("Processing orders for vendor id: "+vendorId);
		Transaction tx = m_dbSession.beginTransaction();		
		Query query = m_dbSession.createQuery("from OimVendorSuppliers s where s.oimSuppliers=:supp and s.vendorId=:vid and s.deleteTm is null");
		query.setEntity("supp", m_supplier);
		query.setInteger("vid", vendorId.intValue());
		Iterator it = query.iterate();
		if (! it.hasNext()) {
			System.out.println("Supplier is not configured for this vendor");
			return false;
		}
		
		OimVendorSuppliers ovs = (OimVendorSuppliers)it.next();
		
		Integer supplierMethodNameId = m_supplierMethods.getOimSupplierMethodNames().getMethodNameId();
		if (OimConstants.SUPPLIER_METHOD_NAME_FTP.equals(supplierMethodNameId)) {
			
			String tmp = PojoHelper.getSupplierMethodAttributeValue(m_supplierMethods, OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FILETYPEID);			
			Integer fileid = Integer.valueOf(tmp);				
			System.out.println("Associated file type id: " + fileid);
			
			query = m_dbSession.createQuery("from OimFileformatParams where fileTypeId=:fileId and deleteTm is null");			
			it = query.setInteger("fileId", fileid.intValue()).iterate();;
			Hashtable fileFormatParams = new Hashtable();			
			while (it.hasNext()) {
				OimFileformatParams param = (OimFileformatParams)it.next();
				fileFormatParams.put(param.getParamName(), param.getParamValue());
			}
			
			String remoteFile = (String)fileFormatParams.get(OimConstants.FILE_FORMAT_PARAMS_NAME);
			if (remoteFile != null && remoteFile.length() > 0) {
				if (remoteFile.indexOf("#SupplierAccountNumber") != -1) {					
					String accountNumber = ovs.getAccountNumber();
					remoteFile = remoteFile.replaceAll("#SupplierAccountNumber",
							accountNumber);
				}
			}		
			String localFileName = "" + vendorId + "-" + m_supplier.getSupplierId() + ".txt";
			String ftpServer = PojoHelper.getSupplierMethodAttributeValue(m_supplierMethods, OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPSERVER);
			String ftpLogin = PojoHelper.getSupplierMethodAttributeValue(m_supplierMethods, OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPLOGIN);
			String ftpPassword = PojoHelper.getSupplierMethodAttributeValue(m_supplierMethods, OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPPASSWORD);
			String ftpFolder = PojoHelper.getSupplierMethodAttributeValue(m_supplierMethods, OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FTPFOLDER);
			try {
				FTPClient ftp = null;
				System.out.println("Connecting to " + ftpServer);
				ftp = new FTPClient(ftpServer);
				System.out.println("Logging in with " + ftpLogin + "/"
						+ ftpPassword);
				ftp.login(ftpLogin, ftpPassword);
				System.out.println("Changing directory to " + ftpFolder);
				ftp.chdir(ftpFolder);
				ftp.debugResponses(false);
				ftp.setType(FTPTransferType.BINARY);

				// get a local file from remote host
				System.out.println("Getting file: '" + remoteFile + "'");
				ftp.get(localFileName, remoteFile);
				System.out.println("Stored " + remoteFile + " locally as "
						+ localFileName);

				ftp.quit();
			} catch (Exception e) {
				ExcHandle.printStackTraceToErr(e);

				// could not fetch the status file
				return false;
			}			
			
			query = m_dbSession.createQuery("select f from OimFiletypes f where f.fileTypeId=:fileId and f.deleteTm is null");
			it = query.setInteger("fileId", fileid.intValue()).iterate();
			if (! it.hasNext()) {
				System.out.println("No file defined for this supplier file id: "+fileid);
				return false;
			}			
			OimFiletypes oimFile = (OimFiletypes)it.next();
			List statusData = readStatusFile(oimFile, localFileName,fileFormatParams);
			
			tx = m_dbSession.beginTransaction();
			if (statusData!=null && statusData.size() > 0) {
				for (int i=0;i<statusData.size();i++) {
					Hashtable values = (Hashtable)statusData.get(i);
					String purchaseOrderNumber = (String)values.get(OimConstants.OIM_FIELD_PRODUCT_ORDER_NUMBER);
					String sku = (String)values.get(OimConstants.OIM_FIELD_SKU);
					String detailStatus = (String)values.get(OimConstants.OIM_FIELD_SUPPLIER_DETAIL_STATUS);
					
					Integer status = null;
					if ("C".equalsIgnoreCase(detailStatus)) {
						System.out.println("Order Id: "+purchaseOrderNumber+"\tSKU:"+sku+"\tOrder Completed");
						status = OimConstants.ORDER_STATUS_PROCESSED_SUCCESS;
					} else if ("B".equals(detailStatus)) {
						System.out.println("Order Id: "+purchaseOrderNumber+"\tSKU:"+sku+"\tBack Ordered");
						status = OimConstants.ORDER_STATUS_PROCESSED_PENDING;
					} else if ("D".equals(detailStatus)) {
						System.out.println("Order Id: "+purchaseOrderNumber+"\tSKU:"+sku+"\tDiscontinued");
						status = OimConstants.ORDER_STATUS_PROCESSED_FAILED;
					} else if ("O".equals(detailStatus)) {
						System.out.println("Order Id: "+purchaseOrderNumber+"\tSKU:"+sku+"\tOut of stock");
						status = OimConstants.ORDER_STATUS_PROCESSED_FAILED;
					} else if ("P".equals(detailStatus)) {
						System.out.println("Order Id: "+purchaseOrderNumber+"\tSKU:"+sku+"\tOrder Pending");
						status = OimConstants.ORDER_STATUS_PROCESSED_PENDING;
					} else {
						System.out.println("Unknown detail status: '"+detailStatus+"'");
						continue;
					}
					
					query = m_dbSession.createQuery("from OimOrderDetails d where d.sku=:sku and d.deleteTm is null and d.oimOrders.orderId=:oid and d.oimOrderStatuses.statusId in (:status1,:status2)");
					query.setString("sku", sku);
					query.setString("oid", purchaseOrderNumber);
					query.setInteger("status1", OimConstants.ORDER_STATUS_PLACED.intValue());
					query.setInteger("status2", OimConstants.ORDER_STATUS_PROCESSED_PENDING.intValue());
					OimOrderDetails detail = (OimOrderDetails)query.iterate().next();
					detail.setProcessingTm(new Date());
					detail.setOimOrderStatuses(new OimOrderStatuses(status));
					m_dbSession.update(detail);
				}
			}
			tx.commit();
			
		}	// if (OimConstants.SUPPLIER_METHOD_NAME_FTP.equals(supplierMethodNameId)) {
		return true;
	}

	private List readStatusFile(OimFiletypes oimFile, String localFileName, Hashtable fileFormatParams) {
		// Process the file now
		boolean useHeader = "1".equals((String)fileFormatParams.get(OimConstants.FILE_FORMAT_PARAMS_USEHEADER));
		String fieldDelimiter = (String)fileFormatParams.get(OimConstants.FILE_FORMAT_PARAMS_FIELD_DELIMITER);
		String textDelimiter = (String)fileFormatParams.get(OimConstants.FILE_FORMAT_PARAMS_TEXT_DELIMITER);
		if ("TAB".equals(fieldDelimiter))
			fieldDelimiter = "\t";
		
		Query query = m_dbSession.createQuery("from OimFileFieldMap m where m.oimFiletypes=:file and m.deleteTm is null");
		List fieldMap = query.setEntity("file", oimFile).list();	
		
		File statusFile = new File(localFileName);
		List statusData = new ArrayList();
		if (statusFile.exists()) {
			try {
				System.out.println("Using field delimiter:"
						+ fieldDelimiter);
				FileInputStream filestream = new FileInputStream(statusFile);
				BufferedReader in = new BufferedReader(
						new InputStreamReader(filestream));
				String line = "";
				while ((line = in.readLine()) != null) {
					// Put space for empy fields
					while (line.indexOf(fieldDelimiter+fieldDelimiter)!=-1)
						line = line.replaceAll(fieldDelimiter+fieldDelimiter, fieldDelimiter+" "+fieldDelimiter);
					
					StringTokenizer str = new StringTokenizer(line,
							fieldDelimiter);
					int fieldIndex = 0;
					Hashtable values = new Hashtable();
					while (str.hasMoreTokens()) {
						if (fieldIndex >= fieldMap.size())
							break;
						OimFileFieldMap map = (OimFileFieldMap)fieldMap.get(fieldIndex);
						Integer fieldId = map.getOimFields().getFieldId();
						String fieldName = map.getOimFields().getFieldName();
						
						String token = str.nextToken();
						if (token.indexOf(textDelimiter) == 0)
							token = token.substring(textDelimiter.length());
						if (token.lastIndexOf(textDelimiter) == token.length()-textDelimiter.length())
							token = token.substring(0,token.length()-textDelimiter.length());
						token.trim();
						
						values.put(fieldId, token);
						System.out.println("Id: " + fieldId + " Field: "
								+ fieldName + " Value: " + token);

						fieldIndex++;
					}
					
					if (fieldIndex >= fieldMap.size())
						statusData.add(values);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return statusData;
		} // if (statusFile.exists()) {		
		return null;
	}
	
	public static void main(String[] args) {
		if (args.length == 1) {
			Session session = SessionManager.currentSession();		

			int supplierId = Integer.parseInt(args[0]);
			System.out.println("Processing orders for SupplierId: "+supplierId);
			OimSupplierOrderStatus osp = new OimSupplierOrderStatus();
			if (!osp.init(supplierId,session)) {
				System.out.println("Failed initializing.");
				System.exit(0);				
			}
			osp.fetchOrderStatus();
			SessionManager.closeSession();
			System.exit(0);	
		}

		System.exit(0);
	}
}
