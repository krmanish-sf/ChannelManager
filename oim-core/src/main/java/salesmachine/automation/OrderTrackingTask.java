package salesmachine.automation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderStatuses;
import salesmachine.hibernatedb.OimOrderTracking;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.stores.impl.ChannelFactory;
import salesmachine.oim.suppliers.Supplier;
import salesmachine.oim.suppliers.SupplierFactory;
import salesmachine.oim.suppliers.modal.OrderStatus;

public class OrderTrackingTask extends TimerTask {
  private static final Logger log = LoggerFactory.getLogger(OrderTrackingTask.class);
  private final EventBus eventBus;
  private SaveAutomationAudit audit;

  public OrderTrackingTask(EventBus eventBus, SaveAutomationAudit audit) {
    this.eventBus = eventBus;
    this.audit = audit;
  }

  @Override
  public void run() {
    Session session = null;
    try {
      log.info("Order Tracking Task Running...");
      // int trackCount1 = HonestGreen.updateFromConfirmation();
      // int trackCount2 = HonestGreen.updateFromTracking();
      // AutomationManager.orderTrackMap.put(2941, trackCount1 + trackCount2);
      // Session session = SessionManager.currentSession();
      session = SessionManager.openSession();
      SessionManager.setSession(session);
      Calendar cal = new GregorianCalendar();
      cal.add(Calendar.DAY_OF_MONTH, -60);
      Date cutoffTime = cal.getTime();
      log.info("New session created for order tracking task at- {}", new Date());
      Query query = null;
      try {
        query = session.createQuery("select distinct o from salesmachine.hibernatedb.OimOrders o "
            + "left join fetch o.oimOrderDetailses d where o.deleteTm is null and "
            + "d.deleteTm is null and d.supplierOrderStatus is not null and "
            + "d.oimOrderStatuses.statusId = '2' order by d.processingTm desc");

        List<OimOrders> trackOrderList = query.list();
        log.info("Found {} orders to track...", trackOrderList.size());
        for (OimOrders oimorder : trackOrderList) {
          if (oimorder.getInsertionTm().before(cutoffTime)) {
            // log.info("Store order id - {} is older than 60 days. so skipping
            // it.",oimorder.getStoreOrderId());
            continue;
          }
          session.refresh(oimorder);
          Set orderdetails = oimorder.getOimOrderDetailses();
          Iterator odIter = orderdetails.iterator();
          log.debug("OrderId: {} Shipping:{} Total:{}", oimorder.getOrderId(),
              oimorder.getShippingDetails(), oimorder.getOrderTotalAmount());
          while (odIter.hasNext()) {
            try {
              OimOrderDetails orderDetails = (OimOrderDetails) odIter.next();
              session.refresh(orderDetails);
              // eventBus.post(od);

              log.info("Order Tracking for detailId :{} and channel is :{}",
                  orderDetails.getDetailId(), orderDetails.getOimOrders().getOimOrderBatches()
                      .getOimChannels().getChannelName());
              SupplierFactory factory = new SupplierFactory(SessionManager.currentSession());
              String trackOrder = factory.trackOrder(orderDetails.getOimOrders()
                  .getOimOrderBatches().getOimChannels().getVendors().getVendorId(),
                  orderDetails.getDetailId());
              log.info("OrderId# {} ItemId# {} Status# {}",
                  orderDetails.getOimOrders().getOrderId(), orderDetails.getDetailId(), trackOrder);

            } catch (Exception e) {
              log.error("Error in tracking Order..", e);
            }
          }
        }
        audit.setTrackTaskCompleted();
        audit.persistAutomationAudit();
        // update the tracking to the store if not sent when got updated in CM (those details which
        // has shipped status but not completed status)
        query = session.createSQLQuery(
            "select * from kdyer.oim_order_tracking t where t.detail_id in (select d.detail_id from KDYER.OIM_ORDER_DETAILS d where"
                + " d.status_id=7 and d.PROCESSING_TM>to_date('31-DEC-15','DD-Mon-YY'))");

        List<OimOrderTracking> trackingList = query.list();

        for (OimOrderTracking tracking : trackingList) {
          OrderStatus orderStatus = OrderStatus.getOrderStatusFromOimOrderTracking(tracking);
          OimOrderDetails detail = tracking.getDetail();
          OimChannels oimChannels = detail.getOimOrders().getOimOrderBatches().getOimChannels();
          OimVendorSuppliers oimVendorSuppliers = null;
          int vendorId = detail.getOimOrders().getOimOrderBatches().getOimChannels().getVendors()
              .getVendorId();
          query = session.createQuery(
              "from OimVendorSuppliers s where s.oimSuppliers=:supp and s.vendors.vendorId=:vid and s.deleteTm is null");
          query.setEntity("supp", detail.getOimSuppliers());
          query.setInteger("vid", vendorId);
          Object it = query.uniqueResult();
          if (it instanceof OimVendorSuppliers)
            oimVendorSuppliers = (OimVendorSuppliers) it;
          try {
            IOrderImport iOrderImport = ChannelFactory.getIOrderImport(oimChannels);

            if (orderStatus != null && oimChannels.getTestMode() == 0) {
              // if (existingQuantity < oimOrderDetails.getQuantity())
              iOrderImport.updateStoreOrder(detail, orderStatus);
              Transaction tx = null;
              try {
                tx = session.getTransaction();
                if (tx != null && tx.isActive())
                  tx.commit();
                tx = session.beginTransaction();
                detail
                    .setOimOrderStatuses(new OimOrderStatuses(OimConstants.ORDER_STATUS_COMPLETE));
                session.update(detail);
                tx.commit();
              } catch (Exception e) {
                if (tx != null && tx.isActive()) {
                  tx.rollback();
                }
                log.error(e.getMessage(), e);
              }
            }

          } catch (ChannelConfigurationException e) {
            log.error(e.getMessage(), e);
            Supplier.updateVendorSupplierOrderHistory(vendorId,
                oimVendorSuppliers.getOimSuppliers(),
                "Error occured in updating store order status due to ChannelConfiguration Error. "
                    + e.getMessage(),
                Supplier.ERROR_ORDER_TRACKING);
          } catch (ChannelCommunicationException e) {
            log.error(e.getMessage(), e);
            Supplier.updateVendorSupplierOrderHistory(vendorId,
                oimVendorSuppliers.getOimSuppliers(),
                "Error occured in updating store order status due to ChannelCommunication Error. "
                    + e.getMessage(),
                Supplier.ERROR_ORDER_TRACKING);
          } catch (ChannelOrderFormatException e) {
            log.error(e.getMessage(), e);
            Supplier.updateVendorSupplierOrderHistory(vendorId,
                oimVendorSuppliers.getOimSuppliers(),
                "Error occured in updating store order status due to ChannelOrderFormat Error. "
                    + e.getMessage(),
                Supplier.ERROR_ORDER_TRACKING);
          }

        }

      } catch (HibernateException ex) {
        log.error(ex.getMessage(), ex);
      } finally {
        // session.close();
      }
      log.info("Order Tracking Task Queued up...");
    } catch (Throwable e) {
      log.error("FATAL ERROR", e);
      StringBuilder sb = new StringBuilder();
      for (StackTraceElement stackTraceElement : e.getStackTrace()) {
        sb.append(stackTraceElement.toString());
      }
      AutomationManager.sendNotification("ORDER TRACKING ERROR: " + e.getMessage(), sb.toString());
    } finally {
      session.close();
      log.info("Session closed for order tracking task at- {}", new Date());
    }
  }

}
