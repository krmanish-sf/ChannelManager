package salesmachine.oim.suppliers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.stores.impl.ShopifyOrderImport;
import salesmachine.oim.suppliers.FtpDetail.WareHouseType;
import salesmachine.util.StringHandle;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;

public class OrderTest {
  private static final Logger log = LoggerFactory.getLogger(OrderTest.class);

  private static final String ASCII = "ASCII";

  private static String startDate = "";// 08/18/2015
  private static String endDate = "";// MM/DD/YYYY

  public static void main(String[] args) throws IOException, FTPException, ParseException {
    long processStartTime = System.currentTimeMillis();
    Map<FtpDetail, String> ftpDetailMap;
    if (args.length == 2) {
      startDate = args[0];
      endDate = args[1];
    }
    long startTime = System.currentTimeMillis();
    log.info("Strated getting all ftp details...");
    ftpDetailMap = getFtpDetails();
    log.info("Total {} number of ftp details found", ftpDetailMap.size());
    Map<String, List<PHIHVAData>> vendorDataMap = new HashMap<String, List<PHIHVAData>>();
    List<File> fileList = new ArrayList<File>();
    for (Iterator<FtpDetail> itr = ftpDetailMap.keySet().iterator(); itr.hasNext();) {
      FtpDetail ftpDetail = itr.next();
      long fileCleanupStartTime = System.currentTimeMillis();
      String fileCleanupStartTimeStr = convertLongToDateString(fileCleanupStartTime);
      log.info("ftp file move process started at {} for {}", fileCleanupStartTimeStr, ftpDetail);
      try {
        ftpMoveFile(ftpDetail);
      } catch (Exception e) {
        log.error("Error occure while moving the files at ftp {}", ftpDetail);
      }
      long fileCleanupEndTime = System.currentTimeMillis();
      String fileCleanupEndTimStr = convertLongToDateString(fileCleanupEndTime);
      log.info("ftp file move process ended at {} for {}", fileCleanupEndTimStr, ftpDetail);
      String vendorId = ftpDetailMap.get(ftpDetail);
      int confCount = 0;
      int shipCount = 0;
      int trackCount = 0;
      // "/home/staging/cm-orders/report/"
      String confirmationPath = "/home/staging/cm-orders/report/" + vendorId + "_"
          + ftpDetail.getUserName() + "/confirmations/";
      String shippingPath = "/home/staging/cm-orders/report/" + vendorId + "_"
          + ftpDetail.getUserName() + "/shipping/";
      String trackingPath = "/home/staging/cm-orders/report/" + vendorId + "_"
          + ftpDetail.getUserName() + "/tracking/";
      File confirmationDir = new File(confirmationPath);
      if (!confirmationDir.exists())
        confirmationDir.mkdirs();
      File trackingDir = new File(trackingPath);
      if (!trackingDir.exists())
        trackingDir.mkdirs();
      File shipingDir = new File(shippingPath);
      if (!shipingDir.exists())
        shipingDir.mkdirs();
      boolean isPHI = ftpDetail.getWhareHouseType()
          .getWharehouseType() == OimConstants.SUPPLIER_METHOD_TYPE_HG_PHI.intValue();
      try {
        long fileDownloadStartTime = System.currentTimeMillis();
        String fileDownloadStartTimeStr = convertLongToDateString(fileDownloadStartTime);
        log.info("Started downloading files at {} from ftp : {}", fileDownloadStartTimeStr,
            ftpDetail);
        downloadFiles(ftpDetail, vendorId, confirmationPath, shippingPath, trackingPath, confCount,
            shipCount, trackCount);
        log.info("Total number of files matched at confirmation dir - {}", confCount);
        log.info("Total number of files matched at shipping dir - {}", shipCount);
        log.info("Total number of files matched at tracking dir - {}", trackCount);
      } catch (Exception e) {
        log.error("error occure while downloading files from ftp ", e);
      }
      long fileDownloadEndTime = System.currentTimeMillis();
      String fileDownloadStartEndStr = convertLongToDateString(fileDownloadEndTime);
      log.info("Files download process completed at {} for ftp : {}", fileDownloadStartEndStr,
          ftpDetail);
      List<PHIHVAData> dataList = getData(ftpDetail, confirmationPath, shippingPath, trackingPath,
          isPHI);
      if (vendorDataMap.get(vendorId) != null) {
        List<PHIHVAData> vendorDataList = vendorDataMap.get(vendorId);
        vendorDataList.addAll(dataList);
      } else {
        vendorDataMap.put(vendorId, dataList);
      }
    }

    long reportStartTime = System.currentTimeMillis();
    String reportStartTimeStr = convertLongToDateString(reportStartTime);
    log.info("Started creating report at {}", reportStartTimeStr);
    makeReportFromData(vendorDataMap, fileList);
    try {
      sendEmail(fileList);
    } catch (Exception e) {
      log.error("Error occured during sending email", e);
    }
    long sendEmailCompleteTime = System.currentTimeMillis();
    String sendEmailCompleteTimeStr = convertLongToDateString(sendEmailCompleteTime);
    log.info("report created and sent as an email at {}", sendEmailCompleteTimeStr);
    log.info("Process complete...");
  }

