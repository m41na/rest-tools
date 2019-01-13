package com.practicaldime.rest.tools.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

public interface JsonLoader {

    JsonNode loadJson();
    
    <T> T readValue(Class<T> type);
    
    <T> T readValue(TypeReference<T> type);
    
    
}
