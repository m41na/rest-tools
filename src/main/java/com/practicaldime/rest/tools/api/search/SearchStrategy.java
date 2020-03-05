package com.practicaldime.rest.tools.api.search;

import com.practicaldime.common.entity.rest.ApiReq;

import java.util.List;

public interface SearchStrategy {

    void setNextStrategy(SearchStrategy strategy);

    ApiReq searchEndpoint(List<ApiReq> list, String path, String method);
}
