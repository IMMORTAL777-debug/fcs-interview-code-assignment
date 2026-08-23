package com.fulfilment.application.monolith.stores;

public record StoreChanged(Operation operation, Store store) {

  public enum Operation {
    CREATED,
    UPDATED
  }

  public static StoreChanged created(Store store) {
    return new StoreChanged(Operation.CREATED, store);
  }

  public static StoreChanged updated(Store store) {
    return new StoreChanged(Operation.UPDATED, store);
  }
}
