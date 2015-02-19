package salesmachine.oim.suppliers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

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
import salesmachine.util.StringHandle;

public class DandH  extends Supplier {
	/***
	 * @Requirement for Integration 
	 * 	1) Username : login ID
	 * 	2) Password : password
	 * This method send orders to DandH
	 * @param vendorId VendorID
	 * @param ovs Order vendor supplier
	 * @param orders list of orders containing order info.
	 */
	public void sendOrders(Integer vendorId, OimVendorSuppliers ovs, List orders){		
		logStream.println("!!Started sending orders to DandH");
		orderSkuPrefixMap = setSkuPrefixForOrders(ovs);  //populate orderSkuPrefixMap with channel id and the prefix to be used for the given supplier.
		
		Session session = SessionManager.currentSession();
		Query query = session.createQuery("select r from salesmachine.hibernatedb.Reps r where r.vendorId = "+ vendorId);
		Reps r = new Reps();
		Iterator repsIt = query.iterate();
		if (repsIt.hasNext()) {
			r = (Reps) repsIt.next();
		}
		Vendors v = new Vendors(); v.setVendorId(r.getVendorId());
		try {
			createAndPostXMLRequest(orders,getFileFieldMap(), new StandardFileSpecificsProvider(session,ovs,v), ovs, vendorId, r);						
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private List getFileFieldMap() {
		List fileFieldMaps = new ArrayList();
		//For blank headers, header values will be append to next header value which is not blank. 
		//In this case headers after "Description" are all blank so they will append in header "Address"
		String fields[] = {"SHIPTONAME","SHIPTOATTN","SHIPTOADDRESS","SHIPTOADDRESS2","SHIPTOCITY",
							"SHIPTOSTATE","SHIPTOZIP","SHIPCARRIER","SHIPSERVICE","PONUM","/ORDERHEADER",
							"ORDERITEMS","ITEM","PARTNUM","QTY","/ITEM"};
		
		Integer mappedFieldIds[]= {3,3,4,0,5,
									6,7,10,0,2,0,
									0,0,1,9,0};
		
		for (int i=0;i<fields.length;i++) {
			OimFields field = new OimFields(fields[i],fields[i],new Date(),null,null);
			field.setFieldId(mappedFieldIds[i]);
			OimFileFieldMap ffm = new OimFileFieldMap(null,field,fields[i],new Date(),null,"","");
			fileFieldMaps.add(ffm);
		}
		return fileFieldMaps;				
	}
		
	private void createAndPostXMLRequest(List orders, List fileFieldMaps, IFileSpecificsProvider fileSpecifics, OimVendorSuppliers ovs,
			Integer vendorId, Reps r) throws Exception {
		String USERID = ovs.getLogin();
		String PASSWORD = ovs.getPassword();
		String lincenceKey = ovs.getAccountNumber();
		boolean emailNotification = false;
		String name = StringHandle.removeNull(r.getFirstName()) + " " + StringHandle.removeNull(r.getLastName());
		String emailContent = "Dear " + name + "<br>";
		emailContent += "<br>Following is the status of the orders processed for the supplier "
				+ ovs.getOimSuppliers().getSupplierName() + " : - <br>";
		// Write the data now			
		for(int i=0;i<orders.size();i++) {
			OimOrders order = (OimOrders)orders.get(i);
			
			boolean addShippingDetails = true;
			String val = "";
			for (Iterator detailIt=order.getOimOrderDetailses().iterator(); detailIt.hasNext();) {
				OimOrderDetails detail = (OimOrderDetails)detailIt.next(); 
				// for all the order details
				for(Iterator it=fileFieldMaps.iterator();it.hasNext();) {
					OimFileFieldMap map = (OimFileFieldMap)it.next();
					String fieldValue = StringHandle.removeNull(fileSpecifics.getFieldValueFromOrder(detail, map));
					if("SHIPTONAME".equals(StringHandle.removeNull(map.getMappedFieldName())) && addShippingDetails){
						val += "<"+map.getMappedFieldName()+"><![CDATA["+StringHandle.removeNull(fieldValue)+"]]></"+map.getMappedFieldName()+">\n";
					}else if("SHIPTOATTN".equals(StringHandle.removeNull(map.getMappedFieldName())) && addShippingDetails){
						val += "<"+map.getMappedFieldName()+"></"+map.getMappedFieldName()+">\n";
					}else if("SHIPTOADDRESS".equals(StringHandle.removeNull(map.getMappedFieldName())) && addShippingDetails){
						val += "<"+map.getMappedFieldName()+"><![CDATA["+StringHandle.removeNull(fieldValue)+"]]></"+map.getMappedFieldName()+">\n";
					}else if("SHIPTOADDRESS2".equals(StringHandle.removeNull(map.getMappedFieldName())) && addShippingDetails){
						val += "<"+map.getMappedFieldName()+"><![CDATA["+StringHandle.removeNull(fieldValue)+"]]></"+map.getMappedFieldName()+">\n";
					}else if("SHIPTOCITY".equals(StringHandle.removeNull(map.getMappedFieldName())) && addShippingDetails){
						val += "<"+map.getMappedFieldName()+"><![CDATA["+StringHandle.removeNull(fieldValue)+"]]></"+map.getMappedFieldName()+">\n";
					}else if("SHIPTOSTATE".equals(StringHandle.removeNull(map.getMappedFieldName())) && addShippingDetails){
						val += "<"+map.getMappedFieldName()+"><![CDATA["+StringHandle.removeNull(fieldValue)+"]]></"+map.getMappedFieldName()+">\n";
					}else if("SHIPTOZIP".equals(StringHandle.removeNull(map.getMappedFieldName())) && addShippingDetails){
						val += "<"+map.getMappedFieldName()+"><![CDATA["+StringHandle.removeNull(fieldValue)+"]]></"+map.getMappedFieldName()+">\n";
					}else if("SHIPCARRIER".equals(StringHandle.removeNull(map.getMappedFieldName())) && addShippingDetails){
						//val += "<"+map.getMappedFieldName()+"><![CDATA["+StringHandle.removeNull(fieldValue)+"]]></"+map.getMappedFieldName()+">\n";
						val += "<"+map.getMappedFieldName()+"><![CDATA[UPS]]></"+map.getMappedFieldName()+">\n";
					}else if("SHIPSERVICE".equals(StringHandle.removeNull(map.getMappedFieldName())) && addShippingDetails){
						//val += "<"+map.getMappedFieldName()+"><![CDATA["+StringHandle.removeNull(fieldValue)+"]]></"+map.getMappedFieldName()+">\n";
						val += "<"+map.getMappedFieldName()+"><![CDATA[Ground]]></"+map.getMappedFieldName()+">\n";
					}else if("PONUM".equals(StringHandle.removeNull(map.getMappedFieldName())) && addShippingDetails){
						val += "<"+map.getMappedFieldName()+"><![CDATA["+StringHandle.removeNull(fieldValue)+"]]></"+map.getMappedFieldName()+">\n";
					}else if("/ORDERHEADER".equals(StringHandle.removeNull(map.getMappedFieldName())) && addShippingDetails){
						val += "<"+map.getMappedFieldName()+">\n";
					}else if("ORDERITEMS".equals(StringHandle.removeNull(map.getMappedFieldName())) && addShippingDetails){
						val += "<"+map.getMappedFieldName()+">\n";
						addShippingDetails = false;
					}else if("ITEM".equals(StringHandle.removeNull(map.getMappedFieldName())) && !addShippingDetails){
						val += "<"+map.getMappedFieldName()+">\n";
					}else if("PARTNUM".equals(StringHandle.removeNull(map.getMappedFieldName())) && !addShippingDetails){
						val += "<"+map.getMappedFieldName()+"><![CDATA["+StringHandle.removeNull(fieldValue)+"]]></"+map.getMappedFieldName()+">\n";
					}else if("QTY".equals(StringHandle.removeNull(map.getMappedFieldName())) && !addShippingDetails){
						val += "<"+map.getMappedFieldName()+"><![CDATA["+StringHandle.removeNull(fieldValue)+"]]></"+map.getMappedFieldName()+">\n";
					}else if("/ITEM".equals(StringHandle.removeNull(map.getMappedFieldName())) && !addShippingDetails){
						val += "<"+map.getMappedFieldName()+">\n";
					}
				}				
			}//END for (Iterator detailIt=order.getOimOrderDetailses().iterator(); detailIt.hasNext();) {
			
			String xmlRequest = "<XMLFORMPOST>\n"+
					            "<REQUEST>orderEntry</REQUEST>\n"+
						            "<LOGIN>\n"+
							            "<USERID>"+USERID+"</USERID>\n"+
					                    "<PASSWORD>"+PASSWORD+"</PASSWORD>\n"+
						            "</LOGIN>\n"+
						            "<ORDERHEADER>\n"+
				                        "<BACKORDERALLOW>N</BACKORDERALLOW>\n"+
				                        val+
						            "</ORDERITEMS>\n"+
					            "</XMLFORMPOST>";
			
			System.out.println("\n\n\n\nPost Request : "+xmlRequest);
			String xmlResponse=null;
			try{
				xmlResponse = postOrder(xmlRequest, r);
			}catch (Exception e) {
				e.printStackTrace();
			}
			//Output the response
			if (xmlResponse != null && xmlResponse.indexOf("<STATUS>success</STATUS>")!=-1) {
				for (Iterator detailIt = order.getOimOrderDetailses()
						.iterator(); detailIt.hasNext();) {
					OimOrderDetails detail = (OimOrderDetails) detailIt.next();
					successfulOrders.add(detail.getDetailId());
				}
			} else {
				for (Iterator detailIt = order.getOimOrderDetailses()
						.iterator(); detailIt.hasNext();) {
					OimOrderDetails detail = (OimOrderDetails) detailIt.next();
					failedOrders.add(detail.getDetailId());
				}
				updateVendorSupplierOrderHistory(vendorId, ovs, xmlResponse.toString());
			}

			// Send Email Notifications if is set to true.
			if (order.getOimOrderBatches().getOimChannels()
					.getEmailNotifications() == 1) {
				emailNotification = true;
				String orderStatus = (xmlResponse != null && xmlResponse.indexOf("<STATUS>success</STATUS>")!=-1) == true ? "Successfully Placed"
						: "Failed to place order";
				emailContent += "<b>Store Order ID " + order.getStoreOrderId()
						+ "</b> -> " + orderStatus + " ";
				emailContent += "<br>";
			}

			String logEmailContent = "";
			if (xmlResponse !=null) {
				logStream.println("!! ---------------STORE ORDER ID : "
						+ order.getStoreOrderId() + "---------");
				logStream.println("!! ORDER PLACED : "
						+ ((xmlResponse.indexOf("<xml_action>PROCESS</xml_action>") != -1)==true?"Yes":"No"));
				logStream.println("!! GET RETURN VALUE : "
						+ xmlResponse);
				logEmailContent = "----------------Store ORDER ID : "+order.getStoreOrderId()+"---------\n\n";
				logEmailContent += "Order Placed : "+((xmlResponse.indexOf("<STATUS>success</STATUS>") != -1)==true?"Yes":"No")+"\n\n";
				logEmailContent += "-------------- XML SOAP REQUEST SENT -------------\n";
				logEmailContent += xmlRequest+"\n";
				logEmailContent += "--------------------------------------------------";
				logEmailContent += "-------------- XML SOAP RESPONSE CAME -------------\n";
				logEmailContent += xmlResponse+"\n";
				logEmailContent += "--------------------------------------------------";
			}
			EmailUtil.sendEmail("oim@inventorysource.com","support@inventorysource.com","","Logs of order processing for order : "+order.getStoreOrderId(),logEmailContent);				        
		
		}//END for(int i=0;i<orders.size();i++) {
		if (emailNotification) {
			emailContent += "<br>Thanks, <br>Inventory Source Team<br>";
			logStream.println("Sending email to " + r.getLogin());
			EmailUtil.sendEmail(r.getLogin(), "support@inventorysource.com","", "Order Processing Results", emailContent, "text/html");
		}
	}
	
	public String postOrder(String request, Reps r){
		URL url;
		HttpsURLConnection connection = null;  
		String response = "";
		try {
			//Create connection
			url = new URL("https://www.dandh.com/dhXML/xmlDispatch");
			connection = (HttpsURLConnection)url.openConnection();
			connection.setRequestMethod("POST");

			byte[] req=request.getBytes();

			connection.setRequestProperty("Content-Type", "text/xml");
			connection.setUseCaches (false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			OutputStream out = connection.getOutputStream();
			out.write(req);
			out.close();
			connection.connect();
			//System.out.print(connection.getContentLength());

			BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line + '\n');
			}
			System.out.println(sb.toString());
			response = sb.toString();

		} catch (Exception e) {
			e.printStackTrace();
			String logEmailContent = "-------------- Order failed with Exception -------------\n";
			logEmailContent += "-------------- XML SOAP REQUEST SENT -------------\n";
			logEmailContent += request+"\n";
			logEmailContent += "--------------------------------------------------";
			logEmailContent += "-------------- XML SOAP RESPONSE CAME -------------\n";
			logEmailContent += e+"\n";
			logEmailContent += "--------------------------------------------------";
			logStream.println(logEmailContent);
			String emailSubject = "Order failed for Vendor : "+r.getFirstName()+" "+r.getLastName()+" VID : "+r.getVendorId();
			EmailUtil.sendEmail("oim@inventorysource.com","support@inventorysource.com","",emailSubject,logEmailContent);				        
		} finally {
			if(connection != null) {
				connection.disconnect(); 
			}
		}
		return response;
	}
}
