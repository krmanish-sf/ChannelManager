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
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.util.FtpDetail;
import salesmachine.util.FtpDetail.WareHouseType;
import salesmachine.util.StringHandle;

public class OrderTest {
  private static final Logger log = LoggerFactory.getLogger(OrderTest.class);

  private static final String ASCII = "ASCII";

  private static String startDate = "";// 08/18/2015
  private static String endDate = "";// MM/DD/YYYY

  public static void main(String[] args) {
    getUnconfirmedOrders();
  }
  
  
  public static void main1(String[] args) throws IOException, FTPException, ParseException {
    long processStartTime = System.currentTimeMillis();
    Map<FtpDetail, String> ftpDetailMap;
    if (args.length == 2) {
      startDate = args[0];
      endDate = args[1];
    }
    log.info("Strated getting all ftp details...");
    ftpDetailMap = getFtpDetails();
    log.info("Total {} number of ftp details found", ftpDetailMap.size());

    List<File> fileList = new ArrayList<File>();
    Map<String, List<PHIHVAData>> vendorDataMap = new HashMap<String, List<PHIHVAData>>();
    for (Iterator<FtpDetail> itr = ftpDetailMap.keySet().iterator(); itr.hasNext();) {
      FtpDetail ftpDetail = itr.next();
      String vendorId = ftpDetailMap.get(ftpDetail);
      if (ftpDetail.getSupplierId() != 1822)
        continue;
      long fileCleanupStartTime = System.currentTimeMillis();
      String fileCleanupStartTimeStr = convertLongToDateString(fileCleanupStartTime);
      log.info("ftp file move process started at {} for {}", fileCleanupStartTimeStr, ftpDetail);
      try {
        ftpMoveFile(ftpDetail, vendorId);
      } catch (Exception e) {
        log.error("Error occure while moving the files at ftp {}", ftpDetail);
      }
      long fileCleanupEndTime = System.currentTimeMillis();
      String fileCleanupEndTimStr = convertLongToDateString(fileCleanupEndTime);
      log.info("ftp file move process ended at {} for {}", fileCleanupEndTimStr, ftpDetail);
      // "/home/staging/cm-orders/report/"
      // test /home/manish-kumar/staging/cm-orders/report
      vendorDataMap = getVendorDataMap(vendorId, ftpDetail);
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
    // get last 48 hrs to 24 hrs orders that are not placed to HG confirmation.
    long unconfirmedOrderStartTime = System.currentTimeMillis();
    String unconfirmedOrderStartTimeStr = convertLongToDateString(unconfirmedOrderStartTime);
    log.info("Started getting all unconfirmed orders at {}", unconfirmedOrderStartTimeStr);
    getUnconfirmedOrders();
    long unconfirmedOrderEndTime = System.currentTimeMillis();
    String unconfirmedOrderEndTimeStr = convertLongToDateString(unconfirmedOrderEndTime);
    log.info("All unconfirmed orders were emailed at - {}", unconfirmedOrderEndTimeStr);
    //
    long total_time = unconfirmedOrderEndTime - processStartTime;
    int minutes = (int) ((total_time / (1000 * 60)) % 60);
    log.info("process completed in {} minutes ", minutes);
    log.info("Process complete...");
  }

  private static void getUnconfirmedOrders() {
    Session session = SessionManager.currentSession();
    Query query = null;
    StringBuffer sb = new StringBuffer();
    Map<Integer, ArrayList<String>> vendorOrderMap = new HashMap<Integer, ArrayList<String>>();
    query = session.createSQLQuery(
        "select o.store_order_id, c.vendor_id from kdyer.oim_orders o inner join kdyer.oim_order_batches b on o.batch_id=b.batch_id"
        + " inner join kdyer.oim_channels c on c.channel_id=b.channel_id where o.order_id in(select d.order_id from kdyer.oim_order_details d "
        + "where d.SUPPLIER_WAREHOUSE_CODE is not null and d.STATUS_ID=2 and d.SUPPLIER_ORDER_STATUS like '%Sent to supplier%' and d.PROCESSING_TM > trunc(sysdate-2) "
        + "and PROCESSING_TM<trunc(sysdate-1))");
    List<Object[]> result = query.list();
    for (int j = 0; j < result.size(); j++) {
      Object[] obj = result.get(j);
      String storeOrderNo = (String)obj[0];
      int vendorId = ((BigDecimal) obj[1]).intValue();
      ArrayList<String> orderList = vendorOrderMap.get(vendorId);
      if(orderList!=null)
        orderList.add(storeOrderNo);
      else{
        ArrayList<String> orderListNew = new ArrayList<String>();
        orderListNew.add(storeOrderNo);
        vendorOrderMap.put(vendorId, orderListNew);
      }
    }
    if(vendorOrderMap.size()>0)
      sb.append("These orders are still not found at confirmation directory for vendor(s) - : \n");
    for(Iterator<Integer> itr = vendorOrderMap.keySet().iterator();itr.hasNext();){
      int vendorId = itr.next();
      sb.append("\n");
      sb.append("Vendor - "+vendorId+" : \n");
      ArrayList<String> orderList =  vendorOrderMap.get(vendorId);
      for(int i=0;i<orderList.size();i++){
        String storeOrderNo = orderList.get(i);
        sb.append(storeOrderNo+"\n");
      }
      sb.append("------------------------------ \n");
    }
    if(vendorOrderMap.size()>0)
    EmailUtil.sendEmail("support@inventorysource.com", "orders@inventorysource.com",
        "manish@inventorysource.com", "Orders not confirmed at HG", sb.toString());
  }

  private static Map<String, List<PHIHVAData>> getVendorDataMap(String vendorId,
      FtpDetail ftpDetail) throws IOException {
    Map<String, List<PHIHVAData>> vendorDataMap = new HashMap<String, List<PHIHVAData>>();
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
      try {
        downloadFiles(ftpDetail, vendorId, confirmationPath, shippingPath, trackingPath);
      } catch (Exception e) {
        log.error("Error occured while downloading files for ftp {} {}", ftpDetail, e);
        if (e instanceof SocketException) {
          // downloadFiles(ftpDetail, vendorId, confirmationPath, shippingPath, trackingPath);
        }
      }
    } catch (Exception e) {
      log.error("error occure while downloading files from ftp ", e);
    }
    long fileDownloadEndTime = System.currentTimeMillis();
    String fileDownloadStartEndStr = convertLongToDateString(fileDownloadEndTime);
    log.info("Files download process completed at {} for ftp : {}", fileDownloadStartEndStr,
        ftpDetail);
    List<PHIHVAData> dataList = getData(ftpDetail, confirmationPath, shippingPath, trackingPath,
        isPHI);
    log.info("dataList size -- {}", dataList.size());
    if (vendorDataMap.get(vendorId) != null) {
      List<PHIHVAData> vendorDataList = vendorDataMap.get(vendorId);
      vendorDataList.addAll(dataList);
    } else {
      vendorDataMap.put(vendorId, dataList);
    }
    return vendorDataMap;

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
        "select supplier_method_id,METHOD_TYPE_ID,VENDOR_ID,supplier_id from KDYER.OIM_SUPPLIER_METHODS where VENDOR_ID is not null and METHOD_NAME_ID=2");
    List<Object[]> result = query.list();
    Map<FtpDetail, String> ftpDetailMap = new HashMap<FtpDetail, String>();
    for (int j = 0; j < result.size(); j++) {
      Object[] obj = result.get(j);
      int supplierMethodId = ((BigDecimal) obj[0]).intValue();
      int methodTypeId = ((BigDecimal) obj[1]).intValue();
      int vendorId = ((BigDecimal) obj[2]).intValue();
      int supplierId = ((BigDecimal) obj[3]).intValue();
      query = session.createSQLQuery(
          "select ATTRIBUTE_VALUE,ATTRIBUTE_ID from KDYER.OIM_SUPPLIER_METHODATTR_VALUES where SUPPLIER_METHOD_ID ="
              + supplierMethodId);
      List<Object[]> res = query.list();
      FtpDetail detail = new FtpDetail();
      detail.setSupplierId(supplierId);
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
      emailSubject = "Honest Green Order status for " + startDate + " to " + endDate;
    } else
      emailSubject = "Honest Green order status for last 2 days";
    if (fileList.size() > 0) {
      // final File f = new File("/home/staging/cm-orders/report/HG_Order_Status.zip");
      final File f = new File("/home/staging/cm-orders/report/HG_Order_Status.zip");
      final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
      for (int i = 0; i < fileList.size(); i++) {

        File file = fileList.get(i);
        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(file.getName());
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

      EmailUtil.sendEmailWithAttachment("orders@inventorysource.com", "support@inventorysource.com",
          "manish@inventorysource.com, kelly@inventorysource.com",
          emailSubject, emailBody, f.getAbsolutePath());

      // EmailUtil.sendEmailWithAttachment("manish@inventorysource.com",
      // "manish@inventorysource.com",
      // "", emailSubject, emailBody, f.getAbsolutePath());
      log.info("Email with attachment sent successfully.");
    } else {
      emailBody = "There is no order found for last two days.";
      EmailUtil.sendEmail("orders@inventorysource.com", "support@inventorysource.com",
          "manish@inventorysource.com, kelly@inventorysource.com",
          emailSubject, emailBody);

      // EmailUtil.sendEmail("manish@inventorysource.com", "manish@inventorysource.com", "",
      // emailSubject, emailBody);
      log.info("Email without attachment sent successfully.");
    }
  }

