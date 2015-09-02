package salesmachine.oim.suppliers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Query;
import org.hibernate.Session;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.util.StringHandle;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;

public class OrderTest {
	private static final String ASCII = "ASCII";
	private static Map<String, String> orderData = new HashMap<String, String>();

	private static List<PHIHVAData> phiDataList = new ArrayList<PHIHVAData>();
	private static List<PHIHVAData> hvaDataList = new ArrayList<PHIHVAData>();
	private static Set<PHIHVAData> phiHvaDataSet = new HashSet<PHIHVAData>();

	private static String startDate = "";// 08/18/2015
	private static String endDate = "";// MM/DD/YYYY
	private static final String phiConfirmPath = "/home/staging/cm-evoxOrders/report/evox/confirmations/";
	private static final String phiShippingPath = "/home/staging/cm-evoxOrders/report/evox/shipping/";
	private static final String phiTrackingPath = "/home/staging/cm-evoxOrders/report/evox/tracking/";

	private static final String hvaConfirmPath = "/home/staging/cm-evoxOrders/report/70757/confirmations/";
	private static final String hvaShippingPath = "/home/staging/cm-evoxOrders/report/70757/shipping/";
	private static final String hvaTrackingPath = "/home/staging/cm-evoxOrders/report/70757/tracking/";
	private static int phiConfirmationDownloadCount = 0;
	private static int phiShippingCount = 0;
	private static int phiTrackingCount = 0;
	private static int hvaConfirmationDownloadCount = 0;
	private static int hvaShippingCount = 0;
	private static int hvaTrackingCount = 0;

	public static void main(String[] args) throws IOException, FTPException,
			ParseException {
		long processStartTime = System.currentTimeMillis();
		if (args.length == 2) {
			startDate = args[0];
			endDate = args[1];
		}
		long startTime = System.currentTimeMillis();
		System.out.println("started downloading files from ftp at --"
				+ startTime);
		downloadPhiData();
		downloadHvaData();
		Long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("total time taken in downloding files --"
				+ totalTime);
		getDataForPHI();
		getDataForHVA();
		File file = new File(
				"/home/staging/cm-evoxOrders/report/orderStatusReport.csv");
		FileWriter fw = new FileWriter(file);
		fw.write("STORE_ORDER_ITEM_ID, DETAIL_ID, SUPPLIER_ORDER_NUMBER, STATUS_VALUE, insertion_tm, processing_tm, isConfirmed, isShipped, isTracked, location\n");
		for (PHIHVAData hvaData : hvaDataList) {
			int index = phiDataList.indexOf(hvaData);
			if (index != -1) {
				PHIHVAData data2 = phiDataList.get(index);
				if (data2.equals(hvaData)) {
					if ((data2.isConfirmed() || data2.isShipped() || data2
							.isTracked())) {
						phiHvaDataSet.add(data2);
					} else
						phiHvaDataSet.add(hvaData);
				}
			} else {
				phiHvaDataSet.add(hvaData);
			}
		}
		phiHvaDataSet.addAll(phiDataList);

		for (PHIHVAData data : phiHvaDataSet) {
			fw.write(data.toString());
		}
		fw.close();
		sendEmail(file.getAbsolutePath());
		long processEndTime = System.currentTimeMillis();
		long totalProcessTime = processEndTime - processStartTime;
		System.out
				.println("total files downloaded from PHI confirmation directory --"
						+ phiConfirmationDownloadCount);
		System.out
				.println("total files downloaded from PHI confirmation directory --"
						+ phiShippingCount);
		System.out
				.println("total files downloaded from PHI confirmation directory --"
						+ phiTrackingCount);
		System.out
				.println("total files downloaded from PHI confirmation directory --"
						+ hvaConfirmationDownloadCount);
		System.out
				.println("total files downloaded from PHI confirmation directory --"
						+ hvaShippingCount);
		System.out
				.println("total files downloaded from PHI confirmation directory --"
						+ hvaTrackingCount);
		System.out
				.println("total time taken in this run --" + totalProcessTime);
		System.out.println("Process complete...");
	}

