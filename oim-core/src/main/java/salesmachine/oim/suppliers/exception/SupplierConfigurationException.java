package salesmachine.oim.suppliers.exception;

import java.net.MalformedURLException;

public class SupplierConfigurationException extends Exception {

	
	
	public SupplierConfigurationException(String message, MalformedURLException e) {
		super(message, e);
	}

	private static final long serialVersionUID = 6611557051409810924L;
	

}
