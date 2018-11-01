package works.hop.rest.tools.api;

import java.lang.annotation.Annotation;

import works.hop.rest.tools.model.ApiReq;

public interface ApiAnnotationHandler {

    void handle(Annotation annotation, ApiReq endpoint);
}
