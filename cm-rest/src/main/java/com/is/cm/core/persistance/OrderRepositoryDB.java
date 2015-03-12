package com.is.cm.core.persistance;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderStatuses;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSuppliers;
import salesmachine.hibernatedb.OimVendorsuppOrderhistory;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.suppliers.OimSupplierOrderPlacement;
import salesmachine.util.StringHandle;

import com.is.cm.core.domain.Order;
import com.is.cm.core.domain.OrderDetail;
import com.is.cm.core.domain.shop.CCTRANSMISSION;

public class OrderRepositoryDB extends RepositoryBase implements
		OrderRepository {
	private static Logger LOG = LoggerFactory
			.getLogger(OrderRepositoryDB.class);

	private String implode(String inputArray[]) {
		String AsImplodedString = "";
		if (inputArray != null && inputArray.length > 0) {
			StringBuffer sb = new StringBuffer();
			sb.append(inputArray[0]);
			for (int i = 1; i < inputArray.length; i++) {
				sb.append(",");
				sb.append(inputArray[i]);
			}
			AsImplodedString = sb.toString();
		}

		return AsImplodedString;
	}

	@Override
	public List<Order> findAll(String[] orderStatus) {
		Date d = new Date();
		// basic search
		String[] supplierValues = {};
		String[] channelValues = {};
		String[] shippingValues = {};
		String[] orderstatusValues = orderStatus;
		String supplier = implode(supplierValues);
		String channel = implode(channelValues);
		String shipping = implode(shippingValues);
		String order_status = implode(orderstatusValues);
		if (order_status.length() == 0) {
			order_status = StringHandle.removeNull("");// request.getParameter("status")
		}

		String datefrom = StringHandle.removeNull("");// request.getParameter("datefrom")
		String dateto = StringHandle.removeNull("");// request.getParameter("dateto")

		// advanced search
		String customer_name = StringHandle.removeNull("");// request.getParameter("customer_name")
		String customer_email = StringHandle.removeNull("");// request.getParameter("customer_email")
		String customer_address = StringHandle.removeNull("");// request.getParameter("customer_address")
		String order_id = StringHandle.removeNull("");// request.getParameter("customer_address")
		String customer_zip = StringHandle.removeNull("");// request.getParameter("customer_zip")
		String customer_phone = StringHandle.removeNull("");// request.getParameter("customer_phone")
		String order_total_min = StringHandle.removeNull("");// request.getParameter("order_total_min")
		String order_total_max = StringHandle.removeNull("");// request.getParameter("order_total_max")
		String sku = StringHandle.removeNull("");// request.getParameter("order_total_max")

		// creating subqueries for all the possible inputs (orderdate,
		// supplierid, statusid, batchid, channelid)
		String orderdatequerysubstring = "";
		if (datefrom.length() > 0)
			orderdatequerysubstring += "o.orderTm >= to_date('" + datefrom
					+ "','yyyy-mm-dd') and ";
		if (dateto.length() > 0)
			orderdatequerysubstring += "o.orderTm <= to_date('" + dateto
					+ "','yyyy-mm-dd') and ";

		String supplierquerysubstring = "";
		if (supplier.length() > 0)
			supplierquerysubstring = "d.oimSuppliers.supplierId in ("
					+ supplier + ") and ";

		String statusquerysubstring = "";
		if (order_status.length() > 0)
			statusquerysubstring = "d.oimOrderStatuses.statusId in ("
					+ order_status + ") and ";

		String channelquerysubstring = "";
		if (channel.length() > 0)
			channelquerysubstring = "o.oimOrderBatches.oimChannels.channelId in ("
					+ channel + ") and ";

		String customer_search = "";
		if (customer_name.length() > 0)
			customer_search += "(lower(o.deliveryName) like '%"
					+ customer_name.toLowerCase()
					+ "%' or lower(o.customerName) like '%"
					+ customer_name.toLowerCase()
					+ "%' or lower(o.billingName) like '%"
					+ customer_name.toLowerCase() + "%') and ";
		if (customer_email.length() > 0)
			customer_search += "(lower(o.deliveryEmail) like '%"
					+ customer_email.toLowerCase()
					+ "%' or lower(o.customerEmail) like '%"
					+ customer_email.toLowerCase()
					+ "%' or lower(o.billingEmail) like '%"
					+ customer_email.toLowerCase() + "%') and ";
		if (customer_address.length() > 0)
			customer_search += "(lower(o.deliveryStreetAddress) like '%"
					+ customer_address.toLowerCase()
					+ "%' or lower(o.customerStreetAddress) like '%"
					+ customer_address.toLowerCase()
					+ "%' or lower(o.billingStreetAddress) like '%"
					+ customer_address.toLowerCase() + "%') and ";
		if (order_id.length() > 0)
			customer_search += " o.storeOrderId = '" + order_id + "' and ";
		if (customer_phone.length() > 0)
			customer_search += "(lower(o.deliveryPhone) like '%"
					+ customer_phone.toLowerCase()
					+ "%' or lower(o.customerPhone) like '%"
					+ customer_phone.toLowerCase()
					+ "%' or lower(o.billingPhone) like '%"
					+ customer_phone.toLowerCase() + "%') and ";
		if (customer_zip.length() > 0)
			customer_search += "(lower(o.deliveryZip) like '%"
					+ customer_zip.toLowerCase()
					+ "%' or lower(o.customerZip) like '%"
					+ customer_zip.toLowerCase()
					+ "%' or lower(o.billingZip) like '%"
					+ customer_zip.toLowerCase() + "%') and ";

		String price_search = "";
		if (order_total_min.length() > 0)
			price_search = "o.orderTotalAmount > " + order_total_min + " and ";
		if (order_total_max.length() > 0)
			price_search = "o.orderTotalAmount < " + order_total_max + " and ";

		String sku_search = "";
		if (sku.length() > 0)
			sku_search = "d.sku = '" + sku + "' and ";

		List<Order> orders = new ArrayList<Order>();
		Session dbSession = SessionManager.currentSession();
		Transaction tx = null;
		try {
			tx = dbSession.beginTransaction();
			String sort_query = "order by o.orderFetchTm desc";

			Query query = dbSession
					.createQuery("select distinct o from salesmachine.hibernatedb.OimOrders o "
							+ "left join fetch o.oimOrderDetailses d "
							+ "where o.deleteTm is null and "
							+ "d.deleteTm is null and "
							+ orderdatequerysubstring
							+ supplierquerysubstring
							+ statusquerysubstring
							+ channelquerysubstring
							+ customer_search
							+ price_search
							+ sku_search
							+ "o.oimOrderBatches.oimChannels.vendors.vendorId=:vid "
							+ sort_query);
			// query.setCacheable(true);
			query.setInteger("vid", getVendorId());
			/*
			 * TODO:: Implement pagination to handle bulk query response
			 * query.setFirstResult(page * page_size);
			 * query.setMaxResults(page_size);
			 */
			// oimorders = query.list();

			for (Iterator iter = query.list().iterator(); iter.hasNext();) {
				OimOrders oimorder = (OimOrders) iter.next();
				Order order = Order.from(oimorder);
				orders.add(order);
				// prefetching it as it will be needed in the view
				oimorder.getOimOrderBatches().getOimChannels().getChannelId();
				LOG.debug("OrderId: {} Shipping:{} Total:{}",
						oimorder.getOrderId(), oimorder.getShippingDetails(),
						oimorder.getOrderTotalAmount());
				Set orderdetails = oimorder.getOimOrderDetailses();
				Iterator odIter = orderdetails.iterator();
				Set<OrderDetail> details = new HashSet<OrderDetail>();
				boolean found = false;
				while (odIter.hasNext()) {
					found = true;
					OimOrderDetails od = (OimOrderDetails) odIter.next();
					details.add(OrderDetail.from(od));
					od.getCostPrice();
				}
				if (!found) {
					List<OimOrderDetails> list = dbSession
							.createCriteria(OimOrderDetails.class)
							.add(Restrictions.eq("oimOrders.orderId",
									oimorder.getOrderId())).list();
					for (OimOrderDetails oimOrderDetails : list) {
						details.add(OrderDetail.from(oimOrderDetails));
					}
				}
				order.setOimOrderDetailses(details);
			}
			tx.commit();
			Date d1 = new Date();
			LOG.info("It took: {} miliseconds to fetch {} Order(s)",
					d1.getTime() - d.getTime(), orders.size());
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			LOG.error(e.getMessage(), e);
		}
		return orders;
	}

	@Override
	public Order save(Order order) {
		OimOrders oimOrders = order.toOimOrder();
		Session dbSession = SessionManager.currentSession();
		Transaction tx = null;
		try {
			tx = dbSession.beginTransaction();
			// oimOrders.setOimOrderDetailses(null);
			dbSession.merge(oimOrders);
			order.setOrderId(oimOrders.getOrderId());
			tx.commit();
			dbSession.evict(oimOrders);
			dbSession.flush();
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			LOG.error("Error occurred in save/update", e);
		}
		return Order.from(oimOrders);
	}

	@Override
	public void delete(int id) {
		Session currentSession = SessionManager.currentSession();
		OimOrders entity = (OimOrders) currentSession.get(OimOrders.class, id);
		entity.setDeleteTm(new Date());
		currentSession.save(entity);
	}

	@Override
	public Order findById(int id) {
		Session currentSession = SessionManager.currentSession();
		OimOrders entity = (OimOrders) currentSession.get(OimOrders.class, id);
		entity.getOimOrderDetailses().size();
		return Order.from(entity);
	}

	@Override
	public void update(OimOrderDetails orderDetail) {
		Session dbSession = SessionManager.currentSession();
		Transaction tx = null;
		try {
			tx = dbSession.beginTransaction();
			dbSession.merge(orderDetail);
			List<OimVendorsuppOrderhistory> list = dbSession
					.createCriteria(OimVendorsuppOrderhistory.class)
					.add(Restrictions.eq("vendors.vendorId", getVendorId()))
					.add(Restrictions.eq("oimSuppliers.supplierId", orderDetail
							.getOimSuppliers().getSupplierId())).list();
			for (OimVendorsuppOrderhistory oimVendorsuppOrderhistory : list) {
				dbSession.delete(oimVendorsuppOrderhistory);
			}
			tx.commit();
			dbSession.flush();
		} catch (Exception e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			LOG.error("Error in updating order detail.", e);
		}
	}

	@Override
	public void update(OrderDetail orderDetail) {
		Session dbSession = SessionManager.currentSession();
		Transaction tx = null;
		try {
			tx = dbSession.beginTransaction();
			OimOrderDetails details = (OimOrderDetails) dbSession.get(
					OimOrderDetails.class, orderDetail.getDetailId());
			details.setSku(orderDetail.getSku());
			details.setQuantity(orderDetail.getQuantity());
			details.setProductName(orderDetail.getProductName());
			details.setSalePrice(orderDetail.getSalePrice());
			details.setOimOrderStatuses(orderDetail.getOimOrderStatuses()
					.toOimOrderStatus());
			if (orderDetail.getOimSuppliers() == null) {
				details.setOimSuppliers(null);
			} else {
				details.setOimSuppliers(orderDetail.getOimSuppliers()
						.toOimSupplier());
				List<OimVendorsuppOrderhistory> list = dbSession
						.createCriteria(OimVendorsuppOrderhistory.class)
						.add(Restrictions.eq("vendors.vendorId", getVendorId()))
						.add(Restrictions.eq("oimSuppliers.supplierId",
								orderDetail.getOimSuppliers().getSupplierId()))
						.list();
				for (OimVendorsuppOrderhistory oimVendorsuppOrderhistory : list) {
					dbSession.delete(oimVendorsuppOrderhistory);
				}
			}

			dbSession.update(details);
			tx.commit();
			dbSession.flush();
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			LOG.error("Error ocured in save/update", e);
		}
	}

	@Override
	public List<Order> findAll() {
		return findAll(null);
	}

	private Map<String, String> orderData;

	@Override
	public Order saveOrder(final Map<String, String> orderData) {
		this.orderData = orderData;
		return Order.from(createOrder(Integer.parseInt(orderData
				.get("channelId"))));
	}

	private OimOrders createOrder(int channelId) {
		OimOrderBatches oimOrderBatches = new OimOrderBatches();
		Date orderTm = new Date();
		Date orderFetchTm = new Date();
		Double orderTotalAmount = null;
		Date insertionTm = new Date();
		Date deleteTm = null;
		String deliveryName = get("DELIVERY_NAME");
		String deliveryStreetAddress = get("DELIVERY_STREET_ADDRESS");
		String deliverySuburb = get("DELIVERY_SUBURB");
		String deliveryCity = get("DELIVERY_CITY");
		String deliveryState = get("DELIVERY_STATE");
		String deliveryCountry = get("DELIVERY_COUNTRY");
		String deliveryZip = get("DELIVERY_ZIP");
		String deliveryCompany = get("DELIVERY_COMPANY");
		String deliveryPhone = get("DELIVERY_PHONE");
		String deliveryEmail = get("DELIVERY_EMAIL");
		String billingName = get("BILLING_NAME");
		String billingStreetAddress = get("BILLING_STREET_ADDRESS");
		String billingSuburb = get("BILLING_SUBURB");
		String billingCity = get("BILLING_CITY");
		String billingState = get("BILLING_STATE");
		String billingCountry = get("BILLING_COUNTRY");
		String billingZip = get("BILLING_ZIP");
		String billingCompany = get("BILLING_COMPANY");
		String billingPhone = get("BILLING_PHONE");
		String billingEmail = get("BILLING_EMAIL");
		String customerName = get("CUSTOMER_NAME");
		String customerStreetAddress = get("CUSTOMER_STREET_ADDRESS");
		String customerSuburb = get("CUSTOMER_SUBURB");
		String customerCity = get("CUSTOMER_CITY");
		String customerState = get("CUSTOMER_STATE");
		String customerCountry = get("CUSTOMER_COUNTRY");
		String customerZip = get("CUSTOMER_ZIP");
		String customerCompany = get("CUSTOMER_COMPANY");
		String customerPhone = get("CUSTOMER_PHONE");
		String customerEmail = get("CUSTOMER_EMAIL");
		String storeOrderId = get("store_order_id");
		String shippingDetails = get("shipping_details");
		String payMethod = get("PAY_METHOD");
		String orderComment = get("ORDER_COMMENT");
		Set<OimOrderDetails> oimOrderDetailset = new HashSet<OimOrderDetails>();

		OimChannels oimChannels = new OimChannels();

		oimChannels.setChannelId(channelId);
		oimOrderBatches.setOimChannels(oimChannels);

		String[] skuArr = get("sku").split(",");
		String[] costpriceArr = get("costprice").split(",");
		// String[] descArr = get("desc").split(",");
		String[] nameArr = get("name").split(",");
		String[] quantityArr = get("quantity").split(",");
		String[] salepriceArr = get("saleprice").split(",");
		String[] statusArr = get("status").split(",");
		String[] supplierArr = get("supplier").split(",");

		OimOrders order = new OimOrders(oimOrderBatches, orderTm, orderFetchTm,
				orderTotalAmount, insertionTm, deleteTm, deliveryName,
				deliveryStreetAddress, deliverySuburb, deliveryCity,
				deliveryState, deliveryCountry, deliveryZip, deliveryCompany,
				deliveryPhone, deliveryEmail, billingName,
				billingStreetAddress, billingSuburb, billingCity, billingState,
				billingCountry, billingZip, billingCompany, billingPhone,
				billingEmail, customerName, customerStreetAddress,
				customerSuburb, customerCity, customerState, customerCountry,
				customerZip, customerCompany, customerPhone, customerEmail,
				storeOrderId, shippingDetails, payMethod, orderComment, null);
		Session dbSession = SessionManager.currentSession();
		Transaction t = null;
		try {
			t = dbSession.beginTransaction();
			dbSession.save(oimOrderBatches);
			order.setOimOrderBatches(oimOrderBatches);
			LOG.debug("SKU:" + get("sku") + "skuArr.length:" + skuArr.length);
			for (int i = 0; i < skuArr.length; i++) {
				OimOrderDetails oimOrderDetails = new OimOrderDetails();
				oimOrderDetails.setCostPrice(Double
						.parseDouble(costpriceArr[i]));
				oimOrderDetails.setInsertionTm(new Date());
				// oimOrderDetails.setProductDesc(descArr[i]);
				oimOrderDetails.setProductName(nameArr[i]);
				oimOrderDetails.setQuantity(Integer.parseInt(quantityArr[i]));
				oimOrderDetails.setSalePrice(Double
						.parseDouble(salepriceArr[i]));
				orderTotalAmount = oimOrderDetails.getSalePrice();
				oimOrderDetails.setSku(skuArr[i]);
				OimOrderStatuses oimOrderStatus = (OimOrderStatuses) dbSession
						.load(OimOrderStatuses.class,
								Integer.parseInt(statusArr[i])); //
				oimOrderStatus.setStatusId(Integer.parseInt(statusArr[i]));
				oimOrderDetails.setOimOrderStatuses(oimOrderStatus);
				OimSuppliers oimSuppliers = (OimSuppliers) dbSession.load(
						OimSuppliers.class, Integer.parseInt(supplierArr[i]));
				oimOrderDetails.setOimSuppliers(oimSuppliers);
				oimOrderDetails.setOimOrders(order);
				dbSession.saveOrUpdate(oimOrderDetails);
				oimOrderDetailset.add(oimOrderDetails);
			}
			order.setOimOrderDetailses(oimOrderDetailset);
			dbSession.save(order);
			t.commit();
			LOG.debug("Order ID:" + order.getOrderId());
			dbSession.flush();
		} catch (HibernateException e) {
			if (t != null)
				t.rollback();
			LOG.error("Exception::" + e.getMessage());
			// e.printStackTrace();
		} catch (Exception e) {
			if (t != null)
				t.rollback(); // request.setAttribute("ERROR", e);
			LOG.error("Exception");
		}

		return order;
	}

	private String get(String attribute) {
		return orderData.get(attribute);
	}

	@Override
	public List<Order> findUnprocessedOrders() {
		Session currentSession = SessionManager.currentSession();
		/*
		 * Criteria criteria = currentSession.createCriteria(OimOrders.class);
		 * criteria.add(Expression.in("status", new Integer[] {
		 * OimConstants.ORDER_STATUS_UNPROCESSED }));
		 * criteria.add(Expression.ne("supplierId", null)); List<OimOrders> list
		 * = criteria.list();
		 */
		List<Order> orderList = new ArrayList<Order>();
		// Transaction tx = null;
		try {
			// tx = currentSession.beginTransaction();
			Query query = currentSession
					.createQuery("select distinct o from salesmachine.hibernatedb.OimOrders o "
							+ "left join fetch o.oimOrderDetailses d "
							+ "where o.deleteTm is null and "
							+ "d.deleteTm is null and "
							+ "d.oimOrderStatuses.statusId = '0' and d.oimSuppliers.supplierId is not null and "
							+ "o.oimOrderBatches.oimChannels.vendors.vendorId=:vid ");
			query.setInteger("vid", getVendorId());

			List<OimOrders> list = query.list();
			for (Iterator iter = query.list().iterator(); iter.hasNext();) {
				OimOrders oimorder = (OimOrders) iter.next();
				Order order = Order.from(oimorder);
				orderList.add(order);
				// prefetching it as it will be needed in the view
				oimorder.getOimOrderBatches().getOimChannels().getChannelId();

				Set orderdetails = oimorder.getOimOrderDetailses();
				Iterator odIter = orderdetails.iterator();
				Set<OrderDetail> details = new HashSet<OrderDetail>();
				LOG.debug("OrderId: {} Shipping:{} Total:{}",
						oimorder.getOrderId(), oimorder.getShippingDetails(),
						oimorder.getOrderTotalAmount());
				boolean found = false;
				while (odIter.hasNext()) {
					found = true;
					OimOrderDetails od = (OimOrderDetails) odIter.next();
					details.add(OrderDetail.from(od));
					od.getCostPrice();
				}
				if (!found) {
					List<OimOrderDetails> list1 = currentSession
							.createCriteria(OimOrderDetails.class)
							.add(Restrictions.eq("oimOrders.orderId",
									oimorder.getOrderId())).list();
					for (OimOrderDetails oimOrderDetails : list1) {
						details.add(OrderDetail.from(oimOrderDetails));
					}
				}
				order.setOimOrderDetailses(details);
			}
			// tx.commit();
		} catch (HibernateException ex) {
			/*
			 * if (tx != null && tx.isActive()) tx.rollback();
			 */
			LOG.error(ex.getMessage(), ex);
		}
		return orderList;
	}

	@Override
	public List<Order> findUnresolvedOrders() {
		Session currentSession = SessionManager.currentSession();
		List<Order> orderList = new ArrayList<Order>();
		// Transaction tx = null;
		try {
			// tx = currentSession.beginTransaction();
			Query query = currentSession
					.createQuery("select distinct o from salesmachine.hibernatedb.OimOrders o "
							+ "left join fetch o.oimOrderDetailses d "
							+ "where o.deleteTm is null and "
							+ "d.deleteTm is null and "
							+ "d.oimOrderStatuses.statusId = '0' and "
							+ "o.oimOrderBatches.oimChannels.vendors.vendorId=:vid ");
			query.setInteger("vid", getVendorId());
			List<OimOrders> list = query.list();
			LOG.debug("Found {} unresolved orders for vendor {}", list.size(),
					getVendorId());
			for (OimOrders oimorder : list) {
				Order order = Order.from(oimorder);
				orderList.add(order);
				// prefetching it as it will be needed in the view
				oimorder.getOimOrderBatches().getOimChannels().getChannelId();
				Set orderdetails = oimorder.getOimOrderDetailses();
				Iterator odIter = orderdetails.iterator();
				LOG.debug("OrderId: {} Shipping:{} Total:{}",
						oimorder.getOrderId(), oimorder.getShippingDetails(),
						oimorder.getOrderTotalAmount());
				Set<OrderDetail> details = new HashSet<OrderDetail>();
				boolean found = false;
				while (odIter.hasNext()) {
					found = true;
					OimOrderDetails od = (OimOrderDetails) odIter.next();
					details.add(OrderDetail.from(od));
					od.getCostPrice();
				}
				if (!found) {
					List<OimOrderDetails> list1 = currentSession
							.createCriteria(OimOrderDetails.class)
							.add(Restrictions.eq("oimOrders.orderId",
									oimorder.getOrderId())).list();
					for (OimOrderDetails oimOrderDetails : list1) {
						details.add(OrderDetail.from(oimOrderDetails));
					}
				}
				order.setOimOrderDetailses(details);
				// tx.commit();
			}
		} catch (HibernateException ex) {
			/*
			 * if (tx != null && tx.isActive()) tx.rollback();
			 */
			LOG.error(ex.getMessage(), ex);
		}
		return orderList;
	}

	@Override
	public boolean processOrders(Order order) {
		Session dbSession = SessionManager.currentSession();
		OimSupplierOrderPlacement osop = new OimSupplierOrderPlacement(
				dbSession);
		OimOrders oimOrders = getById(order.getOrderId());
		return osop.processVendorOrder(getVendorId(), oimOrders);
	}

	@Override
	public List<Order> find(Map<String, String> map) {

		Date d = new Date();
		// basic search
		String supplier = StringHandle.removeNull(map.get("supplier"));
		String channel = StringHandle.removeNull(map.get("channel"));
		String shipping = StringHandle.removeNull(map.get("shipping"));
		String order_status = StringHandle.removeNull(map.get("order_status"));
		if (order_status.length() == 0) {
			order_status = StringHandle.removeNull(map.get("status"));//
		}

		String datefrom = StringHandle.removeNull(map.get("datefrom"));//
		String dateto = StringHandle.removeNull(map.get("dateto"));//

		// advanced search
		String customer_name = StringHandle
				.removeNull(map.get("customer_name"));//
		String customer_email = StringHandle.removeNull(map
				.get("customer_email"));//
		String customer_address = StringHandle.removeNull(map
				.get("customer_address"));//
		String order_id = StringHandle.removeNull(map.get("customer_address"));//
		String customer_zip = StringHandle.removeNull(map.get("customer_zip"));//
		String customer_phone = StringHandle.removeNull(map
				.get("customer_phone"));//
		String order_total_min = StringHandle.removeNull(map
				.get("order_total_min"));//
		String order_total_max = StringHandle.removeNull(map
				.get("order_total_max"));//
		String sku = StringHandle.removeNull(map.get("sku"));//

		// creating subqueries for all the possible inputs (orderdate,
		// supplierid, statusid, batchid, channelid)
		String orderdatequerysubstring = "";
		if (datefrom.length() > 0)
			orderdatequerysubstring += "o.orderTm >= to_date('" + datefrom
					+ "','mm-dd-yyyy') and ";
		if (dateto.length() > 0)
			orderdatequerysubstring += "o.orderTm <= to_date('" + dateto
					+ "','mm-dd-yyyy') and ";

		String supplierquerysubstring = "";
		if (supplier.length() > 0)
			supplierquerysubstring = "d.oimSuppliers.supplierId in ("
					+ supplier + ") and ";

		String statusquerysubstring = "";
		if (order_status.length() > 0)
			statusquerysubstring = "d.oimOrderStatuses.statusId in ("
					+ order_status + ") and ";

		String channelquerysubstring = "";
		if (channel.length() > 0)
			channelquerysubstring = "o.oimOrderBatches.oimChannels.channelId in ("
					+ channel + ") and ";

		String customer_search = "";
		if (customer_name.length() > 0)
			customer_search += "(lower(o.deliveryName) like '%"
					+ customer_name.toLowerCase()
					+ "%' or lower(o.customerName) like '%"
					+ customer_name.toLowerCase()
					+ "%' or lower(o.billingName) like '%"
					+ customer_name.toLowerCase() + "%') and ";
		if (customer_email.length() > 0)
			customer_search += "(lower(o.deliveryEmail) like '%"
					+ customer_email.toLowerCase()
					+ "%' or lower(o.customerEmail) like '%"
					+ customer_email.toLowerCase()
					+ "%' or lower(o.billingEmail) like '%"
					+ customer_email.toLowerCase() + "%') and ";
		if (customer_address.length() > 0)
			customer_search += "(lower(o.deliveryStreetAddress) like '%"
					+ customer_address.toLowerCase()
					+ "%' or lower(o.customerStreetAddress) like '%"
					+ customer_address.toLowerCase()
					+ "%' or lower(o.billingStreetAddress) like '%"
					+ customer_address.toLowerCase() + "%') and ";
		if (order_id.length() > 0)
			customer_search += " o.storeOrderId = '" + order_id + "' and ";
		if (customer_phone.length() > 0)
			customer_search += "(lower(o.deliveryPhone) like '%"
					+ customer_phone.toLowerCase()
					+ "%' or lower(o.customerPhone) like '%"
					+ customer_phone.toLowerCase()
					+ "%' or lower(o.billingPhone) like '%"
					+ customer_phone.toLowerCase() + "%') and ";
		if (customer_zip.length() > 0)
			customer_search += "(lower(o.deliveryZip) like '%"
					+ customer_zip.toLowerCase()
					+ "%' or lower(o.customerZip) like '%"
					+ customer_zip.toLowerCase()
					+ "%' or lower(o.billingZip) like '%"
					+ customer_zip.toLowerCase() + "%') and ";

		String price_search = "";
		if (order_total_min.length() > 0)
			price_search = "o.orderTotalAmount > " + order_total_min + " and ";
		if (order_total_max.length() > 0)
			price_search = "o.orderTotalAmount < " + order_total_max + " and ";

		String sku_search = "";
		if (sku.length() > 0)
			sku_search = "d.sku = '" + sku + "' and ";

		List<Order> orders = new ArrayList<Order>();
		Session dbSession = SessionManager.currentSession();
		Transaction tx = null;
		try {
			String sort_query = "order by o.orderFetchTm desc";
			Query query = dbSession
					.createQuery("select distinct o from salesmachine.hibernatedb.OimOrders o "
							+ "left join fetch o.oimOrderDetailses d "
							+ "where o.deleteTm is null and "
							+ "d.deleteTm is null and "
							+ orderdatequerysubstring
							+ supplierquerysubstring
							+ statusquerysubstring
							+ channelquerysubstring
							+ customer_search
							+ price_search
							+ sku_search
							+ "o.oimOrderBatches.oimChannels.vendors.vendorId=:vid "
							+ sort_query);
			// query.setCacheable(true);
			query.setInteger("vid", getVendorId());
			/*
			 * TODO:: Implement pagination to handle bulk query response
			 * query.setFirstResult(page * page_size);
			 * query.setMaxResults(page_size);
			 */
			// oimorders = query.list();

			for (Iterator iter = query.list().iterator(); iter.hasNext();) {
				OimOrders oimorder = (OimOrders) iter.next();
				Order order = Order.from(oimorder);
				orders.add(order);
				// prefetching it as it will be needed in the view
				oimorder.getOimOrderBatches().getOimChannels().getChannelId();

				Set orderdetails = oimorder.getOimOrderDetailses();
				Iterator odIter = orderdetails.iterator();
				Set<OrderDetail> details = new HashSet<OrderDetail>();
				while (odIter.hasNext()) {
					OimOrderDetails od = (OimOrderDetails) odIter.next();
					details.add(OrderDetail.from(od));
					od.getCostPrice();
				}
				order.setOimOrderDetailses(details);
			}

			Date d1 = new Date();
			LOG.info("It took: {} miliseconds to fetch {} Order(s)",
					d1.getTime() - d.getTime(), orders.size());
		} catch (RuntimeException e) {
			LOG.error("Erorr in fetching orders", e);
		}
		return orders;
	}

	@Override
	public OimOrders getById(int id) {
		Session dbSession = SessionManager.currentSession();
		return (OimOrders) dbSession.get(OimOrders.class, id);
	}

	@Override
	public String trackOrderStatus(Integer entity) {
		Session session = SessionManager.currentSession();
		OimSupplierOrderPlacement osop = new OimSupplierOrderPlacement(session);
		return osop.trackOrder(getVendorId(), entity);
	}

	@Override
	public List<Order> findProcessedOrders() {
		Session currentSession = SessionManager.currentSession();
		List<Order> orderList = new ArrayList<Order>();
		// Transaction tx = null;
		try {
			// tx = currentSession.beginTransaction();
			Query query = currentSession
					.createQuery("select distinct o from salesmachine.hibernatedb.OimOrders o "
							+ "left join fetch o.oimOrderDetailses d "
							+ "where o.deleteTm is null and "
							+ "d.deleteTm is null and d.supplierOrderStatus is not null and "
							+ "d.oimOrderStatuses.statusId = '2' and "
							+ "o.oimOrderBatches.oimChannels.vendors.vendorId=:vid ");
			query.setInteger("vid", getVendorId());
			List<OimOrders> list = query.list();
			LOG.debug("Found {} processed orders for vendor {}", list.size(),
					getVendorId());
			for (OimOrders oimorder : list) {
				Order order = Order.from(oimorder);
				orderList.add(order);
				// prefetching it as it will be needed in the view
				oimorder.getOimOrderBatches().getOimChannels().getChannelId();
				Set orderdetails = oimorder.getOimOrderDetailses();
				Iterator odIter = orderdetails.iterator();
				LOG.debug("OrderId: {} Shipping:{} Total:{}",
						oimorder.getOrderId(), oimorder.getShippingDetails(),
						oimorder.getOrderTotalAmount());
				Set<OrderDetail> details = new HashSet<OrderDetail>();
				while (odIter.hasNext()) {
					OimOrderDetails od = (OimOrderDetails) odIter.next();
					details.add(OrderDetail.from(od));
					od.getCostPrice();
				}
				order.setOimOrderDetailses(details);
				// tx.commit();
			}
		} catch (HibernateException ex) {
			/*
			 * if (tx != null && tx.isActive()) tx.rollback();
			 */
			LOG.error(ex.getMessage(), ex);
		}
		return orderList;
	}

	@Override
	public Order save(CCTRANSMISSION entity) {

		return null;
	}
}
