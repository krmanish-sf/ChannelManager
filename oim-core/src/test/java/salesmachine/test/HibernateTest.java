package salesmachine.test;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.junit.Test;

import salesmachine.hibernatedb.OimChannelAccessDetails;
import salesmachine.hibernatedb.OimChannelAccessFields;
import salesmachine.hibernatedb.OimChannelShippingMap;
import salesmachine.hibernatedb.OimChannelSupplierMap;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimFiletypes;
import salesmachine.hibernatedb.OimOrderProcessingRule;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimShippingCarrier;
import salesmachine.hibernatedb.OimShippingMethod;
import salesmachine.hibernatedb.OimSupplierShippingMethods;
import salesmachine.hibernatedb.OimSupplierShippingOverride;
import salesmachine.hibernatedb.OimSuppliers;
import salesmachine.hibernatedb.OimSupportedChannels;
import salesmachine.hibernatedb.OimVendorShippingMap;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.OimVendorsuppOrderhistory;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.PojoHelper;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.suppliers.OimSupplierOrderPlacement;
import salesmachine.oim.suppliers.Supplier;
import salesmachine.util.Filter;

public class HibernateTest {
	private static List<OimChannelAccessDetails> list;

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

	public static void insertUpdateTest(String args[]) {
		Session session = SessionManager.currentSession();
		Transaction tx = session.beginTransaction();
		OimSuppliers s = new OimSuppliers();
		s.setDeleteTm(null);
		s.setDescription("Test Description");
		s.setInsertionTm(new Date());
		s.setIsCustom(new Integer(0));
		s.setSupplierName("Hibernate-Updated");
		s.setSupplierId(new Integer(4));
		// session.save(s);
		session.update(s);
		System.out.println("Supplier saved successfully");
		tx.commit();
		SessionManager.closeSession();
	}

	private static void saveChannelAccessDetail(Session dbSession,
			OimChannels channel, Integer fieldId, String fieldValue) {
		OimChannelAccessDetails d = new OimChannelAccessDetails();
		d.setDetailFieldValue(fieldValue);
		d.setInsertionTm(new Date());
		OimChannelAccessFields field = new OimChannelAccessFields();
		field.setFieldId(fieldId);
		d.setOimChannelAccessFields(field);
		d.setOimChannels(channel);
		dbSession.save(d);
	}

	public static void insertChannel() {
		Session dbSession = SessionManager.currentSession();
		Query query = dbSession
				.createQuery("from salesmachine.hibernatedb.OimVendorSuppliers ovs inner join fetch ovs.oimSuppliers where ovs.vendors.vendorId=:vid and ovs.deleteTm is null");
		List vendorSuppliers = query.setInteger("vid", 79224).list();

		Transaction tx = dbSession.beginTransaction();
		Vendors v = new Vendors(79224);
		OimChannels c = new OimChannels();
		OimSupportedChannels oimSupportedChannels = new OimSupportedChannels();
		oimSupportedChannels.setSupportedChannelId(2);
		c.setVendors(v);
		c.setOimSupportedChannels(oimSupportedChannels);
		c.setChannelName("HTTH");
		c.setEmailNotifications(1);
		c.setEnableOrderAutomation(1);
		c.setInsertionTm(new Date());
		dbSession.save(c);

		saveChannelAccessDetail(dbSession, c,
				OimConstants.CHANNEL_ACCESSDETAIL_CHANNEL_URL, "url");
		saveChannelAccessDetail(dbSession, c,
				OimConstants.CHANNEL_ACCESSDETAIL_FTP_URL, "ftpurl");
		saveChannelAccessDetail(dbSession, c,
				OimConstants.CHANNEL_ACCESSDETAIL_FTP_LOGIN, "login");
		saveChannelAccessDetail(dbSession, c,
				OimConstants.CHANNEL_ACCESSDETAIL_FTP_PWD, "pwd");

		for (int i = 0; i < vendorSuppliers.size(); i++) {
			OimVendorSuppliers ovs = (OimVendorSuppliers) vendorSuppliers
					.get(i);
			OimSuppliers os = ovs.getOimSuppliers();
			String cbFieldName = "sid_" + os.getSupplierId();
			if (1 > 0) {
				String skuPrefix = "MO";
				boolean enableOrderAuto = true;

				OimChannelSupplierMap m = new OimChannelSupplierMap();
				m.setOimChannels(c);
				m.setOimSuppliers(os);
				m.setSupplierPrefix(skuPrefix);
				m.setEnableOrderAutomation(enableOrderAuto ? 1 : 0);
				m.setInsertionTm(new Date());
				dbSession.save(m);
			}
		}

		OimOrderProcessingRule rule = new OimOrderProcessingRule();
		rule.setOimChannels(c);
		rule.setInsertionTm(new Date());
		rule.setUpdateStoreOrderStatus(0);
		rule.setProcessAll(1);
		dbSession.save(rule);
		tx.commit();
		SessionManager.closeSession();
	}

