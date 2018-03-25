package works.hop.rest.tools.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.hamcrest.Matcher;
import org.hamcrest.core.StringContains;
import org.junit.Test;
import static org.junit.Assert.*;

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
        ApiAssert res = mapper.readValue(json, new TypeReference<ApiAssert<String>>(){});
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