  private static void makeReportFromData(Map<String, List<PHIHVAData>> vendorDataMap,
      List<File> fileList) {
    for (Iterator<String> itr = vendorDataMap.keySet().iterator(); itr.hasNext();) {
      String vendorId = itr.next();
      // File file = new File("/home/staging/cm-orders/report/" + vendorId + "_" + "Report.csv");
      List<PHIHVAData> dataList = vendorDataMap.get(vendorId);
      if (dataList == null || dataList.size() == 0)
        continue;
      File file = new File("/home/staging/cm-orders/report/" + vendorId + "_" + "Report.csv");
      fileList.add(file);
      try {
        FileWriter fw = new FileWriter(file);
        fw.write(
            "STORE_ORDER_ITEM_ID, DETAIL_ID, SUPPLIER_ORDER_NUMBER, STATUS_VALUE, insertion_tm, processing_tm, isConfirmed, isShipped, isTracked, location\n");
        for (PHIHVAData data : dataList) {
          fw.write(data.toString());
        }
        fw.close();
      } catch (IOException e) {
        log.info("Error occure while creating order status file : {}", file.getName());
      }
    }
  }

  private static Map<FtpDetail, String> getFtpDetails() {
    Session session = SessionManager.currentSession();// TO_CHAR(subQuery.FOLLOW_UP_COMPLETE_TM,'DD-MON-YYYY')
    Query query = null;
    query = session.createSQLQuery(
        "select supplier_method_id,METHOD_TYPE_ID,VENDOR_ID from KDYER.OIM_SUPPLIER_METHODS where VENDOR_ID is not null and METHOD_NAME_ID=2");
    List<Object[]> result = query.list();
    Map<FtpDetail, String> ftpDetailMap = new HashMap<FtpDetail, String>();
    for (int j = 0; j < result.size(); j++) {
      Object[] obj = result.get(j);
      int supplierMethodId = ((BigDecimal) obj[0]).intValue();
      int methodTypeId = ((BigDecimal) obj[1]).intValue();
      int vendorId = ((BigDecimal) obj[2]).intValue();
      query = session.createSQLQuery(
          "select ATTRIBUTE_VALUE,ATTRIBUTE_ID from KDYER.OIM_SUPPLIER_METHODATTR_VALUES where SUPPLIER_METHOD_ID ="
              + supplierMethodId);
      List<Object[]> res = query.list();
      FtpDetail detail = new FtpDetail();
      if (methodTypeId == OimConstants.SUPPLIER_METHOD_TYPE_HG_PHI.intValue())
        detail.setWhareHouseType(WareHouseType.PHI);
      else if (methodTypeId == OimConstants.SUPPLIER_METHOD_TYPE_HG_HVA.intValue())
        detail.setWhareHouseType(WareHouseType.HVA);

      for (int i = 0; i < res.size(); i++) {
        Object[] values = res.get(i);
        String attrVal = (String) values[0];
        int attrId = ((BigDecimal) values[1]).intValue();
        if (attrId == 2)
          detail.setUrl(attrVal);
        else if (attrId == 3)
          detail.setUserName(attrVal);
        else if (attrId == 4)
          detail.setPassword(attrVal);
        else if (attrId == 9)
          detail.setAccountNumber(attrVal);
      }
      ftpDetailMap.put(detail, vendorId + "");
    }
    return ftpDetailMap;
  }

