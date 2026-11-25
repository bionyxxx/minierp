package com.minierp.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.infrastructure.persistence.entity.PurchaseOrderJpaEntity;

/**
 * Spring Data repository for purchase orders — internal to the
 * infrastructure layer; the application talks to
 * {@code PurchaseOrderRepositoryPort} instead.
 */
public interface SpringDataPurchaseOrderRepository extends JpaRepository<PurchaseOrderJpaEntity, Long> {

    Optional<PurchaseOrderJpaEntity> findByPoNumber(String poNumber);

    @Query("select po from PurchaseOrderJpaEntity po"
            + " where (:status is null or po.status = :status)"
            + " and (:supplierId is null or po.supplier.id = :supplierId)")
    Page<PurchaseOrderJpaEntity> findPage(@Param("status") DocumentStatus status,
            @Param("supplierId") Long supplierId, Pageable pageable);
}
