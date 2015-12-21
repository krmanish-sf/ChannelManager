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
import salesmachine.oim.suppliers.SupplierFactory;

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
    for (OimOrders oimOrders : orderBatches.getOimOrderses()) {
      try {
        SupplierFactory osop = new SupplierFactory(SessionManager.currentSession());
        Session session = SessionManager.currentSession();
        session.createCriteria(OimChannelSupplierMap.class)
            .add(Restrictions.eq("oimChannels.channelId",
                orderBatches.getOimChannels().getChannelId()))
            .add(Restrictions.eq("oimSuppliers.supplierId", oimOrders));
        String processVendorOrder = osop.processVendorOrder(
            orderBatches.getOimChannels().getVendors().getVendorId(), oimOrders,
            orderBatches.getOimOrderBatchesTypes());
//        if (processVendorOrder) {
//          log.info("Order {} Processed Successfully", oimOrders.getOrderId());
//        } else {
//          log.error("Error in processing order {}", oimOrders.getOrderId());
//        }
        log.info(processVendorOrder);
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }
  }

  @Subscribe
  // @AllowConcurrentEvents
  public void handleOrderTracking(OimOrderDetails orderDetails) {
    try {
      log.info("Order Tracking for detailId :{} and channel is :{}", orderDetails.getDetailId(),
          orderDetails.getOimOrders().getOimOrderBatches().getOimChannels().getChannelName());
      SupplierFactory factory = new SupplierFactory(SessionManager.currentSession());
      String trackOrder = factory.trackOrder(orderDetails.getOimOrders().getOimOrderBatches()
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
    try {
      OimOrderBatches orderBatch = new OimOrderBatches();
      OimOrderBatchesTypes oimOrderBatchesTypes = new OimOrderBatchesTypes(
          OimConstants.ORDERBATCH_TYPE_ID_AUTOMATED);
      try {
        {
          IOrderImport iOrderImport = ChannelFactory.getIOrderImport(channel);
          log.info("Pulling orders for channel id: {}", channel.getChannelId());
          iOrderImport.getVendorOrders(oimOrderBatchesTypes, orderBatch);
          // TODO uncomment to enable automated order processing using automation
          /*
           * if (orderBatch != null) eventBus.post(orderBatch);
           */
        }
      } catch (ChannelConfigurationException | ChannelCommunicationException
          | ChannelOrderFormatException e) {
        if (e instanceof ChannelConfigurationException) {
          log.error(e.getMessage(), e);
          orderBatch.setDescription(
              "Error occured in pulling order due to ChannelConfiguration Error." + e.getMessage());
          orderBatch.setErrorCode(ChannelConfigurationException.getErrorcode());
        }
        if (e instanceof ChannelCommunicationException) {
          log.error(e.getMessage(), e);
          orderBatch.setDescription(
              "Error occured in pulling order due to ChannelCommunication Error." + e.getMessage());
          orderBatch.setErrorCode(ChannelCommunicationException.getErrorcode());
        }
        if (e instanceof ChannelOrderFormatException) {
          log.error(e.getMessage(), e);
          orderBatch.setDescription(
              "Error occured in pulling order due to ChannelOrderFormat Error." + e.getMessage());
          orderBatch.setErrorCode(ChannelOrderFormatException.getErrorcode());
        }
      } finally {
        Session m_dbSession = SessionManager.currentSession();
        Transaction tx = m_dbSession.getTransaction();
        if (tx != null && tx.isActive())
          tx.commit();
        tx = m_dbSession.beginTransaction();

        m_dbSession.save(orderBatch);
        tx.commit();
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

  }
}