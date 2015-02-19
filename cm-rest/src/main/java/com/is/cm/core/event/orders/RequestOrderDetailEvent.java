package com.is.cm.core.event.orders;

import com.is.cm.core.event.RequestReadEvent;

public class RequestOrderDetailEvent extends RequestReadEvent {

	private final int id;

	public RequestOrderDetailEvent(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
