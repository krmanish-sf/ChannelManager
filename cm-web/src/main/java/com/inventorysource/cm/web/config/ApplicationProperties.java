package com.inventorysource.cm.web.config;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ApplicationProperties extends Properties {
	private static final String APPLICATION_PROPERTIES = "application.properties";
	private static final long serialVersionUID = -6365220208198475578L;
	private static final Logger LOG = LoggerFactory
			.getLogger(ApplicationProperties.class);
	private static final Properties instance;
	static {
		instance = new Properties();
		try {
			instance.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(APPLICATION_PROPERTIES));
		} catch (IOException e) {
			LOG.error(APPLICATION_PROPERTIES, e);
		}
	}

	public static final String getRestServiceUrl() {
		return instance.getProperty("com.is.cm.rest.url");
	}

	public static final String getAdminUrl() {
		return instance.getProperty("com.is.cm.ui.url");
	}

	public static String getVendorLogPath() {
		return instance.getProperty("com.is.cm.logshomedir");
	}

}
