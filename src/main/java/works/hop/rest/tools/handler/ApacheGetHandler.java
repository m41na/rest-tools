package works.hop.rest.tools.handler;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import works.hop.rest.tools.model.ApiReq;
import works.hop.rest.tools.model.ApiRes;

public class ApacheGetHandler extends AbstractApacheHandler<ApiRes> {

    @Override
    public ApiRes handle(ApiReq endpoint) throws Exception {
        CloseableHttpClient httpClient = buildClient();
        String queryStr = endpoint.getQueryParams().length > 0 ? "?" + endpoint.getQueryParams()[0]
                : endpoint.getQuery() != null ? "?" + endpoint.getQuery() : "";
        String url = endpoint.getUrl() + endpoint.getPath() + queryStr;
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeaders(createHeaders(endpoint));
        LOG.info("Executing request " + httpGet.getRequestLine());
        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
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
