package salesmachine.util;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.httpclient.cookie.MalformedCookieException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SpecialPostMethod extends PostMethod {
	private static final Log log = LogFactory.getLog(SpecialPostMethod.class);
	public SpecialPostMethod(String something) {
		super(something);
	}
	protected void processResponseHeaders(HttpState state, HttpConnection conn) {
		log.debug("Going to process headers from the response.");
		CookieSpec parser = CookiePolicy.getCompatibilitySpec();
		Header[] headers = getResponseHeaderGroup().getHeaders("set-cookie2");
        if (headers.length == 0) { 
            log.debug("New style cookies are not available. Going to check for old-style cookies.");
        	headers = getResponseHeaderGroup().getHeaders("set-cookie");
        }
        log.info("There are total " + (headers == null ? 0 : headers.length) + " cookies available in the response.");
        for (int i = 0; i < headers.length; i++) {
            Header header = headers[i];
            Cookie[] cookies = null;
            try {
                cookies = parser.parse(conn.getHost(), conn.getPort(), getPath(), conn.isSecure(), header);
            } 
            catch (MalformedCookieException e) {
            	log.warn("Invalid cookie header: \"" + header.getValue() + "\". " + e.getMessage());
            }
            if (cookies != null) {
                for (int j = 0; j < cookies.length; j++) {
                    Cookie cookie = cookies[j];
                    state.addCookie(cookie);
                    log.info("Cookie accepted: \"" + parser.formatCookie(cookie) + "\"");
                }
            }
        }
        log.debug("Done processing headers from the response.");
	}
}
