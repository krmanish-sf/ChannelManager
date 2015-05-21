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

import salesmachine.hibernatedb.OimOrderBatches;
import salesmachine.hibernatedb.OimOrderBatchesTypes;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatehelper.PojoHelper;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.ChannelBase;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.modal.shop.order.status.ADIOSHEADER;
import salesmachine.oim.stores.modal.shop.order.status.ADIOSORDERSTATUSDETAIL;
import salesmachine.oim.stores.modal.shop.order.status.ADIOSORDERSTATUSTRANSMISSION;
import salesmachine.oim.stores.modal.shop.order.status.ADIOSSTATUS;
import salesmachine.oim.stores.modal.shop.order.status.OrderStatus;
import salesmachine.util.OimLogStream;
import salesmachine.util.StringHandle;

public class ShopOrderImport extends ChannelBase implements IOrderImport {
	private static final String STORE_ORDER_STATUS_UPDATE_URL = "https://admin-amos.shop.com/get_order_status+251.xhtml?";
	private static final Logger LOG = LoggerFactory
			.getLogger(ShopOrderImport.class);
	private String catalogId;

	@Override
	public boolean init(int channelID, Session dbSession, OimLogStream log) {
		super.init(channelID, dbSession, log);
		catalogId = StringHandle.removeNull(PojoHelper
				.getChannelAccessDetailValue(m_channel,
						OimConstants.CHANNEL_ACCESSDETAIL_SHOP_CATALOGID));
		if (StringHandle.isNullOrEmpty(catalogId)) {
			LOG.error("Channel setup is not correct. Please provide this details.");
			this.logStream
					.println("Channel setup is not correct. Please provide this details.");
			return false;
		}
		return true;
	}

	/**
	 * This method is not supported on this channel type, as Order are posted in
	 * real time by shop.com to our listener url.
	 * */
	@Override
	public OimOrderBatches getVendorOrders(OimOrderBatchesTypes batchesTypes) {
		return null;
	}

	@Override
	public boolean updateStoreOrder(OimOrderDetails oimOrderDetails,
			salesmachine.oim.suppliers.modal.OrderStatus orderStatus) {
		if (!orderStatus.isShipped()) {
			return true;
		}
		ADIOSORDERSTATUSTRANSMISSION statusRequest = new ADIOSORDERSTATUSTRANSMISSION();
		ADIOSHEADER header = new ADIOSHEADER();
		header.setCATALOGID(catalogId);
		DateFormat df = new SimpleDateFormat("M/d/yyyy h:mm:ss a");
		header.setDATETIMESTAMP(df.format(new Date()));
		statusRequest.setADIOSHEADER(header);
		ADIOSSTATUS status = new ADIOSSTATUS();
		status.setINVOICENUM(oimOrderDetails.getOimOrders().getStoreOrderId());
		ADIOSORDERSTATUSDETAIL statusDetail = new ADIOSORDERSTATUSDETAIL();
		if (orderStatus.isShipped()) {
			statusDetail.setINTERNALSTATUS(OrderStatus.Item_shipped.getValue());
			statusDetail.setEXTERNALSTATUS(OrderStatus.Item_shipped.getValue());
			statusDetail.setCARRIERTRACKINGNUM(orderStatus.getTrackingData()
					.getShipperTrackingNumber());
			statusDetail.setSHIPMETHOD(orderStatus.getTrackingData()
					.getCarrierCode()
					+ " "
					+ orderStatus.getTrackingData().getShippingMethod());
			statusDetail.setSHIPDATE(df.format(orderStatus.getTrackingData()
					.getShipDate().getTime()));
			statusDetail.setPURCHASEID(oimOrderDetails.getStoreOrderItemId());
		} else {
			statusDetail.setINTERNALSTATUS(OrderStatus.Order_received_by_seller
					.getValue());
			statusDetail.setEXTERNALSTATUS(OrderStatus.Order_received_by_seller
					.getValue());
			// statusDetail.setINTERNALTEXT("Order imported to InventorySource CM");
			// statusDetail.setEXTERNALTEXT("Order imported to InventorySource CM");

		}
		status.getADIOSORDERSTATUSDETAILOrINTERNALTEXTOrEXTERNALTEXT().add(
				statusDetail);
		statusRequest.getADIOSSTATUS().add(status);
		DefaultHttpClient client = new DefaultHttpClient();
		// client.setRedirectStrategy(new LaxRedirectStrategy());
		try {

			JAXBContext context = JAXBContext
					.newInstance(ADIOSORDERSTATUSTRANSMISSION.class);
			Marshaller marshaller = context.createMarshaller();
			OutputStream os = new ByteArrayOutputStream();
			marshaller.marshal(statusRequest, os);
			LOG.info(os.toString());
			HttpPost post = new HttpPost(STORE_ORDER_STATUS_UPDATE_URL);
			List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(
					1);
			nameValuePairs.add(new BasicNameValuePair("order_status_data", os
					.toString()));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(post);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				LOG.info("Order Status updated");
			}

			byte[] resp = new byte[(int) response.getEntity()
					.getContentLength()];
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
		return true;
	}

}
