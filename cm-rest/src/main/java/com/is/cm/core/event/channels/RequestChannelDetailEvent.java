package com.is.cm.core.event.channels;

import com.is.cm.core.event.RequestReadEvent;

public class RequestChannelDetailEvent extends RequestReadEvent {

	private final int id;

	public RequestChannelDetailEvent(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
