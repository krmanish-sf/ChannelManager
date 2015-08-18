package com.is.cm.rest.controller;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import salesmachine.util.ApplicationProperties;

@Component
public class SimpleCORSFilter implements Filter {

	private static final Logger log = LoggerFactory
			.getLogger(SimpleCORSFilter.class);

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		log.debug("Adding CORS Header to request path: {}",
				req.getServletContext());
		HttpServletResponse response = (HttpServletResponse) res;
		response.setHeader("Access-Control-Allow-Origin",
				ApplicationProperties.getProperty("cm.auth.CORS.url"));
		response.setHeader("Access-Control-Allow-Methods",
				"POST, GET, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers", "content-type");
		chain.doFilter(req, res);
	}

	@Override
	public void init(FilterConfig filterConfig) {
		log.info("Initializing CORS filter.");
		if (filterConfig == null) {
			throw new RuntimeException("Error oin filter");
		}

	}

	public void destroy() {
	}

}
