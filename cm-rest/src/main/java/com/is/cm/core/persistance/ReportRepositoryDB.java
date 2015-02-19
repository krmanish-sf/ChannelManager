package com.is.cm.core.persistance;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSuppliers;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.suppliers.OimSupplierOrderPlacement;
import salesmachine.util.StringHandle;

import com.is.cm.core.domain.ProductSalesData;
import com.is.cm.core.domain.ReportDataWrapper;

public class ReportRepositoryDB extends RepositoryBase implements
		ReportRepository {
	private static Logger LOG = LoggerFactory
			.getLogger(ReportRepositoryDB.class);

	private Date m_startDate;
	private Date m_endDate;
	ReportDataWrapper reportDataWrapper = new ReportDataWrapper();

	@Override
	public ReportDataWrapper getReportData(Date startDate, Date endDate,
			String reportType) {
		reportDataWrapper = new ReportDataWrapper();
		m_startDate = startDate;
		m_endDate = endDate;
		Session dbSession = SessionManager.currentSession();
		String channelId = "";
		String supplierId = "";
		if (reportType == null)
			reportType = "";
		if (reportType.equalsIgnoreCase("supplier")) {
			List<SupplierSalesData> supplierSales = getSupplierSalesData(
					dbSession, getVendorId(), null);
			reportDataWrapper.put("suppliersales", supplierSales);
		} else if (reportType.equalsIgnoreCase("product")) {
			List<ProductSalesData> productSales = getProductSalesData(
					dbSession, getVendorId(), null, null);
			reportDataWrapper.put("productsales", productSales);
		} else if (reportType.equalsIgnoreCase("channel")) {
			List<ChannelSalesData> channelSales = getChannelSalesData(
					dbSession, getVendorId());
			reportDataWrapper.put("channelsales", channelSales);
		} else if (reportType.equalsIgnoreCase("totalsales")) {
			List<OverAllSalesData> overallSales = getOverallSalesData(
					dbSession, getVendorId(), null, null, m_startDate,
					m_endDate);
			reportDataWrapper.put("overAllSales", overallSales);
		} else {
			List<ChannelSalesData> channelSales = getChannelSalesData(
					dbSession, getVendorId());
			List<SupplierSalesData> supplierSales = getSupplierSalesData(
					dbSession, getVendorId(), null);
			List<ProductSalesData> productSales = getProductSalesData(
					dbSession, getVendorId(), null, null);
			List<OverAllSalesData> overallSales = getOverallSalesData(
					dbSession, getVendorId(), null, null, m_startDate,
					m_endDate);
			reportDataWrapper.put("channelsales", channelSales);
			reportDataWrapper.put("suppliersales", supplierSales);
			reportDataWrapper.put("productsales", productSales);

			reportDataWrapper.put("overAllSales", overallSales);
		}
		return reportDataWrapper;
	}

	@Override
	public ReportDataWrapper getReportData(Date startDate, Date endDate) {
		m_startDate = startDate;
		m_endDate = endDate;
		String channelId = "";
		String supplierId = "";

		// channel report query substring.
		Session dbSession = SessionManager.currentSession();
		// Transaction tx = null;
		try {
			// tx = dbSession.beginTransaction();

			// Here is your db code
			reportDataWrapper.put("OrderSummaryData",
					getOrderSummaryData(dbSession, getVendorId()));
			List<OverAllSalesData> overallSales = null;
			String format = "";// request.getParameter("format");
			format = StringHandle.removeNull(format);
			String reportType = "";/*
									 * StringHandle.removeNull(request
									 * .getParameter("reportType"));
									 */

			if (channelId.equals("") && supplierId.equals("")) {
				List<ChannelSalesData> channelSales = getChannelSalesData(
						dbSession, getVendorId());
				List<SupplierSalesData> supplierSales = getSupplierSalesData(
						dbSession, getVendorId(), null);
				List<ProductSalesData> productSales = getProductSalesData(
						dbSession, getVendorId(), null, null);
				reportDataWrapper.put("channelsales", channelSales);
				reportDataWrapper.put("suppliersales", supplierSales);
				reportDataWrapper.put("productsales", productSales);

				overallSales = getOverallSalesData(dbSession, getVendorId(),
						null, null, m_startDate, m_endDate);
				reportDataWrapper.put("overAllSales", overallSales);
			} else {
				if ((supplierId != "") && (channelId == "")) {
					List<ProductSalesData> productSales = getProductSalesData(
							dbSession, getVendorId(), null, supplierId);
					String supplierName = getSupplierName(dbSession, supplierId);
					reportDataWrapper.put("supplierName", supplierName);
					reportDataWrapper.put("productsales", productSales);

					overallSales = getOverallSalesData(dbSession,
							getVendorId(), supplierId, null, m_startDate,
							m_endDate);
					reportDataWrapper.put("overAllSales", overallSales);
				} else if ((supplierId == "") && (channelId != "")) {
					List<SupplierSalesData> supplierSales = getSupplierSalesData(
							dbSession, getVendorId(), channelId);
					List<ProductSalesData> productSales = getProductSalesData(
							dbSession, getVendorId(), channelId, null);
					String channelName = getChannelName(dbSession, channelId);
					reportDataWrapper.put("channelName", channelName);
					reportDataWrapper.put("suppliersales", supplierSales);
					reportDataWrapper.put("productsales", productSales);

					overallSales = getOverallSalesData(dbSession,
							getVendorId(), null, channelId, m_startDate,
							m_endDate);
					reportDataWrapper.put("overAllSales", overallSales);
				} else {
					List<ProductSalesData> productSales = getProductSalesData(
							dbSession, getVendorId(), channelId, supplierId);
					String channelName = getChannelName(dbSession, channelId);
					reportDataWrapper.put("channelName", channelName);

					String supplierName = getSupplierName(dbSession, supplierId);
					reportDataWrapper.put("supplierName", supplierName);

					reportDataWrapper.put("productsales", productSales);
					overallSales = getOverallSalesData(dbSession,
							getVendorId(), supplierId, channelId, m_startDate,
							m_endDate);
					reportDataWrapper.put("overAllSales", overallSales);
				}
			}
			// tx.commit();
		} catch (RuntimeException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			// SessionManager.closeSession();
		}

		return reportDataWrapper;
	}

	private String getSupplierName(Session dbSession, String supplierid) {
		String supplierName = "";
		try {
			Query query = dbSession
					.createQuery("select s.supplierName from salesmachine.hibernatedb.OimSuppliers s where s.supplierId=:sid");

			Iterator iter = query.setInteger("sid",
					Integer.parseInt(supplierid)).iterate();
			if (iter.hasNext()) {
				supplierName = (String) iter.next();
				// System.out.println("!!! Supplier Name : " + supplierName);
			}
		} catch (NumberFormatException e) {
			supplierName = "unknown";

		} catch (Exception e) {
			LOG.error("Error", e);
		}

		return supplierName;
	}

	private String getChannelName(Session dbSession, String channelid) {
		String channelName = "";
		try {
			Query query = dbSession
					.createQuery("select c.channelName from salesmachine.hibernatedb.OimChannels c where c.channelId=:cid");
			Iterator iter = query
					.setInteger("cid", Integer.parseInt(channelid)).iterate();

			if (iter.hasNext()) {
				channelName = (String) iter.next();
			}

		} catch (Exception e) {
			LOG.error("Error", e);
		}

		return channelName;
	}

	@Override
	public List<OverAllSalesData> getOverallSalesData(Integer vendorId,
			String supplierId, String channelId, Date startDate, Date endDate) {
		Session dbSession = SessionManager.currentSession();
		return getOverallSalesData(dbSession, vendorId, supplierId, channelId,
				startDate, endDate);
	}

	public List<OverAllSalesData> getOverallSalesData(Session dbSession,
			Integer vendorId, String supplierId, String channelId,
			Date startDate, Date endDate) {
		List<OverAllSalesData> overallSales = new ArrayList<OverAllSalesData>();
		Double totalSalePrice = 0.00;
		String dateSubQuery = " and d.insertionTm between to_date ('"
				+ dateToString(startDate) + "', 'mm-dd-yyyy') AND to_date ('"
				+ dateToString(endDate) + "', 'mm-dd-yyyy') ";
		String conditionSubQuery = "";
		if (supplierId == null && channelId == null) {
			conditionSubQuery = "";
		} else if (supplierId != null && channelId == null) {
			conditionSubQuery = " and d.oimSuppliers.supplierId =:sid ";
		} else if (supplierId == null && channelId != null) {
			conditionSubQuery = " and o.oimOrderBatches.oimChannels.channelId =:cid ";
		} else if (supplierId != null && channelId != null) {
			conditionSubQuery = " and d.oimSuppliers.supplierId =:sid and o.oimOrderBatches.oimChannels.channelId =:cid ";
		}
		Query query = dbSession
				.createQuery("select sum(d.salePrice*d.quantity), d.insertionTm from "
						+ "salesmachine.hibernatedb.OimOrders o inner join "
						+ "o.oimOrderDetailses d where "
						+ "o.deleteTm is null and "
						+ "d.deleteTm is null "
						+ dateSubQuery
						+ " and d.salePrice is not null "
						+ " and o.oimOrderBatches.oimChannels.vendors.vendorId =:vid "
						+ conditionSubQuery
						+ " group by d.insertionTm order by d.insertionTm desc");
		// System.out.println(query.getQueryString());
		if (supplierId != null && channelId == null) {
			query.setInteger("sid", Integer.parseInt(supplierId));
		} else if (supplierId == null && channelId != null) {
			query.setInteger("cid", Integer.parseInt(channelId));
		} else if (supplierId != null && channelId != null) {
			query.setInteger("cid", Integer.parseInt(channelId));
			query.setInteger("sid", Integer.parseInt(supplierId));
		}
		query.setInteger("vid", vendorId);
		Iterator iter = query.iterate();

		while (iter.hasNext()) {
			Object[] row = (Object[]) iter.next();
			double sp = (row[0] == null) ? 0 : (Double) row[0];
			totalSalePrice = totalSalePrice + sp;
			final double tsp = totalSalePrice;
			final Date dt = (Date) row[1];
			overallSales.add(new OverAllSalesData() {
				@Override
				public Double getTotalSales() {
					return tsp;
				}

				@Override
				public Date getDate() {
					return dt;
				}
			});
		}
		// Hibernate.close(iter);// ??? // this is explicitly closing the
		// iterator started by the session handler,
		// this is not required here as sessionmanager.closesession will
		// automatically be taking care of that.
		DecimalFormat priceFormatter = new DecimalFormat("#0.00");
		reportDataWrapper.put("additionalRevenues",
				String.valueOf(priceFormatter.format(totalSalePrice)));

		query = dbSession.createQuery("select count(o.orderId) from "
				+ "salesmachine.hibernatedb.OimOrders o inner join "
				+ "o.oimOrderDetailses d where " + "o.deleteTm is null and "
				+ "d.deleteTm is null " + dateSubQuery
				+ " and o.oimOrderBatches.oimChannels.vendors.vendorId =:vid "
				+ conditionSubQuery
		// + " and d.oimOrderStatuses.statusId = 0 "
				);
		// System.out.println(query.getQueryString());
		if (supplierId != null && channelId == null) {
			query.setInteger("sid", Integer.parseInt(supplierId));
		} else if (supplierId == null && channelId != null) {
			query.setInteger("cid", Integer.parseInt(channelId));
		} else if (supplierId != null && channelId != null) {
			query.setInteger("cid", Integer.parseInt(channelId));
			query.setInteger("sid", Integer.parseInt(supplierId));
		}
		query.setInteger("vid", vendorId);
		iter = query.iterate();
		if (iter.hasNext()) {
			reportDataWrapper.put("totalOrders", String.valueOf(iter.next()));
		}

		/*
		 * if (!overallSales.containsKey(m_startDate)) {
		 * overallSales.put(m_startDate, "0"); dates.add(m_startDate); } if
		 * (!overallSales.containsKey(m_endDate)) { overallSales.put(m_endDate,
		 * "0"); dates.add(0, m_endDate); }
		 */
		return overallSales;
	}

	private List<ChannelSalesData> getChannelSalesData(Session dbSession,
			Integer vendorId) {
		List<ChannelSalesData> channelSales = new ArrayList<ChannelSalesData>();

		String dateSubQuery = " and o.insertionTm between to_date ('"
				+ dateToString(m_startDate) + "', 'mm-dd-yyyy')AND to_date ('"
				+ dateToString(m_endDate) + "', 'mm-dd-yyyy') ";
		try {
			Query query = dbSession
					.createQuery("select c.channelId, c.channelName, sum(d.salePrice*d.quantity) from "
							+ "salesmachine.hibernatedb.OimOrders o inner join "
							+ "o.oimOrderDetailses d inner join "
							+ "o.oimOrderBatches.oimChannels c where "
							+ "o.deleteTm is null and "
							+ "d.deleteTm is null and d.salePrice is not null "
							+ "and c.deleteTm is null "
							+ dateSubQuery
							+ " and o.oimOrderBatches.oimChannels.vendors.vendorId =:vid "
							// + "and d.oimOrderStatuses.statusId = 0"
							+ " group by "
							+ "c.channelId, c.channelName order by sum(d.salePrice*d.quantity) desc");
			Iterator iter = query.setInteger("vid", vendorId).setFirstResult(0)
					.setMaxResults(10).iterate();
			while (iter.hasNext()) {
				Object[] row = (Object[]) iter.next();
				final int channelId = (Integer) row[0];
				final String channelName = (String) row[1];
				final Double totalSales = row[2] != null ? (Double) row[2]
						: new Double(0);
				channelSales.add(new ChannelSalesData() {

					@Override
					public Double getTotalSales() {
						return totalSales;
					}

					@Override
					public String getName() {
						return channelName;
					}

					@Override
					public int getId() {
						return channelId;
					}
				});
			}
		} catch (Exception e) {
			LOG.error("Error occured", e);
		}
		return channelSales;
	}

	public interface ChannelSalesData {
		String getName();

		Double getTotalSales();

		int getId();
	}

	public interface SupplierSalesData {
		String getName();

		Double getTotalSales();

		int getId();
	}

	public interface OverAllSalesData {
		Date getDate();

		Double getTotalSales();
	}

	public interface OrderSummaryData {
		Integer getUnresolvedCount();

		Integer getUnprocessedCount();

		Double getUnresolvedAmount();

		Double getUnprocessedAmount();
	}

	private List<SupplierSalesData> getSupplierSalesData(Session dbSession,
			Integer vendorId, String channelid) {
		List<SupplierSalesData> supplierSales = new ArrayList<SupplierSalesData>();
		// channelid =
		// StringHandle.removeNull(request.getParameter("channelid"));

		String dateSubQuery = " and od.oimOrders.insertionTm between to_date ('"
				+ dateToString(m_startDate)
				+ "', 'mm-dd-yyyy')AND to_date ('"
				+ dateToString(m_endDate) + "', 'mm-dd-yyyy') ";

		try {
			Query query = null;
			if (channelid == null || "".equals(channelid)) {
				query = dbSession
						.createQuery("select s.supplierId, s.supplierName, sum(od.salePrice*od.quantity) "
								+ " from salesmachine.hibernatedb.OimOrderDetails od "
								+ " inner join od.oimSuppliers s "
								+ " where od.oimOrders.oimOrderBatches.oimChannels.vendors.vendorId =:vid "
								+ " and od.deleteTm is null "
								+ " and od.oimSuppliers.deleteTm is null and od.salePrice is not null "
								+ dateSubQuery
								// + " and od.oimOrderStatuses.statusId = 0 "
								+ " group by s.supplierId, s.supplierName "
								+ " order by sum(od.salePrice*od.quantity) desc");
				query.setInteger("vid", vendorId);
			} else {

				query = dbSession
						.createQuery("select s.supplierId, s.supplierName, sum(od.salePrice*od.quantity) "
								+ " from salesmachine.hibernatedb.OimOrderDetails od "
								+ " inner join od.oimSuppliers s "
								+ " where od.oimOrders.oimOrderBatches.oimChannels.vendors.vendorId =:vid "
								+ " and od.deleteTm is null and od.salePrice is not null and "
								+ " od.oimOrders.deleteTm is null and "
								+ " od.oimSuppliers.deleteTm is null "
								+ dateSubQuery
								// + " and od.oimOrderStatuses.statusId = 0 "
								+ " and od.oimOrders.oimOrderBatches.oimChannels.channelId=:cid"
								+ " group by s.supplierId, s.supplierName "
								+ " order by sum(od.salePrice*od.quantity) desc");

				query.setInteger("vid", vendorId);
				query.setInteger("cid", Integer.parseInt(channelid));
			}
			// System.out.println(query.getQueryString());
			Iterator iter = query.setFirstResult(0).setMaxResults(10).iterate();
			while (iter.hasNext()) {
				Object[] row = (Object[]) iter.next();
				final Integer id = (Integer) row[0];
				final String supplierName = (String) row[1];
				final Double totalSales = row[2] == null ? 0 : (Double) row[2];
				supplierSales.add(new SupplierSalesData() {

					@Override
					public Double getTotalSales() {
						return totalSales;
					}

					@Override
					public String getName() {
						return supplierName;
					}

					@Override
					public int getId() {
						return id;
					}
				});
			}

			// Get the sales figure for the orders that don't belong to any
			// supplier.
			query = dbSession
					.createQuery("select sum(od.salePrice*od.quantity) "
							+ " from salesmachine.hibernatedb.OimOrderDetails od "
							+ " where od.oimOrders.oimOrderBatches.oimChannels.vendors.vendorId =:vid "
							+ " and od.deleteTm is null "
							+ " and od.oimSuppliers is null and od.salePrice is not null "
							+ dateSubQuery
							// + " and od.oimOrderStatuses.statusId = 0 "
							+ "");
			query.setInteger("vid", vendorId);
			List it = query.list();
			if (it.size() > 0) {
				final Double sales = it.get(0) != null ? (Double) it.get(0) : 0;
				if (sales > 0)
					supplierSales.add(new SupplierSalesData() {
						@Override
						public Double getTotalSales() {
							return sales;
						}

						@Override
						public String getName() {
							return "Unknown Supplier";
						}

						@Override
						public int getId() {
							return 0;
						}
					});
			}
		} catch (Exception e) {
			LOG.error("Error", e);
		}
		return supplierSales;
	}

	private List<ProductSalesData> getProductSalesData(Session dbSession,
			Integer vendorId, String channelid, String supplierid) {
		List<ProductSalesData> productSales = new ArrayList<ProductSalesData>();
		// channelid =
		// StringHandle.removeNull(request.getParameter("channelid"));
		/*
		 * supplierid = StringHandle
		 * .removeNull(request.getParameter("supplierid"));
		 */
		supplierid = "";
		String dateSubQuery = " and d.oimOrders.insertionTm between to_date ('"
				+ dateToString(m_startDate) + "', 'mm-dd-yyyy') AND to_date ('"
				+ dateToString(m_endDate) + "', 'mm-dd-yyyy') ";
		try {
			Query query = null;

			/*
			 * if (supplierid == null || supplierid.equalsIgnoreCase("unknown"))
			 * { query = dbSession
			 * .createQuery("Select d.sku, (d.salePrice * d.quantity) " +
			 * " from salesmachine.hibernatedb.OimOrderDetails d" +
			 * " where d.oimOrders.oimOrderBatches.oimChannels.vendors.vendorId =:vid"
			 * + " and d.deleteTm is null and d.salePrice is not null " +
			 * dateSubQuery +
			 * " and d.oimSuppliers is null order by (d.salePrice * d.quantity) desc"
			 * );
			 * 
			 * query.setInteger("vid", vendorId);
			 * 
			 * } else if ((channelid == "") && (supplierid != "")) { query =
			 * dbSession
			 * .createQuery("Select d.sku, (d.salePrice * d.quantity) " +
			 * " from salesmachine.hibernatedb.OimOrderDetails d" +
			 * " where d.oimOrders.oimOrderBatches.oimChannels.vendors.vendorId =:vid"
			 * + " and d.oimSuppliers.supplierId =:sid " // +
			 * " and d.oimOrderStatuses.statusId = 0 " +
			 * " and d.deleteTm is null and d.salePrice is not null " +
			 * dateSubQuery + " order by (d.salePrice * d.quantity) desc");
			 * 
			 * query.setInteger("vid", vendorId); query.setInteger("sid",
			 * Integer.parseInt(supplierid));
			 * 
			 * } else if ((channelid != "") && (supplierid == "")) { query =
			 * dbSession
			 * .createQuery("Select d.sku, (d.salePrice * d.quantity) " +
			 * " from salesmachine.hibernatedb.OimOrderDetails d" +
			 * " where d.oimOrders.oimOrderBatches.oimChannels.vendors.vendorId =:vid"
			 * + " and d.oimOrders.oimOrderBatches.oimChannels.channelId =:cid "
			 * // + " and d.oimOrderStatuses.statusId = 0 " +
			 * " and d.deleteTm is null and d.salePrice is not null " +
			 * dateSubQuery + " order by (d.salePrice * d.quantity) desc");
			 * 
			 * query.setInteger("vid", vendorId); query.setInteger("cid",
			 * Integer.parseInt(channelid));
			 * 
			 * } else if ((channelid != "") && (supplierid != "")) { query =
			 * dbSession
			 * .createQuery("Select d.sku, (d.salePrice * d.quantity) " +
			 * " from salesmachine.hibernatedb.OimOrderDetails d" +
			 * " where d.oimOrders.oimOrderBatches.oimChannels.vendors.vendorId =:vid"
			 * // + " and d.oimOrderStatuses.statusId = 0 " +
			 * " and d.deleteTm is null and d.salePrice is not null " +
			 * dateSubQuery +
			 * " and d.oimOrders.oimOrderBatches.oimChannels.channelId =:cid and d.oimSuppliers.supplierId =:sid order by (d.salePrice * d.quantity) desc"
			 * );
			 * 
			 * query.setInteger("vid", vendorId); query.setInteger("cid",
			 * Integer.parseInt(channelid)); query.setInteger("sid",
			 * Integer.parseInt(supplierid));
			 * 
			 * } else {
			 */
			query = dbSession
					.createQuery("Select d.sku, sum(d.salePrice * d.quantity),sum(d.quantity) "
							+ " from salesmachine.hibernatedb.OimOrderDetails d"
							+ " where d.oimOrders.oimOrderBatches.oimChannels.vendors.vendorId =:vid "
							// + " and d.oimOrderStatuses.statusId = 0 "
							+ " and d.deleteTm is null and d.salePrice is not null "
							+ dateSubQuery
							+ " group by d.sku order by sum(d.salePrice * d.quantity) desc");

			query.setInteger("vid", vendorId);
			// }
			// System.out.println(query.getQueryString());
			Iterator iter = query.setFirstResult(0).setMaxResults(10).iterate();
			// int i = 1;
			while (iter.hasNext()) {
				Object[] row = (Object[]) iter.next();
				final String productSku = (String) row[0];
				final Double totalSales = row[1] == null ? 0 : (Double) row[1];
				final int totalQuantity = row[2] == null ? 0 : ((Long) row[2])
						.intValue();
				productSales.add(new ProductSalesData() {

					@Override
					public Double getTotalSales() {

						return totalSales;
					}

					@Override
					public int getTotalQunatity() {

						return totalQuantity;
					}

					@Override
					public String getSku() {

						return productSku;
					}
				});
			}
		} catch (Exception e) {
			LOG.error("Error ", e);
		}
		return productSales;
	}

	/*
	 * private void parseDates() { String datetype = "static";
	 * StringHandle.removeNull(request .getParameter("datetype"));
	 * 
	 * if (datetype.equals("static")) { String timeDur = "thismonth"
	 * StringHandle.removeNull(request .getParameter("timeDuration")) ; if
	 * ("thismonth".equals(timeDur)) { m_startDate = new Date();
	 * m_startDate.setDate(1); m_endDate = new Date(); } else if
	 * ("lastseven".equals(timeDur)) { Calendar cal = Calendar.getInstance();
	 * cal.add(Calendar.DATE, -7); m_startDate = cal.getTime(); m_endDate = new
	 * Date(); } else if ("lastMonth".equals(timeDur)) { Calendar cal =
	 * Calendar.getInstance(); cal.add(Calendar.MONTH, -1); m_startDate =
	 * cal.getTime(); m_startDate.setDate(1); m_endDate = cal.getTime();
	 * m_endDate.setDate(cal.getActualMaximum(Calendar.DAY_OF_MONTH)); } else if
	 * ("allTime".equals(timeDur)) { m_startDate = new Date(110, 0, 1);
	 * m_startDate.setDate(1); m_endDate = new Date(); } } else if
	 * (datetype.equals("dynamic")) { String dateSt = null
	 * StringHandle.removeNull(request .getParameter("stdate")) ; String dateEn
	 * = null StringHandle.removeNull(request .getParameter("endate")) ;
	 * 
	 * DateFormat df = new SimpleDateFormat("MM-dd-yy"); try { m_startDate =
	 * df.parse(dateSt); m_endDate = df.parse(dateEn); } catch (Exception e) {
	 * e.printStackTrace(); } } else { m_startDate = new Date(110, 0, 1);
	 * m_startDate.setDate(1); m_endDate = new Date(); } }
	 */

	private String dateToString(Date d) {
		return (1 + d.getMonth()) + "-" + d.getDate() + "-"
				+ (1900 + d.getYear());
	}

	public class CustomerReportData {
		private final String billingEmail, billingName, billingStreetAddress;
		private final Date orderTm;

		public CustomerReportData(final String billingEmail,
				final String billingName, final String billingStreetAddress,
				final Date orderTm) {
			this.billingEmail = billingEmail;
			this.billingName = billingName;
			this.billingStreetAddress = billingStreetAddress;
			this.orderTm = orderTm;
		}

		public String getBillingEmail() {
			return billingEmail;
		}

		public String getBillingName() {
			return billingName;
		}

		public String getBillingStreetAddress() {
			return billingStreetAddress;
		}

		public Date getOrderTm() {
			return orderTm;
		}
	}

	public class ProductReportData {
		private final String sku;
		private final Double salePrice, costPrice;
		private final int quantity;

		public ProductReportData(String sku, Double salePrice,
				Double costPrice, int quantity) {
			this.sku = sku;
			this.salePrice = salePrice;
			this.costPrice = costPrice;
			this.quantity = quantity;
		}

		public String getSku() {
			return sku;
		}

		public Double getSalePrice() {
			return salePrice;
		}

		public Double getCostPrice() {
			return costPrice;
		}

		public int getQuantity() {
			return quantity;
		}
	}

	public class SupplierSalesReportData {
		private final String supplierName;
		private final Double totalSale;

		public String getSupplierName() {
			return supplierName;
		}

		public Double getTotalSale() {
			return totalSale;
		}

		public SupplierSalesReportData(String supplierName, Double totalSale) {
			super();
			this.supplierName = supplierName;
			this.totalSale = totalSale;
		}
	}

	@Override
	public List<?> getReportData(String reportType, Date startDate, Date endDate) {
		Session dbSession = SessionManager.currentSession();
		if (reportType.equals("customerdata")) {
			List<CustomerReportData> reportData = new ArrayList<CustomerReportData>();
			String dateSubQuery = " and o.orderTm between to_date ('"
					+ dateToString(startDate)
					+ "', 'mm-dd-yyyy') AND to_date ('" + dateToString(endDate)
					+ "', 'mm-dd-yyyy') ";

			Query query = dbSession
					.createQuery("select o.billingEmail, o.billingName, o.billingStreetAddress, max(o.orderTm) from "
							+ " salesmachine.hibernatedb.OimOrders o where "
							+ " o.billingEmail is not null "
							+ dateSubQuery
							+ " and o.oimOrderBatches.oimChannels.vendors.vendorId =:vid "
							+ " GROUP by o.billingEmail, o.billingName, o.billingStreetAddress ");
			query.setInteger("vid", getVendorId());
			Iterator iter = query.iterate();
			try {
				CustomerReportData d;
				while (iter.hasNext()) {
					Object[] row = (Object[]) iter.next();
					d = new CustomerReportData((String) row[0],
							(String) row[1], (String) row[2], (Date) row[3]);
					reportData.add(d);
				}
				return reportData;
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		} else if (reportType.equals("totalorder")) {
			List<ProductReportData> reportData = new ArrayList<ProductReportData>();
			String dateSubQuery = " and d.insertionTm between to_date ('"
					+ dateToString(startDate)
					+ "', 'mm-dd-yyyy')AND to_date ('" + dateToString(endDate)
					+ "', 'mm-dd-yyyy') ";

			Query query = dbSession
					.createQuery("select d.sku, d.salePrice , d.quantity, d.costPrice "
							+ " from salesmachine.hibernatedb.OimOrderDetails d "
							+ " where d.oimOrders.oimOrderBatches.oimChannels.vendors.vendorId =:vid "
							+ " and d.deleteTm is null and d.salePrice is not null "
							+ dateSubQuery + "  ");
			query.setInteger("vid", getVendorId());
			Iterator iter = query.iterate();
			ProductReportData d;
			try {
				double saleTotal = 0;
				int quantity = 0;
				double costTotal = 0;
				while (iter.hasNext()) {
					Object[] row = (Object[]) iter.next();
					double sale = 0;
					try {
						sale = Double.parseDouble(row[1] + "");
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
					}
					int qunt = 0;
					try {
						qunt = Integer.parseInt(row[2] + "");
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
					}
					double cost = 0;
					try {
						cost = Double.parseDouble(row[3] + "");
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
					}
					LOG.debug("sale*qunt : " + sale + "*" + qunt + " = " + sale
							* qunt);
					saleTotal = saleTotal + (sale * qunt);
					quantity = quantity + qunt;
					costTotal = costTotal + (cost * qunt); //
					d = new ProductReportData((String) row[0], (Double) row[1],
							(Double) row[3], (Integer) row[2]);
					reportData.add(d);
				}
				return reportData;
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		} else if (reportType.equals("breakdowndistributor")) {
			List<SupplierSalesReportData> reportData = new ArrayList<SupplierSalesReportData>();
			String dateSubQuery = " and od.insertionTm between to_date ('"
					+ dateToString(startDate)
					+ "', 'mm-dd-yyyy')AND to_date ('" + dateToString(endDate)
					+ "', 'mm-dd-yyyy') ";
			Query query = dbSession
					.createQuery("select s.supplierName, sum(od.salePrice*od.quantity) "
							+ " from salesmachine.hibernatedb.OimOrderDetails od "
							+ " inner join od.oimSuppliers s "
							+ " where od.oimOrders.oimOrderBatches.oimChannels.vendors.vendorId =:vid "
							+ " and od.deleteTm is null and od.salePrice is not null"
							+ " and od.oimSuppliers.deleteTm is null "
							+ dateSubQuery
							+ " group by s.supplierName "
							+ " order by sum(od.salePrice*od.quantity) desc");
			query.setInteger("vid", getVendorId());
			Iterator iter = query.iterate();
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy");
			try {
				SupplierSalesReportData d;
				while (iter.hasNext()) {
					Object[] row = (Object[]) iter.next(); //
					// sb.append("\"" + row[0] + "\",\"" + row[1] + "\"\n");
					d = new SupplierSalesReportData((String) row[0],
							(Double) row[1]);
					reportData.add(d);
				}
				return reportData;
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		} else if (reportType.equals("breakdownproduct")) {
			List<ProductSalesData> reportData = new ArrayList<ProductSalesData>();
			String dateSubQuery = " and d.insertionTm between to_date ('"
					+ dateToString(startDate)
					+ "', 'mm-dd-yyyy')AND to_date ('" + dateToString(endDate)
					+ "', 'mm-dd-yyyy') ";
			Query query = dbSession
					.createQuery("select d.sku, sum(d.salePrice * d.quantity), sum(d.quantity) "
							+ " from salesmachine.hibernatedb.OimOrderDetails d "
							+ " where d.oimOrders.oimOrderBatches.oimChannels.vendors.vendorId =:vid "
							+ " and d.deleteTm is null and d.salePrice is not null "
							+ dateSubQuery + " group by d.sku ");
			query.setInteger("vid", getVendorId());
			Iterator iter = query.iterate();
			try {
				// sb.append("\"SKU\",\"Net Sales\",\"Quantity Sold\"\n");
				ProductSalesData d;
				while (iter.hasNext()) {
					Object[] row = (Object[]) iter.next(); //
					final String sku = (String) row[0];
					final Double totalSales = row[1] == null ? 0
							: (Double) row[1];
					final int totalQuantity = row[2] == null ? 0
							: (Integer) row[2];
					d = new ProductSalesData() {

						@Override
						public Double getTotalSales() {
							return totalSales;
						}

						@Override
						public int getTotalQunatity() {
							return totalQuantity;
						}

						@Override
						public String getSku() {
							return sku;
						}
					};
					reportData.add(d);
				}
				return reportData;
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return null;
	}

	@Override
	public String getDownloadReportData(String reportType, Date startDate,
			Date endDate) {
		Session dbSession = SessionManager.currentSession();

		StringBuilder sb = new StringBuilder();
		if (reportType.equals("customerdata")) {
			String dateSubQuery = " and o.orderTm between to_date ('"
					+ dateToString(startDate)
					+ "', 'mm-dd-yyyy')AND to_date ('" + dateToString(endDate)
					+ "', 'mm-dd-yyyy') ";

			Query query = dbSession
					.createQuery("select o.billingEmail, o.billingName, o.billingStreetAddress, max(o.orderTm) from "
							+ " salesmachine.hibernatedb.OimOrders o where "
							+ " o.billingEmail is not null "
							+ dateSubQuery
							+ " and o.oimOrderBatches.oimChannels.vendors.vendorId =:vid "
							+ " GROUP by o.billingEmail, o.billingName, o.billingStreetAddress ");
			query.setInteger("vid", getVendorId());
			Iterator iter = query.iterate();
			try {
				sb.append("\"Emails ID\",\"Name\",\"Address\",\"Date of Last Order\"\n");
				while (iter.hasNext()) {
					Object[] row = (Object[]) iter.next(); //
					sb.append("\"" + row[0] + "\",\"" + row[1] + "\",\""
							+ row[2] + "\",\"" + row[3] + "\"\n");
				}
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		} else if (reportType.equals("totalorder")) {
			String dateSubQuery = " and d.insertionTm between to_date ('"
					+ dateToString(startDate)
					+ "', 'mm-dd-yyyy')AND to_date ('" + dateToString(endDate)
					+ "', 'mm-dd-yyyy') ";

			Query query = dbSession
					.createQuery("select d.sku, d.salePrice , d.quantity, d.costPrice "
							+ " from salesmachine.hibernatedb.OimOrderDetails d "
							+ " where d.oimOrders.oimOrderBatches.oimChannels.vendors.vendorId =:vid "
							+ " and d.deleteTm is null and d.salePrice is not null "
							+ dateSubQuery + "  ");
			query.setInteger("vid", getVendorId());
			Iterator iter = query.iterate();
			try {
				sb.append("\"SKU\",\"Sale Price\",\"Quantity\",\"Cost\"\n");
				double saleTotal = 0;
				int quantity = 0;
				double costTotal = 0;
				while (iter.hasNext()) {
					Object[] row = (Object[]) iter.next();
					double sale = 0;
					try {
						sale = Double.parseDouble(row[1] + "");
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
					}
					int qunt = 0;
					try {
						qunt = Integer.parseInt(row[2] + "");
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
					}
					double cost = 0;
					try {
						cost = Double.parseDouble(row[3] + "");
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
					}
					LOG.debug("sale*qunt : " + sale + "*" + qunt + " = " + sale
							* qunt);
					saleTotal = saleTotal + (sale * qunt);
					quantity = quantity + qunt;
					costTotal = costTotal + (cost * qunt); //
					sb.append("\"" + row[0] + "\",\"" + row[1] + "\",\""
							+ row[2] + "\",\"" + row[3] + "\"\n");
				}
				sb.append("\"Total : \",\"" + saleTotal + "\",\"" + quantity
						+ "\",\"" + costTotal + "\"\n");
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		} else if (reportType.equals("breakdowndistributor")) {
			String dateSubQuery = " and od.insertionTm between to_date ('"
					+ dateToString(startDate)
					+ "', 'mm-dd-yyyy')AND to_date ('" + dateToString(endDate)
					+ "', 'mm-dd-yyyy') ";
			Query query = dbSession
					.createQuery("select s.supplierName, sum(od.salePrice*od.quantity) "
							+ " from salesmachine.hibernatedb.OimOrderDetails od "
							+ " inner join od.oimSuppliers s "
							+ " where od.oimOrders.oimOrderBatches.oimChannels.vendors.vendorId =:vid "
							+ " and od.deleteTm is null and od.salePrice is not null"
							+ " and od.oimSuppliers.deleteTm is null "
							+ dateSubQuery
							+ " group by s.supplierName "
							+ " order by sum(od.salePrice*od.quantity) desc");
			query.setInteger("vid", getVendorId());
			Iterator iter = query.iterate();
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy");
			try {
				sb.append("\"Supplier Name\",\"Net Sales\"\n");
				while (iter.hasNext()) {
					Object[] row = (Object[]) iter.next(); //
					sb.append("\"" + row[0] + "\",\"" + row[1] + "\"\n");
				}
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		} else if (reportType.equals("breakdownproduct")) {
			String dateSubQuery = " and d.insertionTm between to_date ('"
					+ dateToString(startDate)
					+ "', 'mm-dd-yyyy')AND to_date ('" + dateToString(endDate)
					+ "', 'mm-dd-yyyy') ";
			Query query = dbSession
					.createQuery("select d.sku, sum(d.salePrice * d.quantity), sum(d.quantity) "
							+ " from salesmachine.hibernatedb.OimOrderDetails d "
							+ " where d.oimOrders.oimOrderBatches.oimChannels.vendors.vendorId =:vid "
							+ " and d.deleteTm is null and d.salePrice is not null "
							+ dateSubQuery + " group by d.sku ");
			query.setInteger("vid", getVendorId());
			Iterator iter = query.iterate();
			try {
				sb.append("\"SKU\",\"Net Sales\",\"Quantity Sold\"\n");
				while (iter.hasNext()) {
					Object[] row = (Object[]) iter.next(); //
					sb.append("\"" + row[0] + "\",\"" + row[1] + "\",\""
							+ row[2] + "\"\n");
				}
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return sb.toString();
	}

	private OrderSummaryData getOrderSummaryData(Session dbSession, int vendorId) {
		String sort_query = "order by o.orderFetchTm desc";
		Query query = dbSession
				.createQuery("select distinct o from salesmachine.hibernatedb.OimOrders o "
						+ "left join fetch o.oimOrderDetailses d "
						+ "where o.deleteTm is null and "
						+ "d.deleteTm is null and "
						+ "d.oimOrderStatuses.statusId in (0) and "
						+ "o.oimOrderBatches.oimChannels.vendors.vendorId=:vid "
						+ sort_query);
		query.setInteger("vid", getVendorId());
		List<OimOrders> list = query.list();
		int unprocessedCount = 0, unresolvedCount = 0;
		Double unprocessedAmount = 0D, unresolvedAmount = 0D;
		boolean isUnresolved = false;
		for (OimOrders oimOrders : list) {
			if (oimOrders.getOimOrderDetailses() != null) {
				isUnresolved = false;
				Set<OimOrderDetails> listDetails = oimOrders
						.getOimOrderDetailses();
				for (OimOrderDetails details : listDetails) {
					if (details.getOimOrderStatuses() != null
							&& details
									.getOimOrderStatuses()
									.getStatusId()
									.equals(OimConstants.ORDER_STATUS_MANUALLY_PROCESSED)) {
						break;
					}
					if (details.getOimSuppliers() == null
							|| details.getSalePrice() == null
							|| details.getQuantity() == null) {
						isUnresolved = true;
						break;
					}
				}
				Double total = (oimOrders.getOrderTotalAmount() != null && oimOrders
						.getOrderTotalAmount() > 0) ? oimOrders
						.getOrderTotalAmount() : 0;

				if (isUnresolved) {
					unresolvedCount++;
					unresolvedAmount += total;
				} else {
					unprocessedCount++;
					unprocessedAmount += total;
				}
			}
		}
		final int unrCount = unresolvedCount, unpCount = unprocessedCount;
		final Double unrAmount = unresolvedAmount, unpAmount = unprocessedAmount;
		return new OrderSummaryData() {

			@Override
			public Integer getUnresolvedCount() {
				return unrCount;
			}

			@Override
			public Double getUnresolvedAmount() {
				return unrAmount;
			}

			@Override
			public Integer getUnprocessedCount() {
				return unpCount;
			}

			@Override
			public Double getUnprocessedAmount() {
				return unpAmount;
			}
		};
	}

	@Override
	public Map<String, Map> getAlertAndErrors(int vendorId) {
		Map<String, Map> returnMap = new HashMap<String, Map>();
		Vendors vendor = new Vendors(getVendorId());
		Map<Integer, Map<String, Object>> supplierErrors = new HashMap<Integer, Map<String, Object>>();
		String hql = "select history.oimSuppliers as suppliers,"
				+ "history.processingTm,history.errorCode, history.description "
				+ "from salesmachine.hibernatedb.OimVendorsuppOrderhistory history "
				+ "where vendors=:v and processingTm is not null order by processingTm";
		Session dbSession = SessionManager.currentSession();
		Query query = dbSession.createQuery(hql);
		Iterator iter = query.setEntity("v", vendor).iterate();
		Integer errorCode = null;
		String description = null;
		OimSuppliers supplier = null;
		Date processingTm = null;
		while (iter.hasNext()) {
			Object[] row = (Object[]) iter.next();
			supplier = (OimSuppliers) row[0];
			processingTm = (Date) row[1];
			errorCode = (Integer) row[2];
			description = (String) row[3];

			if (StringHandle.isNullOrEmpty(description)) {
				supplierErrors.remove(supplier.getSupplierId());
				continue;
			}

			Map<String, Object> errorDetails = new HashMap<String, Object>();
			errorDetails.put("errorcode", errorCode);

			errorDetails.put("errordesc", description);
			errorDetails.put("supplier", supplier.getSupplierName());
			errorDetails.put("processingtm", processingTm);
			String errorCodeStr = "";
			if (errorCode.intValue() == OimSupplierOrderPlacement.ERROR_PING_FAILURE) {
				errorCodeStr = "Ping Failure";
			} else if (errorCode.intValue() == OimSupplierOrderPlacement.ERROR_UNCONFIGURED_SUPPLIER) {
				errorCodeStr = "UnConfigured Supplier";
			} else if (errorCode.intValue() == OimSupplierOrderPlacement.ERROR_ORDER_PROCESSING) {
				errorCodeStr = "Order Processing failure";
			}
			errorDetails.put("errormsg", errorCodeStr);
			supplierErrors.put(supplier.getSupplierId(), errorDetails);
		}
		returnMap.put("supplierErrors", supplierErrors);
		// Map vendorAlerts = new HashMap();
		hql = "select c.channelId, c.channelName ,count(d) from "
				+ "salesmachine.hibernatedb.OimOrderDetails d inner join "
				+ "d.oimOrders.oimOrderBatches.oimChannels c where "
				+ "d.deleteTm is null and "
				+ "((d.oimSuppliers is null and "
				+ "d.oimOrderStatuses.statusId = "
				+ OimConstants.ORDER_STATUS_UNPROCESSED
				+ ")) and "
				+ "c.vendors.vendorId =:vid group by c.channelId, c.channelName";

		query = dbSession.createQuery(hql);
		query.setInteger("vid", getVendorId());

		Map<Integer, String> channelNameMap = new HashMap<Integer, String>();
		Map<Integer, Long> channelUnresolvedOrdersMap = new HashMap<Integer, Long>();

		for (Iterator it = query.list().iterator(); it.hasNext();) {
			Object[] row = (Object[]) it.next();
			Integer cId = (Integer) row[0];
			String cName = (String) row[1];
			Long cnt = (Long) row[2];
			channelNameMap.put(cId, cName);
			channelUnresolvedOrdersMap.put(cId, cnt);
			LOG.debug("Channel: " + cName + "(Id:" + cId
					+ ")\tCount: " + cnt);
		}
		returnMap.put("channelNameMap", channelNameMap);
		returnMap.put("channelUnresolvedOrdersMap", channelUnresolvedOrdersMap);
		hql = "select c.channelId, c.channelName ,count(d) from "
				+ "salesmachine.hibernatedb.OimOrderDetails d inner join "
				+ "d.oimOrders.oimOrderBatches.oimChannels c where "
				+ "d.deleteTm is null and "
				+ "((d.oimSuppliers is not null and "
				+ "d.oimOrderStatuses.statusId = "
				+ OimConstants.ORDER_STATUS_UNPROCESSED
				+ ") or (d.oimSuppliers is not null and d.oimOrderStatuses.statusId = "
				+ OimConstants.ORDER_STATUS_PROCESSED_FAILED
				+ ")) and "
				+ "c.vendors.vendorId =:vid group by c.channelId, c.channelName";

		query = dbSession.createQuery(hql);
		query.setInteger("vid", getVendorId());

		Map<Integer, Long> channelUnProcessedOrdersMap = new HashMap<Integer, Long>();

		for (Iterator it = query.list().iterator(); it.hasNext();) {
			Object[] row = (Object[]) it.next();
			Integer cId = (Integer) row[0];
			String cName = (String) row[1];
			Long cnt = (Long) row[2];
			channelNameMap.put(cId, cName);
			channelUnProcessedOrdersMap.put(cId, cnt);
			LOG.debug("unprocessed Channel: " + cName + "(Id:" + cId
					+ ")\tCount: " + cnt);
		}

		returnMap.put("channelUnProcessedOrdersMap",
				channelUnProcessedOrdersMap);
		return returnMap;
	}

}
