package salesmachine.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class Cleanup extends AbstractTestExecutionListener {

  private static final Logger log = LoggerFactory.getLogger(Cleanup.class);

  @Override
  public void afterTestClass(TestContext testContext) throws Exception {
    log.info("cleaning up now");

  }
}