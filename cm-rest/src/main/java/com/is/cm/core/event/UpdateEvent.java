package com.is.cm.core.event;

public class UpdateEvent<T> {
	private final T entity;
	private final int id;

	public UpdateEvent(int id, T entity) {
		this.id = id;
		this.entity = entity;
	}

	public T getEntity() {
		return entity;
	}

	public int getId() {
		return id;
	}
}
