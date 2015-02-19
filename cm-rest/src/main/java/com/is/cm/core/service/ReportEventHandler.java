package com.is.cm.core.service;

import java.util.Map;

import com.is.cm.core.domain.ReportDataWrapper;
import com.is.cm.core.event.ReadEvent;
import com.is.cm.core.event.RequestReadEvent;
import com.is.cm.core.event.reports.MaxReportEvent;
import com.is.cm.core.event.reports.RequestDownloadReportEvent;
import com.is.cm.core.event.reports.RequestMaxReportEvent;
import com.is.cm.core.persistance.ReportRepository;

public class ReportEventHandler implements ReportService {

	private final ReportRepository reportRepository;

	public ReportEventHandler(final ReportRepository reportRepository) {
		this.reportRepository = reportRepository;
	}

	@Override
	public MaxReportEvent getReport(RequestMaxReportEvent event) {
		ReportDataWrapper reportData = reportRepository.getReportData(
				event.getStartDate(), event.getEndDate());
		return new MaxReportEvent(1, reportData);
	}

	@Override
	public ReadEvent<String> getDownloadReportData(
			RequestDownloadReportEvent event) {
		String data = reportRepository
				.getDownloadReportData(event.getReportType(),
						event.getStartDate(), event.getEndDate());
		if (data.length() <= 0)
			return new ReadEvent<String>(0);
		else
			return new ReadEvent<String>(0, data);

	}

	@Override
	public ReadEvent<ReportDataWrapper> getReportData(
			RequestDownloadReportEvent event) {
		ReportDataWrapper data = reportRepository
				.getReportData(event.getStartDate(), event.getEndDate(),
						event.getReportType());
		return new ReadEvent<ReportDataWrapper>(0, data);
	}

	@Override
	public ReadEvent<Map<String, Map>> getNotifications(
			RequestReadEvent<Map<String, Map>> requestReadEvent) {
		int vendorId = 0;
		Map<String, Map> alertAndErrors = reportRepository
				.getAlertAndErrors(vendorId);
		return new ReadEvent<Map<String, Map>>(0, alertAndErrors);
	}
}
