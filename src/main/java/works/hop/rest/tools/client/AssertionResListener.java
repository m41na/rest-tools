package works.hop.rest.tools.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.mvel2.MVEL;
import works.hop.rest.tools.api.ApiAssert;
import works.hop.rest.tools.api.ApiRes;
import works.hop.rest.tools.util.SimpleJson;

public class AssertionResListener implements ApiResListener {

    private ApiRes response;
    private List<ApiAssert<?>> assertions;
    private List<String> results;

    @Override
    public void onReadyResponse(ApiRes response, List<ApiAssert<?>> assertions) {
        this.response = response;
        this.assertions = assertions;
        if (assertions != null && assertions.size() > 0) {
            this.results = new ArrayList<>(assertions.size());
            assertions.stream().forEach((assertion) -> {
                results.add(executeAssertion(assertion));
            });
        }
        //print out assertion results
        int index = 0;
        for(String result : results){
            System.out.printf("Assertion #%d:  Result ***** %s%n", ++index, result.trim().length() == 0 ? "passed" : result);
        }
    }

    @Override
    public ApiRes getApiResponse() {
        return this.response;
    }

    @Override
    public List<ApiAssert<?>> getApiAssertions() {
        return this.assertions;
    }

    @Override
    public List<String> getAssertionResults() {
        return this.results;
    }

    private String executeAssertion(ApiAssert assertion) {
        switch (assertion.assertType) {
            case assertContains:
                Map<String, Object> context = SimpleJson.fromJson(response.getResponseBody().toString(), Map.class);
                String actual = MVEL.evalToString(assertion.actualValue, context);
                return actual.contains((String) assertion.expectedValue)? "" : assertion.failMessage;
            case assertEquals:
                context = SimpleJson.fromJson(response.getResponseBody().toString(), Map.class);
                actual = MVEL.evalToString(assertion.actualValue, context);
                return actual.contains((String) assertion.expectedValue) ? "" : assertion.failMessage;
            case assertNotEmpty:
                context = SimpleJson.fromJson(response.getResponseBody().toString(), Map.class);
                actual = MVEL.evalToString(assertion.actualValue, context);
                return actual.length() > 0 ? "" : assertion.failMessage;
            case assertElementExists:
                Document doc = Jsoup.parse(response.getResponseBody().toString());
                Element element = doc.selectFirst(assertion.actualValue);
                return element != null? "" : assertion.failMessage;
            case assertElementTextContains:
                doc = Jsoup.parse(response.getResponseBody().toString());
                element = doc.selectFirst(assertion.actualValue);
                return element.text().contains((String)assertion.expectedValue)? "" : assertion.failMessage;
            default:
                System.out.println("Could not determine result");
                break;
        }
        return "";
    }
}
