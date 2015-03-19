<?php

//*********************** CONFIGURATION VARIABLES	*******************
$PassKey="02446";//This variable is used for authentication of xml file

//******************	 START OF DATABASE TABLES	***************************


$url = "app/etc/local.xml";
$xml = simplexml_load_file($url);

$host = $xml->global[0]->resources->default_setup->connection->host;
$user = $xml->global[0]->resources->default_setup->connection->username;
$pass = $xml->global[0]->resources->default_setup->connection->password;
$dbname = $xml->global[0]->resources->default_setup->connection->dbname;

define('DB_SERVER', $host);
define('DB_PASS', $pass);
define('DB_USER', $user);
define('DB_NAME', $dbname);

$table_prefix = "";

global $selfupdate, $pullstatus;

define('EAV_ATTRIBUTE',$table_prefix."eav_attribute");
define('EAV_ENTITY_TYPE',$table_prefix."eav_entity_type");
define('SALES_ORDER_ENTITY_VARCHAR',$table_prefix."sales_order_entity_varchar");
define('SALES_FLAT_QUOTE',$table_prefix."sales_flat_quote");
define('SALES_FLAT_QUOTE_ADDRESS',$table_prefix."sales_flat_quote_address");
define('SALES_ORDER',$table_prefix."sales_order");
define('SALES_FLAT_QUOTE_PAYMENT',$table_prefix."sales_flat_quote_payment");
define('SALES_FLAT_QUOTE_ITEM',$table_prefix."sales_flat_quote_item");
define('SALES_FLAT_QUOTE_SHIPPING_RATE',$table_prefix."sales_flat_quote_shipping_rate");
define('SALES_ORDER_VARCHAR',$table_prefix."sales_order_varchar");

$selfupdate = 0;

if(isset($_POST['XML_INPUT_VALUE']))
{
	$pullstatus="pending";
	
	if(isset($_POST['orderpulltype'])){
		$pullstatus = $_POST['orderpulltype'];
	}
	
	$arr_xml = xml2php($_POST['XML_INPUT_VALUE']);
	requestType($arr_xml,$PassKey);
}else{
	echo "Xml request data needed to process this file";
	die();
}



function ping()
{
	$xml_str = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n";
	$xml_str.="<xmlPopulateResponse>\n";
	
	if(! mysql_connect(DB_SERVER, DB_USER, DB_PASS))
		$xml_str.="<heartbeat>Database Connection Error</heartbeat>\n";
	else
		$xml_str.="<heartbeat>Alive</heartbeat>\n";
	$xml_str.="</xmlPopulateResponse>";
	output_xml($xml_str);
}

function xml2php($xml_content)
{
	$xml_parser = xml_parser_create();
	xml_parse_into_struct($xml_parser, $xml_content, $arr_vals);
	if(xml_get_error_code($xml_parser)!=false)
	{
		$xmlstr="<magentoPopulate>\n<magentoProductsImportResponse>";
		$xmlstr.="Error : ".xml_error_string(xml_get_error_code($xml_parser))." At Line No :  ".xml_get_current_line_number($xml_parser);
		$xmlstr.="</magentoProductsImportResponse>\n</magentoPopulate>";
		output_xml($xmlstr);
		exit;
	}
	xml_parser_free($xml_parser);
	return $arr_vals;
}

function requestType($array_haystack,$PassKey)
{
	if ((!empty($array_haystack)) AND (is_array($array_haystack)))
	{
		foreach ($array_haystack as $xml_key => $xml_value)
		{
			//for Ping
			if(strtolower($xml_value["tag"])=="requesttype" && strtolower($xml_value["value"])=="ping")
			{
				$type="Checking for test database connection";
				$cat=strtolower($xml_value["value"]);
			}
			//For Product listing
			if(strtolower($xml_value["tag"])=="requesttype" && strtolower($xml_value["value"])=="getorders")
			{
				$type="Get Product Orders";
				$cat=strtolower($xml_value["value"]);
			}
			if(strtolower($xml_value["tag"])=="requesttype" && strtolower($xml_value["value"])=="updateorders")
			{
				$type="update Product Orders";
				$cat=strtolower($xml_value["value"]);
			}
			if(strtolower($xml_value["tag"])=="passkey")
			{
				$entered_key=strtolower($xml_value["value"]);
				break;
			}
		}
	}

	//This section checks if entered key in xml file is valid
	if($entered_key!=$PassKey)
	{
		echo "<br>Error. Invalid Key<br>";
		exit;
	}
	
	switch($cat)
	{
		case "ping":
			{
				ping();
				break;
			}
		case "getorders":
			{
				getOrders();
				break;
			}
		case "updateorders":
			{
				updateorders($array_haystack);
				break;
			}
	}

}

