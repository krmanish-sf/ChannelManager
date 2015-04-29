package salesmachine.automation;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hibernate.Session;

import salesmachine.hibernatehelper.SessionManager;
import salesmachine.util.ApplicationProperties;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

public class AutomationManager {

	public static void main(String[] args) {

		ExecutorService executor = Executors
				.newFixedThreadPool(ApplicationProperties
						.getAutotmationThreadPoolSize());
		EventBus eventBus = new AsyncEventBus(executor);
		Session session = SessionManager.currentSession();
		Timer timer = new Timer();

		TimerTask orderPullTask = new OrderPullTask(eventBus, session);
		timer.schedule(orderPullTask, 1000L,
				ApplicationProperties.getOrderPullInterval());
		TimerTask orderTrackingTask = new OrderTrackingTask(eventBus);
		timer.schedule(orderTrackingTask, 1000L,
				ApplicationProperties.getOrderTrackingInterval());

		OrderPostHandler orderPostHandler = new OrderPostHandler();
		eventBus.register(orderPostHandler);
	}

}
