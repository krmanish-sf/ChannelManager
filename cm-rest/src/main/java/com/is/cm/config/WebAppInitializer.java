package com.is.cm.config;

import java.util.Set;

import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.is.cm.core.config.PersistanceBeans;
import com.is.cm.rest.controller.SimpleCORSFilter;

public class WebAppInitializer implements WebApplicationInitializer {
    private static final String SERVLET_URL = "/";
    private static Logger LOG = LoggerFactory
	    .getLogger(WebAppInitializer.class);

    @Override
    public void onStartup(ServletContext servletContext) {
	WebApplicationContext rootContext = createRootContext(servletContext);
	Dynamic addFilter = servletContext.addFilter("crosFilter",
		SimpleCORSFilter.class);
	addFilter.addMappingForUrlPatterns(null, false, "/*");
	configureSpringMvc(servletContext, rootContext);
    }

    private WebApplicationContext createRootContext(
	    ServletContext servletContext) {
	AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
	rootContext.setDisplayName("InventorySource Channel Manager Rest API");
	rootContext.register(CoreConfig.class, PersistanceBeans.class);
	try {
	    rootContext.refresh();
	} catch (java.lang.IllegalArgumentException e) {
	    LOG.error("Error in refreshing application context.F", e);
	}
	servletContext.addListener(new ContextLoaderListener(rootContext));
	servletContext.setInitParameter("defaultHtmlEscape", "true");

	return rootContext;
    }

    private void configureSpringMvc(ServletContext servletContext,
	    WebApplicationContext rootContext) {
	AnnotationConfigWebApplicationContext mvcContext = new AnnotationConfigWebApplicationContext();
	mvcContext.register(MVCConfig.class);

	mvcContext.setParent(rootContext);

	ServletRegistration.Dynamic appServlet = servletContext
		.addServlet("webservice", new DispatcherServlet(mvcContext));
	appServlet.setLoadOnStartup(1);
	Set<String> mappingConflicts = appServlet.addMapping(SERVLET_URL);
	if (!mappingConflicts.isEmpty()) {
	    for (String s : mappingConflicts) {
		LOG.error("Mapping conflict: " + s);
	    }
	    throw new IllegalStateException(
		    "Servlet 'webservice' cannot be mapped to " + SERVLET_URL);
	}
    }

    @Override
    public String toString() {
	return "InventorySource Channel Manager Rest API";
    }
}
