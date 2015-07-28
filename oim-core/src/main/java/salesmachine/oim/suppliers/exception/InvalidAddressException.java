package salesmachine.oim.suppliers.exception;

public class InvalidAddressException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public InvalidAddressException(){
		super();
	}
	public InvalidAddressException(String msg){
		super(msg);
	}
}
