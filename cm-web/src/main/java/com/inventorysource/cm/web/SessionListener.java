package com.inventorysource.cm.web;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.Reps;

public class SessionListener implements HttpSessionListener {
	private static final Logger log = LoggerFactory
			.getLogger(SessionListener.class);
	private int sessionCount = 0;
	private static final Map<String, HttpSession> sessionMap = new ConcurrentHashMap<String, HttpSession>();;

	public static final HttpSession get(String sessionId) {
		return sessionMap.get(sessionId);
	}

	public static final Reps getRep(String sessionId) {
		HttpSession httpSession = get(sessionId);
		if (httpSession != null) {
			Object attribute = httpSession.getAttribute("reps");
			if (attribute instanceof Reps) {
				return (Reps) attribute;
			}
		}
		return null;
	}

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		HttpSession session = se.getSession();
		// session.setMaxInactiveInterval(60);
		String id = session.getId();
		synchronized (this) {
			sessionMap.put(id, session);
			sessionCount++;
		}

		Date now = new Date();
		String message = new StringBuffer("Session ").append(id)
				.append(" created at ").append(now.toString())
				.append(". Total ").append(sessionCount)
				.append(" live sessions.").toString();
		log.debug(message);
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {

		HttpSession session = se.getSession();
		String id = session.getId();
		synchronized (this) {
			sessionMap.remove(id);
			--sessionCount;
		}
		StringBuffer message = new StringBuffer("Session: {").append("" + id)
				.append("} destroyed.").append(" There are now ")
				.append("" + sessionCount).append(" live sessions.");
		log.info(message.toString());
	}
}
