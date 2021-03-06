package com.practicaldime.rest.tools.api;

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
    private String result;

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

	@SuppressWarnings("unchecked")
	public void setExpectedValue(Object expectedValue) {
        this.expectedValue = (T)expectedValue;
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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}