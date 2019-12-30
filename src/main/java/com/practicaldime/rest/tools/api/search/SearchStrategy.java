package com.practicaldime.rest.tools.api.search;

import java.util.List;

import com.practicaldime.rest.tools.api.ApiReq;

public interface SearchStrategy {

    void setNextStrategy(SearchStrategy strategy);

    ApiReq searchEndpoint(List<ApiReq> list, String path, String method);
}
