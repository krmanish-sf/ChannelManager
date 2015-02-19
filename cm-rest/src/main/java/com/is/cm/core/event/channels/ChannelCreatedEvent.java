package com.is.cm.core.event.channels;

import com.is.cm.core.domain.Channel;
import com.is.cm.core.event.CreatedEvent;

public class ChannelCreatedEvent extends CreatedEvent<Channel> {

	public ChannelCreatedEvent(int newId, Channel entity) {
		super(newId, entity);
	}

}
