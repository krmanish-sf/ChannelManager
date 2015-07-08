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
import salesmachine.util.StringHandle;

public class DrBott extends Supplier {
	/***
	 * This method send orders to DrBott line supplie 
	 * @param vendorId VendorID
	 * @param ovs Order vendor supplier
	 * @param orders list of orders containing order info.
	 */
	public void sendOrders(Integer vendorId, OimVendorSuppliers ovs, List orders){		
		logStream.println("!!Started sending orders to DrBott");
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
		
		SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy");
		String accountNumber = ovs.getAccountNumber();
		String uploadfilename = accountNumber+"-"+sdf.format(new Date())+".csv";
		
		logStream.println("Generating file "+uploadfilename);
		Hashtable fileFormatParams = new Hashtable();
		fileFormatParams.put(OimConstants.FILE_FORMAT_PARAMS_USEHEADER, "1");
		fileFormatParams.put(OimConstants.FILE_FORMAT_PARAMS_FIELD_DELIMITER, ",");
		fileFormatParams.put(OimConstants.FILE_FORMAT_PARAMS_TEXT_DELIMITER, "\"");
		try {
			OimSupplierOrderPlacement.generateCsvFile(orders,getFileFieldMap(), uploadfilename, fileFormatParams, 
					new StandardFileSpecificsProvider(session,ovs,v));						
			String emailAddress = r.getLogin();
			String emailBody = "Account Number : "+accountNumber+"\n Find attached the orders from my store.";
			String emailSubject = accountNumber;
			EmailUtil.sendEmailWithAttachment(emailAddress,"support@inventorysource.com","oim@inventorysource.com,"+r.getLogin(), emailSubject, emailBody,uploadfilename,"");
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
				successfulOrders.add(detail.getDetailId());
			}
			
			//Send Email Notifications if is set to true.
			if(order.getOimOrderBatches().getOimChannels().getEmailNotifications()==1){
				emailNotification = true;
				String orderStatus = "Successfully Placed";
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

		String fields[] = {
				"CustomerShipID","CustomerPO","CustomerRef","PODate","ShipToName","ShipToAttn",
				"ShipToAddress1","ShipToAddress2","ShipToCity","ShipToState","ShipToZip","ShipToCountry",
				"ShipToPhone","BillToName","BillToAttn","BillToAddress1","BillToAddress2","BillToCity",
				"BillToState","BillToZip","BillToCountry","BillToPhone","ShippingMethod","ProductCode",
				"ProductDescShort","ProductQuanity","ProductUPC","ShipmentBillingType","ShipmentBillingAccount",
				};
		Integer mappedFieldIds[]= {2,2,0,14,3,0,
									4,0,5,6,7,8,
									30,3,0,4,0,5,
									6,7,8,30,10,1,
									0,9,0,0,0};		
		for (int i=0;i<fields.length;i++) {
			OimFields field = new OimFields(fields[i],fields[i],new Date(),null,null);
			field.setFieldId(mappedFieldIds[i]);
			OimFileFieldMap ffm = new OimFileFieldMap(null,field,fields[i],new Date(),null,"","");
			fileFieldMaps.add(ffm);
		}
		
		return fileFieldMaps;				
	}			
}
