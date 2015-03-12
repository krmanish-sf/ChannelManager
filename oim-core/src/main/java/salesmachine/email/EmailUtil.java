package salesmachine.email;

import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.util.ExcHandle;
import salesmachine.util.Server;

public class EmailUtil {
	private static final Logger log = LoggerFactory.getLogger(EmailUtil.class);
	public static int SEND_TO_ALL = 1;
	public static int SEND_TO_NOT_RECEIVED_ANY = 2;
	public static int SEND_TO_NOT_RECEIVED_THIS_ETID = 3;

	static boolean useHeader;
	public static String MAILUSER = "mailuser@inventorysource.com";
	public static String MAILPWD = "changeit";
	public static String MAILHOST = "mail.inventorysource.com";

	/**
	 * Constructor which sets whether to use a header in email
	 */
	public EmailUtil(boolean useHeader_) {
		if (useHeader_) {
			useHeader = useHeader_;
		}
	}

	/**
	 * Default constructor which uses a header
	 */
	public EmailUtil() {
		useHeader = true;
	}

	/**
	 * Sends an email, with email information specified in the parameters.
	 * 
	 * @return If an error occurs return -1 else return 1
	 */
	public static int sendEmail(String to, String from, String cc,
			String subject, String message, boolean useHeader) {
		// useHeader = useHeader;
		int returnVal = sendEmail(to, from, cc, subject, message, "");
		useHeader = true;
		return returnVal;
	}

	/**
	 * For sending simultaneous email messages (threading).
	 * 
	 * @return If an error occurs return -1 else return 1
	 */
	public int sendEmailNotStatic(String to, String from, String cc,
			String subject, String message, boolean useHeader) {
		// useHeader = useHeader;
		int returnVal = sendEmailNotStatic(to, from, cc, subject, message, "");
		useHeader = true;
		return returnVal;
	}

	/**
	 * Sends an email, with email information specified in the parameters.
	 * 
	 * @return If an error occurs return -1 else return 1
	 */
	public static int sendEmail(String to, String from, String cc,
			String subject, String message) {
		return sendEmail(to, from, cc, subject, message, "");
	}

	/**
	 * Method added by Hambir Singh on 06 Decemeber 2006 Sends an email, with
	 * email information specified in the parameters.
	 * 
	 * @return If an error occurs return -1 else return 1
	 */
	public static int sendEmailWithAttachment(String to, String from,
			String cc, String subject, String message, String filename) {
		return sendEmailWithAttachment(to, from, cc, subject, message,
				filename, "");
	}

	/**
	 * For sending simultaneous email messages (threading).
	 * 
	 * @return If an error occurs return -1 else return 1
	 */
	public int sendEmailNotStatic(String to, String from, String cc,
			String subject, String message) {
		return sendEmailNotStatic(to, from, cc, subject, message, "");
	}

	/**
	 * Sends an email, with email information specified in the parameters.
	 * 
	 * @return If an error occurs return -1 else return 1
	 */
	public static int sendEmail(String to, String from, String cc,
			String subject, String message, String content_type) {
		log.debug("Sending email to:" + to);
		String mailhost = MAILHOST;
		if (!Server.isLocalhost()) {
			mailhost = MAILHOST;
		}
		File f = new File("C:\\Property");
		if (f.exists()) {
			mailhost = "ICE";
		}

		Properties props = System.getProperties();
		// XXX - could use Session.getTransport() and Transport.connect()
		// XXX - assume we're using SMTP
		if (mailhost != null)
			// props.put("mail.smtp.host", mailhost);
			// props.put("mail.MailTransport.protocol", "smtp");
			props.put("mail.smtp.host", mailhost);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "25");
		SmtpAuthenticator auth = new SmtpAuthenticator(MAILUSER, MAILPWD);
		Session session = Session.getInstance(props, auth);

		// Get a Session object
		// Session session = Session.getDefaultInstance(props, null);

