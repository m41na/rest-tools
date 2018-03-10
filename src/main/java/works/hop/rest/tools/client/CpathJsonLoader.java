package works.hop.rest.tools.client;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

import works.hop.rest.tools.impl.ApiDocJsonData;

public class CpathJsonLoader implements JsonLoader {

    private static final Logger LOG = LoggerFactory.getLogger(CpathJsonLoader.class);
    private final String sourceFile;

    public CpathJsonLoader(String sourceFile) {
        super();
        this.sourceFile = sourceFile;
    }

    @Override
    public JsonNode loadJson() {
        try {
            InputStream is = ApiDocJsonData.class.getResourceAsStream(sourceFile);

            LOG.info("creating sample endpoints data");
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(is);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
