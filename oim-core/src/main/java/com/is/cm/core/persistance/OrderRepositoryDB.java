package com.is.cm.core.persistance;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.is.cm.core.domain.DataTableCriterias;
import com.is.cm.core.domain.DataTableCriterias.OrderCriterias;
import com.is.cm.core.domain.DataTableCriterias.SearchCriterias;
import com.is.cm.core.domain.Order;
import com.is.cm.core.domain.OrderDetail;
import com.is.cm.core.domain.OrderDetailMod;
import com.is.cm.core.domain.PagedDataResult;

import salesmachine.hibernatedb.OimChannelAccessDetails;
import salesmachine.hibernatedb.OimChannelShippingMap;
import salesmachine.hibernatedb.OimChannelSupplierMap;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderBatchesTypes;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderDetailsMods;
import salesmachine.hibernatedb.OimOrderStatuses;
import salesmachine.hibernatedb.OimOrderTracking;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSuppliers;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.OimVendorsuppOrderhistory;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.stores.impl.ChannelFactory;
import salesmachine.oim.stores.modal.shop.order.CCORDER;
import salesmachine.oim.stores.modal.shop.order.CCTRANSMISSION;
import salesmachine.oim.stores.modal.shop.order.ITEMS;
import salesmachine.oim.suppliers.SupplierFactory;
import salesmachine.oim.suppliers.exception.SupplierCommunicationException;
import salesmachine.oim.suppliers.exception.SupplierConfigurationException;
import salesmachine.oim.suppliers.exception.SupplierOrderException;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.oim.suppliers.modal.TrackingData;
import salesmachine.util.StateCodeProperty;
import salesmachine.util.StringHandle;

public class OrderRepositoryDB extends RepositoryBase implements OrderRepository {
  private static Logger LOG = LoggerFactory.getLogger(OrderRepositoryDB.class);
  private static Map<String, String> orderHistoryColumnMap = new HashMap<String, String>();
  private static Map<String, String> processOrderColumnMap = new HashMap<String, String>();

  static {
    orderHistoryColumnMap.put("1", "o.storeOrderId");
    orderHistoryColumnMap.put("2", "d.oimOrderStatuses");
    orderHistoryColumnMap.put("3", "o.orderFetchTm");
    orderHistoryColumnMap.put("4", "o.deliveryName"); // o.oimOrderBatches.oimChannels.channelId
    orderHistoryColumnMap.put("5", "c.channelName");
    orderHistoryColumnMap.put("7", "o.orderTotalAmount");

    processOrderColumnMap.put("1", "o.storeOrderId");
    processOrderColumnMap.put("2", "o.orderFetchTm");
    processOrderColumnMap.put("3", "o.deliveryName");
    processOrderColumnMap.put("4", "c.channelName");
    processOrderColumnMap.put("7", "o.orderTotalAmount");
  }

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
    if (datefrom.trim().length() > 0)
      orderdatequerysubstring += "o.orderTm >= to_date('" + datefrom.trim()
          + "','MM/DD/YYYY') and ";
    if (dateto.trim().length() > 0)
      orderdatequerysubstring += "o.orderTm <= to_date('" + dateto.trim()
          + " 23:59:59', 'MM/DD/YYYY HH24:MI:SS') and ";

    String supplierquerysubstring = "";
    if (supplier.length() > 0)
      supplierquerysubstring = "d.oimSuppliers.supplierId in (" + supplier + ") and ";

    String statusquerysubstring = "";
    if (order_status.length() > 0)
      statusquerysubstring = "d.oimOrderStatuses.statusId in (" + order_status + ") and ";

    String channelquerysubstring = "";
    if (channel.length() > 0)
      channelquerysubstring = "o.oimOrderBatches.oimChannels.channelId in (" + channel + ") and ";

