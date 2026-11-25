package com.minierp.domain.port.out;

import java.time.Instant;

import com.minierp.domain.model.user.User;

/**
 * Driven port: issues signed access tokens for authenticated users.
 */
public interface TokenPort {

    IssuedToken issue(User user);

    record IssuedToken(String token, String tokenType, Instant expiresAt) {
    }
}
