package com.practicaldime.rest.tools.api;

import java.lang.annotation.Annotation;

import com.practicaldime.rest.tools.api.ApiReq;

public interface ApiAnnotationHandler {

    void handle(Annotation annotation, ApiReq endpoint);
}
