package salesmachine.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.servlet.jsp.JspWriter;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpecBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;

import com.stevesoft.pat.Regex;

@Deprecated
public class FormObject2 {
	public static final String ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
	public static final String ACCEPT_LANGUAGE = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
	public static final String ACCEPT_ENCODING = "gzip,deflate";
	public static final String ACCEPT_CHARSET = "ISO-8859-1,utf-8;q=0.7,*;q=0.7";
	public static final String KEEP_ALIVE = "300";
	public static final String CONNECTION = "keep-alive";

	public String Referer;
	public NameValuePair[] form_data;
	public String codeBase, file, desc, match, conType, error, storeFile,
			fileName, fromIP;
	public String from_url, accept, accept_language, userAgent;
	public String[] regMatches;
	public boolean print, store, printResponse, okay, getPage, printBytes,
			printPostingError = true;
	private boolean handleRedirects = false;
	public PrintWriter out;
	public JspWriter jspOut;
	public String page, context;
	public InputStream pageStream;
	public int port = -1;
	public int timeOut;
	public String Location;
	public StringBuffer pageBuffer;
	public URL url;
	public String effectiveURI;
	public String effectiveURL;
	public String post_content;
	public int maxRedirectCount = 10, currentRedirectCount = 1;
	public int statusCode;
	public boolean useFastWay;
	private boolean addAuthorization = false;
	public String authRealm, authUsername, authPassword;
	private boolean saveFile = false;
	public FileOutputStream fileOut;
	private boolean getResponseStream = false;
	public String fileAfterRedirect;

	public HttpClient httpclient;
	public HostConfiguration hostconfig;
	private GetMethod getmethod;
	private PostMethod postmethod;
	private MultipartPostMethod multipartpostmethod;

	private Cookie[] allCookies;
	private CookieSpecBase cookieSpec;

	// private BufferedReader in;
	public FormObject2(String codeBase_, String file_, String desc_,
			String match_, String[] regMatches_, boolean print_,
			boolean store_, boolean printResponse_, PrintWriter out_,
			String context_) {
		codeBase = codeBase_;
		file = file_;
		form_data = null;
		desc = desc_;
		match = match_;
		conType = "http";
		regMatches = regMatches_;
		print = print_;
		store = store_;
		printResponse = printResponse_;
		out = out_;
		error = "";
		getPage = true;
		context = context_;
		from_url = "";
	}

	public FormObject2(String codeBase_, String file_, String desc_,
			String match_, String[] regMatches_, boolean print_,
			boolean store_, boolean printResponse_, JspWriter out_,
			String context_) {
		codeBase = codeBase_;
		file = file_;
		form_data = null;
		desc = desc_;
		match = match_;
		conType = "http";
		regMatches = regMatches_;
		print = print_;
		store = store_;
		printResponse = printResponse_;
		jspOut = out_;
		error = "";
		getPage = true;
		context = context_;
		from_url = "";
	}

	public FormObject2(String codeBase_, int port_, String file_, String desc_,
			String match_, boolean print_, boolean store_,
			boolean printResponse_, JspWriter out_, String context_) {
		codeBase = codeBase_;
		file = file_;
		form_data = null;
		desc = desc_;
		match = match_;
		conType = "http";
		print = print_;
		store = store_;
		printResponse = printResponse_;
		jspOut = out_;
		error = "";
		getPage = true;
		context = context_;
		from_url = "";
		port = port_;
	}

	public FormObject2(String codeBase_, String file_, String desc_,
			String match_, boolean print_, boolean store_,
			boolean printResponse_, PrintWriter out_, String context_) {
		codeBase = codeBase_;
		file = file_;
		form_data = null;
		desc = desc_;
		match = match_;
		conType = "http";
		regMatches = null;
		print = print_;
		store = store_;
		printResponse = printResponse_;
		out = out_;
		error = "";
		getPage = true;
		context = context_;
		from_url = "";
	}

