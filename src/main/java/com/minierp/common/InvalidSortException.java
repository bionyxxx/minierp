package com.minierp.common;

import java.util.Set;

/**
 * A sort property outside of the endpoint's allowlist was requested.
 * Mapped to HTTP 400.
 */
public class InvalidSortException extends RuntimeException {

    private final String property;
    private final Set<String> allowedProperties;

    public InvalidSortException(String property, Set<String> allowedProperties) {
        super(ProblemMessages.INVALID_SORT_FORMAT.formatted(property, String.join(", ", allowedProperties)));
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
