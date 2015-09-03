package salesmachine.oim.suppliers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class CsvMap {
	private File csvFile;
	private String[] header;
	private String splitter;
	private String encoding = "UTF-8";
	private int headerIndex;
	private int lineIndex;
	private BufferedReader br;
	private String line;

	public CsvMap(File csvFile, String[] header, String splitter, int headerIndex) throws Exception {
		this.csvFile = csvFile;
		this.header = header;
		this.splitter = splitter;
		this.headerIndex = headerIndex;
		initAndCheckHeader();
	}
	
	// overloaded - calling initAndCheckHeader because there was a problem with checking headers.
	public CsvMap(String[] header,File csvFile, String splitter, int headerIndex) throws Exception {
		this.csvFile = csvFile;
		this.header = header;
		this.splitter = splitter;
		this.headerIndex = headerIndex;
		initAndCheckHeader();
	}

	/**
	 * @param csvFile
	 * @param header
	 * @param splitter
	 * @param encoding
	 * @param headerIndex
	 * @throws Exception
	 */
	public CsvMap(File csvFile, String[] header, String splitter, String encoding, int headerIndex) throws Exception {
		this.csvFile = csvFile;
		this.header = header;
		this.splitter = splitter;
		this.encoding = encoding;
		this.headerIndex = headerIndex;
		initAndCheckHeader();
	}

	private void init() throws Exception {
		br = new BufferedReader(new InputStreamReader(new FileInputStream(this.csvFile), encoding));
		while(headerIndex != lineIndex && hasNext()){
			if(!checkCsvHeader(line.split(splitter))){
				close();
				throw new Exception("HEDAER MISMATCH");
			}
		}
	}
	private void initAndCheckHeader() throws Exception {
		br = new BufferedReader(new InputStreamReader(new FileInputStream(this.csvFile), encoding));
		while(hasNext()){
			if(headerIndex == lineIndex){
				if(!checkCsvHeader(line.split(splitter))){
					close();
					throw new Exception("HEDAER MISMATCH");
				}
				break;
			}
		}
	}

	private void close() {
		try {
			br.close();
		} catch (IOException e) {}
	}

	private boolean checkCsvHeader(String[] csvFields) {
		if (csvFields.length != header.length) {
			System.out.println("HEADER DID NOT MATCH: EXPECTED HEADER SIZE : " + header.length + " ACTUAL HEADER SIZE : " + csvFields.length);
			return false;
		}
		for (int i = 0; i < csvFields.length; i++) {
			if (!csvFields[i].trim().equals(header[i])) {
				System.out.println("HEADER DID NOT MATCH: EXPECTED : " + header[i] + " GOT IN FEED : " + csvFields[i]);
				return false;
			}
		}
		System.out.println("Header Matched..");
		return true;
	}

	public HashMap next() {
		String[] values = line.split(splitter);
		if (values.length > header.length) {
			System.out.println("No of values in line number : " + lineIndex + " has more fields than expected in header. Ignoring..");
			return null;
		}
		HashMap hash = new HashMap();
		for (int i = 0; i < values.length; i++) {
			if ("".equals(header[i])) {
				continue;
			}
			hash.put(header[i], values[i].replaceAll("\"", ""));
		}
		return hash;
	}

	public boolean hasNext() throws IOException {
		line = br.readLine();
		if(line == null){
			close();
			return false;
		}
		lineIndex++;
		return true;
	}
}