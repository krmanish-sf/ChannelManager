package com.is.cm.core.service;

import java.util.Map;

import salesmachine.hibernatedb.Reps;

import com.is.cm.core.event.ReadEvent;
import com.is.cm.core.event.RequestReadEvent;

public interface UserService {
	ReadEvent<Reps> login(RequestReadEvent<Map<String,String>> userDetails);
}
