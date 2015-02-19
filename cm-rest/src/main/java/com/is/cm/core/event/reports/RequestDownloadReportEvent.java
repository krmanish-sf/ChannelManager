package com.is.cm.core.event.reports;

import java.util.Date;

import com.is.cm.core.event.RequestReadEvent;

public class RequestDownloadReportEvent extends RequestReadEvent<String> {

	private final Date startDate;
	private final Date endDate;
	private final String reportType;

	public RequestDownloadReportEvent(final Date startDate, final Date endDate,
			final String reportType) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.reportType = reportType;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public String getReportType() {
		return reportType;
	}
}
