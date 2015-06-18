package com.inventorysource.cm.web;

import java.util.StringTokenizer;

import salesmachine.email.EmailUtil;
import salesmachine.util.ExcHandle;
import salesmachine.util.Filter;
import salesmachine.util.NumberFormat;
import salesmachine.util.StringHandle;
import HTTPClient.CookieModule;

public class Payments {
	public static class CreditCard {
		public String name_on_card;
		public String card_num;
		public String exp_month;
		public String exp_year;
		public String type;
	}
	public static class Contact {
		  public static int CONTACT_TYPE_SHIPPING = 1;
		  public static int CONTACT_TYPE_BILLING = 2;
		  public static int CONTACT_TYPE_VENDOR = 3;

		  public int contact_id;
		  public int contact_type_id;
		  public String firstName;
		  public String lastName;
		  public String email;
		  public String address1;
		  public String address2;
		  public String city;
		  public String state;
		  public String zip;
		  public String province;
		  public String country;
		  public String phone;
		  public String dailyUpdateEmail;

		  public String errorMessage;  //if anything goes wrong, store it here
	}
	
	public String message;
	// New YourPay info
	public String username = "1001110893";
	public String password = "salesmachine"; // not needed "kocmljch",[hidden];
	
	public boolean chargeVendor(CreditCard credit_card, Contact billing_contact, double total_amount, String charge_description) {
		String statement = "", txnNumber = "";
		Filter f = new Filter("");
		String res_array[] = new String[35];

		try {
			if ("".equals(credit_card.card_num)) {
				// Don't consider this a charge attempt...
				if (!"".equals(message)) {
					message += "<BR>";
				}
				message += "Error with the credit card number.";
				return false;
			}

			// set Credit Card info
			String ccNum = credit_card.card_num;
			String ccExpMonth = credit_card.exp_month + "";
			String ccExpYear = credit_card.exp_year + "";
			if (ccExpYear.length() == 4) {
				ccExpYear = ccExpYear.substring(2); // only use last 2 digits of
													// expiration year
			}

			if (Integer.parseInt(ccExpMonth) < 10) {
				ccExpMonth = "0" + Integer.parseInt(ccExpMonth);
			}

			if (Integer.parseInt(ccExpYear) < 10) {
				ccExpYear = "0" + Integer.parseInt(ccExpYear);
			}

			String xType = "CHARGE";
			String amount = NumberFormat.formatNum(total_amount);

			String invNum = "";
			String commentString = "Inventory Source Channel Manager - invoice #" + invNum;
			commentString += "\n"
					+ Filter.transForm("s/<br>/\n/gi",
							charge_description);

			// for trasanction type
			String txn_type = txn_type = "SALE";
			int txn_no = 0;
			String orderid = "";
			String Address2 = "";

			if (!"".equals(billing_contact.address2)) {
				Address2 = billing_contact.address2;
			}
			String sXml = "<order>" + "<merchantinfo>"
			// -- Replace with your STORE NUMBER or STORENAME-->"
					+ "<configfile>"
					+ username
					+ "</configfile>"
					+ "</merchantinfo>"
					+ "<orderoptions>"
					+ "<ordertype>"
					+ txn_type
					+ "</ordertype>"

					// -- For test transactions, set to GOOD, DECLINE, or
					// DUPLICATE -->"
					// + "<result>GOOD</result>"
					+ "</orderoptions>"
					+ "<transactiondetails>"
					+ "<oid>"
					+ txn_no
					+ "-"
					+ invNum
					+ "</oid>"
					+ "<taxexempt>Y</taxexempt>"
					+ "<ponumber>"
					+ txn_no
					+ "</ponumber>"
					// + "<reference_number>"+txn_no+"</reference_number>"
					+ "</transactiondetails>"
					+ "<payment>"
					+ "<subtotal>"
					+ total_amount
					+ "</subtotal>"//
					// -- Replace value with calculated tax -->"
					+ "<tax>0.0</tax>"
					// -- Replace value with calculated shipping charges -->"
					+ "<shipping>0.0</shipping>"
					// + "<vattax>0.00</vattax>"
					+ "<chargetotal>"
					+ total_amount
					+ "</chargetotal>" // becoz of error no charge total is
										// provided
					+ "</payment>"
					+ "<creditcard>"
					+ "<cardnumber>"
					+ credit_card.card_num
					+ "</cardnumber>"
					+ "<cardexpmonth>"
					+ ccExpMonth
					+ "</cardexpmonth>"
					+ "<cardexpyear>"
					+ ccExpYear
					+ "</cardexpyear>"
					// -- CVM is the three-digit security code usually found on
					// the signature line on the back of the card -->"
					// + "<cvmvalue>123</cvmvalue>"
					// + "<cvmindicator>provided</cvmindicator>"
					+ "</creditcard>"
					+ "<billing>"
					// -- Required for Address Verification -->"
					+ "<addrnum>"
					+ f.filterxml(billing_contact.address1)
					+ "</addrnum>"//
					+ "<userid>"
					+ f.filterxml(billing_contact.firstName)
					+ " "
					+ f.filterxml(billing_contact.lastName)
					+ "</userid>"
					+ "<city>"
					+ f.filterxml(billing_contact.city)
					+ "</city>"
					+ "<state>"
					+ f.filterxml(billing_contact.state)
					+ "</state>"
					+ "<zip>"
					+ f.filterxml(billing_contact.zip)
					+ "</zip>"
					// -- Billing name and address et al. -->"
					+ "<name>"
					+ f.filterxml(billing_contact.firstName)
					+ " "
					+ f.filterxml(billing_contact.lastName)
					+ "</name>"
					// + "<company>SomeWhere, Inc.</company>"
					+ "<address1>"
					+ f.filterxml(billing_contact.address1)
					+ "</address1>"
					+ "<address2>"
					+ f.filterxml(Address2)
					+ "</address2>"// "+Address2+"
					+ "<country>US</country>"
					+ "<phone>"
					+ billing_contact.phone
					+ "</phone>"
					// + "<fax>8059876543</fax>"
					+ "<email>"
					+ billing_contact.email
					+ "</email>"
					+ "</billing>"
					+ "<shipping>"
					// -- Shipping name and address -->"
					+ "<name>"
					+ f.filterxml(billing_contact.firstName)
					+ " "
					+ f.filterxml(billing_contact.lastName)
					+ "</name>"
					+ "<address1>"
					+ f.filterxml(billing_contact.address1)
					+ "</address1>"// "
					+ "<address2>" + f.filterxml(Address2) + "</address2>"
					+ "<city>" + f.filterxml(billing_contact.city) + "</city>"
					+ "<state>" + f.filterxml(billing_contact.state)
					+ "</state>" + "<zip>" + f.filterxml(billing_contact.zip)
					+ "</zip>" + "<country>US</country>" + "</shipping>"
					+ "<notes>" + "<comments>" + f.filterxml(commentString)
					+ "</comments>" + "</notes>" + "</order>";

			System.out.println(sXml);			
			String xmlresponse = sendXmlToPaymentGateway(sXml);

			StringTokenizer str = new StringTokenizer(xmlresponse, "<");
			int i = 0;
			while (str.hasMoreTokens()) {

				res_array[i] = f.filterHTML(str.nextToken());
				i++;
			}

			boolean approved = false;
			boolean declined = false;
			boolean transFailed = false;
			String transError = "";
			String auth_code_found = "";
			String trans_num_found = "";
			String response = "";
			if (xmlresponse != null) {
				System.out.println("Time - " + res_array[2].substring(7));
				System.out.println("Ref# - " + res_array[4].substring(6));
				System.out.println("Appr - " + res_array[20].substring(11));

				if ("APPROVED".equals(res_array[20].substring(11))) {
					approved = true;
				} else if ("DECLINED".equals(res_array[20].substring(11))) {
					declined = true;
				} else {
					transFailed = true;
				}

			}
			System.out.println("Code - " + res_array[12].substring(7));
			System.out.println("AVSCode - " + res_array[22].substring(6));
			// System.out.println("PayServ - " + "");
			System.out.println("Err  - " + res_array[6].substring(8));

			String error_reason = StringHandle.removeNull(res_array[6]
					.substring(8));
			String trans_num = StringHandle.removeNull(res_array[8]
					.substring(11));

			System.out.println("Ord# - " + res_array[8].substring(11));
			response += "Time - " + res_array[2].substring(7);
			response += "\n<BR>" + "Ref# - " + res_array[4].substring(6);
			response += "\n<BR>" + "Appr - " + res_array[20].substring(11);
			response += "\n<BR>" + "Code - " + res_array[11].substring(7);
			response += "\n<BR>" + "AVSCode - " + res_array[22].substring(6);
			// response += "\n<BR>"+"PayServ - " + "";
			response += "\n<BR>" + "Err  - " + res_array[6].substring(8);
			response += "\n<BR>" + "Ord# - " + res_array[8].substring(11);
			String AVSCode = res_array[22].substring(6);
			// //////////////////////////////////////
			// END OF YOUR PAY API
			// //////////////////////////////////////

			String passed_params = "";
			passed_params = "account: " + username + "\namount: " + amount
					+ "\ncardNumber: " + ccNum + "\ncardExp: " + ccExpMonth
					+ "/" + ccExpYear + "\noperation: " + xType
					+ "\ncustName1: " + billing_contact.firstName + " "
					+ billing_contact.lastName + "\nstreetAddr: "
					+ billing_contact.address1 + "\nphone: "
					+ billing_contact.phone + "\nemail: "
					+ billing_contact.email + "\ncity: " + billing_contact.city
					+ "\nprovince: " + billing_contact.state + "\nzip: "
					+ billing_contact.zip + "\ncountry: " + "US"
					+ "\nmerchantTxn: " + invNum + "\nmerchantId: " + username
					+ "\nmerchantPwd: " + password + "\nmerchantData: "
					+ "Invoice #" + invNum;

			System.out.println(passed_params);

			if (!declined && transFailed) {

				message = "<B>INVENTORY SOURCE DIRECT CHARGE: Payment Gateway Error!<BR>Message from CardServices: </B>"
						+ Filter
								.transForm(
										"s/\\+/ /gi",
											error_reason
												+ "<BR>Please contact <A HREF=\"mailto:payments@inventorysource.com\" class=\"reg\">payments@inventorysource.com</A>");

				EmailUtil
						.sendEmail(
								"cm@inventorysource.com",
								"CardServices_payments@inventorysource.com",
								"",
								"INVENTORY SOURCE DIRECT CHARGE: Transaction Error!",
								"INVENTORY SOURCE DIRECT CHARGE: ERROR MESSAGE: \"<B>Payment Gateway Error!<BR>Message from CardServices: </B><BR>"
										+ Filter
												.transForm(
														"s/\\+/ /gi",
														StringHandle
																.removeNull(transError))
										+ "<BR>"
										+ "<P>PARAMETERS: <BR>"
										+ Filter.transForm("s/\n/<BR>/",
												passed_params)
										+ "<BR><BR>"
										+ response
										+ "<BR><BR>",
								"text/html");
				return false;
			}

			if (declined || transFailed) {
				// Card Declined
				message = "Transaction Declined.";
				if (!"".equals(transError)) {
					message += "<BR>Reason: " + transError;
				}
				return false;
			} else {
				// Success
				return true;
			}
		} catch (Exception e) {
			ExcHandle.printStackTraceToErr(e);
			if (!"".equals(message)) {
				message += "<BR>";
			}
			message += "Error found in our system. Please contact our support. Error Code: CSI-CHARGE";
			return false; // "Failure";
		}

	}

	private String sendXmlToPaymentGateway(String xml) {
		System.out.println("Sending request to gateway");
		
		CookieModule.setCookiePolicyHandler(null);
		String[] params = new String[]{
				"xml-request",xml,
				"authkey","MONKEYRULES"
				};		
		/*FormObject formObj = new FormObject(
				"app1.inventorysource.com",8081, 
				"/is6/charge.action",
				"","",false,false,false);
		if(params != null && params.length > 0 ) {
			formObj.addData(params);
		}	*/       
		/*formObj.setTimeOut(60*1000*15);
		formObj.hitForm("Post", null);
		*/
		String pageResponseString = "";//formObj.page;
		System.out.println("Gateway response: \n"+pageResponseString);		
		return pageResponseString;
	}
}
