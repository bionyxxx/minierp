package com.minierp.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minierp.infrastructure.persistence.entity.WarehouseJpaEntity;

/**
 * Spring Data repository for warehouses — internal to the infrastructure layer;
 * the application talks to {@code WarehouseRepositoryPort} instead.
 */
public interface SpringDataWarehouseRepository extends JpaRepository<WarehouseJpaEntity, Long> {

    Optional<WarehouseJpaEntity> findByCode(String code);

    boolean existsByCode(String code);

    @Query("select w from WarehouseJpaEntity w where (:active is null or w.active = :active)")
    Page<WarehouseJpaEntity> findPage(@Param("active") Boolean active, Pageable pageable);
}