	private static void deleteFiletype(int filetypeId) {
		Session dbSession = SessionManager.currentSession();
		Transaction tx = dbSession.beginTransaction();

		Query query = dbSession
				.createQuery("delete from salesmachine.hibernatedb.OimFileFieldMap m where m.oimFiletypes.fileTypeId=:fileid");
		query.setInteger("fileid", filetypeId);
		int row = query.executeUpdate();
		System.out.println("Deleted " + row + " rows from OimFileFieldMap");

		query = dbSession
				.createQuery("delete from salesmachine.hibernatedb.OimFileformatParams where oimFiletypes.fileTypeId=:fileid");
		query.setInteger("fileid", filetypeId);
		row = query.executeUpdate();
		System.out.println("Deleted " + row + " rows from OimFileFormatParams");

		query = dbSession
				.createQuery("delete from salesmachine.hibernatedb.OimChannelFiles where oimFiletypes.fileTypeId=:fileid");
		query.setInteger("fileid", filetypeId);
		row = query.executeUpdate();
		System.out.println("Deleted " + row + " rows from OimChannelFiles");

		query = dbSession
				.createQuery("delete from salesmachine.hibernatedb.OimFiletypes where fileTypeId=:fileid");
		query.setInteger("fileid", filetypeId);
		row = query.executeUpdate();
		System.out.println("Deleted " + row + " rows from OimFiletypes");

		tx.commit();
		SessionManager.closeSession();
	}

	public static void channelDetails() {
		Session dbSession = SessionManager.currentSession();
		Transaction tx = dbSession.beginTransaction();
		Query q = dbSession
				.createQuery("update salesmachine.hibernatedb.OimChannelAccessDetails set detailFieldValue=:v where accessDetailId=242");
		q.setString("v",
				"http://www.inventorysource-teststore.authsafe.com/GetOrder.php");
		System.out.println("Updated " + q.executeUpdate() + " rows");
		/*
		 * OimChannels c = new OimChannels(); c.setChannelId(102);
		 * saveChannelAccessDetail
		 * (dbSession,c,OimConstants.CHANNEL_ACCESSDETAIL_SCRIPT_PATH
		 * ,"http://staging1.sourcefuse.com/isource/orderpull.php");
		 * saveChannelAccessDetail
		 * (dbSession,c,OimConstants.CHANNEL_ACCESSDETAIL_AUTH_KEY,"02446");
		 */
		tx.commit();
		SessionManager.closeSession();
	}

	private static void unresolvedOrdersCount() {
		Session dbSession = SessionManager.currentSession();
		Query query = dbSession
				.createQuery("select c.channelId, c.channelName ,count(d) from "
						+ "salesmachine.hibernatedb.OimOrderDetails d inner join "
						+ "d.oimOrders.oimOrderBatches.oimChannels c where "
						+ "d.deleteTm is null and "
						+ "d.oimSuppliers is null and "
						+ "c.vendors.vendorId =:vid group by c.channelId, c.channelName");
		query.setInteger("vid", 79224);
		for (Iterator it = query.list().iterator(); it.hasNext();) {
			Object[] row = (Object[]) it.next();
			Integer cId = (Integer) row[0];
			String cName = (String) row[1];
			Long cnt = (Long) row[2];
			System.out.println("Channel: " + cName + "(Id:" + cId
					+ ")\tCount: " + cnt);
		}
		SessionManager.closeSession();
	}

