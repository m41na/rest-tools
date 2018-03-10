package works.hop.rest.tools.api;

import java.lang.annotation.Annotation;

public interface ApiHandler {

    void handle(Annotation annotation, ApiReq endpoint);
}
