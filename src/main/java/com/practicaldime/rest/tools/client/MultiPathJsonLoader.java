package com.practicaldime.rest.tools.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiPathJsonLoader implements JsonLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ClassPathJsonLoader.class);
    private final Boolean fileResource;
    private final ClassPathJsonLoader cpLoader;
    private final FilePathJsonLoader fpLoader;
    private final StringJsonLoader strLoader;
    
    public MultiPathJsonLoader(String source) {
        this(false, source);
    }

    public MultiPathJsonLoader(Boolean fileResource, String source) {
        super();
        this.fileResource = fileResource;
        this.cpLoader = new ClassPathJsonLoader(source);
        this.fpLoader = new FilePathJsonLoader(source);
        this.strLoader = new StringJsonLoader(source);
    }

    @Override
    public JsonNode loadJson() {
        if (!fileResource) {
            LOG.info("Not a file resource, so use string content loader");
            return strLoader.loadJson();
        } else {
            LOG.info("It's a file resource, so search classpath first then fallback to file path if need be");
            try {
                //try classpath
                return cpLoader.loadJson();
            } catch (RuntimeException e) {
                //try file path
                return fpLoader.loadJson();
            }
        }
    }

    @Override
    public <T> T readValue(Class<T> type) {
        if (!fileResource) {
            LOG.info("Not a file resource, so use string content loader");
            return strLoader.readValue(type);
        } else {
            LOG.info("It's a file resource, so search classpath first then fallback to file path if need be");
            try {
                //try classpath
                return cpLoader.readValue(type);
            } catch (RuntimeException e) {
                //try file path
                return fpLoader.readValue(type);
            }
        }
    }

    @Override
    public <T> T readValue(TypeReference<T> type) {
        if (!fileResource) {
            LOG.info("Not a file resource, so use string content loader");
            return strLoader.readValue(type);
        } else {
            LOG.info("It's a file resource, so search classpath first then fallback to file path if need be");
            try {
                //try classpath
                return cpLoader.readValue(type);
            } catch (RuntimeException e) {
                //try file path
                return fpLoader.readValue(type);
            }
        }
    }
}
