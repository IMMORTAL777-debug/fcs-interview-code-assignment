package com.fulfilment.application.monolith.common.exceptions;

import java.util.Objects;

public class ResourceException extends RuntimeException {

  private final ResourceErrorCode errorCode;

  public ResourceException(ResourceErrorCode errorCode, String message) {
    super(message);
    this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
  }

  public ResourceErrorCode errorCode() {
    return errorCode;
  }
}