	private static void channelList() {
		Session dbSession = SessionManager.currentSession();
		Query query = dbSession
				.createQuery("select distinct c from salesmachine.hibernatedb.OimChannels c "
						+
						// "inner join fetch c.oimSupportedChannels " +
						// "inner join fetch c.oimOrderProcessingRules r " +
						// "inner join fetch c.oimChannelAccessDetailses d " +
						// "left join fetch c.oimChannelSupplierMaps s " +
						"where c.vendors.vendorId=:vid " +
						// "and r.deleteTm is null " +
						// "and s.deleteTm is null " +
						"and c.deleteTm is null " +
						// "and d.deleteTm is null"
						"");
		List channels = query.setInteger("vid", 79224).list();
		SessionManager.closeSession();
		for (int i = 0; i < channels.size(); i++) {
			OimChannels c = (OimChannels) channels.get(i);
			System.out.println(c.getChannelName());
		}
	}

	private static void channelfiles() {
		// query =
		// dbSession.createQuery("select oimChannels from salesmachine.hibernatedb.OimChannelFiles where oimFiletypes=:oft");
		Session dbSession = SessionManager.currentSession();
		Query query = dbSession
				.createQuery("from salesmachine.hibernatedb.OimFiletypes where fileTypeId=:fileid");
		OimFiletypes oft = (OimFiletypes) query.setInteger("fileid", 102)
				.iterate().next();
		String fieldDelimiter = PojoHelper.getFileFormatParamValue(oft,
				"FIELD_DELIMITER");
		String textDelimiter = PojoHelper.getFileFormatParamValue(oft,
				"TEXT_DELIMITER");
		boolean useHeader = "1".equals(PojoHelper.getFileFormatParamValue(oft,
				"USE_HEADER"));

		query = dbSession
				.createQuery(
						"select r from salesmachine.hibernatedb.OimOrderProcessingRule r, "
								+ "salesmachine.hibernatedb.OimChannelFiles f "
								+ "where r.oimChannels = f.oimChannels and f.oimFiletypes=:oft")
				.setEntity("oft", oft);
		OimOrderProcessingRule rule = (OimOrderProcessingRule) query.iterate()
				.next();
		System.out.println(rule.getRuleId());
	}

	private static void productTest() {
		Session dbSession = SessionManager.currentSession();
		String sku = "CXSST-105P";
		// Doing a fetch on oimOrders as the jsp would need to use that to get
		// the orders information .
		Query query = dbSession
				.createQuery("select p.title, p.descriptionShort from salesmachine.hibernatedb.Product p where upper(p.sku)=:sku");
		Iterator it = query.setString("sku", sku.toUpperCase()).iterate();
		if (it.hasNext()) {
			Object[] row = (Object[]) it.next();
			System.out.println("Fetched Name: " + (String) row[0] + " Desc: "
					+ (String) row[1]);
		} else {
			System.out.println("Could not find product");
		}
	}

	public static void getAlerts() {
		Session dbSession = SessionManager.currentSession();
		Query query = dbSession
				.createQuery("from salesmachine.hibernatedb.OimVendorSuppliers ovs "
						+ "inner join fetch ovs.oimSuppliers where ovs.vendors.vendorId=:v and ovs.deleteTm is null");
		query.setInteger("v", 79224);
		for (Iterator it = query.list().iterator(); it.hasNext();) {
			OimVendorSuppliers s = (OimVendorSuppliers) it.next();
			System.out.println("Processing "
					+ s.getOimSuppliers().getSupplierName());
			query = dbSession
					.createQuery("select ovsh from "
							+ "OimVendorsuppOrderhistory ovsh "
							+ "where ovsh.oimSuppliers=:supp and "
							+ "ovsh.processingTm = "
							+ "(select max(processingTm) from OimVendorsuppOrderhistory "
							+ "where vendors.vendorId=:vid and oimSuppliers=:supp)");
			query.setEntity("supp", s.getOimSuppliers());
			query.setInteger("vid", 79224);

			List results = query.list();
			if (results.size() > 0) {
				OimVendorsuppOrderhistory ovsh = (OimVendorsuppOrderhistory) results
						.get(0);
				if (ovsh.getErrorCode() != OimSupplierOrderPlacement.ERROR_NONE) {
					System.out.println("Supplier: "
							+ s.getOimSuppliers().getSupplierName());
					System.out.println("\tError: " + ovsh.getDescription());
				}
			}
		}
	}

