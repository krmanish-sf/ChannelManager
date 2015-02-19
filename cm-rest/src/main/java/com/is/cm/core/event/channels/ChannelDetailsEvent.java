package com.is.cm.core.event.channels;

import com.is.cm.core.domain.Channel;
import com.is.cm.core.event.ReadEvent;

public class ChannelDetailsEvent extends ReadEvent<Channel> {

	public ChannelDetailsEvent(int id) {
		this(id, null);
	}

	public ChannelDetailsEvent(int id, Channel entity) {
		super(id, entity);
	}

	public static ChannelDetailsEvent notFound(int id) {
		return new ChannelDetailsEvent(id);
	}
}