  private static void sendEmail(List<File> fileList) throws IOException {
    String emailBody = "Please find attached order status file.";
    String emailSubject = null;
    if (!StringHandle.removeNull(startDate).equals("")
        && !StringHandle.removeNull(endDate).equals("")) {
      emailSubject = "Evox Order status for " + startDate + " to " + endDate;
    } else
      emailSubject = "Evox Order status for last 2 days";
    final File f = new File("/home/staging/cm-orders/report/HG_Order_Status.zip");
    final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
    for (int i = 0; i < fileList.size(); i++) {

      File file = fileList.get(i);
      FileInputStream fis = new FileInputStream(file);
      ZipEntry zipEntry = new ZipEntry(file.getAbsolutePath());
      out.putNextEntry(zipEntry);

      byte[] bytes = new byte[1024];
      int length;
      while ((length = fis.read(bytes)) >= 0) {
        out.write(bytes, 0, length);
      }
      out.closeEntry();
      fis.close();
    }
    out.close();

    EmailUtil.sendEmailWithAttachment("orders@inventorysource.com",
        "support@inventorysource.com", "abheeshek@inventorysource.com, kelly@inventorysource.com, andrew@inventorysource.com",
        emailSubject, emailBody, f.getAbsolutePath());
    log.info("Email sent successfully");
  }

