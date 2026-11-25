package com.minierp.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minierp.infrastructure.persistence.entity.CustomerJpaEntity;

/**
 * Spring Data repository for customers — internal to the infrastructure layer;
 * the application talks to {@code CustomerRepositoryPort} instead.
 */
public interface SpringDataCustomerRepository extends JpaRepository<CustomerJpaEntity, Long> {

    Optional<CustomerJpaEntity> findByCode(String code);

    boolean existsByCode(String code);

    @Query("select c from CustomerJpaEntity c where (:active is null or c.active = :active)")
    Page<CustomerJpaEntity> findPage(@Param("active") Boolean active, Pageable pageable);
}
