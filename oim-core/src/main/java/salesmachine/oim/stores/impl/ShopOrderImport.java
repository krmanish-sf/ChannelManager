package salesmachine.oim.stores.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimOrderProcessingRule;
import salesmachine.hibernatehelper.PojoHelper;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.api.IOrderImport;
import salesmachine.oim.stores.modal.shop.order.status.ADIOSHEADER;
import salesmachine.oim.stores.modal.shop.order.status.ADIOSORDERSTATUSDETAIL;
import salesmachine.oim.stores.modal.shop.order.status.ADIOSORDERSTATUSTRANSMISSION;
import salesmachine.oim.stores.modal.shop.order.status.ADIOSSTATUS;
import salesmachine.oim.stores.modal.shop.order.status.OrderStatus;
import salesmachine.util.OimLogStream;
import salesmachine.util.StringHandle;

public class ShopOrderImport implements IOrderImport {
	private static final String STORE_ORDER_STATUS_UPDATE_URL = "https://admin-amos.shop.com/get_order_status+251.xhtml?";
	private static final Logger LOG = LoggerFactory
			.getLogger(ShopOrderImport.class);
	private String catalogId;
	private Session dbSession;
	private OimChannels m_channel;
	private OimOrderProcessingRule m_orderProcessingRule;
	private OimLogStream logStream;

	@Override
	public boolean init(int channelID, Session dbSession, OimLogStream log) {
		this.dbSession = dbSession;
		if (log != null)
			this.logStream = log;
		else
			this.logStream = new OimLogStream();

		Transaction tx = dbSession.beginTransaction();
		Query query = dbSession
				.createQuery("from salesmachine.hibernatedb.OimChannels as c where c.channelId=:channelID");
		query.setInteger("channelID", channelID);
		tx.commit();
		if (!query.iterate().hasNext()) {
			System.out.println("No channel found for channel id: " + channelID);
			return false;
		}

		m_channel = (OimChannels) query.iterate().next();
		LOG.info("Channel : {} initialized", m_channel.getChannelName());
		catalogId = StringHandle.removeNull(PojoHelper
				.getChannelAccessDetailValue(m_channel,
						OimConstants.CHANNEL_ACCESSDETAIL_SHOP_CATALOGID));
		if (StringHandle.isNullOrEmpty(catalogId)) {
			LOG.error("Channel setup is not correct. Please provide this details.");
			this.logStream
					.println("Channel setup is not correct. Please provide this details.");
			return false;
		}

		query = dbSession
				.createQuery("select opr from salesmachine.hibernatedb.OimOrderProcessingRule opr where opr.deleteTm is null and opr.oimChannels=:chan");
		query.setEntity("chan", m_channel);
		Iterator iter = query.iterate();
		if (iter.hasNext()) {
			m_orderProcessingRule = (OimOrderProcessingRule) iter.next();
		}
		return true;
	}

	/**
	 * This method is not supported on this channel type, as Order are posted in
	 * real time by shop.com to our listener url.
	 * */
	@Override
	public boolean getVendorOrders() {
		return false;
	}

	@Override
	public boolean updateStoreOrder(String storeOrderId, String orderStatus,
			String trackingDetail) {
		ADIOSORDERSTATUSTRANSMISSION statusRequest = new ADIOSORDERSTATUSTRANSMISSION();
		ADIOSHEADER header = new ADIOSHEADER();
		header.setCATALOGID(catalogId);
		DateFormat df = new SimpleDateFormat("M/d/yyyy h:mm:ss a");
		header.setDATETIMESTAMP(df.format(new Date()));
		statusRequest.setADIOSHEADER(header);
		ADIOSSTATUS status = new ADIOSSTATUS();
		status.setINVOICENUM(storeOrderId);
		ADIOSORDERSTATUSDETAIL statusDetail = new ADIOSORDERSTATUSDETAIL();
		statusDetail.setINTERNALSTATUS(OrderStatus.Order_received_by_seller
				.getValue());
		statusDetail.setEXTERNALSTATUS(OrderStatus.Order_received_by_seller
				.getValue());
		//statusDetail.setINTERNALTEXT("Order imported to InventorySource CM");
		//statusDetail.setEXTERNALTEXT("Order imported to InventorySource CM");

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
			HttpPost post = new HttpPost(STORE_ORDER_STATUS_UPDATE_URL
					+ URLEncoder.encode("order_status_data=" + os.toString()));
			HttpResponse response = client.execute(post);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				LOG.debug("Order Status updated");
			}

			byte[] resp = new byte[(int) response.getEntity()
					.getContentLength()];
			//response.getEntity().getContent().read(resp);
			int c;
			while ((c = response.getEntity().getContent().read()) != -1) {
				System.out.print((char) c);
			}
		} catch (JAXBException e1) {
			LOG.error(e1.getMessage());
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
		return true;
	}

}
