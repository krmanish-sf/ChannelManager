package salesmachine.oim.suppliers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.OimFileFieldMap;
import salesmachine.hibernatedb.OimFileformatParams;
import salesmachine.hibernatedb.OimFiletypes;
import salesmachine.hibernatedb.OimOrderDetails;
import salesmachine.hibernatedb.OimOrderStatuses;
import salesmachine.hibernatedb.OimSupplierMethods;
import salesmachine.hibernatedb.OimSuppliers;
import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.hibernatehelper.PojoHelper;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.oim.api.OimConstants;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPTransferType;

@Deprecated
public class OimSupplierOrderStatus {

	private static final Logger log = LoggerFactory
			.getLogger(OimSupplierOrderStatus.class);
	Session m_dbSession;
	OimSuppliers m_supplier;
	OimSupplierMethods m_supplierMethods;

	int m_MethodNameId;
	Hashtable m_Attributes;

	public boolean init(int supplierId, Session dbSession) {
		m_dbSession = dbSession;

		Transaction tx = m_dbSession.beginTransaction();
		Query query = m_dbSession
				.createQuery("from salesmachine.hibernatedb.OimSuppliers as s where s.supplierId=:id and s.deleteTm is null");
		query.setInteger("id", supplierId);
		tx.commit();
		if (!query.iterate().hasNext()) {
			System.out.println("No supplier found for supplier id: "
					+ supplierId);
			return false;
		}

		m_supplier = (OimSuppliers) query.iterate().next();
		return true;
	}




	private List readStatusFile(OimFiletypes oimFile, String localFileName,
			Hashtable fileFormatParams) {
		// Process the file now
		boolean useHeader = "1".equals((String) fileFormatParams
				.get(OimConstants.FILE_FORMAT_PARAMS_USEHEADER));
		String fieldDelimiter = (String) fileFormatParams
				.get(OimConstants.FILE_FORMAT_PARAMS_FIELD_DELIMITER);
		String textDelimiter = (String) fileFormatParams
				.get(OimConstants.FILE_FORMAT_PARAMS_TEXT_DELIMITER);
		if ("TAB".equals(fieldDelimiter))
			fieldDelimiter = "\t";

		Query query = m_dbSession
				.createQuery("from OimFileFieldMap m where m.oimFiletypes=:file and m.deleteTm is null");
		List fieldMap = query.setEntity("file", oimFile).list();

		File statusFile = new File(localFileName);
		List statusData = new ArrayList();
		if (statusFile.exists()) {
			try {
				System.out.println("Using field delimiter:" + fieldDelimiter);
				FileInputStream filestream = new FileInputStream(statusFile);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						filestream));
				String line = "";
				while ((line = in.readLine()) != null) {
					// Put space for empy fields
					while (line.indexOf(fieldDelimiter + fieldDelimiter) != -1)
						line = line.replaceAll(fieldDelimiter + fieldDelimiter,
								fieldDelimiter + " " + fieldDelimiter);

					StringTokenizer str = new StringTokenizer(line,
							fieldDelimiter);
					int fieldIndex = 0;
					Hashtable values = new Hashtable();
					while (str.hasMoreTokens()) {
						if (fieldIndex >= fieldMap.size())
							break;
						OimFileFieldMap map = (OimFileFieldMap) fieldMap
								.get(fieldIndex);
						Integer fieldId = map.getOimFields().getFieldId();
						String fieldName = map.getOimFields().getFieldName();

						String token = str.nextToken();
						if (token.indexOf(textDelimiter) == 0)
							token = token.substring(textDelimiter.length());
						if (token.lastIndexOf(textDelimiter) == token.length()
								- textDelimiter.length())
							token = token.substring(0, token.length()
									- textDelimiter.length());
						token.trim();

						values.put(fieldId, token);
						System.out.println("Id: " + fieldId + " Field: "
								+ fieldName + " Value: " + token);

						fieldIndex++;
					}

					if (fieldIndex >= fieldMap.size())
						statusData.add(values);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return statusData;
		} // if (statusFile.exists()) {
		return null;
	}

	//public static void main(String[] args) {}
}
