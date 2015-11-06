package salesmachine.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MotengContryCode {

  private static final Logger log = LoggerFactory.getLogger(MotengContryCode.class);
  private static final Properties prop = new Properties();
  private static InputStream input = null;

  static {
    try {
      String filename = "MotengContryCode.properties";
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

  public static final String getProperty(String key) {
    key = key.trim().replaceAll(" ", "_").toUpperCase();
    return prop.getProperty(key);
  }
}
