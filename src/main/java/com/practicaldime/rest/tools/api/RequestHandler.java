package com.practicaldime.rest.tools.api;

import com.practicaldime.rest.tools.model.ApiReq;

public interface RequestHandler<T> {

    T handle(ApiReq endpoint) throws Exception;
}
