package com.is.cm.core.event;

public class RequestReadEvent<T> {
	private final T entity;

	public RequestReadEvent(T entity) {
		this.entity = entity;
	}

	public RequestReadEvent() {
		this(null);
	}

	public T getEntity() {
		return entity;
	}
}
