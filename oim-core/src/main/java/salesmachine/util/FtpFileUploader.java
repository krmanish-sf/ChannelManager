package salesmachine.util;

import java.io.IOException;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPTransferType;

public class FtpFileUploader {
	protected int m_MaxReconnects;
	private int m_Timeout;

	protected String m_Login;
	protected String m_Password;
	protected String m_Url;
	
	protected int m_Error;

	public final static int ERROR_DISCQUOTAEXCEEDED = 1;
	public final static int ERROR_AUTHENTICATIONFAILURE = 2;
	public final static int ERROR_SERVERTIMEOUT = 3;

	public FtpFileUploader(String ftpUrl, String login, String password,
			int maxReconnects, int timeout) {
		m_Url = ftpUrl;
		m_Login = login;
		m_Password = password;
		m_MaxReconnects = maxReconnects;
		m_Timeout = timeout;
		m_Error = 0;
	}

	public String testConnection() {
		FTPClient ftp = null;
		try {
			ftp = Connect(m_Url, m_Login, m_Password);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Disconnected.");
			return e.getMessage();
		} 

		if (ftp != null) {
			try {
				ftp.quit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
		return null;
	}
	
	public void Upload(String remoteDir, String localFile, String remoteFile) throws Exception {
		System.out.println("Uploading " + localFile + " files to " + remoteDir);

		int reconnectionsTried = 0;
		boolean sent = false;
		while (!sent && (reconnectionsTried < m_MaxReconnects)) {
			FTPClient ftp = null;
			try {
				System.out.println("Connecting to "+m_Url+" with login:"+m_Login+" and password: "+m_Password);
				ftp = Connect(m_Url, m_Login, m_Password);
				if (StringHandle.removeNull(remoteDir).length() > 0)
					ftp.chdir(remoteDir);
				ftp.put(localFile, remoteFile);
				m_Error = 0;
				sent = true;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Disconnected.");

				String msg = e.getMessage().trim();
				if ((msg.indexOf("Disc quota exceeded") != -1)
						|| (msg.indexOf("Disk quota exceeded") != -1)) {
					m_Error = ERROR_DISCQUOTAEXCEEDED;
					throw e;
				} else if ((msg.indexOf("incorrect") != -1)
						|| (msg.indexOf("Login authentication failed") != -1)) {
					m_Error = ERROR_AUTHENTICATIONFAILURE;
					throw e;
				}
				reconnectionsTried++;
			} finally {
				if (ftp != null) {
					try {
						ftp.quit();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		if (reconnectionsTried >= m_MaxReconnects) {
			Exception e = new Exception("FTP Server is not responding");
			m_Error = ERROR_SERVERTIMEOUT;
			throw e;			
		}
	}

	protected FTPClient Connect(String ftpDomainURL, String ftpUserName,
			String ftpPassword) throws com.enterprisedt.net.ftp.FTPException,
			IOException {
		String ftpDomainURLPortless;
		int port = 21;

		int lindex = ftpDomainURL.lastIndexOf(":");
		if (lindex == -1) {
			ftpDomainURLPortless = ftpDomainURL;
		} else {
			ftpDomainURLPortless = ftpDomainURL.substring(0, lindex);
			try {
				port = Integer.parseInt(ftpDomainURL.substring(lindex + 1));
			} catch (Exception e) {
				ftpDomainURLPortless = ftpDomainURL;
			}
		}

		FTPClient ftp = null;
		if (port == 21) {
			System.out.println("Connecting to " + ftpDomainURLPortless);
			ftp = new FTPClient(ftpDomainURLPortless);
		} else {
			System.out.println("Connecting to " + ftpDomainURLPortless
					+ " on port " + port);
			ftp = new FTPClient(ftpDomainURLPortless, port);
		}
		ftp.setTimeout(m_Timeout);
		ftp.login(ftpUserName, ftpPassword);
		System.out.println("Logged in to server.");
		ftp.debugResponses(false);
		ftp.setType(FTPTransferType.BINARY);
		System.out.println("Connected to " + ftpDomainURL);
		return ftp;
	}

	public int getError() {
		return m_Error;
	}
	public static String[] getFileListing(String ftpDomainURL,
			String ftpUserName, String ftpPassword, String remoteDir) {
		try {
			FTPClient ftp = null;
			ftp = new FTPClient(ftpDomainURL);
			ftp.login(ftpUserName, ftpPassword);

			String list[] = (ftp.list(remoteDir)).split("\n");
			return list;
		} catch (Exception e) {
			System.out
					.println("Error Occur in getting the File name from the server");			
		}
		return null;
	}
}
