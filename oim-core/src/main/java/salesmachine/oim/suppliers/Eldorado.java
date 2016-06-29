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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.axis.utils.ByteArrayOutputStream;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSupplierShippingMethod;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.stores.modal.volusion.order.Xmldata;
import salesmachine.oim.suppliers.exception.SupplierCommunicationException;
import salesmachine.oim.suppliers.exception.SupplierConfigurationException;
import salesmachine.oim.suppliers.exception.SupplierOrderException;
import salesmachine.oim.suppliers.exception.SupplierOrderTrackingException;
import salesmachine.oim.suppliers.modal.OrderDetailResponse;
import salesmachine.oim.suppliers.modal.OrderStatus;
import salesmachine.oim.suppliers.modal.TrackingData;
import salesmachine.oim.suppliers.modal.el.XMLInputOrder;
import salesmachine.oim.suppliers.modal.el.XMLInputOrder.Products;
import salesmachine.oim.suppliers.modal.el.XMLInputOrder.Products.Product;
import salesmachine.oim.suppliers.modal.el.XMLOrders;
import salesmachine.oim.suppliers.modal.el.tracking.ELTrackRequest;
import salesmachine.oim.suppliers.modal.el.tracking.ELTrackRequest.XMLOrders.Order;
import salesmachine.util.ApplicationProperties;
import salesmachine.util.StringHandle;

public class Eldorado extends Supplier implements HasTracking {

  private static final Logger log = LoggerFactory.getLogger(Eldorado.class);
  private final String partnerKey = ApplicationProperties
      .getProperty(ApplicationProperties.EL_PARTNER_KEY);

  private final String orderEndPoint = ApplicationProperties
      .getProperty(ApplicationProperties.EL_API_ORDER_ENDPOINT);
  private static final String TRACKING_ENDPOINT = "https://www.eldoradopartner.com/shipping_updates/index.php";
  private static JAXBContext jaxbContext;

  static {
    try {
      jaxbContext = JAXBContext.newInstance(XMLInputOrder.class, ELTrackRequest.class,
          salesmachine.oim.suppliers.modal.el.tracking.XMLOrders.class);
    } catch (JAXBException e) {
      log.error("Error in initializing XML parsing context.");
    }
  }

