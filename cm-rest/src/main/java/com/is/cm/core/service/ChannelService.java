package com.is.cm.core.service;

import java.util.List;
import java.util.Map;

import com.is.cm.core.domain.Channel;
import com.is.cm.core.domain.ChannelShippingMap;
import com.is.cm.core.domain.Filetype;
import com.is.cm.core.domain.SupportedChannel;
import com.is.cm.core.domain.UploadedFile;
import com.is.cm.core.event.CreateEvent;
import com.is.cm.core.event.CreatedEvent;
import com.is.cm.core.event.ReadCollectionEvent;
import com.is.cm.core.event.ReadEvent;
import com.is.cm.core.event.RequestReadEvent;
import com.is.cm.core.event.UpdateEvent;
import com.is.cm.core.event.UpdatedEvent;
import com.is.cm.core.event.channels.AllChannelsEvent;
import com.is.cm.core.event.channels.ChannelDeletedEvent;
import com.is.cm.core.event.channels.ChannelDetailsEvent;
import com.is.cm.core.event.channels.DeleteChannelEvent;
import com.is.cm.core.event.channels.RequestAllChannelsEvent;
import com.is.cm.core.event.channels.RequestChannelDetailEvent;

public interface ChannelService {
	AllChannelsEvent getChannels(RequestAllChannelsEvent event);

	ChannelDetailsEvent getChannel(
			RequestChannelDetailEvent requestChannelDetailEvent);

	ChannelDeletedEvent deleteChannel(DeleteChannelEvent deleteChannelEvent);

	ReadEvent<String> pullOrders(ReadEvent<Channel> readEvent);

	UpdatedEvent<Channel> update(UpdateEvent<Map<String, String>> event);

	CreatedEvent<Channel> create(CreateEvent<Map<String, String>> createEvent);

	ReadEvent<List<Filetype>> getFileTypes(ReadEvent<Integer> event);

	ReadEvent<List<UploadedFile>> getUploadedFiles(ReadEvent<Integer> event);

	ReadEvent<String> pullOrders(RequestReadEvent<Channel> requestReadEvent);

	ReadCollectionEvent<ChannelShippingMap> findShippingMethods(
			RequestReadEvent<Integer> requestReadEvent);

	ReadCollectionEvent<SupportedChannel> getSupportedChannels(
			RequestReadEvent<SupportedChannel> requestReadEvent);

	ReadEvent<Map<String, String>> getBigcommerceAuthDetailsByUrl(RequestReadEvent<String> requestReadEvent);
}
