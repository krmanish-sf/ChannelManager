package salesmachine.oim.stores.exception;

import salesmachine.oim.stores.api.IOrderImport.ChannelError;

public class ChannelCommunicationException extends Exception {
	private static final long serialVersionUID = 1L;
	private static final ChannelError errorCode = ChannelError.CHANNEL_COMMUNICATION_ERROR;

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

	public static int getErrorcode() {
		return errorCode.getErrorCode();
	}

}
