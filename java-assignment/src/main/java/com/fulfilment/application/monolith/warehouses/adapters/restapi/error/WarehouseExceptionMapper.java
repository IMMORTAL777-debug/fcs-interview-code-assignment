package com.fulfilment.application.monolith.warehouses.adapters.restapi.error;

import com.fulfilment.application.monolith.common.rest.ApiErrorResponse;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseErrorCode;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.time.Instant;

@Provider
public class WarehouseExceptionMapper implements ExceptionMapper<WarehouseException> {

  @Override
  public Response toResponse(WarehouseException exception) {
    int status = statusFor(exception.errorCode());
    var error =
        new ApiErrorResponse(
            exception.errorCode().name(), exception.getMessage(), status, Instant.now());

    return Response.status(status).type(MediaType.APPLICATION_JSON_TYPE).entity(error).build();
  }

  private int statusFor(WarehouseErrorCode errorCode) {
    return switch (errorCode) {
      case WAREHOUSE_NOT_FOUND -> Response.Status.NOT_FOUND.getStatusCode();
      case DUPLICATE_BUSINESS_UNIT_CODE,
          LOCATION_WAREHOUSE_LIMIT_REACHED,
          LOCATION_CAPACITY_EXCEEDED -> Response.Status.CONFLICT.getStatusCode();
      default -> Response.Status.BAD_REQUEST.getStatusCode();
    };
  }
}
