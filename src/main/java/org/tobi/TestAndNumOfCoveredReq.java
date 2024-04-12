package org.tobi;

public class TestAndNumOfCoveredReq {
    private final String[] test;
    private final int numOfCoveredReq;

    public TestAndNumOfCoveredReq(String[] test, int numOfCoveredReq) {
        this.test = test;
        this.numOfCoveredReq = numOfCoveredReq;
    }

    public String[] getTest() {
        return test;
    }

    public int getNumOfCoveredReq() {
        return numOfCoveredReq;
    }
}
