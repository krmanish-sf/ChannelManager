package com.is.cm.core.event;

public class DeleteEvent<T> {
	private final int id;

	public DeleteEvent(final int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

}
