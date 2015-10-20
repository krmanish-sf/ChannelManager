package com.is.cm.core.persistance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.is.cm.core.domain.ChannelShippingMap;
import com.is.cm.core.domain.ShippingMethod;

import salesmachine.hibernatedb.OimChannelShippingMap;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimShippingCarrier;
import salesmachine.hibernatedb.OimShippingMethod;
import salesmachine.hibernatedb.OimSupportedChannels;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.util.StringHandle;

public class ShippingRepositoryDB extends RepositoryBase implements ShippingRepository {
  private static Logger LOG = LoggerFactory.getLogger(ShippingRepositoryDB.class);

  @Override
  public List<ShippingMethod> findShippingMethods() {
    Session dbSession = SessionManager.currentSession();
    List<ShippingMethod> shippingMethods = new ArrayList<ShippingMethod>();
    for (OimShippingMethod object : (List<OimShippingMethod>) dbSession
        .createCriteria(OimShippingMethod.class).list()) {
      shippingMethods.add(ShippingMethod.from(object));
    }
    return shippingMethods;
  }

  @Override
  public List<ChannelShippingMap> findRegExForChannel(int supportedChannelId,
      int shippingMethodId) {
    Session dbSession = SessionManager.currentSession();
    List<OimChannelShippingMap> list = dbSession.createCriteria(OimChannelShippingMap.class)
        .add(Restrictions.eq("oimSupportedChannel.supportedChannelId", supportedChannelId))
        .add(Restrictions.eq("oimShippingMethod.id", shippingMethodId)).list();
    List<ChannelShippingMap> map = new ArrayList<ChannelShippingMap>();
    for (OimChannelShippingMap oimChannelShippingMap : list) {
      map.add(ChannelShippingMap.from(oimChannelShippingMap));
    }
    return map;
  }

  @Override
  public ChannelShippingMap saveChannelShippingMap(int shippingMethodId, int supportedChannelId,
      String rexex) {
    Session dbSession = SessionManager.currentSession();
    OimSupportedChannels oimSupportedChannels = (OimSupportedChannels) dbSession
        .get(OimSupportedChannels.class, supportedChannelId);
    OimShippingMethod oimShippingMethod = (OimShippingMethod) dbSession.get(OimShippingMethod.class,
        shippingMethodId);
    OimChannelShippingMap channelShippingMap = new OimChannelShippingMap();
    channelShippingMap.setOimShippingMethod(oimShippingMethod);
    channelShippingMap.setOimSupportedChannel(oimSupportedChannels);
    channelShippingMap.setShippingRegEx(rexex);
    channelShippingMap.setOimShippingCarrier(oimShippingMethod.getOimShippingCarrier());
    dbSession.save(channelShippingMap);
    return ChannelShippingMap.from(channelShippingMap);
  }

  @Override
  public void deleteChannelShippingMapping(int id) {

    Session currentSession = SessionManager.currentSession();
    Transaction tx = null;
    try {
      tx = currentSession.beginTransaction();
      OimChannelShippingMap entity = (OimChannelShippingMap) currentSession
          .get(OimChannelShippingMap.class, id);
      currentSession.delete(entity);
      tx.commit();
    } catch (RuntimeException e) {
      if (tx != null && tx.isActive())
        tx.rollback();
      LOG.error("Error occurred while deleting shippingMapping", e);
    }

  }

  @Override
  public String updateShippingMapping(int channelShippingMapId,
      Map<String, String> channelShippingMappingMap) {
    String channelId = StringHandle.removeNull(channelShippingMappingMap.get("channelId"));
    String methodId = StringHandle.removeNull(channelShippingMappingMap.get("methodId"));
    String carrierId = StringHandle.removeNull(channelShippingMappingMap.get("carrierId"));
    String shippingText = StringHandle.removeNull(channelShippingMappingMap.get("shippingText"));
    String mappingText = StringHandle.removeNull(channelShippingMappingMap.get("mappingText"));
    //mappingText
    Session dbSession = SessionManager.currentSession();
    Transaction tx = null;
    try {
      tx = dbSession.beginTransaction();
      OimChannels channel = (OimChannels) dbSession.get(OimChannels.class,
          Integer.parseInt(channelId));
      OimShippingCarrier carrier = (OimShippingCarrier) dbSession.get(OimShippingCarrier.class,
          Integer.parseInt(carrierId));
      OimShippingMethod method = (OimShippingMethod) dbSession.get(OimShippingMethod.class,
          Integer.parseInt(methodId));
      if(!method.getFullName().equalsIgnoreCase(mappingText)){
        return "Invalid Shipping Mapping. Please chose from the suggestions";
      }
      OimChannelShippingMap channelShippingMap = (OimChannelShippingMap) dbSession
          .get(OimChannelShippingMap.class, channelShippingMapId);
      channelShippingMap.setOimChannel(channel);
      channelShippingMap.setOimShippingCarrier(carrier);
      channelShippingMap.setOimShippingMethod(method);
      channelShippingMap.setOimSupportedChannel(channel.getOimSupportedChannels());
      channelShippingMap.setShippingRegEx(shippingText);
      dbSession.save(channelShippingMap);
      tx.commit();
      return "Shipping Saved";
    } catch (RuntimeException e) {
      if (tx != null && tx.isActive())
        tx.rollback();
      LOG.error("Exception occured while updating channel based shipping", e);
      return "Error while updating shipping Map";

    } catch (Exception e) {
      if (tx != null && tx.isActive())
        tx.rollback();
      LOG.error("Exception occured while updating channel based shipping", e);
      return "Error while updating shipping Map";
    }
  }
}
