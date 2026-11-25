package com.minierp.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minierp.infrastructure.persistence.entity.SupplierJpaEntity;

/**
 * Spring Data repository for suppliers — internal to the infrastructure layer;
 * the application talks to {@code SupplierRepositoryPort} instead.
 */
public interface SpringDataSupplierRepository extends JpaRepository<SupplierJpaEntity, Long> {

    Optional<SupplierJpaEntity> findByCode(String code);

    boolean existsByCode(String code);

    @Query("select s from SupplierJpaEntity s where (:active is null or s.active = :active)")
    Page<SupplierJpaEntity> findPage(@Param("active") Boolean active, Pageable pageable);
}
