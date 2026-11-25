package com.minierp.infrastructure.persistence.mapper;

import com.minierp.domain.model.user.User;
import com.minierp.infrastructure.persistence.entity.UserJpaEntity;

/**
 * Domain ⇄ JPA mapping for the user aggregate. Every column is copied so a
 * merge can never wipe state Hibernate has already persisted.
 */
public final class UserMapper {

    private UserMapper() {
    }

    public static UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId());
        entity.setUsername(user.getUsername());
        entity.setPassword(user.getPassword());
        entity.setFullName(user.getFullName());
        entity.setRole(user.getRole());
        entity.setActive(user.isActive());
        entity.setCreatedAt(user.getCreatedAt());
        return entity;
    }

    public static User toDomain(UserJpaEntity entity) {
        return User.reconstitute(entity.getId(), entity.getUsername(), entity.getPassword(), entity.getFullName(),
                entity.getRole(), entity.isActive(), entity.getCreatedAt());
    }
}
