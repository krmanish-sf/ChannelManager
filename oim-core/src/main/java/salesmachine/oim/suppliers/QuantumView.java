package salesmachine.oim.suppliers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.axis.utils.ByteArrayOutputStream;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.suppliers.exception.SupplierCommunicationException;
import salesmachine.oim.suppliers.exception.SupplierConfigurationException;
import salesmachine.oim.suppliers.exception.SupplierOrderException;
import salesmachine.oim.suppliers.exception.SupplierOrderTrackingException;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.oim.suppliers.modal.TrackingData;
import salesmachine.oim.suppliers.modal.ups.AccessRequest;
import salesmachine.oim.suppliers.modal.ups.track.request.ReferenceNumber;
import salesmachine.oim.suppliers.modal.ups.track.request.Request;
import salesmachine.oim.suppliers.modal.ups.track.request.TrackRequest;
import salesmachine.oim.suppliers.modal.ups.track.response.Activity;
import salesmachine.oim.suppliers.modal.ups.track.response.PackageType;
import salesmachine.oim.suppliers.modal.ups.track.response.TrackResponse;
import salesmachine.util.StringHandle;

public class QuantumView extends Supplier implements HasTracking {

  private static final Logger log = LoggerFactory.getLogger(QuantumView.class);
  private static final String LICENSE_NUMBER = "9D0D1B02DEE68988";
  private static final String USER_NAME = "inventorysource";
  private static final String PASSWORD = "Aut0Inventory!";
  private static final String ENDPOINT_URL = "https://wwwcie.ups.com/ups.app/xml/Track"; // TODO :
                                                                                         // need to
                                                                                         // change
                                                                                         // this
                                                                                         // with :
                                                                                         // https://onlinetools.ups.com/ups.app/xml/Track

  public QuantumView() {
    // TODO Auto-generated constructor stub
  }

  public static void main(String[] args) {
    QuantumView quantumView = new QuantumView();
    try {
      quantumView.getOrderStatus(null, "M-37097-1", null);
    } catch (SupplierOrderTrackingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public OrderStatus getOrderStatus(OimVendorSuppliers ovs, Object trackingMeta,
      OimOrderDetails oimOrderDetails) throws SupplierOrderTrackingException {
    Session m_dbSession = SessionManager.currentSession();
    Transaction tx = m_dbSession.getTransaction();
    try {
      if (tx != null && tx.isActive())
        tx.commit();
      tx = m_dbSession.beginTransaction();
      oimOrderDetails.setLastTrackTm(new Date());
      tx.commit();
    } catch (HibernateException e1) {
      try {
        tx.rollback();
      } catch (Exception e) {
        e.printStackTrace();
      }
      e1.printStackTrace();
    }
    OrderStatus status = new OrderStatus();
    status.setStatus(oimOrderDetails.getSupplierOrderStatus());
    String referenceNum = trackingMeta.toString();
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      JAXBContext accessRequestJAXBC = JAXBContext
          .newInstance(AccessRequest.class.getPackage().getName());
      Marshaller accessRequestMarshaller = accessRequestJAXBC.createMarshaller();
      salesmachine.oim.suppliers.modal.ups.ObjectFactory accessRequestObjectFactory = new salesmachine.oim.suppliers.modal.ups.ObjectFactory();
      AccessRequest accessRequest = accessRequestObjectFactory.createAccessRequest();
      populateAccessRequest(accessRequest);

      // Create JAXBContext and marshaller for TrackRequest object
      JAXBContext trackRequestJAXBC = JAXBContext
          .newInstance(TrackRequest.class.getPackage().getName());
      Marshaller trackRequestMarshaller = trackRequestJAXBC.createMarshaller();
      salesmachine.oim.suppliers.modal.ups.track.request.ObjectFactory requestObjectFactory = new salesmachine.oim.suppliers.modal.ups.track.request.ObjectFactory();
      TrackRequest trackRequest = requestObjectFactory.createTrackRequest();
      populateTrackRequest(trackRequest, referenceNum);
      accessRequestMarshaller.marshal(accessRequest, os);
      trackRequestMarshaller.marshal(trackRequest, os);
      String response = postRequest(ENDPOINT_URL, os.toString());
      // log.info("response - {}", response);
      JAXBContext trackResponseJaxb = JAXBContext
          .newInstance(TrackResponse.class.getPackage().getName());
      Unmarshaller responseUnmarshaller = trackResponseJaxb.createUnmarshaller();
      TrackResponse trackResponse = (TrackResponse) responseUnmarshaller
          .unmarshal(new StringReader(response));
      int statusCode = Integer.parseInt(trackResponse.getResponse().getResponseStatusCode());
      String statusMsg = trackResponse.getResponse().getResponseStatusDescription();
      if (statusCode == 1) {
        if (!StringHandle.isNullOrEmpty(
            trackResponse.getShipment().get(0).getPackage().get(0).getTrackingNumber())) {
          String deliveryDate = trackResponse.getShipment().get(0).getPickupDate();
          PackageType packageType = trackResponse.getShipment().get(0).getPackage().get(0);
          for (int j = 0; j < packageType.getActivity().size(); j++) {
            Activity activity = packageType.getActivity().get(0);
            if ("D".equalsIgnoreCase(activity.getStatus().getStatusType().getCode())) {
              deliveryDate = activity.getDate();
              break;
            }
          }
          SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd"); // 20160525
          GregorianCalendar c = new GregorianCalendar();
          Date date = df.parse(deliveryDate);
          c.setTime(date);
          XMLGregorianCalendar shipDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

          System.out.println("getReferenceNumber - "
              + trackResponse.getShipment().get(0).getReferenceNumber().get(0));

          TrackingData trackingData = new TrackingData();
          trackingData.setCarrierCode("UPS");
          trackingData.setCarrierName("UPS");
          trackingData
              .setShippingMethod(trackResponse.getShipment().get(0).getService().getDescription());
          trackingData.setQuantity(oimOrderDetails.getQuantity());
          trackingData.setShipperTrackingNumber(
              trackResponse.getShipment().get(0).getPackage().get(0).getTrackingNumber());
          trackingData.setShipDate(shipDate);
          status.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_SHIPPED);
          status.addTrackingData(trackingData);
        }
      } else if (statusCode == 0) {
        String subject = "Error in getting tracking from UPS (QuantumView) for vendor - "
            + ovs.getVendors().getVendorId();
        String message = "Error Description - " + statusMsg;
        EmailUtil.sendEmail("orders@inventorysource.com", "support@inventorysource.com", "",
            subject, message, "text/html");
        throw new SupplierOrderException("Error occured in getting tracking for store order id -  "
            + oimOrderDetails.getOimOrders().getStoreOrderId() + " \n " + statusMsg);
      }

    } catch (JAXBException | SupplierConfigurationException | SupplierCommunicationException
        | SupplierOrderException | IOException | ParseException
        | DatatypeConfigurationException e) {
      log.error(e.getMessage(), e);
      throw new SupplierOrderTrackingException(e.getMessage(), e);
    }
    return status;
  }

