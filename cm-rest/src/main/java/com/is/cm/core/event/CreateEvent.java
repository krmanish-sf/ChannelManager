package com.is.cm.core.event;

public class CreateEvent<T> {
	private final T entity;

	public CreateEvent(T entity) {
		this.entity = entity;
	}

	public T getEntity() {
		return entity;
	}
}
