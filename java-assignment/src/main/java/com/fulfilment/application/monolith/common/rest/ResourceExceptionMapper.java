package com.fulfilment.application.monolith.common.rest;

import com.fulfilment.application.monolith.common.exceptions.ResourceErrorCode;
import com.fulfilment.application.monolith.common.exceptions.ResourceException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.time.Instant;

@Provider
public class ResourceExceptionMapper implements ExceptionMapper<ResourceException> {

  @Override
  public Response toResponse(ResourceException exception) {
    int status = statusFor(exception.errorCode());
    var error =
        new ApiErrorResponse(
            exception.errorCode().name(), exception.getMessage(), status, Instant.now());

    return Response.status(status).type(MediaType.APPLICATION_JSON_TYPE).entity(error).build();
  }

  private int statusFor(ResourceErrorCode errorCode) {
    return switch (errorCode) {
      case PRODUCT_NOT_FOUND, STORE_NOT_FOUND -> Response.Status.NOT_FOUND.getStatusCode();
      default -> 422;
    };
  }
}
