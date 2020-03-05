package com.practicaldime.rest.tools.api;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;

import com.practicaldime.common.entity.rest.ApiReq;

public class ApiReqBuilder {

    protected Map<String, ApiAnnotationHandler> handlers = new HashMap<>();

    protected ApiAnnotationHandler method = (Annotation annotation, ApiReq endpoint) -> {
        Class<?> annoType = annotation.annotationType();
        if (annoType.equals(GET.class) || annoType.equals(POST.class) || annoType.equals(PUT.class) || annoType.equals(DELETE.class)) {
            endpoint.setMethod(annotation.annotationType().getSimpleName().toLowerCase());
        }
    };

    protected ApiAnnotationHandler produces = (Annotation annotation, ApiReq endpoint) -> {
        Class<?> annoType = annotation.annotationType();
        if (annoType.equals(Produces.class)) {
            String[] values = ((Produces) annotation).value();
            endpoint.setProduces(String.join(";", values));
        }
    };

    protected ApiAnnotationHandler consumes = (Annotation annotation, ApiReq endpoint) -> {
        Class<?> annoType = annotation.annotationType();
        if (annoType.equals(Consumes.class)) {
            if (endpoint.getConsumes() == null) {
                String[] values = ((Consumes) annotation).value();
                endpoint.setConsumes(String.join(";", values));
            }
        }
    };

    public ApiReqBuilder() {
        super();
        init();
    }

    public final void init() {
        handlers.put("METHOD", method);
        handlers.put("PRODUCES", produces);
        handlers.put("CONSUMES", consumes);
    }

    public void register(String key, ApiAnnotationHandler handler) {
        handlers.put(key, handler);
    }

    public void build(Annotation annotation, ApiReq endpoint) {
        handlers.values().forEach((handler) -> {
            handler.handle(annotation, endpoint);
        });
    }
}
