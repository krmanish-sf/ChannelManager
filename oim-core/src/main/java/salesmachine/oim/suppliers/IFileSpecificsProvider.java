package salesmachine.oim.suppliers;

import salesmachine.hibernatedb.OimFileFieldMap;
import salesmachine.hibernatedb.OimOrderDetails;

public interface IFileSpecificsProvider {
	public abstract String getFieldValueFromOrder(OimOrderDetails detail, OimFileFieldMap fieldMap);
	public abstract String getLastFileLine();
}