  private static void downloadFiles(FtpDetail ftpDetail, String vendorId, String confirmationPath,
      String shippingPath, String trackingPath, int confCount, int shipCount, int trackCount) {
    FTPClient ftp = new FTPClient();
    try {
      ftp.setRemoteHost(ftpDetail.getUrl());
      ftp.setDetectTransferMode(true);
      ftp.connect();
      ftp.login(ftpDetail.getUserName(), ftpDetail.getPassword());
      String fileDir = "/users/" + ftpDetail.getUserName() + "/";
      FTPFile[] ftpFiles = ftp.dirDetails(fileDir);
      for (int i = 0; i < ftpFiles.length; i++) {
        FTPFile ff = ftpFiles[i];
        if (ff.getName().startsWith(".") || ff.getName().startsWith(".."))
          continue;
        if (ff.isDir()) {
          String dirName = ff.getName() + "/";
          log.info("downloading files from {} directory", ff.getName());

          if (ff.getName().startsWith("confirmations")) {
            FTPFile[] confirmedFileList = ftp.dirDetails(fileDir + dirName);
            for (int j = 0; j < confirmedFileList.length; j++) {
              String fileNme = ((FTPFile) confirmedFileList[j]).getName();
              log.info("Downloading file : {}", fileNme);
              File tmp = new File(confirmationPath + fileNme);
              if (!tmp.exists()) {
                ftp.get(confirmationPath + fileNme, fileDir + dirName + fileNme);
                confCount++;
              }
            }
          }
          if (ff.getName().startsWith("shipping")) {
            FTPFile[] confirmedFileList = ftp.dirDetails(fileDir + dirName);
            for (int j = 0; j < confirmedFileList.length; j++) {
              String fileNme = ((FTPFile) confirmedFileList[j]).getName();
              log.info("Downloading file : {}", fileNme);
              File tmp = new File(shippingPath + fileNme);
              if (!tmp.exists()) {
                ftp.get(shippingPath + fileNme, fileDir + dirName + fileNme);
                shipCount++;
              }
            }
          }
          if (ff.getName().startsWith("tracking")) {
            FTPFile[] confirmedFileList = ftp.dirDetails(fileDir + dirName);
            for (int j = 0; j < confirmedFileList.length; j++) {
              String fileNme = ((FTPFile) confirmedFileList[j]).getName();
              log.info("Downloading file : {}", fileNme);
              File tmp = new File(trackingPath + fileNme);
              if (!tmp.exists()) {
                ftp.get(trackingPath + fileNme, fileDir + dirName + fileNme);
                trackCount++;
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

  private static List<PHIHVAData> getData(FtpDetail ftpDetail, String confirmationPath,
      String shippingPath, String trackingPath, boolean isPhi) throws IOException {
    Set<String> shippingNames = new TreeSet<String>();
    Set<String> trackingNames = new TreeSet<String>();
    List<PHIHVAData> dataList = new ArrayList<PHIHVAData>();
    Map<String, String> orderData = new HashMap<String, String>();
    File folder = new File(shippingPath);
    File[] files = folder.listFiles();
    for (File file : files) {
      shippingNames.add(file.getName());
    }
    folder = new File(trackingPath);
    files = folder.listFiles();
    for (File file : files) {
      trackingNames.add(file.getName());
    }
    folder = new File(confirmationPath);
    files = folder.listFiles();
    for (File file : files) {
      String confirmationFile = file.getName();
      if (confirmationFile.equals("..") || confirmationFile.equals("."))
        continue;
      Path path = Paths.get(confirmationPath + confirmationFile);
      byte[] confirmationFileData = Files.readAllBytes(path);
      Map<Integer, String> orderDataMap = parseFileData(confirmationFileData);
      parseOrderConfirmation(orderDataMap, orderData);
    }
    Session session = SessionManager.currentSession();// TO_CHAR(subQuery.FOLLOW_UP_COMPLETE_TM,'DD-MON-YYYY')
    Query query = null;
    String wareHouseCode = isPhi ? "P" : "H";
    if (!StringHandle.removeNull(startDate).equals("")
        && !StringHandle.removeNull(endDate).equals("")) {
      query = session.createSQLQuery(
          "select STORE_ORDER_ITEM_ID, DETAIL_ID, SUPPLIER_ORDER_NUMBER, os.STATUS_VALUE , to_char(INSERTION_TM, 'DD-MON-YYYY'), to_char(PROCESSING_TM, 'DD-MON-YYYY') from "
              + "OIM_ORDER_DETAILS od inner join OIM_ORDER_STATUSES os on os.STATUS_ID=od.STATUS_ID where ORDER_ID "
              + "in (select ORDER_ID from OIM_ORDERS where BATCH_ID in (select BATCH_ID from OIM_ORDER_BATCHES where "
              + "CHANNEL_ID (select channel_id from kdyer.oim_channel_supplier_map where supplier_id=1822 and channel_id in"
              + "(select channel_id from kdyer.oim_channels where delete_tm is null)) and CREATION_TM > TO_DATE('"
              + startDate + " 23:59:59','MM/DD/YYYY HH24:MI:SS') and " + "CREATION_TM < TO_DATE('"
              + endDate + " 23:59:59','MM/DD/YYYY HH24:MI:SS'))) and od.SUPPLIER_WAREHOUSE_CODE='"
              + wareHouseCode + "'");
    } else {
      query = session.createSQLQuery(
          "select STORE_ORDER_ITEM_ID, DETAIL_ID, SUPPLIER_ORDER_NUMBER, os.STATUS_VALUE , to_char(INSERTION_TM, 'DD-MON-YYYY'), to_char(PROCESSING_TM, 'DD-MON-YYYY')"
              + " from kdyer.OIM_ORDER_DETAILS od inner join kdyer.OIM_ORDER_STATUSES os on os.STATUS_ID=od.STATUS_ID where ORDER_ID "
              + "in (select ORDER_ID from kdyer.OIM_ORDERS where BATCH_ID in (select BATCH_ID from kdyer.OIM_ORDER_BATCHES where "
              + "CHANNEL_ID in (select channel_id from kdyer.oim_channel_supplier_map where supplier_id=1822 and channel_id in"
              + "(select channel_id from kdyer.oim_channels where delete_tm is null)) and CREATION_TM >=TRUNC( sysdate-2) and (CREATION_TM)<=TRUNC( sysdate))) "
              + "and od.SUPPLIER_WAREHOUSE_CODE='" + wareHouseCode + "'");
    }
    List<Object[]> result = query.list();
    for (int i = 0; i < result.size(); i++) {
      Object[] values = result.get(i);
      String STORE_ORDER_ITEM_ID = (String) values[0];
      String DETAIL_ID = ((BigDecimal) values[1]).toString();
      String SUPPLIER_ORDER_NUMBER = (String) values[2];
      String STATUS_VALUE = (String) values[3];
      String insertion_tm = (String) values[4];
      String processing_tm = (String) values[5];
      boolean isConfirmed = orderData.containsKey(SUPPLIER_ORDER_NUMBER) ? true : false;

      boolean isShipped = shippingNames.contains(
          ftpDetail.getAccountNumber() + ".S" + orderData.get(SUPPLIER_ORDER_NUMBER) + "A.txt")
              ? true : false;
      boolean isTracked = trackingNames.contains(
          ftpDetail.getAccountNumber() + ".T" + orderData.get(SUPPLIER_ORDER_NUMBER) + "S.txt")
              ? true : false;
      if (!isConfirmed && !isShipped && !isTracked)
        continue;
      PHIHVAData phihvaData = new PHIHVAData(STORE_ORDER_ITEM_ID, DETAIL_ID, SUPPLIER_ORDER_NUMBER,
          STATUS_VALUE, insertion_tm, processing_tm, isConfirmed, isShipped, isTracked,
          isPhi ? "PHI" : "HVA");
      dataList.add(phihvaData);
    }
    return dataList;
  }

  private static Map<Integer, String> parseFileData(byte[] confirmationFileData) {
    Map<Integer, String> fileData = null;
    try {
      fileData = new HashMap<Integer, String>();
      InputStream inputStream = new ByteArrayInputStream(confirmationFileData);
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

  private static void parseOrderConfirmation(Map<Integer, String> orderConfirmationMap,
      Map<String, String> orderData) {
    for (Iterator itr = orderConfirmationMap.values().iterator(); itr.hasNext();) {
      String line = (String) itr.next();
      String[] lineArray = line.split(",(?=(?>[^\"]*\"[^\"]*\")*[^\"]*$)");
      if (lineArray.length == 9) {
        orderData.put(lineArray[6], lineArray[0]);
      }
    }
    // return orderData;
  }

  private static Object deserialize(String filename) throws IOException, ClassNotFoundException {
    FileInputStream fileIn = new FileInputStream(filename);
    ObjectInputStream in = new ObjectInputStream(fileIn);
    Object obj = in.readObject();
    in.close();
    fileIn.close();
    return obj;
  }

  private static void serialize(String filename, Object obj) throws IOException {
    FileOutputStream fileOut = new FileOutputStream(filename);
    ObjectOutputStream out = new ObjectOutputStream(fileOut);
    out.writeObject(obj);
    out.close();
    fileOut.close();
  }

  private static void ftpMoveFile(FtpDetail ftpDetail) {
    Calendar cal = new GregorianCalendar();
    cal.add(Calendar.DAY_OF_MONTH, -60);
    Date moveFileDate = cal.getTime();
    int count = 0;
    int moveCount = 0;
    int skipCount = 0;

    String archiveDir = "IS_Archive/";
    log.info("Moving files from original location to IS_Archive...");
    FTPClient ftp = new FTPClient();
    String fileDir = "";
    try {
      ftp.setRemoteHost(ftpDetail.getUrl());
      fileDir = "/users/" + ftpDetail.getUserName() + "/";
      ftp.connect();
      ftp.login(ftpDetail.getUserName(), ftpDetail.getPassword());
      // File f = new File(fileDir + archiveDir);
      String archivePath = fileDir + archiveDir;
      try {
        ftp.mkdir(archiveDir);
      } catch (Exception e) {

      }
      String archive_shipPath = archiveDir + "shipping/";
      try {
        ftp.mkdir(archive_shipPath);
      } catch (Exception e) {

      }
      String archive_ConfPath = archiveDir + "confirmations/";
      try {
        ftp.mkdir(archive_ConfPath);
      } catch (Exception e) {

      }
      String archive_trackPath = archiveDir + "tracking/";
      try {
        ftp.mkdir(archive_trackPath);
      } catch (Exception e) {

      }

      FTPFile[] ftpFiles = ftp.dirDetails(fileDir);
      for (int i = 0; i < ftpFiles.length; i++) {
        FTPFile ff = ftpFiles[i];
        if (ff.getName().startsWith(".") || ff.getName().startsWith(".."))
          continue;
        if (ff.isDir()) {
          if (ff.getName().equals("confirmations") || ff.getName().equals("shipping")
              || ff.getName().equals("tracking")) {
            String dirName = ff.getName() + "/";
            log.info("Checking files in directory - {}", dirName);

            FTPFile[] test1FileList = ftp.dirDetails(fileDir + dirName);
            for (int j = 0; j < test1FileList.length; j++) {
              String fileNme = ((FTPFile) test1FileList[j]).getName();
              if (fileNme.startsWith(".") || fileNme.startsWith(".."))
                continue;
              FTPFile fileToMove = (FTPFile) test1FileList[j];
              Date lastModifiedDate = fileToMove.lastModified();
              if (lastModifiedDate.before(moveFileDate)) {
                log.info("{} : {} : move {}", count++, fileToMove.getName(), moveCount++);
                String fileOldLocation = fileDir + dirName + fileNme;
                String fileNewLocation = fileDir + archiveDir + dirName + fileNme;
                if (!moveFile(ftp, fileOldLocation, fileNewLocation))
                  log.warn("Failed to move file {}", fileOldLocation);
                if (moveCount > 5000)
                  return;
              } else
                log.info("{} : {} : skip {}", count++, fileToMove.getName(), skipCount++);
            }
          }
        }
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      try {
        ftp.quit();
      } catch (IOException | FTPException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }
  }

  private static boolean moveFile(FTPClient ftp, String fileOldLocation, String fileNewLocation) {
    boolean success = true;
    try {
      ftp.rename(fileOldLocation, fileNewLocation);
      // ftp.delete(fileOldLocation);
    } catch (IOException e) {
      e.printStackTrace();
      success = false;
    } catch (FTPException e) {
      e.printStackTrace();
      success = false;
    }
    return success;
  }

  private static String convertLongToDateString(long time) {
    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
    Date resultdate = new Date(time);
    return sdf.format(resultdate);
  }
}
