package com.minierp.domain.model.shared;

/**
 * Login failed: unknown user or wrong password. Mapped to HTTP 401.
 */
public class AuthenticationFailedException extends RuntimeException {

    public AuthenticationFailedException(String message) {
        super(message);
    }
}
