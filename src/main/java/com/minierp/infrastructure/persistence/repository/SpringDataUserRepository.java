package com.minierp.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minierp.infrastructure.persistence.entity.UserJpaEntity;

/**
 * Spring Data repository for users — internal to the infrastructure layer;
 * the application talks to {@code UserRepositoryPort} instead.
 */
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByUsername(String username);
}