	public static void updateOrders() {
		Session dbSession = SessionManager.currentSession();
		Transaction tx = null;
		try {
			tx = dbSession.beginTransaction();

			// Delete order details
			Query q = dbSession
					.createQuery("select oo from salesmachine.hibernatedb.OimChannels oo where oo.vendors.vendorId = 79224");
			List channels = q.list();
			if (channels.size() > 0) {
				Query q1 = dbSession
						.createQuery("select oo from salesmachine.hibernatedb.OimOrders oo where oo.oimOrderBatches.oimChannels in (:channels)");
				q1.setParameterList("channels", channels);
				List orders = q1.list();
				System.out.println("Number of orders: " + orders.size());
				for (Iterator it = orders.iterator(); it.hasNext();) {
					OimOrders o = (OimOrders) it.next();
					System.out.println("Order: " + o.getOrderId());
				}
				if (orders.size() > 0) {
					Query q2 = dbSession
							.createQuery("update salesmachine.hibernatedb.OimOrderDetails o "
									+ "set o.oimOrderStatuses.statusId="
									+ 0
									+ " where o.oimSuppliers.supplierId = "
									+ 2
									+ " " + "and o.oimOrders in (:orders) ");
					q2.setParameterList("orders", orders);
					int rows = q2.executeUpdate();
					System.out.println("Updated order details. Rows changed: "
							+ rows);
				}
			} else {
				System.out.println("No channels found");
			}

			tx.commit();
		} catch (RuntimeException e) {
			tx.rollback();
			e.printStackTrace();
		}
	}

	public static void addShippingMethod(Integer supplierId,
			String shippingCode, String shippingName) {
		Session dbSession = SessionManager.currentSession();

		Transaction tx = dbSession.beginTransaction();
		OimSuppliers s = new OimSuppliers();
		s.setSupplierId(supplierId);
		Vendors v = new Vendors(79224);
		OimSupplierShippingMethods osm = new OimSupplierShippingMethods();
		osm.setOimSuppliers(s);
		osm.setShippingCode(shippingCode);
		osm.setShippingName(shippingName);
		dbSession.save(osm);
		tx.commit();
		SessionManager.closeSession();
	}

	public static HashMap loadSupplierShippingMap(Integer supplierId) {
		Session dbSession = SessionManager.currentSession();

		Transaction tx = dbSession.beginTransaction();
		OimSuppliers s = new OimSuppliers();
		s.setSupplierId(supplierId);
		Vendors v = new Vendors(79224);
		Query query = dbSession
				.createQuery("from salesmachine.hibernatedb.OimVendorShippingMap c "
						+ "where c.oimSuppliers=:supp and c.vendors=:vendor");
		query.setEntity("supp", s);
		query.setEntity("vendor", v);
		List methods = query.list();

		HashMap shipMap = new HashMap();
		for (Iterator it = methods.iterator(); it.hasNext();) {
			OimVendorShippingMap vsm = (OimVendorShippingMap) it.next();
			String shippingText = vsm.getShippingText();
			String shippingCode = vsm.getOimShippingMethod().getShippingCode();
			shipMap.put(shippingText, shippingCode);

			System.out.println(shippingText + " => " + shippingCode);
		}

		tx.commit();
		SessionManager.closeSession();

		return shipMap;
	}

	public static void testShipMapping() {
		String shippings[] = {
				"Customer Account Fee",
				"Expedited",
				"FedEx Priority to Canada",
				"Standard",
				"UPS",
				"UPS 2nd Day Air",
				"UPS 3 Day Select",
				"UPS Ground",
				"UPS Next Day Air Saver",
				"UPS2",
				"UPSMI",
				"UPSR",
				"US Mail to Continental USA, APO, FPO",
				"US Mail to HI,AK,PR,VI,AS,MP",
				"US Mail to all USA Addresses APO & FPO & Territories",
				"United Parcel Service (1 x 0.36lbs) (Ground):",
				"United Parcel Service (1 x 0.54lbs) (Ground):",
				"United Parcel Service 1 x 0.54lbs) Ground",
				"United States Postal Service (1 x 0.27lbs) (First-Class Mail (Estimated 1 - 5 Days)):",
				"United States Postal Service (1 x 0.28lbs) (First-Class Mail (Estimated 1 - 5 Days)):",
				"United States Postal Service (1 x 0.29lbs) (First-Class Mail (Estimated 1 - 5 Days)):",
				"United States Postal Service (1 x 0.29lbs) (Priority Mail (Estimated 1 - 3 Days)):",
				"United States Postal Service (1 x 0.37lbs) (First-Class Mail (Estimated 1 - 5 Days)):",
				"United States Postal Service (1 x 0.37lbs) (Priority Mail (Estimated 1 - 3 Days)):",
				"United States Postal Service (1 x 0.39lbs) (First-Class Mail (Estimated 1 - 5 Days)):",
				"United States Postal Service (1 x 0.43lbs) (First-Class Mail (Estimated 1 - 5 Days)):",
				"United States Postal Service (1 x 0.45lbs) (First-Class Mail (Estimated 1 - 5 Days)):",
				"United States Postal Service (1 x 0.52lbs) (Priority Mail (Estimated 1 - 3 Days)):",
				"United States Postal Service (1 x 0.53lbs) (First-Class Mail (Estimated 1 - 5 Days)):",
				"United States Postal Service (1 x 0.85lbs) (Priority Mail (Estimated 1 - 3 Days)):" };
		Session dbSession = SessionManager.currentSession();
		OimSuppliers s = new OimSuppliers();
		s.setSupplierId(2);
		Vendors v = new Vendors();
		v.setVendorId(38677);
		HashMap shipMap = Supplier.loadSupplierShippingMap(dbSession, s, v);

		for (int i = 0; i < shippings.length; i++) {
			String code = Supplier.findShippingCodeFromUserMapping(shipMap,
					shippings[i]);
			System.out.println(shippings[i] + " => " + code);
		}
	}

