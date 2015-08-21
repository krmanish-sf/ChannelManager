package salesmachine.oim.suppliers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatedb.OimFields;
import salesmachine.hibernatedb.OimFileFieldMap;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.Reps;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.suppliers.modal.OrderDetailResponse;
import salesmachine.util.FtpFileUploader;
import salesmachine.util.StringHandle;

public class Icella extends Supplier {
	/***
	 * This method send orders to mobile line supplie 
	 * @param vendorId VendorID
	 * @param ovs Order vendor supplier
	 * @param orders list of orders containing order info.
	 */
	public void sendOrders(Integer vendorId, OimVendorSuppliers ovs,List orders){		
		logStream.println("!!Started sending orders to ICELLA");
		orderSkuPrefixMap = setSkuPrefixForOrders(ovs);  //populate orderSkuPrefixMap with channel id and the prefix to be used for the given supplier.
		
		Session session = SessionManager.currentSession();
		Query query = session
				.createQuery("select r from salesmachine.hibernatedb.Reps r where r.vendorId = "
						+ vendorId);
		Reps r = new Reps();
		Iterator repsIt = query.iterate();
		if (repsIt.hasNext()) {
			r = (Reps) repsIt.next();
		}
		Vendors v = new Vendors(); v.setVendorId(r.getVendorId());
				
		String name = StringHandle.removeNull(r.getFirstName()) + " " + StringHandle.removeNull(r.getLastName());
		String emailContent = "Dear " + name + "<br>";
		emailContent += "<br>Following is the status of the orders processed for the supplier "
			+ ovs.getOimSuppliers().getSupplierName() + " : - <br>";
		boolean emailNotification = false;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		String accountNumber = ovs.getAccountNumber();
		String uploadfilename = accountNumber+"_Orders_"+sdf.format(new Date())+".csv";
		
		logStream.println("Generating file "+uploadfilename);
		Hashtable fileFormatParams = new Hashtable();
		fileFormatParams.put(OimConstants.FILE_FORMAT_PARAMS_USEHEADER, "1");
		fileFormatParams.put(OimConstants.FILE_FORMAT_PARAMS_FIELD_DELIMITER, ",");
		fileFormatParams.put(OimConstants.FILE_FORMAT_PARAMS_TEXT_DELIMITER, "\"");
		boolean fileUploaded = false;
		try {			
			OimSupplierOrderPlacement.generateCsvFile(orders,getFileFieldMap(), uploadfilename, fileFormatParams, 
					new StandardFileSpecificsProvider(session,ovs,v));
			
			// Verify the ftp credentials
			String ftpServer = "ftp.icella.com";			
			String ftpLogin = ovs.getLogin();
			String ftpPassword = ovs.getPassword();
			String ftpFolder = "";
						
			FtpFileUploader uploader = new FtpFileUploader(ftpServer,ftpLogin,ftpPassword,5,0);
			try {
				uploader.Upload(ftpFolder, uploadfilename, uploadfilename);
				if (uploader.getError() == 0)
					fileUploaded = true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logStream.println(e.getMessage());
				e.printStackTrace();				
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// In both these cases i.e. Ftp File Upload and Email, orders can not fail at this stage
		// So all of them need to be marked placed
		for (int i=0;i<orders.size();i++) {
			OimOrders order = (OimOrders)orders.get(i);
			for (Iterator detailIt=order.getOimOrderDetailses().iterator(); detailIt.hasNext();) {
				OimOrderDetails detail = (OimOrderDetails)detailIt.next();
				if (fileUploaded)
					successfulOrders.put(detail.getDetailId(),new OrderDetailResponse());
				else
					failedOrders.add(detail.getDetailId());
			}
			
			//Send Email Notifications if is set to true.
			if(order.getOimOrderBatches().getOimChannels().getEmailNotifications()==1){
				emailNotification = true;
				String orderStatus = "Successfully Placed";
				if (! fileUploaded)
					orderStatus = "Failed to FTP";
				emailContent += "<b>Store Order ID "+order.getStoreOrderId()+"</b> -> "+orderStatus+" ";
				emailContent += "<br>";
			}
		}
		if(emailNotification){
			emailContent += "<br>Thanks, <br>Inventorysource support<br>";
			logStream.println("!! Sending email to user about order processing");
			EmailUtil.sendEmail(r.getLogin(), "support@inventorysource.com", "", "Order processing update results", emailContent,"text/html");
		}		        
	}
	
	private List getFileFieldMap() {
		List fileFieldMaps = new ArrayList();

		String fields[] = {"CustomerID","OrderNo","Company","Attn","Phone",
				"Email","Address1","Address2","City","State",
				"Zip","Country","ItemNo","ItemDesc","Qty",
				"UnitPrice","ExtPrice","ShipVia","OrderDate",
				};
		Integer mappedFieldIds[]= {10000,2,11,3,30,
									31,4,12,5,6,
									7,8,1,0,9,
									0,0,10,35};		
		for (int i=0;i<fields.length;i++) {
			OimFields field = new OimFields(fields[i],fields[i],new Date(),null,null);
			field.setFieldId(mappedFieldIds[i]);
			OimFileFieldMap ffm = new OimFileFieldMap(null,field,fields[i],new Date(),null,"","");
			fileFieldMaps.add(ffm);
		}
		
		return fileFieldMaps;				
	}		
}
