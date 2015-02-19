package com.is.cm.core.event;

public class DeletedEvent<T> {
	protected boolean entityFound = true;
	private boolean deletionCompleted;
	private final int id;
	private T entity;

	public DeletedEvent(final int id) {
		this.id = id;
	}

	public DeletedEvent(int id, T entity) {
		this.id = id;
		this.entity = entity;
		this.deletionCompleted = true;
	}

	public boolean isEntityFound() {
		return entityFound;
	}

	public boolean isDeletionCompleted() {
		return deletionCompleted;
	}

	public static <E> DeletedEvent<E> deletionForbidden(int key, E entity) {
		DeletedEvent<E> ev = new DeletedEvent<E>(key, entity);
		ev.entityFound = true;
		ev.deletionCompleted = false;
		return ev;
	}

	public static <E> DeletedEvent<E> notFound(int key) {
		DeletedEvent<E> ev = new DeletedEvent<E>(key);
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
