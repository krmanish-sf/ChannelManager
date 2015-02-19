package salesmachine.util;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.httpclient.methods.multipart.StringPart;

public class SpecialStringPart extends StringPart {
	public SpecialStringPart(String name, String value) {
		super(name, value);
	}
	public SpecialStringPart(String name, String value, String charset) {
		super(name, value, charset);
	}
	protected void sendTransferEncodingHeader(OutputStream out) throws IOException {
		//nothing to be done......
	}
}
