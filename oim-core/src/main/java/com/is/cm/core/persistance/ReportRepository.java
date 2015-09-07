package com.is.cm.core.persistance;

import java.util.Date;
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

	List<VendorsuppOrderhistory> getVendorSupplierHistory(int pageNum,
			int recordCount, Date startDate, Date endDate);

	List getSystemAlerts();

	PagedDataResult<OrderBatch> getChannelPullHistory(
			DataTableCriterias criterias);

	List getChannelAlerts();
}
