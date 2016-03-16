package salesmachine.util;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class OAuthProvider {
  private String host;
  private String path;
  private String consumerKey;
  private String consumerSecret;

  public OAuthProvider(String host, String path, String consumerKey, String consumerSecret) {
    this.host = host;
    this.path = path;
    this.consumerKey = consumerKey;
    this.consumerSecret = consumerSecret;
  }

  public String generateOauthHeader(String method, List<String> additionalParameters) {
    long timestamp = new Date().getTime() / 1000;
    String nonce = Long.toString(timestamp);
    ArrayList<String> parameters = new ArrayList<String>();
    parameters.add("oauth_consumer_key=" + consumerKey);
    parameters.add("oauth_nonce=" + nonce);
    parameters.add("oauth_signature_method=HMAC-SHA1");
    parameters.add("oauth_timestamp=" + timestamp);
    parameters.add("oauth_version=1.0");

    for (String additionalParameter : additionalParameters) {
      parameters.add(additionalParameter);
    }

    Collections.sort(parameters);

    StringBuffer parametersList = new StringBuffer();

    for (int i = 0; i < parameters.size(); i++) {
      parametersList.append(((i > 0) ? "&" : "") + parameters.get(i));
    }

    String signatureString = method + "&" + URLEncoder.encode("https://" + host + path) + "&"
        + URLEncoder.encode(parametersList.toString());

    String signature = null;

    try {
      SecretKeySpec signingKey = new SecretKeySpec((consumerSecret + "&").getBytes(), "HmacSHA1");
      Mac mac = Mac.getInstance("HmacSHA1");
      mac.init(signingKey);
      byte[] rawHMAC = mac.doFinal(signatureString.getBytes());
      signature = Base64.encodeBytes(rawHMAC);
    } catch (Exception e) {
      System.err.println("Unable to append signature");
    }

    System.out.println("signature is - "+signature);

    String authorizationLine = "OAuth " + "oauth_consumer_key=\"" + consumerKey
        + "\", " + "oauth_nonce=\"" + nonce + "\", " + "oauth_timestamp=\"" + timestamp + "\", "
        + "oauth_signature_method=\"HMAC-SHA1\", " + "oauth_signature=\""
        + URLEncoder.encode(signature) + "\", " + "oauth_version=\"1.0\"";

    return authorizationLine;
  }
}