package com.is.cm.core.event;


public class ReadEvent<T> {
	protected boolean entityFound = true;
	private final T entity;
	private final int id;

	public ReadEvent(int id) {
		this(id, null);
	}

	public ReadEvent(int id, final T entitiy) {
		this.id = id;
		this.entity = entitiy;
	}

	public boolean isEntityFound() {
		return entityFound;
	}

	public static <E> ReadEvent<E> notFound(int id) {
		ReadEvent<E> ev = new ReadEvent<E>(id);
		ev.entityFound = false;
		return ev;
	}

	public int getId() {
		return id;
	}

	public T getEntity() {
		return entity;
	}
}
