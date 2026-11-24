package com.minierp.common;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minierp.config.MinierpProperties;

/**
 * Builds RFC 9457 {@code type} URIs from problem slugs against the configured
 * documentation base URI, so every error type resolves to the problem catalog
 * ({@code GET /api/problems/{slug}}).
 */
@Component
public class ProblemTypeUris {

    private final URI baseUri;

    @Autowired
    public ProblemTypeUris(MinierpProperties properties) {
        this(properties.api().problemBaseUri());
    }

    ProblemTypeUris(String baseUri) {
        String normalized = baseUri.endsWith("/") ? baseUri : baseUri + "/";
        this.baseUri = URI.create(normalized);
    }

    public URI uri(String slug) {
        return baseUri.resolve(slug);
    }
}
