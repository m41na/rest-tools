package works.hop.rest.tools.impl;

import java.util.Map;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class EndpointRequest {

    private final Invocation.Builder resource;
    private Entity<?> entity;

    public EndpointRequest(Invocation.Builder resource) {
        super();
        this.resource = resource;
    }

    public Response execute(String value) {
        if (value.equalsIgnoreCase("GET")) {
            return resource.get();
        }
        if (value.equalsIgnoreCase("POST")) {
            return resource.post(entity);
        }
        if (value.equalsIgnoreCase("PUT")) {
            return resource.put(entity);
        }
        if (value.equalsIgnoreCase("DELETE")) {
            return resource.put(entity);
        }
        throw new RuntimeException("unknown method");
    }

    public EndpointRequest accept(String[] values) {
        if (values != null && values.length > 0) {
            resource.accept(values);
        }
        return this;
    }

    public EndpointRequest headers(Map<String, String[]> values) {
        values.keySet().forEach((header) -> {
            String[] value = values.get(header);
            if (value.length > 0 && value[0].trim().length() > 0) {
                resource.header(header, value);
            }
        });
        return this;
    }

    public EndpointRequest headers2(Map<String, String> values) {
        values.keySet().forEach((header) -> {
            resource.header(header, values.get(header));
        });
        return this;
    }

    public EndpointRequest requestEntity(Object entity) {
        this.entity = Entity.entity(entity, MediaType.APPLICATION_JSON);
        return this;
    }

    public EndpointRequest contentType(String[] values) {
        if (values != null && values.length > 0) {
            MediaType type = MediaType.valueOf(values[0]);
            if (type != null) {
                resource.header("content-type", type.toString());
            }
        }
        return this;
    }
}