	public FormObject2(String codeBase_, String file_, String desc_,
			String match_, boolean print_, boolean store_,
			boolean printResponse_, JspWriter out_, String context_) {
		codeBase = codeBase_;
		file = file_;
		form_data = null;
		desc = desc_;
		match = match_;
		conType = "http";
		regMatches = null;
		print = print_;
		store = store_;
		printResponse = printResponse_;
		jspOut = out_;
		error = "";
		getPage = true;
		context = context_;
		from_url = "";
	}

	public FormObject2(PrintWriter out_, String context_) {
		codeBase = null;
		file = null;
		form_data = null;
		desc = null;
		match = null;
		conType = "http";
		regMatches = null;
		print = false;
		store = true;
		printResponse = false;
		out = out_;
		error = "";
		getPage = true;
		context = context_;
		from_url = "";
	}

	public FormObject2(JspWriter out_, String context_) {
		codeBase = null;
		file = null;
		form_data = null;
		desc = null;
		match = null;
		conType = "http";
		regMatches = null;
		print = false;
		store = true;
		printResponse = false;
		jspOut = out_;
		error = "";
		getPage = true;
		context = context_;
		from_url = "";
	}

	public void addData(String[] pairs) {
		form_data = new NameValuePair[pairs.length / 2];
		for (int i = 0; i < pairs.length; i += 2) {
			form_data[i / 2] = new NameValuePair(pairs[i], pairs[i + 1]);
		}
		return;
	}

	public void addPostContent(String content) {
		this.post_content = content;
	}

	public void newHttpClient() {
		httpclient = new HttpClient();
		// Get initial state object
		HttpState initialState = new HttpState();
		// initialState.setCookiePolicy(CookiePolicy.RFC2109);
		initialState.setCookiePolicy(CookiePolicy.COMPATIBILITY);
		httpclient.setState(initialState);
		allCookies = new Cookie[50];
		cookieSpec = new CookieSpecBase();
	}

	private void addHeaders(HttpMethod aMethod) {
		Header aHeader = new Header("User-Agent",
				"Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)");
		aMethod.setRequestHeader(aHeader);

		aHeader = new Header("Accept", this.ACCEPT);
		aMethod.setRequestHeader(aHeader);

		aHeader = new Header("Accept-Language", this.ACCEPT_LANGUAGE);
		aMethod.setRequestHeader(aHeader);

		aHeader = new Header("Accept-Encoding", this.ACCEPT_ENCODING);
		aMethod.setRequestHeader(aHeader);

		aHeader = new Header("Accept-Charset", this.ACCEPT_CHARSET);
		aMethod.setRequestHeader(aHeader);

		aHeader = new Header("Connection", this.CONNECTION);
		aMethod.setRequestHeader(aHeader);

		aHeader = new Header("Keep-Alive", this.KEEP_ALIVE);
		aMethod.setRequestHeader(aHeader);

		if (this.Referer != null && !"".equals(this.Referer)) {
			aHeader = new Header("Referer", this.Referer);
			aMethod.setRequestHeader(aHeader);
		}
	}

	public HttpClient getHttpClient() {
		if (httpclient == null) {
			newHttpClient();
		}
		return httpclient;
	}

	public int getStatusCode() {
		return this.statusCode;
	}

	public void setHttpClient(HttpClient client) {
		this.httpclient = client;
	}

	public void setFromHeader(String from_url_) {
		from_url = from_url_;
		return;
	}

	public void setCurrentRedirectCount(int currentRedirectCount_) {
		currentRedirectCount = currentRedirectCount_;
		return;
	}

	public void https() {
		conType = "https";
		return;
	}

	public void setTimeOut(int time) {
		this.timeOut = time;
	}

	public Hashtable hitForm(String reqType, String uploadFile, String fileName) {
		this.fileName = fileName;
		return hitForm(reqType, uploadFile);
	}

