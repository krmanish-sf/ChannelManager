package salesmachine.oim.stores.exception;

import salesmachine.oim.stores.api.IOrderImport.ChannelError;

public class ChannelConfigurationException extends Exception {
	private static final long serialVersionUID = 1L;
	private static final ChannelError errorCode = ChannelError.CHANNEL_CONFIGURATION_ERROR;

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

	public static int getErrorcode() {
		return errorCode.getErrorCode();
	}
}