    String customer_search = "";
    if (customer_name.length() > 0)
      customer_search += "(lower(o.deliveryName) like '%" + customer_name.toLowerCase()
          + "%' or lower(o.customerName) like '%" + customer_name.toLowerCase()
          + "%' or lower(o.billingName) like '%" + customer_name.toLowerCase() + "%') and ";
    if (customer_email.length() > 0)
      customer_search += "(lower(o.deliveryEmail) like '%" + customer_email.toLowerCase()
          + "%' or lower(o.customerEmail) like '%" + customer_email.toLowerCase()
          + "%' or lower(o.billingEmail) like '%" + customer_email.toLowerCase() + "%') and ";
    if (customer_address.length() > 0)
      customer_search += "(lower(o.deliveryStreetAddress) like '%" + customer_address.toLowerCase()
          + "%' or lower(o.customerStreetAddress) like '%" + customer_address.toLowerCase()
          + "%' or lower(o.billingStreetAddress) like '%" + customer_address.toLowerCase()
          + "%') and ";
    if (order_id.length() > 0)
      customer_search += " o.storeOrderId = '" + order_id + "' and ";
    if (customer_phone.length() > 0)
      customer_search += "(lower(o.deliveryPhone) like '%" + customer_phone.toLowerCase()
          + "%' or lower(o.customerPhone) like '%" + customer_phone.toLowerCase()
          + "%' or lower(o.billingPhone) like '%" + customer_phone.toLowerCase() + "%') and ";
    if (customer_zip.length() > 0)
      customer_search += "(lower(o.deliveryZip) like '%" + customer_zip.toLowerCase()
          + "%' or lower(o.customerZip) like '%" + customer_zip.toLowerCase()
          + "%' or lower(o.billingZip) like '%" + customer_zip.toLowerCase() + "%') and ";

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
              + "left join fetch o.oimOrderDetailses d " + "where o.deleteTm is null and "
              + "d.deleteTm is null and " + orderdatequerysubstring + supplierquerysubstring
              + statusquerysubstring + channelquerysubstring + customer_search + price_search
              + sku_search + "o.oimOrderBatches.oimChannels.vendors.vendorId=:vid " + sort_query);
      // query.setCacheable(true);
      query.setInteger("vid", getVendorId());
      /*
       * TODO:: Implement pagination to handle bulk query response query.setFirstResult(page *
       * page_size); query.setMaxResults(page_size);
       */
      // oimorders = query.list();

      for (Iterator iter = query.list().iterator(); iter.hasNext();) {
        OimOrders oimorder = (OimOrders) iter.next();
        Order order = Order.from(oimorder);
        orders.add(order);
        // prefetching it as it will be needed in the view
        oimorder.getOimOrderBatches().getOimChannels().getChannelId();
        LOG.debug("OrderId: {} Shipping:{} Total:{}", oimorder.getOrderId(),
            oimorder.getShippingDetails(), oimorder.getOrderTotalAmount());
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
          List<OimOrderDetails> list = dbSession.createCriteria(OimOrderDetails.class)
              .add(Restrictions.eq("oimOrders.orderId", oimorder.getOrderId())).list();
          for (OimOrderDetails oimOrderDetails : list) {
            details.add(OrderDetail.from(oimOrderDetails));
          }
        }
        order.setOimOrderDetailses(details);
      }
      tx.commit();
      Date d1 = new Date();
      LOG.info("It took: {} miliseconds to fetch {} Order(s)", d1.getTime() - d.getTime(),
          orders.size());
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
      if (orderDetail.getOimSuppliers() != null) {
        List<OimVendorsuppOrderhistory> list = dbSession
            .createCriteria(OimVendorsuppOrderhistory.class)
            .add(Restrictions.eq("vendors.vendorId", getVendorId())).add(Restrictions
                .eq("oimSuppliers.supplierId", orderDetail.getOimSuppliers().getSupplierId()))
            .list();
        for (OimVendorsuppOrderhistory oimVendorsuppOrderhistory : list) {
          oimVendorsuppOrderhistory.setDeleteTm(new Date());
          dbSession.persist(oimVendorsuppOrderhistory);
        }
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
      OimOrderDetails details = (OimOrderDetails) dbSession.get(OimOrderDetails.class,
          orderDetail.getDetailId());
      details.setSku(orderDetail.getSku());
      details.setQuantity(orderDetail.getQuantity());
      details.setProductName(orderDetail.getProductName());
      details.setSalePrice(orderDetail.getSalePrice());
      details.setOimOrderStatuses(orderDetail.getOimOrderStatuses().toOimOrderStatus());
      if (!StringHandle.isNullOrEmpty(orderDetail.getSupplierOrderNumber()))
        details.setSupplierOrderNumber(orderDetail.getSupplierOrderNumber());
      if (orderDetail.getOimSuppliers() == null) {
        details.setOimSuppliers(null);
      } else {
        details.setOimSuppliers(orderDetail.getOimSuppliers().toOimSupplier());
        List<OimVendorsuppOrderhistory> list = dbSession
            .createCriteria(OimVendorsuppOrderhistory.class)
            .add(Restrictions.eq("vendors.vendorId", getVendorId())).add(Restrictions
                .eq("oimSuppliers.supplierId", orderDetail.getOimSuppliers().getSupplierId()))
            .list();
        for (OimVendorsuppOrderhistory oimVendorsuppOrderhistory : list) {
          oimVendorsuppOrderhistory.setDeleteTm(new Date());
          dbSession.persist(oimVendorsuppOrderhistory);
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
    return Order.from(createOrder(Integer.parseInt(orderData.get("channelId"))));
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

    OimOrders order = new OimOrders(/*
                                     * oimOrderBatches, orderTm, orderFetchTm, orderTotalAmount,
                                     * insertionTm, deleteTm, deliveryName, deliveryStreetAddress,
                                     * deliverySuburb, deliveryCity, deliveryState, deliveryCountry,
                                     * deliveryZip, deliveryCompany, deliveryPhone, deliveryEmail,
                                     * billingName, billingStreetAddress, billingSuburb,
                                     * billingCity, billingState, billingCountry, billingZip,
                                     * billingCompany, billingPhone, billingEmail, customerName,
                                     * customerStreetAddress, customerSuburb, customerCity,
                                     * customerState, customerCountry, customerZip, customerCompany,
                                     * customerPhone, customerEmail, storeOrderId, shippingDetails,
                                     * payMethod, orderComment, null
                                     */);
    order.setOimOrderBatches(oimOrderBatches);
    // FIXME call the setters on the above fields.
    Session dbSession = SessionManager.currentSession();
    Transaction t = null;
    try {
      t = dbSession.beginTransaction();
      dbSession.save(oimOrderBatches);
      order.setOimOrderBatches(oimOrderBatches);
      LOG.debug("SKU:" + get("sku") + "skuArr.length:" + skuArr.length);
      for (int i = 0; i < skuArr.length; i++) {
        OimOrderDetails oimOrderDetails = new OimOrderDetails();
        oimOrderDetails.setCostPrice(Double.parseDouble(costpriceArr[i]));
        oimOrderDetails.setInsertionTm(new Date());
        // oimOrderDetails.setProductDesc(descArr[i]);
        oimOrderDetails.setProductName(nameArr[i]);
        oimOrderDetails.setQuantity(Integer.parseInt(quantityArr[i]));
        oimOrderDetails.setSalePrice(Double.parseDouble(salepriceArr[i]));
        orderTotalAmount = oimOrderDetails.getSalePrice();
        oimOrderDetails.setSku(skuArr[i]);
        OimOrderStatuses oimOrderStatus = (OimOrderStatuses) dbSession.load(OimOrderStatuses.class,
            Integer.parseInt(statusArr[i])); //
        oimOrderStatus.setStatusId(Integer.parseInt(statusArr[i]));
        oimOrderDetails.setOimOrderStatuses(oimOrderStatus);
        OimSuppliers oimSuppliers = (OimSuppliers) dbSession.load(OimSuppliers.class,
            Integer.parseInt(supplierArr[i]));
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
  public PagedDataResult<Order> findUnprocessedOrders(int firstResult, int pageSize,
      String storeOrderId) {
    Session currentSession = SessionManager.currentSession();
    long totalRecords = 0, recordsFiltered = 0;
    List<Order> orderList = new ArrayList<Order>();
    // Transaction tx = null;
    try {
      // tx = currentSession.beginTransaction();
      StringBuilder sb = new StringBuilder();
      sb.append("select distinct o from salesmachine.hibernatedb.OimOrders o")
          .append(" left join fetch o.oimOrderDetailses d").append(" where o.deleteTm is null and")
          .append(" d.deleteTm is null and")
          .append(
              " d.oimOrderStatuses.statusId = '0' and d.oimSuppliers.supplierId is not null and")
          .append(" o.oimOrderBatches.oimChannels.vendors.vendorId=:vid ");
      if (!StringHandle.isNullOrEmpty(storeOrderId)) {
        sb.append(" and o.storeOrderId like :storeOrderId");
      }
      sb.append(" order by d.insertionTm desc");
      Query query = currentSession.createQuery(sb.toString());
      query.setInteger("vid", getVendorId());
      if (!StringHandle.isNullOrEmpty(storeOrderId)) {
        query.setString("storeOrderId", storeOrderId);
      }
      if (firstResult >= 0 && pageSize > 0) {
        query.setFirstResult(firstResult).setMaxResults(pageSize);
      }
      List<OimOrders> list = query.list();
      recordsFiltered = list.size();
      for (Iterator iter = query.list().iterator(); iter.hasNext();) {
        OimOrders oimorder = (OimOrders) iter.next();
        Order order = Order.from(oimorder);
        orderList.add(order);
        // prefetching it as it will be needed in the view
        oimorder.getOimOrderBatches().getOimChannels().getChannelId();

        Set orderdetails = oimorder.getOimOrderDetailses();
        Iterator odIter = orderdetails.iterator();
        Set<OrderDetail> details = new HashSet<OrderDetail>();
        LOG.debug("OrderId: {} Shipping:{} Total:{}", oimorder.getOrderId(),
            oimorder.getShippingDetails(), oimorder.getOrderTotalAmount());
        boolean found = false;
        while (odIter.hasNext()) {
          found = true;
          OimOrderDetails od = (OimOrderDetails) odIter.next();
          details.add(OrderDetail.from(od));
          od.getCostPrice();
        }
        if (!found) {
          List<OimOrderDetails> list1 = currentSession.createCriteria(OimOrderDetails.class)
              .add(Restrictions.eq("oimOrders.orderId", oimorder.getOrderId())).list();
          for (OimOrderDetails oimOrderDetails : list1) {
            details.add(OrderDetail.from(oimOrderDetails));
          }
        }
        order.setOimOrderDetailses(details);
      }
      Query queryCount = currentSession
          .createQuery("select count(distinct o) from salesmachine.hibernatedb.OimOrders o "
              + "left join o.oimOrderDetailses d " + "where o.deleteTm is null and "
              + "d.deleteTm is null and "
              + "d.oimOrderStatuses.statusId = '0' and d.oimSuppliers.supplierId is not null and "
              + "o.oimOrderBatches.oimChannels.vendors.vendorId=:vid ");
      queryCount.setInteger("vid", getVendorId());
      totalRecords = (long) queryCount.uniqueResult();
      if (!StringHandle.isNullOrEmpty(storeOrderId))
        recordsFiltered = list.size();
      else
        recordsFiltered = totalRecords;
      // tx.commit();
    } catch (HibernateException ex) {
      /*
       * if (tx != null && tx.isActive()) tx.rollback();
       */
      LOG.error(ex.getMessage(), ex);
    }
    return new PagedDataResult<Order>(recordsFiltered, totalRecords, orderList);
  }

  @Override
  public PagedDataResult<Order> findUnresolvedOrders(DataTableCriterias criterias) {
    Session currentSession = SessionManager.currentSession();
    List<Order> orderList = new ArrayList<Order>();
    String storeOrderId = criterias.getSearch().get(SearchCriterias.value);
    int firstResult = criterias.getStart();
    int pageSize = criterias.getLength();
    // Transaction tx = null;
    long totalRecords = 0, recordsFiltered = 0;
    try {
      // tx = currentSession.beginTransaction();
      StringBuilder sb = new StringBuilder();
      sb.append("select distinct o from salesmachine.hibernatedb.OimOrders o")
          .append(" left join fetch o.oimOrderDetailses d")
          .append(" left join fetch o.oimOrderBatches b ")
          .append(" left join fetch b.oimChannels c ").append(" where o.deleteTm is null and")
          .append(" d.deleteTm is null and").append(" d.oimOrderStatuses.statusId = '0' and")
          .append(" o.oimOrderBatches.oimChannels.vendors.vendorId=:vid");
      if (!StringHandle.isNullOrEmpty(storeOrderId)) {
        sb.append(" and o.storeOrderId like :storeOrderId");
      }
      String sort_query = " order by";
      for (Map<OrderCriterias, String> map2 : criterias.getOrder()) {
        String column = map2.get(OrderCriterias.column);
        String dir = map2.get(OrderCriterias.dir);
        sort_query += " " + processOrderColumnMap.get(column) + " " + dir + ",";
      }
      sb.append(sort_query.substring(0, sort_query.length() - 1));
      Query query = currentSession.createQuery(sb.toString());
      query.setInteger("vid", getVendorId());
      if (!StringHandle.isNullOrEmpty(storeOrderId)) {
        query.setString("storeOrderId", storeOrderId);
      }
      if (firstResult >= 0 && pageSize > 0) {
        query.setFirstResult(firstResult).setMaxResults(pageSize);
      }
      List<OimOrders> list = query.list();
      LOG.debug("Found {} unresolved orders for vendor {}", recordsFiltered, getVendorId());
      for (OimOrders oimorder : list) {
        Order order = Order.from(oimorder);
        orderList.add(order);
        // prefetching it as it will be needed in the view
        oimorder.getOimOrderBatches().getOimChannels().getChannelId();
        Set orderdetails = oimorder.getOimOrderDetailses();
        Iterator odIter = orderdetails.iterator();
        LOG.debug("OrderId: {} Shipping:{} Total:{}", oimorder.getOrderId(),
            oimorder.getShippingDetails(), oimorder.getOrderTotalAmount());
        Set<OrderDetail> details = new HashSet<OrderDetail>();
        boolean found = false;
        while (odIter.hasNext()) {
          found = true;
          OimOrderDetails od = (OimOrderDetails) odIter.next();
          details.add(OrderDetail.from(od));
          od.getCostPrice();
        }
        if (!found) {
          List<OimOrderDetails> list1 = currentSession.createCriteria(OimOrderDetails.class)
              .add(Restrictions.eq("oimOrders.orderId", oimorder.getOrderId())).list();
          for (OimOrderDetails oimOrderDetails : list1) {
            details.add(OrderDetail.from(oimOrderDetails));
          }
        }
        order.setOimOrderDetailses(details);
        // tx.commit();
      }

      Query queryCount = currentSession
          .createQuery("select count(distinct o) from salesmachine.hibernatedb.OimOrders o "
              + "left join o.oimOrderDetailses d " + "where o.deleteTm is null and "
              + "d.deleteTm is null and " + "d.oimOrderStatuses.statusId = '0' and "
              + "o.oimOrderBatches.oimChannels.vendors.vendorId=:vid ");
      queryCount.setInteger("vid", getVendorId());
      totalRecords = (long) queryCount.uniqueResult();
      if (!StringHandle.isNullOrEmpty(storeOrderId))
        recordsFiltered = list.size();
      else
        recordsFiltered = totalRecords;
    } catch (HibernateException ex) {
      /*
       * if (tx != null && tx.isActive()) tx.rollback();
       */
      LOG.error(ex.getMessage(), ex);
    }
    return new PagedDataResult<Order>(recordsFiltered, totalRecords, orderList);
  }

  @Override
  public String processOrders(Order order) throws SupplierConfigurationException,
      SupplierCommunicationException, SupplierOrderException {
    Session dbSession = SessionManager.currentSession();
    SupplierFactory osop = new SupplierFactory(dbSession);
    OimOrders oimOrders = getById(order.getOrderId());
    return osop.processVendorOrder(getVendorId(), oimOrders);
  }

  public PagedDataResult<Order> find() {
    return find(null);
  }

  @Override
  public PagedDataResult<Order> find(DataTableCriterias criterias) {

    Map<String, String> map = new HashMap<String, String>();
    int start = -1;
    int maxResult = -1;
    if (criterias != null) {
      map = criterias.getFilters();
      start = criterias.getStart();
      maxResult = criterias.getLength();
    }
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
    String customer_name = StringHandle.removeNull(map.get("customer_name"));//
    String customer_email = StringHandle.removeNull(map.get("customer_email"));//
    String customer_address = StringHandle.removeNull(map.get("customer_address"));//
    String order_id = StringHandle.removeNull(map.get("order_id"));//
    String customer_zip = StringHandle.removeNull(map.get("customer_zip"));//
    String customer_phone = StringHandle.removeNull(map.get("customer_phone"));//
    String order_total_min = StringHandle.removeNull(map.get("order_total_min"));//
    String order_total_max = StringHandle.removeNull(map.get("order_total_max"));//
    String sku = StringHandle.removeNull(map.get("sku"));//
    String searchText = "";//
    if (criterias != null) {
      searchText = StringHandle.removeNull(criterias.getSearch().get(SearchCriterias.value));
    }

    // creating subqueries for all the possible inputs (orderdate,
    // supplierid, statusid, batchid, channelid)
    String orderdatequerysubstring = "";
    if (datefrom.trim().length() > 0)
      orderdatequerysubstring += "o.orderTm >= to_date('" + datefrom.trim()
          + "','MM/DD/YYYY') and ";
    if (dateto.trim().length() > 0)
      orderdatequerysubstring += "o.orderTm <= to_date('" + dateto.trim()
          + " 23:59:59', 'MM/DD/YYYY HH24:MI:SS') and ";

    String supplierquerysubstring = "";
    if (supplier.length() > 0)
      supplierquerysubstring = "d.oimSuppliers.supplierId in (" + supplier + ") and ";

    String statusquerysubstring = "";
    if (order_status.length() > 0)
      statusquerysubstring = "d.oimOrderStatuses.statusId in (" + order_status + ") and ";

    String channelquerysubstring = "";
    if (channel.length() > 0)
      channelquerysubstring = "o.oimOrderBatches.oimChannels.channelId in (" + channel + ") and ";

    String customer_search = "";
    if (customer_name.length() > 0)
      customer_search += "(lower(o.deliveryName) like '%" + customer_name.toLowerCase()
          + "%' or lower(o.customerName) like '%" + customer_name.toLowerCase()
          + "%' or lower(o.billingName) like '%" + customer_name.toLowerCase() + "%') and ";
    if (customer_email.length() > 0)
      customer_search += "(lower(o.deliveryEmail) like '%" + customer_email.toLowerCase()
          + "%' or lower(o.customerEmail) like '%" + customer_email.toLowerCase()
          + "%' or lower(o.billingEmail) like '%" + customer_email.toLowerCase() + "%') and ";
    if (customer_address.length() > 0)
      customer_search += "(lower(o.deliveryStreetAddress) like '%" + customer_address.toLowerCase()
          + "%' or lower(o.customerStreetAddress) like '%" + customer_address.toLowerCase()
          + "%' or lower(o.billingStreetAddress) like '%" + customer_address.toLowerCase()
          + "%') and ";
    if (order_id.length() > 0)
      customer_search += " o.storeOrderId = '" + order_id + "' and ";
    if (searchText.length() > 0) {
      customer_search += "( o.storeOrderId like '" + searchText + "%' or ";
      customer_search += "d.sku = '" + searchText + "' ) and ";
    }
    if (customer_phone.length() > 0)
      customer_search += "(lower(o.deliveryPhone) like '%" + customer_phone.toLowerCase()
          + "%' or lower(o.customerPhone) like '%" + customer_phone.toLowerCase()
          + "%' or lower(o.billingPhone) like '%" + customer_phone.toLowerCase() + "%') and ";
    if (customer_zip.length() > 0)
      customer_search += "(lower(o.deliveryZip) like '%" + customer_zip.toLowerCase()
          + "%' or lower(o.customerZip) like '%" + customer_zip.toLowerCase()
          + "%' or lower(o.billingZip) like '%" + customer_zip.toLowerCase() + "%') and ";

    String price_search = "";
    if (order_total_min.length() > 0)
      price_search = "o.orderTotalAmount > " + order_total_min + " and ";
    if (order_total_max.length() > 0)
      price_search = "o.orderTotalAmount < " + order_total_max + " and ";

    String sku_search = "";
    if (sku.length() > 0)
      sku_search = "d.sku = '" + sku + "' and ";

    List<Order> orders = new ArrayList<Order>();
    Long totalRecords = 0L;
    Session dbSession = SessionManager.currentSession();
    Transaction tx = null;
    try {
      String sort_query = "order by";
      if (criterias != null) {
        for (Map<OrderCriterias, String> map2 : criterias.getOrder()) {
          String column = map2.get(OrderCriterias.column);
          String dir = map2.get(OrderCriterias.dir);
          sort_query += " " + orderHistoryColumnMap.get(column) + " " + dir + ",";
        }
        sort_query = sort_query.substring(0, sort_query.length() - 1);
      } else
        sort_query += " d.oimOrderStatuses asc";
      Query query = dbSession
          .createQuery("select distinct o from salesmachine.hibernatedb.OimOrders o "
              + "left join fetch o.oimOrderDetailses d " + "left join fetch o.oimOrderBatches b "
              + "left join fetch b.oimChannels c " + "where o.deleteTm is null and "
              + "d.deleteTm is null and " + orderdatequerysubstring + supplierquerysubstring
              + statusquerysubstring + channelquerysubstring // o.oimOrderBatches.oimChannels.channelId
              + customer_search + price_search + sku_search
              + "o.oimOrderBatches.oimChannels.vendors.vendorId=:vid " + sort_query);
      query.setInteger("vid", getVendorId());
      query.setFirstResult(start).setMaxResults(maxResult);

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

      Query countQuery = dbSession.createQuery(
          "select count( distinct o.orderId) from salesmachine.hibernatedb.OimOrders o "
              + "left join o.oimOrderDetailses d " + "where o.deleteTm is null and "
              + "d.deleteTm is null and " + orderdatequerysubstring + supplierquerysubstring
              + statusquerysubstring + channelquerysubstring + customer_search + price_search
              + sku_search + "o.oimOrderBatches.oimChannels.vendors.vendorId=:vid " + sort_query);
      countQuery.setInteger("vid", getVendorId());
      Object uniqueResult = countQuery.uniqueResult();
      totalRecords = (long) uniqueResult;
      Date d1 = new Date();
      LOG.info("It took: {} miliseconds to fetch {} Order(s)", d1.getTime() - d.getTime(),
          orders.size());
    } catch (RuntimeException e) {
      LOG.error("Erorr in fetching orders", e);
    }
    return new PagedDataResult<Order>(totalRecords, totalRecords, orders);
  }

  @Override
  public OimOrders getById(int id) {
    Session dbSession = SessionManager.currentSession();
    return (OimOrders) dbSession.get(OimOrders.class, id);
  }

  @Override
  public String trackOrderStatus(Integer entity) {
    Session session = SessionManager.currentSession();
    SupplierFactory osop = new SupplierFactory(session);
    return osop.trackOrder(getVendorId(), entity);

  }

  @Override
  public PagedDataResult<Order> findProcessedOrders(int firstResult, int pageSize,
      String storeOrderId) {
    Session currentSession = SessionManager.currentSession();
    List<Order> orderList = new ArrayList<Order>();
    // Transaction tx = null;
    long totalRecords = 0, recordsFiltered = 0;
    try {
      // tx = currentSession.beginTransaction();
      StringBuilder sb = new StringBuilder();
      sb.append("select distinct o from salesmachine.hibernatedb.OimOrders o")
          .append(" left join fetch o.oimOrderDetailses d").append(" where o.deleteTm is null and")
          .append(" d.deleteTm is null and d.supplierOrderStatus is not null")
          .append(" and d.oimOrderStatuses.statusId = '2'")
          .append(" and o.oimOrderBatches.oimChannels.vendors.vendorId=:vid");
      if (!StringHandle.isNullOrEmpty(storeOrderId)) {
        sb.append(" and o.storeOrderId like :storeOrderId");
      }
      sb.append(" order by o.orderTm desc");
      Query query = currentSession.createQuery(sb.toString());
      query.setInteger("vid", getVendorId());
      if (!StringHandle.isNullOrEmpty(storeOrderId)) {
        query.setString("storeOrderId", storeOrderId);
      }
      if (firstResult >= 0 && pageSize > 0) {
        query.setFirstResult(firstResult).setMaxResults(pageSize);
      }
      List<OimOrders> list = query.list();
      recordsFiltered = list.size();
      LOG.debug("Found {} processed orders for vendor {}", recordsFiltered, getVendorId());
      for (OimOrders oimorder : list) {
        Order order = Order.from(oimorder);
        orderList.add(order);
        // prefetching it as it will be needed in the view
        oimorder.getOimOrderBatches().getOimChannels().getChannelId();
        Set orderdetails = oimorder.getOimOrderDetailses();
        Iterator odIter = orderdetails.iterator();
        LOG.debug("OrderId: {} Shipping:{} Total:{}", oimorder.getOrderId(),
            oimorder.getShippingDetails(), oimorder.getOrderTotalAmount());
        Set<OrderDetail> details = new HashSet<OrderDetail>();
        while (odIter.hasNext()) {
          OimOrderDetails od = (OimOrderDetails) odIter.next();
          details.add(OrderDetail.from(od));
          od.getCostPrice();
        }
        order.setOimOrderDetailses(details);
        // tx.commit();
      }
      Query queryCount = currentSession
          .createQuery("select count(distinct o) from salesmachine.hibernatedb.OimOrders o "
              + "left join o.oimOrderDetailses d " + "where o.deleteTm is null and "
              + "d.deleteTm is null and d.supplierOrderStatus is not null and "
              + "d.oimOrderStatuses.statusId = '2' and "
              + "o.oimOrderBatches.oimChannels.vendors.vendorId=:vid ");

      queryCount.setInteger("vid", getVendorId());
      totalRecords = (long) queryCount.uniqueResult();
      if (!StringHandle.isNullOrEmpty(storeOrderId))
        recordsFiltered = list.size();
      else
        recordsFiltered = totalRecords;
    } catch (HibernateException ex) {
      /*
       * if (tx != null && tx.isActive()) tx.rollback();
       */
      LOG.error(ex.getMessage(), ex);
    }
    return new PagedDataResult<Order>(recordsFiltered, totalRecords, orderList);
  }

  @Override
  public List<Order> save(CCTRANSMISSION entity) {

    String catalogid = entity.getCATALOGID();
    OimChannels oimChannel = getOimChannel(catalogid);
    Integer supportedChannelId = oimChannel.getOimSupportedChannels().getSupportedChannelId();
    Session m_dbSession = SessionManager.currentSession();
    Transaction tx = null;
    Map<String, OimSuppliers> supplierMap = new HashMap<String, OimSuppliers>();
    try {

      Set suppliers = oimChannel.getOimChannelSupplierMaps();

      Iterator itr = suppliers.iterator();
      while (itr.hasNext()) {
        OimChannelSupplierMap map = (OimChannelSupplierMap) itr.next();
        if (map.getDeleteTm() != null)
          continue;

        String prefix = map.getSupplierPrefix();
        OimSuppliers supplier = map.getOimSuppliers();
        System.out.println("prefix :: " + prefix + "supplierID :: " + supplier.getSupplierId());
        supplierMap.put(prefix, supplier);
      }

      OimOrderBatches batch = new OimOrderBatches();
      batch.setOimChannels(oimChannel);
      batch.setOimOrderBatchesTypes(
          new OimOrderBatchesTypes(OimConstants.ORDERBATCH_TYPE_ID_AUTOMATED));

      // Save Batch..
      tx = m_dbSession.beginTransaction();
      batch.setInsertionTm(new Date());
      batch.setCreationTm(new Date());
      m_dbSession.save(batch);
      OimOrders order = null;
      for (CCORDER ccorder : entity.getCCORDER()) {
        order = new OimOrders();
        order.setBillingCity(ccorder.getBILLINGLABEL().getADDRESS().getADCITY());
        order.setBillingCompany(ccorder.getBILLINGLABEL().getADDRESS().getADCOMPANY());
        order.setBillingCountry(ccorder.getBILLINGLABEL().getADDRESS().getADCOUNTRY());
        order.setBillingEmail(ccorder.getBILLINGLABEL().getCUSTOMER().getCUEMAIL());
        order.setBillingName(ccorder.getBILLINGLABEL().getCUSTOMER().getCUFIRSTNAME() + " "
            + ccorder.getBILLINGLABEL().getCUSTOMER().getCULASTNAME());
        order.setBillingPhone(ccorder.getBILLINGLABEL().getCUSTOMER().getCUPHONE());
        order.setBillingState(ccorder.getBILLINGLABEL().getADDRESS().getADSTATE());
        order.setBillingStreetAddress(ccorder.getBILLINGLABEL().getADDRESS().getADADDRESS1());
        order.setBillingSuburb(ccorder.getBILLINGLABEL().getADDRESS().getADADDRESS2());
        order.setBillingZip(ccorder.getBILLINGLABEL().getADDRESS().getADZIP());

        // ***************************************
        order.setCustomerCity(ccorder.getBILLINGLABEL().getADDRESS().getADCITY());
        order.setCustomerCompany(ccorder.getBILLINGLABEL().getADDRESS().getADCOMPANY());
        order.setCustomerCountry(ccorder.getBILLINGLABEL().getADDRESS().getADCOUNTRY());
        order.setCustomerEmail(ccorder.getBILLINGLABEL().getCUSTOMER().getCUEMAIL());
        order.setCustomerName(ccorder.getBILLINGLABEL().getCUSTOMER().getCUFIRSTNAME() + " "
            + ccorder.getBILLINGLABEL().getCUSTOMER().getCULASTNAME());
        order.setCustomerPhone(ccorder.getBILLINGLABEL().getCUSTOMER().getCUPHONE());
        order.setCustomerState(ccorder.getBILLINGLABEL().getADDRESS().getADSTATE());
        order.setCustomerStreetAddress(ccorder.getBILLINGLABEL().getADDRESS().getADADDRESS1());
        order.setCustomerSuburb(ccorder.getBILLINGLABEL().getADDRESS().getADADDRESS2());
        order.setCustomerZip(ccorder.getBILLINGLABEL().getADDRESS().getADZIP());
        // ***************************************
        order.setDeliveryCity(ccorder.getSHIPPINGLABEL().getADDRESS().getADCITY());
        order.setDeliveryCompany(ccorder.getSHIPPINGLABEL().getADDRESS().getADCOMPANY());
        order.setDeliveryCountry(ccorder.getSHIPPINGLABEL().getADDRESS().getADCOUNTRY());
        order.setDeliveryEmail(ccorder.getSHIPPINGLABEL().getCUSTOMER().getCUEMAIL());
        order.setDeliveryName(ccorder.getSHIPPINGLABEL().getCUSTOMER().getCUFIRSTNAME() + " "
            + ccorder.getSHIPPINGLABEL().getCUSTOMER().getCULASTNAME());
        order.setDeliveryPhone(ccorder.getSHIPPINGLABEL().getCUSTOMER().getCUPHONE());
        order.setDeliveryState(ccorder.getSHIPPINGLABEL().getADDRESS().getADSTATE());
        // order.setDeliveryStateCode(ccorder.getSHIPPINGLABEL().getADDRESS()
        // .getADSTATE());
        if (ccorder.getSHIPPINGLABEL().getADDRESS().getADSTATE().length() == 2) {
          order.setDeliveryStateCode(ccorder.getSHIPPINGLABEL().getADDRESS().getADSTATE());
        } else {
          String stateCode = validateAndGetStateCode(order);
          if (stateCode != "")
            order.setDeliveryStateCode(stateCode);
        }

        order.setDeliveryStreetAddress(ccorder.getSHIPPINGLABEL().getADDRESS().getADADDRESS1());
        order.setDeliverySuburb(ccorder.getSHIPPINGLABEL().getADDRESS().getADADDRESS2());
        order.setDeliveryZip(ccorder.getSHIPPINGLABEL().getADDRESS().getADZIP());
        order.setOrderComment(ccorder.getSHOPPERCOMMENTS());
        order.setOimOrderBatches(batch);
        order.setInsertionTm(new Date());
        order.setOrderFetchTm(new Date());
        order.setStoreOrderId(ccorder.getINVOICENO());
        String shippingDetails = ccorder.getSHIPPINGLABEL().getSLMETHOD();
        order.setShippingDetails(shippingDetails);

        Criteria findCriteria = m_dbSession.createCriteria(OimChannelShippingMap.class);
        findCriteria
            .add(Restrictions.eq("oimSupportedChannel.supportedChannelId", supportedChannelId));
        List<OimChannelShippingMap> list = findCriteria.list();
        for (OimChannelShippingMap shippingMap : list) {
          String shippingRegEx = shippingMap.getShippingRegEx();
          if (shippingDetails.equalsIgnoreCase(shippingRegEx)) {
            order.setOimShippingMethod(shippingMap.getOimShippingMethod());
            LOG.info("Shipping set to {}", shippingMap.getOimShippingMethod());
            break;
          }
        }

        if (order.getOimShippingMethod() == null)
          LOG.warn("Shipping can't be mapped for order {}", order.getStoreOrderId());
        order.setOrderTm(new Date());
        String paymentMethod = "";
        if (ccorder.getPAYMENTMETHOD().getBANKACCOUNT() != null) {
          paymentMethod = "Bank Account";
        } else if (ccorder.getPAYMENTMETHOD().getBANKTRANSFER() != null) {
          paymentMethod = "Bank Transfer";
        } else if (ccorder.getPAYMENTMETHOD().getCOD() != null) {
          paymentMethod = "Cash on Delivery";
        } else if (ccorder.getPAYMENTMETHOD().getCODWITHDELIVERYDATE() != null) {
          paymentMethod = "Cash on Delivery";
        } else if (ccorder.getPAYMENTMETHOD().getCREDITCARD() != null) {
          paymentMethod = "Credit Card";
        }
        order.setPayMethod(paymentMethod);
        String orderTotal = ccorder.getTOTALS().getTLTOTAL();
        orderTotal = orderTotal.replace("$", "");
        order.setOrderTotalAmount(Double.parseDouble(orderTotal));
        Set<OimOrderDetails> detailSet = new HashSet<OimOrderDetails>();
        for (ITEMS items : ccorder.getITEMS()) {
          OimOrderDetails details = new OimOrderDetails();
          String unitPrice = items.getITEM().getITUNITPRICE();
          unitPrice = unitPrice.replace("$", "");
          details.setCostPrice(Double.parseDouble(unitPrice));
          details.setInsertionTm(new Date());
          details.setOimOrderStatuses(new OimOrderStatuses(OimConstants.ORDER_STATUS_UNPROCESSED));
          details.setProductDesc(items.getITEM().getITDESCRIPTION());
          details.setSku(items.getITEM().getITSKU());
          details.setSalePrice(Double.parseDouble(unitPrice));
          details.setProductName(items.getITEM().getITDESCRIPTION());
          String quantity = items.getITEM().getITQUANTITY();
          details.setQuantity(Integer.valueOf(quantity));
          String sku = items.getITEM().getITSKU();
          OimSuppliers oimSuppliers = null;
          for (String prefix : supplierMap.keySet()) {
            if (sku.startsWith(prefix)) {
              oimSuppliers = supplierMap.get(prefix);
              break;
            }
          }
          if (oimSuppliers != null) {
            details.setOimSuppliers(oimSuppliers);
          }
          details.setStoreOrderItemId(items.getITEM().getITPURCHASEID());
          details.setOimOrders(order);
          m_dbSession.saveOrUpdate(details);
          detailSet.add(details);
        }
        order.setOimOrderDetailses(detailSet);
        m_dbSession.save(order);
      }
      oimChannel.setLastFetchTm(new Date());
      m_dbSession.persist(oimChannel);
      tx.commit();

    } catch (Exception e) {
      if (tx != null && tx.isActive())
        tx.rollback();
      LOG.error(e.getMessage(), e);
    }

    return null;
  }

  private OimChannels getOimChannel(String ccatalogId) {
    Session currentSession = SessionManager.currentSession();
    Criteria add = currentSession.createCriteria(OimChannelAccessDetails.class)
        .add(Restrictions.eq("oimChannelAccessFields.fieldId",
            OimConstants.CHANNEL_ACCESSDETAIL_SHOP_CATALOGID))
        .add(Restrictions.eq("detailFieldValue", ccatalogId));

    List<OimChannelAccessDetails> list = add.list();
    for (OimChannelAccessDetails details : list) {
      OimChannels oimChannels = details.getOimChannels();
      return oimChannels;
    }
    return null;
  }

  @Override
  public List<OrderDetailMod> findOrderDetailModifications(int orderDetailId) {
    Session currentSession = SessionManager.currentSession();
    Criteria orderDetailCriteria = currentSession.createCriteria(OimOrderDetailsMods.class)
        .add(Restrictions.eq("detailId", orderDetailId))
        .addOrder(org.hibernate.criterion.Order.asc("insertionTm"));
    List<OrderDetailMod> modList = new ArrayList<OrderDetailMod>();
    List<OimOrderDetailsMods> list = (List<OimOrderDetailsMods>) orderDetailCriteria.list();
    for (OimOrderDetailsMods mods : list) {
      OrderDetailMod mod = OrderDetailMod.from(mods);
      modList.add(mod);
    }
    return modList;
  }

  protected String validateAndGetStateCode(OimOrders order) {
    LOG.info("Getting state code for - {}", order.getDeliveryState());
    String stateCode = StateCodeProperty.getProperty(order.getDeliveryState());
    stateCode = StringHandle.removeNull(stateCode);
    LOG.info("state code for {} is {}", order.getDeliveryState(), stateCode);
    return stateCode;
  }

  SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");

  @Override
  public String updateTracking(int detailId, Map<String, String> orderTrackingMap) {
    StringBuffer bf = new StringBuffer(0);
    Transaction tx = null;
    try {
      Session session = SessionManager.currentSession();
      OimOrderDetails detail = (OimOrderDetails) session.get(OimOrderDetails.class, detailId);
      if (detail.getOimOrderStatuses().getStatusId().intValue() != OimConstants.ORDER_STATUS_SHIPPED
          .intValue())
        return "This item is not shipped. Please mark it as shipped and try again";
      int loopCount = orderTrackingMap.size() / 6;
      tx = session.beginTransaction();
      int newTrackingCount = 0;
      List<OimOrderTracking> orderTrackingList = new ArrayList<OimOrderTracking>();
      for (int i = 0; i <= loopCount - 1; i++) {
        String trackingIdStr = StringHandle.removeNull(orderTrackingMap.get("trackingId" + i));
        String shipDateString = StringHandle.removeNull(orderTrackingMap.get("shipDate" + i));
        String qty = StringHandle.removeNull(orderTrackingMap.get("shipQuantity" + i));
        int shipQty = Integer.parseInt(qty);
        String shipCarrier = StringHandle.removeNull(orderTrackingMap.get("shippingCarrier" + i));
        String shipMethod = StringHandle.removeNull(orderTrackingMap.get("shippingMethod" + i));
        String trackNo = StringHandle.removeNull(orderTrackingMap.get("trackingNumber" + i));
        if ("".equals(shipDateString) || "".equals(qty) || "".equals(shipQty)
            || "".equals(shipCarrier) || "".equals(shipMethod) || "".equals(trackNo)) {
          continue;
        }
        Date shipDate = null;
        try {
          shipDate = format.parse(shipDateString);
        } catch (ParseException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
        if (!"undefined".equalsIgnoreCase(trackingIdStr)) {
          try {
            int trackingId = Integer.parseInt(trackingIdStr);
            Set<OimOrderTracking> trackingSet = new HashSet<OimOrderTracking>(0);
            OimOrderTracking orderTracking = (OimOrderTracking) session.get(OimOrderTracking.class,
                trackingId);
            orderTracking.setDetail(detail);
            orderTracking.setShipDate(shipDate);
            orderTracking.setShippingCarrier(shipCarrier);
            orderTracking.setShippingMethod(shipMethod);
            orderTracking.setShipQuantity(shipQty);
            orderTracking.setTrackingNumber(trackNo);
            trackingSet.add(orderTracking);
            detail.setOimOrderTracking(trackingSet);
            session.saveOrUpdate(orderTracking);
            session.saveOrUpdate(detail);
            bf.append("Tracking id " + trackingId + " updated. <br>");
            // orderTrackingList.add(orderTracking);
          } catch (NumberFormatException e) {
            bf.append("Error updating existing tracking details. <br>");
          }
        } else if ("undefined".equalsIgnoreCase(trackingIdStr)) {
          newTrackingCount++;
          Set<OimOrderTracking> trackingSet = new HashSet<OimOrderTracking>(0);
          OimOrderTracking orderTracking = new OimOrderTracking();
          orderTracking.setDetail(detail);
          orderTracking.setInsertionTime(new Date());
          orderTracking.setShipDate(shipDate);
          orderTracking.setShippingCarrier(shipCarrier);
          orderTracking.setShippingMethod(shipMethod);
          orderTracking.setShipQuantity(shipQty);
          orderTracking.setTrackingNumber(trackNo);
          trackingSet.add(orderTracking);
          detail.setOimOrderTracking(trackingSet);
          session.save(orderTracking);
          session.save(detail);
          orderTrackingList.add(orderTracking);
        }
      }
      tx.commit();
      updateStoreOrder(detail, orderTrackingList, session);
      if (newTrackingCount > 0) {
        bf.append(newTrackingCount + " tracking details has been added and sent to store . <br>");
      }
     // tx.commit();

      return bf.toString();
    } catch (Exception e) {
      e.printStackTrace();
      bf = new StringBuffer();
      bf.append("Error while updating Tracking for detail ID - " + detailId);
      if (tx != null && tx.isActive())
        tx.rollback();
      return bf.toString();
    }
  }

  private void updateStoreOrder(OimOrderDetails detail, List<OimOrderTracking> orderTrackingList,
      Session session) throws ChannelCommunicationException, ChannelOrderFormatException,
          ChannelConfigurationException {
    // new OimSupplierOrderPlacement(session).trackOrder(detail.getOimOrders().getOimOrderBatches()
    // .getOimChannels().getVendors().getVendorId(), detail.getDetailId());
    OimChannels oimChannels = detail.getOimOrders().getOimOrderBatches().getOimChannels();

    IOrderImport iOrderImport = ChannelFactory.getIOrderImport(oimChannels);
    OrderStatus orderStatus = new OrderStatus();
    orderStatus.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_SHIPPED);
    for (int i = 0; i < orderTrackingList.size(); i++) {
      OimOrderTracking orderTracking = orderTrackingList.get(i);
      TrackingData td = new TrackingData();
      GregorianCalendar c = new GregorianCalendar();
      c.setTime(orderTracking.getShipDate());
      XMLGregorianCalendar date2 = null;
      try {
        date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
      } catch (DatatypeConfigurationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      td.setShipDate(date2);
      td.setCarrierName(orderTracking.getShippingCarrier());
      td.setCarrierCode(orderTracking.getShippingCarrier());
      td.setQuantity(orderTracking.getShipQuantity());
      td.setShipperTrackingNumber(orderTracking.getTrackingNumber());
      td.setShippingMethod(orderTracking.getShippingMethod());
      orderStatus.addTrackingData(td);
    }
    if (orderStatus != null && oimChannels.getTestMode() == 0)
      iOrderImport.updateStoreOrder(detail, orderStatus);

  }

  @Override
  public void deleteTracking(int trackingId) {
    Session currentSession = SessionManager.currentSession();
    Transaction tx = null;
    try {
      tx = currentSession.beginTransaction();
      OimOrderTracking entity = (OimOrderTracking) currentSession.get(OimOrderTracking.class,
          trackingId);
      entity.setDeleteTm(new Date());
      currentSession.save(entity);
      tx.commit();
    } catch (RuntimeException e) {
      if (tx != null && tx.isActive())
        tx.rollback();
      LOG.error("Error occurred while deleting Tracking", e);
    }
  }

  @Override
  public String geSuppliertTestModeStatus(Integer orderId) {
    Session session = SessionManager.currentSession();
    OimOrders oimOrder = (OimOrders) session.get(OimOrders.class, orderId);
    Vendors vender = oimOrder.getOimOrderBatches().getOimChannels().getVendors();
    for (OimOrderDetails detail : oimOrder.getOimOrderDetailses()) {
      OimSuppliers supplier = detail.getOimSuppliers();
      OimVendorSuppliers ovs = null;
      Query query;
      try {
        query = session.createQuery(
            "from OimVendorSuppliers s where s.oimSuppliers=:supp and s.vendors.vendorId=:vid and s.deleteTm is null");
        query.setEntity("supp", supplier);
        query.setInteger("vid", vender.getVendorId());
        Iterator it = query.iterate();
        if (it.hasNext()) {
          ovs = (OimVendorSuppliers) it.next();
          if (ovs != null && ovs.getTestMode().intValue() == 1)
            return "Test mode is enable for supplier - " + supplier.getSupplierName()
                + ". Please make it disable and try again.";
        }
      } catch (Exception e) {
      }
    }
    return "";
  }

  @Override
  public List<OrderDetail> checkHGItemAvailability(Map<Integer, String> hgItemMap) {
    List<OrderDetail> list = new ArrayList<OrderDetail>();
    Session dbSession = SessionManager.currentSession();
    OimChannels channel = null;
    String skuPrefix = null;
    int count = 0;
    for (Iterator<Integer> itr = hgItemMap.keySet().iterator(); itr.hasNext();) {
      int detailId = itr.next();
      String sku = hgItemMap.get(detailId);
      OimOrderDetails oimOrderDetail = (OimOrderDetails) dbSession.get(OimOrderDetails.class,
          detailId);
      if (count == 0) {
        channel = oimOrderDetail.getOimOrders().getOimOrderBatches().getOimChannels();
        Query query = dbSession.createSQLQuery(
            "select SUPPLIER_PREFIX from KDYER.OIM_CHANNEL_SUPPLIER_MAP where supplier_id=1822 and CHANNEL_ID=:channelId");
        query.setInteger("channelId", channel.getChannelId());
        Object q = null;
        try {
          q = query.uniqueResult();
        } catch (NonUniqueResultException e) {
          LOG.error("Error in getting supplier prefix for supplier - Honest Green and Channel - {}",
              channel.getChannelId());
        }
        if (q != null) {
          skuPrefix = (String) q;
        }
      }
      if(skuPrefix==null)
        sku="HG"+sku;
      else if(!"HG".equalsIgnoreCase(skuPrefix)){
        sku = sku.replaceFirst(skuPrefix, "HG");
      }
      List<String> findSkuList = new ArrayList<String>();
      String prefix = sku.substring(0, 2);
      String tempSku = sku.substring(2, sku.length());
      findSkuList.add(sku);
      if (tempSku.startsWith("0")) {
        String tempSku2 = tempSku.substring(1, tempSku.length());
        findSkuList.add(prefix + tempSku2);
      } else
        findSkuList.add(prefix + "0" + tempSku);

      boolean isPhi = isPHI(sku, findSkuList, dbSession);
      if (isPhi){
        count++;
        continue;
      }
      boolean isHva = isHVA(sku, getVendorId(), findSkuList, dbSession);
      if (!isHva) {
        OrderDetail detail = OrderDetail.from(oimOrderDetail);
        list.add(detail);
      }
      count++;
    }
    return list;
  }

  private boolean isHVA(String sku, Integer vendorId, List<String> skuList, Session dbSession) {
    Query query = dbSession.createSQLQuery(
        "select SKU from VENDOR_CUSTOM_FEEDS_PRODUCTS where sku IN (:skuList) and is_restricted=1 and VENDOR_CUSTOM_FEED_ID=(select VENDOR_CUSTOM_FEED_ID from VENDOR_CUSTOM_FEEDS where vendor_id=:vendorID AND IS_RESTRICTED=1)");
    query.setParameterList("skuList", skuList);
    query.setInteger("vendorID", vendorId);
    List q = query.list();
    if(q!=null && q.size()>0)
      return true;
    return false;
  }

  private boolean isPHI(String sku, List<String> skuList, Session dbSession) {
    Query query = dbSession.createSQLQuery("select sku from PRODUCT where SKU IN (:skuList)");
    query.setParameterList("skuList", skuList);
    List q = query.list();
    if (q != null && q.size() > 0)
      return true;
    return false;
  }
  
  @Override
  public void updateDetailWithStatus(OimOrderDetails detail, Integer orderStatus) {

    Session dbSession = SessionManager.currentSession();
    Transaction tx = null;
    try {
      tx = dbSession.beginTransaction();
      detail.setOimOrderStatuses(new OimOrderStatuses(orderStatus));
      dbSession.saveOrUpdate(detail);
      tx.commit();
    } catch (Exception e) {
      if (tx != null && tx.isActive())
        tx.rollback();
      LOG.error("Error in updating order detail.", e);
    }
  
    
  }
}
