package salesmachine.oim.suppliers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.suppliers.exception.SupplierCommunicationException;
import salesmachine.oim.suppliers.exception.SupplierConfigurationException;
import salesmachine.oim.suppliers.exception.SupplierOrderException;
import salesmachine.oim.suppliers.modal.OrderDetailResponse;
import salesmachine.util.StringHandle;

public class Petra extends Supplier {
	static final int SHIP_COMPLETE = 100001;
	/***
	 * This method send orders to mobile line supplie 
	 * @param vendorId VendorID
	 * @param ovs Order vendor supplier
	 * @param orders list of orders containing order info.
	 */
	public void sendOrders(Integer vendorId, OimVendorSuppliers ovs,List orders){		
		logStream.println("!!Started sending orders to PETRA");
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
		String uploadfilename = accountNumber+"-"+sdf.format(new Date())+".txt";
		
		logStream.println("Generating file "+uploadfilename);
		Hashtable fileFormatParams = new Hashtable();
		fileFormatParams.put(OimConstants.FILE_FORMAT_PARAMS_USEHEADER, "1");
		fileFormatParams.put(OimConstants.FILE_FORMAT_PARAMS_FIELD_DELIMITER, "TAB");
		fileFormatParams.put(OimConstants.FILE_FORMAT_PARAMS_TEXT_DELIMITER, "\"");
		try {
			SupplierFactory.generateCsvFile(orders,getFileFieldMap(), uploadfilename, fileFormatParams, new PetraFileSpecificsProvider(session, ovs, v));						
			String emailAddress = "flatfileorders@petra.com";			
			//EmailUtil.sendEmailWithAttachment(emailAddress,"support@inventorysource.com", "mayank@inventorysource.com", "Orders", "Find attached the orders from my store.",uploadfilename);
			EmailUtil.sendEmailWithAttachment(emailAddress,"support@inventorysource.com","oim@inventorysource.com,"+r.getLogin(), "Petra Orders", "Find attached the orders from my store.",uploadfilename,"");
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
				successfulOrders.put(detail.getDetailId(),new OrderDetailResponse());//TODO get PO Number and add as value here
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
		String fields[] = {"Petra Customer Number","PO Number","PO Date","Cancel Date","Order Reference",
						"Ship Via Code","Ship Complete","Signature Required","Ship To Name","Ship To Address 1",
						"Ship To Address 2","Ship To City","Ship To State","Ship To Zip code","Ship To Phone",
						"Line Number","Petra SKU","Order Quantity","Line Reference","UPC Code",
						"Price Match","Price Match Vendor","Comments 1","Comments 2","Comments 3",
						"Petra Internal","Petra Internal","Ship To Address 3","Special Shipping Instructions"
						};
		Integer mappedFieldIds[]= {0,2,0,0,0,
									10,SHIP_COMPLETE,0,3,4,
									12,5,6,7,0,
									0,1,9,0,0,
									0,0,0,0,0,
									0,0,0,OimConstants.OIM_FIELD_ORDER_COMMENTS};
		
		for (int i=0;i<fields.length;i++) {
			OimFields field = new OimFields(fields[i],fields[i],new Date(),null,null);
			field.setFieldId(mappedFieldIds[i]);
			OimFileFieldMap ffm = new OimFileFieldMap(null,field,fields[i],new Date(),null,"","");
			fileFieldMaps.add(ffm);
		}
		
		return fileFieldMaps;				
	}	
	
	
	public HashMap getDefaultShippingMapping() {
		HashMap finalMap = new HashMap();
				
		HashMap map = new HashMap();
		String fedexprefixes[] = {"Federal Express ","FEDEX "};
		map.put("Ground","40");
		map.put("Home","41");
		map.put("Next Day", "39");
		map.put("2 Day","37");
		map.put("3 Day","38");			
		duplicateMapWithPrefixes(finalMap, map, fedexprefixes);
				
		map = new HashMap();
		String upsprefixes[] = {"United Parcel Service ","UPS "};
		map.put("Ground", "43");
		map.put("Next Day", "44");
		map.put("2 Day", "42");
		map.put("3 Day", "46");
		map.put("Mail Innovations","45");
		duplicateMapWithPrefixes(finalMap, map, upsprefixes);
		
		map = new HashMap();
		String uspsprefixes[] = {"United States Postal Service ","USPS ","US Mail "};
		map.put("APO", "48");
		map.put("FPO","48");
		map.put("APO & FPO","48");
		map.put("APO/FPO","48");
		map.put("Priority Mail","47");
				
		duplicateMapWithPrefixes(finalMap, map, uspsprefixes);

		return finalMap;
	}

  @Override
  public void sendOrders(Integer vendorId, OimVendorSuppliers ovs, OimOrders orders)
      throws SupplierConfigurationException, SupplierCommunicationException, SupplierOrderException,
      ChannelConfigurationException, ChannelCommunicationException, ChannelOrderFormatException {
    // TODO Auto-generated method stub
    
  }	
}
