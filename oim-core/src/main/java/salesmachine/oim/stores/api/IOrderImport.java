package salesmachine.oim.stores.api;

import org.hibernate.Session;

import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderBatchesTypes;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.suppliers.modal.OrderStatus;

public interface IOrderImport {
  boolean init(OimChannels channels, Session dbSession) throws ChannelConfigurationException;

  void getVendorOrders(OimOrderBatchesTypes batchesTypes, OimOrderBatches batch)
      throws ChannelCommunicationException, ChannelOrderFormatException,
      ChannelConfigurationException;

  void updateStoreOrder(OimOrderDetails oimOrderDetails, OrderStatus orderStatus)
      throws ChannelCommunicationException, ChannelOrderFormatException,
      ChannelConfigurationException;

  void cancelOrder(OimOrders oimOrder)
      throws ChannelOrderFormatException, ChannelCommunicationException, ChannelConfigurationException;

  void cancelOrder(OimOrderDetails oimOrder)
      throws ChannelOrderFormatException, ChannelCommunicationException;

  public static enum ChannelError {
    CHANNEL_COMMUNICATION_ERROR(2), CHANNEL_CONFIGURATION_ERROR(1), CHANNEL_ORDERFORMAT_ERROR(3);

    private final int errorCode;

    ChannelError(int errorCode) {
      this.errorCode = errorCode;
    }

    public int getErrorCode() {
      return errorCode;
    }
  }
}
