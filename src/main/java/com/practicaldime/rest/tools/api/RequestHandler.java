package com.practicaldime.rest.tools.api;

import com.practicaldime.common.entity.rest.ApiReq;

public interface RequestHandler<T> {

    T handle(ApiReq endpoint) throws Exception;
}
