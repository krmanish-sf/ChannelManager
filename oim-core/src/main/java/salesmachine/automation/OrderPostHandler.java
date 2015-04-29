package salesmachine.automation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimOrderBatches;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

public class OrderPostHandler {
	private static final Logger log = LoggerFactory
			.getLogger(OrderPostHandler.class);

	@Subscribe
	@AllowConcurrentEvents
	public void handleOrderPull(OimOrderBatches orderBatches) {
		log.info("Order Recieved with BatchSize:{}", orderBatches
				.getOimOrderses().size());
	}
}
