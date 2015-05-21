package salesmachine.automation;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.oim.suppliers.OimSupplierOrderPlacement;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

public class OrderHandler {
	private static final Logger log = LoggerFactory
			.getLogger(OrderHandler.class);
	private final Session session;

	public OrderHandler(Session session) {
		this.session = session;
	}

	@Subscribe
	@AllowConcurrentEvents
	public void handleOrderPull(OimOrderBatches orderBatches) {
		log.info("Order Recieved with BatchSize:{}", orderBatches
				.getOimOrderses().size());
		orderBatches.getOimOrderses();
	}

	@Subscribe
	@AllowConcurrentEvents
	public void handleOrderTracking(OimOrderDetails orderDetails) {
		log.info("Order Tracking :{}", orderDetails.getDetailId());
		OimSupplierOrderPlacement osop = new OimSupplierOrderPlacement(session);
		String trackOrder = osop.trackOrder(orderDetails.getOimOrders()
				.getOimOrderBatches().getOimChannels().getVendors()
				.getVendorId(), orderDetails.getDetailId());
		log.info("OrderId# {} ItemId# {} Status# {}", orderDetails
				.getOimOrders().getOrderId(), orderDetails.getDetailId(),
				trackOrder);
	}
}