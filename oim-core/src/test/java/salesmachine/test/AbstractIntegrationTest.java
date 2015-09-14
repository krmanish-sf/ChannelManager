package salesmachine.test;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.is.cm.core.config.PersistanceBeans;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(Cleanup.class)
@ContextConfiguration(classes = PersistanceBeans.class)
public abstract class AbstractIntegrationTest {
}
