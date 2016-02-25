package salesmachine.oim.stores.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimChannelShippingMap;
import salesmachine.hibernatedb.OimChannelSupplierMap;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderProcessingRule;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSuppliers;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.util.CountryCodeProperty;
import salesmachine.util.StateCodeProperty;
import salesmachine.util.StringHandle;

public abstract class ChannelBase implements IOrderImport {
  private static final Logger log = LoggerFactory.getLogger(ChannelBase.class);
  protected Session m_dbSession;
  protected OimChannels m_channel;
  protected OimOrderProcessingRule m_orderProcessingRule;
  protected Map<OimSuppliers, String> supplierMap;
  protected List<OimChannelShippingMap> oimChannelShippingMapList;

  @Override
  public boolean init(OimChannels oimChannel, Session dbSession)
      throws ChannelConfigurationException {
    m_dbSession = dbSession;
    m_channel = oimChannel;

    log.info("Initializing Channel : {}", m_channel.getChannelName());
    Query query = m_dbSession.createQuery(
        "select opr from salesmachine.hibernatedb.OimOrderProcessingRule opr where opr.deleteTm is null and opr.oimChannels=:chan");
    query.setEntity("chan", m_channel);
    Iterator iter = query.iterate();
    if (iter.hasNext()) {
      m_orderProcessingRule = (OimOrderProcessingRule) iter.next();
    } else {
      throw new ChannelConfigurationException(
          "No associated order processing rule found  with : " + m_channel.getChannelName());
    }
    Set suppliers = m_channel.getOimChannelSupplierMaps();
    supplierMap = new HashMap<OimSuppliers, String>();
    Iterator itr = suppliers.iterator();
    while (itr.hasNext()) {
      OimChannelSupplierMap map = (OimChannelSupplierMap) itr.next();
      if (map.getDeleteTm() != null)
        continue;

      String prefix = map.getSupplierPrefix();
      OimSuppliers supplier = map.getOimSuppliers();
      log.info("Supplier Prefix: {} ID: {}", prefix, supplier.getSupplierId());
      supplierMap.put(supplier, prefix);
    }

    Criteria findCriteria = m_dbSession.createCriteria(OimChannelShippingMap.class);
    findCriteria.add(Restrictions.eq("oimSupportedChannel", m_channel.getOimSupportedChannels()));
    findCriteria.add(Restrictions.or(Restrictions.eq("oimChannel", m_channel),Restrictions.isNull("oimChannel")));

    oimChannelShippingMapList = findCriteria.list();
    return true;
  }

  @Deprecated
  protected List<String> getCurrentOrders() {
    List<String> orders = new ArrayList<String>();

    Query query = m_dbSession.createQuery(
        "select o from salesmachine.hibernatedb.OimOrders o where o.oimOrderBatches.oimChannels=:chan");
    query.setEntity("chan", m_channel);
    Iterator iter = query.iterate();
    while (iter.hasNext()) {
      OimOrders o = (OimOrders) iter.next();
      orders.add(o.getStoreOrderId());
    }
    return orders;
  }

  protected boolean orderAlreadyImported(String storeOrderId) {
    Query query = m_dbSession.createQuery(
        "select o from salesmachine.hibernatedb.OimOrders o where o.oimOrderBatches.oimChannels=:chan and o.storeOrderId=:storeOrderId");
    query.setEntity("chan", m_channel);
    query.setString("storeOrderId", storeOrderId);
    int rowCount = query.list().size();
    return rowCount > 0;

  }

  protected String validateAndGetStateCode(OimOrders order) {
    log.info("Getting state code for - {}", order.getDeliveryState());
    String stateCode = StateCodeProperty.getProperty(order.getDeliveryState());
    stateCode = StringHandle.removeNull(stateCode);
    log.info("state code for {} is {}", order.getDeliveryState(), stateCode);
    return stateCode;
  }

  public static String validateAndGetCountryCode(OimOrders order) {
    log.info("Getting Country code for - {}", order.getDeliveryCountry());
    String countryCode = CountryCodeProperty.getProperty(order.getDeliveryCountry());
    countryCode = StringHandle.removeNull(countryCode);
    log.info("Country code for {} is {}", order.getDeliveryCountry(), countryCode);
    return countryCode;
  }
  protected String getStringFromStream(InputStream is) throws IOException {
    StringBuffer streamBuffer = new StringBuffer();
    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    String inputLine;
    while ((inputLine = reader.readLine()) != null) {
      streamBuffer.append(inputLine + '\n');
    }
    reader.close();
    return streamBuffer.toString();
  }

}
