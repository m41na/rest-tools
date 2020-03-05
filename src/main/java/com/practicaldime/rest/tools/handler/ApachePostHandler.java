package com.practicaldime.rest.tools.handler;

import com.practicaldime.common.entity.rest.ApiReq;
import com.practicaldime.common.entity.rest.ApiRes;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

public class ApachePostHandler extends AbstractApacheHandler<ApiRes> {

    @Override
    public ApiRes handle(ApiReq endpoint) throws Exception {
        CloseableHttpClient httpClient = buildClient();
        String queryStr = endpoint.getQueryParams().length > 0 ? "?" + endpoint.getQueryParams()[0]
                : endpoint.getQuery() != null ? "?" + endpoint.getQuery() : "";
        String url = endpoint.getUrl() + endpoint.getPath() + queryStr;
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeaders(createHeaders(endpoint));
        httpPost.setEntity(extractEntity(endpoint));
        handleMultipartContentType(httpPost);
        LOG.info("Executing request " + httpPost);
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
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
