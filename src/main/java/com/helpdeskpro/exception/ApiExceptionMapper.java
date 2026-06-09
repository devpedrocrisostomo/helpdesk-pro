package com.helpdeskpro.exception;

import com.helpdeskpro.dto.error.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {
    @Override
    public Response toResponse(ApiException exception) {
        ErrorResponse error = new ErrorResponse(
                exception.getStatus().getStatusCode(),
                exception.getMessage()
        );

        return Response.status(exception.getStatus())
                .entity(error)
                .build();
    }
}
