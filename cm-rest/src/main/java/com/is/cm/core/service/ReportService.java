package com.is.cm.core.service;

import java.util.Map;

import com.is.cm.core.domain.ReportDataWrapper;
import com.is.cm.core.event.ReadEvent;
import com.is.cm.core.event.RequestReadEvent;
import com.is.cm.core.event.reports.MaxReportEvent;
import com.is.cm.core.event.reports.RequestDownloadReportEvent;
import com.is.cm.core.event.reports.RequestMaxReportEvent;

public interface ReportService {
	MaxReportEvent getReport(RequestMaxReportEvent event);

	ReadEvent<String> getDownloadReportData(
			RequestDownloadReportEvent requestDownloadReportEvent);

	ReadEvent<Map<String, Map>> getNotifications(
			RequestReadEvent<Map<String, Map>> requestReadEvent);

	ReadEvent<ReportDataWrapper> getReportData(RequestDownloadReportEvent event);

	ReadEvent<ReportDataWrapper> getSystemReportData(
			RequestDownloadReportEvent event);
}
