package salesmachine.hibernatehelper;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionManager {
	private static Logger log = LoggerFactory.getLogger(SessionManager.class);
	private static final SessionFactory sessionFactory;
	static {
		try {
			// Create the SessionFactory
			// File configFile = new
			// File("/home/shared/salesmachine/hibernatehelper/hibernate.cfg.xml");
			sessionFactory = new Configuration().configure()
					.buildSessionFactory();
		} catch (Throwable ex) {
			// Make sure you log the exception, as it might be swallowed
			log.error("Initial SessionFactory creation failed.", ex);
			throw new ExceptionInInitializerError(ex);
		}
	}
	public static final ThreadLocal<Session> session = new ThreadLocal<Session>();

	public static Session currentSession() {
		Session s = session.get();
		// Open a new Session, if this Thread has none yet
		if (s == null) {
			s = sessionFactory.openSession();
			session.set(s);
		}
		return s;
	}

	public static void closeSession() {
		Session s = (Session) session.get();
		if (s != null)
			s.close();
		session.set(null);
	}
}
