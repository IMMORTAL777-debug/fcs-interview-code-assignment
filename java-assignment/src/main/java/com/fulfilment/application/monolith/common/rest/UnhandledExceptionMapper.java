package com.fulfilment.application.monolith.common.rest;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.time.Instant;
import org.jboss.logging.Logger;

@Provider
public class UnhandledExceptionMapper implements ExceptionMapper<Exception> {

  private static final Logger LOGGER = Logger.getLogger(UnhandledExceptionMapper.class);

  @Override
  public Response toResponse(Exception exception) {
    if (exception instanceof WebApplicationException webApplicationException) {
      int status = webApplicationException.getResponse().getStatus();
      var error =
          new ApiErrorResponse(
              "HTTP_" + status, webApplicationException.getMessage(), status, Instant.now());
      return Response.status(status).type(MediaType.APPLICATION_JSON_TYPE).entity(error).build();
    }

    LOGGER.error("Unhandled request failure", exception);
    int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
    var error =
        new ApiErrorResponse(
            "INTERNAL_SERVER_ERROR", "An unexpected error occurred.", status, Instant.now());

    return Response.status(status).type(MediaType.APPLICATION_JSON_TYPE).entity(error).build();
  }
}
