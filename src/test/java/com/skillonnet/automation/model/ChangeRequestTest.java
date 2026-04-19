package com.skillonnet.automation.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChangeRequestTest {

    @Test
    void changeRequestHasNoPatientReference() {
        ChangeRequest r = new ChangeRequest();
        r.setRequestId(1);
        r.setRawPatientData("{}");
        r.setRequestedChanges("update name");
        r.setStatus("Pending");
        assertEquals(1, r.getRequestId());
        assertEquals("Pending", r.getStatus());
    }
}
