package salesmachine.oim.suppliers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.hibernate.Session;

import salesmachine.hibernatedb.OimChannelSupplierMap;
import salesmachine.hibernatedb.OimFields;
import salesmachine.hibernatedb.OimFileFieldMap;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSuppliers;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.Vendors;
import salesmachine.util.StringHandle;
/*
 * Host: ftp.petra.com
 * Username: ecommtest
 * Password: 04wHYv0R
 */
public class PetraFileSpecificsProvider implements IFileSpecificsProvider {
	OimVendorSuppliers m_ovs;
	HashMap m_supplierMaps = new HashMap();
	HashMap shipMap = new HashMap();
	
	public PetraFileSpecificsProvider(Session dbSession, OimVendorSuppliers ovs, Vendors v) {
		m_ovs = ovs;
		shipMap = Supplier.loadSupplierShippingMap(dbSession, ovs.getOimSuppliers(), v);
	}
	
	@Override
	public String getFieldValueFromOrder(OimOrderDetails detail,
			OimFileFieldMap fieldMap) {
		OimFields field = fieldMap.getOimFields();
		String mappedField = fieldMap.getMappedFieldName();
		
		OimOrders order = detail.getOimOrders();
		switch(field.getFieldId().intValue()) {
		case 0://IGNORE
			if ("Petra Customer Number".equals(mappedField)) {
				return m_ovs.getAccountNumber();
			} 
			break;
		case 1://SKU
			String sku = detail.getSku();
			if (detail.getOimSuppliers() != null) {
				// Drop the prefix
				Integer channelId = order.getOimOrderBatches().getOimChannels().getChannelId();
				Set supplierMap = (Set)m_supplierMaps.get(channelId);
				if (supplierMap == null) {			
					supplierMap = order.getOimOrderBatches().getOimChannels().getOimChannelSupplierMaps();
					m_supplierMaps.put(channelId, supplierMap);
				}
	
				if (supplierMap != null) {
					Iterator itr = supplierMap.iterator();
					while (itr.hasNext()) {
						OimChannelSupplierMap map = (OimChannelSupplierMap)itr.next();
						if (map.getDeleteTm() != null)
							continue;
						
						String prefix = map.getSupplierPrefix();
						OimSuppliers supplier = map.getOimSuppliers();
						System.out.println("Prefix: "+prefix+" -- "+supplier.getSupplierName());

						if (detail.getOimSuppliers().getSupplierId().equals(supplier.getSupplierId())) {
							if (sku.toLowerCase().startsWith(prefix.toLowerCase())) {
								sku = sku.substring(prefix.length());
							}
							break;
						}
					}				
				}
				
			}
			return sku;
		case 2://ProductOrderNumber
			String storeOrderId = StringHandle.removeNull(order.getStoreOrderId());
			if (storeOrderId.startsWith("cellularaccessorycom-")) {
				storeOrderId = storeOrderId.substring("cellularaccessorycom-".length());
			}
			return storeOrderId;
		case 3://CustomerName
			return order.getDeliveryName();
		case 4://StreetAddress
			return order.getDeliveryStreetAddress();
		case 5://CustomerCity
			return order.getDeliveryCity();
		case 6://CustomerState
			return order.getDeliveryState();
		case 7://CustomerZip
			return order.getDeliveryZip();
		case 8://CustomerCountry
			return order.getDeliveryCountry();
		case 9://Quantity
			return detail.getQuantity().toString();
		case 10://ShipMethod
			String code = Supplier.findShippingCodeFromUserMapping(shipMap, order.getShippingDetails());
			if (code != null && code.length() > 0)
				return code;
			return m_ovs.getDefShippingMethodCode();
		case 11://CustomerCompany
			return order.getDeliveryCompany();
		case 12://CustomerSuburb
			return order.getDeliverySuburb();
		case Petra.SHIP_COMPLETE:
			return "SHIP COMPLETE";
		case 32:
			return StringHandle.removeNull(order.getOrderComment());
		case 13://SupplierSalesOrderNumber
		case 14://SupplierOrderAcceptTime
		case 15://SupplierPurchaseOrderNumber
		case 16://SupplierOrderQuantity
		case 17://SupplierShippedQuantity
		case 18://SupplierShipDate
		case 19://SupplierExpectedReceiptDate
		case 20://SupplierTrackingNumber
		case 21://SupplierHeaderType
		case 22://SupplierHeaderStatus
		case 23://SupplierDetailStatus
		}
		return "";
	}


	@Override
	public String getLastFileLine() {
		// TODO Auto-generated method stub
		return "DONE\n";
	}	
	
	/***
	 * 
	 * @return Hashmap containing the shipping info
	 */
	private HashMap createShippingMethodsMap() {
		HashMap shippingMethods = new HashMap();

		HashMap fedex = new HashMap();
		fedex.put("FDXG", "Ground");
		fedex.put("FDXH", "Home");
		fedex.put("FDXN", "Next Day");
		fedex.put("FDX2", "2 Day");
		fedex.put("FDX3", "3 Day");

		HashMap ups = new HashMap();		
		ups.put("UPSG", "Ground");
		ups.put("UPSN", "Next Day");
		ups.put("UPS2", "2 Day");
		ups.put("UPS3", "3 Day");
		ups.put("UPS MI", "Mail Innovations");

		HashMap usps = new HashMap();
		usps.put("APO/FPO (USPS)", "APO");
		usps.put("APO/FPO (USPS)", "FPO");
		usps.put("APO/FPO (USPS)", "APO/FPO");
		usps.put("APO/FPO (USPS)", "APO & FPO");
		usps.put("USPS SHIP CON", "Priority Mail");

		shippingMethods.put("FEDEX", fedex);
		shippingMethods.put("Federal Express", fedex);
		
		shippingMethods.put("United Parcel Service", ups);
		shippingMethods.put("UPS", ups);
		
		shippingMethods.put("United States Postal Service", usps);
		shippingMethods.put("USPS", usps);
		shippingMethods.put("US Mail", usps);
		
		return shippingMethods;
	}	
}
