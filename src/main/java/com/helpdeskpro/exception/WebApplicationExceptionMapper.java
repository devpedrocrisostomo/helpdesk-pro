package com.helpdeskpro.exception;

import com.helpdeskpro.dto.error.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
    @Override
    public Response toResponse(WebApplicationException exception) {
        int status = exception.getResponse().getStatus();
        String message = exception.getMessage() == null || exception.getMessage().isBlank()
                ? "Erro na requisicao."
                : exception.getMessage();

        return Response.status(status)
                .entity(new ErrorResponse(status, message))
                .build();
    }
}
