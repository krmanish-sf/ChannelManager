package salesmachine.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CountryCodeProperty {

  private static final Logger log = LoggerFactory.getLogger(CountryCodeProperty.class);
  private static final Properties prop = new Properties();
  private static InputStream input = null;

  static {
    try {
      String filename = "countryCodeMapping.properties";
      input = ApplicationProperties.class.getClassLoader().getResourceAsStream(filename);
      prop.load(input);
    } catch (IOException ex) {
      log.error(ex.getMessage(), ex);
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          log.error(e.getMessage(), e);
        }
      }
    }
  }

  private CountryCodeProperty() {
  }

  // public static final String getProperty(String key) {
  // key = key.trim().toLowerCase();
  // return prop.getProperty(key);
  // }

  public static final String getProperty(String key) {
    key = key.trim().replaceAll(" ", "_").toLowerCase();
    return prop.getProperty(key);
  }
}
