package io.quarkus.activity.providers;

import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.apptastic.rssreader.RssReader;

public class RssReaderProvider {

    @Singleton
    @Produces
    RssReader rssReader() {
        return new RssReader(createHttpClient());
    }

    private HttpClient createHttpClient() {
        HttpClient client;
        SSLContext context = insecureContext();
        client = HttpClient.newBuilder().sslContext(context).connectTimeout(Duration.ofSeconds(25L)).followRedirects(HttpClient.Redirect.NORMAL).build();
        return client;
    }

    private static SSLContext insecureContext() {
        TrustManager[] noopTrustManager = new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] xcs, String string) {}
                    public void checkServerTrusted(X509Certificate[] xcs, String string) {}
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
        };
        try {
            SSLContext sc = SSLContext.getInstance("ssl");
            sc.init(null, noopTrustManager, null);
            return sc;
        } catch (KeyManagementException | NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
}
