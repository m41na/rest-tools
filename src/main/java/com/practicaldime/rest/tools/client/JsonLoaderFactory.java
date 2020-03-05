package com.practicaldime.rest.tools.client;

import java.util.function.Function;

public class JsonLoaderFactory implements Function<String, JsonLoader> {

    private final String FILE_PATH_LOADER = "file:";
    private final String CLASS_PATH_LOADER = "classpath:";
    private final String MULTI_PATH_LOADER = "multi:";

    @Override
    public JsonLoader apply(String resource) {
        if(resource.startsWith(FILE_PATH_LOADER)){
            String path = resource.substring(FILE_PATH_LOADER.length());
            return new FilePathJsonLoader(path);
        }
        if(resource.startsWith(CLASS_PATH_LOADER)){
            String path = resource.substring(CLASS_PATH_LOADER.length());
            return new ClassPathJsonLoader(path);
        }
        if(resource.startsWith(MULTI_PATH_LOADER)){
            String path = resource.substring(MULTI_PATH_LOADER.length());
            return new MultiPathJsonLoader(path);
        }
        return new StringJsonLoader(resource);
    }
}
