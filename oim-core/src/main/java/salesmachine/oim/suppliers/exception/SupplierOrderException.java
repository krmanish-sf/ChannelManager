package salesmachine.oim.suppliers.exception;

public class SupplierOrderException extends Exception {
	private static final String MSG = "Error in sending order to Supplier.";
	private static final long serialVersionUID = 1L;

	public SupplierOrderException() {
		super(MSG);
	}

	public SupplierOrderException(String message) {
		super(message);
	}

	public SupplierOrderException(String message, Throwable cause) {
		super(message, cause);
	}
}
