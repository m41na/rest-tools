package com.practicaldime.rest.tools.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practicaldime.common.entity.rest.ApiReq;
import com.practicaldime.common.entity.rest.ApiRes;
import com.practicaldime.rest.tools.api.RequestHandler;
import com.practicaldime.rest.tools.client.FileDataReader;
import com.practicaldime.rest.tools.client.FileDataReader.ByteArrayCallback;
import com.practicaldime.rest.tools.client.FileDataReader.StringCallback;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.*;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractApacheHandler<T> implements RequestHandler<T> {

    protected static final Logger LOG = LoggerFactory.getLogger(AbstractApacheHandler.class);

    public static SSLContext createSSLContext() {
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            // InputStream stream = new
            // FileInputStream("E:/IBM/WebSphere/AppServer/bin/keystore.p12");
            ks.load(null, null);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);

            final SSLContext ctx = SSLContext.getInstance("TLSv1.2");
            ctx.init(null, tmf.getTrustManagers(), null);

            return ctx;
        } catch (IOException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException("Unable to create SSLContext", e);
        }
    }

    public static CloseableHttpClient buildClientAlt() {
        // plain http socket
        ConnectionSocketFactory http = PlainConnectionSocketFactory.getSocketFactory();

        // layered https socket
        SSLContext ctx = createSSLContext();
        final SSLConnectionSocketFactory https = new SSLConnectionSocketFactory(ctx, new NoopHostnameVerifier());

        // socket registry
        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create().register("http", http)
                .register("https", https).build();

        // client connection manager
        HttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);

        // configure client builder
        final HttpClientBuilder builder = HttpClientBuilder.create();
        RequestConfig config = RequestConfig.custom().setConnectTimeout(120 * 1000)
                .setConnectionRequestTimeout(120 * 1000).setSocketTimeout(120 * 1000).build();
        builder.setDefaultRequestConfig(config);
        return builder.setConnectionManager(cm).build();
    }

    public static CloseableHttpClient buildClient() {
        try {
            // plain http socket
            ConnectionSocketFactory http = PlainConnectionSocketFactory.getSocketFactory();

            // layered https socket
            SSLContext sslc = SSLContexts.custom().loadTrustMaterial((TrustStrategy) (X509Certificate[] chain, String authType) -> true).build();
            SSLConnectionSocketFactory https = new SSLConnectionSocketFactory(sslc, NoopHostnameVerifier.INSTANCE);

            // socket registry
            Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", http).register("https", https).build();

            // client connection manager
            HttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);

            // configure client builder
            final HttpClientBuilder builder = HttpClientBuilder.create();
            RequestConfig config = RequestConfig.custom().setConnectTimeout(300 * 1000)
                    .setConnectionRequestTimeout(300 * 1000).setSocketTimeout(300 * 1000).build();
            builder.setDefaultRequestConfig(config);
            return builder.setConnectionManager(cm).build();
        } catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException("Unable to create http client", e);
        }
    }

    public Header[] createHeaders(ApiReq rep) {
        List<Header> headersList = new ArrayList<>();

        // extract header values
        Map<String, String> headers = rep.getHeaders();
        if (headers != null && headers.size() > 0) {
            headers.keySet().forEach((key) -> {
                String value = headers.get(key);
                //should multiple values in one header have individual BasicHeader objects created?
                headersList.add(new BasicHeader(key.toUpperCase(), value));
            });
        }

        // extract content-type
        String consumes = rep.getConsumes();
        if (isNotEmpty(consumes) && (hasValidMediaType(consumes))) {
            headersList.add(new BasicHeader("content-type", consumes));
        }

        // extract accept
        String produces = rep.getProduces();
        if (isNotEmpty(produces) && hasValidMediaType(produces)) {
            headersList.add(new BasicHeader("accept", produces));
        }
        return headersList.toArray(new Header[headersList.size()]);
    }

    protected boolean isNotEmpty(String value) {
        return (value != null && value.trim().length() > 0);
    }

    protected boolean hasValidMediaType(String value) {
        String[] types = value.split(",");
        for (String type : types) {
            if (!type.contains("/")) {
                return false;
            }
        }
        return true;
    }

    protected boolean isJSONValid(String jsonInString) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(jsonInString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    protected void handleMultipartContentType(HttpEntityEnclosingRequest req) {
        if (req.getEntity() != null) {
            String contentType = req.getEntity().getContentType().getValue();
            if (contentType.contains("boundary=")) {
                req.setHeader("content-type", contentType);
            }
        }
    }

    protected HttpEntity extractEntity(ApiReq req) throws UnsupportedEncodingException {
        if (req.getConsumes().contains("application/json") || req.hasHeaderValue("content-type", "application/json")) {
            String entity = stripStartEndCommas(req.getEntity().replace("'", "\""));
            return new StringEntity(entity, ContentType.APPLICATION_JSON);
        }
        if (req.getConsumes().contains("multipart/form-data") || req.hasHeaderValue("content-type", "multipart/form-data")) {
            // start creating multipart entity
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setContentType(ContentType.MULTIPART_FORM_DATA);
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            // break up entity string and add to builder
            String formParamsString = stripStartEndCommas(req.getEntity());
            List<NameValuePair> formParams = extractFormParameters(formParamsString);
            formParams.forEach((param) -> {
                if (param.getName().equals("file")) {
                    File file = new File(param.getValue());
                    FileBody fileBody = new FileBody(file);
                    builder.addPart(param.getName(), fileBody);
                } else {
                    StringBody paramValue = new StringBody(param.getValue(), ContentType.TEXT_PLAIN);
                    builder.addPart(param.getName(), paramValue);
                }
            });

            HttpEntity entity = builder.build();
            return entity;
        }
        if (req.getConsumes().contains("multipart/form-data --> this is an alternative approach")) {
            String fileName = stripStartEndCommas(req.getEntity());
            File file = new File(fileName);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());
            builder.addTextBody("text", "awesome!", ContentType.DEFAULT_BINARY);
            return builder.build();
        }
        if (req.getConsumes().contains("application/x-www-form-urlencoded") || req.hasHeaderValue("content-type", "application/x-www-form-urlencoded")) {
            List<NameValuePair> nvps = new ArrayList<>();
            String entity = stripStartEndCommas(req.getEntity());
            String[] pairs = entity.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                nvps.add(new BasicNameValuePair(keyValue[0], keyValue[1]));
            }
            return new UrlEncodedFormEntity(nvps);
        }
        return null;
    }

    protected List<NameValuePair> extractFormParameters(String input) {
        // strip brackets
        if (input.matches("^\\[.*?\\]$")) {
            input = input.substring(input.indexOf("[") + 1, input.lastIndexOf("]"));
        }
        String[] keyValues = input.split(",");
        List<NameValuePair> params = new ArrayList<>();
        for (String pair : keyValues) {
            String[] split = stripStartEndCommas(pair).split("=");
            if (split.length == 2) {
                params.add(new BasicNameValuePair(split[0], split[1]));
            } else {
                params.add(new BasicNameValuePair(split[0], ""));
            }
        }
        return params;
    }

    protected String stripStartEndCommas(String input) {
        if (input.matches("^\".*?")) {
            input = input.substring(1);
        }
        if (input.matches(".*?\"$")) {
            input = input.substring(0, input.length() - 1);
        }
        return input;
    }

    protected String retrieveStackTrace(Throwable th) {
        StringWriter sw = new StringWriter();
        th.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    protected byte[] readResponseStream(InputStream response) throws IOException {
        return FileDataReader.readBytes(response);
    }

    protected String readResponseStreamAsString(InputStream response) throws IOException {
        return FileDataReader.readBytes(response, new StringCallback());
    }

    protected ByteArrayOutputStream readResponseStreamAsByteArray(InputStream response) throws IOException {
        return FileDataReader.readBytes(response, new ByteArrayCallback());
    }

    /**
     * Shows a style usage that employs older api for httpclient. Currently not
     * in use in favor of the newer api
     *
     * @param url
     * @return
     */
    protected String processRequestWithHttpClient(String url) {
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(url);
        DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(10, true);
        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler);

        try {
            int code = client.executeMethod(method);
            if (code != HttpStatus.SC_OK) {
                LOG.error("Method failed: " + method.getStatusLine());
            }

            String responseBody = readResponseStreamAsString(method.getResponseBodyAsStream());
            System.out.println(responseBody);
            return responseBody;
        } catch (IOException e) {
            LOG.error(String.format("There was an error making this request -> %s", e.getMessage()));
            throw new RuntimeException(e);
        } finally {
            method.releaseConnection();
        }
    }

    protected ResponseHandler<byte[]> readResponseHandler() {
        return (HttpResponse response) -> {
            HttpEntity entity = response.getEntity();
            byte[] responseBody = readResponseStream(entity.getContent());
            return responseBody;
        };
    }

    protected ResponseHandler<String> readResponseAsStringHandler() {
        return (HttpResponse response) -> {
            HttpEntity entity = response.getEntity();
            String responseBody = readResponseStreamAsString(entity.getContent());
            return responseBody;
        };
    }

    protected void buildApiRes(ApiRes res, HttpResponse response) throws IOException {
        res.setStatusCode(response.getStatusLine().getStatusCode());
        res.setDescription(response.getStatusLine().getReasonPhrase());
        res.setProtocol(response.getStatusLine().getProtocolVersion().getProtocol());
        for (Header header : response.getAllHeaders()) {
            res.getHeaders().put(header.getName(), header.getValue());
        }
        String responseBody = readResponseAsStringHandler().handleResponse(response);
        res.setResponseBody(responseBody);
    }
}
