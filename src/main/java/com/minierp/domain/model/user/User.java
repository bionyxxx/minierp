package com.minierp.domain.model.user;

import java.time.Instant;

/**
 * Application user with login credentials. Pure domain model.
 */
public class User {

    private Long id;
    private String username;
    private String password;
    private String fullName;
    private Role role;
    private boolean active;
    private Instant createdAt;

    private User() {
    }

    public static User create(String username, String password, String fullName, Role role) {
        User user = new User();
        user.username = username;
        user.password = password;
        user.fullName = fullName;
        user.role = role;
        user.active = true;
        return user;
    }

    /**
     * Reconstitutes an existing user loaded from persistence.
     */
    public static User reconstitute(Long id, String username, String password, String fullName, Role role,
            boolean active, Instant createdAt) {
        User user = new User();
        user.id = id;
        user.username = username;
        user.password = password;
        user.fullName = fullName;
        user.role = role;
        user.active = active;
        user.createdAt = createdAt;
        return user;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