  @Override
  public OrderStatus getOrderStatus(OimVendorSuppliers ovs, Object trackingMeta,
      OimOrderDetails oimOrderDetails) throws SupplierOrderTrackingException {
    OrderStatus status = new OrderStatus();
    status.setStatus(oimOrderDetails.getSupplierOrderStatus());
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      Marshaller marshaller = jaxbContext.createMarshaller();
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      ELTrackRequest request = new ELTrackRequest();
      request.setKey(partnerKey);
      salesmachine.oim.suppliers.modal.el.tracking.ELTrackRequest.XMLOrders xmlOrder = new salesmachine.oim.suppliers.modal.el.tracking.ELTrackRequest.XMLOrders();
      Order order = new Order();
      order.setOrderCustomer(ovs.getAccountNumber());
      int trackingId = Integer.parseInt(trackingMeta.toString());
      order.setOrderId(trackingId);
      order.setOrderShippingCost(false);
      xmlOrder.setOrder(order);
      request.setXMLOrders(xmlOrder);
      marshaller.marshal(request, os);
      log.info("request for tracking for storeOrderId {} is -- {}",oimOrderDetails.getOimOrders().getStoreOrderId(), os.toString());

      String response = postRequest(TRACKING_ENDPOINT,
          os.toString().replace("<ELTrackRequest>", "").replace("</ELTrackRequest>", ""));
      salesmachine.oim.suppliers.modal.el.tracking.XMLOrders trackingResponse = (salesmachine.oim.suppliers.modal.el.tracking.XMLOrders) unmarshaller
          .unmarshal(new StringReader(response));
      String responseCode = trackingResponse.getOrder().getResponseCode();
      switch (responseCode) {
      case "RECORD":
        if(!StringHandle.isNullOrEmpty(trackingResponse.getOrder().getTrackingNumber())){
        TrackingData trackingData = new TrackingData();
        trackingData.setCarrierCode(trackingResponse.getOrder().getCarrierCode());
        trackingData.setCarrierName(trackingResponse.getOrder().getCarrierCode());
        trackingData.setShippingMethod(trackingResponse.getOrder().getServiceCode());
        trackingData.setQuantity(oimOrderDetails.getQuantity());
        trackingData.setShipperTrackingNumber(trackingResponse.getOrder().getTrackingNumber());
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        GregorianCalendar c = new GregorianCalendar();
        Date date = df.parse(trackingResponse.getOrder().getDateShipment());
       
//        if (date.before(oimOrderDetails.getProcessingTm())) {
//          String subject = "Old Tracking found for Vendor - " + ovs.getVendors().getVendorId();
//          String message = "Order tracking response - " + response;
//          EmailUtil.sendEmail("orders@inventorysource.com", "support@inventorysource.com",
//              "ruchi@inventorysource.com", subject, message);
//          throw new SupplierOrderTrackingException(
//              "Got an older tracking details from Eldorado dated - " + date.toString()
//                  + " for StoreOrderId -" + oimOrderDetails.getOimOrders().getStoreOrderId()
//                  + " and PONumber - " + oimOrderDetails.getSupplierOrderNumber());
//
//        }
        c.setTime(date);
        XMLGregorianCalendar shipDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        trackingData.setShipDate(shipDate);
        status.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_SHIPPED);
        status.addTrackingData(trackingData);
        break;
        }else{
          log.error("Got Empty Tracking Response for storeOrderId - {} and poNum - {}",oimOrderDetails.getOimOrders().getStoreOrderId(),trackingId);
        String subject = "Got Empty Tracking Response for storeOrderId - "+oimOrderDetails.getOimOrders().getStoreOrderId();
        String message = response;
        EmailUtil.sendEmail("manish@inventorysource.com", "support@inventorysource.com",
            "", subject, message);
        }
      case "NO_RECORD":
        throw new SupplierOrderTrackingException(
            "No record found with the given order id:" + trackingMeta);
      case "BAD_REQUEST":
        throw new SupplierOrderTrackingException(
            "Eldorado system failed to process request for PO:" + trackingMeta);
      }
    } catch (JAXBException | SupplierConfigurationException | SupplierCommunicationException
        | SupplierOrderException | IOException | ParseException
        | DatatypeConfigurationException e) {
      log.error(e.getMessage(), e);
      throw new SupplierOrderTrackingException(e.getMessage(), e);
    }
    catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    return status;
  }

   public static void main(String[] args) {
//   Eldorado el = new Eldorado();
//   Session dbSession = SessionManager.currentSession();
//   OimVendorSuppliers ovs = (OimVendorSuppliers)dbSession.get(OimVendorSuppliers.class, 10701);
//   OimOrders order = (OimOrders)dbSession.get(OimOrders.class, 504103);
//   try {
//   el.sendOrders(431906, ovs, order);
//   } catch (SupplierConfigurationException | SupplierCommunicationException
//   | SupplierOrderException | ChannelConfigurationException | ChannelCommunicationException
//   | ChannelOrderFormatException e) {
//   // TODO Auto-generated catch block
//   e.printStackTrace();
//   }
     int val = generateNineDigitPO(111111);
     System.out.println(val);
   }

  @Override
  public void sendOrders(Integer vendorId, OimVendorSuppliers ovs, OimOrders order)
      throws SupplierConfigurationException, SupplierCommunicationException, SupplierOrderException,
      ChannelConfigurationException, ChannelCommunicationException, ChannelOrderFormatException {
    orderSkuPrefixMap = setSkuPrefixForOrders(ovs);

    try {
      Marshaller marshaller = jaxbContext.createMarshaller();
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      XMLInputOrder elOrder = new XMLInputOrder();
      elOrder.setAccountId(ovs.getAccountNumber());
      elOrder.setKey(partnerKey);
      elOrder.setAddressLine1(order.getDeliveryStreetAddress());
      elOrder.setAddressLine2(order.getDeliverySuburb());
      elOrder.setCity(order.getDeliveryCity());
      elOrder.setCountryCode(order.getDeliveryCountryCode());
      String poNum = null;
      Session session = SessionManager.currentSession();
      Query query = session.createSQLQuery(
          "select  distinct SUPPLIER_ORDER_NUMBER from kdyer.OIM_ORDER_DETAILS where ORDER_ID=:orderId and SUPPLIER_ID=:supplierId");
      query.setInteger("orderId", order.getOrderId());
      query.setInteger("supplierId", ovs.getOimSuppliers().getSupplierId());
      Object q = null;
      try {
        q = query.uniqueResult();
      } catch (NonUniqueResultException e) {
        log.error(
            "This order has more than one product having different PO number. Please make them unique. store order id is - {}",
            order.getStoreOrderId());
        throw new SupplierConfigurationException(
            "This order has more than one product having different PO number. Please make them unique.");
      }
      if (q != null) {
        poNum = (String) q;
        log.info("Reprocessing PO NUmber - {}", poNum);
      } else {
      //poNum = StringHandle.removeNull(order.getOrderId()).toString();
        poNum = String.valueOf(generateNineDigitPO(order.getOrderId()));
      }
      if (poNum.matches("[0-9]+") == false) {
        log.error(
            "This order - {} conatins an alphanumeric PO Number. Eldorado only supports numbers only for PO",
            order.getOrderId());
        throw new SupplierOrderException("This order - " + order.getStoreOrderId()
            + " conatins an alphanumeric PO Number. Eldorado only supports numbers only for PO");
      }
      elOrder.setCustPONumber(poNum);// EL requires numbers only PO
      elOrder.setEnteredByCode(ovs.getLogin());
      elOrder.setName(order.getDeliveryName());
      elOrder.setPhoneNumber(order.getDeliveryPhone());
      elOrder.setSourceCode(ovs.getPassword());
      elOrder.setSourceOrderNumber(poNum);
      elOrder.setSpecialInstructions(order.getOrderComment());
      elOrder.setStateCode(order.getDeliveryStateCode());
      elOrder.setZipCode(order.getDeliveryZip());
      Products products = new Products();

      for (OimOrderDetails oimOrderDetails : order.getOimOrderDetailses()) {
        if (!oimOrderDetails.getOimSuppliers().getSupplierId()
            .equals(ovs.getOimSuppliers().getSupplierId()))
          continue;
        String skuPrefix = null, sku = oimOrderDetails.getSku();
        if (!orderSkuPrefixMap.isEmpty()) {
          skuPrefix = orderSkuPrefixMap.values().toArray()[0].toString();
        }
        skuPrefix = StringHandle.removeNull(skuPrefix);
        if (sku.startsWith(skuPrefix)) {
          sku = sku.substring(skuPrefix.length());
        }
        Product product = new Product();
        product.setCode(sku);
        product.setQuantity(oimOrderDetails.getQuantity());
        products.getProduct().add(product);
      }
      if (order.getOimShippingMethod() == null) {
        throw new SupplierOrderException(
            "Shipping method is not resolved for order :" + order.getStoreOrderId());
      }
      OimSupplierShippingMethod code = Supplier.findShippingCodeFromUserMapping(
          Supplier.loadSupplierShippingMap(ovs.getOimSuppliers(), ovs.getVendors()),
          order.getOimShippingMethod());
      if (code == null) {
        throw new SupplierOrderException(
            "the shipping method specified is not configured for Eldorado for order id :"
                + order.getStoreOrderId());
      }
      // elOrder.setShipVia("UGR"/* code.toString() */);
      elOrder.setShipVia(code.getName());
      elOrder.setProducts(products);
      try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
        JAXBContext jaxbContext1 = JAXBContext
            .newInstance(salesmachine.oim.suppliers.modal.el.XMLInputOrder.class);
        marshaller = jaxbContext1.createMarshaller();
        marshaller.marshal(elOrder, os);
        // System.out.println("request -->"+ os.toString().replace("<XML_InputOrder>",
        // "").replace("</XML_InputOrder>", ""));
        String response = postRequest(orderEndPoint,
            os.toString().replace("<XML_InputOrder>", "").replace("</XML_InputOrder>", ""));
        // String response = "<XML_Orders><Success>SAP: Your order (Reference ID: ) was accepted by
        // the Eldorado Partner Gateway!</Success></XML_Orders>";
        log.info("order id - {} is processed. Here is the response - {}", order.getStoreOrderId(),
            response);
        JAXBContext jaxbContext = JAXBContext.newInstance(XMLOrders.class);
        unmarshaller = jaxbContext.createUnmarshaller();
        // salesmachine.oim.suppliers.modal.el.tracking.XMLOrders cannot be cast to
        // salesmachine.oim.suppliers.modal.el.XMLOrders

        XMLOrders xmlOrder = (XMLOrders) unmarshaller.unmarshal(new StringReader(response));
        if (!StringHandle.isNullOrEmpty(xmlOrder.getSuccess())) {
          for (OimOrderDetails oimOrderDetails : order.getOimOrderDetailses()) {
            successfulOrders.put(oimOrderDetails.getDetailId(),
                new OrderDetailResponse(poNum, "InProcess", null));
          }
          EmailUtil.sendEmail(
              "orders@inventorysource.com", "support@inventorysource.com", "", "VID: " + vendorId
                  + ", Order id " + order.getStoreOrderId() + " processed to Eldorado",
              response, "text/html");
        } else {
          for (OimOrderDetails oimOrderDetails : order.getOimOrderDetailses()) {
            failedOrders.put(oimOrderDetails.getDetailId(),
                "Failed order processing for sku - " + oimOrderDetails.getSku());

            EmailUtil.sendEmail("orders@inventorysource.com",
                "support@inventorysource.com", "", "VID: " + vendorId + ", Order id "
                    + order.getStoreOrderId() + " Failed to process to Eldorado",
                response, "text/html");
          }
        }
      }
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    } catch (JAXBException | ClassCastException e) {
      log.error(e.getMessage());
      // temp email. will remove once it is tested properly.
      EmailUtil
          .sendEmail("orders@inventorysource.com",
              "support@inventorysource.com", "", "Error in processing Eldorado order for VID: "
                  + vendorId + ", Order id " + order.getStoreOrderId(),
              e.getMessage(), "text/html");
      throw new SupplierConfigurationException("Error in serializing order object.", e);
    }

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

  private static int generateNineDigitPO(int poNum) {
    int randomNum = 0;
    int returnVal = poNum;
    randomNum = generateRandom(poNum);
    if (randomNum > 0) {
      String num_str = poNum + "" + randomNum;
      returnVal = Integer.parseInt(num_str);
    }
    return returnVal;
  }

  public static int generateRandom(int num) {
    int length = String.valueOf(num).length();
    int randomNumSize = 9 - length;
    if (randomNumSize > 0) {
      Random random = new Random();
      char[] digits = new char[randomNumSize];
      digits[0] = (char) (random.nextInt(9) + '1');
      for (int i = 1; i < randomNumSize; i++) {
        digits[i] = (char) (random.nextInt(10) + '0');
      }
      return Integer.parseInt(new String(digits));
    }
    return 0;
  }
}
