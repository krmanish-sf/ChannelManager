<?php
//php display error 
ini_set("display_errors", 1);
error_reporting(E_ALL);
//*********************** CONFIGURATION VARIABLES	*******************
$PassKey="02446";//This variable is used for authentication of xml file

//******************	 START OF DATABASE TABLES	***************************


$url = "app/etc/local.xml";
$xml = simplexml_load_file($url);

$host = $xml->global[0]->resources->default_setup->connection->host;
$user = $xml->global[0]->resources->default_setup->connection->username;
$pass = $xml->global[0]->resources->default_setup->connection->password;
$dbname = $xml->global[0]->resources->default_setup->connection->dbname;
$table_prefix = $xml->global[0]->resources->db->table_prefix;

define('DB_SERVER', $host);
define('DB_PASS', $pass);
define('DB_USER', $user);
define('DB_NAME', $dbname);

//$table_prefix = "mage";

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

define('SALES_FLAT_ORDER',$table_prefix."sales_flat_order");
define('SALES_FLAT_ORDER_GRID',$table_prefix."sales_flat_order_grid");
define('SALES_ORDER_STATUS_STATE',$table_prefix."sales_order_status_state");
define('SALES_FLAT_ORDER_ITEM',$table_prefix."sales_flat_order_item");
define('SALES_FLAT_ORDER_STATUS_HISTORY',$table_prefix."sales_flat_order_status_history");
define('SALES_FLAT_SHIPMENT',$table_prefix."sales_flat_shipment");
define('SALES_FLAT_SHIPMENT_COMMENT',$table_prefix."sales_flat_shipment_comment");
define('SALES_FLAT_SHIPMENT_GRID',$table_prefix."sales_flat_shipment_grid");
define('SALES_FLAT_SHIPMENT_ITEM',$table_prefix."sales_flat_shipment_item");


$selfupdate = 0;
//vr_dump ($_POST);
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
	
	if(! @mysql_connect(DB_SERVER, DB_USER, DB_PASS))
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
			if(strtolower($xml_value["tag"])=="orderstatus")
			{
				$type="Get Product Orders";
				$order_status_name=$xml_value["value"];
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
				getOrders($order_status_name);
				break;
			}
		case "updateorders":
			{
				updateorders($array_haystack, $order_status_name);
				break;
			}
	}

}

