package works.hop.rest.tools.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class SimpleJson {

    public static ObjectMapper provideObjectMapper() {
        return new ObjectMapper();
    }

    public static <T> String toJson(T value) {
        try {
            ObjectMapper mapper = provideObjectMapper();
            try {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
            } catch (NoSuchMethodError e) {
                return mapper.writeValueAsString(value);
            }
        } catch (JsonProcessingException cause) {
            throw new RuntimeException(cause);
        }
    }

    public static <T> T fromJson(String json, Class<T> type) {
        try {
            ObjectMapper mapper = provideObjectMapper();
            return mapper.readValue(json, type);
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    public static void main(String... args) {
        System.err.println("for testing purposes");
    }
}
