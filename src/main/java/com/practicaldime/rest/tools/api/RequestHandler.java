package com.practicaldime.rest.tools.api;

import com.practicaldime.rest.tools.api.ApiReq;

public interface RequestHandler<T> {

    T handle(ApiReq endpoint) throws Exception;
}
