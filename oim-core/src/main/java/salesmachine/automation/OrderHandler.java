package salesmachine.automation;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimChannelSupplierMap;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderBatchesTypes;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.stores.impl.ChannelFactory;
import salesmachine.oim.suppliers.OimSupplierOrderPlacement;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class OrderHandler {
  private static final Logger log = LoggerFactory.getLogger(OrderHandler.class);
  private final EventBus eventBus;

  public OrderHandler(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Subscribe
  // @AllowConcurrentEvents
  public void handleOrderPull(OimOrderBatches orderBatches) {
    log.info("Order Recieved with BatchSize:{}", orderBatches.getOimOrderses().size());
    for (Object object : orderBatches.getOimOrderses()) {
      try {
        OimOrders oimOrders = (OimOrders) object;
        OimSupplierOrderPlacement osop = new OimSupplierOrderPlacement(
            SessionManager.currentSession());
        Session session = SessionManager.currentSession();
        session.createCriteria(OimChannelSupplierMap.class)
            .add(Restrictions.eq("oimChannels.channelId",
                orderBatches.getOimChannels().getChannelId()))
            .add(Restrictions.eq("oimSuppliers.supplierId", oimOrders));
        boolean processVendorOrder = osop.processVendorOrder(
            orderBatches.getOimChannels().getVendors().getVendorId(), oimOrders,
            orderBatches.getOimOrderBatchesTypes());
        if (processVendorOrder) {
          log.info("Order {} Processed Successfully", oimOrders.getOrderId());
        } else {
          log.error("Error in processing order {}", oimOrders.getOrderId());
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }
  }

  @Subscribe
  // @AllowConcurrentEvents
  public void handleOrderTracking(OimOrderDetails orderDetails) {
    try {
      log.info("Order Tracking :{}", orderDetails.getDetailId());
      OimSupplierOrderPlacement osop = new OimSupplierOrderPlacement(
          SessionManager.currentSession());
      String trackOrder = osop.trackOrder(orderDetails.getOimOrders().getOimOrderBatches()
          .getOimChannels().getVendors().getVendorId(), orderDetails.getDetailId());
      log.info("OrderId# {} ItemId# {} Status# {}", orderDetails.getOimOrders().getOrderId(),
          orderDetails.getDetailId(), trackOrder);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @Subscribe
  // @AllowConcurrentEvents
  public void handleOrderPull(OimChannels channel) {

    log.info("Channel Type: [{}], Name:[{}]", channel.getOimSupportedChannels().getChannelName(),
        channel.getChannelName());
    String orderFetchBean = channel.getOimSupportedChannels().getOrderFetchBean();

    if (orderFetchBean != null && orderFetchBean.length() > 0) {
      try {

        OimOrderBatches vendorOrders = new OimOrderBatches();
        OimOrderBatchesTypes oimOrderBatchesTypes = new OimOrderBatchesTypes(
            OimConstants.ORDERBATCH_TYPE_ID_AUTOMATED);
        try {
          {
            IOrderImport iOrderImport = ChannelFactory.getIOrderImport(channel);
            log.info("Pulling orders for channel id: {}", channel.getChannelId());
            // OimOrderBatches vendorOrders = new OimOrderBatches();
            iOrderImport.getVendorOrders(oimOrderBatchesTypes, vendorOrders);
            if (vendorOrders != null)
              eventBus.post(vendorOrders);
          }
        } catch (ChannelConfigurationException | ChannelCommunicationException
            | ChannelOrderFormatException e) {
          if (e instanceof ChannelConfigurationException) {
            log.error(e.getMessage(), e);
            vendorOrders
                .setDescription("Error occured in pulling order due to ChannelConfiguration Error."
                    + e.getMessage());
            vendorOrders.setErrorCode(ChannelConfigurationException.getErrorcode());
          }
          if (e instanceof ChannelCommunicationException) {
            log.error(e.getMessage(), e);
            vendorOrders
                .setDescription("Error occured in pulling order due to ChannelCommunication Error."
                    + e.getMessage());
            vendorOrders.setErrorCode(ChannelCommunicationException.getErrorcode());
          }
          if (e instanceof ChannelOrderFormatException) {
            log.error(e.getMessage(), e);
            vendorOrders.setDescription(
                "Error occured in pulling order due to ChannelOrderFormat Error." + e.getMessage());
            vendorOrders.setErrorCode(ChannelOrderFormatException.getErrorcode());
          }
        } finally {
          Session m_dbSession = SessionManager.currentSession();
          Transaction tx = m_dbSession.getTransaction();
          if (tx != null && tx.isActive())
            tx.commit();
          tx = m_dbSession.beginTransaction();

          m_dbSession.save(vendorOrders);
          tx.commit();
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }
  }
}