/*******************************************************************************
 * Copyright 2009-2015 Amazon Services. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 *
 * You may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at: http://aws.amazon.com/apache2.0
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 * specific language governing permissions and limitations under the License.
 *******************************************************************************
 * Marketplace Web Service Orders
 * API Version: 2013-09-01
 * Library Version: 2015-02-13
 * Generated: Tue Feb 10 22:00:47 UTC 2015
 */
package salesmachine.oim.stores.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersAsyncClient;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersClient;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersConfig;

import salesmachine.util.ApplicationProperties;

/**
 * Configuration for MarketplaceWebServiceOrders samples.
 */
class MarketplaceWebServiceOrdersClientConfig {
  /** Developer AWS access key. */
  private static final String accessKey = ApplicationProperties
      .getProperty(ApplicationProperties.MWS_ACCESS_KEY);

  /** Developer AWS secret key. */
  private static final String secretKey = ApplicationProperties
      .getProperty(ApplicationProperties.MWS_SECRET_KEY);

  /** The client application name. */
  private static final String appName = ApplicationProperties
      .getProperty(ApplicationProperties.MWS_APP_NAME);

  /** The client application version. */
  private static final String appVersion = ApplicationProperties
      .getProperty(ApplicationProperties.MWS_APP_VERSION);

  /**
   * The endpoint for region service and version. ex: serviceURL = MWSEndpoint.NA_PROD.toString();
   */
  private static final String serviceURL = "https://mws.amazonservices.com";

  private static final ExecutorService service = Executors.newFixedThreadPool(1);

  /** The client, lazy initialized. Async client is also a sync client. */
  private static MarketplaceWebServiceOrdersAsyncClient client = null;

  /**
   * Get a client connection ready to use.
   *
   * @return A ready to use client connection.
   */
  public static MarketplaceWebServiceOrdersClient getClient() {
    return getAsyncClient();
  }

  /**
   * Get an async client connection ready to use.
   *
   * @return A ready to use client connection.
   */
  public static synchronized MarketplaceWebServiceOrdersAsyncClient getAsyncClient() {
    if (client == null) {
      MarketplaceWebServiceOrdersConfig config = new MarketplaceWebServiceOrdersConfig();
      config.setServiceURL(serviceURL);
      // Set other client connection configurations here.
      client = new MarketplaceWebServiceOrdersAsyncClient(accessKey, secretKey, appName, appVersion,
          config, service);
    }
    return client;
  }

}
