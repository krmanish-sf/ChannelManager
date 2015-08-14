package salesmachine.oim.suppliers.exception;

public class SupplierConfigurationException extends Exception {
	private static final String MSG = "Supplier configuration information invalid.";

	public SupplierConfigurationException() {
		super(MSG);
	}

	public SupplierConfigurationException(String message) {
		super(message);
	}

	public SupplierConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	private static final long serialVersionUID = 1L;

}
