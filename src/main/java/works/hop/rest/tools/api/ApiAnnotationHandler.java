package works.hop.rest.tools.api;

import java.lang.annotation.Annotation;

public interface ApiAnnotationHandler {

    void handle(Annotation annotation, ApiReq endpoint);
}
