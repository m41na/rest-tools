package works.hop.rest.tools.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore("fix up this test class")
public class RestConnectorTest {

    Logger log = LoggerFactory.getLogger(RestConnectorTest.class);

    String keystoreFile = "Drive:/PATH/TO/keystore.p12";

    private final String ENDPOINT = "";

    @Test
    public void testRunSingleEndpoint() {
        ApiResListener listener = new AssertionResListener();
        RestConnector client = new RestConnector(new StringJsonLoader(ENDPOINT), listener);
        client.run();
        assertNotNull("Expecting a response feedback", listener.getApiResponse());
    }

    public void processUpload(String url, String file) throws ClientProtocolException, IOException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httppost = new HttpPost(url);

            FileBody bin = new FileBody(new File(file));
            StringBody userId = new StringBody("spidy", ContentType.TEXT_PLAIN);
            StringBody fileSize = new StringBody("20", ContentType.TEXT_PLAIN);

            HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("bin", bin).addPart("userId", userId).addPart("fileSize", fileSize).build();

            httppost.setEntity(reqEntity);
            httppost.addHeader("iv-user", "spidy");

            System.out.println("executing request " + httppost.getRequestLine());
            try (CloseableHttpResponse response = httpclient.execute(httppost)) {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    System.out.println("Response content length: " + resEntity.getContentLength());
                }
                EntityUtils.consume(resEntity);
            }
        }
    }

    @Test
    public void testSwitchFromHttpToHttps() throws KeyStoreException, KeyManagementException, NoSuchAlgorithmException, ClientProtocolException, IOException {
        // plain http socket
        ConnectionSocketFactory http = PlainConnectionSocketFactory.getSocketFactory();

        // layered https socket
        SSLContext sslc = SSLContexts.custom().loadTrustMaterial((TrustStrategy) (X509Certificate[] chain, String authType) -> true).build();
        SSLConnectionSocketFactory https = new SSLConnectionSocketFactory(sslc, NoopHostnameVerifier.INSTANCE);

        // socket registry
        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", http)
                .register("https", https)
                .build();

        // client connection manager
        HttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();

        //fire request
        HttpGet getMethod = new HttpGet("https://www.google.com/");
        HttpResponse response = httpClient.execute(getMethod);

        //assert response
        assertEquals(response.getStatusLine().getStatusCode(), 200);
    }

    @Test
    public void testClientCustomSSL() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(new File(keystoreFile),
                        "passW0rd".toCharArray(),
                        new TrustSelfSignedStrategy())
                .build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[]{"TLSv1"},
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build()) {

            HttpGet httpget = new HttpGet("FIX_ME");

            log.info("Executing request " + httpget.getRequestLine());

            try (CloseableHttpResponse response = httpclient.execute(httpget)) {
                HttpEntity entity = response.getEntity();

                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                EntityUtils.consume(entity);
            }
        }
    }
}
