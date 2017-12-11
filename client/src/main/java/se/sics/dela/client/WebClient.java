package se.sics.dela.client;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class WebClient {
  public static WebTarget getClient(String hopssiteVersion, String hopssiteTarget) {

    TrustManager[] trustAllCerts = new TrustManager[]{
      new X509TrustManager() {
        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
      }
    };
    
    HostnameVerifier anyHost = new HostnameVerifier() {
      @Override
      public boolean verify(String string, SSLSession ssls) {
        return true;
      }
    };

    try {
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      if (hopssiteVersion.equals("bbc5")) {
        return ClientBuilder.newBuilder().sslContext(sc).hostnameVerifier(anyHost).build().target(hopssiteTarget);
      } else {
        throw new RuntimeException("unknown hopssite version:" + hopssiteVersion);
      }
    } catch (NoSuchAlgorithmException | KeyManagementException ex) {
      throw new RuntimeException(ex);
    }
  }
}
