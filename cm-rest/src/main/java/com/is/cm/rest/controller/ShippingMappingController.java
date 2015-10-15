package com.is.cm.rest.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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

import com.is.cm.core.domain.ChannelShippingMap;
import com.is.cm.core.domain.OrderTracking;
import com.is.cm.core.domain.ShippingMethod;
import com.is.cm.core.event.CreateEvent;
import com.is.cm.core.event.CreatedEvent;
import com.is.cm.core.event.DeleteEvent;
import com.is.cm.core.event.DeletedEvent;
import com.is.cm.core.event.ReadCollectionEvent;
import com.is.cm.core.event.RequestReadEvent;
import com.is.cm.core.service.ShippingService;

@Controller
@RequestMapping("/aggregators/shipping")
public class ShippingMappingController {
	private static final Logger LOG = LoggerFactory
			.getLogger(ShippingMappingController.class);
	@Autowired
	private ShippingService shippingService;

	@RequestMapping(method = RequestMethod.GET, value = "/methods")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<ShippingMethod> getShippingMethods() {
		return shippingService.getShippingMethods();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{shippingMethodId}/{supportedChannelId}")
	public ResponseEntity<Collection<ChannelShippingMap>> viewChannelShipping(
			@PathVariable int supportedChannelId,
			@PathVariable int shippingMethodId) {
		Map<String, Integer> args = new HashMap<String, Integer>();
		args.put("SMID", shippingMethodId);
		args.put("SCID", supportedChannelId);
		ReadCollectionEvent<ChannelShippingMap> details = shippingService
				.findShippingMethods(new RequestReadEvent<Map<String, Integer>>(
						args));
		if (!details.isEntityFound()) {
			return new ResponseEntity<Collection<ChannelShippingMap>>(
					HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Collection<ChannelShippingMap>>(
				details.getEntity(), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/{shippingMethodId}/{supportedChannelId}")
	public ResponseEntity<ChannelShippingMap> createChannelShipping(
			@PathVariable int supportedChannelId,
			@PathVariable int shippingMethodId, @RequestBody String regex) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("SMID", shippingMethodId);
		args.put("SCID", supportedChannelId);
		args.put("REGEX", regex);
		CreatedEvent<ChannelShippingMap> details = shippingService
				.createShippingMethods(new CreateEvent<Map<String, Object>>(
						args));
		if (!details.isAlradyExists()) {
			return new ResponseEntity<ChannelShippingMap>(HttpStatus.FORBIDDEN);
		}
		return new ResponseEntity<ChannelShippingMap>(details.getEntity(),
				HttpStatus.OK);
	}
	
	  @RequestMapping(method = RequestMethod.DELETE, value = "/deleteShipping/{shippingMethodId}")
	    public ResponseEntity<OrderTracking> deleteChannelShippingMapping(
	      @PathVariable String shippingMethodId) {
	  LOG.debug("Recieved request to delete shipping Mapping for {}", shippingMethodId);
	  DeletedEvent<OrderTracking> deletedEvent = shippingService
	    .deleteChannelShippingMapping(new DeleteEvent<ChannelShippingMap>(Integer
	      .parseInt(shippingMethodId)));
	  if (!deletedEvent.isEntityFound()) {
	      return new ResponseEntity<OrderTracking>(HttpStatus.NOT_FOUND);
	  }
	  if (deletedEvent.isDeletionCompleted()) {
	      return new ResponseEntity<OrderTracking>(deletedEvent.getEntity(),
	        HttpStatus.OK);
	  }
	  LOG.debug("Delete failed for Entity:{} with Id:{}", deletedEvent
	    .getEntity().getClass(), deletedEvent.getEntity());
	  return new ResponseEntity<OrderTracking>(deletedEvent.getEntity(),
	    HttpStatus.FORBIDDEN);
	    }
	
	
}
