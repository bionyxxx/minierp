package com.minierp.infrastructure.web.error;

/**
 * One invalid request field, exposed under the {@code errors} extension member
 * of a validation problem detail.
 */
public record FieldViolation(String field, String message) {
}