function getOrders(){
	
	global $pullstatus;
	global $selfupdate;
	
	$xml_str = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n";
	$xml_str .="<OrderXML>";
	
	//$xml_update .= "<UpdateXML>";

	connect_db();
	
	//Getting attribute ids of shipping_method, status and customer_email
	$sqlattrids = "select * from ".EAV_ATTRIBUTE." where attribute_code in ('shipping_method', 'status', 'customer_email') and entity_type_id in (select entity_type_id from ".EAV_ENTITY_TYPE." where entity_type_code = 'order')";
	$resultattrids = mysql_query($sqlattrids);
	while($rows = mysql_fetch_array($resultattrids)){
		$attr_code = $rows['attribute_code'];
		
		if(strtolower($attr_code) == "shipping_method"){
			$attr_shipping_method = $rows['attribute_id'];
		}else if(strtolower($attr_code) == "status"){
			$attr_status = $rows['attribute_id'];
		}else if(strtolower($attr_code) == "customer_email"){
			$attr_customer_email = $rows['attribute_id'];
		}
	}
	
	$sql = "select * from ".SALES_FLAT_QUOTE;
	
	$result = mysql_query($sql);
	while($rows = mysql_fetch_array($result)){
		
		$entity_id = $rows['entity_id'];
		$order_date = $rows['created_at'];
		$grand_total = $rows['grand_total'];
		$currency_code = $rows['quote_currency_code'];
		
		//attribute id 214 = shipping_method, 215 = status and 548 = customer email
		$sql1 = "select * from ".SALES_ORDER_VARCHAR." where attribute_id in (".$attr_shipping_method.", ".$attr_status." ,".$attr_customer_email.") and entity_id =".$entity_id;
		$result1 = mysql_query($sql1);
		while($row1 = mysql_fetch_array($result1)){
			$attr_id = $row1['attribute_id'];
			$value = $row1['value'];
			
			if($attr_id == $attr_shipping_method){
				$shippingmethodcode = $value;
			}else if($attr_id == $attr_status){
				$orderstatus = $value;
			}else if($attr_id == $attr_customer_email){
				$email = $value;
			}
		}
		
		//Get only the orders that are in pending state
		if(strtolower($orderstatus) != $pullstatus){
			continue;
		}
		
		$xml_str .= "<Order>\n";
		
		$sqlOrderId = "select increment_id from ".SALES_ORDER." where entity_id = ".$entity_id;
		$resultOrderId = mysql_query($sqlOrderId);
		while($roworderid = mysql_fetch_array($resultOrderId)){
			$orderid = $roworderid['increment_id'];   // increment_id is order id
		}
		
		$xml_str .= "<o_id>".$orderid."</o_id>";
		//$xml_str .= "<status>".$orderstatus."</status>";  Not in cre thats why comment


		//Creating the xml for update the status from pending to processing.
		$xml_update .= "<order>";
		$xml_update .= "<order_id>".$orderid."</order_id>";
		$xml_update .= "<order_status>processing</order_status>";
		$xml_update .= "</order>";
		//******************//
		
		$sqlPaymentMethod = "select method from ".SALES_FLAT_QUOTE_PAYMENT." where quote_id = ".$entity_id;
		
		$resultPaymentMethod = mysql_query($sqlPaymentMethod);
		while($rowPaymentMethod = mysql_fetch_array($resultPaymentMethod)){
			$paymentmethod = $rowPaymentMethod['method'];
		}
		$xml_str .= "<o_pay_method>".$paymentmethod."</o_pay_method>";
		
		
		//Shipping Details
		$shipDetailsSql = "select * from ".SALES_FLAT_QUOTE_ADDRESS." where quote_id = ".$entity_id." and address_type = 'shipping'";
		$resultShipDetails = mysql_query($shipDetailsSql);
		$xml_str .= "<deliverydetails>";
		while($shipDetails = mysql_fetch_array($resultShipDetails)){
			$firstname = $shipDetails['firstname'];
			$lastname = $shipDetails['lastname'];
			$company = $shipDetails['company'];
			$street = $shipDetails['street'];
			$city = $shipDetails['city'];
			$region = $shipDetails['region'];
			$regionid = $shipDetails['regionid'];
			$postcode = $shipDetails['postcode'];
			$countryid = $shipDetails['country_id'];
			$telephone = $shipDetails['telephone'];
			$fax = $shipDetails['fax'];
			$addressid = $shipDetails['address_id'];
			
			$xml_str .= "<name><![CDATA[".$firstname." ".$lastname."]]></name>";
			$xml_str .= "<streetaddress><![CDATA[".$street."]]></streetaddress>";
			$xml_str .= "<suburb></suburb>";
			$xml_str .= "<city><![CDATA[".$city."]]></city>";
			$xml_str .= "<state><![CDATA[".$region."]]></state>";
			$xml_str .= "<country>".$countryid."</country>";
			$xml_str .= "<zip>".$postcode."</zip>";
			$xml_str .= "<company><![CDATA[".$company."]]></company>";
			$xml_str .= "<phone>".$telephone."</phone>";
			$xml_str .= "<email>".$email."</email>";
			
		}
		$xml_str .= "</deliverydetails>";
		
		//Billing Details
		$billDetailsSql = "select * from ".SALES_FLAT_QUOTE_ADDRESS." where quote_id = ".$entity_id." and address_type = 'billing'";
		$resultBillDetails = mysql_query($billDetailsSql);
		
		$xml_str .="<billingdetails>";
		while($billDetails = mysql_fetch_array($resultBillDetails)){
			$firstname = $billDetails['firstname'];
			$lastname = $billDetails['lastname'];
			$company = $billDetails['company'];
			$street = $billDetails['street'];
			$city = $billDetails['city'];
			$region = $billDetails['region'];
			$regionid = $billDetails['regionid'];
			$postcode = $billDetails['postcode'];
			$countryid = $billDetails['country_id'];
			$telephone = $billDetails['telephone'];
			$fax = $billDetails['fax'];
		
		
			$xml_str .= "<name><![CDATA[".$firstname." ".$lastname."]]></name>";
			$xml_str .= "<streetaddress><![CDATA[".$street."]]></streetaddress>";
			$xml_str .= "<suburb></suburb>";
			$xml_str .= "<city><![CDATA[".$city."]]></city>";
			$xml_str .= "<state><![CDATA[".$region."]]></state>";
			$xml_str .= "<country>".$countryid."</country>";
			$xml_str .= "<zip>".$postcode."</zip>";
			$xml_str .= "<company><![CDATA[".$company."]]></company>";
			$xml_str .= "<phone>".$telephone."</phone>";
			$xml_str .= "<email><![CDATA[".$email."]]></email>";
			
		}
		$xml_str .="</billingdetails>";
		
		
		//Customer Details
		$xml_str .="<customerdetails>";
		$xml_str .="<name></name>";
		$xml_str .="<streetaddress></streetaddress>";
		$xml_str .="<suburb></suburb>";
		$xml_str .="<city></city>";
		$xml_str .="<state></state>";
		$xml_str .="<country></country>";
		$xml_str .="<zip></zip>";
		$xml_str .="<company></company>";
		$xml_str .="<phone></phone>\n";
		$xml_str .="<email></email>\n";				
		$xml_str .="</customerdetails>";						
		
		//Getting ordered products
		$xml_str .= "<products>";
		$sqlProductDetails = "select * from ".SALES_FLAT_QUOTE_ITEM." where quote_id = ".$entity_id." order by item_id";
		$resultProductDetails = mysql_query($sqlProductDetails);
		
		$configurable = 0;
		
		while($rowsProductDetails = mysql_fetch_array($resultProductDetails)){
			$productmodel = $rowsProductDetails['sku'];
			$productname = $rowsProductDetails['name'];
			$producttype = $rowsProductDetails['product_type'];
			$parentid = $rowsProductDetails['parent_item_id'];
			$itemid = $rowsProductDetails['item_id'];
			
			if($configurable == 0){
				$productqty = $rowsProductDetails['qty'];
				$productprice = $rowsProductDetails['price'];
			}
			
			//If the product is configurable then the DB has 2 entries for the same item. the parent product id (have correct qty and price)+ the configured product id (always have qty as 1 and price as 0).
			//So the query will be ordered by SKU. and the price and qty is saved from the parent product id and the sku is taken from the next row
			if(strtolower($producttype) == "configurable"){
				$configurable = 1;
				continue;
			}
			
			if($parentid != NULL){
				if($configurable == 1){
					$configurable = 0;
				}else{
					continue;
				}
				
			}
			
			$xml_str .= "<product>";
			$xml_str .= "<p_model>".$productmodel."</p_model>";
			$xml_str .= "<p_quantity>".intval($productqty)."</p_quantity>";
			$xml_str .= "<p_price_each>".$productprice."</p_price_each>";
			$xml_str .= "</product>";
		}
		
		
		$xml_str .= "</products>";
		$xml_str .= "<p_bill_amount>".$grand_total."</p_bill_amount>";
		$xml_str .= "<o_shipping><![CDATA[".$shippingmethodcode."]]></o_shipping>";
		$xml_str .= "<o_note></o_note>";
		$xml_str .= "<o_time>".$order_date."</o_time>";
		
		$xml_str .= "</Order>\n";
	}
	
//	$xml_update .= "</UpdateXML>";
	
	$selfupdate = 1;

	//Update the status of the orders to make them as "Processing" so that the same order is not pulled again.
/*	if(strtolower($pullstatus) == "pending" && trim($xml_update) != ""){
		echo "1111";
		echo $xml_update;
		updateorders(xml2php($xml_update));
		echo "2222";
	}*/
	
	$selfupdate = 0;
	
	$xml_str .="</OrderXML>";
	output_xml($xml_str) ;
}


