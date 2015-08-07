package salesmachine.oim.stores.exception;

public class ChannelConfigurationException extends Exception {
	private static final long serialVersionUID = 1L;

	public ChannelConfigurationException() {
		super();
	}

	public ChannelConfigurationException(String message) {
		super(message);
	}

	public ChannelConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ChannelConfigurationException(Throwable cause) {
		super(cause);
	}
}
