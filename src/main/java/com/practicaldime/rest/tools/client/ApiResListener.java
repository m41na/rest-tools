package com.practicaldime.rest.tools.client;

import java.util.List;

import com.practicaldime.rest.tools.model.ApiAssert;
import com.practicaldime.rest.tools.model.ApiRes;

public interface ApiResListener {

    ApiRes getApiResponse();
    
    List<ApiAssert> getApiAssertions();
    
    void onReadyResponse(ApiRes response, List<ApiAssert> assertions);
}
