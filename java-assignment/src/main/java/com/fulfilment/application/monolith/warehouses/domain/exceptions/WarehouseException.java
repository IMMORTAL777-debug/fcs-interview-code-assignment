package com.fulfilment.application.monolith.warehouses.domain.exceptions;

import java.util.Objects;

public class WarehouseException extends RuntimeException {

  private final WarehouseErrorCode errorCode;

  public WarehouseException(WarehouseErrorCode errorCode, String message) {
    super(message);
    this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
  }

  public WarehouseErrorCode errorCode() {
    return errorCode;
  }
}
