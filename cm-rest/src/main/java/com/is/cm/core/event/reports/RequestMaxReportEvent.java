package com.is.cm.core.event.reports;

import java.util.Date;

import com.is.cm.core.domain.ReportDataWrapper;
import com.is.cm.core.event.RequestReadEvent;

public class RequestMaxReportEvent extends RequestReadEvent<ReportDataWrapper> {
	private final Date startDate;
	private final Date endDate;

	public RequestMaxReportEvent(final Date startDate, final Date endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}
}
