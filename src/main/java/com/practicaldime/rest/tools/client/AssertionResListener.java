package com.practicaldime.rest.tools.client;

import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.mvel2.MVEL;

import com.practicaldime.common.entity.rest.ApiAssert;
import com.practicaldime.common.entity.rest.ApiRes;
import com.practicaldime.rest.tools.util.RestToolsJson;

public class AssertionResListener implements ApiResListener {

    private ApiRes response;
    private List<ApiAssert> assertions;

    @Override
    public void onReadyResponse(ApiRes response, List<ApiAssert> assertions) {
        this.response = response;
        this.assertions = assertions;
        if (assertions != null && assertions.size() > 0) {
            assertions.stream().forEach((assertion) -> {
                String result = executeAssertion(assertion);
                assertion.setResult(result);
            });
        }
        //print response
        System.out.print(response.getResponseBody());
    }

    @Override
    public ApiRes getApiResponse() {
        return this.response;
    }

    @Override
    public List<ApiAssert> getApiAssertions() {
        return this.assertions;
    }

    private String executeAssertion(ApiAssert assertion) {
        switch (assertion.getAssertType()) {
            case assertContains: {
                Map<String, Object> context = RestToolsJson.fromJson(response.getResponseBody().toString(), Map.class);
                String actual = MVEL.evalToString(assertion.getActualValue(), context);
                String result = actual.contains((String) assertion.getExpectedValue()) ? "pass" : assertion.getFailMessage();
                assertion.setResult(result);
                return result;
            }
            case assertEquals: {
                Map<String, Object> context = RestToolsJson.fromJson(response.getResponseBody().toString(), Map.class);
                String actual = MVEL.evalToString(assertion.getActualValue(), context);
                String result = actual.equals(assertion.getExpectedValue()) ? "pass" : assertion.getFailMessage();
                assertion.setResult(result);
                return result;
            }
            case assertNotEmpty: {
                Map<String, Object> context = RestToolsJson.fromJson(response.getResponseBody().toString(), Map.class);
                String actual = MVEL.evalToString(assertion.getActualValue(), context);
                String result = actual.length() > 0 ? "pass" : assertion.getFailMessage();
                assertion.setResult(result);
                return result;
            }
            case assertElementExists:{
                Document doc = Jsoup.parse(response.getResponseBody().toString());
                Element element = doc.selectFirst(assertion.getActualValue());
                String result = element != null ? "pass" : assertion.getFailMessage();
                assertion.setResult(result);
                return result;
            }
            case assertElementTextContains:{
                Document doc = Jsoup.parse(response.getResponseBody().toString());
                Element element = doc.selectFirst(assertion.getActualValue());
                String result = element.text().contains((String) assertion.getExpectedValue()) ? "pass" : assertion.getFailMessage();
                assertion.setResult(result);
                return result;
            }
            default:
                System.out.println("Could not determine result");
                break;
        }
        return "";
    }
}
