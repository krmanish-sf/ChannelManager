package salesmachine.automation;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderBatchesTypes;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.suppliers.OimSupplierOrderPlacement;
import salesmachine.util.OimLogStream;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class OrderHandler {
	private static final Logger log = LoggerFactory
			.getLogger(OrderHandler.class);
	private final Session session;
	private final EventBus eventBus;

	public OrderHandler(Session session, EventBus eventBus) {
		this.session = session;
		this.eventBus = eventBus;
	}

	@Subscribe
	//@AllowConcurrentEvents
	public void handleOrderPull(OimOrderBatches orderBatches) {
		log.info("Order Recieved with BatchSize:{}", orderBatches
				.getOimOrderses().size());
		// orderBatches.getOimOrderses();
	}

	@Subscribe
	//@AllowConcurrentEvents
	public void handleOrderTracking(OimOrderDetails orderDetails) {
		log.info("Order Tracking :{}", orderDetails.getDetailId());
		OimSupplierOrderPlacement osop = new OimSupplierOrderPlacement(
				SessionManager.currentSession());
		String trackOrder = osop.trackOrder(orderDetails.getOimOrders()
				.getOimOrderBatches().getOimChannels().getVendors()
				.getVendorId(), orderDetails.getDetailId());
		log.info("OrderId# {} ItemId# {} Status# {}", orderDetails
				.getOimOrders().getOrderId(), orderDetails.getDetailId(),
				trackOrder);
	}

	@Subscribe
	//@AllowConcurrentEvents
	public void handleOrderPull(OimChannels channel) {

		log.info("Channel Type: [{}], Name:[{}]", channel
				.getOimSupportedChannels().getChannelName(), channel
				.getChannelName());
		String orderFetchBean = channel.getOimSupportedChannels()
				.getOrderFetchBean();
		IOrderImport iOrderImport = null;
		if (orderFetchBean != null && orderFetchBean.length() > 0) {
			try {
				Class<?> theClass = Class.forName(orderFetchBean);
				iOrderImport = (IOrderImport) theClass.newInstance();

				log.debug("Created the orderimport object");
				if (!iOrderImport.init(channel.getChannelId(),
						SessionManager.currentSession(), new OimLogStream())) {
					log.error(
							"Failed initializing the channel with channelId {},",
							channel.getChannelId());
				} else {
					log.info("Pulling orders for channel id: {}",
							channel.getChannelId());
					OimOrderBatches vendorOrders = iOrderImport
							.getVendorOrders(new OimOrderBatchesTypes(
									OimConstants.ORDERBATCH_TYPE_ID_AUTOMATED));
					if (vendorOrders != null)
						eventBus.post(vendorOrders);
				}

			} catch (InstantiationException | IllegalAccessException e) {
				log.error("CONFIG ERROR: Error in Instantiating Channel Bean.");
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

	}
}