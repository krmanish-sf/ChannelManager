package com.is.cm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.is.cm.core.security.AuthenticationInterceptor;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = { "com.is.cm.rest.controller" })
public class MVCConfig extends WebMvcConfigurerAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(MVCConfig.class);

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/static/**").addResourceLocations(
				"/static/");
	}

	// @Override
	/*
	 * @Bean public HandlerMapping resourceHandlerMapping() {
	 * AbstractHandlerMapping handlerMapping = (AbstractHandlerMapping) super
	 * .resourceHandlerMapping(); handlerMapping.setOrder(-1); return
	 * handlerMapping; }
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		LOG.info("Adding AuthenticationInterceptor ");
		registry.addInterceptor(new AuthenticationInterceptor());
	}
}
