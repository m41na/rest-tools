package works.hop.rest.tools.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import works.hop.rest.tools.util.RestToolsJson;

public class EndpointConfig {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointConfig.class);

    private String URL;
    private String aliveEndpoint;
    private String basePackage = "works.hop.tools.rest.resource";
    private Boolean inspectAll = Boolean.FALSE;
    private Class<?>[] targetedResources = {};
    private String[] excludedResources = {};
    private String[] targetedEndpoints = {};
    private Boolean writeToRemoteFile = Boolean.FALSE;
    private String remoteFilePath;
    private Boolean mergeWithExisting = Boolean.FALSE;
    private String[] ignoreMethods = new String[]{"POST"};
    private Boolean generateRAML;
    private String endpointDefinitions;
    private String ramlInputData;

    public String getURL() {
        return URL;
    }

    public void setURL(String uRL) {
        URL = uRL;
    }

    public String getAliveEndpoint() {
        return aliveEndpoint;
    }

    public void setAliveEndpoint(String aliveEndpoint) {
        this.aliveEndpoint = aliveEndpoint;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public Boolean getInspectAll() {
        return inspectAll;
    }

    public void setInspectAll(Boolean inspectAll) {
        this.inspectAll = inspectAll;
    }

    public Class<?>[] getTargetedResources() {
        return targetedResources;
    }

    public void setTargetedResources(Class<?>[] targetedResources) {
        this.targetedResources = targetedResources;
    }

    public String[] getExcludedResources() {
        return excludedResources;
    }

    public void setExcludedResources(String[] excludedResources) {
        this.excludedResources = excludedResources;
    }

    public String[] getTargetedEndpoints() {
        return targetedEndpoints;
    }

    public void setTargetedEndpoints(String[] targetedEndpoints) {
        this.targetedEndpoints = targetedEndpoints;
    }

    public Boolean getWriteToRemoteFile() {
        return writeToRemoteFile;
    }

    public void setWriteToRemoteFile(Boolean writeToRemoteFile) {
        this.writeToRemoteFile = writeToRemoteFile;
    }

    public String getRemoteFilePath() {
        return remoteFilePath;
    }

    public void setRemoteFilePath(String remoteFilePath) {
        this.remoteFilePath = remoteFilePath;
    }

    public Boolean getMergeWithExisting() {
        return mergeWithExisting;
    }

    public void setMergeWithExisting(Boolean mergeWithExisting) {
        this.mergeWithExisting = mergeWithExisting;
    }

    public String[] getIgnoreMethods() {
        return ignoreMethods;
    }

    public void setIgnoreMethods(String[] ignoreMethods) {
        this.ignoreMethods = ignoreMethods;
    }

    public Boolean getGenerateRAML() {
        return generateRAML;
    }

    public void setGenerateRAML(Boolean generateRAML) {
        this.generateRAML = generateRAML;
    }

    public String getEndpointDefinitions() {
        return endpointDefinitions;
    }

    public void setEndpointDefinitions(String endpointDefinitions) {
        this.endpointDefinitions = endpointDefinitions;
    }

    public String getRamlInputData() {
        return ramlInputData;
    }

    public void setRamlInputData(String ramlInputData) {
        this.ramlInputData = ramlInputData;
    }

    public String sanitizedURL(String endpointPath) {
        String serviceURL = getURL();
        if (serviceURL.lastIndexOf("/") == serviceURL.length() - 1 && endpointPath.indexOf("/") == 0) {
            serviceURL = serviceURL.substring(0, serviceURL.length() - 1);
        }
        if (serviceURL.lastIndexOf("/") != serviceURL.length() - 1 && endpointPath.indexOf("/") != 0) {
            serviceURL = serviceURL + "/";
        }
        return serviceURL + endpointPath;
    }

    public static boolean isValid(String value) {
        return value != null && value.trim().length() > 0;
    }

    public static int[] convert(String[] values) {
        int[] result = new int[values.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = StringUtils.isNumeric(values[i]) ? Integer.valueOf(values[i]) : 0;
        }
        return result;
    }

    public static void parseTargetedEndpointId(String targetedEndpointIds, List<String> result) {
        Matcher rangeMatch = Pattern.compile("\\/\\d+-\\d+\\/").matcher(targetedEndpointIds);
        Matcher seriesMatch = Pattern.compile("\\/[\\d\\|]+\\/").matcher(targetedEndpointIds);
        //Matcher matchAll = Pattern.compile("\\/\\*\\/").matcher(targetedEndpointIds);

        if (rangeMatch.find()) {
            String endpointId = targetedEndpointIds.substring(0, rangeMatch.start());
            String group = rangeMatch.group();
            int[] range = convert(group.substring(1, group.length() - 1).split("-"));

            int index = range[0];
            if (index > range[1]) {
                throw new RuntimeException("The range provided is invalid");
            }
            while (index <= range[1]) {
                result.add(endpointId + index);
                index++;
            }
        } else if (seriesMatch.find()) {
            String endpointId = targetedEndpointIds.substring(0, seriesMatch.start());
            String group = seriesMatch.group();
            String[] values = group.substring(1, group.length() - 1).split("\\|");

            for (String value : values) {
                result.add(endpointId + value);
            }
        } else {
            result.add(targetedEndpointIds);
        }
    }

    public static EndpointConfig build(String location) {
        InputStream in = null;
        Properties prop = new Properties();
        try {
            in = EndpointConfig.class.getClassLoader().getResourceAsStream(location);
            prop.load(in);
        } catch (IOException | RuntimeException e) {
            LOG.error("Could not build the application configiration from the file specified. Falling back to default values");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return build(prop);
    }

    public static EndpointConfig build(Properties prop) {
        // map properties
        EndpointConfig config = new EndpointConfig();
        String basePackage = prop.getProperty("basePackage");
        if (isValid(basePackage)) {
            config.setBasePackage(basePackage);
        }
        String aliveEndpoint = prop.getProperty("aliveEndpoint");
        if (isValid(aliveEndpoint)) {
            config.setAliveEndpoint(aliveEndpoint);
        }
        String inspectAll = prop.getProperty("inspectAll");
        if (isValid(inspectAll)) {
            config.setInspectAll(Boolean.valueOf(inspectAll));
        }
        String mergeWithExisting = prop.getProperty("mergeWithExisting");
        if (isValid(mergeWithExisting)) {
            config.setMergeWithExisting(Boolean.valueOf(mergeWithExisting));
        }
        String writeToRemoteFile = prop.getProperty("writeToRemoteFile");
        if (isValid(writeToRemoteFile)) {
            config.setWriteToRemoteFile(Boolean.valueOf(writeToRemoteFile));
        }
        String remoteFilePath = prop.getProperty("remoteFilePath");
        if (isValid(remoteFilePath)) {
            config.setRemoteFilePath(remoteFilePath);
        }
        String ignoreMethods = prop.getProperty("ignoreMethods");
        if (isValid(ignoreMethods)) {
            config.setIgnoreMethods(ignoreMethods.split(";|,"));
        }
        String url = prop.getProperty("url");
        if (isValid(url)) {
            config.setURL(url);
        }
        String generateRAML = prop.getProperty("generateRAML");
        if (isValid(generateRAML)) {
            config.setGenerateRAML(Boolean.valueOf(generateRAML));
        }
        String endpointDefinitions = prop.getProperty("endpointDefinitions");
        if (isValid(endpointDefinitions)) {
            config.setEndpointDefinitions(endpointDefinitions);
        }
        String ramlInputData = prop.getProperty("ramlInputData");
        if (isValid("ramlInputData")) {
            config.setRamlInputData(ramlInputData);
        }
        String targetedEndpoints = prop.getProperty("targetedEndpoints");
        if (isValid(targetedEndpoints)) {
            String[] targetedEndpointsSplit = targetedEndpoints.split(";|,");
            List<String> result = new ArrayList<>();
            for (String target : targetedEndpointsSplit) {
                parseTargetedEndpointId(target, result);
            }
            config.setTargetedEndpoints(result.toArray(new String[result.size()]));
        }
        String targetedResources = prop.getProperty("targetedResources");
        if (isValid(targetedResources)) {
            String[] targetedResourcesSplit = targetedResources.split(";|,");
            Class<?>[] targetedResourceClasses = new Class<?>[targetedResourcesSplit.length];
            for (int i = 0; i < targetedResourceClasses.length; i++) {
                String className = targetedResourcesSplit[i];
                try {
                    if (className != null && className.trim().length() > 0) {
                        targetedResourceClasses[i] = Class.forName(className);
                    }
                } catch (ClassNotFoundException e) {
                    LOG.error(String.format("unable to load class '%s'", className));
                }
            }
            config.setTargetedResources(targetedResourceClasses);
        }
        String excludedResources = prop.getProperty("excludedResources");
        if (isValid(excludedResources)) {
            String[] excludedResourcesSplit = excludedResources.split(";|,");
            config.setExcludedResources(excludedResourcesSplit);
        }
        return config;
    }

    @Override
    public String toString() {
        return RestToolsJson.toJson(this);
    }

    public static void main(String... args) {
        EndpointConfig config = EndpointConfig.build("/rest/api-gen-config.properties");
        System.out.println(config);
    }
}
