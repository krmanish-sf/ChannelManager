package com.is.cm.core.event;

import com.is.cm.core.domain.PagedDataResult;

public class PagedDataResultEvent<T> {

	private final PagedDataResult<T> entity;

	public PagedDataResultEvent(PagedDataResult<T> entity) {
		this.entity = entity;
	}

	public PagedDataResult<T> getEntity() {
		return entity;
	}

}
