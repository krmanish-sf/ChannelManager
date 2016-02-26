package com.is.cm.core.persistance;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSuppliers;
import salesmachine.hibernatedb.OimVendorsuppOrderhistory;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.suppliers.Supplier;
import salesmachine.util.StringHandle;

import com.is.cm.core.domain.DataTableCriterias;
import com.is.cm.core.domain.DataTableCriterias.SearchCriterias;
import com.is.cm.core.domain.OrderBatch;
import com.is.cm.core.domain.PagedDataResult;
import com.is.cm.core.domain.ProductSalesData;
import com.is.cm.core.domain.ReportDataWrapper;
import com.is.cm.core.domain.VendorsuppOrderhistory;

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
		if (reportType == null)
			reportType = "";
		if (reportType.equalsIgnoreCase("supplier")) {
			List<SupplierSalesData> supplierSales = getSupplierSalesData(
					dbSession, getVendorId());
			reportDataWrapper.put("suppliersales", supplierSales);
		} else if (reportType.equalsIgnoreCase("product")) {
			List<ProductSalesData> productSales = getProductSalesData(
					dbSession, getVendorId());
			reportDataWrapper.put("productsales", productSales);
		} else if (reportType.equalsIgnoreCase("channel")) {
			List<ChannelSalesData> channelSales = getChannelSalesData(
					dbSession, getVendorId());
			reportDataWrapper.put("channelsales", channelSales);
		} else if (reportType.equalsIgnoreCase("totalsales")) {
			List overallSales = getOverallSalesData(dbSession, getVendorId());
			reportDataWrapper.put("overAllSales", overallSales);
		} else {
			List<ChannelSalesData> channelSales = getChannelSalesData(
					dbSession, getVendorId());
			List<SupplierSalesData> supplierSales = getSupplierSalesData(
					dbSession, getVendorId());
			List<ProductSalesData> productSales = getProductSalesData(
					dbSession, getVendorId());
			List overallSales = getOverallSalesData(dbSession, getVendorId());
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
		Session dbSession = SessionManager.currentSession();
		try {
			reportDataWrapper.put("OrderSummaryData",
					getOrderSummaryData(dbSession, getVendorId()));
			List<OverAllSalesData> overallSales = null;
			List<ChannelSalesData> channelSales = getChannelSalesData(
					dbSession, getVendorId());
			List<SupplierSalesData> supplierSales = getSupplierSalesData(
					dbSession, getVendorId());
			List<ProductSalesData> productSales = getProductSalesData(
					dbSession, getVendorId());
			reportDataWrapper.put("channelsales", channelSales);
			reportDataWrapper.put("suppliersales", supplierSales);
			reportDataWrapper.put("productsales", productSales);
			overallSales = getOverallSalesData(dbSession, getVendorId());
			reportDataWrapper.put("overAllSales", overallSales);
		} catch (RuntimeException e) {
			LOG.error(e.getMessage(), e);
		}
		return reportDataWrapper;
	}

	private List getOverallSalesData(Session dbSession, Integer vendorId) {

		long start = m_startDate.getTime();
		long end = m_endDate.getTime();
		long days = (end - start) / (24 * 60 * 60 * 1000);
		String trimLevel = days < 4 ? "'HH24'" : "'dd'";
		String dateFormat = days < 4 ? "'MON dd hhPM'" : "'YYYY-MM-DD'";
		StringBuilder sb = new StringBuilder();
		sb.append("select to_char(trunc(d.insertion_tm," + trimLevel + "),");
		sb.append(dateFormat);
		sb.append("), sum(quantity*sale_price) from oim_order_details d");
		sb.append(" inner join oim_orders o on d.order_id=o.order_id");
		sb.append(" inner join oim_order_batches b on b.batch_id = o.batch_id");
		sb.append(" inner join oim_channels c on c.channel_id = b.channel_id");
		sb.append(" where c.vendor_id =:vendorId");
		sb.append(" and d.insertion_tm between :startDate and :endDate");
		sb.append(" group by trunc(d.insertion_tm," + trimLevel
				+ ") order by trunc(d.insertion_tm," + trimLevel + ")");

		SQLQuery query = dbSession.createSQLQuery(sb.toString());
		query.setInteger("vendorId", vendorId);
		query.setTimestamp("startDate", m_startDate);
		query.setTimestamp("endDate", m_endDate);
		return query.list();
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
			Integer vendorId) {
		List<SupplierSalesData> supplierSales = new ArrayList<SupplierSalesData>();
		String dateSubQuery = " and od.oimOrders.insertionTm between to_date ('"
				+ dateToString(m_startDate)
				+ "', 'mm-dd-yyyy')AND to_date ('"
				+ dateToString(m_endDate) + "', 'mm-dd-yyyy') ";

		try {
			Query query = null;
			query = dbSession
					.createQuery("select s.supplierId, s.supplierName, sum(od.salePrice*od.quantity) "
							+ " from salesmachine.hibernatedb.OimOrderDetails od "
							+ " inner join od.oimSuppliers s "
							+ " where od.deleteTm is null "
							+ " and od.oimOrders.oimOrderBatches.oimChannels.vendors.vendorId =:vid "
							+ " and od.oimSuppliers.deleteTm is null and od.salePrice is not null "
							+ dateSubQuery
							// + " and od.oimOrderStatuses.statusId = 0 "
							+ " group by s.supplierId, s.supplierName "
							+ " order by sum(od.salePrice*od.quantity) desc");
			query.setInteger("vid", vendorId);

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
			Integer vendorId) {
		List<ProductSalesData> productSales = new ArrayList<ProductSalesData>();
		try {
			Query query = null;
			query = dbSession
					.createQuery("Select d.sku, sum(d.salePrice * d.quantity),sum(d.quantity) "
							+ " from salesmachine.hibernatedb.OimOrderDetails d"
							+ " where d.oimOrders.oimOrderBatches.oimChannels.vendors.vendorId =:vid "
							// + " and d.oimOrderStatuses.statusId = 0 "
							+ " and d.deleteTm is null and d.salePrice is not null "
							+ " and d.oimOrders.insertionTm between :m_startDate AND :m_endDate"
							+ " group by d.sku order by sum(d.salePrice * d.quantity) desc");

			query.setInteger("vid", vendorId);
			query.setTimestamp("m_startDate", m_startDate);
			query.setTimestamp("m_endDate", m_endDate);
			// }
			LOG.info(query.getQueryString());
			Iterator iter = query.iterate();
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
		OimSuppliers firstSupplier = null;
		boolean isUnresolved = false;
		for (OimOrders oimOrders : list) {
			if (oimOrders.getOimOrderDetailses() != null) {
				firstSupplier = null;
				isUnresolved = false;
				if (oimOrders.getDeliveryStateCode() == null)
					isUnresolved = true;
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
							|| details.getQuantity() == null
							|| (firstSupplier != null && !firstSupplier
									.getSupplierId().equals(
											details.getOimSuppliers()
													.getSupplierId()))) {
						isUnresolved = true;
						break;
					}
					firstSupplier = details.getOimSuppliers();
				}

				if (oimOrders.getOimShippingMethod() == null)
					isUnresolved = true;
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
				+ "where vendors=:v and processingTm is not null and processingTm>(sysdate-1) and deleteTm is null order by processingTm";
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
			if (errorCode.intValue() == Supplier.ERROR_PING_FAILURE) {
				errorCodeStr = "Supplier ("
						+ supplier.getSupplierName()
						+ ") can not be reached to post orders in their system.";
			} else if (errorCode.intValue() == Supplier.ERROR_UNCONFIGURED_SUPPLIER) {
				errorCodeStr = "Supplier (" + supplier.getSupplierName()
						+ ") is not configured properly to send orders.";
			} else if (errorCode.intValue() == Supplier.ERROR_ORDER_PROCESSING) {
				errorCodeStr = "You have some Failed orders that need your attention to resolve.";
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
			LOG.debug("Channel: " + cName + "(Id:" + cId + ")\tCount: " + cnt);
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

	@Override
	public ReportDataWrapper getSystemReportData(String reportType,
			Date startDate, Date endDate) {
		reportDataWrapper = new ReportDataWrapper();
		m_startDate = startDate;
		m_endDate = endDate;
		Session dbSession = SessionManager.currentSession();
		if (reportType == null)
			reportType = "";
		if ("channel-import".equalsIgnoreCase(reportType)) {
			List orderImport = getOrderImportData(dbSession);
			reportDataWrapper.put("order_import", orderImport);
		} else if (reportType.equalsIgnoreCase("supplier-processing")) {
			List orderProcessing = getOrderProcessingData(dbSession);
			reportDataWrapper.put("order_processing", orderProcessing);
		} else if (reportType.equalsIgnoreCase("order-tracking")) {
			List trackingData = getOrderTrackingData(dbSession);
			reportDataWrapper.put("order_tracking", trackingData);
		} else if (reportType.equalsIgnoreCase("order-summary")) {
			List orderSummaryData = getOrderImportSummary(dbSession);
			reportDataWrapper.put("order_summary", orderSummaryData);
		} else {
			List orderImport = getOrderImportData(dbSession);
			List orderProcesssing = getOrderProcessingData(dbSession);
			List trackingData = getOrderTrackingData(dbSession);
			List orderSummaryData = getOrderImportSummary(dbSession);

			reportDataWrapper.put("order_summary", orderSummaryData);
			reportDataWrapper.put("order_import", orderImport);
			reportDataWrapper.put("order_processing", orderProcesssing);
			reportDataWrapper.put("order_tracking", trackingData);

		}
		return reportDataWrapper;
	}

	private List getOrderTrackingData(Session dbSession) {
		StringBuilder sb = new StringBuilder();
		sb.append(
				"WITH pivot_data AS (select sc.channel_name,s.status_value, count(distinct o.order_id) order_count from oim_orders o inner join oim_order_details od on o.order_id=od.order_id ")
				.append("inner join oim_order_batches ob on o.batch_id = ob.batch_id ")
				.append("inner join oim_channels oc on ob.channel_id = oc.channel_id ")
				.append("inner join oim_supported_channels sc on oc.supported_channel_id = sc.supported_channel_id ")
				.append("inner join oim_order_statuses s on od.status_id = s.status_id ")
				.append("where sc.delete_tm is null and o.delete_tm is null and o.insertion_tm between :startDate and :endDate ")
				.append("group by sc.channel_name, s.status_value order by sc.channel_name , s.status_value) select * from pivot_data PIVOT (  sum(order_count)   for status_value in ('Unprocessed','Processed','Failed','Manually Processed','Canceled','Shipped'))");
		SQLQuery reportQuery = dbSession.createSQLQuery(sb.toString());
		reportQuery.setDate("startDate", m_startDate);
		reportQuery.setDate("endDate", m_endDate);
		List list = reportQuery.list();
		return list;
	}

	private List getOrderProcessingData(Session dbSession) {
		StringBuilder sb = new StringBuilder();
		sb.append(
				"WITH pivot_data AS (select os.supplier_name,s.status_value, count(distinct o.order_id) order_count from oim_orders o inner join oim_order_details od on o.order_id=od.order_id ")
				.append("inner join oim_suppliers os on od.supplier_id = os.supplier_id and os.delete_tm is null ")
				.append("inner join oim_order_statuses s on od.status_id = s.status_id ")
				.append("where o.delete_tm is null and o.insertion_tm between :startDate and :endDate ")
				.append("group by od.supplier_id,os.supplier_name,s.status_value order by os.supplier_name , s.status_value) select * from pivot_data PIVOT ( sum(order_count) for status_value in ('Unprocessed','Processed','Failed','Manually Processed','Canceled','Shipped'))");
		SQLQuery reportQuery = dbSession.createSQLQuery(sb.toString());
		reportQuery.setDate("startDate", m_startDate);
		reportQuery.setDate("endDate", m_endDate);
		List list = reportQuery.list();
		return list;
	}

	private List getOrderImportData(Session dbSession) {
		StringBuilder sb = new StringBuilder();
		sb.append(
				"WITH pivot_data AS (select sc.channel_name, bt.batch_type_name, count(order_id) order_count from oim_orders o inner join oim_order_batches ob on o.batch_id = ob.batch_id ")
				.append("inner join oim_order_batches_types bt on ob.batch_type_id = bt.batch_type_id ")
				.append("inner join oim_channels oc on ob.channel_id = oc.channel_id ")
				.append("inner join oim_supported_channels sc on oc.supported_channel_id = sc.supported_channel_id ")
				.append("where sc.delete_tm is null and o.delete_tm is null and o.insertion_tm between :startDate and :endDate ")
				.append("group by sc.channel_name,bt.batch_type_name) select * from pivot_data PIVOT ( sum(order_count) for  batch_type_name in ('Automated','Manual'))");
		SQLQuery reportQuery = dbSession.createSQLQuery(sb.toString());
		reportQuery.setDate("startDate", m_startDate);
		reportQuery.setDate("endDate", m_endDate);
		List list = reportQuery.list();
		return list;
	}

	private List getOrderImportSummary(Session dbSession) {
		StringBuilder sb = new StringBuilder(
				"select to_char(order_fetch_tm,'YYYY-MM-DD'), count(order_id) from oim_orders where order_fetch_tm between :startDate and :endDate  group by to_char(order_fetch_tm,'YYYY-MM-DD') order by to_char(order_fetch_tm,'YYYY-MM-DD')");
		SQLQuery reportQuery = dbSession.createSQLQuery(sb.toString());
		reportQuery.setDate("startDate", m_startDate);
		reportQuery.setDate("endDate", m_endDate);
		List list = reportQuery.list();
		return list;
	}

	@Override
	public PagedDataResult<VendorsuppOrderhistory> getVendorSupplierHistory(
			DataTableCriterias criterias) {

		Map<String, String> map = criterias.getFilters();
		String searchText = criterias.getSearch().get(SearchCriterias.value);

		String st = StringHandle.removeNull(map.get("startDate"));
		String ed = StringHandle.removeNull(map.get("endDate"));
		String errorCode = map.get("errorCode");
		Date startDate, endDate;
		try {
			startDate = df.parse(st);
			endDate = df.parse(ed);

		} catch (ParseException | NullPointerException e) {
			LOG.warn(e.getMessage());
			startDate = new Date();
			endDate = new Date();
		}
		endDate.setHours(23);
		endDate.setMinutes(59);
		endDate.setSeconds(59);
		int erroCodeNum = Integer.parseInt(errorCode);
		Session dbSession = SessionManager.currentSession();
		Criteria createCriteria = dbSession
				.createCriteria(OimVendorsuppOrderhistory.class)
				// .setFirstResult(pageNum).setMaxResults(recordCount)
				// .add(Restrictions.isNotNull("description"))
				.add(Restrictions.isNotNull("oimSuppliers"))
				.add(Restrictions.ge("processingTm", startDate))
				.add(Restrictions.le("processingTm", endDate))
				.add(Restrictions.eq("errorCode", erroCodeNum))
				// .add(Restrictions.le("processingTm", endDate))
				.addOrder(Order.desc("processingTm"));
		
		DetachedCriteria cr = DetachedCriteria.forClass(OimVendorsuppOrderhistory.class);
		ProjectionList projectionList = Projections.projectionList();
		projectionList.add(Projections.max("processingTm"));
		projectionList.add(Projections.groupProperty("errorCode"));
		projectionList.add(Projections.groupProperty("oimSuppliers.supplierId"));
		cr.setProjection(projectionList);
		createCriteria.add(Subqueries.propertiesIn(new String[] { "processingTm",
				"errorCode", "oimSuppliers.supplierId" }, cr));
		
		int recordsTotal = createCriteria.list().size();
		createCriteria.setFirstResult(criterias.getStart()).setMaxResults(
				criterias.getLength());
		List<VendorsuppOrderhistory> list = new ArrayList<VendorsuppOrderhistory>(
				recordsTotal);
		List<OimVendorsuppOrderhistory> list2 = createCriteria.list();
		for (OimVendorsuppOrderhistory object : list2) {
			VendorsuppOrderhistory e = new VendorsuppOrderhistory(object);
			list.add(e);
		}
		LOG.trace("History Size : {} and Elements {}", list.size(),
				list.toString());
		return new PagedDataResult<VendorsuppOrderhistory>(recordsTotal,
				recordsTotal, list);
	}

	private static final DateFormat df = new SimpleDateFormat("MM/dd/yyyy");

	@Override
	public List getSystemAlerts() {
		// StringBuilder sb = new StringBuilder(
		// "select error_code,count(error_code) from OIM_VENDORSUPP_ORDERHISTORY where error_code>0 and processing_tm > trunc(sysdate-2) group by error_code");
		StringBuilder sb = new StringBuilder(
				"select a.error_code, count(distinct a.supplier_id) from OIM_VENDORSUPP_ORDERHISTORY a where a.ERROR_CODE>0 and a.processing_tm>trunc(sysdate-2) group by a.ERROR_CODE");
		Session dbSession = SessionManager.currentSession();
		SQLQuery reportQuery = dbSession.createSQLQuery(sb.toString());
		List list = reportQuery.list();
		return list;
	}

	@Override
	public PagedDataResult<OrderBatch> getChannelPullHistory(
			DataTableCriterias criterias) {

		Map<String, String> map = criterias.getFilters();
		String searchText = criterias.getSearch().get(SearchCriterias.value);

		String st = StringHandle.removeNull(map.get("startDate"));
		String ed = StringHandle.removeNull(map.get("endDate"));
		String errorCode = map.get("errorCode");
		Date startDate, endDate;
		try {
			startDate = df.parse(st);
			endDate = df.parse(ed);

		} catch (ParseException | NullPointerException e) {
			LOG.warn(e.getMessage());
			startDate = new Date();
			endDate = new Date();
		}
		endDate.setHours(23);
		endDate.setMinutes(59);
		endDate.setSeconds(59);
		int errorCodeNum = Integer.parseInt(errorCode);
		Session dbSession = SessionManager.currentSession();
		Criteria criteria = dbSession.createCriteria(OimOrderBatches.class)
				.add(Restrictions.isNotNull("description"))
				.add(Restrictions.ge("insertionTm", startDate))
				.add(Restrictions.le("insertionTm", endDate))
				.add(Restrictions.eq("errorCode", errorCodeNum));

		DetachedCriteria cr = DetachedCriteria.forClass(OimOrderBatches.class);

		ProjectionList projectionList = Projections.projectionList();
		projectionList.add(Projections.max("insertionTm"));
		projectionList.add(Projections.groupProperty("errorCode"));
		projectionList.add(Projections.groupProperty("oimChannels.channelId"));
		cr.setProjection(projectionList);
		criteria.add(Subqueries.propertiesIn(new String[] { "insertionTm",
				"errorCode", "oimChannels.channelId" }, cr));

		int recordsTotal = criteria.list().size();
		criteria.setFirstResult(criterias.getStart()).setMaxResults(
				criterias.getLength());

		List<OrderBatch> batchesHistory = new ArrayList<OrderBatch>();
		List<OimOrderBatches> list = criteria.list();
		for (OimOrderBatches oimOrderBatches : list) {
			OrderBatch batch = OrderBatch.from(oimOrderBatches);
			batchesHistory.add(batch);
		}
		return new PagedDataResult<OrderBatch>(recordsTotal, recordsTotal,
				batchesHistory);
	}

	@Override
	public List getChannelAlerts() {
		StringBuilder sb = new StringBuilder(
				"select a.error_code, count(distinct a.CHANNEL_ID) from OIM_ORDER_BATCHES a where a.ERROR_CODE>0 and a.insertion_tm>trunc(sysdate-2) group by a.ERROR_CODE");
		Session dbSession = SessionManager.currentSession();
		SQLQuery reportQuery = dbSession.createSQLQuery(sb.toString());
		List list = reportQuery.list();
		return list;
	}
}
