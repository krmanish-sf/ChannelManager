package com.is.cm.core.persistance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimChannelAccessDetails;
import salesmachine.hibernatedb.OimChannelAccessFields;
import salesmachine.hibernatedb.OimChannelShippingMap;
import salesmachine.hibernatedb.OimChannelSupplierMap;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimFiletypes;
import salesmachine.hibernatedb.OimOrderProcessingRule;
import salesmachine.hibernatedb.OimSuppliers;
import salesmachine.hibernatedb.OimSupportedChannels;
import salesmachine.hibernatedb.OimUploadedFiles;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.util.StringHandle;

import com.is.cm.core.domain.Channel;
import com.is.cm.core.domain.ChannelShippingMap;
import com.is.cm.core.domain.Filetype;
import com.is.cm.core.domain.SupportedChannel;
import com.is.cm.core.domain.UploadedFile;

public class ChannelRepositoryDb extends RepositoryBase implements
		ChannelRepository {
	private static Logger LOG = LoggerFactory
			.getLogger(ChannelRepositoryDb.class);

	private Map<String, String> channelDetails;

	@Override
	public Channel save(int channelId, Map<String, String> channelDetails) {
		this.channelDetails = channelDetails;
		List vendorSuppliers = null;
		Transaction tx = null;
		try {
			Session dbSession = SessionManager.currentSession();
			tx = dbSession.beginTransaction();
			Vendors v = new Vendors(getVendorId());
			Query query = dbSession
					.createQuery("from salesmachine.hibernatedb.OimVendorSuppliers ovs inner join fetch ovs.oimSuppliers where ovs.vendors.vendorId=:vid and ovs.deleteTm is null");
			vendorSuppliers = query.setInteger("vid", getVendorId()).list();
			OimChannels c = new OimChannels();
			if (channelId > 0) {
				c = (OimChannels) dbSession.get(OimChannels.class, channelId);
				Set<OimChannelAccessDetails> oimChannelAccessDetailses = c
						.getOimChannelAccessDetailses();
				for (OimChannelAccessDetails oimChannelAccessDetails : oimChannelAccessDetailses) {
					oimChannelAccessDetails.setDeleteTm(new Date());
					dbSession.delete(oimChannelAccessDetails);
				}

			} else {
				c = new OimChannels();
			}
			c.setVendors(v);
			updateChannelWithRequest(c, channelDetails);
			dbSession.saveOrUpdate(c);
			LOG.debug("Saved channel");
			if (c.getOimSupportedChannels().getSupportedChannelId() == 4) {
				addChannelAccessDetail(dbSession, c,
						OimConstants.CHANNEL_ACCESSDETAIL_CHANNEL_URL,
						StringHandle.removeNull(getParameter("storeurl")));
				addChannelAccessDetail(dbSession, c,
						OimConstants.CHANNEL_ACCESSDETAIL_AMAZON_SELLERID,
						StringHandle.removeNull(getParameter("mws-seller-id")));
				addChannelAccessDetail(
						dbSession,
						c,
						OimConstants.CHANNEL_ACCESSDETAIL_AMAZON_MWS_AUTH_TOKEN,
						StringHandle.removeNull(getParameter("mws-auth-token")));
				addChannelAccessDetail(
						dbSession,
						c,
						OimConstants.CHANNEL_ACCESSDETAIL_AMAZON_MWS_MARKETPLACE_ID,
						StringHandle
								.removeNull(getParameter("mws-marketplace-id")));
				LOG.debug("Saved amazon channel access details");
			} else if (c.getOimSupportedChannels().getSupportedChannelId() == 5) {
				addChannelAccessDetail(dbSession, c,
						OimConstants.CHANNEL_ACCESSDETAIL_CHANNEL_URL,
						StringHandle.removeNull(getParameter("storeurl")));
				addChannelAccessDetail(dbSession, c,
						OimConstants.CHANNEL_ACCESSDETAIL_YAHOO_STOREID,
						StringHandle.removeNull(getParameter("yahoostoreid")));
				LOG.debug("Saved yahoo channel access details");
			} else if (c.getOimSupportedChannels().getSupportedChannelId() == 7) {
				addChannelAccessDetail(dbSession, c,
						OimConstants.CHANNEL_ACCESSDETAIL_CHANNEL_URL,
						StringHandle.removeNull(getParameter("storeurl")));
				addChannelAccessDetail(dbSession, c,
						OimConstants.CHANNEL_ACCESSDETAIL_SHOP_CATALOGID,
						StringHandle.removeNull(getParameter("catalog-id")));
				LOG.debug("Saved yahoo channel access details");
			} else if (c.getOimSupportedChannels().getSupportedChannelId() != 0) {
				addChannelAccessDetail(dbSession, c,
						OimConstants.CHANNEL_ACCESSDETAIL_CHANNEL_URL,
						StringHandle.removeNull(getParameter("storeurl")));
				addChannelAccessDetail(dbSession, c,
						OimConstants.CHANNEL_ACCESSDETAIL_FTP_URL,
						StringHandle.removeNull(getParameter("ftpurl")));
				addChannelAccessDetail(dbSession, c,
						OimConstants.CHANNEL_ACCESSDETAIL_FTP_LOGIN,
						StringHandle.removeNull(getParameter("ftplogin")));
				addChannelAccessDetail(dbSession, c,
						OimConstants.CHANNEL_ACCESSDETAIL_FTP_PWD,
						StringHandle.removeNull(getParameter("ftppwd")));
				addChannelAccessDetail(dbSession, c,
						OimConstants.CHANNEL_ACCESSDETAIL_SCRIPT_PATH,
						StringHandle.removeNull(getParameter("scriptpath")));
				addChannelAccessDetail(dbSession, c,
						OimConstants.CHANNEL_ACCESSDETAIL_AUTH_KEY,
						StringHandle.removeNull(getParameter("authkey")));
				LOG.debug("Saved channel access details");
			} else {
				// For custom channels add blank channel access details
				addChannelAccessDetail(dbSession, c,
						OimConstants.CHANNEL_ACCESSDETAIL_CHANNEL_URL,
						StringHandle.removeNull(getParameter("storeurl")));
				addChannelAccessDetail(dbSession, c,
						OimConstants.CHANNEL_ACCESSDETAIL_FTP_URL, "");
				addChannelAccessDetail(dbSession, c,
						OimConstants.CHANNEL_ACCESSDETAIL_FTP_LOGIN, "");
				addChannelAccessDetail(dbSession, c,
						OimConstants.CHANNEL_ACCESSDETAIL_FTP_PWD, "");
				addChannelAccessDetail(dbSession, c,
						OimConstants.CHANNEL_ACCESSDETAIL_SCRIPT_PATH, "");
				addChannelAccessDetail(dbSession, c,
						OimConstants.CHANNEL_ACCESSDETAIL_AUTH_KEY, "");
				LOG.debug("Custom channel, saving blank channel access details");
			}
			addChannelSupplierMapWithRequest(dbSession, c, vendorSuppliers,
					channelDetails);
			LOG.debug("Saved channel suppliers");
			addRuleWithRequest(dbSession, c, channelDetails);
			LOG.debug("Saved rule");
			tx.commit();
			dbSession.evict(c);
			return Channel.from(c);
		} catch (Exception e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			LOG.error(e.getMessage(), e);
		}

		return null;
	}

	private void updateChannelWithRequest(OimChannels c,
			Map<String, String> request) {
		String channelName = request.get("channelname");
		boolean emailUpdates = ("on".equals(request.get("emailupdates")));
		int supportedChannelId = Integer.parseInt(request
				.get("supportedChannelId"));
		OimSupportedChannels oimSupportedChannels = new OimSupportedChannels();
		oimSupportedChannels.setSupportedChannelId(supportedChannelId);
		c.setOimSupportedChannels(oimSupportedChannels);
		c.setChannelName(channelName);
		c.setEmailNotifications(emailUpdates ? 1 : 0);
		c.setEnableOrderAutomation(1);
		if (c.getInsertionTm() == null)
			c.setInsertionTm(new Date());
	}

	private void addChannelSupplierMapWithRequest(Session dbSession,
			OimChannels c, List vendorSuppliers, Map<String, String> request) {
		Set<OimChannelSupplierMap> oimChannelSupplierMaps = c
				.getOimChannelSupplierMaps();
		for (OimChannelSupplierMap oimChannelSupplierMap : oimChannelSupplierMaps) {
			dbSession.delete(oimChannelSupplierMap);
		}
		for (int i = 0; i < vendorSuppliers.size(); i++) {
			OimVendorSuppliers ovs = (OimVendorSuppliers) vendorSuppliers
					.get(i);
			OimSuppliers os = ovs.getOimSuppliers();
			if (os.getSupplierId().toString()
					.equals(request.get("sid_" + os.getSupplierId()))) {
				String skuPrefix = request.get("ss_" + os.getSupplierId()
						+ "_skuprefix");
				boolean enableOrderAuto = (Integer.parseInt(request.get("ss_"
						+ os.getSupplierId() + "_enableorderauto")) > 0);
				OimChannelSupplierMap m = new OimChannelSupplierMap();
				m.setOimChannels(c);
				m.setOimSuppliers(os);
				m.setSupplierPrefix(skuPrefix);
				m.setEnableOrderAutomation(enableOrderAuto ? 1 : 0);
				m.setInsertionTm(new Date());
				dbSession.save(m);
				LOG.debug("Saved supplier map");
			}
		}
	}

	private void addRuleWithRequest(Session dbSession, OimChannels c,
			Map<String, String> channelDetails) {
		Set<OimOrderProcessingRule> oimOrderProcessingRules = c
				.getOimOrderProcessingRules();
		for (OimOrderProcessingRule oimOrderProcessingRule : oimOrderProcessingRules) {
			dbSession.delete(oimOrderProcessingRule);
		}
		OimOrderProcessingRule rule = new OimOrderProcessingRule();
		rule.setOimChannels(c);
		rule.setInsertionTm(new Date());

		if ("1".equals(channelDetails.get("processorders"))) {
			rule.setProcessAll(1);
		} else {
			String customStatus = channelDetails
					.get("processorderscustomstatus");
			rule.setProcessAll(0);
			rule.setProcessWithStatus(customStatus);
		}

		if ("1".equals(channelDetails.get("updateorders"))) {
			String orderStatus = channelDetails.get("updateorderscustomstatus");
			rule.setUpdateStoreOrderStatus(1);
			rule.setUpdateWithStatus(orderStatus);
		} else {
			rule.setUpdateStoreOrderStatus(0);
		}
		dbSession.save(rule);
	}

	private void addChannelAccessDetail(Session dbSession, OimChannels channel,
			Integer fieldId, String fieldValue) {
		OimChannelAccessDetails d = new OimChannelAccessDetails();
		d.setDetailFieldValue(fieldValue);
		d.setInsertionTm(new Date());
		OimChannelAccessFields field = new OimChannelAccessFields();
		field.setFieldId(fieldId);
		d.setOimChannelAccessFields(field);
		d.setOimChannels(channel);
		dbSession.save(d);
	}

	@Override
	public void delete(final int channelId) {
		Transaction tx = null;
		Session dbSession = SessionManager.currentSession();
		try {
			tx = dbSession.beginTransaction();
			// Integer channelId = id;

			// Delete OimChannelAccessDetails
			Query query = dbSession
					.createQuery("delete from salesmachine.hibernatedb.OimChannelAccessDetails where oimChannels.channelId="
							+ channelId);
			LOG.debug("Deleted " + query.executeUpdate()
					+ " OimChannelAccessDetails rows");

			// Delete OimChannelSupplierMap
			query = dbSession
					.createQuery("delete from salesmachine.hibernatedb.OimChannelSupplierMap where oimChannels.channelId="
							+ channelId);
			LOG.debug("Deleted " + query.executeUpdate()
					+ " OimChannelSupplierMap rows");

			// Delete OimOrderProcessingRule
			query = dbSession
					.createQuery("delete from salesmachine.hibernatedb.OimOrderProcessingRule where oimChannels.channelId="
							+ channelId);
			LOG.debug("Deleted " + query.executeUpdate()
					+ " OimOrderProcessingRule rows");

			// Delete OimChannelFiles
			query = dbSession
					.createQuery("delete from salesmachine.hibernatedb.OimChannelFiles where oimChannels.channelId="
							+ channelId);
			LOG.debug("Deleted " + query.executeUpdate()
					+ " OimChannelFiles rows");

			// Delete order details
			Query q = dbSession
					.createQuery("select oo from salesmachine.hibernatedb.OimOrders oo where oo.oimOrderBatches.oimChannels.channelId = "
							+ channelId);
			List orders = q.list();
			if (orders.size() > 0) {
				query = dbSession
						.createQuery("delete from salesmachine.hibernatedb.OimOrderDetails d where d.oimOrders in (:orders)");
				query.setParameterList("orders", orders);
				LOG.debug("Deleted " + query.executeUpdate()
						+ " OimOrderDetails rows");
			} else {
				LOG.debug("Deleted 0 OimOrderDetails rows");
			}

			// Delete orders
			q = dbSession
					.createQuery("select oob from salesmachine.hibernatedb.OimOrderBatches oob where oob.oimChannels.channelId = "
							+ channelId);
			List batches = q.list();
			if (batches.size() > 0) {
				query = dbSession
						.createQuery("delete from salesmachine.hibernatedb.OimOrders where oimOrderBatches in (:batches)");
				query.setParameterList("batches", batches);
				LOG.debug("Deleted " + query.executeUpdate()
						+ " OimOrders rows");
			} else {
				LOG.debug("Deleted 0 OimOrders rows");
			}

			// Delete OIM uploaded files
			query = dbSession
					.createQuery("delete from salesmachine.hibernatedb.OimUploadedFiles where oimChannels.channelId="
							+ channelId);
			LOG.debug("Deleted " + query.executeUpdate()
					+ " OimUploadedFiles rows");
			// Delete order batches
			query = dbSession
					.createQuery("delete from salesmachine.hibernatedb.OimOrderBatches where oimChannels.channelId="
							+ channelId);
			LOG.debug("Deleted " + query.executeUpdate()
					+ " OimOrderBatches rows");
			// Delete OimChannels
			query = dbSession
					.createQuery("delete from salesmachine.hibernatedb.OimChannels where channelId="
							+ channelId);
			LOG.debug("Deleted " + query.executeUpdate() + " OimChannels rows");
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			e.printStackTrace();
		}
	}

	@Override
	public Channel findById(int id) {
		Session dbSession = SessionManager.currentSession();
		try {
			OimChannels oimchannel = (OimChannels) dbSession.get(
					OimChannels.class, id);
			if (oimchannel == null) {
				LOG.debug("No channel found for id: {}", id);
			}
			return Channel.from(oimchannel);
		} catch (Exception e) {
			LOG.error("Error in fetching channel by id: {} ", id, e);
		}
		return null;
	}

	@Override
	public Channel findByName(String name) {
		Session dbSession = SessionManager.currentSession();
		try {
			Object oimChannels = dbSession.createCriteria(OimChannels.class)
					.add(Restrictions.eq("channelName", name)).uniqueResult();
			if (oimChannels == null) {
				LOG.debug("No channel found by Name: {}", name);
			}
			return Channel.from((OimChannels) oimChannels);
		} catch (Exception e) {
			LOG.error("Error in fetching channel by Name: {} ", name, e);
		}
		return null;
	}

	@Override
	public List<Channel> findAll() {
		Session dbSession = SessionManager.currentSession();
		List<Channel> channels = new ArrayList<Channel>();
		try {
			Criteria createCriteria = dbSession
					.createCriteria(OimChannels.class);
			createCriteria.add(Restrictions.eq("vendors.vendorId",
					getVendorId()));
			createCriteria.addOrder(Order.asc("channelName"));
			List<OimChannels> list = createCriteria.list();
			LOG.debug("Fetched {} Channel(s) ", list.size());
			if (list != null) {
				for (OimChannels oimChannel : list) {
					channels.add(Channel.from(oimChannel));
				}
			}
			return channels;
		} catch (RuntimeException e) {
			LOG.error("Error occured in fetching channels", e);
		}
		return channels;
	}

	private String getParameter(String key) {
		return channelDetails.get(key);
	}

	@Override
	public List<Filetype> getFileTypes(int channelId) {
		Session dbSession = SessionManager.currentSession();
		Query query = dbSession
				.createQuery("select distinct f from salesmachine.hibernatedb.OimFiletypes f inner join f.oimChannelFileses cf where f.deleteTm is null and cf.oimChannels.channelId=:channelId");
		List<OimFiletypes> filetypes = (List<OimFiletypes>) query.setInteger(
				"channelId", channelId).list();
		List<Filetype> fileList = new ArrayList<Filetype>();
		for (OimFiletypes oimFiletypes : filetypes) {
			fileList.add(Filetype.from(oimFiletypes));
		}
		return fileList;
	}

	@Override
	public List<UploadedFile> getOimUploadedFiles(int channelId) {
		Session dbSession = SessionManager.currentSession();
		Query query = dbSession
				.createQuery("select distinct uf from salesmachine.hibernatedb.OimUploadedFiles uf inner join fetch uf.oimOrderBatches b inner join fetch b.oimOrderses where uf.oimChannels.channelId=:channelId and uf.deleteTm is null");
		List<OimUploadedFiles> uploadedfiles = (List<OimUploadedFiles>) query
				.setEntity("channelId", channelId).list();
		List<UploadedFile> uploadList = new ArrayList<UploadedFile>();
		for (OimUploadedFiles oimFiletypes : uploadedfiles) {
			uploadList.add(UploadedFile.from(oimFiletypes));
		}
		return uploadList;
	}

	@Override
	public List<ChannelShippingMap> findShippingMapping(
			Integer supportedChannelId) {
		Session dbSession = SessionManager.currentSession();
		Criteria findCriteria = dbSession
				.createCriteria(OimChannelShippingMap.class);
		findCriteria.add(Restrictions.eq(
				"oimSupportedChannel.supportedChannelId", supportedChannelId));
		List<OimChannelShippingMap> list = findCriteria.list();
		List<ChannelShippingMap> retList = new ArrayList<ChannelShippingMap>();
		for (OimChannelShippingMap entity : list) {
			retList.add(ChannelShippingMap.from(entity));
		}
		return retList;
	}

	@Override
	public List<SupportedChannel> findSupportedChannels() {
		Session session = SessionManager.currentSession();
		List<OimSupportedChannels> list = session
				.createCriteria(OimSupportedChannels.class)
				.add(Restrictions.isNull("deleteTm"))
				.addOrder(Order.asc("channelName")).list();
		List<SupportedChannel> supportedChannels = new ArrayList<SupportedChannel>();
		for (OimSupportedChannels oimSupportedChannel : list) {
			supportedChannels.add(SupportedChannel.from(oimSupportedChannel));
		}
		return supportedChannels;
	}
}
