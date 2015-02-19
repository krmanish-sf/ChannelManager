package com.is.cm.core.event;

public class UpdatedEvent<T> {
	protected boolean entityFound = true;
	private boolean updateCompleted;
	private final int id;
	private T entity;

	public UpdatedEvent(final int id) {
		this.id = id;
	}

	public UpdatedEvent(int id, T entity) {
		this.id = id;
		this.entity = entity;
		this.updateCompleted = true;
	}

	public boolean isEntityFound() {
		return entityFound;
	}

	public boolean isUpdateCompleted() {
		return updateCompleted;
	}

	public static <E> UpdatedEvent<E> updateForbidden(int key, E entity) {
		UpdatedEvent<E> ev = new UpdatedEvent<E>(key, entity);
		ev.entityFound = true;
		ev.updateCompleted = false;
		return ev;
	}

	public static <E> UpdatedEvent<E> notFound(int key) {
		UpdatedEvent<E> ev = new UpdatedEvent<E>(key);
		ev.entityFound = false;
		return ev;
	}

	public T getEntity() {
		return entity;
	}

	public int getId() {
		return id;
	}
}
