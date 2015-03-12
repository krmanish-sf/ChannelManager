package com.is.cm.core.persistance;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.is.cm.core.domain.ReportDataWrapper;
import com.is.cm.core.persistance.ReportRepositoryDB.OverAllSalesData;

public interface ReportRepository {
	ReportDataWrapper getReportData(Date startDate, Date endDate);

	String getDownloadReportData(String reportType, Date startDate, Date endDate);

	Map<String, Map> getAlertAndErrors(int vendorId);

	List<?> getReportData(String reportType, Date startDate, Date endDate);

	ReportDataWrapper getReportData(Date startDate, Date endDate,
			String reportType);

	List<OverAllSalesData> getOverallSalesData(Integer vendorId,
			int supplierId, int channelId, Date startDate, Date endDate);
}
