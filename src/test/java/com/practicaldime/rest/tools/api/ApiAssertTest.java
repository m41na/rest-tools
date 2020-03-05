package com.practicaldime.rest.tools.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practicaldime.common.entity.rest.ApiAssert;
import org.hamcrest.core.StringContains;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ApiAssertTest {

    @Test
    public void testDeserializeString() throws IOException {
        String[] input = {
                "{\"actualValue\":\"sdsd\"",
                "\"assertType\":\"assertNotEmpty\"",
                "\"expectedValue\":\"dsds\"",
                "\"failMessage\":\"sdsds\"",
                "\"id\":\"\"}"
        };
        String json = String.join(",", input);

        ObjectMapper mapper = new ObjectMapper();
        //ApiAssert res = mapper.readValue(json, ApiAssert.class);
        ApiAssert res = mapper.readValue(json, new TypeReference<ApiAssert<String>>() {
        });
        assertEquals("Expecting 'sdsd' for actual value", "sdsd", res.getActualValue());
    }

    @Test
    public void testSerializeString() throws IOException {
        ApiAssert api = new ApiAssert();
        api.setActualValue("sssss");
        api.setExpectedValue(1234);
        api.setExecute(Boolean.TRUE);
        api.setAssertType(ApiAssert.AssertType.assertEquals);
        api.setFailMessage("wow");

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(api);
        assertThat("Should contains'wow'", json, new StringContains("wow"));
    }
}
