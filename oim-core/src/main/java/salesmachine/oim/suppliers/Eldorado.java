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

import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.axis.utils.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimSupplierShippingMethod;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
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

  private static JAXBContext jaxbContext;

  static {
    try {
      jaxbContext = JAXBContext.newInstance(XMLInputOrder.class, XMLOrders.class,
          ELTrackRequest.class, salesmachine.oim.suppliers.modal.el.tracking.XMLOrders.class);
    } catch (JAXBException e) {
      log.error("Error in initializing XML parsing context.");
    }
  }

  @Override
  public OrderStatus getOrderStatus(OimVendorSuppliers ovs, Object trackingMeta,
      OimOrderDetails oimOrderDetails) throws SupplierOrderTrackingException {
    String urlString = "https://www.eldoradopartner.com/shipping_updates/index.php";
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
      Integer trackingId = Integer.parseInt(trackingMeta.toString());
      order.setOrderId(trackingId);
      order.setOrderShippingCost(false);
      xmlOrder.setOrder(order);
      request.setXMLOrders(xmlOrder);
      marshaller.marshal(request, os);
      String response = postRequest(urlString,
          os.toString().replace("<ELTrackRequest>", "").replace("</ELTrackRequest>", ""));
      salesmachine.oim.suppliers.modal.el.tracking.XMLOrders trackingResponse = (salesmachine.oim.suppliers.modal.el.tracking.XMLOrders) unmarshaller
          .unmarshal(new StringReader(response));
      String responseCode = trackingResponse.getOrder().getResponseCode();
      switch (responseCode) {
      case "RECORD":
        TrackingData trackingData = new TrackingData();
        trackingData.setCarrierCode(trackingResponse.getOrder().getCarrierCode());
        trackingData.setShippingMethod(trackingResponse.getOrder().getServiceCode());
        trackingData.setQuantity(oimOrderDetails.getQuantity());
        trackingData.setShipperTrackingNumber(trackingResponse.getOrder().getTrackingNumber());
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        GregorianCalendar c = new GregorianCalendar();
        Date date = df.parse(trackingResponse.getOrder().getDateShipment());
        c.setTime(date);
        XMLGregorianCalendar shipDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        trackingData.setShipDate(shipDate);
        status.setStatus(OimConstants.OIM_SUPPLER_ORDER_STATUS_SHIPPED);
        status.addTrackingData(trackingData);
        break;
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

    return status;
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
      elOrder.setCustPONumber(order.getOrderId().toString());// EL requires numbers only PO
      elOrder.setEnteredByCode("API");
      elOrder.setName(order.getDeliveryName());
      elOrder.setPhoneNumber(order.getDeliveryPhone());
      elOrder.setSourceCode("API");
      elOrder.setSourceOrderNumber(order.getOrderId().toString());
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
      elOrder.setShipVia("UGR"/* code.toString() */);
      elOrder.setProducts(products);
      try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
        marshaller.marshal(elOrder, os);
        String response = postRequest(orderEndPoint,
            os.toString().replace("<XML_InputOrder>", "").replace("</XML_InputOrder>", ""));
        XMLOrders xmlOrder = (XMLOrders) unmarshaller.unmarshal(new StringReader(response));
        if (!StringHandle.isNullOrEmpty(xmlOrder.getSuccess())) {
          for (OimOrderDetails oimOrderDetails : order.getOimOrderDetailses()) {
            successfulOrders.put(oimOrderDetails.getDetailId(),
                new OrderDetailResponse(order.getOrderId().toString(), "InProcess", null));
          }
        } else {
          for (OimOrderDetails oimOrderDetails : order.getOimOrderDetailses()) {
            failedOrders.add(oimOrderDetails.getDetailId());
          }
        }
      }
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    } catch (JAXBException | ClassCastException e) {
      log.error(e.getMessage());
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
}
