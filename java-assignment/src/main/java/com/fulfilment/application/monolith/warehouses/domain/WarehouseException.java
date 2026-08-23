package com.fulfilment.application.monolith.warehouses.domain;

public class WarehouseException extends RuntimeException {

  public enum Reason {
    INVALID,
    NOT_FOUND,
    CONFLICT
  }

  private final Reason reason;

  public WarehouseException(Reason reason, String message) {
    super(message);
    this.reason = reason;
  }

  public Reason reason() {
    return reason;
  }

  public static WarehouseException invalid(String message) {
    return new WarehouseException(Reason.INVALID, message);
  }

  public static WarehouseException notFound(String message) {
    return new WarehouseException(Reason.NOT_FOUND, message);
  }

  public static WarehouseException conflict(String message) {
    return new WarehouseException(Reason.CONFLICT, message);
  }
}
