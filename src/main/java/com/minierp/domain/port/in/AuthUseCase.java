package com.minierp.domain.port.in;

/**
 * Authentication use cases offered by the application.
 */
public interface AuthUseCase {

    LoginResult login(LoginCommand command);
}
