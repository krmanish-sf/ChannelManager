<?php
error_reporting(E_ERROR);
ini_set('display_errors','On');
//require_once('nusoap.php');

/*$apiuser = 'clientTestUser';
$apikey = 'SpuNa5a5';
$customer_number = 'W7344';
$method = 'cconfile';
$po_number = 'APIPO123';
$customer_note = 'API 1ORDER TESTING';
$shipping_method = 'usps_Priority Mail';
$firstname = 'Peter';
$lastname = 'Parker';
$company = 'Marvel.Inc';
$street = '111 Long';
$city = 'Irvine';
$country_id = 'US';
$region = 'CA';
$postcode = '92602';
$telephone = '9491231234';
$fax = '2223334444';
$email = '123@abc.com';
$products= "LGP-VX10KS|1,MA-89170|2,FULFILLMENT FEE|1";
*/

$orderData=array();

$apiuser = $_POST['apiuser'];
$apikey = $_POST['apikey'];
$customer_number = $_POST['customer_number'];
$method = $_POST['method'];
$po_number = $_POST['po_number'];
$customer_note = $_POST['customer_note'];
$shipping_method = $_POST['shipping_method'];
$firstname = $_POST['firstname'];
$lastname = $_POST['lastname'];
$company = $_POST['company'];
$street = $_POST['street'];
$city = $_POST['city'];
$country_id = $_POST['country_id'];
$region = $_POST['region'];
$postcode = $_POST['postcode'];
$telephone = $_POST['telephone'];
$fax = $_POST['fax'];
$email = $_POST['email'];
$products = $_POST['products'];

$productsArr = explode(",",$products);
$productsArrSplit = array();
for($i=0;$i<sizeof($productsArr); $i++)
{
	$qty = explode("|",$productsArr[$i]);
	if(sizeof($qty) == 2 )
		$productsArrSplit[$qty[0]] = array('qty' => $qty[1]);
}

$proxy = new soapclient('http://sna-srv-tst-04.mobileline.com/store/api/soap/?wsdl', array('trace' => 1));

$sessionId = $proxy->login($apiuser, $apikey);
				$orderData = array(
					'api' => array('api_user' => $apiuser,'api_key' => $apikey),
					'session' => array('customer_number'   => $customer_number),
					'payment' => array('method' => $method, 'po_number' => $po_number),
					'add_products' => $productsArrSplit,
				    'order' => array(
									'comment' => array('customer_note' => $customer_note),
									'shipping_method' => $shipping_method, 
									'shipping_address'  => array(
										'firstname' => $firstname,
										'lastname' => $lastname,
										'company' => $company,
										'street' => $street,
										'city' => $city,
										'country_id' => $country_id,
										'region' => $region,
										'postcode' => $postcode,
										'telephone' => $telephone,
										'fax' => $fax,
										'email' => $email),
									),
				);
try{			 
	$result = $proxy->call($sessionId, 'sales_order.create', array($orderData));
	} catch (Exception $e)
	{
		$result = "Exception =>" .$e->getMessage();	
	}

$request = $proxy->__getLastRequest();

$response = $proxy->__getLastResponse();

$xml ='<root>';
	$xml.='<request>';
		$xml.= $request;
	$xml.='</request>';
	
	$xml.='<response>';
		$xml.= $response;
	$xml.='</response>';

	$xml.='<result>';
		$xml.= $result;
	$xml.='</result>';

$xml.='</root>';
$xml = '<?xml version="1.0" encoding="utf-8"?>'.str_replace('<?xml version="1.0" encoding="UTF-8"?>','',$xml);
echo $xml;
?>
