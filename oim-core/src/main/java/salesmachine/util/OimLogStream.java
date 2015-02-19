package salesmachine.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class OimLogStream {
	PrintStream ofls = null;
	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	public OimLogStream() {
		ofls = new PrintStream(baos);
	}

	public OimLogStream(String filename, boolean printConsole) {
		ofls = OimFileLogStream.getStream(filename, printConsole);
	}

	public void println(StringBuffer x) {
		println(x.toString());
	}

	public void println(String x) {
		if (ofls != null)
			ofls.println(x);
		else
			System.out.println(x);
	}

	public PrintStream getPrintStream() {
		if (ofls != null)
			return ofls;
		return System.out;
	}

	public void close() {
		if (ofls != null) {
			ofls.flush();
			ofls.close();
		}
	}

	@Override
	public String toString() {
		String content = baos.toString();
		return content;
	}
}
