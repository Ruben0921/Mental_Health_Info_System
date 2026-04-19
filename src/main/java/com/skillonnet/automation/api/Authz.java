package com.skillonnet.automation.api;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

public final class Authz {

    private Authz() {
    }

    public static void require(SecurityContext sc, String... roles) {
        for (String r : roles) {
            if (sc.isUserInRole(r)) {
                return;
            }
        }
        throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiError("forbidden"))
                .build());
    }
}
