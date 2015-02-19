package com.is.cm.core.event.channels;

import com.is.cm.core.domain.Channel;
import com.is.cm.core.event.CreateEvent;

public class CreateChannelEvent extends CreateEvent<Channel> {

	public CreateChannelEvent(Channel entity) {
		super(entity);
	}

}
