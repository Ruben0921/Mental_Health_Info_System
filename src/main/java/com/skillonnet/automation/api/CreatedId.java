package com.skillonnet.automation.api;

public class CreatedId {

    private int requestId;

    public CreatedId() {
    }

    public CreatedId(int requestId) {
        this.requestId = requestId;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }
}
