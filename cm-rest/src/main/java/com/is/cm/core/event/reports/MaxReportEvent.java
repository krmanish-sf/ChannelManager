package com.is.cm.core.event.reports;

import com.is.cm.core.domain.ReportDataWrapper;
import com.is.cm.core.event.ReadEvent;

public class MaxReportEvent extends ReadEvent<ReportDataWrapper> {

	public MaxReportEvent(int id, ReportDataWrapper entitiy) {
		super(id, entitiy);
	}

}
