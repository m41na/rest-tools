package com.practicaldime.rest.tools.client;

import java.util.List;

import com.practicaldime.rest.tools.api.ApiAssert;
import com.practicaldime.rest.tools.api.ApiRes;

public interface ApiResListener {

    ApiRes getApiResponse();
    
    List<ApiAssert> getApiAssertions();
    
    void onReadyResponse(ApiRes response, List<ApiAssert> assertions);
}
