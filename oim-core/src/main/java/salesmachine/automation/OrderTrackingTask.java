package salesmachine.automation;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

public class OrderTrackingTask extends TimerTask {
	private static final Logger log = LoggerFactory
			.getLogger(OrderTrackingTask.class);
	private final EventBus eventBus;

	public OrderTrackingTask(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	@Override
	public void run() {
		log.info("Order Tracking Task Running...");
		// eventBus.post(new OimOrderBatches());
	}

}
