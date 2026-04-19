package com.skillonnet.automation.api;

import com.skillonnet.automation.db.DatabaseException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DatabaseExceptionMapper implements ExceptionMapper<DatabaseException> {

    @Override
    public Response toResponse(DatabaseException e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiError(e.getMessage()))
                .build();
    }
}
