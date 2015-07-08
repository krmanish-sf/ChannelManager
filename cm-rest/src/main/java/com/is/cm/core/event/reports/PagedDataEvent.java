package com.is.cm.core.event.reports;

import java.util.Date;
import java.util.Map;

import com.is.cm.core.event.RequestReadEvent;

public class PagedDataEvent<T> extends RequestReadEvent<T> {
	private final int pageNum, recordCount;
	private final Map<String, Date> dateRange;

	public PagedDataEvent(int pageNum, int recordCount,
			Map<String, Date> dateRange) {
		this.pageNum = pageNum;
		this.recordCount = recordCount;
		this.dateRange = dateRange;
	}

	public int getRecordCount() {
		return recordCount;
	}

	public int getPageNum() {
		return pageNum;
	}

	public Map<String, Date> getDateRange() {
		return dateRange;
	}
}
