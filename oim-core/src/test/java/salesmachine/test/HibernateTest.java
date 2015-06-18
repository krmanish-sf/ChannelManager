package salesmachine.test;

import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimChannelShippingMap;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimShippingCarrier;
import salesmachine.hibernatedb.OimShippingMethod;
import salesmachine.hibernatedb.OimSupplierShippingOverride;
import salesmachine.hibernatehelper.SessionManager;

public class HibernateTest {

	private static final Logger log = LoggerFactory
			.getLogger(HibernateTest.class);

	@Test
	public void selectExamples() {
		Session session = SessionManager.currentSession();
		Transaction tx = session.beginTransaction();
		Query query = session
				.createQuery("from salesmachine.hibernatedb.OimShippingCarrier c");
		Query query1 = session
				.createQuery("from salesmachine.hibernatedb.OimShippingMethod");
		Query query2 = session
				.createQuery("from salesmachine.hibernatedb.OimSupplierShippingMethod");
		Query query3 = session
				.createQuery("from salesmachine.hibernatedb.OimChannelShippingMap");
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
		Criteria findCriteria = session
				.createCriteria(OimChannelShippingMap.class);
		findCriteria.add(Restrictions.eq(
				"oimSupportedChannel.supportedChannelId", 6));
		List<OimChannelShippingMap> list = findCriteria.list();
		Assert.assertNotNull(list);
		tx.commit();
		SessionManager.closeSession();
	}

	@Test
	public void testChannelShippingMappingOverride() {
		Session session = SessionManager.currentSession();
		Transaction tx = session.beginTransaction();
		Criteria findCriteria = session
				.createCriteria(OimSupplierShippingOverride.class);

		List list = findCriteria.list();
		Assert.assertTrue(list.size() >= 0);
		tx.commit();
		SessionManager.closeSession();
	}

	@Test
	public void testOrderDateFields() {
		log.debug("Test Start");
		Date deleteTm = new Date();
		Session session = SessionManager.currentSession();
		Transaction tx = session.beginTransaction();
		OimOrders order = (OimOrders) session.get(OimOrders.class, 3035);
		log.debug(deleteTm.toString());
		if (order.getDeleteTm() != null) {
			log.debug(order.getDeleteTm().getClass().getTypeName());
			log.debug(order.getDeleteTm().toString());
		}
		order.setDeleteTm(deleteTm);
		session.saveOrUpdate(order);
		tx.commit();
		session.flush();
		log.debug("Transaction 1 Committed..");
		tx = session.beginTransaction();
		OimOrders order2 = (OimOrders) session.get(OimOrders.class, 3035);
		log.debug(order2.getDeleteTm().toString());
		Assert.assertEquals(deleteTm, order2.getDeleteTm());
		tx.commit();
		log.debug("Transaction 2 Committed..");
		// session.close();
	}
}
