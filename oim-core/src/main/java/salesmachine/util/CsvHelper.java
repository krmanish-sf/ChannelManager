package salesmachine.util;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimFileFieldMap;
import salesmachine.hibernatedb.OimFiletypes;
import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderProcessingRule;
import salesmachine.hibernatedb.OimOrderStatuses;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatehelper.PojoHelper;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;

public class CsvHelper {
	private static final Logger LOG = LoggerFactory.getLogger(CsvHelper.class);

	public static List getColumnHeaders(File file, String fieldDelimiter,
			String textDelimiter, boolean useHeader) {
		LOG.info("Text Delimiter: {} Field Delimiter: {} Use Header: {}",
				textDelimiter, fieldDelimiter, useHeader);
		List header = null;
		if (file.exists()) {
			try {
				CSVReader reader = new CSVReader(
						new FileReader(file),
						fieldDelimiter != null && fieldDelimiter.length() > 0 ? fieldDelimiter
								.charAt(0) : CSVParser.DEFAULT_SEPARATOR,
						textDelimiter != null && textDelimiter.length() > 0 ? textDelimiter
								.charAt(0) : CSVParser.DEFAULT_QUOTE_CHARACTER);
				String[] nextLine = reader.readNext();
				if (nextLine != null) {
					header = new ArrayList();
					for (int i = 0; i < nextLine.length; i++) {
						if (useHeader)
							header.add(nextLine[i]);
						else
							header.add("Column#" + i);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} // if (statusFile.exists()) {
		return header;
	}

	private static Double stringToDouble(String val) {
		try {
			Double dVal = Double.parseDouble(val);
			return dVal;
		} catch (Exception e) {
			return null;
		}
	}

	public static OimOrderBatches processOrdersFromFile(Session dbSession,
			File file, Integer fileTypeId) {
		Query query = dbSession
				.createQuery("from salesmachine.hibernatedb.OimFiletypes where fileTypeId=:fileid");
		OimFiletypes oft = (OimFiletypes) query
				.setInteger("fileid", fileTypeId.intValue()).iterate().next();
		String fieldDelimiter = PojoHelper.getFileFormatParamValue(oft,
				"FIELD_DELIMITER");
		String textDelimiter = PojoHelper.getFileFormatParamValue(oft,
				"TEXT_DELIMITER");
		boolean useHeader = "1".equals(PojoHelper.getFileFormatParamValue(oft,
				"USE_HEADER"));

		query = dbSession
				.createQuery(
						"select r from salesmachine.hibernatedb.OimOrderProcessingRule r, "
								+ "salesmachine.hibernatedb.OimChannelFiles f "
								+ "where r.oimChannels = f.oimChannels and f.oimFiletypes=:oft")
				.setEntity("oft", oft);
		OimOrderProcessingRule rule = (OimOrderProcessingRule) query.iterate()
				.next();

		if ("tab".equalsIgnoreCase(fieldDelimiter))
			fieldDelimiter = "\t";

		LOG.info("Text Delimiter: {} Field Delimiter: {} Use Header: {}",
				textDelimiter, fieldDelimiter, useHeader);

		if (file.exists()) {
			try {
				CSVReader reader = new CSVReader(
						new FileReader(file),
						fieldDelimiter != null && fieldDelimiter.length() > 0 ? fieldDelimiter
								.charAt(0) : CSVParser.DEFAULT_SEPARATOR,
						textDelimiter != null && textDelimiter.length() > 0 ? textDelimiter
								.charAt(0) : CSVParser.DEFAULT_QUOTE_CHARACTER);
				List rows = reader.readAll();

				// Now all the data has been fetched in header and rows
				query = dbSession
						.createQuery("from OimFileFieldMap m where m.oimFiletypes=:file and m.deleteTm is null");
				List fieldMap = query.setEntity("file", oft).list();
				Map fieldMapping = new HashMap();
				for (int i = 0; i < fieldMap.size(); i++) {
					OimFileFieldMap ffm = (OimFileFieldMap) fieldMap.get(i);
					String key = "Column#" + i;
					if (useHeader)
						key = ffm.getMappedFieldName();
					fieldMapping.put(key, ffm.getOimFields().getFieldId());
				}

				int beginIndex = 0;
				String[] header = null;
				if (useHeader) {
					header = (String[]) rows.get(0);
					beginIndex = 1;
				}
				Map orders = new HashMap();
				for (int i = beginIndex; i < rows.size(); i++) {
					String[] row = (String[]) rows.get(i);
					if (row.length == 0) {
						LOG.warn("Empty row found at index {}", i);
						continue;
					}
					Map fieldValues = new HashMap();
					for (int j = 0; j < row.length; j++) {
						String field = row[j];
						String key = "Column#" + j;
						if (useHeader)
							key = header[j];
						Integer fieldId = (Integer) fieldMapping.get(key);
						if (OimConstants.OIM_FIELD_IGNORE.equals(fieldId))
							continue;
						// System.out.println("Setting fieldId:"+fieldId+"\t"+field);
						fieldValues.put(fieldId, field);
					}

					String sku = (String) fieldValues
							.get(OimConstants.OIM_FIELD_SKU);
					String orderNumber = (String) fieldValues
							.get(OimConstants.OIM_FIELD_PRODUCT_ORDER_NUMBER);
					String delName = (String) fieldValues
							.get(OimConstants.OIM_FIELD_DELIVERYNAME);
					String delAddress = (String) fieldValues
							.get(OimConstants.OIM_FIELD_DELIVERY_ADDRESS);
					String delCity = (String) fieldValues
							.get(OimConstants.OIM_FIELD_DELIVERY_CITY);
					String delState = (String) fieldValues
							.get(OimConstants.OIM_FIELD_DELIVERY_STATE);
					String delZip = (String) fieldValues
							.get(OimConstants.OIM_FIELD_DELIVERY_ZIP);
					String delCountry = (String) fieldValues
							.get(OimConstants.OIM_FIELD_DELIVERY_COUNTY);
					String delQty = (String) fieldValues
							.get(OimConstants.OIM_FIELD_QTY);
					String shipMethod = (String) fieldValues
							.get(OimConstants.OIM_FIELD_SHIPMETHOD);
					String delCompany = (String) fieldValues
							.get(OimConstants.OIM_FIELD_DELIVERY_COMPANY);
					String delSuburb = (String) fieldValues
							.get(OimConstants.OIM_FIELD_DELIVERY_SUBURB);

					String productName = (String) fieldValues
							.get(OimConstants.OIM_FIELD_PRODUCT_NAME);
					String productDesc = (String) fieldValues
							.get(OimConstants.OIM_FIELD_PRODUCT_DESC);
					String productCost = StringHandle
							.removeNull((String) fieldValues
									.get(OimConstants.OIM_FIELD_PRODUCT_COST));
					String productSalePrice = StringHandle
							.removeNull((String) fieldValues
									.get(OimConstants.OIM_FIELD_PRODUCT_SALEPRICE));
					String orderTotal = StringHandle
							.removeNull((String) fieldValues
									.get(OimConstants.OIM_FIELD_ORDER_TOTAL_AMOUNT));

					OimOrders order = null;
					if (!orders.containsKey(orderNumber)) {
						order = new OimOrders();
						order.setStoreOrderId(orderNumber);
						order.setDeliveryName(delName);
						order.setDeliveryStreetAddress(delAddress);
						order.setDeliveryCity(delCity);
						order.setDeliveryState(delState);
						order.setDeliveryZip(delZip);
						order.setDeliveryCountry(delCountry);
						order.setDeliveryCompany(delCompany);
						order.setDeliverySuburb(delSuburb);
						order.setShippingDetails(shipMethod);
						order.setOrderTotalAmount(stringToDouble(orderTotal));
						orders.put(orderNumber, order);
					} else {
						order = (OimOrders) orders.get(orderNumber);
					}

					OimOrderDetails d = new OimOrderDetails();
					d.setSku(sku);
					d.setCostPrice(stringToDouble(productCost));
					d.setSalePrice(stringToDouble(productSalePrice));
					d.setProductName(productName);
					d.setProductDesc(productDesc);
					try {
						d.setQuantity(Integer.valueOf(delQty));
					} catch (Exception e) {
						LOG.warn("Invalid quantity: {}", delQty);
					}
					String status = StringHandle
							.removeNull((String) fieldValues
									.get(OimConstants.OIM_FIELD_PRODUCT_STORE_STATUS));
					if ((rule.getProcessAll() > 0)
							|| (status.equals(rule.getProcessWithStatus()))) {
						d.setOimOrderStatuses(new OimOrderStatuses(
								OimConstants.ORDER_STATUS_UNPROCESSED));
					} else {
						d.setOimOrderStatuses(new OimOrderStatuses(
								OimConstants.ORDER_STATUS_MANUALLY_PROCESSED));
					}
					order.getOimOrderDetailses().add(d);
				} // for (int i=0;i<rows.size();i++) {

				OimOrderBatches b = new OimOrderBatches();
				for (Iterator it = orders.keySet().iterator(); it.hasNext();) {
					String orderNumber = (String) it.next();
					OimOrders o = (OimOrders) orders.get(orderNumber);
					b.getOimOrderses().add(o);
				}

				return b;
			} catch (Exception e1) {
				LOG.error(e1.getMessage(), e1);
			}
		}
		return null;
	}

	public static void main(String[] args) {
		/*
		 * List header = getColumnHeaders(new
		 * File("/home/mayank/Desktop/full_order_detail_export.csv"
		 * ),",","\"",true); for (int i=0;i<header.size();i++) { String col =
		 * (String)header.get(i); System.out.println("Column: "+col); }
		 */
		Session dbSession = SessionManager.currentSession();
		OimOrderBatches b = processOrdersFromFile(dbSession, new File(
				"/home/mayank/Desktop/full_order_detail_export.csv"),
				new Integer(61));
		for (Iterator it = b.getOimOrderses().iterator(); it.hasNext();) {
			OimOrders orders = (OimOrders) it.next();
			LOG.info("Order Id: " + orders.getStoreOrderId());
			for (Iterator itt = orders.getOimOrderDetailses().iterator(); itt
					.hasNext();) {
				OimOrderDetails d = (OimOrderDetails) itt.next();
				LOG.info("\t" + d.getSku() + "\t" + d.getQuantity());
			}
		}
		SessionManager.closeSession();
	}
}
