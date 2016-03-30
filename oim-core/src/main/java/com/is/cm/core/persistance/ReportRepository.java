package com.is.cm.core.persistance;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.is.cm.core.domain.DataTableCriterias;
import com.is.cm.core.domain.OrderBatch;
import com.is.cm.core.domain.PagedDataResult;
import com.is.cm.core.domain.ReportDataWrapper;
import com.is.cm.core.domain.VendorsuppOrderhistory;

public interface ReportRepository {
	ReportDataWrapper getReportData(Date startDate, Date endDate);

	String getDownloadReportData(String reportType, Date startDate, Date endDate);

	Map<String, Map> getAlertAndErrors(int vendorId);

	List<?> getReportData(String reportType, Date startDate, Date endDate);

	ReportDataWrapper getReportData(Date startDate, Date endDate,
			String reportType);

	ReportDataWrapper getSystemReportData(String reportType, Date startDate,
			Date endDate);

	PagedDataResult<VendorsuppOrderhistory> getVendorSupplierHistory(DataTableCriterias criterias);

	List getSystemAlerts();

	PagedDataResult<OrderBatch> getChannelPullHistory(
			DataTableCriterias criterias);

	List getChannelAlerts();

	List<HashMap<String, Integer>> getOrderHistory();

  List fetchUnShippedOrders(int vendorID);

  List fetchUnConfirmedOrders(int vendorID);

  List trackOrderFileLocation(String vid, String poNumber, String location, String detailId);
}
