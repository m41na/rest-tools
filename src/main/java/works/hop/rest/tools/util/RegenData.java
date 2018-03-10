package works.hop.rest.tools.util;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import works.hop.rest.tools.api.ApiReqComparator;

import works.hop.rest.tools.api.ApiReq;
import works.hop.rest.tools.client.CpathJsonLoader;
import works.hop.rest.tools.client.JsonLoader;
import works.hop.rest.tools.client.RestConnector;

public class RegenData {

    private static final Logger LOG = LoggerFactory.getLogger(RegenData.class);

    public static void main(String[] args) {
        String ENDPOINTS_FILE = "/rest/sample-endpoints.json";
        JsonLoader loader = new CpathJsonLoader(ENDPOINTS_FILE);
        List<ApiReq> endpoints = RestConnector.extractAndMergeEndpoints(loader.loadJson());
        Collections.sort(endpoints, new ApiReqComparator());
        
        //renumber nodes
        for (int i = 1; i <= endpoints.size(); i++) {
            endpoints.get(i - 1).setId(String.valueOf(i));
        }
        //print regenerated endpoints
        LOG.info(endpoints.toString());
    }
}