  private static void downloadFiles(FtpDetail ftpDetail, String vendorId, String confirmationPath,
      String shippingPath, String trackingPath) throws IOException, FTPException, SocketException {
    int confCount = 0;
    int shipCount = 0;
    int trackCount = 0;
    FTPClient ftp = new FTPClient();
    ftp.setRemoteHost(ftpDetail.getUrl());
    ftp.setDetectTransferMode(true);
    ftp.connect();
    ftp.login(ftpDetail.getUserName(), ftpDetail.getPassword());
    String fileDir = "/users/" + ftpDetail.getUserName() + "/";
    FTPFile[] ftpFiles;
    try {
      ftpFiles = ftp.dirDetails(fileDir);
    } catch (ParseException e) {
      log.error("Error occure while getting ftp files..");
      throw new FTPException("Unable to get ftpFiles.");
    }
    for (int i = 0; i < ftpFiles.length; i++) {
      FTPFile ff = ftpFiles[i];
      if (ff.getName().startsWith(".") || ff.getName().startsWith(".."))
        continue;
      if (ff.isDir()) {
        String dirName = ff.getName() + "/";
        log.info("downloading files from {} directory", ff.getName());

        if (ff.getName().startsWith("confirmations")) {
          try {
            FTPFile[] confirmedFileList = ftp.dirDetails(fileDir + dirName);
            for (int j = 0; j < confirmedFileList.length; j++) {
              String fileNme = ((FTPFile) confirmedFileList[j]).getName();
              if (fileNme.startsWith(".") || fileNme.startsWith(".."))
                continue;

              File tmp = new File(confirmationPath + fileNme);
              if (!tmp.exists()) {
                log.info("Downloading file : {}", fileNme);
                ftp.get(confirmationPath + fileNme, fileDir + dirName + fileNme);
                confCount++;
              }
            }
          } catch (ParseException e) {
            log.error("Error while listing confirmation directory.", e);
          }
        }
        if (ff.getName().startsWith("shipping")) {
          try {
            FTPFile[] confirmedFileList = ftp.dirDetails(fileDir + dirName);
            for (int j = 0; j < confirmedFileList.length; j++) {
              String fileNme = ((FTPFile) confirmedFileList[j]).getName();
              if (fileNme.startsWith(".") || fileNme.startsWith(".."))
                continue;
              File tmp = new File(shippingPath + fileNme);
              if (!tmp.exists()) {
                log.info("Downloading file : {}", fileNme);
                ftp.get(shippingPath + fileNme, fileDir + dirName + fileNme);
                shipCount++;
              }
            }
          } catch (ParseException e) {
            log.error("Error while listing shipping directory.", e);
          }
        }
        if (ff.getName().startsWith("tracking")) {
          try {
            FTPFile[] confirmedFileList = ftp.dirDetails(fileDir + dirName);
            for (int j = 0; j < confirmedFileList.length; j++) {
              String fileNme = ((FTPFile) confirmedFileList[j]).getName();
              if (fileNme.startsWith(".") || fileNme.startsWith(".."))
                continue;

              File tmp = new File(trackingPath + fileNme);
              if (!tmp.exists()) {
                log.info("Downloading file : {}", fileNme);
                ftp.get(trackingPath + fileNme, fileDir + dirName + fileNme);
                trackCount++;
              }
            }
          } catch (ParseException e) {
            log.error("Error while listing tracking directory.", e);
          }
        }
      }
    }
    log.info("Total number of files downloaded from confirmation Dir - {}", confCount++);
    log.info("Total number of files downloaded from shipping Dir - {}", shipCount++);
    log.info("Total number of files downloaded from tracking Dir - {}", trackCount++);
  }

