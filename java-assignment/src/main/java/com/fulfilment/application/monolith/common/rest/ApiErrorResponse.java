package com.fulfilment.application.monolith.common.rest;

import java.time.Instant;

public record ApiErrorResponse(String code, String message, int status, Instant timestamp) {}
