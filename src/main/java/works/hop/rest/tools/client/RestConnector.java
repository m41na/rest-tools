package works.hop.rest.tools.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import works.hop.rest.tools.api.search.ApiInfoSearch;
import works.hop.rest.tools.handler.ApacheDeleteHandler;
import works.hop.rest.tools.handler.ApacheGetHandler;
import works.hop.rest.tools.handler.ApacheHeadHandler;
import works.hop.rest.tools.handler.ApacheOptionsHandler;
import works.hop.rest.tools.handler.ApachePostHandler;
import works.hop.rest.tools.handler.ApachePutHandler;
import works.hop.rest.tools.model.ApiAssert;
import works.hop.rest.tools.model.ApiReq;
import works.hop.rest.tools.model.ApiRes;

public class RestConnector implements Runnable, RestConstants {

    private static final Logger LOG = LoggerFactory.getLogger(RestConnector.class);
    private JsonLoader jsonLoader;
    private ApiResListener responseListener;

    public RestConnector(JsonLoader jsonLoader) {
        super();
        this.jsonLoader = jsonLoader;
    }

    public RestConnector(JsonLoader jsonLoader, ApiResListener listener) {
        super();
        this.jsonLoader = jsonLoader;
        this.responseListener = listener;
    }

    public JsonLoader getJsonLoader() {
        return jsonLoader;
    }

    public void setJsonLoader(JsonLoader jsonLoader) {
        this.jsonLoader = jsonLoader;
    }

    public ApiResListener getResponseListener() {
        return responseListener;
    }

    public void setResponseListener(ApiResListener responseListener) {
        this.responseListener = responseListener;
    }

    public void notifyResponse(ApiRes response, List<ApiAssert> assertions) {
        if (getResponseListener() != null) {
            getResponseListener().onReadyResponse(response, assertions);
        }
    }

    public JsonNode loadJson() {
        return getJsonLoader().loadJson();
    }
    
    public static ApiReq mergeEndpoint(ApiReq base, ApiReq endpoint){
        return mergeEndpoints(base, Arrays.asList(endpoint)).get(0);
    }

    public static List<ApiReq> mergeEndpoints(ApiReq base, List<ApiReq> endpoints) {
        endpoints.stream().forEach((rep) -> {
            // override url if a key is provided instead of a valid url
            String urlValue = rep.getUrl();
            if (isNotEmpty(urlValue)) {
                if (!urlValue.matches("^http.*")) {
                    String resolvedUrl = rep.getEnvs().get(urlValue);
                    if (resolvedUrl != null && resolvedUrl.matches("^http.*")) {
                        rep.setUrl(resolvedUrl);
                    } else {
                        rep.setUrl(base.getUrl());
                    }
                }
            } else {
                rep.setUrl(base.getUrl());
            }
            //check 'execute'
            if (rep.getExecute() == null) {
                rep.setExecute(Boolean.TRUE);
            }
            //check 'method'
            if (isEmpty(rep.getMethod())) {
                rep.setMethod(base.getMethod());
            }
            //check 'path'
            if (isEmpty(rep.getPath())) {
                rep.setPath(base.getPath());
            }
            //check 'request body'
            if (isEmpty(rep.getEntity())) {
                rep.setEntity(base.getEntity());
            }
            //check 'response body'
            if (rep.getResponse().getResponseBody() == null) {
                rep.getResponse().setResponseBody(base.getResponse().getResponseBody());
            }
            //check 'response status code'
            if (rep.getResponse().getStatusCode() == null) {
                rep.getResponse().setStatusCode(base.getResponse().getStatusCode());
            }
            //check 'consumes'
            if (isEmpty(rep.getConsumes())) {
                rep.setConsumes(base.getConsumes());
            }
            //check 'produces'
            if(isEmpty(rep.getProduces())){
                rep.setProduces(base.getProduces());
            }
        });
        return endpoints;
    }

    public static List<ApiReq> mergeEndpoints(List<ApiReq> nodes) {
        LOG.info("merging endpoints with template (should always be the first one)");
        // 1. Extract base/template node
        ApiReq templateNode = nodes.get(0);
        // 2. Extract other nodes
        List<ApiReq> otherNodes = nodes.subList(1, nodes.size());
        // 3. Merge missing values with those in base
        return RestConnector.mergeEndpoints(templateNode, otherNodes);
    }

    public static ApiReq searchEndpoint(List<ApiReq> endpoints, String path, String method) {
        ApiInfoSearch search = new ApiInfoSearch();
        return search.searchEndpoint(endpoints, path, method);
    }

    public static boolean isEmpty(String value) {
        return (value == null || value.trim().length() == 0);
    }

    public static boolean isNotEmpty(String value) {
        return (value != null && value.trim().length() > 0);
    }

    public static boolean isJSONValid(String jsonInString) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(jsonInString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public List<ApiReq> prepareEndpoints() {
        LOG.info("preparing json endpoints");
        List<ApiReq> nodes = loadEndpoints();
        if (nodes.size() > 1) {
            return mergeEndpoints(nodes);
        } else {
            return nodes;
        }
    }

    public List<ApiReq> loadEndpoints() {
        LOG.info("loading json");
        return getJsonLoader().readValue(new TypeReference<List<ApiReq>>() {
        });
    }

    public void executeEndpoint(ApiReq endpoint) {
        try {
            String method = endpoint.getMethod();
            if (Objects.equals(endpoint.getExecute(), Boolean.TRUE)) {
                if (method.equalsIgnoreCase(GET)) {
                    ApiRes response = new ApacheGetHandler().handle(endpoint);
                    notifyResponse(response, endpoint.getAssertions());
                    System.out.println(endpoint);
                } else if (method.equalsIgnoreCase(POST)) {
                    ApiRes response = new ApachePostHandler().handle(endpoint);
                    notifyResponse(response, endpoint.getAssertions());
                    System.out.println(endpoint);
                } else if (method.equalsIgnoreCase(PUT)) {
                    ApiRes response = new ApachePutHandler().handle(endpoint);
                    notifyResponse(response, endpoint.getAssertions());
                    System.out.println(endpoint);
                } else if (method.equalsIgnoreCase(DELETE)) {
                    ApiRes response = new ApacheDeleteHandler().handle(endpoint);
                    notifyResponse(response, endpoint.getAssertions());
                    System.out.println(endpoint);
                } else if (method.equalsIgnoreCase(HEAD)) {
                    ApiRes response = new ApacheHeadHandler().handle(endpoint);
                    notifyResponse(response, endpoint.getAssertions());
                    System.out.println(endpoint);
                } else if (method.equalsIgnoreCase(OPTIONS)) {
                    ApiRes response = new ApacheOptionsHandler().handle(endpoint);
                    notifyResponse(response, endpoint.getAssertions());
                    System.out.println(endpoint);
                } else {
                    String message = String.format("Handler for '%s' method has not yet been implemented", method);
                    throw new UnsupportedOperationException(message);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        // 1. Fetch endpoints
        List<ApiReq> mergedNodes = prepareEndpoints();
        // 2. fire request to each endpoint
        mergedNodes.stream().forEach((endpoint) -> {
            executeEndpoint(endpoint);
        });
    }

    public static void main(String... args) {
        String ENDPOINTS_FILE = "/rest/target-endpoints.json";
        RestConnector client = new RestConnector(new ClassPathJsonLoader(ENDPOINTS_FILE));
        client.run();
    }
}
