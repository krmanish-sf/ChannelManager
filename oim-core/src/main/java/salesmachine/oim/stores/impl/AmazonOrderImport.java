package salesmachine.oim.stores.impl;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.xml.soap.AttachmentPart;

import org.apache.axis.holders.OctetStreamHolder;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import salesmachine.hibernatedb.OimChannelSupplierMap;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderBatchesTypes;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderProcessingRule;
import salesmachine.hibernatedb.OimOrderStatuses;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSuppliers;
import salesmachine.hibernatehelper.PojoHelper;
import salesmachine.markets.sellercentral.Merchant;
import salesmachine.markets.sellercentral.MerchantDocumentInfo;
import salesmachine.markets.sellercentral.MerchantInterfaceMimeLocator;
import salesmachine.markets.sellercentral.MerchantInterface_BindingStub;
import salesmachine.markets.sellercentral.MerchantInterface_PortType;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.util.NumberFormat;
import salesmachine.util.OimLogStream;
import salesmachine.util.StringHandle;

public class AmazonOrderImport implements IOrderImport {
	private String m_merchantIdentifier, m_user, m_password;
	private Session m_dbSession;
	private OimChannels m_channel;
	private OimOrderProcessingRule m_orderProcessingRule;
	private OimLogStream logStream;

	public boolean getVendorOrders() {

		try {
			DecimalFormat df = new DecimalFormat("#.##");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Set suppliers = m_channel.getOimChannelSupplierMaps();
			Map supplierMap = new HashMap();
			Iterator itr = suppliers.iterator();
			while (itr.hasNext()) {
				OimChannelSupplierMap map = (OimChannelSupplierMap) itr.next();
				if (map.getDeleteTm() != null)
					continue;

				String prefix = map.getSupplierPrefix();
				OimSuppliers supplier = map.getOimSuppliers();
				System.out.println("prefix :: " + prefix + "supplierID :: "+ supplier.getSupplierId());
				supplierMap.put(prefix, supplier);
			}

			Transaction tx = null;
			boolean ordersSaved = false;

			OimOrderBatches batch = new OimOrderBatches();
			batch.setOimChannels(m_channel);
			batch.setOimOrderBatchesTypes(new OimOrderBatchesTypes(OimConstants.ORDERBATCH_TYPE_ID_AUTOMATED));

			// Save Batch..
			tx = m_dbSession.beginTransaction();
			batch.setInsertionTm(new Date());
			batch.setCreationTm(new Date());
			m_dbSession.save(batch);
			tx.commit();

			tx = m_dbSession.beginTransaction();

			long start = System.currentTimeMillis();

			MerchantInterfaceMimeLocator locator = new MerchantInterfaceMimeLocator();
			MerchantInterface_PortType port = locator.getMerchantInterface(
					new URL("https://mws.amazonservices.com"));
			Merchant merchant = new Merchant();
			merchant.setMerchantIdentifier(m_merchantIdentifier);
			((MerchantInterface_BindingStub) port).setUsername(m_user);
			((MerchantInterface_BindingStub) port).setPassword(m_password);

			MerchantDocumentInfo[] info = port.getAllPendingDocumentInfo(merchant, "_GET_FLAT_FILE_ORDERS_DATA_");
			// MerchantDocumentInfo[] info = port.getLastNPendingDocumentInfo(merchant, "_GET_FLAT_FILE_ORDERS_DATA_", 100);
			// MerchantDocumentInfo[] info = port.getAllPendingDocumentInfo(merchant, "_GET_FLAT_FILE_ORDER_REPORT_DATA_");
			// MerchantDocumentInfo[] info = port.getAllPendingDocumentInfo(merchant, "_GET_ORDERS_DATA_");
			// MerchantDocumentInfo[] info = port.getAllPendingDocumentInfo(merchant, "_GET_FLAT_FILE_ACTIONABLE_ORDER_DATA_");

			System.out.println("Total pending order data documents found : "+ info.length);

			// Get all the orders for the current channel
			ArrayList currentOrders = getCurrentOrders();

			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.DATE, -14);
			Date cutoffDate = c.getTime();

			OimOrders lastOrder = null;
			double totalOrderAmount = 0;

			int numOrdersSaved = 0;
			for (int i = 0; i < info.length; i++) {

				System.out.println("Downloading next order document file " + i);
				String docId = info[i].getDocumentID();
				port.getDocument(merchant, docId, new OctetStreamHolder());
				Object[] returnAttachments = ((MerchantInterface_BindingStub) port).getAttachments();
				if (returnAttachments.length <= 0)
					continue;
				AttachmentPart attachment = (AttachmentPart) returnAttachments[0];
				DataHandler handler = attachment.getDataHandler();
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				handler.writeTo(os);
				String orderFileStr = os.toString();
				// System.out.println("****** orderFileStr : "+orderFileStr);
				StringTokenizer linetokenizer = new StringTokenizer(orderFileStr, "\n");
				if (linetokenizer.countTokens() > 0)
					linetokenizer.nextToken();
				else
					continue;

				while (linetokenizer.hasMoreTokens()) {
					String orderId = "", orderItemId = "", purchaseDate = "", paymentsDate = "", buyerEmail = "", buyerName = "";
					String buyerPhone = "", currency = "";
					String deliveryName = "", deliveryAddr1 = "";
					String deliveryAddr2 = "", deliveryAddr3 = "", deliveryCity = "", deliveryState = "", deliveryPin = "";
					String deliveryCountry = "", deliveryPhone = "", deliveryStartDate = "", deliveryEndDate = "";
					String deliveryTimeZone = "", deliveryInstructions = "", salesChannel = "", orderChannel = "";
					String orderChannelInstance = "", shipServiceLevel = "";
					String sku = "", productName = "", quantity = "", itemPrice = "", itemTax = "";
					String shippingPrice = "", shippingTax = "";
					
					// System.out.println("!NEXT LINE OF ORDER");
					String orderData = linetokenizer.nextToken();
					// System.out.println("**** orderData : "+orderData);
					String[] orderTokenizer = orderData.split("	");

					for (int j = 0; j < orderTokenizer.length; j++) {

						switch (j) {
						case 0:
							orderId = StringHandle.removeNull(orderTokenizer[j]);
							// System.out.println(j+" oid "+orderId);
							break;
						case 1:
							orderItemId = orderTokenizer[j];
							// System.out.println(j+" oitemid "+orderItemId);
							break;
						case 2:
							purchaseDate = orderTokenizer[j];
							// System.out.println(j+" purchasedate : "+purchaseDate);
							break;
						case 3:
							paymentsDate = orderTokenizer[j];
							// System.out.println(j+" paymentdate : "+paymentsDate);
							break;
						case 4:
							buyerEmail = orderTokenizer[j];
							// System.out.println(j+" buyweremial : "+buyerEmail);
							break;
						case 5:
							buyerName = orderTokenizer[j];
							// System.out.println(j+" buywer name "+buyerName);
							break;
						case 6:
							buyerPhone = orderTokenizer[j];
							// System.out.println(j+" buywer phone "+buyerPhone);
							break;
						case 7:
							sku = orderTokenizer[j];
							// System.out.println(j+" sku : "+sku);
							break;
						case 8:
							productName = orderTokenizer[j];
							break;
						case 9:
							quantity = orderTokenizer[j];
							break;
						case 10:
							currency = orderTokenizer[j];
							// System.out.println(j+" curr : "+currency);
							break;
						case 11:
							itemPrice = orderTokenizer[j];
							break;
						case 12:
							itemTax = orderTokenizer[j];
							break;
						case 13:
							shippingPrice = orderTokenizer[j];
							break;
						case 14:
							shippingTax = orderTokenizer[j];
							break;
						case 15:
							shipServiceLevel = orderTokenizer[j];
							break;
						case 16:
							deliveryName = orderTokenizer[j];
							// System.out.println(j+" del name "+deliveryName);
							break;
						case 17:
							deliveryAddr1 = orderTokenizer[j];
							// System.out.println(j+" del add1 "+deliveryAddr1);
							break;
						case 18:
							deliveryAddr2 = orderTokenizer[j];
							// System.out.println(j+" del add2 "+deliveryAddr2);
							break;
						case 19:
							deliveryAddr3 = orderTokenizer[j];
							// System.out.println(j+" del add3 "+deliveryAddr3);
							break;
						case 20:
							deliveryCity = orderTokenizer[j];
							// System.out.println(j+" del city "+deliveryCity);
							break;
						case 21:
							deliveryState = orderTokenizer[j];
							// System.out.println(j+" del state "+deliveryState);
							break;
						case 22:
							deliveryPin = orderTokenizer[j];
							// System.out.println(j+" del pin "+deliveryPin);
							break;
						case 23:
							deliveryCountry = orderTokenizer[j];
							// System.out.println(j+" del country : "+deliveryCountry);
							break;
						case 24:
							deliveryPhone = orderTokenizer[j];
							// System.out.println(j+" del phone "+deliveryPhone);
							break;
						case 25:
							break;
						case 26:
							break;
						case 27:
							break;
						case 28:
							break;
						case 29:
							deliveryStartDate = orderTokenizer[j];
							break;
						case 30:
							deliveryEndDate = orderTokenizer[j];
							break;
						case 31:
							deliveryTimeZone = orderTokenizer[j];
							break;
						case 32:
							deliveryInstructions = orderTokenizer[j];
							break;
						case 33:
							salesChannel = orderTokenizer[j];
							break;
						case 34:
							orderChannel = orderTokenizer[j];
							break;
						case 35:
							orderChannelInstance = orderTokenizer[j];
							break;
						}
					}

					if (lastOrder != null && !lastOrder.getStoreOrderId().equals(orderId)) {
						numOrdersSaved++;
						//System.out.println(new Date().toString() + " Order("+ numOrdersSaved + ")");

						lastOrder.setOrderTotalAmount(totalOrderAmount);
						m_dbSession.save(lastOrder);
						tx.commit();

						ordersSaved = false;

						currentOrders.add(lastOrder.getStoreOrderId());
						lastOrder = null;

						totalOrderAmount = 0;
						tx = m_dbSession.beginTransaction();
					}

					Date orderTime = null;
					try {
						orderTime = sdf.parse(purchaseDate);
						if (orderTime.before(cutoffDate))
							continue;
					} catch (Exception e) {

					}

					if (!orderId.equals("") && !currentOrders.contains(orderId)) {
						OimOrders order = null;
						if (lastOrder != null && lastOrder.getStoreOrderId().equals(orderId)) {
							order = lastOrder;
						} else {
							// New order
							order = new OimOrders();
							order.setStoreOrderId(orderId);

							order.setDeliveryName(deliveryName);
							order.setDeliveryStreetAddress(deliveryAddr1 + ", "+ deliveryAddr2);
							order.setDeliverySuburb(deliveryAddr3);
							order.setDeliveryCity(deliveryCity);
							order.setDeliveryState(deliveryState);
							order.setDeliveryCountry(deliveryCountry);
							order.setDeliveryZip(deliveryPin);
							order.setDeliveryEmail(buyerEmail);
							order.setDeliveryPhone(deliveryPhone);

							order.setCustomerName(buyerName);
							order.setCustomerPhone(buyerPhone);
							order.setCustomerEmail(buyerEmail);

							order.setInsertionTm(new Date());
							order.setOrderTm(orderTime);
							order.setShippingDetails(shipServiceLevel);
							order.setOrderTotalAmount(totalOrderAmount);

							// Saving Order..
							order.setOimOrderBatches(batch);
							order.setOrderFetchTm(new Date());
							order.setInsertionTm(new Date());

							lastOrder = order;
						}

						int qty = 0;
						double itemPriceD = 0;
						try {
							qty = Integer.parseInt(quantity);
							itemPriceD = Double.parseDouble(itemPrice);
						} catch (Exception e) {
							qty = 0;
							itemPriceD = 0;
						}

						OimOrderDetails details = new OimOrderDetails();
						details.setSalePrice(new Double(df.format(itemPriceD)));
						details.setQuantity(new Integer(quantity));
						details.setSku(sku);
						details.setOimOrders(order);
						details.setInsertionTm(new Date());
						details.setOimOrderStatuses(new OimOrderStatuses(OimConstants.ORDER_STATUS_UNPROCESSED));

						String prefix = "";
						if (sku.length() > 2) 
							prefix = sku.substring(0, 2);

						OimSuppliers supplier = null;
						if (supplierMap.containsKey(prefix)) 
							supplier = (OimSuppliers) supplierMap.get(prefix);

						details.setOimSuppliers(supplier);

						totalOrderAmount += qty * itemPriceD;

						m_dbSession.save(details);
						ordersSaved = true;
					} else {
						// String skippingOrder =
						// orderId.equals("")?"order is empty.":"order already exists. Store order id : "+orderId;
						// System.out.println("!!! Skipping order as "+skippingOrder);
					}
				}
			}

			long time = (System.currentTimeMillis() - start);
			System.out.println(" -- Finished importing orders in "+ (time / 1000) + " seconds ("
					+ NumberFormat.roundDouble((time / 1000 / 60))+ " minutes)" + "\n");

			if (ordersSaved) {
				numOrdersSaved++;
				System.out.println(new Date().toString() + " Order("+ numOrdersSaved + ")");

				lastOrder.setOrderTotalAmount(totalOrderAmount);
				m_dbSession.save(lastOrder);
				tx.commit();
				System.out.println("Import process complete !!!! ");
			} else {
				tx.rollback();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return true;
	}

	public boolean init(int channelID, Session dbSession, OimLogStream log) {
		m_dbSession = dbSession;
		if (log != null)
			logStream = log;
		else
			logStream = new OimLogStream();

		Transaction tx = m_dbSession.beginTransaction();
		Query query = m_dbSession.createQuery("from salesmachine.hibernatedb.OimChannels as c where c.channelId=:channelID");
		query.setInteger("channelID", channelID);
		tx.commit();
		if (!query.iterate().hasNext()) {
			System.out.println("No channel found for channel id: " + channelID);
			return false;
		}

		m_channel = (OimChannels) query.iterate().next();
		System.out.println("Channel name : " + m_channel.getChannelName());
		m_merchantIdentifier = PojoHelper.getChannelAccessDetailValue(m_channel, OimConstants.CHANNEL_ACCESSDETAIL_MERCHANT_TOKEN);
		m_user = PojoHelper.getChannelAccessDetailValue(m_channel,OimConstants.CHANNEL_ACCESSDETAIL_AMAZON_USER);
		m_password = PojoHelper.getChannelAccessDetailValue(m_channel,OimConstants.CHANNEL_ACCESSDETAIL_AMAZON_PASS);
		m_merchantIdentifier = StringHandle.removeNull(m_merchantIdentifier);
		m_user = StringHandle.removeNull(m_user);
		m_password = StringHandle.removeNull(m_password);

		if (m_merchantIdentifier.length() == 0) {
			System.out.println("Merchant identifier is blank. Please provide this detail.");
			return false;
		}
		return true;
	}

	public ArrayList getCurrentOrders() {
		ArrayList orders = new ArrayList();

		Query query = m_dbSession.createQuery("select o from salesmachine.hibernatedb.OimOrders o where o.oimOrderBatches.oimChannels=:chan");
		query.setEntity("chan", m_channel);
		Iterator iter = query.iterate();
		while (iter.hasNext()) {
			OimOrders o = (OimOrders) iter.next();
			orders.add(o.getStoreOrderId());
		}
		return orders;
	}
}
