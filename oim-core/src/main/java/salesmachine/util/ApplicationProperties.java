package salesmachine.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationProperties {
	public static final String MWS_ACCESS_KEY = "cm.mws.accessKey";
	public static final String MWS_SECRET_KEY = "cm.mws.secretKey";
	public static final String MWS_APP_NAME = "cm.mws.appName";
	public static final String MWS_APP_VERSION = "cm.mws.appVersion";
	public static final String MWS_SERVICE_URL = "cm.mws.serviceURL";
	public static final String AUTOMATION_THREAD_POOL_SIZE = "cm.service.poolsize";
	public static final String AUTOMATION_ORDER_PULL_INTERVAL = "cm.service.pullinterval";
	public static final String AUTOMATION_ORDER_TRACK_INTERVAL = "cm.service.trackinginterval";
	
	//Shopify
	public static final String SHOPIFY_API_KEY="cm.shopify.apiKey";
	public static final String SHOPIFY_SECRET_KEY = "cm.shopify.secretKey";
	
	private static final Logger log = LoggerFactory
			.getLogger(ApplicationProperties.class);
	private static final Properties prop = new Properties();
	private static InputStream input = null;
	static {
		try {
			String filename = "config.properties";
			input = ApplicationProperties.class.getClassLoader()
					.getResourceAsStream(filename);
			// input = new FileInputStream("config.properties");

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
	}

	private ApplicationProperties() {
		// Can't be instantiated
	}

	public static final String getProperty(String key) {
		return prop.getProperty(key);
	}

	public static final int getAutotmationThreadPoolSize() {
		int i = 3;
		try {
			i = Integer.parseInt(prop.getProperty(AUTOMATION_THREAD_POOL_SIZE));
		} catch (NumberFormatException e) {
			log.error("PROPERTY [{}] can't be parsed as Integer.",
					AUTOMATION_THREAD_POOL_SIZE);
			log.warn("Defaulting to thread-pool size {}", i);
		}
		return i;
	}

	public static final long getOrderPullInterval() {
		long i = 60000;
		try {
			i = Long.valueOf(prop.getProperty(AUTOMATION_ORDER_PULL_INTERVAL));
		} catch (NumberFormatException e) {
			log.error("PROPERTY [{}] can't be parsed as Long.",
					AUTOMATION_ORDER_PULL_INTERVAL);
			log.warn("Defaulting to pull interval of {} milliseconds", i);
		}
		return i;
	}

	public static final long getOrderTrackingInterval() {
		long i = 60000;
		try {
			i = Long.valueOf(prop.getProperty(AUTOMATION_ORDER_TRACK_INTERVAL));
		} catch (NumberFormatException e) {
			log.error("PROPERTY [{}] can't be parsed as Long.",
					AUTOMATION_ORDER_TRACK_INTERVAL);
			log.warn("Defaulting to track interval of {} milliseconds", i);
		}
		return i;
	}
}
