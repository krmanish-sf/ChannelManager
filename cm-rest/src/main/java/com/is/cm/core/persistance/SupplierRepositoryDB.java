package com.is.cm.core.persistance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.JoinType;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimShippingCarrier;
import salesmachine.hibernatedb.OimSupplierMethodNames;
import salesmachine.hibernatedb.OimSupplierMethodTypes;
import salesmachine.hibernatedb.OimSupplierMethodattrNames;
import salesmachine.hibernatedb.OimSupplierMethodattrValues;
import salesmachine.hibernatedb.OimSupplierMethods;
import salesmachine.hibernatedb.OimSupplierShippingMethod;
import salesmachine.hibernatedb.OimSupplierShippingMethods;
import salesmachine.hibernatedb.OimSupplierShippingOverride;
import salesmachine.hibernatedb.OimSuppliers;
import salesmachine.hibernatedb.OimVendorShippingMap;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.util.StringHandle;

import com.is.cm.core.domain.ShippingCarrier;
import com.is.cm.core.domain.Supplier;
import com.is.cm.core.domain.SupplierShippingMethod;
import com.is.cm.core.domain.VendorShippingMap;
import com.is.cm.core.domain.VendorSupplier;

public class SupplierRepositoryDB extends RepositoryBase implements
		SupplierRepository {
	private static final Logger LOG = LoggerFactory
			.getLogger(SupplierRepositoryDB.class);
	private Map<String, String> map;

	@Override
	public List<VendorSupplier> findAll() {
		LOG.info("Fetching suppliers for vendorId# {}", getVendorId());
		Session dbSession = SessionManager.currentSession();
		List<VendorSupplier> suppliers = new ArrayList<VendorSupplier>();
		Transaction tx = null;
		try {
			tx = dbSession.beginTransaction();
			Criteria fetchCriteria = dbSession
					.createCriteria(OimVendorSuppliers.class);
			fetchCriteria
					.add(Restrictions.eq("vendors.vendorId", getVendorId()))
					.add(Restrictions.isNull("deleteTm"))
					.createCriteria("oimSuppliers.oimSupplierMethodses",
							"methods", JoinType.INNER.ordinal())
					// .addOrder(Order.asc("supplierMethodId"))
					.createCriteria("methods.oimSupplierMethodNames",
							JoinType.INNER.ordinal())
					.createCriteria("methods.oimSupplierMethodattrValueses",
							JoinType.INNER.ordinal());

			List<OimVendorSuppliers> vendorSuppliers = fetchCriteria.list();
			VendorSupplier vendorSupplier;
			for (OimVendorSuppliers s : vendorSuppliers) {
				LOG.info("Supplier {} has {} method(s)", s.getOimSuppliers()
						.getSupplierName(), s.getOimSuppliers()
						.getOimSupplierMethodses().size());

				Set<OimSupplierMethods> oimSupplierMethodses = s
						.getOimSuppliers().getOimSupplierMethodses();
				if (oimSupplierMethodses.size() <= 0) {
					List<OimSupplierMethods> list = dbSession
							.createCriteria(OimSupplierMethods.class, "methods")
							.add(Restrictions.eq(
									"methods.oimSuppliers.supplierId", s
											.getOimSuppliers().getSupplierId()))
							// .addOrder(Order.asc("supplierMethodId"))
							.createCriteria("methods.oimSupplierMethodNames",
									JoinType.INNER.ordinal())
							.createCriteria(
									"methods.oimSupplierMethodattrValueses",
									JoinType.INNER.ordinal())
							.createCriteria(
									"methods.oimSupplierMethodattrValueses.oimSupplierMethodattrNames",
									JoinType.INNER.ordinal()).list();
					oimSupplierMethodses.addAll(list);
					Collections.sort(list,
							new Comparator<OimSupplierMethods>() {

								@Override
								public int compare(OimSupplierMethods o1,
										OimSupplierMethods o2) {
									return o1
											.getOimSupplierMethodNames()
											.getMethodNameId()
											.compareTo(
													o2.getOimSupplierMethodNames()
															.getMethodNameId());
								}
							});
					LOG.info("Now Supplier {} has {} method(s)", s
							.getOimSuppliers().getSupplierName(), s
							.getOimSuppliers().getOimSupplierMethodses().size());

				}

				for (OimSupplierMethods oimSupplierMethods : oimSupplierMethodses) {
					// oimSupplierMethods.setDeleteTm(new Date());
					Set<OimSupplierMethodattrValues> oimSupplierMethodattrValueses = oimSupplierMethods
							.getOimSupplierMethodattrValueses();
					LOG.info("Reading "
							+ oimSupplierMethods.getOimSupplierMethodNames()
									.getMethodName()
							+ ":"
							+ oimSupplierMethods.getOimSupplierMethodTypes()
									.getMethodTypeName());
					LOG.info("This Supplier method has {} attribute values",
							oimSupplierMethodattrValueses.size());
					if (oimSupplierMethodattrValueses.size() == 0) {
						oimSupplierMethodattrValueses
								.addAll(dbSession
										.createCriteria(
												OimSupplierMethodattrValues.class)
										.add(Restrictions
												.eq("oimSupplierMethods.supplierMethodId",
														oimSupplierMethods
																.getSupplierMethodId()))
										.list());
						LOG.info(
								"Now this Supplier method has {} attribute values",
								oimSupplierMethodattrValueses.size());
					}
					for (OimSupplierMethodattrValues oimSupplierMethodattrValues : oimSupplierMethodattrValueses) {
						// oimSupplierMethodattrValues.setDeleteTm(new Date());
						LOG.info("Reading "
								+ oimSupplierMethodattrValues
										.getOimSupplierMethodattrNames()
										.getAttrName()
								+ ":"
								+ oimSupplierMethodattrValues
										.getAttributeValue());
					}

				}

				vendorSupplier = VendorSupplier.from(s);
				suppliers.add(vendorSupplier);
			}
			tx.commit();
			return suppliers;
		} catch (HibernateException exception) {
			if (tx != null && tx.isActive())
				tx.rollback();
			LOG.error("Error in fetching Vendor Suppliers", exception);
		}
		return null;
	}

	@Override
	public VendorSupplier delete(int id) {
		Session dbSession = SessionManager.currentSession();
		Transaction tx = null;
		VendorSupplier vendorSupplier = null;
		try {
			tx = dbSession.beginTransaction();
			vendorSupplier = (VendorSupplier) dbSession.get(
					VendorSupplier.class, id);
			vendorSupplier.setDeleteTm(new Date());
			dbSession.update(vendorSupplier);
			tx.commit();
			LOG.info("Deleted VendorSupplier with AccountNumber# {}",
					vendorSupplier.getAccountNumber());
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			LOG.error("Error ocured in deleting", e);
		}
		return vendorSupplier;
	}

	@Override
	public VendorSupplier findById(int id) {
		Session dbSession = SessionManager.currentSession();
		return (VendorSupplier) dbSession.get(VendorSupplier.class, id);
	}

	@Override
	public VendorSupplier update(int vendorSupplierId,
			Map<String, String> vendorSupplierMap) {
		this.map = vendorSupplierMap;
		OimVendorSuppliers oimVendorSuppliers = null;
		String accountno = getParameter("accountno");
		String login = getParameter("login");
		String password = getParameter("password");
		String defShippingMc = getParameter("defshippingmc");
		Integer testMode = Integer.parseInt(getParameter("testmode"));

		String supplierName = getParameter("name");
		boolean emailOrders = true;// "on".equals(getParameter("emailorders"));
		String supplierEmail = getParameter("supplieremail");
		String fileFormat = getParameter("customSupplierFileFormat");

		Session dbSession = SessionManager.currentSession();
		Transaction tx = null;
		try {
			tx = dbSession.beginTransaction();
			oimVendorSuppliers = (OimVendorSuppliers) dbSession.get(
					OimVendorSuppliers.class, vendorSupplierId);
			oimVendorSuppliers.setAccountNumber(accountno);
			oimVendorSuppliers.setDefShippingMethodCode(defShippingMc);
			oimVendorSuppliers.setLogin(login);
			oimVendorSuppliers.setPassword(password);
			oimVendorSuppliers.setTestMode(testMode);
			dbSession.update(oimVendorSuppliers);
			dbSession.flush();
			OimSuppliers oimSuppliers = oimVendorSuppliers.getOimSuppliers();
			if (oimSuppliers.getIsCustom() != null
					&& oimSuppliers.getIsCustom().equals(1)) {
				Set<OimSupplierMethods> oimSupplierMethodses = oimSuppliers
						.getOimSupplierMethodses();
				for (OimSupplierMethods oimSupplierMethods : oimSupplierMethodses) {
					Set<OimSupplierMethodattrValues> oimSupplierMethodattrValueses = oimSupplierMethods
							.getOimSupplierMethodattrValueses();
					for (OimSupplierMethodattrValues oimSupplierMethodattrValues : oimSupplierMethodattrValueses) {
						// oimSupplierMethodattrValues.setDeleteTm(new Date());
						if (OimConstants.SUPPLIER_METHOD_ATTRIBUTES_EMAILADDRESS
								.equals(oimSupplierMethodattrValues
										.getOimSupplierMethodattrNames()
										.getAttrId())) {
							oimSupplierMethodattrValues
									.setAttributeValue(supplierEmail);
							dbSession.update(oimSupplierMethodattrValues);
						} else if (OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FILEFORMAT
								.equals(oimSupplierMethodattrValues
										.getOimSupplierMethodattrNames()
										.getAttrId())) {
							oimSupplierMethodattrValues
									.setAttributeValue(fileFormat);
							dbSession.update(oimSupplierMethodattrValues);
						}
					}
				}
			}
			VendorSupplier vs = VendorSupplier.from(oimVendorSuppliers);
			dbSession.flush();
			tx.commit();
			dbSession.evict(oimVendorSuppliers);
			dbSession.evict(oimSuppliers);
			return vs;
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			LOG.error("Error ocured in update", e);
		}
		return null;
	}

	@Override
	public VendorSupplier removeSubscription(Integer vendorSupplierId) {
		Transaction tx = null;
		try {
			Session dbSession = SessionManager.currentSession();
			tx = dbSession.beginTransaction();
			OimVendorSuppliers ovs = (OimVendorSuppliers) dbSession.get(
					OimVendorSuppliers.class, vendorSupplierId);
			ovs.setDeleteTm(new Date());
			dbSession.update(ovs);
			tx.commit();
			dbSession.evict(ovs);
			return VendorSupplier.from(ovs);
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
		}
		return null;
	}

	@Override
	public List<Supplier> findUnsubscribed() {
		Session dbSession = SessionManager.currentSession();
		try {
			Criteria createCriteria = dbSession
					.createCriteria(OimSuppliers.class)
					.add(Restrictions.eq("isCustom", new Integer(0)))
					.add(Restrictions.isNull("deleteTm"))
					.addOrder(Order.asc("supplierName"))
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			// .setProjection(Projections.groupProperty("supplierName"))
			// .createCriteria("oimVendorSupplierses")
			// .add(Restrictions.isNull("vendors.vendorId"));
			List<OimSuppliers> suppliers = createCriteria.list();
			List<Supplier> supplier = new ArrayList<Supplier>();
			for (OimSuppliers s : suppliers) {
				supplier.add(Supplier.from(s));
			}
			return supplier;
		} catch (HibernateException exception) {
			LOG.error("Error in fetching Suppliers", exception);
		}
		return null;
	}

	@Override
	public VendorSupplier addSubscription(Integer supplierId, String login,
			String password, String accountno, String defShippingMc,
			Integer testmode) {
		OimSuppliers supplier = new OimSuppliers();
		supplier.setSupplierId(supplierId);
		Session dbSession = SessionManager.currentSession();
		Transaction tx = dbSession.beginTransaction();
		OimVendorSuppliers ovs = new OimVendorSuppliers();
		ovs.setVendors(new Vendors(getVendorId()));
		ovs.setOimSuppliers(supplier);
		ovs.setInsertionTm(new Date());
		ovs.setAccountNumber(accountno);
		ovs.setDefShippingMethodCode(defShippingMc);
		ovs.setLogin(login);
		ovs.setPassword(password);
		ovs.setTestMode(testmode);
		dbSession.save(ovs);
		tx.commit();
		dbSession.evict(supplier);
		dbSession.evict(ovs);
		return VendorSupplier.from(ovs);
	}

	@Override
	public VendorSupplier addCustomSubscription(Map<String, String> map) {
		this.map = map;
		Session dbSession = SessionManager.currentSession();
		String accountno = getParameter("accountno");
		String login = getParameter("login");
		String password = getParameter("password");
		String defShippingMc = getParameter("defshippingmc");

		String supplierName = getParameter("name");
		boolean emailOrders = true;// "on".equals(getParameter("emailorders"));
		String supplierEmail = getParameter("supplieremail");
		String fileFormat = getParameter("customSupplierFileFormat");
		Transaction tx = null;
		try {
			tx = dbSession.beginTransaction();
			OimSuppliers os = addCustomSupplier(dbSession, supplierName,
					emailOrders, supplierEmail, getVendorId(), fileFormat);
			Integer supplierId = os.getSupplierId();
			OimVendorSuppliers oimVendorSuppliers = addSubscription(dbSession,
					supplierId, login, password, accountno, defShippingMc,
					getVendorId());
			dbSession.flush();
			tx.commit();
			dbSession.evict(oimVendorSuppliers);
			dbSession.evict(os);
			oimVendorSuppliers = (OimVendorSuppliers) dbSession.get(
					OimVendorSuppliers.class,
					oimVendorSuppliers.getVendorSupplierId());
			return VendorSupplier.from(oimVendorSuppliers);
		} catch (HibernateException e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			LOG.error("Error occured in saving subscription", e);
		}
		return null;
	}

	private OimVendorSuppliers addSubscription(Session dbSession,
			Integer supplierId, String login, String password,
			String accountno, String defShippingMc, Integer vendorId) {
		return addSubscription(dbSession, supplierId, login, password,
				accountno, defShippingMc, vendorId, 1);
	}

	private OimVendorSuppliers addSubscription(Session dbSession,
			Integer supplierId, String login, String password,
			String accountno, String defShippingMc, Integer vendorId,
			Integer testmode) {
		OimSuppliers supplier = new OimSuppliers();
		supplier.setSupplierId(supplierId);
		OimVendorSuppliers ovs = new OimVendorSuppliers();
		ovs.setVendors(new Vendors(vendorId));
		ovs.setOimSuppliers(supplier);
		ovs.setInsertionTm(new Date());
		ovs.setAccountNumber(accountno);
		ovs.setDefShippingMethodCode(defShippingMc);
		ovs.setLogin(login);
		ovs.setPassword(password);
		ovs.setTestMode(testmode);
		dbSession.save(ovs);
		return ovs;
	}

	public OimSuppliers addCustomSupplier(Session dbSession,
			String supplierName, boolean emailOrders, String supplierEmail,
			Integer vendorId, String fileFormat) {
		OimSuppliers os = new OimSuppliers();
		os.setInsertionTm(new Date());
		os.setIsCustom(1);
		os.setSupplierName(supplierName);
		os.setDescription("Custom Supplier for Vendor Id: " + vendorId);
		dbSession.save(os);
		dbSession.flush();
		// if (true || emailOrders) { // Always configure email push
		addCustomSupplierMethods(dbSession, os, supplierEmail, fileFormat);
		// }
		return os;
	}

	private String getParameter(String key) {
		return map.get(key);
	}

	private void addCustomSupplierMethods(Session dbSession, OimSuppliers os,
			String supplierEmail, String newFileFormat) {
		OimSupplierMethods osm = new OimSupplierMethods();
		OimSupplierMethodNames osmn = (OimSupplierMethodNames) dbSession.get(
				OimSupplierMethodNames.class,
				OimConstants.SUPPLIER_METHOD_NAME_EMAIL);
		osm.setInsertionTm(new Date());
		osm.setOimSupplierMethodNames(osmn);
		OimSupplierMethodTypes osmt = (OimSupplierMethodTypes) dbSession.get(
				OimSupplierMethodTypes.class,
				OimConstants.SUPPLIER_METHOD_TYPE_ORDERPUSH);
		osm.setOimSupplierMethodTypes(osmt);
		osm.setOimSuppliers(os);
		dbSession.save(osm);

		supplierEmail = StringHandle.removeNull(supplierEmail);
		OimSupplierMethodattrNames osmanemail = (OimSupplierMethodattrNames) dbSession
				.get(OimSupplierMethodattrNames.class,
						OimConstants.SUPPLIER_METHOD_ATTRIBUTES_EMAILADDRESS);
		OimSupplierMethodattrValues osmv = new OimSupplierMethodattrValues();
		osmv.setInsertionTm(new Date());
		osmv.setOimSupplierMethods(osm);
		osmv.setAttributeValue(supplierEmail);
		osmv.setOimSupplierMethodattrNames(osmanemail);
		dbSession.save(osmv);

		newFileFormat = StringHandle.removeNull(newFileFormat);
		if (newFileFormat.length() == 0)
			newFileFormat = "1";
		OimSupplierMethodattrNames osmanff = (OimSupplierMethodattrNames) dbSession
				.get(OimSupplierMethodattrNames.class,
						OimConstants.SUPPLIER_METHOD_ATTRIBUTES_FILEFORMAT);
		OimSupplierMethodattrValues osmvff = new OimSupplierMethodattrValues();
		osmvff.setInsertionTm(new Date());
		osmvff.setOimSupplierMethods(osm);
		osmvff.setAttributeValue(newFileFormat);
		osmvff.setOimSupplierMethodattrNames(osmanff);
		dbSession.save(osmvff);
		dbSession.flush();
	}

	@Override
	public void editShippingMethodMapping(Map<String, String> map) {
		this.map = map;
		Session dbSession = SessionManager.currentSession();
		Integer supplierId = Integer.valueOf(getParameter("supplierId"));
		Transaction tx = null;
		try {
			tx = dbSession.beginTransaction();
			Query query = dbSession
					.createQuery("delete from salesmachine.hibernatedb.OimVendorShippingMap where oimSuppliers.supplierId="
							+ supplierId
							+ " and vendors.vendorId="
							+ getVendorId());
			LOG.debug("Deleted {} OimVendorShippingMap rows",
					query.executeUpdate());

			OimSuppliers s = new OimSuppliers();
			s.setSupplierId(supplierId);
			Vendors v = new Vendors();
			v.setVendorId(getVendorId());
			for (Iterator it = map.keySet().iterator(); it.hasNext();) {
				String paramName = (String) it.next();
				if (paramName.startsWith("shipping_text")) {
					String shippingText = getParameter(paramName);
					String index = paramName
							.substring("shipping_text".length());
					try {
						String strShippingMethodId = StringHandle
								.removeNull(getParameter("shipping_method_id"
										+ index));
						String strShippingMethodText = StringHandle
								.removeNull(getParameter("shipping_method_text"
										+ index));
						if (strShippingMethodId.length() > 0) {
							Integer shipping_method_id = Integer
									.parseInt(strShippingMethodId);
							LOG.debug("{}:{}", shippingText, shipping_method_id);

							OimSupplierShippingMethods osm = new OimSupplierShippingMethods();
							osm.setId(shipping_method_id);
							OimVendorShippingMap ovsm = new OimVendorShippingMap();
							ovsm.setOimShippingMethod(osm);
							ovsm.setOimSuppliers(s);
							ovsm.setShippingText(shippingText);
							ovsm.setVendors(v);
							dbSession.save(ovsm);
						} else if (strShippingMethodText.length() > 0) {
							// Custom shipping
							OimSupplierShippingMethods osm = addShippingMethod(
									dbSession, s.getSupplierId(),
									strShippingMethodText,
									strShippingMethodText, v.getVendorId());
							OimVendorShippingMap ovsm = new OimVendorShippingMap();
							ovsm.setOimShippingMethod(osm);
							ovsm.setOimSuppliers(s);
							ovsm.setShippingText(shippingText);
							ovsm.setVendors(v);
							dbSession.save(ovsm);
						}

					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
					}
				}
			}

			tx.commit();
		} catch (Exception e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			LOG.debug(e.getMessage(), e);
		}
	}

	public static OimSupplierShippingMethods addShippingMethod(
			Session dbSession, Integer supplierId, String shippingCode,
			String shippingName, Integer vendor_id) {
		OimSuppliers s = new OimSuppliers();
		s.setSupplierId(supplierId);

		Query query = dbSession
				.createQuery("from salesmachine.hibernatedb.OimSupplierShippingMethods c "
						+ "where c.oimSuppliers=:supp and c.shippingCode=:code");
		query.setEntity("supp", s);
		query.setString("code", shippingCode);
		List methods = query.list();
		if (methods.size() > 0) {
			System.out
					.println("Found "
							+ methods.size()
							+ " existing shipping methods with the same code for this supplier");
			OimSupplierShippingMethods osm = (OimSupplierShippingMethods) methods
					.get(0);
			return osm;
		}

		OimSupplierShippingMethods osm = new OimSupplierShippingMethods();
		osm.setOimSuppliers(s);
		osm.setShippingCode(shippingCode);
		osm.setShippingName(shippingName);
		osm.setSpecificVendorId(vendor_id);
		dbSession.save(osm);
		return osm;
	}

	@Override
	public List<SupplierShippingMethod> findSupplierShippingMapping(
			int supplierId) {
		Session dbSession = SessionManager.currentSession();
		try {
			Criteria findShippingMapping = dbSession
					.createCriteria(OimSupplierShippingMethod.class);
			findShippingMapping.add(Restrictions.eq("oimSupplier.supplierId",
					supplierId));
			List<OimSupplierShippingMethod> list = findShippingMapping.list();
			List<SupplierShippingMethod> retList = new ArrayList<SupplierShippingMethod>();
			for (OimSupplierShippingMethod method : list) {
				retList.add(SupplierShippingMethod.from(method));
			}
			return retList;
		} catch (HibernateException e) {
			LOG.error("Error occured : ", e);
		}
		return null;
	}

	@Override
	public List<VendorShippingMap> findVendorShippingMapping(int supplierId) {

		Session dbSession = SessionManager.currentSession();
		try {
			Query query = dbSession
					.createQuery("from salesmachine.hibernatedb.OimVendorShippingMap c "
							+ "where c.oimSuppliers.supplierId=:supplierId and c.vendors.vendorId=:vendorId");
			query.setInteger("supplierId", supplierId);
			query.setInteger("vendorId", getVendorId());
			List<OimVendorShippingMap> methods = query.list();
			List<VendorShippingMap> vendorShippingMaps = new ArrayList<VendorShippingMap>();
			for (OimVendorShippingMap map : methods) {
				vendorShippingMaps.add(VendorShippingMap.from(map));
			}
			return vendorShippingMaps;
		} catch (HibernateException e) {
			LOG.error("Error occured : ", e);
		}
		return null;
	}

	@Override
	public List<ShippingCarrier> findVendorShippingCarrier(Integer entity) {
		Session dbSession = SessionManager.currentSession();
		List<OimShippingCarrier> list = dbSession.createCriteria(
				OimShippingCarrier.class).list();
		List<ShippingCarrier> shippingCarriers = new ArrayList<ShippingCarrier>();
		for (OimShippingCarrier oimShippingCarrier : list) {
			shippingCarriers.add(ShippingCarrier.from(oimShippingCarrier));
		}
		return shippingCarriers;
	}

	@Override
	public List<SupplierShippingMethod> findChannelShippingForSupplierCarrier(
			Integer supplierId, Integer carrierId) {
		Session dbSession = SessionManager.currentSession();
		List<OimSupplierShippingMethod> list = dbSession
				.createCriteria(OimSupplierShippingMethod.class)
				.add(Restrictions.eq("oimSupplier.supplierId", supplierId))
				.add(Restrictions.eq("oimShippingCarrier.id", carrierId))
				.list();
		List<SupplierShippingMethod> supplierShippingMethodList = new ArrayList<SupplierShippingMethod>();
		for (OimSupplierShippingMethod oimSupplierShippingMethod : list) {
			OimSupplierShippingOverride override = (OimSupplierShippingOverride) dbSession
					.createCriteria(OimSupplierShippingOverride.class)
					.add(Restrictions.eq("oimSupplierShippingMethod",
							oimSupplierShippingMethod.getId())).uniqueResult();
			if (override != null)
				oimSupplierShippingMethod.setOverride(override);
			supplierShippingMethodList.add(SupplierShippingMethod
					.from(oimSupplierShippingMethod));
		}
		return supplierShippingMethodList;
	}

	@Override
	public void deleteShippingOverrideForSupplierMethod(int id) {
		Session dbSession = SessionManager.currentSession();
		Transaction tx = null;
		try {
			tx = dbSession.beginTransaction();
			OimSupplierShippingOverride result = (OimSupplierShippingOverride) dbSession
					.createCriteria(OimSupplierShippingOverride.class)
					.add(Restrictions.eq("oimSupplierShippingMethod", id))
					.uniqueResult();
			if (result != null)
				dbSession.delete(result);
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null && tx.isActive())
				tx.rollback();
		}
	}

	@Override
	public List<SupplierShippingMethod> saveShippingOverrideForSupplierMethod(
			Integer integer, String string) {
		Session dbSession = SessionManager.currentSession();
		Transaction tx = null;
		OimSupplierShippingOverride override = null;
		try {
			override = new OimSupplierShippingOverride();
			override.setShippingMethod(string);
			override.setOimSupplierShippingMethod(integer);
			tx = dbSession.beginTransaction();
			dbSession.save(override);
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			return null;
		}
		return null;
	}
}
