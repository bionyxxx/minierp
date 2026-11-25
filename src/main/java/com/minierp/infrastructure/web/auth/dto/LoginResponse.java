package com.minierp.infrastructure.web.auth.dto;

import java.time.Instant;

import com.minierp.domain.port.in.LoginResult;

public record LoginResponse(
        String token,
        String tokenType,
        Instant expiresAt,
        String username,
        String fullName,
        String role) {

    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(result.token(), result.tokenType(), result.expiresAt(),
                result.user().getUsername(), result.user().getFullName(), result.user().getRole().name());
    }
}
