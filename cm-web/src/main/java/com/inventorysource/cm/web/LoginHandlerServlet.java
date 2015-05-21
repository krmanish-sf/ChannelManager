package com.inventorysource.cm.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import salesmachine.hibernatedb.Reps;
import salesmachine.util.StringHandle;

import com.inventorysource.cm.web.config.ApplicationProperties;

@WebServlet
public class LoginHandlerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory
			.getLogger(LoginHandlerServlet.class);

	@Override
	public void init() throws ServletException {
		super.init();
		LOG.debug("LoginHandlerServlet initialised successfully.");
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		LOG.debug(
				"Webapp [{}] LoginHandlerServlet initialised on context path: {}",
				config.getServletContext().getServletContextName(), config
						.getServletContext().getContextPath());
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		if (session == null || session.getAttribute("reps") == null) {
			String username = req.getParameter("username");
			String password = req.getParameter("password");
			String done = req.getParameter("done");
			Map<String, String> loginDetails = new HashMap<String, String>();
			loginDetails.put("username", username);
			loginDetails.put("password", password);
			RestTemplate template = new RestTemplate();
			ResponseEntity<Reps> entity = template.postForEntity(
					ApplicationProperties.getRestServiceUrl() + "login",
					loginDetails, salesmachine.hibernatedb.Reps.class);
			Reps r = entity.getBody();
			if (r == null) {
				req.setAttribute("error",
						"Invalid user name or password! Please try again!");
				RequestDispatcher rd = req.getServletContext()
						.getRequestDispatcher("/login.jsp");
				rd.include(req, resp);
				return;
			}

			session = req.getSession(true);
			session.setAttribute("reps", r);
			session.setAttribute("REST_URL",
					ApplicationProperties.getRestServiceUrl());
			Cookie cookie = new Cookie("SESSIONID", session.getId());
			cookie.setPath("/");
			resp.addCookie(cookie);
			req.setAttribute("reps", r);
			if (r.getCmAllowed().intValue() == 1) {
				if (StringHandle.isNullOrEmpty(done))
					done = "index.jsp";
				resp.sendRedirect(done);
			} else {
				req.setAttribute(
						"error",
						"Channel Manager Service is not activated for this account. Use the Sign Up link to activate the service.");
				LOG.debug("Channel Manager Service is not activated for this account. Use the Sign Up link to activate the service.");
				resp.sendRedirect("payment.jsp");
			}

		} else {
			resp.sendRedirect("index.jsp");
		}
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String sessionid = req.getParameter("sessionid");
		if (sessionid != null) {
			Reps rep = SessionListener.getRep(sessionid);
			resp.addHeader("vid",
					String.valueOf((rep == null ? 0 : rep.getVendorId())));
			resp.getOutputStream().write("Success".getBytes());
			return;
		}
		HttpSession session = req.getSession(false);
		if (session == null) {
			RequestDispatcher rd = req.getServletContext()
					.getRequestDispatcher("/login.jsp");
			rd.include(req, resp);
		} else if (session.getAttribute("reps") == null) {

			RequestDispatcher rd = req.getServletContext()
					.getRequestDispatcher("/login.jsp");
			rd.include(req, resp);

		} else {
			resp.sendRedirect("index.jsp");
		}
	}
}
