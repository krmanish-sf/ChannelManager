package salesmachine.oim.stores.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderBatchesTypes;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatehelper.PojoHelper;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.ChannelBase;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.exception.ChannelCommunicationException;
import salesmachine.oim.stores.exception.ChannelConfigurationException;
import salesmachine.oim.stores.exception.ChannelOrderFormatException;
import salesmachine.oim.stores.modal.shop.order.status.ADIOSHEADER;
import salesmachine.oim.stores.modal.shop.order.status.ADIOSORDERSTATUSDETAIL;
import salesmachine.oim.stores.modal.shop.order.status.ADIOSORDERSTATUSTRANSMISSION;
import salesmachine.oim.stores.modal.shop.order.status.ADIOSSTATUS;
import salesmachine.oim.stores.modal.shop.order.status.OrderStatus;
import salesmachine.oim.suppliers.modal.TrackingData;
import salesmachine.util.StringHandle;

public class ShopOrderImport extends ChannelBase implements IOrderImport {
  private static final String STORE_ORDER_STATUS_UPDATE_URL = "https://admin-amos.shop.com/get_order_status+251.xhtml?";
  private static final Logger LOG = LoggerFactory.getLogger(ShopOrderImport.class);
  private String catalogId;

  @Override
  public boolean init(OimChannels oimChannel, Session dbSession)
      throws ChannelConfigurationException {
    super.init(oimChannel, dbSession);
    catalogId = StringHandle.removeNull(PojoHelper.getChannelAccessDetailValue(m_channel,
        OimConstants.CHANNEL_ACCESSDETAIL_SHOP_CATALOGID));
    if (StringHandle.isNullOrEmpty(catalogId)) {
      LOG.error("Channel setup is not correct. Please provide this details.");
      throw new ChannelConfigurationException(
          "Channel setup is not correct. Please provide this details.");

    }
    return true;
  }

  /**
   * This method is not supported on this channel type, as Order are posted in real time by shop.com
   * to our listener url.
   */
  @Override
  public void getVendorOrders(OimOrderBatchesTypes batchesTypes, OimOrderBatches batch)
      throws ChannelCommunicationException, ChannelOrderFormatException,
      ChannelConfigurationException {

  }

  @Override
  public void updateStoreOrder(OimOrderDetails oimOrderDetails,
      salesmachine.oim.suppliers.modal.OrderStatus orderStatus)
          throws ChannelCommunicationException, ChannelOrderFormatException {
    if (!orderStatus.isShipped()) {
      return;
    }
    ADIOSORDERSTATUSTRANSMISSION statusRequest = new ADIOSORDERSTATUSTRANSMISSION();
    ADIOSHEADER header = new ADIOSHEADER();
    header.setCATALOGID(catalogId);
    DateFormat df = new SimpleDateFormat("M/d/yyyy h:mm:ss a");
    header.setDATETIMESTAMP(df.format(new Date()));
    statusRequest.setADIOSHEADER(header);
    ADIOSSTATUS status = new ADIOSSTATUS();
    status.setINVOICENUM(oimOrderDetails.getOimOrders().getStoreOrderId());

    if (orderStatus.isShipped()) {
      for (TrackingData td : orderStatus.getTrackingData()) {
        ADIOSORDERSTATUSDETAIL statusDetail = new ADIOSORDERSTATUSDETAIL();
        statusDetail.setINTERNALSTATUS(OrderStatus.Item_shipped.getValue());
        statusDetail.setEXTERNALSTATUS(OrderStatus.Item_shipped.getValue());
        statusDetail.setCARRIERTRACKINGNUM(td.getShipperTrackingNumber());
        statusDetail.setSHIPMETHOD(td.getCarrierCode() + " " + td.getShippingMethod());
        statusDetail.setSHIPDATE(df.format(td.getShipDate()));
        statusDetail.setPURCHASEID(oimOrderDetails.getStoreOrderItemId());
        status.getADIOSORDERSTATUSDETAILOrINTERNALTEXTOrEXTERNALTEXT().add(statusDetail);
      }
      statusRequest.getADIOSSTATUS().add(status);
      DefaultHttpClient client = new DefaultHttpClient();
      // client.setRedirectStrategy(new LaxRedirectStrategy());
      try {

        JAXBContext context = JAXBContext.newInstance(ADIOSORDERSTATUSTRANSMISSION.class);
        Marshaller marshaller = context.createMarshaller();
        OutputStream os = new ByteArrayOutputStream();
        marshaller.marshal(statusRequest, os);
        LOG.info(os.toString());
        HttpPost post = new HttpPost(STORE_ORDER_STATUS_UPDATE_URL);
        List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("order_status_data", os.toString()));
        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        HttpResponse response = client.execute(post);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
          LOG.info("Order Status updated");
        }

        byte[] resp = new byte[(int) response.getEntity().getContentLength()];
        // response.getEntity().getContent().read(resp);
        int c;
        StringBuilder sb = new StringBuilder();
        while ((c = response.getEntity().getContent().read()) != -1) {
          sb.append(c);
        }
        LOG.info("Response: {}", sb);
      } catch (JAXBException e1) {
        LOG.error(e1.getMessage());
      } catch (IOException e) {
        LOG.error(e.getMessage());
      }
    }
  }

  @Override
  public void cancelOrder(OimOrders oimOrder) {
    // TODO Auto-generated method stub

  }

  @Override
  public void cancelOrder(OimOrderDetails oimOrder) {
    // TODO Auto-generated method stub

  }
}
