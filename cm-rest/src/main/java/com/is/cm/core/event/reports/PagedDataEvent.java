package com.is.cm.core.event.reports;

import com.is.cm.core.event.RequestReadEvent;

public class PagedDataEvent<T> extends RequestReadEvent<T> {
	private final int pageNum, recordCount;

	public PagedDataEvent(int pageNum, int recordCount) {
		this.pageNum = pageNum;
		this.recordCount = recordCount;
	}

	public int getRecordCount() {
		return recordCount;
	}

	public int getPageNum() {
		return pageNum;
	}
}
