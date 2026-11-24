package com.minierp.common;

import com.minierp.user.Role;

/**
 * Identity of the user performing the current use case.
 */
public record AuthenticatedUser(String username, Role role) {

    public boolean isStaff() {
        return role == Role.STAFF;
    }
}
