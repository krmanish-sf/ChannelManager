package com.is.cm.core.event.channels;

import com.is.cm.core.domain.Channel;
import com.is.cm.core.event.DeleteEvent;

public class DeleteChannelEvent extends DeleteEvent<Channel> {

	public DeleteChannelEvent(int id) {
		super(id);
	}

}
