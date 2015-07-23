package salesmachine.oim.suppliers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimChannelSupplierMap;
import salesmachine.hibernatedb.OimFields;
import salesmachine.hibernatedb.OimFileFieldMap;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSupplierShippingMethod;
import salesmachine.hibernatedb.OimSuppliers;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.Vendors;
import salesmachine.util.StringHandle;

public class StandardFileSpecificsProvider implements IFileSpecificsProvider {
	private static final Logger log = LoggerFactory
			.getLogger(StandardFileSpecificsProvider.class);
	private final OimVendorSuppliers m_ovs;
	private final Map m_supplierMaps = new HashMap();
	private final List<OimSupplierShippingMethod> shipMap;

	public StandardFileSpecificsProvider(Session dbSession,
			OimVendorSuppliers ovs) {
		this(dbSession, ovs, ovs.getVendors());
	}

	public StandardFileSpecificsProvider(Session dbSession,
			OimVendorSuppliers ovs, Vendors v) {
		m_ovs = ovs;
		shipMap = Supplier.loadSupplierShippingMap(ovs.getOimSuppliers(), v);
	}

	@Override
	public String getFieldValueFromOrder(OimOrderDetails detail,
			OimFileFieldMap fieldMap) {
		OimFields field = fieldMap.getOimFields();
		OimOrders order = detail.getOimOrders();

		switch (field.getFieldId().intValue()) {
		case 10000:
			if (m_ovs != null)
				return m_ovs.getAccountNumber();
			break;
		case 0:// IGNORE
			break;
		case 1:// SKU
			String sku = detail.getSku();
			if (detail.getOimSuppliers() != null) {
				// Drop the prefix
				Integer channelId = order.getOimOrderBatches().getOimChannels()
						.getChannelId();
				Set supplierMap = (Set) m_supplierMaps.get(channelId);
				if (supplierMap == null) {
					supplierMap = order.getOimOrderBatches().getOimChannels()
							.getOimChannelSupplierMaps();
					m_supplierMaps.put(channelId, supplierMap);
				}

				Iterator itr = supplierMap.iterator();
				while (itr.hasNext()) {
					OimChannelSupplierMap map = (OimChannelSupplierMap) itr
							.next();
					if (map.getDeleteTm() != null)
						continue;

					String prefix = map.getSupplierPrefix();
					OimSuppliers supplier = map.getOimSuppliers();
					log.info("Prefix: {} - {}", prefix,
							supplier.getSupplierName());

					if (detail.getOimSuppliers().getSupplierId()
							.equals(supplier.getSupplierId())) {
						if (sku.toLowerCase().startsWith(prefix.toLowerCase())) {
							sku = sku.substring(prefix.length());
						}
						break;
					}
				}
			}
			return sku;
		case 2:// ProductOrderNumber
			return String.format("%s-%s", order.getStoreOrderId(), m_ovs
					.getVendors().getVendorId());
		case 3:// CustomerName
			return order.getDeliveryName();
		case 4:// StreetAddress
			return order.getDeliveryStreetAddress();
		case 5:// CustomerCity
			return order.getDeliveryCity();
		case 6:// CustomerState
			return order.getDeliveryState();
		case 7:// CustomerZip
			return order.getDeliveryZip();
		case 8:// CustomerCountry
			return order.getDeliveryCountry();
		case 9:// Quantity
			return detail.getQuantity().toString();
		case 10:// ShipMethod e.g. Ground, Next Day Air
			if (order.getOimShippingMethod() != null) {
				OimSupplierShippingMethod code = Supplier
						.findShippingCodeFromUserMapping(shipMap,
								order.getOimShippingMethod());
				return code.getOverride()==null? code.getName():code.getOverride().getShippingMethod();
			} else
				return order.getShippingDetails();
		case 11:// CustomerCompany
			return order.getDeliveryCompany();
		case 12:// CustomerSuburb
			return order.getDeliverySuburb();
		case 13:// SupplierSalesOrderNumber
		case 14:// SupplierOrderAcceptTime
		case 15:// SupplierPurchaseOrderNumber
		case 16:// SupplierOrderQuantity
		case 17:// SupplierShippedQuantity
		case 18:// SupplierShipDate
		case 19:// SupplierExpectedReceiptDate
		case 20:// SupplierTrackingNumber
		case 21:// SupplierHeaderType
		case 22:// SupplierHeaderStatus
		case 23:// SupplierDetailStatus
			break;
		case 24:// Product Name
			return StringHandle.removeNull(detail.getProductName());
		case 25:// Product Description
			return StringHandle.removeNull(detail.getProductDesc());
		case 30:
			return order.getDeliveryPhone();
		case 31:
			return order.getDeliveryEmail();
		case 34:
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
			return sdf.format(new Date());
		case 35:// SHIPPING CARRIER
			if (order.getOimShippingMethod() != null) {
				OimSupplierShippingMethod code = Supplier
						.findShippingCodeFromUserMapping(shipMap,
								order.getOimShippingMethod());
				return code.getCarrierName();
			} else
				return "";
		case 36 :
			return order.getDeliveryStateCode()!=null?order.getDeliveryStateCode():order.getDeliveryState();
		}
		return null;
	}

	@Override
	public String getLastFileLine() {
		return null;
	}

}
