package com.minierp.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.minierp.user.Role;

/**
 * Resolves the authenticated user from the security context.
 * Controllers pass the result into application services so that
 * services stay free of security-framework types.
 */
@Component
public class CurrentUser {

    public AuthenticatedUser get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("Tidak ada user terautentikasi pada konteks keamanan");
        }
        String username = jwt.getSubject();
        Role role = Role.valueOf(jwt.getClaimAsString("role"));
        return new AuthenticatedUser(username, role);
    }
}
