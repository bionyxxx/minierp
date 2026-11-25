package com.minierp.domain.port.in;

/**
 * Credentials submitted for login.
 */
public record LoginCommand(String username, String password) {
}
