package salesmachine.oim.suppliers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.hibernate.Query;
import org.hibernate.Session;

import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimSuppliers;
import salesmachine.hibernatedb.OimVendorShippingMap;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.OimVendorsuppOrderhistory;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.util.OimLogStream;
import salesmachine.util.StringHandle;

import com.suppliers.pcs.OrderReturnInfo;

public abstract class Supplier {
	protected ArrayList successfulOrders = new ArrayList();
	protected ArrayList failedOrders = new ArrayList();
	protected HashMap stateCodeMapping = new HashMap();
	protected HashMap countryCodeMapping = new HashMap();
	protected HashMap orderSkuPrefixMap = new HashMap();
	protected OimLogStream logStream = new OimLogStream();
	protected static final Integer ERROR_ORDER_PROCESSING = new Integer(3);
	
	public Supplier() {
		logStream.println("!! CUstom processing initiated");
		createStateNameMapping();
		createCountryCodeMapping();		
	}
	
	public void setLogStream(OimLogStream ls) {
		logStream = ls;		
	}
	
	abstract public void sendOrders(Integer vendorId, OimVendorSuppliers ovs,
			List orders);
	
	protected String getUSStateFullName(String stateCode) {
		if (stateCodeMapping.containsKey(stateCode))
			return (String) stateCodeMapping.get(stateCode);
		return null;
	}
	
	protected void updateVendorSupplierOrderHistory(Integer vid,
			OimVendorSuppliers ovs, Object response) {
		Session session = SessionManager.currentSession();
		OimVendorsuppOrderhistory history = new OimVendorsuppOrderhistory();
		Vendors vendor = new Vendors();
		vendor.setVendorId(vid);
		history.setVendors(vendor);
		history.setOimSuppliers(ovs.getOimSuppliers());
		history.setProcessingTm(new Date());
		history.setErrorCode(ERROR_ORDER_PROCESSING);
		
		if (response != null){
			if(response instanceof OrderReturnInfo){
				history.setDescription(((OrderReturnInfo)response).getReturnValue());
			}else{
				history.setDescription(response.toString());
			}
		}
		
		session.save(history);
		logStream
				.println("!!! Added the order processing output to vendor supplier order history");
	}		
	

	/***
	 * 
	 * @param ovs OimVendorSupplier object
	 * @return hashmap containing the channelids and sku prefix mentioned in the channel supplier map
	 */
	protected HashMap setSkuPrefixForOrders(OimVendorSuppliers ovs){
		HashMap prefixChannelsSupplier = new HashMap();
		Session session = SessionManager.currentSession();
		Query query = session
				.createQuery("select ocs.oimChannels, ocs.supplierPrefix from salesmachine.hibernatedb.OimChannelSupplierMap ocs where ocs.oimSuppliers=:supp ");
		Iterator iter = query.setEntity("supp", ovs.getOimSuppliers()).iterate();
		while(iter.hasNext()){
			Object[] row = (Object[])iter.next();
			OimChannels chan = (OimChannels)row[0];
			String prefix = (String)row[1];
			prefix = StringHandle.removeNull(prefix);
			prefixChannelsSupplier.put(chan.getChannelId(), prefix);
		}
		return prefixChannelsSupplier;
	}

	public static HashMap loadSupplierShippingMap(Session dbSession, OimSuppliers s, Vendors v) {
		Query query = dbSession.createQuery("from salesmachine.hibernatedb.OimVendorShippingMap c " +
								"where c.oimSuppliers=:supp and c.vendors=:vendor");
		query.setEntity("supp", s);
		query.setEntity("vendor", v);
		List methods = query.list();
		
		HashMap shipMap = new HashMap();
		for (Iterator it = methods.iterator(); it.hasNext();) {
			OimVendorShippingMap vsm = (OimVendorShippingMap)it.next();
			String shippingText = vsm.getShippingText();
			String shippingCode = vsm.getOimShippingMethod().getShippingCode();
			shipMap.put(shippingText, shippingCode);
			
			System.out.println(shippingText + " => " + shippingCode);
		}
			
		return shipMap;
	}
	
