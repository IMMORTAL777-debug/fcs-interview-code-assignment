package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class WarehouseExceptionMapper implements ExceptionMapper<WarehouseException> {

  @Override
  public Response toResponse(WarehouseException exception) {
    int status =
        switch (exception.reason()) {
          case INVALID -> 400;
          case NOT_FOUND -> 404;
          case CONFLICT -> 409;
        };
    return Response.status(status)
        .entity(Map.of("code", status, "error", exception.getMessage()))
        .build();
  }
}
