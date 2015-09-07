package salesmachine.automation;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimAutomationAudit;
import salesmachine.hibernatehelper.SessionManager;

public class SaveAutomationAudit {

	private static final Logger log = LoggerFactory
			.getLogger(SaveAutomationAudit.class);

	private boolean isPullTaskCompleted;
	private boolean isTrackTaskCompleted;
	private Date startTime;
	private Date endTime;

	public SaveAutomationAudit() {
		super();
	}

	public void setPullTaskCompleted() {
		this.isPullTaskCompleted = true;
	}

	public void setTrackTaskCompleted() {
		this.isTrackTaskCompleted = true;
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

	public void persistAutomationAudit() {

		if (isPullTaskCompleted && isTrackTaskCompleted) {
			log.info("Saving automation audit");
			Set<Integer> channelSet = new HashSet<Integer>();
			setEndTime(new Date());
			Map<Integer, Integer> pullMap = new HashMap<Integer, Integer>();
			pullMap.putAll(AutomationManager.orderPullMap);
			Map<Integer, Integer> trackMap = new HashMap<Integer, Integer>();
			trackMap.putAll(AutomationManager.orderTrackMap);

			channelSet.addAll(pullMap.keySet());
			channelSet.addAll(trackMap.keySet());
			Session session = SessionManager.currentSession();

			for (Iterator<Integer> itr = channelSet.iterator(); itr.hasNext();) {
				int channelId = itr.next();
				OimAutomationAudit automationAudit = new OimAutomationAudit();
				automationAudit.setStartTime(this.getStartTime());
				automationAudit.setEndTime(this.getEndTime());
				automationAudit.setChannelID(channelId);
				automationAudit
						.setTotalOrderPull(pullMap.get(channelId) != null ? pullMap
								.get(channelId) : 0);
				automationAudit
						.setTotalOrderTracked(trackMap.get(channelId) != null ? trackMap
								.get(channelId) : 0);
				session.save(automationAudit);
			}
			this.isPullTaskCompleted = false;
			this.isTrackTaskCompleted = false;
			AutomationManager.orderPullMap.clear();
			AutomationManager.orderTrackMap.clear();
		}
	}

}