function output_xml($content)
{
	header("Content-Type: application/xml; charset=ISO-8859-1");
	header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
	header("Last-Modified: ". gmdate("D, d M Y H:i:s") ." GMT");
	header("Cache-Control: no-store, no-cache, must-revalidate");
	header("Cache-Control: post-check=0, pre-check=0", false);
	header("Pragma: no-cache");
	print $content;

}

function connect_db(){
	$dbhandle = mysql_connect(DB_SERVER, DB_USER, DB_PASS) or die("Unable to connect to MySQL");
	$db_selected = mysql_select_db(DB_NAME,$dbhandle);
}

function updateorders($array_haystack){
	global $selfupdate;
	connect_db();
	$oID="";
	$status="";
	
	//Getting attribute ids of order_status and state
	$sqlattrids = "select * from ".EAV_ATTRIBUTE." where attribute_code in ('status', 'state') and entity_type_id in (select entity_type_id from ".EAV_ENTITY_TYPE." where entity_type_code = 'order')";
	$resultattrids = mysql_query($sqlattrids);
	while($rows = mysql_fetch_array($resultattrids)){
		$attr_code = $rows['attribute_code'];
		
		if(strtolower($attr_code) == "status"){
			$attr_status = $rows['attribute_id'];
		}else if(strtolower($attr_code) == "state"){
			$attr_state = $rows['attribute_id'];
		}
	}
	
		
	$xml_str = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n";
	$xml_str .="<OrderUpdateXML>";
		
	if ((!empty($array_haystack)) AND (is_array($array_haystack)))
	{
		foreach ($array_haystack as $xml_key => $xml_value)
		{
			
			if(strtolower($xml_value["tag"])=="order_id")
			{
				$type="Checking for test database connection";
				$oID=strtolower($xml_value["value"]);
			}
			if(strtolower($xml_value["tag"])=="order_status")
			{
				$type="Checking for test database connection";
				$tempstatus=strtolower($xml_value["value"]);
				if($tempstatus == 1){
					$status = "processing";
				}
			}
			
			if($oID!="" && $status!="") {
				
				$entityIdSql = "select entity_id from ".SALES_ORDER." where increment_id = '".$oID."'";
				
				$resultEntityId = mysql_query($entityIdSql);
				
				while($rowEntityId = mysql_fetch_array($resultEntityId)){
					$entityid = $rowEntityId['entity_id'];
					$updateordersql = "update ".SALES_ORDER_VARCHAR." set value = '" . $status . "' where entity_id = ".$entityid." and attribute_id in (".$attr_status." ,".$attr_state.")";
					$resupdateorder = mysql_query($updateordersql); 
					$xml_str .="<UpdatedOrder>" . $oID . "</UpdatedOrder>";
					
				}
				$oID="";
				$status="";
			} 
			
		}
	}  
	
	$xml_str .="</OrderUpdateXML>";
	if($selfupdate == 0){
		output_xml($xml_str);
	}
	
}

exit;
?>
