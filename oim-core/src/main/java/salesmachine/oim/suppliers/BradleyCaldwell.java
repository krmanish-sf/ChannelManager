package salesmachine.oim.suppliers;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import salesmachine.email.EmailUtil;
import salesmachine.hibernatedb.OimFields;
import salesmachine.hibernatedb.OimFileFieldMap;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrders;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatedb.Reps;
import salesmachine.hibernatedb.Vendors;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;
import salesmachine.oim.suppliers.modal.OrderDetailResponse;
import salesmachine.util.StringHandle;

import com.enterprisedt.net.ftp.FTPClient;

public class BradleyCaldwell extends Supplier {

	public void sendOrders(Integer vendorId, OimVendorSuppliers ovs, OimOrders order1) {
	  List<OimOrders> orders = new ArrayList<>();
	  orders.add(order1);
		logStream.println("!!Started sending orders to BradleyCaldwell");
		orderSkuPrefixMap = setSkuPrefixForOrders(ovs); // populate
														// orderSkuPrefixMap
														// with channel id and
														// the prefix to be used
														// for the given
														// supplier.

		Session session = SessionManager.currentSession();
		Query query = session
				.createQuery("select r from salesmachine.hibernatedb.Reps r where r.vendorId = "
						+ vendorId);
		Reps r = new Reps();
		Iterator repsIt = query.iterate();
		if (repsIt.hasNext()) {
			r = (Reps) repsIt.next();
		}
		Vendors v = new Vendors();
		v.setVendorId(r.getVendorId());

		String name = StringHandle.removeNull(r.getFirstName()) + " "
				+ StringHandle.removeNull(r.getLastName());
		String emailContent = "Dear " + name + "<br>";
		emailContent += "<br>Following is the status of the orders file uploaded on FTP for the supplier "
				+ ovs.getOimSuppliers().getSupplierName() + " : - <br>";
		boolean emailNotification = false;

		SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
		String accountNumber = ovs.getAccountNumber();
		String dtl_uploadfilename = "dtl_" + accountNumber + "_"
				+ sdf.format(new Date()) + ".txt";
		String hdr_uploadfilename = "hdr_" + accountNumber + "_"
				+ sdf.format(new Date()) + ".txt";

		Hashtable fileFormatParams = new Hashtable();
		fileFormatParams.put(OimConstants.FILE_FORMAT_PARAMS_USEHEADER, "0");
		fileFormatParams.put(OimConstants.FILE_FORMAT_PARAMS_FIELD_DELIMITER,
				"");
		fileFormatParams
				.put(OimConstants.FILE_FORMAT_PARAMS_TEXT_DELIMITER, "");
		try {
			logStream.println("Generating file " + dtl_uploadfilename);
			dtl_generateCsvFile(orders, dtl_getFileFieldMap(),
					dtl_uploadfilename, fileFormatParams,
					new StandardFileSpecificsProvider(session, ovs, v));

			logStream.println("Generating file " + hdr_uploadfilename);
			hdr_generateCsvFile(orders, hdr_getFileFieldMap(),
					hdr_uploadfilename, fileFormatParams,
					new StandardFileSpecificsProvider(session, ovs, v),
					accountNumber);

			FTPClient ftp = new FTPClient("ftp.bradleycaldwell.com");
			// if (activeModeFtp)
			// ftp.setConnectMode(FTPConnectMode.ACTIVE);
			ftp.login(ovs.getLogin(), ovs.getPassword());
			ftp.setTimeout(60 * 1000 * 60 * 5);

			ftp.chdir("/");
			ftp.put(dtl_uploadfilename, dtl_uploadfilename);
			ftp.put(hdr_uploadfilename, hdr_uploadfilename);
			ftp.quit();

			String emailBody = "Account Number : " + accountNumber
					+ "\n Find attached hdr file for the orders from my store.";
			String emailSubject = hdr_uploadfilename;
			EmailUtil.sendEmailWithAttachment(r.getLogin(),
					"support@inventorysource.com",
					"oim@inventorysource.com,aruppar@inventorysource.com",
					emailSubject, emailBody, hdr_uploadfilename, "");
			emailSubject = dtl_uploadfilename;
			EmailUtil.sendEmailWithAttachment(r.getLogin(),
					"support@inventorysource.com",
					"oim@inventorysource.com,aruppar@inventorysource.com",
					emailSubject, emailBody, dtl_uploadfilename, "");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// In both these cases i.e. Ftp File Upload and Email, orders can not
		// fail at this stage
		// So all of them need to be marked placed
		for (int i = 0; i < orders.size(); i++) {
			OimOrders order = (OimOrders) orders.get(i);
			for (Iterator detailIt = order.getOimOrderDetailses().iterator(); detailIt
					.hasNext();) {
				OimOrderDetails detail = (OimOrderDetails) detailIt.next();
				successfulOrders.put(detail.getDetailId(),
						new OrderDetailResponse()); // TODO get po and
																// add as value
																// here
			}

			// Send Email Notifications if is set to true.
			if (order.getOimOrderBatches().getOimChannels()
					.getEmailNotifications() == 1) {
				emailNotification = true;
				String orderStatus = "Successfully Placed";
				emailContent += "<b>Store Order ID " + order.getStoreOrderId()
						+ "</b> -> " + orderStatus + " ";
				emailContent += "<br>";
			}
		}
		if (emailNotification) {
			emailContent += "<br>Thanks, <br>Inventorysource support<br>";
			logStream
					.println("!! Sending email to user about order processing");
			EmailUtil.sendEmail(r.getLogin(), "support@inventorysource.com",
					"", "Order processing update results", emailContent,
					"text/html");
		}
	}

	private List dtl_getFileFieldMap() {
		List fileFieldMaps = new ArrayList();

		String fields[] = { "Order Number", "Line Number", "Item Number",
				"Description", "Quantity to Order", "SoldUOM", "STDUOM",
				"ALTUOM" };
		Integer mappedFieldIds[] = { 2, 0, 1, 24, 9, 0, 0, 0 };
		for (int i = 0; i < fields.length; i++) {
			OimFields field = new OimFields(fields[i], fields[i], new Date(),
					null, null);
			field.setFieldId(mappedFieldIds[i]);
			OimFileFieldMap ffm = new OimFileFieldMap(null, field, fields[i],
					new Date(), null, "", "");
			fileFieldMaps.add(ffm);
		}

		return fileFieldMaps;
	}

	private List hdr_getFileFieldMap() {
		List fileFieldMaps = new ArrayList();

		String fields[] = { "Order Number", "Customer Number", "Ship To",
				"Order Date ", "Purchase Order Text", "Ship By", "Name",
				"Add1", "Add2", "City", "State", "Zip", "Affil" };
		Integer mappedFieldIds[] = { 2, 0, 0, 34, 2, 0, 3, 4, 0, 5, 6, 7, 0 };
		for (int i = 0; i < fields.length; i++) {
			OimFields field = new OimFields(fields[i], fields[i], new Date(),
					null, null);
			field.setFieldId(mappedFieldIds[i]);
			OimFileFieldMap ffm = new OimFileFieldMap(null, field, fields[i],
					new Date(), null, "", "");
			fileFieldMaps.add(ffm);
		}

		return fileFieldMaps;
	}

	public void dtl_generateCsvFile(List orders, List fileFieldMaps,
			String fileName, Hashtable fileFormatParams,
			IFileSpecificsProvider fileSpecifics) throws Exception {
		FileWriter outputFile = new FileWriter(fileName);

		boolean useHeader = "1".equals((String) fileFormatParams
				.get(OimConstants.FILE_FORMAT_PARAMS_USEHEADER));
		String fieldDelimiter = (String) fileFormatParams
				.get(OimConstants.FILE_FORMAT_PARAMS_FIELD_DELIMITER);
		String textDelimiter = (String) fileFormatParams
				.get(OimConstants.FILE_FORMAT_PARAMS_TEXT_DELIMITER);
		if ("TAB".equals(fieldDelimiter)) {
			fieldDelimiter = "\t";
		}

		// Get the headers now
		if (useHeader) {
			// Write the header first
			String headerline = "";
			int i = 0;
			for (Iterator it = fileFieldMaps.iterator(); it.hasNext();) {
				OimFileFieldMap map = (OimFileFieldMap) it.next();
				if (i > 0)
					headerline += fieldDelimiter;
				headerline += textDelimiter
						+ StringHandle.removeNull(map.getMappedFieldName())
						+ textDelimiter;
				i++;
			}
			headerline += "\n";
			outputFile.write(headerline);
		}

		// Write the data now
		for (int i = 0; i < orders.size(); i++) {
			int lineCount = 0;
			OimOrders order = (OimOrders) orders.get(i);
			for (Iterator detailIt = order.getOimOrderDetailses().iterator(); detailIt
					.hasNext();) {
				OimOrderDetails detail = (OimOrderDetails) detailIt.next();
				// for all the order details
				String dataline = "";
				int j = 0;
				lineCount++;
				for (Iterator it = fileFieldMaps.iterator(); it.hasNext();) {
					OimFileFieldMap map = (OimFileFieldMap) it.next();
					OimFields field = map.getOimFields();
					String writeModifier = StringHandle.removeNull(map
							.getMappedFieldModifierRuleWr());
					String fieldValue = StringHandle.removeNull(fileSpecifics
							.getFieldValueFromOrder(detail, map));
					// logStream.println("Field Id: "+field.getFieldId()+"\t ("+field.getFieldName()+")"+"\t:"+fieldValue);
					if (j == 0) {
						int defaultLength = 6;
						int fieldValueLength = fieldValue.length();
						if (defaultLength > fieldValueLength) {
							int appendDefault = defaultLength
									- fieldValueLength;
							for (int ad = 0; ad < appendDefault; ad++) {
								fieldValue = "0" + fieldValue;
							}
						} else if (defaultLength < fieldValueLength) {
							fieldValue = fieldValue.substring(0, defaultLength);
						}
					}
					if (j == 1) {
						int defaultLength = 3;
						fieldValue = lineCount + "";
						int fieldValueLength = fieldValue.length();
						if (defaultLength > fieldValueLength) {
							int appendDefault = defaultLength
									- fieldValueLength;
							for (int ad = 0; ad < appendDefault; ad++) {
								fieldValue = "0" + fieldValue;
							}
						} else if (defaultLength < fieldValueLength) {
							fieldValue = fieldValue.substring(0, defaultLength);
						}
					}
					if (j == 2) {
						int defaultLength = 13;
						int fieldValueLength = fieldValue.length();
						if (defaultLength > fieldValueLength) {
							int appendDefault = defaultLength
									- fieldValueLength;
							for (int ad = 0; ad < appendDefault; ad++) {
								fieldValue = fieldValue + " ";
							}
						} else if (defaultLength < fieldValueLength) {
							fieldValue = fieldValue.substring(0, defaultLength);
						}
					}
					if (j == 3) {
						int defaultLength = 31;
						int fieldValueLength = fieldValue.length();
						if (defaultLength > fieldValueLength) {
							int appendDefault = defaultLength
									- fieldValueLength;
							for (int ad = 0; ad < appendDefault; ad++) {
								fieldValue = fieldValue + " ";
							}
						} else if (defaultLength < fieldValueLength) {
							fieldValue = fieldValue.substring(0, defaultLength);
						}
					}
					if (j == 4) {
						int defaultLength = 5;
						int fieldValueLength = fieldValue.length();
						if (defaultLength > fieldValueLength) {
							int appendDefault = defaultLength
									- fieldValueLength;
							for (int ad = 0; ad < appendDefault; ad++) {
								fieldValue = "0" + fieldValue;
							}
						} else if (defaultLength < fieldValueLength) {
							fieldValue = fieldValue.substring(0, defaultLength);
						}
					}
					if (j == 5) {
						int defaultLength = 3;
						int fieldValueLength = fieldValue.length();
						if (defaultLength > fieldValueLength) {
							int appendDefault = defaultLength
									- fieldValueLength;
							for (int ad = 0; ad < appendDefault; ad++) {
								fieldValue = " " + fieldValue;
							}
						} else if (defaultLength < fieldValueLength) {
							fieldValue = fieldValue.substring(0, defaultLength);
						}
					}
					if (j == 6) {
						int defaultLength = 3;
						int fieldValueLength = fieldValue.length();
						if (defaultLength > fieldValueLength) {
							int appendDefault = defaultLength
									- fieldValueLength;
							for (int ad = 0; ad < appendDefault; ad++) {
								fieldValue = " " + fieldValue;
							}
						} else if (defaultLength < fieldValueLength) {
							fieldValue = fieldValue.substring(0, defaultLength);
						}
					}
					if (j == 7) {
						int defaultLength = 3;
						int fieldValueLength = fieldValue.length();
						if (defaultLength > fieldValueLength) {
							int appendDefault = defaultLength
									- fieldValueLength;
							for (int ad = 0; ad < appendDefault; ad++) {
								fieldValue = " " + fieldValue;
							}
						} else if (defaultLength < fieldValueLength) {
							fieldValue = fieldValue.substring(0, defaultLength);
						}
					}
					if (j > 0)
						dataline += fieldDelimiter;
					dataline += textDelimiter + fieldValue + textDelimiter;
					j++;
				}
				dataline += "\n";
				outputFile.write(dataline);
			}
		} // for (int i=0;i<orders.numRows();i++) {

		if (fileSpecifics.getLastFileLine() != null) {
			outputFile.write(fileSpecifics.getLastFileLine());
		}
		outputFile.close();
	}

	public void hdr_generateCsvFile(List orders, List fileFieldMaps,
			String fileName, Hashtable fileFormatParams,
			IFileSpecificsProvider fileSpecifics, String accountNumber)
			throws Exception {
		FileWriter outputFile = new FileWriter(fileName);

		boolean useHeader = "1".equals((String) fileFormatParams
				.get(OimConstants.FILE_FORMAT_PARAMS_USEHEADER));
		String fieldDelimiter = (String) fileFormatParams
				.get(OimConstants.FILE_FORMAT_PARAMS_FIELD_DELIMITER);
		String textDelimiter = (String) fileFormatParams
				.get(OimConstants.FILE_FORMAT_PARAMS_TEXT_DELIMITER);
		if ("TAB".equals(fieldDelimiter)) {
			fieldDelimiter = "\t";
		}

		// Get the headers now
		if (useHeader) {
			// Write the header first
			String headerline = "";
			int i = 0;
			for (Iterator it = fileFieldMaps.iterator(); it.hasNext();) {
				OimFileFieldMap map = (OimFileFieldMap) it.next();
				if (i > 0)
					headerline += fieldDelimiter;
				headerline += textDelimiter
						+ StringHandle.removeNull(map.getMappedFieldName())
						+ textDelimiter;
				i++;
			}
			headerline += "\n";
			outputFile.write(headerline);
		}

		// Write the data now
		for (int i = 0; i < orders.size(); i++) {
			OimOrders order = (OimOrders) orders.get(i);
			for (Iterator detailIt = order.getOimOrderDetailses().iterator(); detailIt
					.hasNext();) {
				OimOrderDetails detail = (OimOrderDetails) detailIt.next();
				// for all the order details
				String dataline = "";
				int j = 0;

				for (Iterator it = fileFieldMaps.iterator(); it.hasNext();) {
					OimFileFieldMap map = (OimFileFieldMap) it.next();
					OimFields field = map.getOimFields();
					String writeModifier = StringHandle.removeNull(map
							.getMappedFieldModifierRuleWr());
					String fieldValue = StringHandle.removeNull(fileSpecifics
							.getFieldValueFromOrder(detail, map));
					// logStream.println("Field Id: "+field.getFieldId()+"\t ("+field.getFieldName()+")"+"\t:"+fieldValue);
					if (j == 0) {
						int defaultLength = 6;
						int fieldValueLength = fieldValue.length();
						if (defaultLength > fieldValueLength) {
							int appendDefault = defaultLength
									- fieldValueLength;
							for (int ad = 0; ad < appendDefault; ad++) {
								fieldValue = "0" + fieldValue;
							}
						} else if (defaultLength < fieldValueLength) {
							fieldValue = fieldValue.substring(0, defaultLength);
						}
					}
					if (j == 1) {
						int defaultLength = 8;
						fieldValue = accountNumber;
						int fieldValueLength = fieldValue.length();
						if (defaultLength > fieldValueLength) {
							int appendDefault = defaultLength
									- fieldValueLength;
							for (int ad = 0; ad < appendDefault; ad++) {
								fieldValue = "0" + fieldValue;
							}
						} else if (defaultLength < fieldValueLength) {
							fieldValue = fieldValue.substring(0, defaultLength);
						}
					}
					if (j == 2) {
						int defaultLength = 2;
						int fieldValueLength = fieldValue.length();
						if (defaultLength > fieldValueLength) {
							int appendDefault = defaultLength
									- fieldValueLength;
							for (int ad = 0; ad < appendDefault; ad++) {
								fieldValue = fieldValue + "0";
							}
						} else if (defaultLength < fieldValueLength) {
							fieldValue = fieldValue.substring(0, defaultLength);
						}
					}
					if (j == 3) {
						int defaultLength = 8;
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
						fieldValue = sdf.format(new Date());
						int fieldValueLength = fieldValue.length();
						if (defaultLength > fieldValueLength) {
							int appendDefault = defaultLength
									- fieldValueLength;
							for (int ad = 0; ad < appendDefault; ad++) {
								fieldValue = fieldValue + " ";
							}
						} else if (defaultLength < fieldValueLength) {
							fieldValue = fieldValue.substring(0, defaultLength);
						}
					}
					if (j == 4) {
						int defaultLength = 16;
						int fieldValueLength = fieldValue.length();
						if (defaultLength > fieldValueLength) {
							int appendDefault = defaultLength
									- fieldValueLength;
							for (int ad = 0; ad < appendDefault; ad++) {
								fieldValue = fieldValue + " ";
							}
						} else if (defaultLength < fieldValueLength) {
							fieldValue = fieldValue.substring(0, defaultLength);
						}
					}
					if (j == 5) {
						int defaultLength = 30;
						fieldValue = "UPS";
						int fieldValueLength = fieldValue.length();
						if (defaultLength > fieldValueLength) {
							int appendDefault = defaultLength
									- fieldValueLength;
							for (int ad = 0; ad < appendDefault; ad++) {
								fieldValue = fieldValue + " ";
							}
						} else if (defaultLength < fieldValueLength) {
							fieldValue = fieldValue.substring(0, defaultLength);
						}
					}
					if (j == 6) {
						int defaultLength = 25;
						int fieldValueLength = fieldValue.length();
						if (defaultLength > fieldValueLength) {
							int appendDefault = defaultLength
									- fieldValueLength;
							for (int ad = 0; ad < appendDefault; ad++) {
								fieldValue = fieldValue + " ";
							}
						} else if (defaultLength < fieldValueLength) {
							fieldValue = fieldValue.substring(0, defaultLength);
						}
					}
					if (j == 7) {
						int defaultLength = 25;
						int fieldValueLength = fieldValue.length();
						if (defaultLength > fieldValueLength) {
							int appendDefault = defaultLength
									- fieldValueLength;
							for (int ad = 0; ad < appendDefault; ad++) {
								fieldValue = fieldValue + " ";
							}
						} else if (defaultLength < fieldValueLength) {
							fieldValue = fieldValue.substring(0, defaultLength);
						}
					}
					if (j == 8) {
						int defaultLength = 25;
						int fieldValueLength = fieldValue.length();
						if (defaultLength > fieldValueLength) {
							int appendDefault = defaultLength
									- fieldValueLength;
							for (int ad = 0; ad < appendDefault; ad++) {
								fieldValue = fieldValue + " ";
							}
						} else if (defaultLength < fieldValueLength) {
							fieldValue = fieldValue.substring(0, defaultLength);
						}
					}
					if (j == 9) {
						int defaultLength = 23;
						int fieldValueLength = fieldValue.length();
						if (defaultLength > fieldValueLength) {
							int appendDefault = defaultLength
									- fieldValueLength;
							for (int ad = 0; ad < appendDefault; ad++) {
								fieldValue = fieldValue + " ";
							}
						} else if (defaultLength < fieldValueLength) {
							fieldValue = fieldValue.substring(0, defaultLength);
						}
					}
					if (j == 10) {
						int defaultLength = 2;
						int fieldValueLength = fieldValue.length();
						if (defaultLength > fieldValueLength) {
							int appendDefault = defaultLength
									- fieldValueLength;
							for (int ad = 0; ad < appendDefault; ad++) {
								fieldValue = fieldValue;
							}
						} else if (defaultLength < fieldValueLength) {
							fieldValue = fieldValue.substring(0, defaultLength);
						}
					}
					if (j == 11) {
						int defaultLength = 10;
						int fieldValueLength = fieldValue.length();
						if (defaultLength > fieldValueLength) {
							int appendDefault = defaultLength
									- fieldValueLength;
							for (int ad = 0; ad < appendDefault; ad++) {
								fieldValue = fieldValue + " ";
							}
						} else if (defaultLength < fieldValueLength) {
							fieldValue = fieldValue.substring(0, defaultLength);
						}
					}
					if (j == 12) {
						int defaultLength = 20;
						int fieldValueLength = fieldValue.length();
						if (defaultLength > fieldValueLength) {
							int appendDefault = defaultLength
									- fieldValueLength;
							for (int ad = 0; ad < appendDefault; ad++) {
								fieldValue = " " + fieldValue;
							}
						} else if (defaultLength < fieldValueLength) {
							fieldValue = fieldValue.substring(0, defaultLength);
						}
					}
					if (j > 0)
						dataline += fieldDelimiter;
					dataline += textDelimiter + fieldValue + textDelimiter;
					j++;
				}
				dataline += "\n";
				outputFile.write(dataline);
				break;
			}
		} // for (int i=0;i<orders.numRows();i++) {

		if (fileSpecifics.getLastFileLine() != null) {
			outputFile.write(fileSpecifics.getLastFileLine());
		}
		outputFile.close();
	}
}
