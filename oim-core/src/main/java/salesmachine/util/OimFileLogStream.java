package salesmachine.util;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class OimFileLogStream extends PrintStream {
	private boolean printConsole = false;

	public static OimFileLogStream getStream(String filename){
		return getStream(filename,true);
	}
	
	public static OimFileLogStream getStream(String filename, boolean printConsole){
		OimFileLogStream p = null;
		try {
			 p = new OimFileLogStream(filename,printConsole);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return p;
	}
	
	@Override
	public void println(String x) {
		super.println(x);
		
		if (printConsole){
			System.out.println(x);
		}
	}
	
	protected OimFileLogStream(String fileName) throws FileNotFoundException {
		super(fileName);
	}
	
	protected OimFileLogStream(String fileName, boolean printConsole) throws FileNotFoundException {
		super(fileName);
		this.printConsole = printConsole;
	}	
}
