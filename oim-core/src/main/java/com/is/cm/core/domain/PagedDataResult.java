package com.is.cm.core.domain;

import java.io.Serializable;
import java.util.List;

public class PagedDataResult<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	private final int pageNum, pageSize;
	private final long totalRecords;

	public PagedDataResult(int pageNum, int pageSize, long totalRecords,
			List<T> data) {
		this.totalRecords = totalRecords;
		this.pageNum = pageNum;
		this.pageSize = pageSize;
		this.data = data;
	}

	private final List<T> data;

	public int getPageSize() {
		return pageSize;
	}

	public int getPageNum() {
		return pageNum;
	}

	public long getTotalRecords() {
		return totalRecords;
	}

	public List<T> getData() {
		return data;
	}
}
