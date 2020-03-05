package com.practicaldime.rest.tools.client;

import java.util.List;

import com.practicaldime.common.entity.rest.ApiAssert;
import com.practicaldime.common.entity.rest.ApiRes;

public interface ApiResListener {

    ApiRes getApiResponse();
    
    List<ApiAssert> getApiAssertions();
    
    void onReadyResponse(ApiRes response, List<ApiAssert> assertions);
}
