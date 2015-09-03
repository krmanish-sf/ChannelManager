package salesmachine.automation;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatedb.OimAutomationAudit;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.util.ApplicationProperties;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

public class AutomationManager {
	private static final Logger log = LoggerFactory
			.getLogger(AutomationManager.class);
	
	public static Map<Integer, Integer> orderPullMap = new HashMap<Integer, Integer>(); // key = channel_id, val = count
	public static Map<Integer, Integer> orderTrackMap = new HashMap<Integer, Integer>(); // key = channel_id, val = count

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
		audit.setStartTime(new Date());
		TimerTask orderPullTask = new OrderPullTask(eventBus,audit);
		timer.schedule(orderPullTask, 1000L,
				ApplicationProperties.getOrderPullInterval());
		TimerTask orderTrackingTask = new OrderTrackingTask(eventBus,audit);
		timer.schedule(orderTrackingTask, 1000L,
				ApplicationProperties.getOrderTrackingInterval());

		OrderHandler orderHandler = new OrderHandler(eventBus);
		eventBus.register(orderHandler);
	}

	private static void sendNotification() {
		String subject = "Channel Manager Order Automation Service Stopped.";
		String message = "Channel Manager Order Automation Service Stopped On HOST: "
				+ ApplicationProperties.getHostName();
		log.error(subject);
		sendNotification(subject, message);
		System.exit(0);

	}

	public static void sendNotification(String subject, String message) {

		log.error(subject);
		EmailUtil
				.sendEmail(
						ApplicationProperties
								.getProperty(ApplicationProperties.AUTOMATION_MONITORING_EMAIL_TO),
						ApplicationProperties
								.getProperty(ApplicationProperties.AUTOMATION_MONITORING_EMAIL_FROM),
						ApplicationProperties
								.getProperty(ApplicationProperties.AUTOMATION_MONITORING_EMAIL_CC),
						subject, message);
	}

}

class SaveAutomationAudit{
	
	private static final Logger log = LoggerFactory
			.getLogger(SaveAutomationAudit.class);
	
	private boolean isPullTaskCompleted;
	private boolean isTrackTaskCompleted;
	private Date startTime;
	private Date endTime;
	

	public SaveAutomationAudit() {
		super();
	}
	
	public boolean isPullTaskCompleted() {
		return isPullTaskCompleted;
	}
	public void setPullTaskCompleted(boolean isPullTaskCompleted) {
		this.isPullTaskCompleted = isPullTaskCompleted;
	}
	public boolean isTrackTaskCompleted() {
		return isTrackTaskCompleted;
	}
	public void setTrackTaskCompleted(boolean isTrackTaskCompleted) {
		this.isTrackTaskCompleted = isTrackTaskCompleted;
	}
	
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public void persistAutomationAudit(){
		
		if(isPullTaskCompleted() && isTrackTaskCompleted()){
			Set<Integer> channelSet = new HashSet<Integer>();
			setEndTime(new Date());
			Map<Integer, Integer> pullMap = new HashMap<Integer, Integer>();
			pullMap.putAll(AutomationManager.orderPullMap);
			Map<Integer, Integer> trackMap = new HashMap<Integer, Integer>();
			trackMap.putAll(AutomationManager.orderTrackMap);
			
			channelSet.addAll(pullMap.keySet());
			channelSet.addAll(trackMap.keySet());
			Session session = SessionManager.currentSession();
			Transaction tx = session.beginTransaction();
			for(Iterator<Integer> itr = channelSet.iterator();itr.hasNext();){
				int channelId = itr.next();
				OimAutomationAudit automationAudit = new OimAutomationAudit();
				automationAudit.setStartTime(this.getStartTime());
				automationAudit.setEndTime(this.getEndTime());
				automationAudit.setChannelID(channelId);
				automationAudit.setTotalOrderPull(pullMap.get(channelId));
				automationAudit.setTotalOrderTracked(trackMap.get(channelId));
				session.save(automationAudit);
			}
			try {
				tx.commit();
			} catch (HibernateException e) {
				log.error(e.getMessage(),e);
			}
			AutomationManager.orderPullMap.clear();
			AutomationManager.orderTrackMap.clear();
		}
	}
	
	
}
