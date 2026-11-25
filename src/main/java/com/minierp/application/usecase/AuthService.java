package com.minierp.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minierp.domain.model.shared.AuthenticationFailedException;
import com.minierp.domain.model.user.User;
import com.minierp.domain.port.in.AuthUseCase;
import com.minierp.domain.port.in.LoginCommand;
import com.minierp.domain.port.in.LoginResult;
import com.minierp.domain.port.out.CredentialPort;
import com.minierp.domain.port.out.TokenPort;
import com.minierp.domain.port.out.UserRepositoryPort;

/**
 * Login use case: validates credentials and issues a JWT.
 */
@Service
public class AuthService implements AuthUseCase {

    private static final String LOGIN_FAILED_MESSAGE = "Username atau password salah";

    private final CredentialPort credentialPort;
    private final UserRepositoryPort userRepository;
    private final TokenPort tokenPort;

    public AuthService(CredentialPort credentialPort, UserRepositoryPort userRepository, TokenPort tokenPort) {
        this.credentialPort = credentialPort;
        this.userRepository = userRepository;
        this.tokenPort = tokenPort;
    }

    @Transactional(readOnly = true)
    @Override
    public LoginResult login(LoginCommand command) {
        if (!credentialPort.verify(command.username(), command.password())) {
            throw new AuthenticationFailedException(LOGIN_FAILED_MESSAGE);
        }
        User user = userRepository.findByUsername(command.username())
                .orElseThrow(() -> new AuthenticationFailedException(LOGIN_FAILED_MESSAGE));
        TokenPort.IssuedToken issued = tokenPort.issue(user);
        return new LoginResult(issued.token(), issued.tokenType(), issued.expiresAt(), user);
    }
}
