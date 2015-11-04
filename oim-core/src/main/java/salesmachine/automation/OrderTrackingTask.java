package salesmachine.automation;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.suppliers.HonestGreen;

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
    try {
      log.info("Order Tracking Task Running...");
      int trackCount1 = HonestGreen.updateFromConfirmation();
      int trackCount2 = HonestGreen.updateFromTracking();
      AutomationManager.orderTrackMap.put(2941, trackCount1 + trackCount2);
      Session session = SessionManager.currentSession();
      try {
        Query query = session
            .createQuery("select distinct o from salesmachine.hibernatedb.OimOrders o "
                + "left join fetch o.oimOrderDetailses d where o.deleteTm is null and "
                + "d.deleteTm is null and d.supplierOrderStatus is not null and "
                + "d.oimOrderStatuses.statusId = '2' order by d.processingTm desc");

        List<OimOrders> trackOrderList = query.list();
        log.info("Found {} orders to track...", trackOrderList.size());
        for (OimOrders oimorder : trackOrderList) {
          Set orderdetails = oimorder.getOimOrderDetailses();
          Iterator odIter = orderdetails.iterator();
          log.debug("OrderId: {} Shipping:{} Total:{}", oimorder.getOrderId(),
              oimorder.getShippingDetails(), oimorder.getOrderTotalAmount());
          while (odIter.hasNext()) {
            try {
              OimOrderDetails od = (OimOrderDetails) odIter.next();
              session.refresh(od);
              eventBus.post(od);
            } catch (Exception e) {
              log.error("Error in tracking Order..", e);
            }
          }
        }
        audit.setTrackTaskCompleted();
        audit.persistAutomationAudit();
      } catch (HibernateException ex) {
        log.error(ex.getMessage(), ex);
      } finally {
        session.close();
      }
      log.info("Order Tracking Task Queued up...");
    } catch (Throwable e) {
      log.error("FATAL ERROR", e);
      StringBuilder sb = new StringBuilder();
      for (StackTraceElement stackTraceElement : e.getStackTrace()) {
        sb.append(stackTraceElement.toString());
      }
      AutomationManager.sendNotification("ORDER TRACKING ERROR: " + e.getMessage(), sb.toString());
    }
  }

}