	public static String findShippingCodeFromUserMapping(HashMap shippingMethods, String shippingDetails) {
		String code = (String)shippingMethods.get(shippingDetails);
		if (code != null && StringHandle.removeNull(code).length() > 0) {			
			return code;
		}
		
		// Find the best match now
		List shippingWords = new ArrayList();
		StringTokenizer tokenizer = new StringTokenizer(shippingDetails.toLowerCase()," ");
		while(tokenizer.hasMoreTokens()){
			String token = tokenizer.nextToken();
			shippingWords.add(token);
		}		
		
		int maxMatch = 0;
		String closestKey = "";
		
		for (Iterator it = shippingMethods.keySet().iterator(); it.hasNext();) {
			String shipMethod = (String)it.next();
			int matchCount = 0;
			for (Iterator tmp = shippingWords.iterator(); tmp.hasNext();) {
				String word = (String)tmp.next();				
				if (word.length() < 3) continue;
				
				if (shipMethod.toLowerCase().contains(word)) 
					matchCount++;
			}
			
			if (matchCount > maxMatch) {
				maxMatch = matchCount;
				closestKey = shipMethod;
			}
		}
		
		if (maxMatch > 0 && maxMatch > shippingWords.size()/2) {
			System.out.println(shippingDetails + " ["+maxMatch+"] " + closestKey);
			return (String)shippingMethods.get(closestKey);
		} else
			return "";
	}

	public static String findShippingCodeFromSuggested(HashMap shippingMethods, String shippingDetails) {
		String shippingMethodCode = "";
		// iterate over the shipping methods to find the shippingcode based
		// on the order shipping details.
		Iterator shipMethodsIt = shippingMethods.keySet().iterator();		
		
		while (shipMethodsIt.hasNext()) {
			String key = (String) shipMethodsIt.next();
			if (shippingDetails.contains(key)) {
				HashMap detailedShippingMethods = (HashMap) shippingMethods
						.get(key);
				Iterator iter = detailedShippingMethods.keySet().iterator();
				int largestMatchCount=0;
				
				while (iter.hasNext()) {
					
					String code = (String) iter.next();
					String value = (String) detailedShippingMethods
							.get(code);
					
					StringTokenizer tokenizer = new StringTokenizer(value," ");
					int currentMatchCount=0;
					int currentNotMatchCount=0;
					boolean notFound = false;
					while(tokenizer.hasMoreTokens()){
						String token = tokenizer.nextToken();
						if(shippingDetails.contains(token)){
							currentMatchCount++;
						}else{
							currentNotMatchCount++;
						}
					}
					
					if(currentMatchCount>currentNotMatchCount && currentMatchCount>largestMatchCount){
						shippingMethodCode = code;
						largestMatchCount = currentMatchCount;
					}
				}
				
			}	// if (shippingDetails.contains(key)) {
		}	// while (shipMethodsIt.hasNext()) {		
		
		return shippingMethodCode;
	}
		
	public ArrayList getSuccessfulOrders() {
		return successfulOrders;
	}

	public ArrayList getFailedOrders() {
		return failedOrders;
	}	
	
