package salesmachine.oim.suppliers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.Reps;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.util.StringHandle;

import com.enterprisedt.net.ftp.FTPClient;

public class HonestGreen extends Supplier {
	private static final Logger log = LoggerFactory
			.getLogger(HonestGreen.class);
	// private static final byte BLANK_SPACE = ' ';
	private static final byte[] NEW_LINE = new byte[] { '\r', '\n' };
	private static final byte[] HG_EOF = new byte[] { '*', '*', '*', 'E', 'O',
			'F', '*', '*', '*' };
	private static final byte[] FIL = new byte[] { 'F', 'I', 'L' };
	private static final byte[] COMMA = new byte[] { ',' };

	@Override
	public void sendOrders(Integer vendorId, OimVendorSuppliers ovs, List orders) {
		log.info("Sending orders of Account: {}", ovs.getAccountNumber());
		if (ovs.getTestMode().equals(1))
			return;
		// populate orderSkuPrefixMap with channel id and the prefix to be used
		// for the given supplier.
		orderSkuPrefixMap = setSkuPrefixForOrders(ovs);
		Session session = SessionManager.currentSession();
		Reps r = (Reps) session.createCriteria(Reps.class)
				.add(Restrictions.eq("vendorId", vendorId)).uniqueResult();
		Vendors v = new Vendors();
		v.setVendorId(r.getVendorId());
		String name = StringHandle.removeNull(r.getFirstName()) + " "
				+ StringHandle.removeNull(r.getLastName());
		String emailContent = "Dear " + name + "<br>";
		emailContent += "<br>Following is the status of the orders file uploaded on FTP for the supplier "
				+ ovs.getOimSuppliers().getSupplierName() + " : - <br>";

		String accountNumber = ovs.getAccountNumber();

		for (Object object : orders) {
			boolean emailNotification = false;

			if (object instanceof OimOrders) {
				OimOrders order = (OimOrders) object;
				try {
					String fileName = createOrderFile(order, ovs);

					FTPClient ftp = new FTPClient();
					ftp.setRemoteHost("ftp1.unfi.com");
					ftp.setDetectTransferMode(true);
					ftp.connect();
					ftp.login(ovs.getLogin(), ovs.getPassword());
					ftp.setTimeout(60 * 1000 * 60 * 5);
					ftp.put(fileName, fileName);
					ftp.quit();

					String emailBody = "Account Number : "
							+ accountNumber
							+ "\n Find attached order file for the orders from my store.";
					String emailSubject = fileName;
					EmailUtil
							.sendEmailWithAttachmentAndBCC(
									r.getLogin(),
									"support@inventorysource.com",
									null,
									null,
									"oim@inventorysource.com,aruppar@inventorysource.com",
									emailSubject, emailBody, fileName, "");

					for (Iterator detailIt = order.getOimOrderDetailses()
							.iterator(); detailIt.hasNext();) {
						OimOrderDetails detail = (OimOrderDetails) detailIt
								.next();
						successfulOrders.add(detail.getDetailId());
					}

					if (order.getOimOrderBatches().getOimChannels()
							.getEmailNotifications() == 1) {
						emailNotification = true;
						String orderStatus = "Successfully Placed";
						emailContent += "<b>Store Order ID "
								+ order.getStoreOrderId() + "</b> -> "
								+ orderStatus + " ";
						emailContent += "<br>";
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				if (emailNotification) {
					emailContent += "<br>Thanks, <br>Inventorysource support<br>";
					logStream
							.println("Sending email to user about order processing");
					log.debug("Sending email to user about order processing");
					EmailUtil.sendEmail(r.getLogin(),
							"support@inventorysource.com", "",
							"Order processing update results", emailContent,
							"text/html");
				}
			}
		}
	}

	private String createOrderFile(OimOrders order, OimVendorSuppliers ovs) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
		String uploadfilename = "HG_" + ovs.getAccountNumber() + "_"
				+ sdf.format(new Date()) + ".txt";
		File f = new File(uploadfilename);
		log.debug("Creating order file for OrderId:{}", order.getOrderId());
		try {
			FileOutputStream fOut = new FileOutputStream(f);
			// Integer orderSize = order.getOimOrderDetailses().size();
			fOut.write("1".getBytes("ASCII"));
			fOut.write(NEW_LINE);
			fOut.write(ovs.getAccountNumber().getBytes("ASCII"));
			fOut.write(COMMA);
			// fOut.write(BLANK_SPACE);
			fOut.write(COMMA);
			fOut.write(order.getStoreOrderId().getBytes("ASCII"));
			fOut.write(COMMA);
			// fOut.write(BLANK_SPACE);
			fOut.write(COMMA);
			fOut.write(FIL);
			fOut.write(COMMA);
			// fOut.write(BLANK_SPACE);
			fOut.write(NEW_LINE);
			fOut.write(StringHandle.removeNull(order.getDeliveryName())
					.toUpperCase().getBytes("ASCII"));
			fOut.write(COMMA);
			fOut.write(StringHandle
					.removeNull(order.getDeliveryStreetAddress()).toUpperCase()
					.getBytes("ASCII"));
			fOut.write(COMMA);
			fOut.write(StringHandle.removeNull(order.getDeliverySuburb())
					.toUpperCase().getBytes("ASCII"));
			fOut.write(COMMA);
			fOut.write(StringHandle.removeNull(order.getDeliveryCity())
					.toUpperCase().getBytes("ASCII"));
			fOut.write(COMMA);
			fOut.write(StringHandle.removeNull(order.getDeliveryState())
					.toUpperCase().getBytes("ASCII"));
			fOut.write(COMMA);
			fOut.write(StringHandle.removeNull(order.getDeliveryZip())
					.toUpperCase().getBytes("ASCII"));
			fOut.write(COMMA);
			fOut.write(StringHandle.removeNull(order.getDeliveryPhone())
					.toUpperCase().getBytes("ASCII"));
			fOut.write(COMMA);
			fOut.write('A');
			fOut.write(COMMA);
			fOut.write("5001".getBytes("ASCII"));
			fOut.write(NEW_LINE);
			for (OimOrderDetails od : ((Set<OimOrderDetails>) order
					.getOimOrderDetailses())) {
				String skuPrefix = null, sku = od.getSku();
				if (!orderSkuPrefixMap.isEmpty()) {
					skuPrefix = orderSkuPrefixMap.values().toArray()[0]
							.toString();
				}
				skuPrefix = StringHandle.removeNull(skuPrefix);
				if (sku.startsWith(skuPrefix)) {
					sku = sku.substring(skuPrefix.length());
				}
				fOut.write(sku.getBytes("ASCII"));
				fOut.write(COMMA);
				// fOut.write(BLANK_SPACE);
				fOut.write(COMMA);
				fOut.write(od.getQuantity().toString().getBytes("ASCII"));
				fOut.write(COMMA);
				// fOut.write(BLANK_SPACE);
				fOut.write(COMMA);
				// fOut.write(BLANK_SPACE);
				fOut.write(COMMA);
				// fOut.write(BLANK_SPACE);
				// fOut.write(COMMA);
				fOut.write(NEW_LINE);
			}
			fOut.write(HG_EOF);
			fOut.write(NEW_LINE);
			fOut.flush();
			fOut.close();
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return uploadfilename;
	}
}
