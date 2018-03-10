package works.hop.rest.tools.client;

import java.util.List;
import works.hop.rest.tools.api.ApiAssert;
import works.hop.rest.tools.api.ApiRes;

public interface ApiResListener {

    ApiRes getApiResponse();
    
    List<ApiAssert<?>> getApiAssertions();
    
    List<String> getAssertionResults();
    
    void onReadyResponse(ApiRes response, List<ApiAssert<?>> assertions);
}
