package com.minierp.domain.model.shared;

import com.minierp.domain.model.user.Role;

/**
 * Identity of the user performing the current use case.
 */
public record AuthenticatedUser(String username, Role role) {

    public boolean isStaff() {
        return role == Role.STAFF;
    }
}
