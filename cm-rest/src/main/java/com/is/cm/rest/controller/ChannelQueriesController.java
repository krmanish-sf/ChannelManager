package com.is.cm.rest.controller;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.is.cm.core.domain.Channel;
import com.is.cm.core.domain.ChannelShippingMap;
import com.is.cm.core.domain.SupportedChannel;
import com.is.cm.core.event.ReadCollectionEvent;
import com.is.cm.core.event.ReadEvent;
import com.is.cm.core.event.RequestReadEvent;
import com.is.cm.core.event.channels.AllChannelsEvent;
import com.is.cm.core.event.channels.ChannelDetailsEvent;
import com.is.cm.core.event.channels.RequestAllChannelsEvent;
import com.is.cm.core.event.channels.RequestChannelDetailEvent;
import com.is.cm.core.service.ChannelService;

@Controller
@RequestMapping("/aggregators/channels")
public class ChannelQueriesController {
	private static Logger LOG = LoggerFactory
			.getLogger(ChannelQueriesController.class);

	@Autowired
	private ChannelService channelService;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Collection<Channel> getAllChannels() {
		LOG.debug("Getting all channels...");
		AllChannelsEvent details = channelService
				.getChannels(new RequestAllChannelsEvent());
		return details.getEntity();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{id}")
	public ResponseEntity<Channel> viewChannel(@PathVariable String id) {

		ChannelDetailsEvent details = channelService
				.getChannel(new RequestChannelDetailEvent(Integer.parseInt(id)));

		if (!details.isEntityFound()) {
			return new ResponseEntity<Channel>(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<Channel>(details.getEntity(), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/shipping/{supportedChannelId}")
	public ResponseEntity<Collection<ChannelShippingMap>> viewChannelShipping(
			@PathVariable int supportedChannelId) {
		ReadCollectionEvent<ChannelShippingMap> details = channelService
				.findShippingMethods(new RequestReadEvent<Integer>(
						supportedChannelId));
		if (!details.isEntityFound()) {
			return new ResponseEntity<Collection<ChannelShippingMap>>(
					HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Collection<ChannelShippingMap>>(
				details.getEntity(), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/supported-channels")
	public ResponseEntity<Collection<SupportedChannel>> getSupportedChannels() {
		ReadCollectionEvent<SupportedChannel> details = channelService
				.getSupportedChannels(new RequestReadEvent<SupportedChannel>());
		if (!details.isEntityFound()) {
			return new ResponseEntity<Collection<SupportedChannel>>(
					HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Collection<SupportedChannel>>(
				details.getEntity(), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/bc-app")
	public ResponseEntity<Map<String, String>> getBigCommerceAuth(
			@RequestBody String storeUrl) {
		ReadEvent<Map<String, String>> authData = channelService
				.getBigcommerceAuthDetailsByUrl(new RequestReadEvent<String>(
						storeUrl));
		return new ResponseEntity<Map<String, String>>(authData.getEntity(),
				HttpStatus.OK);
	}
}
