package com.is.cm.core.event;

import com.is.cm.core.domain.DomainBase;

public class CreatedEvent<T> {
	private final T entity;
	private final int newId;
	private boolean alradyExists = false;

	public CreatedEvent(int newId, T entity) {
		this.newId = newId;
		this.entity = entity;
	}

	public T getEntity() {
		return entity;
	}

	public int getNewId() {
		return newId;
	}

	public static <S extends DomainBase> CreatedEvent<S> AlreadyExists(int id,
			S entity) {
		CreatedEvent<S> createdEvent = new CreatedEvent<S>(id, entity);
		createdEvent.alradyExists = true;
		return createdEvent;
	}

	public boolean isAlradyExists() {
		return alradyExists;
	}
}
