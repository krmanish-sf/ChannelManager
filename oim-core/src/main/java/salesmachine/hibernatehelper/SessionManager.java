package salesmachine.hibernatehelper;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * This class has been deprecated in favor of using the Auto-wired <a> SessionFactory<a> in
 * GenericHibernateDao.
 * 
 * @author amit-yadav
 *
 */

public class SessionManager {
  private SessionManager() {
    // Making private to
  }
  
  private static Logger log = LoggerFactory.getLogger(SessionManager.class);
  private static final SessionFactory sessionFactory;
  private static Session sessionObj;

  static {
    try {
      Configuration configuration = new Configuration().configure();
      StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
          .applySettings(configuration.getProperties());
      sessionFactory = configuration.buildSessionFactory(builder.build());

    } catch (Throwable ex) {
      // Make sure you log the exception, as it might be swallowed
      log.error("Initial SessionFactory creation failed.", ex);
      throw new ExceptionInInitializerError(ex);
    }
  }

  public static final ThreadLocal<Session> session = new ThreadLocal<Session>();

  public static Session currentSession() {
    if(sessionObj!=null && sessionObj.isOpen())
      return sessionObj;
    // log.info("Fetching Current Session");
    Session s = session.get();
    // Open a new Session, if this Thread has none yet
    if (s == null) {
      // log.info("No existing session found, opening new Session");
      s = sessionFactory.openSession();
      session.set(s);
    }
    // log.info("Session returned for TenantIdentifier : {}",
    // s.getTenantIdentifier());
    return s;
  }
  
  public static void setSession(Session dbSession){
    session.set(dbSession);
    sessionObj=dbSession;
  }

  public static void closeSession() {
    Session s = (Session) session.get();

    if (s != null)
      s.close();
    session.set(null);
  }
  
  public static Session openSession(){
    return sessionFactory.openSession();
  }
}
