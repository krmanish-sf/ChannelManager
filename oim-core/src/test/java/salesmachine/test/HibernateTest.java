package salesmachine.test;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.is.cm.core.domain.OrderDetailMod;
import com.is.cm.core.persistance.GenericHibernateDao;
import com.is.cm.core.persistance.IGenericDao;
import com.is.cm.core.persistance.OrderRepository;
import com.is.cm.core.persistance.OrderRepositoryDB;

import junit.framework.Assert;
import salesmachine.hibernatedb.OimAutomationAudit;
import salesmachine.hibernatedb.OimChannelShippingMap;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimShippingCarrier;
import salesmachine.hibernatedb.OimShippingMethod;
import salesmachine.hibernatedb.OimSupplierShippingOverride;
import salesmachine.hibernatehelper.SessionManager;

public class HibernateTest extends AbstractIntegrationTest {

  private static final Logger log = LoggerFactory.getLogger(HibernateTest.class);

  @Test
  public void selectExamples() {
    Session session = SessionManager.currentSession();
    Transaction tx = session.beginTransaction();
    Query query = session.createQuery("from salesmachine.hibernatedb.OimShippingCarrier c");
    Query query1 = session.createQuery("from salesmachine.hibernatedb.OimShippingMethod");
    Query query2 = session.createQuery("from salesmachine.hibernatedb.OimSupplierShippingMethod");
    Query query3 = session.createQuery("from salesmachine.hibernatedb.OimChannelShippingMap");
    // ChannelRepository repo = new ChannelRepositoryDB();
    Assert.assertEquals(query.list().size() >= 1, true);
    Assert.assertEquals(query1.list().size() >= 1, true);
    Assert.assertEquals(query2.list().size() >= 1, true);
    Assert.assertEquals(query3.list().size() >= 1, true);
    Object object = session.get(OimShippingCarrier.class, 1);
    Object shipping_method = session.get(OimShippingMethod.class, 1);
    Assert.assertNotNull(object);
    Assert.assertNotNull(shipping_method);
    tx.commit();
    SessionManager.closeSession();
  }

  @Test
  public void testChannelShippingMapping() {
    Session session = SessionManager.currentSession();
    Transaction tx = session.beginTransaction();
    Criteria findCriteria = session.createCriteria(OimChannelShippingMap.class);
    findCriteria.add(Restrictions.eq("oimSupportedChannel.supportedChannelId", 6));
    List<OimChannelShippingMap> list = findCriteria.list();
    Assert.assertNotNull(list);
    tx.commit();
    SessionManager.closeSession();
  }

  @Test
  public void testChannelShippingMappingOverride() {
    Session session = SessionManager.currentSession();
    Transaction tx = session.beginTransaction();
    Criteria findCriteria = session.createCriteria(OimSupplierShippingOverride.class);

    List list = findCriteria.list();
    Assert.assertTrue(list.size() >= 0);
    tx.commit();
    SessionManager.closeSession();
  }

  // @Test
  public void testOrderDetailMods() {
    OrderRepository db = new OrderRepositoryDB();
    List<OrderDetailMod> findOrderDetailModifications = db.findOrderDetailModifications(8963830);
    Assert.assertNotNull(findOrderDetailModifications);
    Assert.assertTrue(findOrderDetailModifications.size() > 0);
  }

  // @Test
  public void testIf_OimAutomationAudit_Pesrists() {
    Session session = SessionManager.currentSession();
    Transaction tx = session.beginTransaction();
    OimAutomationAudit audit = new OimAutomationAudit();
    audit.setStartTime(new Date());
    audit.setEndTime(new Date());
    audit.setChannelID(123123);
    audit.setTotalOrderPull(10);
    audit.setTotalOrderTracked(10);
    session.save(audit);
    tx.commit();

  }

  //@Test
  public void testGenericHibernateDao() {

    IGenericDao<OimOrderDetails> dao = new GenericHibernateDao<>();
    dao.findOne(9000321);
  }

}
