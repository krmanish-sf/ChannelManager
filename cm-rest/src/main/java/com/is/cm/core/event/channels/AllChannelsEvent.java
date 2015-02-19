package com.is.cm.core.event.channels;

import java.util.List;

import com.is.cm.core.domain.Channel;
import com.is.cm.core.event.ReadCollectionEvent;

public class AllChannelsEvent extends ReadCollectionEvent<Channel> {

	public AllChannelsEvent(List<Channel> entities) {
		super(entities);
	}

}
