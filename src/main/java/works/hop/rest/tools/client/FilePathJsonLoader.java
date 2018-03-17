package works.hop.rest.tools.client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FilePathJsonLoader implements JsonLoader {

    private static final Logger LOG = LoggerFactory.getLogger(FilePathJsonLoader.class);
    private final String sourceFile;

    public FilePathJsonLoader(String sourceFile) {
        super();
        this.sourceFile = sourceFile;
    }

    @Override
    public JsonNode loadJson() {
        File file = new File(".", sourceFile);
        if (file.exists() && file.isFile()) {
            try (InputStream is = new FileInputStream(file)) {
                LOG.info("creating sample endpoints data");
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readTree(is);
            } catch (IOException e) {
                LOG.error(e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("This file could not be located on the path provided: " + sourceFile);
        }
    }

    @Override
    public <T> T readValue(Class<T> type) {
        File file = new File(".", sourceFile);
        if (file.exists() && file.isFile()) {
            try (InputStream is = new FileInputStream(file)) {
                LOG.info("creating sample endpoints data");
                ObjectMapper mapper = new ObjectMapper();
                T result = mapper.readValue(is, type);
                return result;
            } catch (IOException e) {
                e.printStackTrace(System.err);
                return null;
            }
        } else {
            throw new RuntimeException("This file could not be located on the path provided: " + sourceFile);
        }
    }

    @Override
    public <T> T readValue(TypeReference<T> type) {
        File file = new File(".", sourceFile);
        if (file.exists() && file.isFile()) {
            try (InputStream is = new FileInputStream(file)) {
                LOG.info("creating sample endpoints data");
                ObjectMapper mapper = new ObjectMapper();
                T result = mapper.readValue(is, type);
                return result;
            } catch (IOException e) {
                e.printStackTrace(System.err);
                return null;
            }
        } else {
            throw new RuntimeException("This file could not be located on the path provided: " + sourceFile);
        }
    }
}
