package com.practicaldime.rest.tools.api;

import com.practicaldime.common.entity.rest.ApiReq;

import java.lang.annotation.Annotation;

public interface ApiHandler {

    void handle(Annotation annotation, ApiReq endpoint);
}
