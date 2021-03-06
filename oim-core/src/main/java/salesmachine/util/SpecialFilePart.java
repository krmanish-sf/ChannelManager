package salesmachine.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.PartSource;
public class SpecialFilePart extends FilePart {
	protected void sendTransferEncodingHeader(OutputStream out) throws IOException {
		//nothing to be done......
	}
	public SpecialFilePart(String name, PartSource partSource, String contentType, String charset) {
		super(name,partSource,contentType,charset);
	}
	public SpecialFilePart(String name, PartSource partSource) {
		super(name, partSource);
	}
	public SpecialFilePart(String name, File file) throws FileNotFoundException {
		super(name, file);
	}
	public SpecialFilePart(String name, File file, String contentType, String charset) throws FileNotFoundException {
		super(name, file, contentType, charset);
	}
	public SpecialFilePart(String name, String fileName, File file) throws FileNotFoundException {
		super(name, fileName, file);
	}
	public SpecialFilePart(String name, String fileName, File file, String contentType, String charset) throws FileNotFoundException {
		super(name, fileName, file, contentType, charset);
	}
}
