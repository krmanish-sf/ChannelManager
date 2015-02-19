package com.is.cm.core.event;

import java.util.List;

public class ReadCollectionEvent<T> {
	private final List<T> entities;

	public ReadCollectionEvent(List<T> entities) {
		this.entities = entities;
	}

	public List<T> getEntity() {
		return entities;
	}

	public boolean isEntityFound() {
		return entities != null;
	}
}
