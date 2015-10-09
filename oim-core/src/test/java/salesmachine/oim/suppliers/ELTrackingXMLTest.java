package salesmachine.oim.suppliers;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import salesmachine.oim.suppliers.modal.el.XMLInputOrder;
import salesmachine.oim.suppliers.modal.el.XMLOrders;
import salesmachine.oim.suppliers.modal.el.tracking.ELTrackRequest;

public class ELTrackingXMLTest {
  private static JAXBContext jaxbContext;

  static {
    try {
      jaxbContext = JAXBContext.newInstance(XMLInputOrder.class, XMLOrders.class,
          ELTrackRequest.class, salesmachine.oim.suppliers.modal.el.tracking.XMLOrders.class);
    } catch (JAXBException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testMockXMLMarshalling() throws JAXBException {
    String filename = "salesmachine/oim/suppliers/el-tracking.xml";
    InputStream input = ELTrackingXMLTest.class.getClassLoader().getResourceAsStream(filename);

    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    salesmachine.oim.suppliers.modal.el.tracking.XMLOrders trackingResponse = (salesmachine.oim.suppliers.modal.el.tracking.XMLOrders) unmarshaller
        .unmarshal(input);
    System.out.println(trackingResponse);
  }
}
