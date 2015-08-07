package salesmachine.oim.stores.exception;

public class ChannelCommunicationException extends Exception {
	private static final long serialVersionUID = 1L;

	public ChannelCommunicationException() {
		super();
	}

	public ChannelCommunicationException(String message) {
		super(message);
	}

	public ChannelCommunicationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ChannelCommunicationException(Throwable cause) {
		super(cause);
	}

}
