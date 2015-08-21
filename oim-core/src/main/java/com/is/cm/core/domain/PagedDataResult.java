package com.is.cm.core.domain;

import java.io.Serializable;
import java.util.List;

public class PagedDataResult<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	private final long recordsTotal, recordsFiltered;
	private int draw;
	private final List<T> data;

	public PagedDataResult(long recordsFiltered, long totalRecords, List<T> data) {
		this.recordsTotal = totalRecords;
		this.recordsFiltered = recordsFiltered;
		this.data = data;
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
