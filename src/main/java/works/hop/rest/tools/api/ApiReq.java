package works.hop.rest.tools.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import works.hop.rest.tools.util.SimpleJson;

public class ApiReq implements Serializable {

    private static final long serialVersionUID = 5742436875626069669L;
    private String id;
    private String name;
    private String method;
    private String descr;
    private String url;
    private String path;
    private String[] pathParams = {}; //new
    private String query;
    private String[] queryParams = {};//new
    private String consumes;
    private String produces;
    private Map<String, String> headers = new HashMap<>();
    private String entity;
    private String[] authorized = {}; //new
    private ApiRes response = new ApiRes();
    private List<ApiAssert<?>>  assertions = new LinkedList<>();
    //added these to work with Httpfied interface
    private Boolean execute;
    //added to hold available urls to environments which instances can use
    private Map<String, String> envs = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String[] getPathParams() {
        return pathParams;
    }

    public void setPathParams(String[] pathParams) {
        this.pathParams = pathParams;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String[] getQueryParams() {
        return this.queryParams;
    }

    public void setQueryParams(String[] queryParams) {
        this.queryParams = queryParams;
    }

    public String getConsumes() {
        return consumes;
    }

    public void setConsumes(String consumes) {
        this.consumes = consumes;
    }

    public String getProduces() {
        return produces;
    }

    public void setProduces(String produces) {
        this.produces = produces;
    }

    public String[] getAuthorized() {
        return authorized;
    }

    public void setAuthorized(String[] authorized) {
        this.authorized = authorized;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void addHeader(String header, String value) {
        headers.put(header, value);
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public ApiRes getResponse() {
        return response;
    }

    public void setResponse(ApiRes response) {
        this.response = response;
    }

    public List<ApiAssert<?>> getAssertions() {
        return assertions;
    }

    public void setAssertions(List<ApiAssert<?>> assertions) {
        this.assertions = assertions;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getExecute() {
        return execute;
    }

    public void setExecute(Boolean execute) {
        this.execute = execute;
    }

    public ApiReq copySelfTo(ApiReq guest) {
        //ensure first that id, method and path do match
        if (!this.name.equalsIgnoreCase(guest.getName())
                || !this.method.equalsIgnoreCase(guest.getMethod())
                || !this.path.equalsIgnoreCase(guest.path)) {
            throw new RuntimeException(String.format("Looks like these two endpoints \r\n %s \r\n AND \r\n %s \r\n do not quite match", this, guest));
        }
        //copy without remorse
        if (queryParams != null) {
            guest.setQueryParams(queryParams);
        }
        if (consumes != null) {
            guest.setConsumes(consumes);
        }
        if (descr != null) {
            guest.setDescr(descr);
        }
        if (headers != null) {
            guest.setHeaders(headers);
        }
        if (produces != null) {
            guest.setProduces(produces);
        }
        if (entity != null && entity.trim().length() > 0) {
            guest.setEntity(entity);
        }
        //return happy guest
        return guest;
    }

    public Map<String, String> getEnvs() {
        return envs;
    }

    public void setEnvs(Map<String, String> envs) {
        this.envs = envs;
    }

    @Override
    public String toString() {
        String output = SimpleJson.toJson(this);
        return output;
    }
}
