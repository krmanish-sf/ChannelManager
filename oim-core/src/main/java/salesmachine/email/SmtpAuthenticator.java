package salesmachine.email;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;


/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SmtpAuthenticator extends Authenticator {
                private String user = null;
                private String pwd  = null;

                public SmtpAuthenticator(String user, String pwd) {
                        this.user = user;
                        this.pwd = pwd;
                }

                protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(this.user, this.pwd);
                }
        }



