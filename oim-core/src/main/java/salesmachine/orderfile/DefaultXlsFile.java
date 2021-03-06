package salesmachine.orderfile;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.hibernate.Session;

import salesmachine.hibernatedb.OimFields;
import salesmachine.hibernatedb.OimFileFieldMap;
import salesmachine.oim.api.OimConstants;

public class DefaultXlsFile extends OrderFile {
	public DefaultXlsFile(Session session) {
		super(session);		
	}
	
	public void build() {
		// Build the file format params
		fileFormatParams = new Hashtable();
		fileFormatParams.put(OimConstants.FILE_FORMAT_PARAMS_USEHEADER, "1");
		fileFieldMaps = getFileFieldMap();
	}
	
	public List getFileFieldMap() {
		List fileFieldMaps = new ArrayList();
		//For blank headers, header values will be append to next header value which is not blank. 
		//In this case headers after "Description" are all blank so they will append in header "Address"
		String fields[] = {"Sku","Quantity","Description","","",
							"","","","Address","Ship Method",
							"Customer PO","Cost","Manuf# or Customer sku"							
							};
		Integer mappedFieldIds[]= {1,9,25,3,4,
									5,6,7,8,10,
									2,26,1};
		
		for (int i=0;i<fields.length;i++) {
			OimFields field = new OimFields(fields[i],fields[i],new Date(),null,null);
			field.setFieldId(mappedFieldIds[i]);
			OimFileFieldMap ffm = new OimFileFieldMap(null,field,fields[i],new Date(),null,"","");
			fileFieldMaps.add(ffm);
		}
		return fileFieldMaps;				
	}			
}
