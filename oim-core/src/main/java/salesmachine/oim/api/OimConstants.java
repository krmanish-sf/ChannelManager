package salesmachine.oim.api;

public class OimConstants {
	public static final Integer CHANNEL_ACCESSDETAIL_CHANNEL_URL = new Integer(1);
	public static final Integer CHANNEL_ACCESSDETAIL_FTP_URL = new Integer(2);
	public static final Integer CHANNEL_ACCESSDETAIL_FTP_LOGIN = new Integer(3);
	public static final Integer CHANNEL_ACCESSDETAIL_FTP_PWD = new Integer(4);
	public static final Integer CHANNEL_ACCESSDETAIL_ADMIN_LOGIN = new Integer(5);
	public static final Integer CHANNEL_ACCESSDETAIL_ADMIN_PWD = new Integer(6);
	public static final Integer CHANNEL_ACCESSDETAIL_SCRIPT_PATH = new Integer(7);
	public static final Integer CHANNEL_ACCESSDETAIL_AUTH_KEY = new Integer(8);
	@Deprecated
	public static final Integer CHANNEL_ACCESSDETAIL_MERCHANT_TOKEN = new Integer(9);
	@Deprecated 
	public static final Integer CHANNEL_ACCESSDETAIL_AMAZON_USER = new Integer(10);
	@Deprecated 
	public static final Integer CHANNEL_ACCESSDETAIL_AMAZON_PASS = new Integer(11);
	public static final Integer CHANNEL_ACCESSDETAIL_YAHOO_STOREID = new Integer(12);
	public static final Integer CHANNEL_ACCESSDETAIL_SHOP_CATALOGID = new Integer(13);
	public static final Integer CHANNEL_ACCESSDETAIL_AMAZON_SELLERID = new Integer(14);
	public static final Integer CHANNEL_ACCESSDETAIL_AMAZON_MWS_AUTH_TOKEN = new Integer(15);
	public static final Integer CHANNEL_ACCESSDETAIL_AMAZON_MWS_MARKETPLACE_ID= new Integer(16);
	public static final Integer CHANNEL_ACCESSDETAIL_SHOPIFY_ACCESS_CODE = new Integer(17);
	public static final Integer CHANNEL_ACCESSDETAIL_BIGCOMMERCE_STORE_ID = new Integer(18);
	public static final Integer CHANNEL_ACCESSDETAIL_DEVHUB_SITE_ID = new Integer(19);	
	public static final Integer CHANNEL_ACCESSDETAIL_ORDORO_CART_ID = new Integer(21);
	
	public static final Integer ORDERBATCH_TYPE_ID_AUTOMATED = new Integer(1);
	public static final Integer ORDERBATCH_TYPE_ID_MANUAL = new Integer(2);
	public static final Integer ORDER_STATUS_SHIPPED = new Integer(7);
	public static final Integer ORDER_STATUS_UNPROCESSED = new Integer(0);
	public static final Integer ORDER_STATUS_PLACED = new Integer(1);
	public static final Integer ORDER_STATUS_PROCESSED_SUCCESS = new Integer(2);
	public static final Integer ORDER_STATUS_PROCESSED_FAILED = new Integer(3);
	public static final Integer ORDER_STATUS_PROCESSED_PENDING = new Integer(4);
	public static final Integer ORDER_STATUS_MANUALLY_PROCESSED = new Integer(5);
	public static final Integer ORDER_STATUS_CANCELED = new Integer(6);
	public static final Integer ORDER_STATUS_COMPLETE = new Integer(8);
	//public static final Integer ORDER_STATUS_UNCONFIRMED = new Integer(100);
	
	public static final Integer SUPPLIER_METHOD_TYPE_ORDERPUSH = new Integer(1);
	public static final Integer SUPPLIER_METHOD_TYPE_STATUSPULL = new Integer(2);	
	public static final Integer SUPPLIER_METHOD_TYPE_HG_PHI = new Integer(3);
	public static final Integer SUPPLIER_METHOD_TYPE_HG_HVA = new Integer(4);
	
	public static final Integer SUPPLIER_METHOD_NAME_EMAIL = new Integer(1);
	public static final Integer SUPPLIER_METHOD_NAME_FTP = new Integer(2);
	public static final Integer SUPPLIER_METHOD_NAME_CUSTOM = new Integer(3);
	
	public static final Integer SUPPLIER_METHOD_ATTRIBUTES_EMAILADDRESS = new Integer(1);
	public static final Integer SUPPLIER_METHOD_ATTRIBUTES_FTPSERVER = new Integer(2);
	public static final Integer SUPPLIER_METHOD_ATTRIBUTES_FTPLOGIN = new Integer(3);
	public static final Integer SUPPLIER_METHOD_ATTRIBUTES_FTPPASSWORD = new Integer(4);
	public static final Integer SUPPLIER_METHOD_ATTRIBUTES_FTPFOLDER = new Integer(5);
	public static final Integer SUPPLIER_METHOD_ATTRIBUTES_FILETYPEID = new Integer(6);
	public static final Integer SUPPLIER_METHOD_ATTRIBUTES_FILEFORMAT = new Integer(8);
	public static final Integer SUPPLIER_METHOD_ATTRIBUTES_FTPACCOUNT = new Integer(9);
	public static final Integer SUPPLIER_METHOD_ATTRIBUTES_FTPTYPE = new Integer(10);
	public static final Integer SUPPLIER_METHOD_ATTRIBUTES_ACCOUNTNAME = new Integer(11);
	