  private static List<PHIHVAData> getData(FtpDetail ftpDetail, String confirmationPath,
      String shippingPath, String trackingPath, boolean isPhi) throws IOException {
    Set<String> shippingNames = new TreeSet<String>();
    Set<String> trackingNames = new TreeSet<String>();
    List<PHIHVAData> dataList = new ArrayList<PHIHVAData>();
    Map<String, String> orderData = new HashMap<String, String>();
    File folder = new File(shippingPath);
    File[] files = folder.listFiles();
    if (files != null) {
      for (File file : files) {
        shippingNames.add(file.getName());
      }
    }
    folder = new File(trackingPath);
    files = folder.listFiles();
    if (files != null) {
      for (File file : files) {
        trackingNames.add(file.getName());
      }
    }
    folder = new File(confirmationPath);
    files = folder.listFiles();
    if (files != null) {
      for (File file : files) {
        String confirmationFile = file.getName();
        if (confirmationFile.equals("..") || confirmationFile.equals("."))
          continue;
        Path path = Paths.get(confirmationPath + confirmationFile);
        byte[] confirmationFileData = Files.readAllBytes(path);
        Map<Integer, String> orderDataMap = parseFileData(confirmationFileData);
        parseOrderConfirmation(orderDataMap, orderData);
      }
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
              + "CHANNEL_ID in (select channel_id from kdyer.oim_channel_supplier_map where supplier_id=1822 and channel_id in"
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

  private static void ftpMoveFile(FtpDetail ftpDetail, String vendorId) {
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
      // String archivePath = fileDir + archiveDir;

      FTPFile[] ftpFiles = ftp.dirDetails(fileDir);
      // checking the existence of confirmations, shipping and tracking directories
      boolean isConfirmationDirExists = false;
      boolean isShippingDirExists = false;
      boolean isTrackingDirExists = false;
      for (int i = 0; i < ftpFiles.length; i++) {
        FTPFile ff = ftpFiles[i];
        if (ff.getName().startsWith(".") || ff.getName().startsWith(".."))
          continue;
        if (ff.isDir()) {
          String dirName = ff.getName() + "/";
          log.info("Checking files in directory - {}", dirName);
          if (dirName.equals("confirmations/")) {
            isConfirmationDirExists = true;
            continue;
          } else if (dirName.equals("shipping/")) {
            isShippingDirExists = true;
            continue;
          } else if (dirName.equals("tracking/")) {
            isTrackingDirExists = true;
            continue;
          }
        }
      }
      if (isConfirmationDirExists && isShippingDirExists && isTrackingDirExists) {
        try {
          ftp.mkdir(archiveDir);
        } catch (Exception e) {

        }
        ftp.chdir(archiveDir);
        String[] archiveDirArr = ftp.dir();
        for (int i = 0; i < archiveDirArr.length; i++) {
          String fileName = archiveDirArr[i];
          if (fileName.equals("shipping"))
            ftp.rename("shipping", "IS_shipping");
          if (fileName.equals("confirmations"))
            ftp.rename("confirmations", "IS_confirmations");
          if (fileName.equals("tracking"))
            ftp.rename("tracking", "IS_tracking");
        }
        ftp.cdup();
        String archive_shipPath = archiveDir + "IS_shipping/";
        try {
          ftp.mkdir(archive_shipPath);
        } catch (Exception e) {

        }
        String archive_ConfPath = archiveDir + "IS_confirmations/";
        try {
          ftp.mkdir(archive_ConfPath);
        } catch (Exception e) {

        }
        String archive_trackPath = archiveDir + "IS_tracking/";
        try {
          ftp.mkdir(archive_trackPath);
        } catch (Exception e) {

        }
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
                  String fileNewLocation = fileDir + archiveDir + "IS_" + dirName + fileNme;
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
      } else {
        // generate email alert
        StringBuffer sb = new StringBuffer();
        sb.append(
            "Default order directories were not found on the HG FTP drive and it needs to be corrected by HG for VendorID - "
                + vendorId + "\n");
        sb.append("Missing directories are : \n");
        if (!isConfirmationDirExists)
          sb.append("confirmations \n");
        if (!isShippingDirExists)
          sb.append("shipping \n");
        if (!isTrackingDirExists)
          sb.append("tracking \n");
        sb.append("FTP login details :- \n");
        sb.append("FTP : " + ftpDetail.getUrl() + "\n");
        sb.append("USERNAME : " + ftpDetail.getUserName() + "\n");
        sb.append("PASSWORD : " + ftpDetail.getPassword() + "\n");
        String subject = vendorId + " : Honest Green Default Ftp directories are missing";
        EmailUtil.sendEmail("support@inventorysource.com", "orders@inventorysource.com",
            "manish@inventorysource.com", subject, sb.toString());

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
