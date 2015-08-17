package salesmachine.oim.stores.exception;

import salesmachine.oim.stores.api.IOrderImport.ChannelError;

public class ChannelOrderFormatException extends Exception {
	private static final long serialVersionUID = -1029361468331513541L;
	private static final ChannelError errorCode = ChannelError.CHANNEL_ORDERFORMAT_ERROR;

	public ChannelOrderFormatException() {
		super();
	}

	public ChannelOrderFormatException(String message) {
		super(message);
	}

	public ChannelOrderFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public ChannelOrderFormatException(Throwable cause) {
		super(cause);
	}

	public static int getErrorcode() {
		return errorCode.getErrorCode();
	}
}
