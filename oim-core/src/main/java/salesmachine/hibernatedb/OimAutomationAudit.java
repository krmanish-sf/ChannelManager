package salesmachine.hibernatedb;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "OIM_AUTOMATION_AUDIT")
public class OimAutomationAudit implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "auditSequence", sequenceName = "OIM_AUTOMATION_AUDIT_SEQUENCES", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auditSequence")
	@Column(name = "ID", nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	private Integer id;
	private Date startTime;
	private Date endTime;
	private Integer channelID;
	private Integer totalOrderPull;
	private Integer totalOrderTracked;

	public OimAutomationAudit() {
		super();
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "START_TIME", nullable = true, length = 7)
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "END_TIME", nullable = true, length = 7)
	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	@Column(name = "CHANNEL_ID", nullable = true, precision = 12)
	public Integer getChannelID() {
		return channelID;
	}

	public void setChannelID(Integer channelID) {
		this.channelID = channelID;
	}

	@Column(name = "ORDER_PULL_COUNT", nullable = true, precision = 12)
	public Integer getTotalOrderPull() {
		return totalOrderPull;
	}

	public void setTotalOrderPull(Integer totalOrderPull) {
		this.totalOrderPull = totalOrderPull;
	}

	@Column(name = "ORDER_TRACK_COUNT", nullable = true, precision = 12)
	public Integer getTotalOrderTracked() {
		return totalOrderTracked;
	}

	public void setTotalOrderTracked(Integer totalOrderTracked) {
		this.totalOrderTracked = totalOrderTracked;
	}

}