	public static final String FILE_FORMAT_PARAMS_USEHEADER = "USE_HEADER";
	public static final String FILE_FORMAT_PARAMS_FIELD_DELIMITER = "FIELD_DELIMITER";
	public static final String FILE_FORMAT_PARAMS_TEXT_DELIMITER = "TEXT_DELIMITER";
	public static final String FILE_FORMAT_PARAMS_NAME = "NAME";	
	
	public static final int FILE_FORMAT_CSV = 1;
	public static final int FILE_FORMAT_XLS = 2;
	public static final int FILE_FORMAT_SEND_PLAIN_TEXT_IN_EMAIL = 3;
	
	public static final Integer OIM_FIELD_IGNORE = new Integer(0);
	public static final Integer OIM_FIELD_SKU = new Integer(1);
	public static final Integer OIM_FIELD_PRODUCT_ORDER_NUMBER = new Integer(2);
	public static final Integer OIM_FIELD_DELIVERYNAME = new Integer(3);
	public static final Integer OIM_FIELD_DELIVERY_ADDRESS = new Integer(4);
	public static final Integer OIM_FIELD_DELIVERY_CITY = new Integer(5);
	public static final Integer OIM_FIELD_DELIVERY_STATE = new Integer(6);
	public static final Integer OIM_FIELD_DELIVERY_ZIP = new Integer(7);
	public static final Integer OIM_FIELD_DELIVERY_COUNTY = new Integer(8);
	public static final Integer OIM_FIELD_QTY = new Integer(9);
	public static final Integer OIM_FIELD_SHIPMETHOD = new Integer(10);
	public static final Integer OIM_FIELD_DELIVERY_COMPANY = new Integer(11);
	public static final Integer OIM_FIELD_DELIVERY_SUBURB = new Integer(12);
	public static final Integer OIM_FIELD_SUPPLIER_SALESORDER_NUMBER = new Integer(13);
	public static final Integer OIM_FIELD_SUPPLIER_ORDER_ACCEPTTIME = new Integer(14);
	public static final Integer OIM_FIELD_SUPPLIER_PONUMBER = new Integer(15);
	public static final Integer OIM_FIELD_SUPPLIER_ORDER_QTY = new Integer(16);
	public static final Integer OIM_FIELD_SUPPLIER_SHIP_QTY = new Integer(17);
	public static final Integer OIM_FIELD_SUPPLIER_SHIPDATE = new Integer(18);
	public static final Integer OIM_FIELD_SUPPLIER_EXP_RECT_DATE = new Integer(19);
	public static final Integer OIM_FIELD_SUPPLIER_TRACKING_NUMBER = new Integer(20);
	public static final Integer OIM_FIELD_SUPPLIER_HDR_TYPE = new Integer(21);
	public static final Integer OIM_FIELD_SUPPLIER_HDR_STATUS = new Integer(22);
	public static final Integer OIM_FIELD_SUPPLIER_DETAIL_STATUS = new Integer(23);
	public static final Integer OIM_FIELD_PRODUCT_NAME = new Integer(24);
	public static final Integer OIM_FIELD_PRODUCT_DESC = new Integer(25);
	public static final Integer OIM_FIELD_PRODUCT_COST = new Integer(26);
	public static final Integer OIM_FIELD_PRODUCT_SALEPRICE = new Integer(27);
	public static final Integer OIM_FIELD_ORDER_TOTAL_AMOUNT = new Integer(28);
	public static final Integer OIM_FIELD_PRODUCT_STORE_STATUS = new Integer(29);
	public static final Integer OIM_FIELD_CUSTOMER_PHONE = new Integer(30);
	public static final Integer OIM_FIELD_CUSTOMER_EMAIL = new Integer(31);
	public static final Integer OIM_FIELD_ORDER_COMMENTS = new Integer(32);
	public static final Integer OIM_FIELD_UPC = new Integer(33);
	public static final Integer OIM_FIELD_ORDER_PROCESS_DATE = new Integer(34);
	
	public static final String OIM_SUPPLER_ORDER_STATUS_IN_PROCESS = "In-Process";
	public static final String OIM_SUPPLER_ORDER_STATUS_SENT_TO_SUPPLIER = "Sent to supplier.";
	public static final String OIM_SUPPLER_ORDER_STATUS_SHIPPED = "Shipped";
	public static final String OIM_SUPPLER_ORDER_STATUS_FAILED = "Failed";
	public static final String OIM_SUPPLER_ORDER_STATUS_COMPLETED = "Completed";
	public static final Integer OIM_SUPLLIER_HONEST_GREEN_ID = 1822;
	
}
