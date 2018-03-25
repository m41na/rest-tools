package works.hop.rest.tools.api;

public class ApiAssert<T extends Comparable<T>> {
     
    public static enum AssertType {
        assertContains, assertEquals, assertNotEmpty, assertElementExists, assertElementTextContains
    }
    private Long id;
    private AssertType assertType;
    private String failMessage;
    private T expectedValue;
    private String actualValue;   
    private Boolean execute = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AssertType getAssertType() {
        return assertType;
    }

    public void setAssertType(AssertType assertType) {
        this.assertType = assertType;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage;
    }

    public T getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(T expectedValue) {
        this.expectedValue = expectedValue;
    }

    public String getActualValue() {
        return actualValue;
    }

    public void setActualValue(String actualValue) {
        this.actualValue = actualValue;
    }

    public Boolean getExecute() {
        return execute;
    }

    public void setExecute(Boolean execute) {
        this.execute = execute;
    }
}