	public static void main(String args[]) {
		try {
			String abc = Filter.transForm("s/$//g", "$33.44");
			String txt = "United Parcel Service (1 x 2.60lbs) (Next Day Air):";

			String re1 = "(United)"; // Variable Name 1
			String re2 = ".*?"; // Non-greedy match on filler
			String re3 = "(Parcel)"; // Variable Name 2
			String re4 = ".*?"; // Non-greedy match on filler
			String re5 = "(Service)"; // Variable Name 3
			String re6 = ".*?"; // Non-greedy match on filler
			String re7 = "(?:[a-z][a-z0-9_]*)"; // Uninteresting: var
			String re8 = ".*?"; // Non-greedy match on filler
			String re9 = "(?:[a-z][a-z0-9_]*)"; // Uninteresting: var
			String re10 = ".*?"; // Non-greedy match on filler
			String re11 = "(Next)"; // Variable Name 4
			String re12 = ".*?"; // Non-greedy match on filler
			String re13 = "(Day)"; // Variable Name 5
			String re14 = ".*?"; // Non-greedy match on filler
			String re15 = "(Air)"; // Variable Name 6

			Pattern p = Pattern.compile(
					"(United Parcel Service).*?(Next Day Air)",
					Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			Matcher m = p.matcher(txt);
			if (m.find()) {
				System.out.println(m.groupCount());
				String var1 = m.group(1);
				String var2 = m.group(2);
				String var3 = m.group(3);
				String var4 = m.group(4);
				String var5 = m.group(5);
				String var6 = m.group(6);
				System.out.print("(" + var1.toString() + ")" + "("
						+ var2.toString() + ")" + "(" + var3.toString() + ")"
						+ "(" + var4.toString() + ")" + "(" + var5.toString()
						+ ")" + "(" + var6.toString() + ")" + "\n");
				System.out.println(txt
						.matches("(United Parcel Service).*?(Next Day Air)"));
				Pattern p1 = Pattern
						.compile("(United Parcel Service).*?(Next Day Air)");
				CharSequence c;
				System.out.println(Pattern.matches(
						"(United Parcel Service).*?(Next Day Air)",
						txt.subSequence(0, txt.length() - 1)));

			}
		} finally {
			System.exit(0);
		}
		// testShipMapping();
		// updateOrders();
		// channelfiles();
		// insertChannel();
		// selectExamples();
		// channelDetails();
		// deleteFiletype(61);
		// unresolvedOrdersCount();
		/*
		 * Session session = SessionManager.currentSession(); Transaction tx =
		 * session.beginTransaction();
		 * 
		 * OimOrderBatches b = new OimOrderBatches(); b.setCreationTm(new
		 * Date()); b.setInsertionTm(new Date()); b.setOimOrderBatchesTypes(new
		 * OimOrderBatchesTypes(new BigDecimal(1)));
		 * 
		 * Set oimOrderses = new HashSet(); b.setOimOrderses(oimOrderses);
		 * //session.save(s); session.save(b);
		 * System.out.println("Supplier saved successfully"); tx.commit();
		 * SessionManager.closeSession();
		 */
	}
}
