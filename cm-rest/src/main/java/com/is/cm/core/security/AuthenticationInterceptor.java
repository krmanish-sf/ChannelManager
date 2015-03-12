package com.is.cm.core.security;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import salesmachine.hibernatehelper.SessionManager;

public class AuthenticationInterceptor implements HandlerInterceptor {
	private static final Logger LOG = LoggerFactory
			.getLogger(AuthenticationInterceptor.class);

	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1,
			Object arg2, ModelAndView arg3) throws Exception {
		// DO NOTHING
	}

	@Override
	public boolean preHandle(HttpServletRequest req, HttpServletResponse res,
			Object arg2) throws Exception {

		if ("/login".equalsIgnoreCase(req.getServletPath()))
			return true;
		else if (req.getServletPath().contains("shop-com-order-listener")) {
			LOG.info("Recieved Order XML post for SHOP.COM order processing...");
			LOG.info("Content-Type:" + req.getContentType());
			if (req.getParameter("data") != null)
				LOG.info(req.getParameter("data"));
			return true;
		}
		LOG.info(
				"Checking request header for auth parameters to access resource path {}",
				req.getServletPath());
		Cookie[] cookies = req.getCookies();
		String sessionid = null;
		for (Cookie cookie : cookies) {
			if ("SESSIONID".equals(cookie.getName())) {
				sessionid = cookie.getValue();
				break;
			}
		}
		if (sessionid == null) {
			res.setStatus(403);
			LOG.warn("Authentication parameters not found in request header. Setting Response status 403.");
			return false;
		}
		Map<String, String> params = new HashMap<String, String>();
		params.put("sessionid", sessionid);
		RestTemplate template = new RestTemplate();
		HttpHeaders headers = template.headForHeaders(
				"http://localhost:8080/admin/login?sessionid={sessionid}",
				params);
		Integer valueOf = Integer.valueOf(headers.get("vid").get(0));
		if (valueOf > 0) {
			LOG.debug("Authenticated successfully to access resource path {}",
					req.getServletPath());
		} else {
			LOG.warn("Authentication failed to access resource path {}",
					req.getServletPath());
			res.setStatus(401);
			return false;
		}
		MyThreadLocal.set(valueOf);
		SessionManager.currentSession();
		LOG.debug("Setting vendorId# {} in ThreadLocal ", MyThreadLocal.get());
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest arg0,
			HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {
		// DO NOTHING
		LOG.debug("Removing vendorId# {} from ThreadLocal ",
				MyThreadLocal.get());
		SessionManager.closeSession();
		MyThreadLocal.unset();
	}

	public static class MyThreadLocal {

		public static final ThreadLocal<Integer> userThreadLocal = new ThreadLocal<Integer>();

		public static void set(Integer user) {
			userThreadLocal.set(user);
		}

		public static void unset() {
			userThreadLocal.remove();
		}

		public static Integer get() {
			return userThreadLocal.get();
		}
	}
}
