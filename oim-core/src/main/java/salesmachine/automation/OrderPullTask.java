package salesmachine.automation;

import java.util.List;
import java.util.TimerTask;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.Reps;

import com.google.common.eventbus.EventBus;

public class OrderPullTask extends TimerTask {
	private static final Logger log = LoggerFactory
			.getLogger(OrderPullTask.class);
	private final EventBus eventBus;
	private final Session session;

	public OrderPullTask(EventBus eventBus, Session session) {
		this.eventBus = eventBus;
		this.session = session;
	}

	@Override
	public void run() {
		log.info("Order Pull Task Running...");
		Query vendorQuery = session
				.createQuery("from salesmachine.hibernatedb.Reps r where r.cmAllowed=1");
		List<Reps> list = vendorQuery.list();
		log.info("Found {} active vendors.", list.size());
		for (Reps r : list) {
			Query channelQuery = session
					.createQuery("select distinct c from salesmachine.hibernatedb.OimChannels c "
							+ "inner join c.oimSupportedChannels "
							+ "inner join c.oimOrderProcessingRules r "
							+ "left join c.oimChannelAccessDetailses d "
							+ "where c.vendors.vendorId=:vid");
			channelQuery.setInteger("vid", r.getVendorId());
			for (Object object : channelQuery.list()) {
				OimChannels channel = (OimChannels) object;
				eventBus.post(channel);
			}
		}
	}
}