  private static void populateTrackRequest(TrackRequest trackRequest, String shipmentID) {
    Request request = new Request();
    ReferenceNumber referenceNumber = new ReferenceNumber();
    referenceNumber.setValue(shipmentID);
    List<String> optoinsList = request.getRequestOption();
    optoinsList.add("activity"); // If the request option here is of 2 ~ 15, then Signature tracking
                                 // must validate the rights to signature tracking.
    request.setRequestAction("Track");
    trackRequest.setRequest(request);
    trackRequest.setReferenceNumber(referenceNumber);
  }

  private static void populateAccessRequest(AccessRequest accessRequest) {
    accessRequest.setAccessLicenseNumber(LICENSE_NUMBER);
    accessRequest.setUserId(USER_NAME);
    accessRequest.setPassword(PASSWORD);
  }

  private String postRequest(String urlString, String request)
      throws SupplierConfigurationException, SupplierCommunicationException,
      SupplierOrderException {
    URL url;
    HttpsURLConnection connection = null;
    String response = "";
    try {
      // Create connection
      url = new URL(urlString);
      connection = (HttpsURLConnection) url.openConnection();
      connection.setRequestMethod("POST");

      byte[] req = request.getBytes();
      log.info("Request: {}", request);
      connection.setRequestProperty("Content-Type", "text/xml");
      connection.setUseCaches(false);
      connection.setDoInput(true);
      connection.setDoOutput(true);

      OutputStream out = connection.getOutputStream();
      out.write(req);
      out.close();
      connection.connect();
      BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = rd.readLine()) != null) {
        sb.append(line + '\n');
      }
      response = sb.toString();
      log.info("Response: {}", response);
      return response;
    } catch (RuntimeException e) {
      log.error("Failed to send request ...", e);
      String logEmailContent = "-------------- Order failed with Exception -------------\n";
      logEmailContent += "-------------- XML SOAP REQUEST SENT -------------\n";
      logEmailContent += request + "\n";
      logEmailContent += "--------------------------------------------------";
      logEmailContent += "-------------- XML SOAP RESPONSE CAME -------------\n";
      logEmailContent += e + "\n";
      logEmailContent += "--------------------------------------------------";
      logStream.println(logEmailContent);
      /*
       * String emailSubject = "Order failed for Vendor : " + r.getFirstName() + " " +
       * r.getLastName() + " VID : " + r.getVendorId();
       * EmailUtil.sendEmail("orders@inventorysource.com", "support@inventorysource.com", "",
       * emailSubject, logEmailContent);
       */
      throw new SupplierOrderException("Error occured in posting request:" + e.getMessage(), e);
    } catch (MalformedURLException e) {
      log.error(e.getMessage());
      throw new SupplierConfigurationException("Supplier Communication URL is invalid.", e);
    } catch (IOException e) {
      log.error(e.getMessage());
      throw new SupplierCommunicationException(e.getMessage(), e.getCause());
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  @Override
  public void sendOrders(Integer vendorId, OimVendorSuppliers ovs, OimOrders orders)
      throws SupplierConfigurationException, SupplierCommunicationException, SupplierOrderException,
      ChannelConfigurationException, ChannelCommunicationException, ChannelOrderFormatException {
    // TODO Auto-generated method stub
    // This method is not applicable for UPS/Quantum

  }

}
