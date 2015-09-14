package com.is.cm.core.config;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.is.cm.core.persistance.ChannelRepository;
import com.is.cm.core.persistance.ChannelRepositoryDb;
import com.is.cm.core.persistance.OrderRepository;
import com.is.cm.core.persistance.OrderRepositoryDB;
import com.is.cm.core.persistance.ReportRepository;
import com.is.cm.core.persistance.ReportRepositoryDB;
import com.is.cm.core.persistance.ShippingRepository;
import com.is.cm.core.persistance.ShippingRepositoryDB;
import com.is.cm.core.persistance.SupplierRepository;
import com.is.cm.core.persistance.SupplierRepositoryDB;
import com.is.cm.core.persistance.UserRepository;
import com.is.cm.core.persistance.UserRepositoryDB;

@Configuration
public class PersistanceBeans {
  @Bean
  public ChannelRepository createChannelRepo() {
    return new ChannelRepositoryDb();
  }

  @Bean
  OrderRepository createOrderRepo() {
    return new OrderRepositoryDB();
  }

  @Bean
  public SupplierRepository createSupplierRepository() {
    return new SupplierRepositoryDB();
  }

  @Bean
  public ReportRepository createReportRepository() {
    return new ReportRepositoryDB();
  }

  @Bean
  public UserRepository createUserRepository() {
    return new UserRepositoryDB();
  }

  @Bean
  ShippingRepository createShippingRepository() {
    return new ShippingRepositoryDB();
  }

  @Bean
  SessionFactory createSessionFactory() {
    org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration()
        .configure();
    StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
        .applySettings(configuration.getProperties());
    return configuration.buildSessionFactory(builder.build());
  }
}
