package salesmachine.oim.stores.exception;

public class ChannelOrderFormatException extends Exception {
	private static final long serialVersionUID = -1029361468331513541L;

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
}
