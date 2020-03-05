package com.practicaldime.rest.tools.client;

import com.practicaldime.common.entity.rest.ApiAssert;
import com.practicaldime.common.entity.rest.ApiRes;

import java.util.List;

public interface ApiResListener {

    ApiRes getApiResponse();

    List<ApiAssert> getApiAssertions();

    void onReadyResponse(ApiRes response, List<ApiAssert> assertions);
}
