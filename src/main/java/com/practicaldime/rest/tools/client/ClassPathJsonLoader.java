package com.practicaldime.rest.tools.client;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class ClassPathJsonLoader implements JsonLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ClassPathJsonLoader.class);
    private final String sourceFile;

    public ClassPathJsonLoader(String sourceFile) {
        super();
        this.sourceFile = sourceFile.startsWith("/")? sourceFile.substring(1) : sourceFile;
    }

    @Override
    public JsonNode loadJson() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(sourceFile)) {
            LOG.info("creating sample endpoints data");
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(is);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T readValue(Class<T> type) {
        try (InputStream src = getClass().getClassLoader().getResourceAsStream(sourceFile)) {
            ObjectMapper mapper = new ObjectMapper();
            T result = mapper.readValue(src, type);
            return result;
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }

    @Override
    public <T> T readValue(TypeReference<T> type) {
        try (InputStream src = getClass().getClassLoader().getResourceAsStream(sourceFile)) {
            ObjectMapper mapper = new ObjectMapper();
            T result = mapper.readValue(src, type);
            return result;
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }
}
