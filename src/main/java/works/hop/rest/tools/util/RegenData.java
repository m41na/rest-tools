package works.hop.rest.tools.util;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import works.hop.rest.tools.api.ApiReqComparator;
import works.hop.rest.tools.client.ClassPathJsonLoader;
import works.hop.rest.tools.client.JsonLoader;
import works.hop.rest.tools.client.RestConnector;
import works.hop.rest.tools.model.ApiReq;

public class RegenData {

    private static final Logger LOG = LoggerFactory.getLogger(RegenData.class);

    public static void main(String[] args) {
        String ENDPOINTS_FILE = "/rest/sample-endpoints.json";
        JsonLoader loader = new ClassPathJsonLoader(ENDPOINTS_FILE);
        List<ApiReq> nodes = loader.readValue(new TypeReference<List<ApiReq>>(){});
        List<ApiReq> endpoints = RestConnector.mergeEndpoints(nodes);
        Collections.sort(endpoints, new ApiReqComparator());
        
        //renumber nodes
        for (int i = 1; i <= endpoints.size(); i++) {
            endpoints.get(i - 1).setId(String.valueOf(i));
        }
        //print regenerated endpoints
        LOG.info(endpoints.toString());
    }
}
