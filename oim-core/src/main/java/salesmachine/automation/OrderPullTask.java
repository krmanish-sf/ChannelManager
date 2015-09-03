package salesmachine.automation;

import java.util.List;
import java.util.TimerTask;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderBatchesTypes;
import salesmachine.hibernatedb.Reps;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.util.OimLogStream;

import com.google.common.eventbus.EventBus;

public class OrderPullTask extends TimerTask {
	private static final Logger log = LoggerFactory
			.getLogger(OrderPullTask.class);
	private final EventBus eventBus;
	private SaveAutomationAudit audit;

	// private final Session session;

	public OrderPullTask(EventBus eventBus,SaveAutomationAudit audit) {
		this.eventBus = eventBus;
		this.audit = audit;
	}

	@Override
	public void run() {
		try {
			log.info("Order Pull Task Running...");
			Session session = SessionManager.currentSession();
			Criteria vendorQuery = session.createCriteria(Reps.class).add(
					Restrictions.eq("cmAllowed", 1));
			List<Reps> list = vendorQuery.list();
			log.info("Found {} active vendors.", list.size());
			for (Reps r : list) {
				Query channelQuery = session
						.createQuery("select distinct c from salesmachine.hibernatedb.OimChannels c "
								+ "inner join c.oimSupportedChannels "
								+ "inner join c.oimOrderProcessingRules r "
								+ "left join c.oimChannelAccessDetailses d "
								+ "where c.vendors.vendorId=:vid and c.deleteTm is null");
				channelQuery.setInteger("vid", r.getVendorId());
				List list2 = channelQuery.list();
				for (Object object : list2) {
					OimChannels channel = (OimChannels) object;
					// FIXME Thread Scoped Session is behaving bad
					// eventBus.post(channel);

					log.info("Channel Type: [{}], Name:[{}]", channel
							.getOimSupportedChannels().getChannelName(),
							channel.getChannelName());
					String orderFetchBean = channel.getOimSupportedChannels()
							.getOrderFetchBean();
					IOrderImport iOrderImport = null;
					if (orderFetchBean != null && orderFetchBean.length() > 0) {
						OimOrderBatches vendorOrders = new OimOrderBatches();
						OimOrderBatchesTypes oimOrderBatchesTypes = new OimOrderBatchesTypes(
								OimConstants.ORDERBATCH_TYPE_ID_AUTOMATED);
						// try {
						try {
							Class<?> theClass = Class.forName(orderFetchBean);
							iOrderImport = (IOrderImport) theClass
									.newInstance();

							log.debug("Created the orderimport object");
							if (!iOrderImport.init(channel.getChannelId(),
									SessionManager.currentSession())) {
								log.error(
										"Failed initializing the channel with channelId {},",
										channel.getChannelId());
							} else {
								log.info("Pulling orders for channel id: {}",
										channel.getChannelId());

								iOrderImport.getVendorOrders(
										oimOrderBatchesTypes, vendorOrders);

								if (vendorOrders != null
										&& vendorOrders.getOimOrderses().size() > 0) {
									AutomationManager.orderPullMap.put(channel
											.getChannelId(), vendorOrders
											.getOimOrderses().size());
									// TODO uncomment the line below if there is
									// need for the Order to be processed
									// automatically
									// eventBus.post(vendorOrders);
								} else {
									AutomationManager.orderPullMap.put(
											channel.getChannelId(), 0);
								}
							}
						} catch (ClassNotFoundException
								| InstantiationException
								| IllegalAccessException
								| ChannelConfigurationException
								| ChannelCommunicationException
								| ChannelOrderFormatException e) {
							log.error(e.getMessage(), e);
							if (e instanceof ChannelConfigurationException) {
								vendorOrders
										.setDescription("Error occured in pulling order due to ChannelConfiguration Error."
												+ e.getMessage());
								vendorOrders
										.setErrorCode(ChannelConfigurationException
												.getErrorcode());
								AutomationManager.orderPullMap.put(
										channel.getChannelId(), 0);
							} else if (e instanceof ChannelCommunicationException) {
								vendorOrders
										.setDescription("Error occured in pulling order due to ChannelCommunication Error."
												+ e.getMessage());
								vendorOrders
										.setErrorCode(ChannelCommunicationException
												.getErrorcode());
								AutomationManager.orderPullMap.put(
										channel.getChannelId(), 0);
							} else if (e instanceof ChannelOrderFormatException) {
								vendorOrders
										.setDescription("Error occured in pulling order due to ChannelOrderFormat Error."
												+ e.getMessage());
								vendorOrders
										.setErrorCode(ChannelOrderFormatException
												.getErrorcode());
								AutomationManager.orderPullMap.put(
										channel.getChannelId(), 0);
							} else {
								vendorOrders
										.setDescription("Error occured in pulling order due to ChannelConfiguration Error."
												+ e.getMessage());
								vendorOrders
										.setErrorCode(ChannelConfigurationException
												.getErrorcode());
								AutomationManager.orderPullMap.put(
										channel.getChannelId(), 0);
							}

						} catch (Exception e) {
							log.error(e.getMessage(), e);
						} finally {
							Session m_dbSession = SessionManager
									.currentSession();
							Transaction tx = m_dbSession.getTransaction();
							if (tx != null && tx.isActive())
								tx.commit();
							tx = m_dbSession.beginTransaction();

							m_dbSession.save(vendorOrders);
							tx.commit();
						}
					}

				}
			}
			audit.setPullTaskCompleted(true);
			audit.persistAutomationAudit();
		} catch (Throwable e) {
			log.error("FATAL ERROR", e);
			AutomationManager.sendNotification(
					"ORDER PULL ERROR: " + e.getMessage(), e.toString());

		}
	}
}
