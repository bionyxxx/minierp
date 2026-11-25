package com.minierp.domain.model.shared;

/**
 * The document is not in a state that allows the requested transition
 * (e.g. approving an already rejected PO). Mapped to HTTP 409.
 */
public class InvalidStateException extends RuntimeException {

    public InvalidStateException(String message) {
        super(message);
    }
}