	public HashMap getDefaultShippingMapping() {
		return null;
	}
	/***
	 * US states mapping with 2 digit codes.
	 */
	protected void createStateNameMapping() {
		stateCodeMapping.put("AL", "Alabama");
		stateCodeMapping.put("AK", "Alaska");
		stateCodeMapping.put("AS", "American Samoa");
		stateCodeMapping.put("AZ", "Arizona");
		stateCodeMapping.put("AR", "Arkansas");
		stateCodeMapping.put("CA", "California");
		stateCodeMapping.put("CO", "Colorado");
		stateCodeMapping.put("CT", "Connecticut");
		stateCodeMapping.put("DE", "Delaware");
		stateCodeMapping.put("DC", "Dist. of Columbia");
		stateCodeMapping.put("FL", "Florida");
		stateCodeMapping.put("GA", "Georgia");
		stateCodeMapping.put("GU", "GUAM");
		stateCodeMapping.put("HI", "Hawaii");
		stateCodeMapping.put("ID", "Idaho");
		stateCodeMapping.put("IL", "Illinois");
		stateCodeMapping.put("IN", "Indiana");
		stateCodeMapping.put("IA", "Iowa");
		stateCodeMapping.put("KS", "Kansas");
		stateCodeMapping.put("KY", "Kentucky");
		stateCodeMapping.put("LA", "Louisiana");
		stateCodeMapping.put("ME", "Maine");
		stateCodeMapping.put("MD", "Maryland");
		stateCodeMapping.put("MH", "Marshall Islands");
		stateCodeMapping.put("MA", "Massachusetts");
		stateCodeMapping.put("MI", "Michigan");
		stateCodeMapping.put("FM", "Micronesia");
		stateCodeMapping.put("MN", "Minnesota");
		stateCodeMapping.put("MS", "Mississippi");
		stateCodeMapping.put("MO", "Missouri");
		stateCodeMapping.put("MT", "Montana");
		stateCodeMapping.put("NE", "Nebraska");
		stateCodeMapping.put("NV", "Nevada");
		stateCodeMapping.put("NH", "New Hampshire");
		stateCodeMapping.put("NJ", "New Jersey");
		stateCodeMapping.put("NM", "New Mexico");
		stateCodeMapping.put("NY", "New York");
		stateCodeMapping.put("NC", "North Carolina");
		stateCodeMapping.put("ND", "North Dakota");
		stateCodeMapping.put("MP", "Northern Marianas");
		stateCodeMapping.put("OH", "Ohio");
		stateCodeMapping.put("OK", "Oklahoma");
		stateCodeMapping.put("OR", "Oregon");
		stateCodeMapping.put("PW", "Palau");
		stateCodeMapping.put("PA", "Pennsylvania");
		stateCodeMapping.put("PR", "Puerto Rico");
		stateCodeMapping.put("RI", "Rhode Island");
		stateCodeMapping.put("SC", "South Carolina");
		stateCodeMapping.put("SD", "South Dakota");
		stateCodeMapping.put("TN", "Tennessee");
		stateCodeMapping.put("TX", "Texas");
		stateCodeMapping.put("UT", "Utah");
		stateCodeMapping.put("VT", "Vermont");
		stateCodeMapping.put("VA", "Virginia");
		stateCodeMapping.put("VI", "Virgin Islands");
		stateCodeMapping.put("WA", "Washington");
		stateCodeMapping.put("WV", "West Virginia");
		stateCodeMapping.put("WI", "Wisconsin");
		stateCodeMapping.put("WY", "Wyoming");
	}

	
	/***
	 * creates the country mapping with 2 digit country code.
	 */
	protected void createCountryCodeMapping(){
		countryCodeMapping.put("Afghanistan", "AF");
		countryCodeMapping.put("Aland Islands", "AX");
		countryCodeMapping.put("Albania", "AL");
		countryCodeMapping.put("Algeria", "DZ");
		countryCodeMapping.put("American Samoa", "AS");
		countryCodeMapping.put("Andorra", "AD");
		countryCodeMapping.put("Angola", "AO");
		countryCodeMapping.put("Anguilla", "AI");
		countryCodeMapping.put("Antarctica", "AQ");
		countryCodeMapping.put("Antigua and Barbuda", "AG");
		countryCodeMapping.put("Argentina", "AR");
		countryCodeMapping.put("Armenia", "AM");
		countryCodeMapping.put("Aruba", "AW");
		countryCodeMapping.put("Ascension Island", "AC");
		countryCodeMapping.put("Australia", "AU");
		countryCodeMapping.put("Austria", "AT");
		countryCodeMapping.put("Azerbaijan", "AZ");
		countryCodeMapping.put("Bahamas", "BS");
		countryCodeMapping.put("Bahrain", "BH");
		countryCodeMapping.put("Barbados", "BB");
		countryCodeMapping.put("Bangladesh", "BD");
		countryCodeMapping.put("Belarus", "BY");
		countryCodeMapping.put("Belgium", "BE");
		countryCodeMapping.put("Belize", "BZ");
		countryCodeMapping.put("Benin", "BJ");
		countryCodeMapping.put("Bermuda", "BM");
		countryCodeMapping.put("Bhutan", "BT");
		countryCodeMapping.put("Botswana", "BW");
		countryCodeMapping.put("Bolivia", "BO");
		countryCodeMapping.put("Bosnia and Herzegovina", "BA");
		countryCodeMapping.put("Bouvet Island", "BV");
		countryCodeMapping.put("Brazil", "BR");
		countryCodeMapping.put("British Indian Ocean Territory", "IO");
		countryCodeMapping.put("Brunei Darussalam", "BN");
		countryCodeMapping.put("Bulgaria", "BG");
		countryCodeMapping.put("Burkina Faso", "BF");
		countryCodeMapping.put("Burundi", "BI");
		countryCodeMapping.put("Cambodia", "KH");
		countryCodeMapping.put("Cameroon", "CM");
		countryCodeMapping.put("Canada", "CA");
		countryCodeMapping.put("Cape Verde", "CV");
		countryCodeMapping.put("Cayman Islands", "KY");
		countryCodeMapping.put("Central African Republic", "CF");
		countryCodeMapping.put("Chad", "TD");
		countryCodeMapping.put("Chile", "CL");
		countryCodeMapping.put("China", "CN");
		countryCodeMapping.put("Christmas Island", "CX");
		countryCodeMapping.put("Cocos (Keeling) Islands", "CC");
		countryCodeMapping.put("Colombia", "CO");
		countryCodeMapping.put("Comoros", "KM");
		countryCodeMapping.put("Congo", "CG");
		countryCodeMapping.put("Congo, Democratic Republic", "CD");
		countryCodeMapping.put("Cook Islands", "CK");
		countryCodeMapping.put("Costa Rica", "CR");
		countryCodeMapping.put("Cote D'Ivoire (Ivory Coast)", "CI");
		countryCodeMapping.put("Croatia (Hrvatska)", "HR");
		countryCodeMapping.put("Cuba", "CU");
		countryCodeMapping.put("Cyprus", "CY");
		countryCodeMapping.put("Czech Republic", "CZ");
		countryCodeMapping.put("Czechoslovakia (former)", "CS");
		countryCodeMapping.put("Denmark", "DK");
		countryCodeMapping.put("Djibouti", "DJ");
		countryCodeMapping.put("Dominica", "DM");
		countryCodeMapping.put("Dominican Republic", "DO");
		countryCodeMapping.put("East Timor", "TP");
		countryCodeMapping.put("Ecuador", "EC");
		countryCodeMapping.put("Egypt", "EG");
		countryCodeMapping.put("El Salvador", "SV");
		countryCodeMapping.put("Equatorial Guinea", "GQ");
		countryCodeMapping.put("Eritrea", "ER");
		countryCodeMapping.put("Estonia", "EE");
		countryCodeMapping.put("Ethiopia", "ET");
		countryCodeMapping.put("Falkland Islands (Malvinas)", "FK");
		countryCodeMapping.put("Faroe Islands", "FO");
		countryCodeMapping.put("Fiji", "FJ");
		countryCodeMapping.put("Finland", "FI");
		countryCodeMapping.put("France", "FR");
		countryCodeMapping.put("France, Metropolitan", "FX");
		countryCodeMapping.put("French Guiana", "GF");
		countryCodeMapping.put("French Polynesia", "PF");
		countryCodeMapping.put("French Southern Territories", "TF");
		countryCodeMapping.put("F.Y.R.O.M. (Macedonia)", "MK");
		countryCodeMapping.put("Gabon", "GA");
		countryCodeMapping.put("Gambia", "GM");
		countryCodeMapping.put("Georgia", "GE");
		countryCodeMapping.put("Germany", "DE");
		countryCodeMapping.put("Ghana", "GH");
		countryCodeMapping.put("Gibraltar", "GI");
		countryCodeMapping.put("Great Britain (UK)", "GB");
		countryCodeMapping.put("Greece", "GR");
		countryCodeMapping.put("Greenland", "GL");
		countryCodeMapping.put("Grenada", "GD");
		countryCodeMapping.put("Guadeloupe", "GP");
		countryCodeMapping.put("Guam", "GU");
		countryCodeMapping.put("Guatemala", "GT");
		countryCodeMapping.put("Guernsey", "GG");
		countryCodeMapping.put("Guinea", "GN");
		countryCodeMapping.put("Guinea-Bissau", "GW");
		countryCodeMapping.put("Guyana", "GY");
		countryCodeMapping.put("Haiti", "HT");
		countryCodeMapping.put("Heard and McDonald Islands", "HM");
		countryCodeMapping.put("Honduras", "HN");
		countryCodeMapping.put("Hong Kong", "HK");
		countryCodeMapping.put("Hungary", "HU");
		countryCodeMapping.put("Iceland", "IS");
		countryCodeMapping.put("India", "IN");
		countryCodeMapping.put("Indonesia", "ID");
		countryCodeMapping.put("Iran", "IR");
		countryCodeMapping.put("Iraq", "IQ");
		countryCodeMapping.put("Ireland", "IE");
		countryCodeMapping.put("Israel", "IL");
		countryCodeMapping.put("Isle of Man", "IM");
		countryCodeMapping.put("Italy", "IT");
		countryCodeMapping.put("Jersey", "JE");
		countryCodeMapping.put("Jamaica", "JM");
		countryCodeMapping.put("Japan", "JP");
		countryCodeMapping.put("Jordan", "JO");
		countryCodeMapping.put("Kazakhstan", "KZ");
		countryCodeMapping.put("Kenya", "KE");
		countryCodeMapping.put("Kiribati", "KI");
		countryCodeMapping.put("Korea (North)", "KP");
		countryCodeMapping.put("Korea (South)", "KR");
		countryCodeMapping.put("Kosovo", "XK");
		countryCodeMapping.put("Kuwait", "KW");
		countryCodeMapping.put("Kyrgyzstan", "KG");
		countryCodeMapping.put("Laos", "LA");
		countryCodeMapping.put("Latvia", "LV");
		countryCodeMapping.put("Lebanon", "LB");
		countryCodeMapping.put("Liechtenstein", "LI");
		countryCodeMapping.put("Liberia", "LR");
		countryCodeMapping.put("Libya", "LY");
		countryCodeMapping.put("Lesotho", "LS");
		countryCodeMapping.put("Lithuania", "LT");
		countryCodeMapping.put("Luxembourg", "LU");
		countryCodeMapping.put("Macau", "MO");
		countryCodeMapping.put("Madagascar", "MG");
		countryCodeMapping.put("Malawi", "MW");
		countryCodeMapping.put("Malaysia", "MY");
		countryCodeMapping.put("Maldives", "MV");
		countryCodeMapping.put("Mali", "ML");
		countryCodeMapping.put("Malta", "MT");
		countryCodeMapping.put("Marshall Islands", "MH");
		countryCodeMapping.put("Martinique", "MQ");
		countryCodeMapping.put("Mauritania", "MR");
		countryCodeMapping.put("Mauritius", "MU");
		countryCodeMapping.put("Mayotte", "YT");
		countryCodeMapping.put("Mexico", "MX");
		countryCodeMapping.put("Micronesia", "FM");
		countryCodeMapping.put("Moldova", "MD");
		countryCodeMapping.put("Monaco", "MC");
		countryCodeMapping.put("Montenegro", "ME");
		countryCodeMapping.put("Montserrat", "MS");
		countryCodeMapping.put("Morocco", "MA");
		countryCodeMapping.put("Mozambique", "MZ");
		countryCodeMapping.put("Myanmar", "MM");
		countryCodeMapping.put("Namibia", "NA");
		countryCodeMapping.put("Nauru", "NR");
		countryCodeMapping.put("Nepal", "NP");
		countryCodeMapping.put("Netherlands", "NL");
		countryCodeMapping.put("Netherlands Antilles", "AN");
		countryCodeMapping.put("Neutral Zone", "NT");
		countryCodeMapping.put("New Caledonia", "NC");
		countryCodeMapping.put("New Zealand (Aotearoa)", "NZ");
		countryCodeMapping.put("Nicaragua", "NI");
		countryCodeMapping.put("Niger", "NE");
		countryCodeMapping.put("Nigeria", "NG");
		countryCodeMapping.put("Niue", "NU");
		countryCodeMapping.put("Norfolk Island", "NF");
		countryCodeMapping.put("Northern Mariana Islands", "MP");
		countryCodeMapping.put("Norway", "NO");
		countryCodeMapping.put("Oman", "OM");
		countryCodeMapping.put("Pakistan", "PK");
		countryCodeMapping.put("Palau", "PW");
		countryCodeMapping.put("Palestinian Territory, Occupied", "PS");
		countryCodeMapping.put("Panama", "PA");
		countryCodeMapping.put("Papua New Guinea", "PG");
		countryCodeMapping.put("Paraguay", "PY");
		countryCodeMapping.put("Peru", "PE");
		countryCodeMapping.put("Philippines", "PH");
		countryCodeMapping.put("Pitcairn", "PN");
		countryCodeMapping.put("Poland", "PL");
		countryCodeMapping.put("Portugal", "PT");
		countryCodeMapping.put("Puerto Rico", "PR");
		countryCodeMapping.put("Qatar", "QA");
		countryCodeMapping.put("Reunion", "RE");
		countryCodeMapping.put("Romania", "RO");
		countryCodeMapping.put("Russian Federation", "RU");
		countryCodeMapping.put("Rwanda", "RW");
		countryCodeMapping.put("S. Georgia and S. Sandwich Isls.", "GS");
		countryCodeMapping.put("Saint Kitts and Nevis", "KN");
		countryCodeMapping.put("Saint Lucia", "LC");
		countryCodeMapping.put("Saint Vincent & the Grenadines", "VC");
		countryCodeMapping.put("Samoa", "WS");
		countryCodeMapping.put("San Marino", "SM");
		countryCodeMapping.put("Sao Tome and Principe", "ST");
		countryCodeMapping.put("Saudi Arabia", "SA");
		countryCodeMapping.put("Senegal", "SN");
		countryCodeMapping.put("Serbia", "RS");
		countryCodeMapping.put("Seychelles", "SC");
		countryCodeMapping.put("Sierra Leone", "SL");
		countryCodeMapping.put("Singapore", "SG");
		countryCodeMapping.put("Slovenia", "SI");
		countryCodeMapping.put("Slovak Republic", "SK");
		countryCodeMapping.put("Solomon Islands", "SB");
		countryCodeMapping.put("Somalia", "SO");
		countryCodeMapping.put("South Africa", "ZA");
		countryCodeMapping.put("S. Georgia and S. Sandwich Isls.", "GS");
		countryCodeMapping.put("Spain", "ES");
		countryCodeMapping.put("Sri Lanka", "LK");
		countryCodeMapping.put("St. Helena", "SH");
		countryCodeMapping.put("St. Pierre and Miquelon", "PM");
		countryCodeMapping.put("Sudan", "SD");
		countryCodeMapping.put("Suriname", "SR");
		countryCodeMapping.put("Svalbard & Jan Mayen Islands", "SJ");
		countryCodeMapping.put("Swaziland", "SZ");
		countryCodeMapping.put("Sweden", "SE");
		countryCodeMapping.put("Switzerland", "CH");
		countryCodeMapping.put("Syria", "SY");
		countryCodeMapping.put("Taiwan", "TW");
		countryCodeMapping.put("Tajikistan", "TJ");
		countryCodeMapping.put("Tanzania", "TZ");
		countryCodeMapping.put("Thailand", "TH");
		countryCodeMapping.put("Togo", "TG");
		countryCodeMapping.put("Tokelau", "TK");
		countryCodeMapping.put("Tonga", "TO");
		countryCodeMapping.put("Trinidad and Tobago", "TT");
		countryCodeMapping.put("Tunisia", "TN");
		countryCodeMapping.put("Turkey", "TR");
		countryCodeMapping.put("Turkmenistan", "TM");
		countryCodeMapping.put("Turks and Caicos Islands", "TC");
		countryCodeMapping.put("Tuvalu", "TV");
		countryCodeMapping.put("Uganda", "UG");
		countryCodeMapping.put("Ukraine", "UA");
		countryCodeMapping.put("United Arab Emirates", "AE");
		countryCodeMapping.put("United Kingdom", "UK");
		countryCodeMapping.put("United States", "US");
		countryCodeMapping.put("US Minor Outlying Islands", "UM");
		countryCodeMapping.put("Uruguay", "UY");
		countryCodeMapping.put("USSR (former)", "SU");
		countryCodeMapping.put("Uzbekistan", "UZ");
		countryCodeMapping.put("Vanuatu", "VU");
		countryCodeMapping.put("Vatican City State (Holy See)", "VA");
		countryCodeMapping.put("Venezuela", "VE");
		countryCodeMapping.put("Viet Nam", "VN");
		countryCodeMapping.put("British Virgin Islands", "VG");
		countryCodeMapping.put("Virgin Islands (U.S.)", "VI");
		countryCodeMapping.put("Wallis and Futuna Islands", "WF");
		countryCodeMapping.put("Western Sahara", "EH");
		countryCodeMapping.put("Yemen", "YE");
		countryCodeMapping.put("Yugoslavia (former)", "YU");
		countryCodeMapping.put("Zambia", "ZM");
		countryCodeMapping.put("See CD Congo, Democratic Republic", "ZR");
		countryCodeMapping.put("Zaire", "ZR");
		countryCodeMapping.put("Zimbabwe", "ZW");		
	}	
	
	protected void duplicateMapWithPrefixes(HashMap map, HashMap tmpmap, String[] prefixes) {
		for (int i=0;i<prefixes.length;i++) {
			String prefix = prefixes[i];
			for (Iterator it = tmpmap.keySet().iterator(); it.hasNext();) {
				String tmpKey = (String)it.next();
				String tmpValue = (String)tmpmap.get(tmpKey);
				map.put(prefix+tmpKey, tmpValue);
			}
		}		
	}	
}
