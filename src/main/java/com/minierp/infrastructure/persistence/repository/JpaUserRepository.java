package com.minierp.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.minierp.domain.model.user.User;
import com.minierp.domain.port.out.UserRepositoryPort;
import com.minierp.infrastructure.persistence.entity.UserJpaEntity;
import com.minierp.infrastructure.persistence.mapper.UserMapper;

/**
 * Persistence adapter — implements the user port using JPA.
 * The domain layer never sees JPA, only this adapter does.
 */
@Component
public class JpaUserRepository implements UserRepositoryPort {

    private final SpringDataUserRepository repository;

    public JpaUserRepository(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username).map(UserMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity saved = repository.save(UserMapper.toEntity(user));
        return UserMapper.toDomain(saved);
    }
}
