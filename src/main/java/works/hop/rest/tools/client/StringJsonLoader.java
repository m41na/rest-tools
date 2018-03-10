package works.hop.rest.tools.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class StringJsonLoader implements JsonLoader {

    private static final Logger LOG = LoggerFactory.getLogger(StringJsonLoader.class);
    private final String jsonString;

    public StringJsonLoader(String jsonString) {
        super();
        this.jsonString = jsonString;
    }

    @Override
    public JsonNode loadJson() {
        try {
            LOG.info("creating sample endpoints data");
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(jsonString);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
