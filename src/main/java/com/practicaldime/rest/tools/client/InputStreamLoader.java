package com.practicaldime.rest.tools.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputStreamLoader implements JsonLoader {

    private static final Logger LOG = LoggerFactory.getLogger(FilePathJsonLoader.class);
    private final InputStream input;

    public InputStreamLoader(InputStream input) {
        this.input = input;
    }

    @Override
    public JsonNode loadJson() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(input);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T readValue(Class<T> type) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            T result = mapper.readValue(input, type);
            return result;
        } catch (IOException e) {
            LOG.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T readValue(TypeReference<T> type) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            T result = mapper.readValue(input, type);
            return result;
        } catch (IOException e) {
            LOG.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
