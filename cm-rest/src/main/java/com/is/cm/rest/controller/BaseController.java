package com.is.cm.rest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.is.cm.core.domain.DomainBase;
import com.is.cm.core.event.DeletedEvent;
import com.is.cm.core.event.UpdatedEvent;

@Controller
public class BaseController {
	private static Logger LOG = LoggerFactory.getLogger(BaseController.class);

	public static <T extends DomainBase> ResponseEntity<T> createResponseBody(
			DeletedEvent<T> deletedEvent) {
		if (!deletedEvent.isEntityFound()) {
			return new ResponseEntity<T>(HttpStatus.NOT_FOUND);
		}
		if (deletedEvent.isDeletionCompleted()) {
			return new ResponseEntity<T>(deletedEvent.getEntity(),
					HttpStatus.OK);
		}
		LOG.debug("Delete failed for Entity:{} with Id:{}", deletedEvent
				.getEntity().getClass(), deletedEvent.getEntity());
		return new ResponseEntity<T>(deletedEvent.getEntity(),
				HttpStatus.FORBIDDEN);
	}

	public static <T extends DomainBase> ResponseEntity<T> createResponseBody(
			UpdatedEvent<T> updatedEvent) {
		if (!updatedEvent.isEntityFound()) {
			return new ResponseEntity<T>(HttpStatus.NOT_FOUND);
		}
		if (updatedEvent.isUpdateCompleted()) {
			return new ResponseEntity<T>(updatedEvent.getEntity(),
					HttpStatus.OK);
		}
		LOG.debug("Update failed for Entity:{} with Id:{}", updatedEvent
				.getEntity().getClass(), updatedEvent.getEntity());
		return new ResponseEntity<T>(updatedEvent.getEntity(),
				HttpStatus.FORBIDDEN);
	}
}
