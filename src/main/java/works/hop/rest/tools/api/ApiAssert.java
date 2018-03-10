package works.hop.rest.tools.api;

public class ApiAssert<T extends Comparable<T>> {
    
    public AssertType assertType;
    public String failMessage;
    public T expectedValue;
    public String actualValue;    
    public static enum AssertType {
        assertContains, assertEquals, assertNotEmpty, assertElementExists, assertElementTextContains
    }
}
