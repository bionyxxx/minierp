package com.minierp.domain.port.out;

import java.util.Optional;

import com.minierp.domain.model.user.User;

/**
 * Driven port: user persistence needed by the application.
 */
public interface UserRepositoryPort {

    Optional<User> findByUsername(String username);

    User save(User user);
}
