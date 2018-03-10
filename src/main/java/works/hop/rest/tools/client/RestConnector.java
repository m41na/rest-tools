package works.hop.rest.tools.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import works.hop.rest.tools.api.ApiAssert;
import works.hop.rest.tools.api.ApiAssert.AssertType;

import works.hop.rest.tools.api.ApiReq;
import works.hop.rest.tools.api.ApiRes;
import works.hop.rest.tools.api.search.ApiInfoSearch;
import works.hop.rest.tools.handler.ApacheDeleteHandler;
import works.hop.rest.tools.handler.ApacheGetHandler;
import works.hop.rest.tools.handler.ApacheHeadHandler;
import works.hop.rest.tools.handler.ApacheOptionsHandler;
import works.hop.rest.tools.handler.ApachePostHandler;
import works.hop.rest.tools.handler.ApachePutHandler;

public class RestConnector implements Runnable {

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

    public void notifyResponse(ApiRes response, List<ApiAssert<?>> assertions) {
        if (getResponseListener() != null) {
            getResponseListener().onReadyResponse(response, assertions);
        }
    }

    public JsonNode loadJson() {
        return getJsonLoader().loadJson();
    }

    public static ApiReq extractEndpoint(JsonNode node) {
        ApiReq rep = new ApiReq();
        JsonNode id = node.get("id");
        if (id != null) {
            rep.setId(id.asText());
        }

        JsonNode name = node.get("name");
        if (name != null) {
            rep.setName(name.asText());
        }

        JsonNode descr = node.get("descr");
        if (descr != null) {
            rep.setDescription(descr.asText());
        }

        JsonNode method = node.get("method");
        if (method != null) {
            rep.setMethod(method.asText());
        }

        JsonNode path = node.get("path");
        if (path != null) {
            rep.setPath(path.asText());
        }

        JsonNode query = node.get("query");
        if (query != null) {
            rep.setQuery(query.asText());
        }

        JsonNode cNode = node.get("consumes");
        if (cNode != null) {
            rep.setConsumes(cNode.asText());
        }

        JsonNode pNode = node.get("produces");
        if (pNode != null) {
            rep.setProduces(pNode.asText());
        }

        JsonNode hNode = node.get("headers");
        if (hNode != null) {
            Map<String, String[]> headers = new HashMap<>();
            for (int i = 0; i < hNode.size(); i++) {
                String keyValue;
                keyValue = hNode.get(i).asText();
                //some header values may have '=' in them, so use first index of '=' to separate key/value
                int index = keyValue.indexOf(":");
                if (index < 0) {
                    index = keyValue.indexOf("=");
                }
                //String[] splitKeyValue = keyValue.split("[=:]");
                String[] splitKeyValue = new String[]{keyValue.substring(0, index), keyValue.substring(index + 1)};
                String key = splitKeyValue[0];
                if (headers.keySet().contains(key)) {
                    String[] existingValue = headers.get(key);
                    String[] newValue = new String[existingValue.length + 1];
                    System.arraycopy(existingValue, 0, newValue, 0, existingValue.length);
                    newValue[newValue.length - 1] = splitKeyValue[1];
                    headers.put(key, newValue);
                } else {
                    String[] newValue = (splitKeyValue.length > 1) ? new String[]{splitKeyValue[1]}
                            : new String[]{};
                    headers.put(key, newValue);
                }
            }
            rep.setHeaders(headers);
        }

        JsonNode eNode = node.get("envs");
        if (eNode != null) {
            Map<String, String> entries = new HashMap<>();
            for (int i = 0; i < eNode.size(); i++) {
                String keyValue;
                keyValue = eNode.get(i).asText();
                String[] splitKeyValue = keyValue.split("=");
                String key = splitKeyValue[0];
                if (entries.keySet().contains(key)) {
                    String existingValue = entries.get(key);
                    String newValue = splitKeyValue[1];
                    LOG.warn("overwriting value of '{}' from '{}' to '{}'", key, existingValue, newValue);
                    entries.put(key, newValue);
                } else {
                    String newValue = splitKeyValue[1];
                    entries.put(key, newValue);
                }
            }
            ApiReq.setEnvs(entries);
        }

        JsonNode entity = node.get("entity");
        if (entity != null) {
            rep.setRequestBody(entity.toString());
        }

        JsonNode assertsNode = node.get("assertions");
        if (assertsNode != null) {
            List<ApiAssert<?>> assertions = new LinkedList<>();
            for(int i = 0; i < assertsNode.size(); i++){
                JsonNode listNode = assertsNode.get(i);
                ApiAssert assertion = new ApiAssert();
                assertion.actualValue = listNode.get("actualValue").asText();
                assertion.expectedValue = listNode.get("expectedValue").asText();
                assertion.assertType = AssertType.valueOf(listNode.get("assertType").asText());
                assertion.failMessage = listNode.get("failMessage").asText();
                assertions.add(assertion);
            }
            rep.setAssertions(assertions);
        }

        JsonNode url = node.get("url");
        if (url != null) {
            rep.setBaseUrl(url.asText());
        }

        JsonNode execute = node.get("execute");
        if (execute != null) {
            rep.setExecute(execute.asBoolean(Boolean.FALSE));
        }
        return rep;
    }

    public static ApiReq extractEndpoint(JsonNode node, int index) {
        JsonNode target = node.get(index);
        return extractEndpoint(target);
    }

