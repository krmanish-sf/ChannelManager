package salesmachine.test;

import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.email.EmailUtil;
import salesmachine.util.ApplicationProperties;

public class EmailTest {

  private static final Logger log = LoggerFactory.getLogger(EmailTest.class);

  // @Test
  public void sendEmailTest() {
    log.debug("Send Email test running...");
    EmailUtil.sendEmail(
        ApplicationProperties.getProperty(ApplicationProperties.AUTOMATION_MONITORING_EMAIL_TO),
        ApplicationProperties.getProperty(ApplicationProperties.AUTOMATION_MONITORING_EMAIL_FROM),
        ApplicationProperties.getProperty(ApplicationProperties.AUTOMATION_MONITORING_EMAIL_CC),
        "Test Email From Junit Test Suite.",
        "Please delete this email. Test is successful. From HOST: "
            + ApplicationProperties.getHostName());
  }

  @Test
  public void testGetSystemName() {
    String hostname = null;

    try {
      InetAddress addr;
      addr = InetAddress.getLocalHost();
      hostname = addr.getHostName();
    } catch (UnknownHostException ex) {
      log.error("Hostname can not be resolved");
    }
    log.info("HostName: {}", hostname);
    Assert.assertNotNull(hostname);
  }

  public static void main(String[] args) {
    SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss zZ");
    Date d = new Date();
    String format = df.format(d);
    System.out.println(format);
    System.out.println(URLEncoder.encode(format));
  }
}
