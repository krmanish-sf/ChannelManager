package salesmachine.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationProperties {
  // Amazon MWS property keys
  public static final String MWS_ACCESS_KEY = "cm.mws.accessKey";
  public static final String MWS_SECRET_KEY = "cm.mws.secretKey";
  public static final String MWS_APP_NAME = "cm.mws.appName";
  public static final String MWS_APP_VERSION = "cm.mws.appVersion";
  public static final String MWS_SERVICE_URL = "cm.mws.serviceURL";

  // Pulling interval and thread pool size keys
  public static final String AUTOMATION_THREAD_POOL_SIZE = "cm.service.poolsize";
  public static final String AUTOMATION_ORDER_PULL_INTERVAL = "cm.service.pullinterval";
  public static final String AUTOMATION_ORDER_TRACK_INTERVAL = "cm.service.trackinginterval";
  private static String HOSTNAME = "Unknown";

  // Automation Notification Email Keys
  public static final String AUTOMATION_MONITORING_EMAIL_TO = "cm.service.monitoring.to";
  public static final String AUTOMATION_MONITORING_EMAIL_FROM = "cm.service.monitoring.from";
  public static final String AUTOMATION_MONITORING_EMAIL_CC = "cm.service.monitoring.cc";

  // Shopify Keys
  public static final String SHOPIFY_API_KEY = "cm.shopify.apiKey";
  public static final String SHOPIFY_SECRET_KEY = "cm.shopify.secretKey";

  // BigCommerce Keys
  public static final String BIGCOMMERCE_CLIENT_API_URL = "cm.bigcommerce.apiURL";
  public static final String BIGCOMMERCE_CLIENT_ID = "cm.bigcommerce.clientId";
  public static final String BIGCOMMERCE_CLIENT_SECRET = "cm.bigcommerce.clientSecret";

  public static final String EL_PARTNER_KEY = "cm.supplier.el.partnerKey";
  public static final String EL_API_ORDER_ENDPOINT = "cm.supplier.el.api.orders.endpoint";
  
  public static final String DEVHUB_CLIENT_API_URL = "cm.devhub.apiURL";
  public static final String DEVHUB_CLIENT_ID = "cm.devhub.clientId";
  public static final String DEVHUB_CLIENT_SECRET = "cm.devhub.clientSecret";
  
  private static final Logger log = LoggerFactory.getLogger(ApplicationProperties.class);
  private static final Properties prop = new Properties();
  private static InputStream input = null;

  static {
    try {
      String filename = "config.properties";
      input = ApplicationProperties.class.getClassLoader().getResourceAsStream(filename);
      // load a properties file
      prop.load(input);
    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    try {
      HOSTNAME = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException ex) {
      log.warn("Hostname can not be resolved");
    }
  }

  private ApplicationProperties() {
    // Instance not needed, static getter only.
  }

  public static final String getProperty(String key) {
    return prop.getProperty(key);
  }

  /**
   * Returns Automation Thread Pool Size specified by key {@code AUTOMATION_THREAD_POOL_SIZE} in
   * {@link ApplicationProperties}. If the above mentioned key is not found or can't be parsed as
   * Integer; Default value 3 is returned.
   * 
   * @return long value containing order pull interval (in milliseconds)
   * @since 1.0
   */
  public static final int getAutotmationThreadPoolSize() {
    int i = 3;
    try {
      i = Integer.parseInt(prop.getProperty(AUTOMATION_THREAD_POOL_SIZE));
    } catch (NumberFormatException | NullPointerException e) {
      log.error("PROPERTY [{}] can't be parsed as Integer.", AUTOMATION_THREAD_POOL_SIZE);
      log.warn("Defaulting to thread-pool size {}", i);
    }
    return i;
  }

  /**
   * Returns Order pull interval specified by key {@code AUTOMATION_ORDER_PULL_INTERVAL} in
   * {@link ApplicationProperties} if the above mentioned key is not found or can't be parsed as
   * long; Default value 3600000 is returned.
   * 
   * @return long value containing order pull interval (in milliseconds)
   */
  public static final long getOrderPullInterval() {
    long i = 3600000;
    try {
      i = Long.valueOf(prop.getProperty(AUTOMATION_ORDER_PULL_INTERVAL));
    } catch (NumberFormatException | NullPointerException e) {
      log.error("PROPERTY [{}] can't be parsed as Long.", AUTOMATION_ORDER_PULL_INTERVAL);
      log.warn("Defaulting to pull interval of {} milliseconds", i);
    }
    return i;
  }

  /**
   * Returns Order tracking interval specified by key {@code AUTOMATION_ORDER_TRACK_INTERVAL} in
   * {@link ApplicationProperties} if the above mentioned key is not found or can't be parsed as
   * long; Default value 3600000 is returned.
   * 
   * @return long value containing order track interval (in milliseconds)
   */
  public static final long getOrderTrackingInterval() {
    long i = 60000;
    try {
      i = Long.valueOf(prop.getProperty(AUTOMATION_ORDER_TRACK_INTERVAL));
    } catch (NumberFormatException e) {
      log.error("PROPERTY [{}] can't be parsed as Long.", AUTOMATION_ORDER_TRACK_INTERVAL);
      log.warn("Defaulting to track interval of {} milliseconds", i);
    }
    return i;
  }

  /**
   * Returns Hostname as resolved by {@link java.net.InetAddress.getHostName()}
   * 
   * @return String value containing the above value returned by above method, if an exception is
   *         encountered {@value "Unknown"} is returned.
   */
  public static final String getHostName() {
    log.debug("HostName: {}", HOSTNAME);
    return HOSTNAME;
  }
}
