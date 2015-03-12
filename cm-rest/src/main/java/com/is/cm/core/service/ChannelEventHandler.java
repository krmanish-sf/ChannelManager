package com.is.cm.core.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimChannelShippingMap;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.util.OimLogStream;

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
import com.is.cm.core.persistance.ChannelRepository;

public class ChannelEventHandler implements ChannelService {
	private final static Logger LOG = LoggerFactory
			.getLogger(ChannelEventHandler.class);
	private final ChannelRepository channelRepository;

	public ChannelEventHandler(ChannelRepository repo) {
		channelRepository = repo;
	}

	@Override
	public AllChannelsEvent getChannels(RequestAllChannelsEvent event) {
		return new AllChannelsEvent(channelRepository.findAll());
	}

	@Override
	public ChannelDetailsEvent getChannel(
			RequestChannelDetailEvent requestChannelDetailEvent) {
		Channel channel = channelRepository.findById(requestChannelDetailEvent
				.getId());
		return channel == null ? ChannelDetailsEvent
				.notFound(requestChannelDetailEvent.getId())
				: new ChannelDetailsEvent(requestChannelDetailEvent.getId(),
						channel);
	}

	@Override
	public ChannelDeletedEvent deleteChannel(
			DeleteChannelEvent deleteChannelEvent) {
		channelRepository.delete(deleteChannelEvent.getId());
		// FIXME::
		return new ChannelDeletedEvent(deleteChannelEvent.getId(),
				new Channel());
	}

	@Override
	public ReadEvent<String> pullOrders(ReadEvent<Channel> readEvent) {
		return new ReadEvent<String>(readEvent.getId(),
				pullOrders(readEvent.getId()));
	}

	@Override
	public UpdatedEvent<Channel> update(UpdateEvent<Map<String, String>> event) {
		Channel entity = channelRepository.save(event.getId(),
				event.getEntity());
		return new UpdatedEvent<Channel>(entity.getChannelId(), entity);
	}

	@Override
	public CreatedEvent<Channel> create(
			CreateEvent<Map<String, String>> createEvent) {
		Channel channel = channelRepository.findByName(createEvent.getEntity()
				.get("channelname"));
		if (channel != null) {
			return CreatedEvent.AlreadyExists(channel.getChannelId(), channel);
		}
		Channel entity = channelRepository.save(0, createEvent.getEntity());
		return new CreatedEvent<Channel>(entity.getChannelId(), entity);
	}

	@Override
	public ReadEvent<List<Filetype>> getFileTypes(ReadEvent<Integer> event) {
		List<Filetype> fileTypes = channelRepository
				.getFileTypes(event.getId());
		return new ReadEvent<List<Filetype>>(event.getId(), fileTypes);
	}

	@Override
	public ReadEvent<List<UploadedFile>> getUploadedFiles(
			ReadEvent<Integer> event) {
		List<UploadedFile> fileTypes = channelRepository
				.getOimUploadedFiles(event.getId());
		return new ReadEvent<List<UploadedFile>>(event.getId(), fileTypes);
	}

	@Override
	public ReadEvent<String> pullOrders(
			RequestReadEvent<Channel> requestReadEvent) {
		StringBuilder sb = new StringBuilder();
		List<Channel> findAll = channelRepository.findAll();
		for (Channel channel : findAll) {
			sb.append(pullOrders(channel.getChannelId()));
		}
		return new ReadEvent<String>(0, sb.toString());
	}

	private String pullOrders(int channelId) {
		Channel channel = channelRepository.findById(channelId);
		String channelName = channel.getChannelName();
		LOG.debug("Supported channel : " + channelName);
		String orderFetchBean = channel.getOimSupportedChannels()
				.getOrderFetchBean();
		IOrderImport coi = null;
		if (orderFetchBean != null && orderFetchBean.length() > 0) {
			try {
				Class<?> theClass = Class.forName(orderFetchBean);
				coi = (IOrderImport) theClass.newInstance();
			} catch (Exception cnfe) {
				LOG.error("Import Order", cnfe);
				coi = null;
			}
		}
		OimLogStream stream = new OimLogStream();
		stream.println(channelName + " : ");
		if (coi != null) {
			LOG.debug("Created the iorderimport object");
			if (!coi.init(channelId, SessionManager.currentSession(), stream)) {
				LOG.debug("Failed initializing the channel with Id:{}",
						channelId);
			} else {
				LOG.debug("Pulling orders for channel id: {}", channelId);
				try {
					coi.getVendorOrders();
				} catch (Throwable e) {
					LOG.error("Error in pulling orders for channel id: {}",
							channelId, e);
					stream.println("Error in pulling orders from channel.");
				}
			}
		} else {
			LOG.error("Could not find a bean to work with this Channel.");
			stream.println("This Channel type is not supported for pulling orders from.");
		}
		stream.println("<br>");
		return stream.toString();
	}

	@Override
	public ReadCollectionEvent<ChannelShippingMap> findShippingMethods(
			RequestReadEvent<Integer> requestReadEvent) {
		List<ChannelShippingMap> findShippingMapping = channelRepository
				.findShippingMapping(requestReadEvent.getEntity());
		return new ReadCollectionEvent<ChannelShippingMap>(findShippingMapping);
	}

	@Override
	public ReadCollectionEvent<SupportedChannel> getSupportedChannels(
			RequestReadEvent<SupportedChannel> requestReadEvent) {
		List<SupportedChannel> supportedChannels = channelRepository
				.findSupportedChannels();
		return new ReadCollectionEvent<SupportedChannel>(supportedChannels);
	}
}
