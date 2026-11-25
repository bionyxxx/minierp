package com.minierp.domain.model.shared;

import java.util.Set;

/**
 * A sort property outside of the endpoint's allowlist was requested.
 * Mapped to HTTP 400.
 */
public class InvalidSortException extends RuntimeException {

    /** Format for an invalid sort property; arguments are the property and the allowlist. */
    private static final String MESSAGE_FORMAT =
            "Properti sort '%s' tidak dikenali. Properti yang diizinkan: %s";

    private final String property;
    private final Set<String> allowedProperties;

    public InvalidSortException(String property, Set<String> allowedProperties) {
        super(MESSAGE_FORMAT.formatted(property, String.join(", ", allowedProperties)));
        this.property = property;
        this.allowedProperties = Set.copyOf(allowedProperties);
    }

    public String getProperty() {
        return property;
    }

    public Set<String> getAllowedProperties() {
        return allowedProperties;
    }
}
