<?php

include('includes/configure.php');
define('DB_SERVER', DB_SERVER);
define('DB_USER', DB_SERVER_USERNAME);
define('DB_PASS', DB_SERVER_PASSWORD);
define('DB_NAME', DB_DATABASE);

$table_prefix=DB_PREFIX;
define('TABLE_ORDERS',  $table_prefix . "orders");
define('TABLE_COUNTRIES',  $table_prefix . "countries");
define('TABLE_ZONES',  $table_prefix . "zones");
define('TABLE_ORDERS_STATUS_HISTORY',  $table_prefix . "orders_status_history");


$table = 'orders';
$file = 'export';
$count = 0;
$PassKey="02446";
global $order_arr,$title, $desc, $test, $code, $pass, $def_ship;
$order_arr = array();


if(isset($_POST['XML_INPUT_VALUE']))
{
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
		$xmlstr="<xmlPopulate>\n<xmlProductsImportResponse>";
		$xmlstr.="Error : ".xml_error_string(xml_get_error_code($xml_parser))." At Line No :  ".xml_get_current_line_number($xml_parser);
		$xmlstr.="</xmlProductsImportResponse>\n</xmlPopulate>";
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
				updateorders($array_haystack,$order_status_name);
				break;
			}
	}

}

function getOrders($order_status_name){
	
	$xml_str = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n";
	$xml_str .="<OrderXML>";

	connect_db();
	//$sql="select o.orders_id, o.payment_method,o.delivery_name, o.customers_name, o.billing_name, o.delivery_company, o.customers_company, o.billing_company, o.delivery_city, o.customers_city, o.billing_city, o.delivery_street_address, o.customers_street_address, o.billing_street_address, o.delivery_suburb, o.customers_suburb, o.billing_suburb, o.delivery_postcode, o.customers_postcode, o.billing_postcode, o.delivery_state, o.customers_state, o.billing_state, o.delivery_country, o.customers_country, o.billing_country, o.customers_telephone, o.customers_email_address, date_format(o.date_purchased,'%m-%d-%Y') date_purchased, o.customers_id,op.products_model,op.final_price,op.products_price,op.products_quantity  from orders o ,orders_products op where o.orders_id=op.orders_id and o.orders_status = '1' order by o.date_purchased DESC";
	$sql="select o.orders_id, o.payment_method,o.delivery_name, o.customers_name, o.billing_name, o.delivery_company, o.customers_company, o.billing_company, o.delivery_city, o.customers_city, o.billing_city, o.delivery_street_address, o.customers_street_address, o.billing_street_address, o.delivery_suburb, o.customers_suburb, o.billing_suburb, o.delivery_postcode, o.customers_postcode, o.billing_postcode, o.delivery_state, o.customers_state, o.billing_state, o.delivery_country, o.customers_country, o.billing_country, o.customers_telephone, o.customers_email_address, date_format(o.date_purchased,'%m-%d-%Y') date_purchased, o.customers_id from orders o INNER JOIN orders_status os on o.orders_status=os.orders_status_id where os.orders_status_name = '".$order_status_name."' order by o.date_purchased DESC";
	$ordPendingQuery = mysql_query($sql);

	// Checking for pending orders
	if (mysql_num_rows($ordPendingQuery) > 0)
	{
		$dr_orders_id_str = "";
		while($res = mysql_fetch_array($ordPendingQuery))
		{
			$xml_str .="<Order>\n";
			$oID = $res['orders_id'];
			$xml_str .="<o_id>".$oID ."</o_id>\n";
			//		$orders_query = mysql_query("select distinct orders_id, payment_method,delivery_name, customers_name, billing_name, delivery_company, customers_company, billing_company, delivery_city, customers_city, billing_city, delivery_street_address, customers_street_address, billing_street_address, delivery_suburb, customers_suburb, billing_suburb, delivery_postcode, customers_postcode, billing_postcode, delivery_state, customers_state, billing_state, delivery_country, customers_country, billing_country, customers_telephone, customers_email_address, date_purchased, customers_id from " . TABLE_ORDERS . " where orders_id = '" . $oID . "' order by date_purchased DESC");
			$shipping_address=array();
			//		$res = mysql_fetch_array($orders_query);
			$xml_str .="<o_pay_method>".$res['payment_method'] ."</o_pay_method>\n";
			if($res['delivery_name']!=""){
				//If a delivery name was specified, use delivery address
				$ssql=mysql_query("select countries_id from " . TABLE_COUNTRIES . " where countries_name='" . $res['delivery_country'] . "'");
				$ssql=mysql_fetch_array($ssql);
				$idc=$ssql['countries_id'];
				
				$ssql=mysql_query("select zone_code from " . TABLE_ZONES . " where zone_country_id='" . $idc . "' and zone_name='" . $res['delivery_state'] . "'");
				$ssql=mysql_fetch_array($ssql);
				$sc=$ssql['zone_code'];
				
				if(!$sc)
					$sc=$res['delivery_state'];
					
				$delivery_address['City']=$res['delivery_city'];
				$delivery_address['StateCode']=$sc;
				$delivery_address['Country']=$res['delivery_country'];
				$delivery_address['Zip']=$res['delivery_postcode'];
				$delivery_address['Company']=$res['delivery_company'];
				
				$xml_str .="<deliverydetails>";
				$xml_str .="<name>".$res['delivery_name']."</name>";
				$xml_str .="<streetaddress>".$res['delivery_street_address']."</streetaddress>";
				$xml_str .="<suburb>".$res['delivery_suburb']."</suburb>";
				$xml_str .="<city>".$delivery_address['City']."</city>";
				$xml_str .="<state>".$delivery_address['StateCode']."</state>";
				$xml_str .="<country>".$delivery_address['Country']."</country>";
				$xml_str .="<zip>".$delivery_address['Zip']."</zip>";
				$xml_str .="<company>".$delivery_address['Company']."</company>";
				$xml_str .="<phone>". $res['customers_telephone'] ."</phone>\n";
				$xml_str .="<email>". $res['customers_email_address'] ."</email>\n";								
				$xml_str .="</deliverydetails>";
			} 
			
			if($res['billing_name']!=""){
				$ssql=mysql_query("select countries_id from " . TABLE_COUNTRIES . " where countries_name='" . $res['billing_country'] . "'");
				$ssql=mysql_fetch_array($ssql);
				$idc=$ssql['countries_id'];
				
				$ssql=mysql_query("select zone_code from " . TABLE_ZONES . " where zone_country_id='" . $idc . "' and zone_name='" . $res['billing_state'] . "'");
				$ssql=mysql_fetch_array($ssql);
				$sc=$ssql['zone_code'];
				if(!$sc)
					$sc=$res['billing_state'];
					
				$billing_address['City']=$res['billing_city'];
				$billing_address['StateCode']=$sc;
				$billing_address['Country']=$res['billing_country'];
				$billing_address['Zip']=$res['billing_postcode'];
				$billing_address['Company']=$res['billing_company'];
				
				$xml_str .="<billingdetails>";
				$xml_str .="<name>".$res['billing_name']."</name>";
				$xml_str .="<streetaddress>".$res['billing_street_address']."</streetaddress>";
				$xml_str .="<suburb>".$res['billing_suburb']."</suburb>";
				$xml_str .="<city>".$billing_address['City']."</city>";
				$xml_str .="<state>".$billing_address['StateCode']."</state>";
				$xml_str .="<country>".$billing_address['Country']."</country>";
				$xml_str .="<zip>".$billing_address['Zip']."</zip>";
				$xml_str .="<company>".$billing_address['Company']."</company>";
				$xml_str .="<phone>". $res['customers_telephone'] ."</phone>\n";
				$xml_str .="<email>". $res['customers_email_address'] ."</email>\n";								
				$xml_str .="</billingdetails>";				
			}

			if($res['customers_name']!=""){
				$ssql=mysql_query("select countries_id from " . TABLE_COUNTRIES . " where countries_name='" . $res['customers_country'] . "'");
				$ssql=mysql_fetch_array($ssql);
				$idc=$ssql['countries_id'];
				
				$ssql=mysql_query("select zone_code from " . TABLE_ZONES . " where zone_country_id='" . $idc . "' and zone_name='" . $res['customers_state'] . "'");
				$ssql=mysql_fetch_array($ssql);
				$sc=$ssql['zone_code'];
				if(!$sc)
					$sc=$res['customers_state'];
					
				$customers_address['City']=$res['customers_city'];
				$customers_address['StateCode']=$sc;
				$customers_address['Country']=$res['customers_country'];
				$customers_address['Zip']=$res['customers_postcode'];
				$customers_address['Company']=$res['customers_company'];
								
				$xml_str .="<customerdetails>";
				$xml_str .="<name>".$res['customers_name']."</name>";
				$xml_str .="<streetaddress>".$res['customers_street_address']."</streetaddress>";
				$xml_str .="<suburb>".$res['customers_suburb']."</suburb>";
				$xml_str .="<city>".$customers_address['City']."</city>";
				$xml_str .="<state>".$customers_address['StateCode']."</state>";
				$xml_str .="<country>".$customers_address['Country']."</country>";
				$xml_str .="<zip>".$customers_address['Zip']."</zip>";
				$xml_str .="<company>".$customers_address['Company']."</company>";
				$xml_str .="<phone>". $res['customers_telephone'] ."</phone>\n";
				$xml_str .="<email>". $res['customers_email_address'] ."</email>\n";				
				$xml_str .="</customerdetails>";						
			}
				
			$xml_str .= "<products>";
			$prodsQuery = mysql_query("select pd.products_name,op.products_model,op.final_price,op.products_price,op.products_quantity from orders_products op left join products_description pd on pd.products_id=op.products_id where op.orders_id=".$oID);
			while($prodsQueryRes = mysql_fetch_array($prodsQuery)) {
				$xml_str .= "<product>";
				$xml_str .="<p_model>". $prodsQueryRes['products_model'] ."</p_model>\n";
				$xml_str .="<p_quantity>". $prodsQueryRes['products_quantity'] ."</p_quantity>\n";
				$xml_str .="<p_price_each>". $prodsQueryRes['products_price'] ."</p_price_each>\n";
				$xml_str .="<p_name>". $prodsQueryRes['products_name'] ."</p_name>\n";
				$xml_str .= "</product>";				
			}
			$xml_str .= "</products>";			

			$totalAmtQuery = mysql_query("select value from orders_total where orders_id = '".$oID."' and class = 'ot_total'");
			$totalAmt = mysql_fetch_array($totalAmtQuery);
			$xml_str .="<p_bill_amount>". $totalAmt['value'] ."</p_bill_amount>\n";

			$shipDetailQuery = mysql_query("select title from orders_total where orders_id = '".$oID."' and class = 'ot_shipping'");
			$shipDetail = mysql_fetch_array($shipDetailQuery);

			$order['ShippingMethodCode'] = $shipDetail['title'];
			$orders_com_query = mysql_query("select comments from " . TABLE_ORDERS_STATUS_HISTORY . " where orders_id = '" . $oID . "'");
			$orders_com = mysql_fetch_array($orders_com_query);
			$order['OrderNote']=str_replace("<br>","",$orders_com['comments']);
			$order['OrderDateAndTime'] = str_replace(' ', 'T', $res['date_purchased']);

			$xml_str .="<o_shipping><![CDATA[". $order['ShippingMethodCode'] ."]]></o_shipping>\n";
			$xml_str .="<o_note>". $order['OrderNote'] ."</o_note>\n";
			$xml_str .="<o_time><![CDATA[". $order['OrderDateAndTime'] ."]]></o_time>\n";

			$xml_str .="</Order>\n";
		}
	}
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

function updateorders($array_haystack,$order_status_name){
	connect_db();
	$oID="";
	$status="";
	$order_tracking=null;
	$status_id_query = "select orders_status_id from orders_status where orders_status_name='".$order_status_name."'";
	$status_result = mysql_query($status_id_query);
	if(mysql_num_rows($status_result) > 0){
		$status = mysql_fetch_array($status_result);
	}
	$xml_str = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n";
	$xml_str .="<OrderUpdateXML>";
	//echo "$order_tracking:".$order_tracking."A";
	if ((!empty($array_haystack)) AND (is_array($array_haystack)))
	{
		foreach ($array_haystack as $xml_key => $xml_value)
		{
			if(strtolower($xml_value["tag"])=="order_id")
			{
				//$type="Checking for test database connection";
				$oID=strtolower($xml_value["value"]);
			}
			if(strtolower($xml_value["tag"])=="order_status")
			{
				//$type="Checking for test database connection";
				//$status=strtolower($xml_value["value"]);
			}
			if(strtolower($xml_value["tag"])=="order_tracking")
			{
				//$type="Checking for test database connection";
				$order_tracking=strtolower($xml_value["value"]);
				//echo "$order_tracking:".$order_tracking."A";
			}
			
			if($oID!="" && $status!="" && $order_tracking!="") {
				mysql_query("update orders set orders_status = '" . $status["orders_status_id"] . "' where orders_id = '".$oID."'");
				//echo  "amit".$status["orders_status_id"];
				if($order_tracking!=""){
					$insert_status_history = "INSERT INTO orders_status_history (`orders_status_history_id`, `orders_id`, `orders_status_id`, `date_added`, `customer_notified`, `comments`) VALUES (NULL, '".$oID."', '". $status["orders_status_id"] ."', now(), '0', '".$order_tracking."');";
					mysql_query($insert_status_history);
				}				
				$xml_str .="<UpdatedOrder>" . $oID . "</UpdatedOrder>";
				$oID="";
				//$status="";
			} else {
					
			}
		}
	}
	$xml_str .="</OrderUpdateXML>";
	output_xml($xml_str) ;
}

exit;
?>
