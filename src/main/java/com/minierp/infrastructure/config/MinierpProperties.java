package com.minierp.infrastructure.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the {@code minierp.*} configuration properties.
 */
@ConfigurationProperties(prefix = "minierp")
public record MinierpProperties(Security security, Approval approval, Seed seed, Api api) {

    public record Security(Jwt jwt) {
    }

    public record Jwt(String secret, Duration expiration) {
    }

    public record Approval(long thresholdAmount) {
    }

    public record Seed(boolean enabled) {
    }

    /** Base URI for RFC 9457 problem {@code type} links (points at the problem catalog). */
    public record Api(String problemBaseUri) {
    }
}