	public Hashtable hitForm(String reqType, String uploadFile) {
		error = "";
		boolean check = false;
		page = "";
		String status;
		String inputLine;
		Hashtable matches = new Hashtable();
		storeFile = null;
		int matchesInd = 0;
		int i = 0;
		int j = 0;
		int length = 0;

		if (httpclient == null) {
			this.newHttpClient();
		}

		pageBuffer = null;
		try {
			if (store)
				error += "Attempting " + desc + "\n";
			try {

				if (this.userAgent == null || "".equals(this.userAgent)) {
					this.userAgent = "Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)";
				}

				// set the config
				hostconfig = new HostConfiguration();
				hostconfig.setHost(this.codeBase, this.port, this.conType);
				// System.out.println("Setting Timeout...");
				if (this.timeOut > 0) {
					System.out.println("Setting FormObject2 Timeout");
					httpclient.setConnectionTimeout(this.timeOut);
				} else {
					httpclient.setConnectionTimeout(10000);
				}

				// System.out.println("Doing Method...");

				// do the request
				if ("Get".equals(reqType)) {
					getmethod = new SpecialGetMethod(this.conType + "://"
							+ this.codeBase + this.file);

					this.addHeaders(getmethod);
					getmethod.setFollowRedirects(true);
					if (form_data != null)
						getmethod.setQueryString(form_data);
					// if (sendCookies != null && sendCookies.length > 0)
					// getmethod.setRequestHeader(cookieSpec.formatCookieHeader(sendCookies));

					statusCode = httpclient
							.executeMethod(hostconfig, getmethod);
					if (!getResponseStream) {
						this.page = getmethod.getResponseBodyAsString();
					} else {
						this.pageStream = getmethod.getResponseBodyAsStream();
					}

					Header location = getmethod.getResponseHeader("Location");
					if (location != null) {
						this.Location = location.getValue();
					}
					if (getmethod.getURI() != null) {
						URI uri = getmethod.getURI();
						this.effectiveURI = uri.getURI();
						this.effectiveURL = uri.getURI();
					}
				}

				if ("Post".equals(reqType)) {
					postmethod = new SpecialPostMethod(this.conType + "://"
							+ this.codeBase + this.file);
					this.addHeaders(postmethod);
					// /postmethod.setFollowRedirects(true);
					if (form_data != null)
						postmethod.setRequestBody(form_data);
					// if (sendCookies != null && sendCookies.length > 0)
					// postmethod.setRequestHeader(cookieSpec.formatCookieHeader(sendCookies));

					statusCode = httpclient.executeMethod(hostconfig,
							postmethod);
					if (!getResponseStream) {
						this.page = postmethod.getResponseBodyAsString();
					} else {
						this.pageStream = postmethod.getResponseBodyAsStream();
					}
					Header location = postmethod.getResponseHeader("Location");
					if (location != null) {
						this.Location = location.getValue();
					}
					if (postmethod.getURI() != null) {
						URI uri = postmethod.getURI();
						this.effectiveURI = uri.getURI();
						this.effectiveURL = uri.getURI();
					}

				}

				if ("PostEbay".equals(reqType)) {
					postmethod = new SpecialPostMethod(this.conType + "://"
							+ this.codeBase + this.file);
					this.addHeaders(postmethod);

					Header aHeader = new Header("Content-Type",
							"application/x-www-form-urlencoded");
					postmethod.setRequestHeader(aHeader);
					postmethod.setFollowRedirects(true);
					if (form_data != null) {
						postmethod.setRequestBody(form_data);
						StringBuffer buffer = new StringBuffer(1024);
						for (int index = 0; index < form_data.length; index++) {
							buffer.append(form_data[index].getName() + "="
									+ form_data[index].getValue() + "&");
						}
						String queryString = buffer.toString();
						System.out.println(queryString);
						String contentLength = "" + (queryString.length() - 1);
						aHeader = new Header("Content-Length", contentLength);
						postmethod.setRequestHeader(aHeader);
					}
					statusCode = httpclient.executeMethod(hostconfig,
							postmethod);
					System.out.println("Status Code -> " + statusCode);
					if (!getResponseStream) {
						this.page = postmethod.getResponseBodyAsString();
					} else {
						this.pageStream = postmethod.getResponseBodyAsStream();
					}
					if (saveFile) {
						// System.out.println("Going to save the file. The page stream object is "
						// + (this.pageStream == null ? "invalid" : "valid") +
						// ".");
						BufferedReader br = new BufferedReader(
								new InputStreamReader(this.pageStream));
						String line = null;
						int numLines = 0;
						while ((line = br.readLine()) != null) {
							line = line + "\n";
							fileOut.write(line.getBytes());
							numLines++;
						}
						System.out.println("File written successfully with "
								+ numLines + " lines.");
						br.close();
						fileOut.close();
					}
					Header location = postmethod.getResponseHeader("Location");
					if (location != null) {
						this.Location = location.getValue();
					}
					if (postmethod.getURI() != null) {
						URI uri = postmethod.getURI();
						this.effectiveURI = uri.getURI();
						this.effectiveURL = uri.getURI();
					}
				}

				if ("PostFile".equals(reqType)) {
					multipartpostmethod = new MultipartPostMethod(this.conType
							+ "://" + this.codeBase + this.file);
					this.addHeaders(multipartpostmethod);
					multipartpostmethod.setFollowRedirects(true);
					if (form_data != null) {
						for (int k = 0; k < form_data.length; k++) {
							multipartpostmethod.addParameter(
									form_data[k].getName(),
									form_data[k].getValue());
						}
					}
					File f = new File(uploadFile);
					FilePart filepart = new FilePart(this.fileName, f);
					multipartpostmethod.addPart(filepart);
					// if (sendCookies != null && sendCookies.length > 0)
					// multipartpostmethod.setRequestHeader(cookieSpec.formatCookieHeader(sendCookies));

					statusCode = httpclient.executeMethod(hostconfig,
							multipartpostmethod);
					if (!getResponseStream) {
						this.page = multipartpostmethod
								.getResponseBodyAsString();
					} else {
						this.pageStream = multipartpostmethod
								.getResponseBodyAsStream();
					}
					Header location = multipartpostmethod
							.getResponseHeader("Location");
					if (location != null) {
						this.Location = location.getValue();
					}
					if (multipartpostmethod.getURI() != null) {
						URI uri = multipartpostmethod.getURI();
						this.effectiveURI = uri.getURI();
						this.effectiveURL = uri.getURI();
					}
				}

				if ("PostEbayProstoresFile".equals(reqType)) {
					multipartpostmethod = new MultipartPostMethod(this.conType
							+ "://" + this.codeBase + this.file);
					this.addHeaders(multipartpostmethod);
					Header aHeader = new Header("Content-Transfer-Encoding",
							"base64");
					multipartpostmethod.addRequestHeader(aHeader);
					aHeader = new Header("Content-Type", "text/csv");
					multipartpostmethod.addRequestHeader(aHeader);
					multipartpostmethod.setFollowRedirects(true);
					if (form_data != null) {
						for (int k = 0; k < form_data.length; k++) {
							multipartpostmethod.addPart(new SpecialStringPart(
									form_data[k].getName(), form_data[k]
											.getValue()));
						}
					}
					// StringPart bodypart = new
					// StringPart("xte",this.post_content);
					// bodypart.setCharSet(this.post_content);
					// multipartpostmethod.addPart(bodypart);

					// multipartpostmethod.setQueryString(this.post_content);
					// multipartpostmethod.addParameter("xml",
					// this.post_content);
					/*
					 * Header[] headers =
					 * multipartpostmethod.getRequestHeaders();
					 * System.out.println(
					 * "==================================================================="
					 * ); for (int ix = 0; ix < headers.length;ix++) {
					 * StringBuffer buffer = new StringBuffer(1024); aHeader =
					 * headers[ix]; buffer.append("Name: " + aHeader.getName() +
					 * ", Value: " + aHeader.getValue());
					 * System.out.println("Header " + (ix+1) + ":-> " +
					 * buffer.toString()); } System.out.println(
					 * "==================================================================="
					 * );
					 */
					File f = new File(uploadFile);
					FilePart filepart = new SpecialFilePart(this.fileName, f,
							"text/csv", "ISO-8859-1");
					filepart.setContentType("text/csv");
					filepart.setTransferEncoding("");
					filepart.setCharSet("ISO-8859-1");
					multipartpostmethod.addPart(filepart);
					// if (sendCookies != null && sendCookies.length > 0)
					// multipartpostmethod.setRequestHeader(cookieSpec.formatCookieHeader(sendCookies));
					System.out.println(multipartpostmethod
							.getRequestHeader("Content-Type"));
					statusCode = httpclient.executeMethod(hostconfig,
							multipartpostmethod);
					if (!getResponseStream) {
						this.page = multipartpostmethod
								.getResponseBodyAsString();
					} else {
						this.pageStream = multipartpostmethod
								.getResponseBodyAsStream();
					}
					Header location = multipartpostmethod
							.getResponseHeader("Location");
					if (location != null) {
						this.Location = location.getValue();
					}
					if (multipartpostmethod.getURI() != null) {
						URI uri = multipartpostmethod.getURI();
						this.effectiveURI = uri.getURI();
						this.effectiveURL = uri.getURI();
					}
				}

				// Get all the cookies
				Cookie[] cookies = httpclient.getState().getCookies();
				// int allCookieCount = realCookies.size();
				// Display the cookies
				// System.out.println("Present cookies: ");
				for (int h = 0; h < cookies.length; h++) {
					// System.out.println(" - " + cookies[h].toExternalForm());
					// allCookies[h+allCookieCount] = cookies[h];
				}

			} catch (Exception e) {
				System.out.println(e);
				ExcHandle.printStackTraceToErr(e);
			} finally {
				if (!getResponseStream) {
					if (getmethod != null)
						getmethod.releaseConnection();
					if (postmethod != null)
						postmethod.releaseConnection();
					if (multipartpostmethod != null)
						multipartpostmethod.releaseConnection();
				}
			}

			if (handleRedirects && statusCode == 302
					&& currentRedirectCount <= maxRedirectCount
					&& Location != null && !"".equals(Location)) { // handle
																	// redirect!
				System.out.println("\n" + "!!! REDIRECTING TO:\n" + Location);

				boolean hasCodeBase = false;
				if (Location.startsWith("http")) {
					hasCodeBase = true;
				}

				boolean isSecure = false;
				if (Location.startsWith("https:")) {
					isSecure = true;
				}

				String codeBase_ = "", file_ = "";
				if (!hasCodeBase) {
					if ("https".equals(conType)) {
						isSecure = true;
					}
					codeBase_ = this.codeBase;
					file_ = Location;
				} else {
					// ensure that code bases end with a '/'. NOT like
					// "http://my.yahoo.com" but instead like
					// "http://my.yahoo.com/"
					if (Location.lastIndexOf("/") <= (Location
							.lastIndexOf("//") + 1)) {
						Location += "/";
					}

					Regex locationMatch = Regex
							.perlCode("/http:\\/\\/(.+?)(\\/.*)/gi");
					if (isSecure) {
						locationMatch = Regex
								.perlCode("/https:\\/\\/(.+?)(\\/.*)/gi");
					}

					if (locationMatch.search(Location)) {
						codeBase_ = locationMatch.stringMatched(1);
						file_ = locationMatch.stringMatched(2);
					} else {
						System.out
								.println("Failed to parse location for redirect.");
					}
				}

				System.out.println("codeBase_ = \"" + codeBase_ + "\"");
				System.out.println("file_ = \"" + file_ + "\"");
				System.out.println("currentRedirectCount = "
						+ currentRedirectCount);
				this.fileAfterRedirect = file_;

				FormObject2 formObj = new FormObject2(codeBase_, file_,
						"inside FormObject2 redirecting...", "", regMatches,
						print, store, printResponse, out, context);
				if (isSecure) {
					formObj.https();
				}
				formObj.setHttpClient(this.httpclient);
				formObj.handleRedirects();
				formObj.setCurrentRedirectCount((currentRedirectCount + 1));
				formObj.setTimeOut(this.timeOut);
				matches = formObj.hitForm("Get", null);
				// matches = formObj.hitForm(reqType,null);

				this.Location = formObj.Location;
				statusCode = formObj.getStatusCode();
				this.page = formObj.page;
				this.pageBuffer = new StringBuffer(formObj.page);
				this.okay = formObj.okay;
				this.httpclient = formObj.getHttpClient();
				this.effectiveURI = formObj.effectiveURI;
				this.effectiveURL = formObj.effectiveURL;
				this.url = formObj.url;

				if (store) {
					error += formObj.error;
				}

				return matches;
			}

			boolean hasCodeBase = false;

			String codeBase_ = "", file_ = "";
			if (!hasCodeBase) {
				codeBase_ = this.codeBase;
				file_ = Location;
			} else {
				Regex locationMatch = Regex
						.perlCode("/http:\\/\\/(.+?)(\\/.+)/gi");
				if (locationMatch.search(Location)) {
					codeBase_ = locationMatch.stringMatched(1);
					file_ = locationMatch.stringMatched(2);
				}
			}

			Regex signedout = Regex.perlCode("/" + match + "/");
			boolean returnEarly = false;
			if (regMatches == null) {
				length = 0;
				if (!getPage) {
					returnEarly = true;
				}
			} else {
				length = regMatches.length;
			}
			storeFile = "";
			boolean keepGoing = true;
			int lineNum = 0;
			StringTokenizer st = new StringTokenizer(this.page, "\n");
			pageBuffer = new StringBuffer();
			while (st.hasMoreTokens() && keepGoing) {
				inputLine = st.nextToken();
				lineNum++;
				pageBuffer.append(inputLine + "\n");
				// System.out.println(inputLine);
				if (printResponse) {
					System.out.println(inputLine);
				}
				if (signedout.search(inputLine)) {
					check = true;
					if (returnEarly && !match.equals("")) {
						keepGoing = false;
					}
				}

				if (uploadFile instanceof String) {
					if (uploadFile.equals("store")) {
						storeFile += inputLine;
					}
				}

				for (i = 0; i < length; i += 2) {
					Filter filt = new Filter("");
					Regex temp = Regex.perlCode(regMatches[i]);
					if (temp == null) {
						// System.out.println("TEMP IS NULL -- '"+regMatches[i]+"'");
					} else {
						// System.out.println("MATCH = " +regMatches[i+1]);
					}
					if (temp.search(inputLine)) {
						// System.err.println(regMatches[i] + " here");
						int absInd = Math.abs(new Integer(regMatches[i + 1])
								.intValue());
						for (j = 1; j <= absInd; j++) {
							if (temp.stringMatched(j) instanceof String) {
								matches.put(new Integer(i / 2 + 1).toString()
										+ ":" + new Integer(j).toString(),
										temp.stringMatched(j));
								if (new Integer(regMatches[i + 1]).intValue() < 0) {
									if (!getPage) {
										keepGoing = false;
									}
								}
							}
						}
					}
				}
			}
			if (check) {
				if (store)
					error += desc + " Successful!\n";
				okay = true;
			} else {
				if (store)
					error += desc + " Unsuccessful! Reason:\n"
							+ pageBuffer.toString();
				okay = false;
				return matches;
			}

		} catch (Exception e) {
			ExcHandle.printStackTraceToErr(e);
			if (store)
				error += ExcHandle.getStackTraceAsString(e) + "\n";
			okay = false;
			return matches;
		} catch (Error e) {
			System.err.println("ERROR! == " + e.toString());
			ExcHandle.printErrorStackTraceToErr(e);

		} finally {

			if (pageBuffer != null)
				page = pageBuffer.toString();

		}
		okay = true;

		return matches;
	}

	public void getPage() {
		getPage = true;
	}

	public void handleRedirects() {
		handleRedirects = true;
	}

	public void addAuthorization(String realm, String username, String password) {
		addAuthorization = true;
		authRealm = realm;
		authUsername = username;
		authPassword = password;
	}

	public void saveReceivedFile(String path) {
		saveFile = true;
		try {
			fileOut = new FileOutputStream(path);
		} catch (Exception e) {
			saveFile = false;
			System.out.println(ExcHandle.getStackTraceAsString(e));
		}
	}

	public void returnResponseStream() {
		getResponseStream = true;
	}

	public void closeStreams() {
		if (getmethod != null)
			getmethod.releaseConnection();
		if (postmethod != null)
			postmethod.releaseConnection();
		if (multipartpostmethod != null)
			multipartpostmethod.releaseConnection();
	}

}
