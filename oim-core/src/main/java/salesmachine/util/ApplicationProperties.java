package salesmachine.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationProperties {
	public static final String MWS_ACCESS_KEY = "cm.mws.accessKey";
	public static final String MWS_SECRET_KEY = "cm.mws.secretKey";
	public static final String MWS_APP_NAME = "cm.mws.appName";
	public static final String MWS_APP_VERSION = "cm.mws.appVersion";
	public static final String MWS_SERVICE_URL = "cm.mws.serviceURL";
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

	public static String getProperty(String key) {
		return prop.getProperty(key);
	}
}
