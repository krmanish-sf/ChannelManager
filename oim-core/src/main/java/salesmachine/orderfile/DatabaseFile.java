package salesmachine.orderfile;

import java.util.Hashtable;
import java.util.Iterator;

import org.hibernate.Query;
import org.hibernate.Session;

import salesmachine.hibernatedb.OimFileformatParams;
import salesmachine.hibernatedb.OimFiletypes;

public class DatabaseFile extends OrderFile {	
	OimFiletypes oimFile;
	public DatabaseFile(Session session,OimFiletypes of) {
		super(session);
		oimFile = of;
	}
	
	public void build() {
		// Build the file format params
		Query query = m_dbSession.createQuery("from OimFileformatParams where oimFiletypes=:ft and deleteTm is null");			
		Iterator it = query.setEntity("ft", oimFile).iterate();
		fileFormatParams = new Hashtable();			
		while (it.hasNext()) {
			OimFileformatParams param = (OimFileformatParams)it.next();
			String value = param.getParamValue();
			if (value == null)
				value = "";
			fileFormatParams.put(param.getParamName(), value);
		}
											
		query = m_dbSession.createQuery("from OimFileFieldMap m where m.oimFiletypes=:file and m.deleteTm is null");
		fileFieldMaps = query.setEntity("file", oimFile).list();														
	}
}
