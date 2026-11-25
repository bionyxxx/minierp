package com.minierp.domain.port.in;

import java.time.Instant;

import com.minierp.domain.model.user.User;

/**
 * Outcome of a successful login: an access token plus the authenticated user.
 */
public record LoginResult(String token, String tokenType, Instant expiresAt, User user) {
}
