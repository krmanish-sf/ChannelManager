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

import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.oim.suppliers.OimSupplierOrderPlacement;

import com.google.common.eventbus.EventBus;

public class OrderTrackingTask extends TimerTask {
	private static final Logger log = LoggerFactory
			.getLogger(OrderTrackingTask.class);
	private final EventBus eventBus;
	private final Session session;

	public OrderTrackingTask(EventBus eventBus, Session session) {
		this.eventBus = eventBus;
		this.session = session;
	}

	@Override
	public void run() {
		log.info("Order Tracking Task Running...");
		try {
			Query query = session
					.createQuery("select distinct o from salesmachine.hibernatedb.OimOrders o "
							+ "left join fetch o.oimOrderDetailses d "
							+ "where o.deleteTm is null and "
							+ "d.deleteTm is null and d.supplierOrderStatus is not null and "
							+ "d.oimOrderStatuses.statusId = '2'");

			List<OimOrders> list = query.list();
			log.info("Found {} orders to track...", list.size());
			for (OimOrders oimorder : list) {
				Set orderdetails = oimorder.getOimOrderDetailses();
				Iterator odIter = orderdetails.iterator();
				log.debug("OrderId: {} Shipping:{} Total:{}",
						oimorder.getOrderId(), oimorder.getShippingDetails(),
						oimorder.getOrderTotalAmount());
				while (odIter.hasNext()) {
					try {
						OimOrderDetails od = (OimOrderDetails) odIter.next();
						eventBus.post(od);
					} catch (Exception e) {
						log.error("Error in tracking Order..", e);
					}
				}
			}
		} catch (HibernateException ex) {
			log.error(ex.getMessage(), ex);
		}
		log.info("Order Tracking Task Finished...");
	}

}
