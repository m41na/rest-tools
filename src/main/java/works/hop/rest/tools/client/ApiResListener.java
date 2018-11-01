package works.hop.rest.tools.client;

import java.util.List;

import works.hop.rest.tools.model.ApiAssert;
import works.hop.rest.tools.model.ApiRes;

public interface ApiResListener {

    ApiRes getApiResponse();
    
    List<ApiAssert> getApiAssertions();
    
    void onReadyResponse(ApiRes response, List<ApiAssert> assertions);
}
