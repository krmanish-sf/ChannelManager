package com.is.cm.core.event.channels;

import com.is.cm.core.domain.Channel;
import com.is.cm.core.event.DeletedEvent;

public class ChannelDeletedEvent extends DeletedEvent<Channel> {

	public ChannelDeletedEvent(int id) {
		super(id);
	}

	public ChannelDeletedEvent(int id, Channel entity) {
		super(id, entity);
	}
}