function getOrders($order_status_name){
	
	global $pullstatus;
	global $selfupdate;
	
	$xml_str = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n";
	$xml_str .="<OrderXML>";
	
	//$xml_update .= "<UpdateXML>";

	connect_db();
	
	$sql = "select entity_id, date_format(created_at,'%m-%d-%Y %T') created_at, grand_total, order_currency_code, customer_email, status, shipping_method, shipping_description, increment_id, quote_id from ".SALES_FLAT_ORDER." where status = '".$order_status_name."' ";
	//$xml_str .="<echo2>".$sql."</echo2>";
	$result = mysql_query($sql);
	while($rows = mysql_fetch_array($result)){
		
		$entity_id = $rows['entity_id'];
		$order_date = $rows['created_at'];
		$grand_total = $rows['grand_total'];
		$currency_code = $rows['order_currency_code'];
		$email = $rows['customer_email'];
		$orderstatus = $rows['status'];
		$shippingmethodcode = $rows['shipping_method'];
		$shippingmethoddescription = $rows['shipping_description'];
		$orderid = $rows['increment_id']; 
		$quote_id = $rows['quote_id']; 
		
		//Get only the orders that are in pending state
		if(strtolower($orderstatus) != $pullstatus){
			continue;
		}
		
		$xml_str .= "<Order>\n";
		$xml_str .= "<o_id><![CDATA[".$orderid."]]></o_id>";
		//$xml_str .= "<status>".$orderstatus."</status>";  Not in cre thats why comment

		//Creating the xml for update the status from pending to processing.
		/*$xml_update .= "<order>";
		$xml_update .= "<order_id>".$orderid."</order_id>";
		$xml_update .= "<order_status>".$orderstatus."</order_status>";
		$xml_update .= "</order>";*/
		//******************//
		
		$sqlPaymentMethod = "select method from ".SALES_FLAT_QUOTE_PAYMENT." where quote_id = ".$quote_id;
		$resultPaymentMethod = mysql_query($sqlPaymentMethod);
		$paymentmethod = "";
		while($rowPaymentMethod = mysql_fetch_array($resultPaymentMethod)){
			$paymentmethod = $rowPaymentMethod['method'];
		}
		$xml_str .= "<o_pay_method><![CDATA[".$paymentmethod."]]></o_pay_method>";
		
		
		//Shipping Details
		$shipDetailsSql = "select * from ".SALES_FLAT_QUOTE_ADDRESS." where quote_id = ".$quote_id." and address_type = 'shipping'";
		//$xml_str .="<echo6>".$shipDetailsSql."</echo6>";
		$resultShipDetails = mysql_query($shipDetailsSql);
		$xml_str .= "<deliverydetails>";
		while($shipDetails = mysql_fetch_array($resultShipDetails)){
			$firstname = $shipDetails['firstname'];
			$lastname = $shipDetails['lastname'];
			$company = $shipDetails['company'];
			$street = $shipDetails['street'];
			$city = $shipDetails['city'];
			$region = $shipDetails['region'];
			$regionid = $shipDetails['region_id'];
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
			$xml_str .= "<country><![CDATA[".$countryid."]]></country>";
			$xml_str .= "<zip><![CDATA[".$postcode."]]></zip>";
			$xml_str .= "<company><![CDATA[".$company."]]></company>";
			$xml_str .= "<phone><![CDATA[".$telephone."]]></phone>";
			$xml_str .= "<email><![CDATA[".$email."]]></email>";
			
		}
		$xml_str .= "</deliverydetails>";
		
		//Billing Details
		$billDetailsSql = "select * from ".SALES_FLAT_QUOTE_ADDRESS." where quote_id = ".$quote_id." and address_type = 'billing'";
		//$xml_str .="<echo7>".$billDetailsSql."</echo7>";
		$resultBillDetails = mysql_query($billDetailsSql);
		
		$xml_str .="<billingdetails>";
		while($billDetails = mysql_fetch_array($resultBillDetails)){
			$firstname = $billDetails['firstname'];
			$lastname = $billDetails['lastname'];
			$company = $billDetails['company'];
			$street = $billDetails['street'];
			$city = $billDetails['city'];
			$region = $billDetails['region'];
			$regionid = $billDetails['region_id'];
			$postcode = $billDetails['postcode'];
			$countryid = $billDetails['country_id'];
			$telephone = $billDetails['telephone'];
			$fax = $billDetails['fax'];
		
		
			$xml_str .= "<name><![CDATA[".$firstname." ".$lastname."]]></name>";
			$xml_str .= "<streetaddress><![CDATA[".$street."]]></streetaddress>";
			$xml_str .= "<suburb></suburb>";
			$xml_str .= "<city><![CDATA[".$city."]]></city>";
			$xml_str .= "<state><![CDATA[".$region."]]></state>";
			$xml_str .= "<country><![CDATA[".$countryid."]]></country>";
			$xml_str .= "<zip><![CDATA[".$postcode."]]></zip>";
			$xml_str .= "<company><![CDATA[".$company."]]></company>";
			$xml_str .= "<phone><![CDATA[".$telephone."]]></phone>";
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
		$sqlProductDetails = "select * from ".SALES_FLAT_QUOTE_ITEM." where quote_id = ".$quote_id." order by item_id";
		//$xml_str .="<echo8>".$sqlProductDetails."</echo8>";
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
			$xml_str .= "<p_model><![CDATA[".$productmodel."]]></p_model>";
			$xml_str .= "<p_quantity><![CDATA[".intval($productqty)."]]></p_quantity>";
			$xml_str .= "<p_price_each><![CDATA[".$productprice."]]></p_price_each>";
			$xml_str .= "<p_name><![CDATA[".$productname."]]></p_name>";
			$xml_str .= "</product>";
		}
		
		
		$xml_str .= "</products>";
		$xml_str .= "<p_bill_amount><![CDATA[".$grand_total."]]></p_bill_amount>";
		$xml_str .= "<o_shipping><![CDATA[".$shippingmethodcode."]]></o_shipping>";
		$xml_str .= "<o_note></o_note>";
		$xml_str .= "<o_time><![CDATA[".$order_date."]]></o_time>";
		
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
	$dbhandle = @mysql_connect(DB_SERVER, DB_USER, DB_PASS) or die("Unable to connect to MySQL");
	$db_selected = mysql_select_db(DB_NAME,$dbhandle);
}

function updateorders($array_haystack, $order_status_name){
	global $selfupdate;
	connect_db();
	$oID="";
	$status="";
	$state="";
	//var_dump($order_status_name);
	//var_dump($array_haystack);

	$xml_str = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n";
	$xml_str .="<OrderUpdateXML>";
		
	if ((!empty($array_haystack)) AND (is_array($array_haystack)))
	{
		foreach ($array_haystack as $xml_key => $xml_value)
		{
			//echo "\n".strtolower($xml_value["tag"]);
			if(strtolower($xml_value["tag"])=="order_id")
			{
				$type="Checking for test database connection";
				$oID=strtolower($xml_value["value"]);
				//echo "\n".strtolower($xml_value["value"]);
			}
			if(strtolower($xml_value["tag"])=="order_status")
			{
				$type="Checking for test database connection";
				$status=strtolower($xml_value["value"]);
				//echo "\n".strtolower($xml_value["value"]);
			}
			
			if($oID!="" && $status!="" && $status == "processing") {
				
				$stateSql = "SELECT state FROM ".SALES_ORDER_STATUS_STATE." where status = '".$status."'";
				//echo "\n01".$stateSql;
				$resultState = mysql_query($stateSql);
				while($rowState = mysql_fetch_array($resultState)){
					$state = $rowState['state'];
				}
				
				$orderDetailsSql = "select entity_id, store_id, total_qty_ordered, customer_firstname, customer_lastname, quote_id from ".SALES_FLAT_ORDER." where increment_id = '".$oID."' ";
				//echo "\n02".$orderDetailsSql;
				$resultorderDetails = mysql_query($orderDetailsSql);
				
				while($roworderDetails = mysql_fetch_array($resultorderDetails)){
					$entityid = $roworderDetails['entity_id'];
					$storeID = $roworderDetails['store_id'];
					$totalQtyOrdered = $roworderDetails['total_qty_ordered'];
					$customer_firstname = $roworderDetails['customer_firstname'];
					$customer_lastname = $roworderDetails['customer_lastname'];
					$quote_id = $roworderDetails['quote_id']; 
					
					$addressTypeSql = "SELECT address_id, address_type, date_format(created_at,'%m-%d-%Y %T') created_at FROM ".SALES_FLAT_QUOTE_ADDRESS." where quote_id = '".$quote_id."'";
					$resultAddressType = mysql_query($addressTypeSql);
					$shippingAddressID = "";
					$billingAddressID = "";
					$createdAt = "";
					while($rowAddressType = mysql_fetch_array($resultAddressType)){
						$createdAt = $rowAddressType['created_at'];
						$addressType = $rowAddressType['address_type'];
						if(strtolower($addressType) == "shipping"){
							$shippingAddressID = $rowAddressType['address_id'];
						}else if(strtolower($addressType) == "billing"){
							$billingAddressID = $rowAddressType['address_id'];
						}
					}

					$updateordersql = "update ".SALES_FLAT_ORDER." set `state` = '".$state."', `status` = '".$status."', customer_note_notify='0', `updated_at` = CURRENT_TIMESTAMP where `increment_id` = '".$oID."' ";
					//echo "\n1".$updateordersql;
					$resupdateorder = mysql_query($updateordersql);
					
					$updateordersql = "update ".SALES_FLAT_ORDER_GRID." set `status` = '".$status."', `updated_at` = CURRENT_TIMESTAMP where `increment_id` = '".$oID."' ";
					//echo "\n2".$updateordersql;
					$resupdateorder = mysql_query($updateordersql);
					
					$updateordersql = "update ".SALES_FLAT_ORDER_ITEM." set `qty_shipped` = '1', `updated_at` = CURRENT_TIMESTAMP where `order_id` = '".$entityid."' ";
					//echo "\n3".$updateordersql;
					$resupdateorder = mysql_query($updateordersql);
					
					//sales_flat_quote_item
					$updateordersql = "INSERT INTO ".SALES_FLAT_ORDER_STATUS_HISTORY." (`parent_id`, `is_customer_notified`, `is_visible_on_front`, `comment`, `status`, `created_at`, `entity_name`) VALUES
					(".$entityid.", 0, 0, NULL, '".$status."', CURRENT_TIMESTAMP, 'shipment') ";
					//echo "\n4".$updateordersql;
					$resupdateorder = mysql_query($updateordersql);
					
					$updateordersql = "INSERT INTO ".SALES_FLAT_SHIPMENT." ( `store_id`, `total_weight`, `total_qty`, `email_sent`, `order_id`, `customer_id`, `shipping_address_id`, `billing_address_id`, `shipment_status`, `increment_id`, `created_at`, `updated_at`, `packages`, `shipping_label`) VALUES 
					('".$storeID."', NULL, '".$totalQtyOrdered."', NULL, '".$entityid."', NULL, '".$shippingAddressID."', '".$billingAddressID."', NULL, '".$oID."', '".$createdAt."', CURRENT_TIMESTAMP, NULL, NULL)";
					//echo "\n5".$updateordersql;
					$resupdateorder = mysql_query($updateordersql);
					$shippingEntityID = mysql_insert_id();
					
					$updateordersql = "INSERT INTO ".SALES_FLAT_SHIPMENT_COMMENT." (`parent_id`, `is_customer_notified`, `is_visible_on_front`, `comment`, `created_at`) VALUES 
					('".$entityid."', 0, 0, 'Imported to InventorySource Channel Manager', CURRENT_TIMESTAMP)";
					//echo "\n6".$updateordersql;
					$resupdateorder = mysql_query($updateordersql);
					
					$updateordersql = "INSERT INTO ".SALES_FLAT_SHIPMENT_GRID." (`entity_id`, `store_id`, `total_qty`, `order_id`, `shipment_status`, `increment_id`, `order_increment_id`, `created_at`, `order_created_at`, `shipping_name`) VALUES 
					('".$shippingEntityID."', '".$storeID."', '".$totalQtyOrdered."', '".$entityid."', NULL, '".$oID."', '".$oID."', CURRENT_TIMESTAMP, '".$createdAt."', '".$customer_firstname." ".$customer_lastname."')";
					//echo "\n7".$updateordersql;
					$resupdateorder = mysql_query($updateordersql);
					
					/*$updateordersql = "INSERT INTO ".SALES_FLAT_SHIPMENT_ITEM." (`entity_id`, `parent_id`, `row_total`, `price`, `weight`, `qty`, `product_id`, `order_item_id`, `additional_data`, `description`, `name`, `sku`) VALUES 
					('".$shippingEntityID."', 1, NULL, 6.9500, 0.1100, 1.0000, 1, 1, NULL, NULL, 'Tenga Egg - Spider', 'ELTEST4')";
					echo "\n8".$updateordersql;
					$resupdateorder = mysql_query($updateordersql);
					*/
					$xml_str .="<UpdatedOrder>" . $oID . "</UpdatedOrder>";
					
				}
				$oID="";
				$status="";
			} else if($oID!="" && $status!="" && $status == "complete") {
				
				$stateSql = "SELECT state FROM ".SALES_ORDER_STATUS_STATE." where status = '".$status."'";
				//echo "\n01".$stateSql;
				$resultState = mysql_query($stateSql);
				while($rowState = mysql_fetch_array($resultState)){
					$state = $rowState['state'];
				}
				
				$orderDetailsSql = "select entity_id, store_id, total_qty_ordered, customer_firstname, customer_lastname, quote_id from ".SALES_FLAT_ORDER." where increment_id = '".$oID."' ";
				//echo "\n02".$orderDetailsSql;
				$resultorderDetails = mysql_query($orderDetailsSql);
				
				while($roworderDetails = mysql_fetch_array($resultorderDetails)){
					$entityid = $roworderDetails['entity_id'];
					$storeID = $roworderDetails['store_id'];
					$totalQtyOrdered = $roworderDetails['total_qty_ordered'];
					$customer_firstname = $roworderDetails['customer_firstname'];
					$customer_lastname = $roworderDetails['customer_lastname'];
					$quote_id = $roworderDetails['quote_id'];
					
					$addressTypeSql = "SELECT address_id, address_type, date_format(created_at,'%m-%d-%Y %T') created_at FROM ".SALES_FLAT_QUOTE_ADDRESS." where quote_id = '".$quote_id."'";
					$resultAddressType = mysql_query($addressTypeSql);
					$shippingAddressID = "";
					$billingAddressID = "";
					$createdAt = "";
					while($rowAddressType = mysql_fetch_array($resultAddressType)){
						$createdAt = $rowAddressType['created_at'];
						$addressType = $rowAddressType['address_type'];
						if(strtolower($addressType) == "shipping"){
							$shippingAddressID = $rowAddressType['address_id'];
						}else if(strtolower($addressType) == "billing"){
							$billingAddressID = $rowAddressType['address_id'];
						}
					}

					$updateordersql = "update ".SALES_FLAT_ORDER." set `state` = '".$state."', `status` = '".$status."', customer_note_notify='0', `updated_at` = CURRENT_TIMESTAMP where `increment_id` = '".$oID."' ";
					//echo "\n1".$updateordersql;
					//base_discount_invoiced='0.0000'
					//base_shipping_invoiced='5.0000'
					//base_subtotal_invoiced='6.9500'
					//base_tax_invoiced='0.0000'
					//base_total_invoiced='11.9500'
					//base_total_invoiced_cost='3.7500'
					//base_total_paid='11.9500'
					//discount_invoiced='0.0000'
					//shipping_invoiced='5.0000'
					//subtotal_invoiced='6.9500'
					//tax_invoiced='0.0000'
					//total_invoiced='11.9500'
					//total_paid='11.9500'
					//base_total_due='0.0000'
					//total_due='0.0000'
					//hidden_tax_invoiced='0.0000'
					//base_hidden_tax_invoiced='0.0000'
					$resupdateorder = mysql_query($updateordersql);
					
					$updateordersql = "update ".SALES_FLAT_ORDER_GRID." set `status` = '".$status."', `updated_at` = CURRENT_TIMESTAMP where `increment_id` = '".$oID."' ";
					//echo "\n2".$updateordersql;
					$resupdateorder = mysql_query($updateordersql);
					
					$updateordersql = "update ".SALES_FLAT_ORDER_ITEM." set `qty_shipped` = '1', `updated_at` = CURRENT_TIMESTAMP where `order_id` = '".$entityid."' ";
					//echo "\n3".$updateordersql;
					$resupdateorder = mysql_query($updateordersql);
					
					//sales_flat_quote_item
					$updateordersql = "INSERT INTO ".SALES_FLAT_ORDER_STATUS_HISTORY." (`parent_id`, `is_customer_notified`, `is_visible_on_front`, `comment`, `status`, `created_at`, `entity_name`) VALUES
					(".$entityid.", 0, 0, NULL, '".$status."', CURRENT_TIMESTAMP, 'shipment') ";
					//echo "\n4".$updateordersql;
					$resupdateorder = mysql_query($updateordersql);
					
					$updateordersql = "INSERT INTO ".SALES_FLAT_SHIPMENT." ( `store_id`, `total_weight`, `total_qty`, `email_sent`, `order_id`, `customer_id`, `shipping_address_id`, `billing_address_id`, `shipment_status`, `increment_id`, `created_at`, `updated_at`, `packages`, `shipping_label`) VALUES 
					('".$storeID."', NULL, '".$totalQtyOrdered."', NULL, '".$entityid."', NULL, '".$shippingAddressID."', '".$billingAddressID."', NULL, '".$oID."', '".$createdAt."', CURRENT_TIMESTAMP, NULL, NULL)";
					//echo "\n5".$updateordersql;
					$resupdateorder = mysql_query($updateordersql);
					$shippingEntityID = mysql_insert_id();
					
					$updateordersql = "INSERT INTO ".SALES_FLAT_SHIPMENT_COMMENT." (`parent_id`, `is_customer_notified`, `is_visible_on_front`, `comment`, `created_at`) VALUES 
					('".$entityid."', 0, 0, 'Imported to InventorySource Channel Manager', CURRENT_TIMESTAMP)";
					//echo "\n6".$updateordersql;
					$resupdateorder = mysql_query($updateordersql);
					
					$updateordersql = "INSERT INTO ".SALES_FLAT_SHIPMENT_GRID." (`entity_id`, `store_id`, `total_qty`, `order_id`, `shipment_status`, `increment_id`, `order_increment_id`, `created_at`, `order_created_at`, `shipping_name`) VALUES 
					('".$shippingEntityID."', '".$storeID."', '".$totalQtyOrdered."', '".$entityid."', NULL, '".$oID."', '".$oID."', CURRENT_TIMESTAMP, '".$createdAt."', '".$customer_firstname." ".$customer_lastname."')";
					//echo "\n7".$updateordersql;
					$resupdateorder = mysql_query($updateordersql);
					
					/*$updateordersql = "INSERT INTO ".SALES_FLAT_SHIPMENT_ITEM." (`entity_id`, `parent_id`, `row_total`, `price`, `weight`, `qty`, `product_id`, `order_item_id`, `additional_data`, `description`, `name`, `sku`) VALUES 
					('".$shippingEntityID."', 1, NULL, 6.9500, 0.1100, 1.0000, 1, 1, NULL, NULL, 'Tenga Egg - Spider', 'ELTEST4')";
					echo "\n8".$updateordersql;
					$resupdateorder = mysql_query($updateordersql);
					*/
					$xml_str .="<UpdatedOrder><![CDATA[" . $oID . "]]></UpdatedOrder>";
					
				}
				$oID="";
				$status="";
			}  
			
		}
	}  
	//base_total_due, total_due
	$xml_str .="</OrderUpdateXML>";
	//die("");
	if($selfupdate == 0){
		output_xml($xml_str);
	}
	
}

exit;
?>
