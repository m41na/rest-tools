package com.practicaldime.rest.tools.api;

import java.lang.annotation.Annotation;

import com.practicaldime.common.entity.rest.ApiReq;

public interface ApiAnnotationHandler {

    void handle(Annotation annotation, ApiReq endpoint);
}
