package salesmachine.orderfile;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.hibernate.Session;

import salesmachine.hibernatedb.OimFields;
import salesmachine.hibernatedb.OimFileFieldMap;
import salesmachine.oim.api.OimConstants;

public class DefaultCsvFile extends OrderFile {
	public DefaultCsvFile(Session session) {
		super(session);		
	}
	
	public void build() {
		// Build the file format params
		fileFormatParams = new Hashtable();
		fileFormatParams.put(OimConstants.FILE_FORMAT_PARAMS_USEHEADER, "1");
		fileFormatParams.put(OimConstants.FILE_FORMAT_PARAMS_FIELD_DELIMITER, "TAB");
		fileFormatParams.put(OimConstants.FILE_FORMAT_PARAMS_TEXT_DELIMITER, "\"");
	
		fileFieldMaps = getFileFieldMap();
	}
	
	private List getFileFieldMap() {
		List fileFieldMaps = new ArrayList();
		String fields[] = {"CustomerID","Order Number","Company","Attn","Phone",
							"Email","Address1","Address2","City","State",
							"Zip","Country","ItemNo","Item Desc","Qty",
							"UnitPrice","ExtPrice","ShipVia","OrderDate"							
							};
		Integer mappedFieldIds[]= {10000,2,11,3,30,
									31,4,12,5,6,
									7,8,1,24,9,
									26,27,10,0};
		
		for (int i=0;i<fields.length;i++) {
			OimFields field = new OimFields(fields[i],fields[i],new Date(),null,null);
			field.setFieldId(mappedFieldIds[i]);
			OimFileFieldMap ffm = new OimFileFieldMap(null,field,fields[i],new Date(),null,"","");
			fileFieldMaps.add(ffm);
		}
		
		return fileFieldMaps;				
	}			
}