    public static List<ApiReq> extractEndpoints(JsonNode node) {
        return extractEndpoints(node, 0);
    }

    public static List<ApiReq> extractEndpoints(JsonNode node, int startIndex) {
        List<ApiReq> endpoints = new LinkedList<>();
        for (int i = startIndex; i < node.size(); i++) {
            ApiReq rep = extractEndpoint(node.get(i));
            endpoints.add(rep);
        }
        return endpoints;
    }

    public static List<ApiReq> mergeEndpoints(ApiReq base, List<ApiReq> endpoints) {
        endpoints.stream().map((rep) -> {
            // override url if a key is provided instead of a valid url
            String urlValue = rep.getBaseUrl();
            if (isNotEmpty(urlValue)) {
                if (!urlValue.matches("^http.*")) {
                    String resolvedUrl = ApiReq.getEnvs().get(urlValue);
                    if (resolvedUrl != null && resolvedUrl.matches("^http.*")) {
                        rep.setBaseUrl(resolvedUrl);
                    } else {
                        rep.setBaseUrl(base.getBaseUrl());
                    }
                }
            } else {
                rep.setBaseUrl(base.getBaseUrl());
            }
            return rep;
        }).map((rep) -> {
            if (isEmpty(rep.getMethod())) {
                rep.setMethod(base.getMethod());
            }
            return rep;
        }).map((rep) -> {
            if (isEmpty(rep.getPath())) {
                rep.setPath(base.getPath());
            }
            return rep;
        }).map((rep) -> {
            if (isEmpty(rep.getRequestBody())) {
                rep.setRequestBody(base.getRequestBody());
            }
            return rep;
        }).map((rep) -> {
            if (rep.getResponse().getResponseBody() == null) {
                rep.getResponse().setResponseBody(base.getResponse().getResponseBody());
            }
            return rep;
        }).map((ApiReq rep) -> {
            if (rep.getResponse().getStatusCode() == null) {
                rep.getResponse().setStatusCode(base.getResponse().getStatusCode());
            }
            return rep;
        }).map((rep) -> {
            if (isEmpty(rep.getConsumes())) {
                rep.setConsumes(base.getConsumes());
            }
            return rep;
        }).filter((rep) -> (isEmpty(rep.getProduces()))).forEachOrdered((rep) -> {
            rep.setProduces(base.getProduces());
        });
        return endpoints;
    }

    public static List<ApiReq> extractAndMergeEndpoints(JsonNode endpointNodes) {
        // 1. Extract base/template node
        ApiReq defaultNode = extractEndpoint(endpointNodes, 0);
        // 2. Extract other nodes
        List<ApiReq> otherNodes = extractEndpoints(endpointNodes, 1);
        // 3. Merge missing values with those in base
        return mergeEndpoints(defaultNode, otherNodes);
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
        // 1. Read json endpoints
        JsonNode endpointNodes = loadJson();
        if (endpointNodes.isArray()) {
            // 2 extract and merge all endpoints
            return extractAndMergeEndpoints(endpointNodes);
        } else {
            // 2 Extract and return as a list of 1
            ApiReq singleNode = extractEndpoint(endpointNodes);
            return Arrays.asList(singleNode);
        }
    }

    @Override
    public void run() {
        try {
            // 1. Fetch endpoints
            List<ApiReq> mergedNodes = prepareEndpoints();

            // 2. fire request to each endpoint
            for (ApiReq endpoint : mergedNodes) {
                String method = endpoint.getMethod();
                if (Objects.equals(endpoint.getExecute(), Boolean.TRUE)) {
                    if (method.equalsIgnoreCase("GET")) {
                        ApiRes response = new ApacheGetHandler().handle(endpoint);
                        notifyResponse(response, endpoint.getAssertions());
                        System.out.println(endpoint);
                    } else if (method.equalsIgnoreCase("POST")) {
                        ApiRes response = new ApachePostHandler().handle(endpoint);
                        notifyResponse(response, endpoint.getAssertions());
                        System.out.println(endpoint);
                    } else if (method.equalsIgnoreCase("PUT")) {
                        ApiRes response = new ApachePutHandler().handle(endpoint);
                        notifyResponse(response, endpoint.getAssertions());
                        System.out.println(endpoint);
                    }
                    else if(method.equalsIgnoreCase("DELETE")){
                        ApiRes response = new ApacheDeleteHandler().handle(endpoint);
                        notifyResponse(response, endpoint.getAssertions());
                        System.out.println(endpoint);
                    }
                    else if(method.equalsIgnoreCase("HEAD")){
                        ApiRes response = new ApacheHeadHandler().handle(endpoint);
                        notifyResponse(response, endpoint.getAssertions());
                        System.out.println(endpoint);
                    }
                    else if(method.equalsIgnoreCase("OPTIONS")){
                        ApiRes response = new ApacheOptionsHandler().handle(endpoint);
                        notifyResponse(response, endpoint.getAssertions());
                        System.out.println(endpoint);
                    } else {
                        String message = String.format("Handler for '%s' method has not yet been implemented", method);
                        throw new UnsupportedOperationException(message);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String... args) {
        String ENDPOINTS_FILE = "/rest/target-endpoints.json";
        RestConnector client = new RestConnector(new CpathJsonLoader(ENDPOINTS_FILE));
        client.run();
    }
}
