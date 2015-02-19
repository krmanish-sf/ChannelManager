package salesmachine.orderfile;

import java.util.Hashtable;
import java.util.List;

import org.hibernate.Session;

import salesmachine.hibernatedb.OimVendorSuppliers;
import salesmachine.oim.suppliers.IFileSpecificsProvider;
import salesmachine.oim.suppliers.StandardFileSpecificsProvider;

public class OrderFile {
	Session m_dbSession;
	protected Hashtable fileFormatParams;
	protected List fileFieldMaps;
	
	public OrderFile(Session session) {
		m_dbSession = session;
	}

	public Hashtable getFileFormatParams() {
		return fileFormatParams;
	}
	
	public List getFileFieldMaps() {
		return fileFieldMaps;
	}
	
	public void build() {
		
	}
	
	public IFileSpecificsProvider getSpecificsProvider(OimVendorSuppliers ovs) {
		 return new StandardFileSpecificsProvider(m_dbSession, ovs);
	}
}
