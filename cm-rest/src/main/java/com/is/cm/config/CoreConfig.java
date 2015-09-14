package com.is.cm.config;

import javax.servlet.Filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.is.cm.core.persistance.ChannelRepository;
import com.is.cm.core.persistance.OrderRepository;
import com.is.cm.core.persistance.ReportRepository;
import com.is.cm.core.persistance.ShippingRepository;
import com.is.cm.core.persistance.SupplierRepository;
import com.is.cm.core.persistance.UserRepository;
import com.is.cm.core.service.ChannelEventHandler;
import com.is.cm.core.service.ChannelService;
import com.is.cm.core.service.OrderEventHandler;
import com.is.cm.core.service.OrderService;
import com.is.cm.core.service.ReportEventHandler;
import com.is.cm.core.service.ReportService;
import com.is.cm.core.service.ShippingEventHandler;
import com.is.cm.core.service.ShippingService;
import com.is.cm.core.service.SupplierEventHandler;
import com.is.cm.core.service.SupplierService;
import com.is.cm.core.service.UserEventHandler;
import com.is.cm.core.service.UserService;
import com.is.cm.rest.controller.SimpleCORSFilter;

@Configuration()
public class CoreConfig {
    private static final Logger LOG = LoggerFactory.getLogger(CoreConfig.class);

    @Bean
    public ChannelService createChannelService(ChannelRepository repo) {
	return new ChannelEventHandler(repo);
    }

    @Bean
    OrderService createOrderService(OrderRepository repo) {
	return new OrderEventHandler(repo);
    }

    @Bean(name = "objectMapper")
    public ObjectMapper createObjectMapper() {
	LOG.debug("Injecting custom ObjectMapper instance {}",
		HibernateAwareObjectMapper.class);
	return new HibernateAwareObjectMapper();
    }

    @Bean
    public SupplierService createSupplierService(
	    SupplierRepository supplierRepository) {
	return new SupplierEventHandler(supplierRepository);
    }

    @Bean
    public ReportService createReportService(
	    ReportRepository reportRepository) {
	return new ReportEventHandler(reportRepository);
    }

    @Bean
    public UserService createUserService(UserRepository userRepository) {
	return new UserEventHandler(userRepository);
    }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
	return new CommonsMultipartResolver();
    }

    @Bean
    Filter requestLoggingFilter() {
	Filter filter = new CommonsRequestLoggingFilter();
	return filter;
    }

    @Bean
    public Filter corsFilter() {
	return new SimpleCORSFilter();
    }

    @Bean
    ShippingService createShippingService(
	    ShippingRepository shippingRepository) {
	return new ShippingEventHandler(shippingRepository);
    }

}
