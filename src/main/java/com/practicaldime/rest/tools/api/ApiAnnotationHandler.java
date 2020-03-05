package com.practicaldime.rest.tools.api;

import com.practicaldime.common.entity.rest.ApiReq;

import java.lang.annotation.Annotation;

public interface ApiAnnotationHandler {

    void handle(Annotation annotation, ApiReq endpoint);
}
