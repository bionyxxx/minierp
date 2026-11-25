package com.minierp.infrastructure.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.minierp.domain.port.out.CredentialPort;

/**
 * Credential verification adapter — delegates to the Spring Security
 * {@link AuthenticationManager}. Failure reasons stay inside this adapter so
 * callers cannot tell unknown user, wrong password or disabled account apart.
 */
@Component
public class AuthenticationAdapter implements CredentialPort {

    private final AuthenticationManager authenticationManager;

    public AuthenticationAdapter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public boolean verify(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            return true;
        }
        catch (AuthenticationException ex) {
            return false;
        }
    }
}
