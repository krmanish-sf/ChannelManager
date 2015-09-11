package salesmachine.oim.stores.impl;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.exception.ChannelConfigurationException;

public class ChannelFactory {
  public static final Logger log = LoggerFactory.getLogger(ChannelFactory.class);

  // Make extendable, avoid instantiation
  protected ChannelFactory() {

  }

  public static IOrderImport getIOrderImport(OimChannels channel)
      throws ChannelConfigurationException {
    String channelName = channel.getChannelName();
    log.debug("Supported channel : " + channelName);
    String orderFetchBean = channel.getOimSupportedChannels().getOrderFetchBean();
    if (orderFetchBean == null)
      throw new ChannelConfigurationException("Channel can't be initialised");
    IOrderImport coi = null;
    try {
      Class<?> channelHandlerClass = Class.forName(orderFetchBean);
      coi = (IOrderImport) channelHandlerClass.newInstance();
      coi.init(channel, SessionManager.currentSession());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      log.error(e.getMessage(), e);
      throw new ChannelConfigurationException("Channel can't be initialised", e);
    }
    return coi;
  }

  @Deprecated
  public static IOrderImport getIOrderImport(int channelId) throws ChannelConfigurationException {
    Session session = SessionManager.currentSession();
    OimChannels channel = (OimChannels) session.get(OimChannels.class, channelId);
    return getIOrderImport(channel);
  }
}
