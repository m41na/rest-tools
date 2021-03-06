package com.practicaldime.rest.tools.handler;

import com.practicaldime.common.entity.rest.ApiReq;
import com.practicaldime.common.entity.rest.ApiRes;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

public class ApacheHeadHandler extends AbstractApacheHandler<ApiRes> {

    @Override
    public ApiRes handle(ApiReq endpoint) throws Exception {
        CloseableHttpClient httpClient = buildClient();
        String queryStr = endpoint.getQueryParams().length > 0 ? "?" + endpoint.getQueryParams()[0]
                : endpoint.getQuery() != null ? "?" + endpoint.getQuery() : "";
        String url = endpoint.getUrl() + endpoint.getPath() + queryStr;
        HttpHead httpHead = new HttpHead(url);
        httpHead.setHeaders(createHeaders(endpoint));
        LOG.info("Executing request " + httpHead.getRequestLine());
        try {
            CloseableHttpResponse response = httpClient.execute(httpHead);
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
