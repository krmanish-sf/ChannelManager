package com.is.cm.core.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.Reps;

import com.is.cm.core.event.ReadEvent;
import com.is.cm.core.event.RequestReadEvent;
import com.is.cm.core.persistance.UserRepository;

public class UserEventHandler implements UserService {
	private static final Logger LOG = LoggerFactory
			.getLogger(UserEventHandler.class);
	private final UserRepository userRepository;

	public UserEventHandler(final UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public ReadEvent<Reps> login(
			RequestReadEvent<Map<String, String>> userDetails) {
		LOG.debug("Verifying User Details");
		Reps login = userRepository.login(
				userDetails.getEntity().get("username"), userDetails
						.getEntity().get("password"));
		return new ReadEvent<Reps>(login == null ? 0 : login.getRepId(), login);
	}
}
