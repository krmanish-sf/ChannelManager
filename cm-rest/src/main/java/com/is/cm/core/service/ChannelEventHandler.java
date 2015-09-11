package com.is.cm.core.service;

import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderBatchesTypes;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.stores.impl.ChannelFactory;
import salesmachine.util.OimLogStream;

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
	OimChannels channel = channelRepository
		.findById(requestChannelDetailEvent.getId());
	return channel == null
		? ChannelDetailsEvent
			.notFound(requestChannelDetailEvent.getId())
		: new ChannelDetailsEvent(requestChannelDetailEvent.getId(),
			Channel.from(channel));
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
    public UpdatedEvent<Channel> update(
	    UpdateEvent<Map<String, String>> event) {
	Channel entity = channelRepository.save(event.getId(),
		event.getEntity());
	return new UpdatedEvent<Channel>(entity.getChannelId(), entity);
    }

    @Override
    public CreatedEvent<Channel> create(
	    CreateEvent<Map<String, String>> createEvent) {
	Channel channel = channelRepository
		.findByName(createEvent.getEntity().get("channelname"));
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
	OimChannels channel = channelRepository.findById(channelId);

	OimLogStream stream = new OimLogStream();
	stream.println(channel.getChannelName() + " : ");

	LOG.debug("Created the iorderimport object");
	OimOrderBatches oimOrderBatches = new OimOrderBatches();
	OimOrderBatchesTypes oimOrderBatchesTypes = new OimOrderBatchesTypes(
		OimConstants.ORDERBATCH_TYPE_ID_MANUAL);
	try {
	    IOrderImport coi = ChannelFactory.getIOrderImport(channel);
	    LOG.debug("Pulling orders for channel id: {}", channelId);
	    // coi.getVendorOrders(new OimOrderBatchesTypes(
	    // OimConstants.ORDERBATCH_TYPE_ID_MANUAL));
	    coi.getVendorOrders(oimOrderBatchesTypes, oimOrderBatches);
	    stream.println("Pulled " + oimOrderBatches.getOimOrderses().size()
		    + " orders.");

	} catch (RuntimeException e) {
	    LOG.error("Error in pulling orders for channel id: {}", channelId,
		    e);
	    stream.println("Error in pulling orders from channel.");
	} catch (ChannelConfigurationException e) {
	    LOG.error("Error in pulling orders for channel id: {}", channelId,
		    e);
	    stream.println(e.getMessage());
	    oimOrderBatches.setDescription(
		    "Error occured in pulling order due to ChannelConfiguration Error."
			    + e.getMessage());
	    oimOrderBatches
		    .setErrorCode(ChannelCommunicationException.getErrorcode());
	} catch (ChannelCommunicationException e) {
	    LOG.error("Error in pulling orders for channel id: {}", channelId,
		    e);
	    stream.println(e.getMessage());
	    oimOrderBatches.setDescription(
		    "Error occured in pulling order due to ChannelComunication Error."
			    + e.getMessage());
	    oimOrderBatches
		    .setErrorCode(ChannelCommunicationException.getErrorcode());
	} catch (ChannelOrderFormatException e) {
	    LOG.error("Error in pulling orders for channel id: {}", channelId,
		    e);
	    stream.println(e.getMessage());
	    oimOrderBatches.setDescription(
		    "Error occured in pulling order due to ChannelOrderFormat Error."
			    + e.getMessage());
	    oimOrderBatches
		    .setErrorCode(ChannelOrderFormatException.getErrorcode());
	} finally {
	    Session m_dbSession = SessionManager.currentSession();
	    Transaction tx = m_dbSession.getTransaction();
	    if (tx != null && tx.isActive())
		tx.commit();
	    tx = m_dbSession.beginTransaction();

	    m_dbSession.save(oimOrderBatches);
	    tx.commit();
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

    @Override
    public ReadEvent<Map<String, String>> getBigcommerceAuthDetailsByUrl(
	    RequestReadEvent<String> requestReadEvent) {
	Map<String, String> authDetails = channelRepository
		.findBigcommerceAuthDetailsByUrl(requestReadEvent.getEntity());
	return new ReadEvent<Map<String, String>>(0, authDetails);
    }
}
