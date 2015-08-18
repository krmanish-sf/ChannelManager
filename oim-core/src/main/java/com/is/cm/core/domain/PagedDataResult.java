package com.is.cm.core.domain;

import java.io.Serializable;
import java.util.List;

public class PagedDataResult<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	private final int pageNum, pageSize;
	private final long recordsTotal, recordsFiltered;
	private int draw;
	private final List<T> data;

	public PagedDataResult(int pageNum, int pageSize, long totalRecords,
			List<T> data) {
		this.recordsTotal = totalRecords;
		this.recordsFiltered = totalRecords;
		this.pageNum = pageNum;
		this.pageSize = pageSize;
		this.data = data;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getPageNum() {
		return pageNum;
	}

	public long getRecordsTotal() {
		return recordsTotal;
	}

	public List<T> getData() {
		return data;
	}

	public int getDraw() {
		return draw;
	}

	public void setDraw(int draw) {
		this.draw = draw;
	}

	public long getRecordsFiltered() {
		return recordsFiltered;
	}
}
