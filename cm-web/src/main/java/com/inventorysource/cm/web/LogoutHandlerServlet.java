package com.inventorysource.cm.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet
public class LogoutHandlerServlet extends HttpServlet {
	private static final long serialVersionUID = 2394064180615585082L;
	Logger LOG = LoggerFactory.getLogger(LogoutHandlerServlet.class);

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		LOG.debug("Loggin out");
		if (session != null) {
			session.removeAttribute("reps");
			try {
				session.invalidate();
			} catch (IllegalStateException e) {
				LOG.debug("User session already invalidated");
			}
		}
		resp.sendRedirect("login.jsp");
	}
}