	private static void sendEmail(String fileName) {
		String emailBody = "Please find attached order status file.";
		String emailSubject = null;
		if (!StringHandle.removeNull(startDate).equals("")
				&& !StringHandle.removeNull(endDate).equals("")) {
			emailSubject = "Evox Order status for " + startDate + " to "
					+ endDate;
		} else
			emailSubject = "Evox Order status for last 2 days";

		EmailUtil.sendEmailWithAttachment("orders@inventorysource.com",
				"support@inventorysource.com", "abheeshek@inventorysource.com, kelly@inventorysource.com, andrew@inventorysource.com",
				emailSubject, emailBody, fileName);
		System.out.println("Email sent successfully");
	}

	private static void downloadHvaData() {

		System.out.println("downloading files from HVA location...");
		try {
			FTPClient ftp = new FTPClient();
			System.out.println(1);
			ftp.setRemoteHost("ftp1.unfi.com");
			System.out.println(2);
			ftp.setDetectTransferMode(true);
			ftp.connect();
			ftp.login("70757", "vU!6akAB");
			String fileDir = "/users/70757/";
			FTPFile[] ftpFiles = ftp.dirDetails(fileDir);
			for (int i = 0; i < ftpFiles.length; i++) {
				FTPFile ff = ftpFiles[i];
				if (ff.isDir()) {
					String dirName = ff.getName() + "/";
					if (ff.getName().startsWith("confirmations")) {
						System.out
								.println("downloading files from HVA confirmations.......");
						FTPFile[] confirmedFileList = ftp.dirDetails(fileDir
								+ dirName);
						for (int j = 0; j < confirmedFileList.length; j++) {
							String fileNme = ((FTPFile) confirmedFileList[j])
									.getName();
							System.out.println("Getting file : " + fileNme);
							File tmp = new File(hvaConfirmPath + fileNme);
							if (!tmp.exists()) {
								ftp.get(hvaConfirmPath + fileNme, fileDir
										+ dirName + fileNme);
								hvaConfirmationDownloadCount++;
							}
						}
					}
					if (ff.getName().startsWith("shipping")) {
						System.out
								.println("downloading files from HVA shipping.......");
						FTPFile[] confirmedFileList = ftp.dirDetails(fileDir
								+ dirName);
						for (int j = 0; j < confirmedFileList.length; j++) {
							String fileNme = ((FTPFile) confirmedFileList[j])
									.getName();
							System.out.println("Getting file : " + fileNme);
							File tmp = new File(hvaShippingPath + fileNme);
							if (!tmp.exists()) {
								ftp.get(hvaShippingPath + fileNme, fileDir
										+ dirName + fileNme);
								hvaShippingCount++;
							}
						}
					}
					if (ff.getName().startsWith("tracking")) {
						System.out
								.println("downloading files from HVA tracking.......");
						FTPFile[] confirmedFileList = ftp.dirDetails(fileDir
								+ dirName);
						for (int j = 0; j < confirmedFileList.length; j++) {
							String fileNme = ((FTPFile) confirmedFileList[j])
									.getName();
							System.out.println("Getting file : " + fileNme);
							File tmp = new File(hvaTrackingPath + fileNme);
							if (!tmp.exists()) {
								ftp.get(hvaTrackingPath + fileNme, fileDir
										+ dirName + fileNme);
								hvaTrackingCount++;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void downloadPhiData() {
		System.out.println("downloading files from PHI location...");
		FTPClient ftp = new FTPClient();
		try {
			ftp.setRemoteHost("ftp1.unfi.com");
			ftp.setDetectTransferMode(true);
			ftp.connect();
			ftp.login("evox", "evoftp093!");
			String fileDir = "/users/evox/";
			FTPFile[] ftpFiles = ftp.dirDetails(fileDir);
			for (int i = 0; i < ftpFiles.length; i++) {
				FTPFile ff = ftpFiles[i];
				if (ff.getName().startsWith(".")
						|| ff.getName().startsWith(".."))
					continue;
				if (ff.isDir()) {
					String dirName = ff.getName() + "/";
					System.out.println("dirName -->" + dirName);

					if (ff.getName().startsWith("confirmations")) {
						System.out
								.println("downloading files from PHI confirmations.......");
						FTPFile[] confirmedFileList = ftp.dirDetails(fileDir
								+ dirName);
						for (int j = 0; j < confirmedFileList.length; j++) {
							String fileNme = ((FTPFile) confirmedFileList[j])
									.getName();
							System.out.println("Getting file : " + fileNme);
							File tmp = new File(phiConfirmPath + fileNme);
							if (!tmp.exists()) {
								ftp.get(phiConfirmPath + fileNme, fileDir
										+ dirName + fileNme);
								phiConfirmationDownloadCount++;
							}
						}

					}
					if (ff.getName().startsWith("shipping")) {
						System.out
								.println("downloading files from PHI shipping.......");
						FTPFile[] confirmedFileList = ftp.dirDetails(fileDir
								+ dirName);
						for (int j = 0; j < confirmedFileList.length; j++) {
							String fileNme = ((FTPFile) confirmedFileList[j])
									.getName();
							System.out.println("Getting file : " + fileNme);
							File tmp = new File(phiShippingPath + fileNme);
							if (!tmp.exists()) {
								ftp.get(phiShippingPath + fileNme, fileDir
										+ dirName + fileNme);
								phiShippingCount++;
							}
						}
					}
					if (ff.getName().startsWith("tracking")) {
						System.out
								.println("downloading files from PHI tracking.......");
						FTPFile[] confirmedFileList = ftp.dirDetails(fileDir
								+ dirName);
						for (int j = 0; j < confirmedFileList.length; j++) {
							String fileNme = ((FTPFile) confirmedFileList[j])
									.getName();
							System.out.println("Getting file : " + fileNme);
							File tmp = new File(phiTrackingPath + fileNme);
							if (!tmp.exists()) {
								ftp.get(phiTrackingPath + fileNme, fileDir
										+ dirName + fileNme);
								phiTrackingCount++;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void getDataForPHI() throws IOException {
		Set<String> shippingNames = new TreeSet<String>();
		Set<String> trackingNames = new TreeSet<String>();
		File folder = new File(phiShippingPath);
		File[] files = folder.listFiles();
		for (File file : files) {
			shippingNames.add(file.getName());
		}
		folder = new File(phiTrackingPath);
		files = folder.listFiles();
		for (File file : files) {
			trackingNames.add(file.getName());
		}
		folder = new File(phiConfirmPath);
		files = folder.listFiles();
		for (File file : files) {
			String confirmationFile = file.getName();
			if (confirmationFile.equals("..") || confirmationFile.equals("."))
				continue;
			Path path = Paths.get(phiConfirmPath + confirmationFile);
			byte[] confirmationFileData = Files.readAllBytes(path);
			Map<Integer, String> orderDataMap = parseFileData(confirmationFileData);
			parseOrderConfirmation(orderDataMap);
		}
		Session session = SessionManager.currentSession();// TO_CHAR(subQuery.FOLLOW_UP_COMPLETE_TM,'DD-MON-YYYY')
		Query query = null;
		if (!StringHandle.removeNull(startDate).equals("")
				&& !StringHandle.removeNull(endDate).equals("")) {
			query = session
					.createSQLQuery("select STORE_ORDER_ITEM_ID, DETAIL_ID, SUPPLIER_ORDER_NUMBER, os.STATUS_VALUE , to_char(INSERTION_TM, 'DD-MON-YYYY'), to_char(PROCESSING_TM, 'DD-MON-YYYY') from "
							+ "OIM_ORDER_DETAILS od inner join OIM_ORDER_STATUSES os on os.STATUS_ID=od.STATUS_ID where ORDER_ID "
							+ "in (select ORDER_ID from OIM_ORDERS where BATCH_ID in (select BATCH_ID from OIM_ORDER_BATCHES where "
							+ "CHANNEL_ID = 2941 and CREATION_TM > TO_DATE('"
							+ startDate
							+ " 23:59:59','MM/DD/YYYY HH24:MI:SS') and "
							+ "CREATION_TM < TO_DATE('"
							+ endDate
							+ " 23:59:59','MM/DD/YYYY HH24:MI:SS')))");
		} else {
			query = session
					.createSQLQuery("select STORE_ORDER_ITEM_ID, DETAIL_ID, SUPPLIER_ORDER_NUMBER, os.STATUS_VALUE , to_char(INSERTION_TM, 'DD-MON-YYYY'), to_char(PROCESSING_TM, 'DD-MON-YYYY') from "
							+ "OIM_ORDER_DETAILS od inner join OIM_ORDER_STATUSES os on os.STATUS_ID=od.STATUS_ID where ORDER_ID "
							+ "in (select ORDER_ID from OIM_ORDERS where BATCH_ID in (select BATCH_ID from OIM_ORDER_BATCHES where "
							+ "CHANNEL_ID = 2941 and CREATION_TM >=TRUNC( sysdate-2) and (CREATION_TM)<=TRUNC( sysdate)))");
		}
		List<Object[]> result = query.list();
		StringBuilder sb = new StringBuilder();
		sb.append("STORE_ORDER_ITEM_ID, DETAIL_ID, SUPPLIER_ORDER_NUMBER, STATUS_VALUE, insertion_tm, processing_tm, isConfirmed, isShipped, isTracked\n");
		for (int i = 0; i < result.size(); i++) {
			Object[] values = result.get(i);
			String STORE_ORDER_ITEM_ID = (String) values[0];
			String DETAIL_ID = ((BigDecimal) values[1]).toString();
			String SUPPLIER_ORDER_NUMBER = (String) values[2];
			String STATUS_VALUE = (String) values[3];
			String insertion_tm = (String) values[4];
			String processing_tm = (String) values[5];
			boolean isConfirmed = orderData.containsKey(SUPPLIER_ORDER_NUMBER) ? true
					: false;

			boolean isShipped = shippingNames.contains("40968.O"
					+ orderData.get(SUPPLIER_ORDER_NUMBER) + "A.txt") ? true
					: false;
			boolean isTracked = trackingNames.contains("40968.T"
					+ orderData.get(SUPPLIER_ORDER_NUMBER) + "S.txt") ? true
					: false;

			PHIHVAData phihvaData = new PHIHVAData(STORE_ORDER_ITEM_ID,
					DETAIL_ID, SUPPLIER_ORDER_NUMBER, STATUS_VALUE,
					insertion_tm, processing_tm, isConfirmed, isShipped,
					isTracked, "PHI");
			phiDataList.add(phihvaData);
		}
		orderData.clear();
	}

	private static void getDataForHVA() throws IOException {
		Set<String> shippingNames = new TreeSet<String>();
		Set<String> trackingNames = new TreeSet<String>();
		File folder = new File(hvaShippingPath);
		File[] files = folder.listFiles();
		for (File file : files) {
			shippingNames.add(file.getName());
		}
		folder = new File(hvaTrackingPath);
		files = folder.listFiles();
		for (File file : files) {
			trackingNames.add(file.getName());
		}
		folder = new File(hvaConfirmPath);
		files = folder.listFiles();
		for (File file : files) {
			String confirmationFile = file.getName();
			if (confirmationFile.equals("..") || confirmationFile.equals("."))
				continue;
			Path path = Paths.get(hvaConfirmPath + confirmationFile);
			byte[] confirmationFileData = Files.readAllBytes(path);
			Map<Integer, String> orderDataMap = parseFileData(confirmationFileData);
			parseOrderConfirmation(orderDataMap);
		}
		Session session = SessionManager.currentSession();// TO_CHAR(subQuery.FOLLOW_UP_COMPLETE_TM,'DD-MON-YYYY')
		Query query = null;
		if (!StringHandle.removeNull(startDate).equals("")
				&& !StringHandle.removeNull(endDate).equals("")) {
			query = session
					.createSQLQuery("select STORE_ORDER_ITEM_ID, DETAIL_ID, SUPPLIER_ORDER_NUMBER, os.STATUS_VALUE , to_char(INSERTION_TM, 'DD-MON-YYYY'), to_char(PROCESSING_TM, 'DD-MON-YYYY') from "
							+ "OIM_ORDER_DETAILS od inner join OIM_ORDER_STATUSES os on os.STATUS_ID=od.STATUS_ID where ORDER_ID "
							+ "in (select ORDER_ID from OIM_ORDERS where BATCH_ID in (select BATCH_ID from OIM_ORDER_BATCHES where "
							+ "CHANNEL_ID = 2941 and CREATION_TM > TO_DATE('"
							+ startDate
							+ " 23:59:59','MM/DD/YYYY HH24:MI:SS') and "
							+ "CREATION_TM < TO_DATE('"
							+ endDate
							+ " 23:59:59','MM/DD/YYYY HH24:MI:SS')))");
		} else {
			query = session
					.createSQLQuery("select STORE_ORDER_ITEM_ID, DETAIL_ID, SUPPLIER_ORDER_NUMBER, os.STATUS_VALUE , to_char(INSERTION_TM, 'DD-MON-YYYY'), to_char(PROCESSING_TM, 'DD-MON-YYYY') from "
							+ "OIM_ORDER_DETAILS od inner join OIM_ORDER_STATUSES os on os.STATUS_ID=od.STATUS_ID where ORDER_ID "
							+ "in (select ORDER_ID from OIM_ORDERS where BATCH_ID in (select BATCH_ID from OIM_ORDER_BATCHES where "
							+ "CHANNEL_ID = 2941 and CREATION_TM >=TRUNC( sysdate-2) and (CREATION_TM)<=TRUNC( sysdate)))");
		}
		List<Object[]> result = query.list();
		StringBuilder sb = new StringBuilder();
		sb.append("STORE_ORDER_ITEM_ID, DETAIL_ID, SUPPLIER_ORDER_NUMBER, STATUS_VALUE, insertion_tm, processing_tm, isConfirmed, isShipped, isTracked\n");
		for (int i = 0; i < result.size(); i++) {
			Object[] values = result.get(i);
			String STORE_ORDER_ITEM_ID = (String) values[0];
			String DETAIL_ID = ((BigDecimal) values[1]).toString();
			String SUPPLIER_ORDER_NUMBER = (String) values[2];
			String STATUS_VALUE = (String) values[3];
			String insertion_tm = (String) values[4];
			String processing_tm = (String) values[5];
			boolean isConfirmed = orderData.containsKey(SUPPLIER_ORDER_NUMBER) ? true
					: false;

			boolean isShipped = shippingNames.contains("70757.O"
					+ orderData.get(SUPPLIER_ORDER_NUMBER) + "A.txt") ? true
					: false;
			boolean isTracked = trackingNames.contains("70757.T"
					+ orderData.get(SUPPLIER_ORDER_NUMBER) + "S.txt") ? true
					: false;
			PHIHVAData phihvaData = new PHIHVAData(STORE_ORDER_ITEM_ID,
					DETAIL_ID, SUPPLIER_ORDER_NUMBER, STATUS_VALUE,
					insertion_tm, processing_tm, isConfirmed, isShipped,
					isTracked, "HVA");

			hvaDataList.add(phihvaData);
		}
		orderData.clear();
	}

	private static Map<Integer, String> parseFileData(
			byte[] confirmationFileData) {
		Map<Integer, String> fileData = null;
		try {
			fileData = new HashMap<Integer, String>();
			InputStream inputStream = new ByteArrayInputStream(
					confirmationFileData);
			Reader r = new InputStreamReader(inputStream, ASCII);

			int i = -1, lineNum = 1;
			char c;
			StringBuilder sb = new StringBuilder();
			while ((i = r.read()) != -1) {
				c = (char) i;
				if (c == '"')
					continue;
				if (c != '\n') {
					sb.append(c);
				} else {
					fileData.put(lineNum, sb.toString());
					lineNum++;
					sb = new StringBuilder();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileData;
	}

	private static void parseOrderConfirmation(
			Map<Integer, String> orderConfirmationMap) {
		for (Iterator itr = orderConfirmationMap.values().iterator(); itr
				.hasNext();) {
			String line = (String) itr.next();
			String[] lineArray = line
					.split(",(?=(?>[^\"]*\"[^\"]*\")*[^\"]*$)");
			if (lineArray.length == 9) {
				orderData.put(lineArray[6], lineArray[0]);
			}
		}
		// return orderData;
	}

	private static Object deserialize(String filename) throws IOException,
			ClassNotFoundException {
		FileInputStream fileIn = new FileInputStream(filename);
		ObjectInputStream in = new ObjectInputStream(fileIn);
		Object obj = in.readObject();
		in.close();
		fileIn.close();
		return obj;
	}

	private static void serialize(String filename, Object obj)
			throws IOException {
		FileOutputStream fileOut = new FileOutputStream(filename);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(obj);
		out.close();
		fileOut.close();
	}

}
