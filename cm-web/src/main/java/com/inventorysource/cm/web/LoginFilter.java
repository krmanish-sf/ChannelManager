package com.inventorysource.cm.web;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.auth.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginFilter implements javax.servlet.Filter {

	private String errorPage;
	private static final Logger LOG = LoggerFactory
			.getLogger(LoginFilter.class);

	/** Filter should be configured with an system error page. */
	public void init(FilterConfig FilterConfig) throws ServletException {
		if (FilterConfig != null) {
			errorPage = FilterConfig.getInitParameter("error_page");
			LOG.debug("Login filter initialised successfully.");
		}
	}

	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws ServletException, IOException {
		if (errorPage == null) {
			throw new AuthenticationException(
					"AuthorizationFilter not properly configured! Contact Administrator.");
		}

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String URI = request.getRequestURI();
		HttpSession session = request.getSession(false);
		if (URI.endsWith("login") || URI.endsWith("logout")
				|| URI.contains("/static/") || URI.endsWith("signup.jsp")
				|| URI.endsWith("signup")) {
			chain.doFilter(request, response);
		} else if (session == null || session.getAttribute("reps") == null) {
			// Reps currentUser = (Reps) session.getAttribute("reps");
			// Get relevant URI.

			RequestDispatcher rd = request.getServletContext()
					.getRequestDispatcher("/login.jsp");
			// PrintWriter out = response.getWriter();
			// out.println("<font color=red>Either user name or password is wrong.</font>");
			rd.include(request, response);
		} else {
			RequestDispatcher rd = request.getServletContext()
					.getRequestDispatcher(
							URI.replace(request.getContextPath(), ""));
			// PrintWriter out = response.getWriter();
			// out.println("<font color=red>Either user name or password is wrong.</font>");
			rd.include(request, response);
			// ((HttpServletResponse) response).sendRedirect(URI);
		}

	}

	public void destroy() {
		errorPage = null;
	}
}
