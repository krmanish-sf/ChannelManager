package com.is.cm.rest.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import salesmachine.hibernatedb.Reps;

import com.is.cm.core.event.ReadEvent;
import com.is.cm.core.event.RequestReadEvent;
import com.is.cm.core.service.UserService;

@Controller
@RequestMapping("/login")
public class UserAuthenticationController {

	private static Logger LOG = LoggerFactory
			.getLogger(UserAuthenticationController.class);

	@Autowired
	UserService userService;

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<Reps> login(
			@RequestBody Map<String, String> loginDetails) {
		LOG.debug("Login request for:{}", loginDetails.get("username"));
		ReadEvent<Reps> login = userService
				.login(new RequestReadEvent<Map<String, String>>(loginDetails));
		return new ResponseEntity<Reps>(login.getEntity(), HttpStatus.OK);
	}

}
