package salesmachine.hibernatehelper;

import java.util.Iterator;
import java.util.Set;

import salesmachine.hibernatedb.OimChannelAccessDetails;
import salesmachine.hibernatedb.OimChannels;
import salesmachine.hibernatedb.OimFileformatParams;
import salesmachine.hibernatedb.OimFiletypes;
import salesmachine.hibernatedb.OimSupplierMethodattrValues;
import salesmachine.hibernatedb.OimSupplierMethods;

public class PojoHelper {
	public static String getSupplierMethodAttributeValue(OimSupplierMethods method, Integer attributeId) {
		Iterator it = method.getOimSupplierMethodattrValueses().iterator();
		while (it.hasNext()) {
			OimSupplierMethodattrValues value = (OimSupplierMethodattrValues)it.next();
			if (attributeId.equals(value.getOimSupplierMethodattrNames().getAttrId())) {
				System.out.println("Attribute Name: "+value.getOimSupplierMethodattrNames().getAttrName()+"\tValue:"+value.getAttributeValue());
				return value.getAttributeValue();
			}
		}
		
		return null;
	}

	public static String getChannelAccessDetailValue(OimChannels channel, Integer field) {
		Set allDetails = channel.getOimChannelAccessDetailses();
		if (allDetails==null || allDetails.size() == 0)
			return null;
		
		Iterator it = allDetails.iterator();
		while (it.hasNext()) {
			OimChannelAccessDetails oca = (OimChannelAccessDetails)it.next();
			if (oca.getDeleteTm() != null) {
				System.out.println("Ignoring access detail as deleted");
				continue;
			}
			
			if (oca.getOimChannelAccessFields().getFieldId().equals(field))
				return oca.getDetailFieldValue();
		}
		return null;
	}	
	
	public static String getFileFormatParamValue(OimFiletypes oft, String paramName) {
		for (Iterator it = oft.getOimFileformatParamses().iterator();it.hasNext();) {
			OimFileformatParams param = (OimFileformatParams)it.next();
			if (param.getDeleteTm()!=null) {
				System.out.println("Ignoring deleted entry");
				continue;
			}
			if (paramName.equals(param.getParamName()))
					return param.getParamValue();
		}
		return "";
	}
}
