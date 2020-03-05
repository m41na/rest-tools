package com.practicaldime.rest.tools.impl;

import com.practicaldime.common.entity.rest.ApiReq;
import com.practicaldime.rest.tools.annotation.Api;
import com.practicaldime.rest.tools.api.ApiReqBuilder;
import com.practicaldime.rest.tools.util.RestToolsJson;
import org.apache.commons.compress.utils.Lists;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for generating API documentation based on available and correctly
 * annotated end-points. Uses Apache's httpclient API to get the job done
 */
public abstract class ApiDocGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(ApiDocGenerator.class);

    protected EndpointConfig config;
    protected ApiReqBuilder builder;

    public ApiDocGenerator(EndpointConfig config, ApiReqBuilder builder) {
        super();
        this.config = config;
        this.builder = builder;
    }

    public static boolean contains(String obj, String[] either) {
        for (String o : either) {
            if (obj.equalsIgnoreCase(o)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matches(Pattern pattern, String... items) {
        for (String item : items) {
            Matcher matcher = pattern.matcher(item);
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchesBySimpleClassName(String clsName, String[] either) {
        for (String name : either) {
            if (clsName.endsWith(name)) {
                return true;
            }
        }
        return false;
    }

    public static Annotation[] hasTargetAnnotation(Method method, Class<?> target) {
        Annotation[] annotations = method.getAnnotations();
        for (Annotation anno : annotations) {
            if (anno.annotationType().equals(target)) {
                return annotations;
            }
        }
        return null;
    }

    public static Annotation fetchAnnotation(Annotation[] annotations, Class<?> target) {
        for (Annotation anno : annotations) {
            if (anno.annotationType().equals(target)) {
                return anno;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        EndpointConfig config = EndpointConfig.build("rest/api-gen-config.properties");
        ApiDocGenerator gen = new ApiDocGenerator(config, new ApiReqBuilder()) {

            @Override
            public ApiReq createApiReq() {
                return new ApiReq();
            }

            @Override
            public void mergeWithOtherEndpoints(Collection<ApiReq> targetEndpoints) {
                // TODO: write implementation if need be
            }

            @Override
            public String getEndpointDefinitions() {
                return config.getEndpointDefinitions();
            }

            @Override
            public CloseableHttpClient getRestClient() {
                return HttpClients.createDefault();
            }
        };
        gen.start();
    }

    public abstract ApiReq createApiReq();

    public abstract String getEndpointDefinitions();

    public abstract void mergeWithOtherEndpoints(Collection<ApiReq> targetEndpoints);

    public abstract CloseableHttpClient getRestClient();

    public List<Class<?>> getResources(String packageName, List<Class<?>> list) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(packagePath);
        while (resources.hasMoreElements()) {
            URL res = resources.nextElement();
            String resName = res.getFile();
            File resFile = new File(resName);
            File[] files = resFile.listFiles();
            LOG.info(String.format("inspecting %s inside %s", resName, packagePath));
            for (File file : files) {
                String fileName = file.getName();
                if (!file.isHidden() && file.isFile() && fileName.endsWith(".class")) {
                    String javaFileName = String.format("%s.%s", packageName, fileName).replace(".class", "");
                    Class<?> clazz = Class.forName(javaFileName);
                    Annotation[] classAnno = clazz.getAnnotations();
                    for (Annotation anno : classAnno) {
                        if (anno.annotationType().getSimpleName().equals("Path")) {
                            // this is a resource, so add to list, and break
                            // from this inner loop
                            list.add(clazz);
                            LOG.info(String.format("added %s to resources list", fileName));
                            break;
                        }
                    }
                }

                if (file.isDirectory()) {
                    getResources(String.format("%s%s%s", packageName, ".", fileName), list);
                }
            }
        }
        return list;
    }

    public List<Class<?>> getTargetedResources() throws Exception {
        List<Class<?>> resources;
        if (config.getInspectAll()) {
            resources = getResources(config.getBasePackage(), new ArrayList<>());
        } else {
            resources = Arrays.asList(config.getTargetedResources());
        }
        LOG.info(String.format("found %d resources", resources.size()));
        return resources;
    }

    public List<Method> filterTargetedEndpoints(Class<?> resourceClass) {
        List<String> targetedList = Arrays.asList(config.getTargetedEndpoints());
        List<Method> methods = new ArrayList<>(Arrays.asList(resourceClass.getMethods()));

        for (Iterator<Method> iter = methods.iterator(); iter.hasNext(); ) {
            Method method = iter.next();
            Boolean dropMethod = Boolean.TRUE;
            String endpointId = null;
            Annotation[] annotations = method.getAnnotations();
            for (Annotation anno : annotations) {
                if (anno.annotationType().equals(Api.class)) {
                    // check if this method if targeted
                    endpointId = ((Api) anno).id();
                    if (config.getInspectAll()) {
                        dropMethod = Boolean.FALSE;
                        break;
                    }

                    if (targetedList.contains(endpointId)) {
                        dropMethod = Boolean.FALSE;
                        break;
                    }
                }
            }
            if (dropMethod) {
                iter.remove();
                if (endpointId != null) {
                    LOG.info("dropped '{} - {}' endpoint from targeted list", endpointId, method.getName());
                }
            }
        }
        return methods;
    }

    public Map<String, ApiReq> generateEndpointsInfo(Class<?> resourceClass) {
        Map<String, ApiReq> resourceEndpoints = new HashMap<>();
        // if server is up, generate the end-points

        LOG.info("inspecting the class '{}' for admissible endpoints", resourceClass.getName());
        // fetch targeted end-points (resource methods)
        List<Method> methods = filterTargetedEndpoints(resourceClass);
        for (Method method : methods) {

            Annotation[] methodAnnotations = method.getAnnotations();

            ApiReq endpoint = createApiReq();

            for (Annotation anno : methodAnnotations) {
                Class<?> annoType = anno.annotationType();

                // use custom builder for custom annotations
                this.builder.build(anno, endpoint);

                // build internally
                if (annoType.equals(Path.class)) {
                    String parentPath = resourceClass.getAnnotation(Path.class).value();
                    String methodPath = ((Path) anno).value();
                    if (parentPath.indexOf("/") != 0) {
                        parentPath = "/" + parentPath;
                    }
                    if (parentPath.lastIndexOf("/") != parentPath.length() - 1 && methodPath.indexOf("/") != 0) {
                        parentPath = parentPath + "/";
                    }
                    endpoint.setPath(parentPath + methodPath);
                    continue;
                }

                if (annoType.equals(Api.class)) {
                    LOG.info(String.format("generating doc for the %s() endpoint", method.getName()));
                    Api doc = (Api) anno;
                    endpoint.setId(doc.id());
                    endpoint.setName(method.getName());
                    endpoint.getResponse().setResponseBody(doc.response().getBytes());
                    endpoint.getResponse().setStatusCode(doc.status());
                    endpoint.setEntity(doc.entity());
                    endpoint.setConsumes(String.join(";", doc.consumes()));
                    endpoint.setProduces(String.join(";", doc.produces()));
                    endpoint.setQuery(doc.query());

                    // populate headers with test data if available
                    for (String header : doc.headers()) {
                        int index = header.indexOf(':');
                        if (index == -1) {
                            index = header.indexOf('=');
                        }
                        if (index > -1) {
                            String key = header.substring(0, index);
                            String value = header.substring(index + 1);
                            endpoint.addHeader(key, value);
                        } else {
                            endpoint.addHeader(header, "");
                        }
                    }
                }
            }

            // some methods don't have @Path annotation, so use parent's for the
            // path value
            if (fetchAnnotation(methodAnnotations, Path.class) == null) {
                String parentPath = resourceClass.getAnnotation(Path.class).value();
                if (parentPath.indexOf("/") != 0) {
                    parentPath = "/" + parentPath;
                }
                endpoint.setPath(parentPath);
            }

            // extract headers from parameters
            Annotation[][] params = method.getParameterAnnotations();
            for (Annotation[] param : params) {
                for (Annotation anno : param) {
                    if (anno.annotationType().equals(HeaderParam.class)) {
                        String key = ((HeaderParam) anno).value();
                        if (!endpoint.getHeaders().containsKey(key)) {
                            endpoint.addHeader(key, "");
                        }
                    }
                }
            }

            // if 'consumes' indicates json, and the request method is not GET,
            // then resolve 'entity' from JsonData
            if (matches(Pattern.compile(".*json$"), endpoint.getConsumes()) && !endpoint.getMethod().equalsIgnoreCase("GET")) {
                String entity = endpoint.getEntity();
                if (entity != null && entity.trim().length() > 0) {
                    // check if entity is not valid json, and if so, resolve
                    // from data file
                    if (!ApiDocJsonData.isValidJson(entity)) {
                        entity = ApiDocJsonData.instance(config.getRamlInputData()).resolve(endpoint.getName());
                    }
                }
                endpoint.setEntity(entity);
            }

            // check that endpoint name is unique
            if (resourceEndpoints.keySet().contains(endpoint.getName())) {
                ApiReq existing = resourceEndpoints.get(endpoint.getName());
                String errorMessage = String.format("These two endpoints [%s, %s] appear to have the same name, You should assign a unique id for each endpoint", existing,
                        endpoint);
                LOG.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }

            // fire off request to endpoint to fetch response
            String resourcePath = endpoint.getPath();
            String serviceURL = config.sanitizedURL(resourcePath);

            try {
                if (!(contains(endpoint.getMethod(), config.getIgnoreMethods()))) {
                    if (serverIsUp()) {
                        LOG.info(String.format("fetching response for [%s] %s", endpoint.getMethod(), serviceURL));
                        //prepare headers
                        Header accepts = new BasicHeader(HttpHeaders.ACCEPT, endpoint.getProduces());
                        Header contentType = new BasicHeader(HttpHeaders.CONTENT_TYPE, endpoint.getConsumes());
                        List<Header> headers = Lists.newArrayList();
                        headers.add(accepts);
                        headers.add(contentType);
                        endpoint.getHeaders().forEach((key, value) -> headers.add(new BasicHeader(key, value)));
                        //configure request (if need be)
                        final RequestConfig requestConfig = RequestConfig.DEFAULT;

                        try (final CloseableHttpClient httpClient =
                                     HttpClients.custom().setDefaultHeaders(headers).setDefaultRequestConfig(requestConfig).build()) {
                            final HttpPost httpPost = new HttpPost(serviceURL);
                            httpPost.setEntity(new StringEntity(endpoint.getEntity()));

                            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {

                                String responseBody = EntityUtils.toString(response.getEntity());
                                endpoint.getResponse().setResponseBody(responseBody.replaceAll("\\r|\\n", "").getBytes());
                            }
                        }
                    }
                } else {
                    LOG.info(String.format("skipping request for [%s] %s", endpoint.getMethod(), serviceURL));
                }
            } catch (Exception e) {
                // if an endpoint blows out for some reason, log the error and
                // ignore
                LOG.error(String.format("problem executing this endpoint '%s' with error message '%s'", endpoint.toString(), e.getMessage()));
            }

            // add to endpoints map
            resourceEndpoints.put(endpoint.getId(), endpoint);

        }
        return resourceEndpoints;
    }

    public Map<String, ApiReq> generateApiReq(List<Class<?>> resourceClasses) {
        Map<String, ApiReq> endpoints = new HashMap<>();
        resourceClasses.stream().filter((resource) -> (!matchesBySimpleClassName(resource.getName(), config.getExcludedResources()))).forEachOrdered((resource) -> {
            endpoints.putAll(generateEndpointsInfo(resource));
        });
        LOG.info(String.format("found %d endpoints", endpoints.size()));
        return endpoints;
    }

    public Collection<ApiReq> getTargetedEndpoints(Map<String, ApiReq> endpoints) {
        Collection<ApiReq> targetEndpoints;
        if (config.getInspectAll()) {
            targetEndpoints = endpoints.values();
        } else {
            targetEndpoints = new ArrayList<>();
            for (String endpointId : config.getTargetedEndpoints()) {
                ApiReq target = endpoints.get(endpointId);
                if (target != null) {
                    targetEndpoints.add(target);
                } else {
                    LOG.error("Could NOT find endpoint with id {}. This endpoint will be IGNORED", endpointId);
                }
            }
        }
        return targetEndpoints;
    }

    public boolean serverIsUp() {
        String aliveURL = config.getAliveEndpoint();
        if (aliveURL != null && aliveURL.trim().length() > 0) {
            String serviceURL = config.sanitizedURL(aliveURL);
            //prepare headers
            List<Header> headers = Lists.newArrayList();
            Header accepts = new BasicHeader(HttpHeaders.ACCEPT, "text/plain");
            Header contentType = new BasicHeader(HttpHeaders.ACCEPT, "application/json");
            headers.add(accepts);
            headers.add(contentType);
            createApiReq().getHeaders().forEach((key, value) -> headers.add(new BasicHeader(key, value)));

            try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultHeaders(headers).build()) {
                HttpGet httpGet = new HttpGet(serviceURL);
                try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                    return response.getStatusLine().getStatusCode() == 200;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean equals(Object obj, Object... either) {
        for (Object o : either) {
            if (obj.equals(o)) {
                return true;
            }
        }
        return false;
    }

    private String getStringValue(Class<?> clazz) {
        if (clazz.equals(Boolean.class)) {
            return "{\"result\" : \"success\"}";
        } else {
            // try using default constructor
            try {
                return RestToolsJson.toJson(clazz.newInstance());
            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException("The class should have a default constructor", e);
            }
        }
    }

    public void start() {
        try {
            // identify targeted resources
            List<Class<?>> resources = getTargetedResources();

            // extract endpoints
            Map<String, ApiReq> endpoints = generateApiReq(resources);

            // identify targeted endpoints
            Collection<ApiReq> targetEndpoints = getTargetedEndpoints(endpoints);

            // merge with other endpoints from other sources
            mergeWithOtherEndpoints(targetEndpoints);

            // create raml output
            String merged = TemplateEngine.getInstance().mergeWithTemplates(targetEndpoints, config.getWriteToRemoteFile());
            LOG.info(merged);

        } catch (Exception e) {
            LOG.error(String.format("Ooops! Report this error. '%s'", e.getMessage()));
            e.printStackTrace(System.err);
        }
    }
}
