package works.hop.rest.tools.handler;

import java.io.IOException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.impl.client.CloseableHttpClient;

import works.hop.rest.tools.api.ApiReq;
import works.hop.rest.tools.api.ApiRes;

public class ApacheOptionsHandler extends AbstractApacheHandler<ApiRes> {

    @Override
    public ApiRes handle(ApiReq endpoint) throws Exception {
        CloseableHttpClient httpClient = buildClient();
        String queryStr = endpoint.getQueryParams().length > 0 ? "?" + endpoint.getQueryParams()[0]
                : endpoint.getQuery() != null ? "?" + endpoint.getQuery() : "";
        String url = endpoint.getUrl() + endpoint.getPath() + queryStr;
        HttpOptions httpOptions = new HttpOptions(url);
        httpOptions.setHeaders(createHeaders(endpoint));
        LOG.info("Executing request " + httpOptions.getRequestLine());
        try {
            CloseableHttpResponse response = httpClient.execute(httpOptions);
            //build api response
            ApiRes res = endpoint.getResponse();
            buildApiRes(res, response);
            return res;
        } catch (IOException th) {
            String error = retrieveStackTrace(th);
            endpoint.getResponse().setResponseBody(error);
            return endpoint.getResponse();
        }
    }
}