		try {
			// construct the message
			Message msg = new MimeMessage(session);
			if (from != null)
				msg.setFrom(new InternetAddress(from));
			else
				msg.setFrom();

			msg.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to, false));
			if (cc != null)
				msg.setRecipients(Message.RecipientType.CC,
						InternetAddress.parse(cc, false));

			msg.setSubject(subject);
			msg.setText(message);

			if (!content_type.equals("")) {
				msg.setHeader("Content-Type", content_type);
			}

			if (useHeader) {
				msg.setHeader("X-Mailer", "Goober");
				java.util.Date d = new java.util.Date();
				msg.setSentDate(d);
			} else {
			}

			// send the thing off
			Transport transport = session.getTransport("smtp");
			transport.connect(mailhost, MAILUSER, MAILPWD);

			Transport.send(msg);
		} catch (Exception e) {
			ExcHandle.printStackTraceToErr(e);
			return -1;
		}

		return 1;
	}

	/**
	 * 
	 * Sends an email, with email information specified in the parameters.
	 * 
	 * @return If an error occurs return -1 else return 1
	 */
	public static int sendEmail(String to, String from, String replyTo,
			String cc, String bcc, String subject, String message,
			String content_type) {
		log.debug("Sending email to:" + to);
		String mailhost = MAILHOST;
		if (!Server.isLocalhost()) {
			mailhost = MAILHOST;
		}
		File f = new File("C:\\Property");
		if (f.exists()) {
			mailhost = "ICE";
		}

		Properties props = System.getProperties();
		// XXX - could use Session.getTransport() and Transport.connect()
		// XXX - assume we're using SMTP
		if (mailhost != null)
			// props.put("mail.smtp.host", mailhost);
			// props.put("mail.MailTransport.protocol", "smtp");
			props.put("mail.smtp.host", mailhost);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "25");
		SmtpAuthenticator auth = new SmtpAuthenticator(MAILUSER, MAILPWD);
		Session session = Session.getInstance(props, auth);

		// Get a Session object
		// Session session = Session.getDefaultInstance(props, null);

		try {
			// construct the message
			Message msg = new MimeMessage(session);
			if (from != null)
				msg.setFrom(new InternetAddress(from));
			else
				msg.setFrom();

			msg.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to, false));
			if (cc != null)
				msg.setRecipients(Message.RecipientType.CC,
						InternetAddress.parse(cc, false));
			if (bcc != null)
				msg.setRecipients(Message.RecipientType.BCC,
						InternetAddress.parse(bcc, false));
			msg.setSubject(subject);

			if (replyTo != null) {
				InternetAddress reply[] = new InternetAddress[1];
				reply[0] = new InternetAddress(replyTo);
				msg.setReplyTo(reply);
			}

			msg.setText(message);

			if (!content_type.equals("")) {
				msg.setHeader("Content-Type", content_type);
			}

			if (useHeader) {
				msg.setHeader("X-Mailer", "Goober");
				java.util.Date d = new java.util.Date();
				msg.setSentDate(d);
			} else {
			}

			// send the thing off
			Transport transport = session.getTransport("smtp");
			transport.connect(mailhost, MAILUSER, MAILPWD);

			Transport.send(msg);
		} catch (Exception e) {
			ExcHandle.printStackTraceToErr(e);
			return -1;
		}

		return 1;
	}

	/**
	 * Method added by Hambir Singh on 06 Decemeber 2006 Sends an email with an
	 * attachement and email other information specified in the parameters.
	 * 
	 * @return If an error occurs return -1 else return 1
	 */
	public static int sendEmailWithAttachment(String to, String from,
			String cc, String subject, String message, String filename,
			String content_type) {

		log.debug("Sending email to:" + to);
		String mailhost = MAILHOST;
		if (!Server.isLocalhost()) {
			mailhost = MAILHOST;
		}
		File f = new File("C:\\Property");
		if (f.exists()) {
			mailhost = "ICE";
		}

		Properties props = System.getProperties();
		// XXX - could use Session.getTransport() and Transport.connect()
		// XXX - assume we're using SMTP
		if (mailhost != null)
			props.put("mail.smtp.host", mailhost);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "25");
		SmtpAuthenticator auth = new SmtpAuthenticator(MAILUSER, MAILPWD);
		Session session = Session.getInstance(props, auth);

		// Get a Session object
		// Session session = Session.getDefaultInstance(props, null);

		try {
			// construct the message
			Message msg = new MimeMessage(session);
			if (from != null)
				msg.setFrom(new InternetAddress(from));
			else
				msg.setFrom();

			msg.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to, false));
			if (cc != null)
				msg.setRecipients(Message.RecipientType.CC,
						InternetAddress.parse(cc, false));

			msg.setSubject(subject);
			// msg.setText(message);

			// create and fill the first message part
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(message);

			// create the second message part
			MimeBodyPart mbp2 = new MimeBodyPart();

			// attach the file to the message
			FileDataSource fds = new FileDataSource(filename);
			mbp2.setDataHandler(new DataHandler(fds));
			mbp2.setFileName(fds.getName());

			// create the Multipart and add its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);
			mp.addBodyPart(mbp2);

			// add the Multipart to the message
			msg.setContent(mp);

			if (!content_type.equals("")) {
				msg.setHeader("Content-Type", content_type);
			}

			if (useHeader) {
				msg.setHeader("X-Mailer", "Goober");
				java.util.Date d = new java.util.Date();
				msg.setSentDate(d);
			} else {

			}

			// send the thing off
			Transport transport = session.getTransport("smtp");
			transport.connect(mailhost, MAILUSER, MAILPWD);
			Transport.send(msg);
		} catch (Exception e) {
			ExcHandle.printStackTraceToErr(e);
			return -1;
		}

		return 1;
	}

	/**
	 * For sending simultaneous email messages (threading).
	 * 
	 * @return If an error occurs return -1 else return 1
	 */
	public int sendEmailNotStatic(String to, String from, String cc,
			String subject, String message, String content_type) {
		log.debug("Sending email to:" + to);
		String mailhost;
		if (System.getProperty("java.version").equals("1.3.0")) { // on UTAH
			mailhost = MAILHOST;
		} else { // texas
			mailhost = MAILHOST;
		}
		Properties props = System.getProperties();
		// XXX - could use Session.getTransport() and Transport.connect()
		// XXX - assume we're using SMTP
		if (mailhost != null)
			props.put("mail.smtp.host", mailhost);

		// Get a Session object
		Session session = Session.getDefaultInstance(props, null);
		// System.out.println("to = " + to);
		// System.out.println("from = " + from);
		try {
			// construct the message
			Message msg = new MimeMessage(session);
			if (from != null)
				msg.setFrom(new InternetAddress(from));
			else
				msg.setFrom();

			msg.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to, false));
			if (cc != null)
				msg.setRecipients(Message.RecipientType.CC,
						InternetAddress.parse(cc, false));

			msg.setSubject(subject);
			msg.setText(message);

			if (!content_type.equals("")) {
				msg.setHeader("Content-Type", content_type);
			}
			if (useHeader) {
				msg.setHeader("X-Mailer", "Goober");
				java.util.Date d = new java.util.Date();
				msg.setSentDate(d);
			} else {
			}

			// send the thing off
			Transport.send(msg);
		} catch (Exception e) {
			ExcHandle.printStackTraceToErr(e);
			return -1;
		}

		return 1;
	}

	public static int sendEmailWithAttachmentAndBCC(String to, String from,
			String replyTo, String cc, String bcc, String subject,
			String message, String filename, String content_type) {
		log.debug("Sending email to:" + to);
		String mailhost = MAILHOST;
		if (!Server.isLocalhost()) {
			mailhost = MAILHOST;
		}
		File f = new File("C:\\Property");
		if (f.exists()) {
			mailhost = "ICE";
		}

		Properties props = System.getProperties();
		// XXX - could use Session.getTransport() and Transport.connect()
		// XXX - assume we're using SMTP
		if (mailhost != null)
			props.put("mail.smtp.host", mailhost);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "25");
		SmtpAuthenticator auth = new SmtpAuthenticator(MAILUSER, MAILPWD);
		Session session = Session.getInstance(props, auth);

		// Get a Session object
		// Session session = Session.getDefaultInstance(props, null);

		try {
			// construct the message
			Message msg = new MimeMessage(session);
			if (from != null)
				msg.setFrom(new InternetAddress(from));
			else
				msg.setFrom();

			msg.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to, false));
			if (cc != null)
				msg.setRecipients(Message.RecipientType.CC,
						InternetAddress.parse(cc, false));
			if (bcc != null)
				msg.setRecipients(Message.RecipientType.BCC,
						InternetAddress.parse(bcc, false));

			msg.setSubject(subject);

			if (replyTo != null) {
				InternetAddress reply[] = new InternetAddress[1];
				reply[0] = new InternetAddress(replyTo);
				msg.setReplyTo(reply);
			}

			// msg.setText(message);

			// create and fill the first message part
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(message);

			// create the second message part
			MimeBodyPart mbp2 = new MimeBodyPart();

			// attach the file to the message
			FileDataSource fds = new FileDataSource(filename);
			mbp2.setDataHandler(new DataHandler(fds));
			mbp2.setFileName(fds.getName());

			// create the Multipart and add its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);
			mp.addBodyPart(mbp2);

			// add the Multipart to the message
			msg.setContent(mp);

			if (!content_type.equals("")) {
				msg.setHeader("Content-Type", content_type);
			}

			if (useHeader) {
				msg.setHeader("X-Mailer", "Goober");
				java.util.Date d = new java.util.Date();
				msg.setSentDate(d);
			} else {

			}

			// send the thing off
			Transport transport = session.getTransport("smtp");
			transport.connect(mailhost, MAILUSER, MAILPWD);
			Transport.send(msg);
		} catch (Exception e) {
			ExcHandle.printStackTraceToErr(e);
			return -1;
		}

		return 1;
	}

}
