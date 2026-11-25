package com.minierp.domain.port.out;

/**
 * Driven port: verifies end-user credentials against the configured identity
 * store. Implementations answer with a plain boolean so callers cannot tell
 * unknown user, wrong password or disabled account apart.
 */
public interface CredentialPort {

    boolean verify(String username, String password);
}
