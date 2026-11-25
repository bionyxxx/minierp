package com.minierp.domain.model.shared;

/**
 * Requested entity does not exist. Mapped to HTTP 404.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String resource, Object id) {
        super("%s dengan id %s tidak ditemukan".formatted(resource, id));
    }

    public NotFoundException(String message) {
        super(message);
    }
}
