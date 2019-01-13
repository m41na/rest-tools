package com.practicaldime.rest.tools.impl;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiDocJsonData {

    private static final Logger LOG = LoggerFactory.getLogger(ApiDocJsonData.class);
    private final String dataFile;

    private static ApiDocJsonData instance;

    private JsonNode data;

    private ApiDocJsonData(String dataFile) {
        try {
            this.dataFile = dataFile;
            InputStream is = ApiDocJsonData.class.getResourceAsStream(dataFile);

            ObjectMapper objectMapper = new ObjectMapper();
            data = objectMapper.readTree(is);
            LOG.info("json input data initialized successfully");
        } catch (IOException e) {
            LOG.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public String getDataFile() {
        return dataFile;
    }

    public String resolve(String serviceId) {
        JsonNode entity = data.get(serviceId);
        return (entity != null) ? entity.toString() : null;
    }

    public static ApiDocJsonData instance(String dataFile) {
        if (instance == null) {
            instance = new ApiDocJsonData(dataFile);
        }
        return instance;
    }

    public static boolean isValidJson(String input) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(input);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void main(String... args) {
        System.out.println(ApiDocJsonData.instance("/rest/raml-input-data.json").resolve("PING_1"));
    }
}
