package com.minierp.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minierp.infrastructure.persistence.entity.ItemJpaEntity;

/**
 * Spring Data repository for items — internal to the infrastructure layer;
 * the application talks to {@code ItemRepositoryPort} instead.
 */
public interface SpringDataItemRepository extends JpaRepository<ItemJpaEntity, Long> {

    Optional<ItemJpaEntity> findBySku(String sku);

    boolean existsBySku(String sku);

    @Query("select i from ItemJpaEntity i where (:active is null or i.active = :active)")
    Page<ItemJpaEntity> findPage(@Param("active") Boolean active, Pageable pageable);
}
