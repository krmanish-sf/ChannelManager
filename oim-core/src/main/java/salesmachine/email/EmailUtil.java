package salesmachine.email;

import java.util.Iterator;
import java.util.List;
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

public class EmailUtil {

	private static final Logger log = LoggerFactory.getLogger(EmailUtil.class);
	public static int SEND_TO_ALL = 1;
	public static int SEND_TO_NOT_RECEIVED_ANY = 2;
	public static int SEND_TO_NOT_RECEIVED_THIS_ETID = 3;

	private boolean useHeader;
	private static final String MAILUSER = "support@inventorysource.com";
	private static final String MAILPWD = "IS#312@8965";
	private static final String MAILHOST = "smtp.gmail.com";
	private static final String MAILPORT = "465";
	private static Properties props;
	static {
		props = new Properties();
		props.put("mail.smtp.host", MAILHOST);
		props.put("mail.smtp.socketFactory.port", MAILPORT);
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", MAILPORT);
	}

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
		int returnVal = sendEmail(to, from, cc, subject, message, "", useHeader);
		return returnVal;
	}

	/**
	 * For sending simultaneous email messages (threading).
	 *
	 * @return If an error occurs return -1 else return 1
	 */
	public int sendEmailNotStatic(String to, String from, String cc,
			String subject, String message, boolean useHeader) {
		this.useHeader = useHeader;
		int returnVal = sendEmailNotStatic(to, from, cc, subject, message, "");
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
	 * ] * @return If an error occurs return -1 else return 1
	 */
	public int sendEmailNotStatic(String to, String from, String cc,
			String subject, String message) {
		return sendEmailNotStatic(to, from, cc, subject, message, "");
	}

	public static int sendEmail(String to, String from, String cc,
			String subject, String message, String content_type) {
		return sendEmail(to, from, cc, subject, message, true);
	}

	/**
	 * Sends an email, with email information specified in the parameters.
	 *
	 * @return If an error occurs return -1 else return 1
	 */
	public static int sendEmail(String to, String from, String cc,
			String subject, String message, String content_type,
			boolean useHeader) {

		log.debug("Entered sendEmail. TO:" + to);
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
			// Transport transport = session.getTransport("smtp");
			// transport.connect(MAILHOST, MAILUSER, MAILPWD);

			Transport.send(msg);
		} catch (Exception e) {
			ExcHandle.printStackTraceToErr(e);
			return -1;
		}

		return 1;
	}

	public static int sendEmailWithReplyTo(String to, String from,
			String replyto, String cc, String subject, String message,
			String content_type) {
		return sendEmailWithReplyTo(to, from, replyto, cc, subject, message,
				content_type, true);
	}

	/**
	 * Sends an email, with email information specified in the parameters.
	 *
	 * @return If an error occurs return -1 else return 1
	 */
	public static int sendEmailWithReplyTo(String to, String from,
			String replyto, String cc, String subject, String message,
			String content_type, boolean useHeader) {

		log.debug("Entered sendEmail. TO:" + to);
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
			if (replyto != null) {
				msg.setReplyTo(InternetAddress.parse(replyto, false));
			}

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
			// Transport transport = session.getTransport("smtp");
			// transport.connect(MAILHOST, MAILUSER, MAILPWD);

			Transport.send(msg);
		} catch (Exception e) {
			ExcHandle.printStackTraceToErr(e);
			return -1;
		}

		return 1;
	}

	public static int sendEmail(String to, String from, String cc, String bcc,
			String subject, String message, String content_type) {

		return sendEmail(to, from, cc, bcc, subject, message, content_type,
				true);
	}

	/***
	 * This method will accept bcc field also apart from the normal parameters
	 * 
	 * @param to
	 * @param from
	 * @param cc
	 * @param bcc
	 * @param subject
	 * @param message
	 * @param content_type
	 * @return
	 */
	public static int sendEmail(String to, String from, String cc, String bcc,
			String subject, String message, String content_type,
			boolean useHeader) {

		log.debug("Entered sendEmail. TO:" + to);
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
			// Transport transport = session.getTransport("smtp");
			// transport.connect(MAILHOST, MAILUSER, MAILPWD);

			Transport.send(msg);
		} catch (Exception e) {
			ExcHandle.printStackTraceToErr(e);
			return -1;
		}

		return 1;
	}

	public static int sendEmailWithAttachment(String to, String from,
			String cc, String subject, String message, String filename,
			String content_type) {
		return sendEmailWithAttachment(to, from, cc, subject, message,
				filename, content_type, true);
	}

	/**
	 * Method added by Hambir Singh on 06 Decemeber 2006 Sends an email with an
	 * attachement and email other information specified in the parameters.
	 *
	 * @return If an error occurs return -1 else return 1
	 */
	public static int sendEmailWithAttachment(String to, String from,
			String cc, String subject, String message, String filename,
			String content_type, boolean useHeader) {

		log.debug("Entered sendEmail. TO:" + to);
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
			// Transport transport = session.getTransport("smtp");
			// transport.connect(MAILHOST, MAILUSER, MAILPWD);
			Transport.send(msg);
		} catch (Exception e) {
			ExcHandle.printStackTraceToErr(e);
			return -1;
		}

		return 1;
	}

	public static int sendEmailWithAttachment(String to, String from,
			String cc, String subject, String message, List attachments,
			String content_type) {
		return sendEmailWithAttachment(to, from, cc, subject, message,
				attachments, content_type, true);
	}

	public static int sendEmailWithAttachment(String to, String from,
			String cc, String subject, String message, List attachments,
			String content_type, boolean useHeader) {

		log.debug("Entered sendEmail. TO:" + to);
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
			// create the Multipart and add its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);

			// attach the file to the message
			for (Iterator it = attachments.iterator(); it.hasNext();) {
				// create the second message part
				MimeBodyPart mbp2 = new MimeBodyPart();
				FileDataSource fds = new FileDataSource((String) it.next());
				mbp2.setDataHandler(new DataHandler(fds));
				mbp2.setFileName(fds.getName());
				mp.addBodyPart(mbp2);
			}

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
			// Transport transport = session.getTransport("smtp");
			// transport.connect(MAILHOST, MAILUSER, MAILPWD);
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

		log.debug("Entered sendEmail. TO:" + to);
		Session session = Session.getDefaultInstance(props, null);
		// log.debug("to = " + to);
		// log.debug("from = " + from);
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

}
