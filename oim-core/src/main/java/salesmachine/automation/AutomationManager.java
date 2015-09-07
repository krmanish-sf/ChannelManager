package salesmachine.automation;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.email.EmailUtil;
import salesmachine.util.ApplicationProperties;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

public class AutomationManager {
	private static final Logger log = LoggerFactory
			.getLogger(AutomationManager.class);
	// key = channel_id, val = count
	public static Map<Integer, Integer> orderPullMap = new HashMap<Integer, Integer>();
	// key = channel_id, val = count
	public static Map<Integer, Integer> orderTrackMap = new HashMap<Integer, Integer>();

	public static void main(String[] args) {
		if (args != null && args.length > 0
				&& "stopped".equalsIgnoreCase(args[0])) {
			sendNotification();
		}
		log.info("Channel Manager Order Automation Service Started.");
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				sendNotification();
			}
		});

		ExecutorService executor = Executors
				.newFixedThreadPool(ApplicationProperties
						.getAutotmationThreadPoolSize());
		EventBus eventBus = new AsyncEventBus(executor);
		Timer timer = new Timer();

		SaveAutomationAudit audit = new SaveAutomationAudit();
		TimerTask orderPullTask = new OrderPullTask(eventBus, audit);
		timer.schedule(orderPullTask, 1000L,
				ApplicationProperties.getOrderPullInterval());
		TimerTask orderTrackingTask = new OrderTrackingTask(eventBus, audit);
		timer.schedule(orderTrackingTask, 1000L,
				ApplicationProperties.getOrderTrackingInterval());

		OrderHandler orderHandler = new OrderHandler(eventBus);
		eventBus.register(orderHandler);
	}

	private static void sendNotification() {
		String subject = "Channel Manager Order Automation Service Stopped.";
		String message = "Channel Manager Order Automation Service Stopped";
		log.error(subject);
		sendNotification(subject, message);
		System.exit(0);
	}

	public static void sendNotification(String subject, String message) {
		String host = "\nHost:" + ApplicationProperties.getHostName();
		log.error(subject);
		EmailUtil
				.sendEmail(
						ApplicationProperties
								.getProperty(ApplicationProperties.AUTOMATION_MONITORING_EMAIL_TO),
						ApplicationProperties
								.getProperty(ApplicationProperties.AUTOMATION_MONITORING_EMAIL_FROM),
						ApplicationProperties
								.getProperty(ApplicationProperties.AUTOMATION_MONITORING_EMAIL_CC),
						subject, message + host);
	}

}
