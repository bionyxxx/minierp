package com.minierp.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.infrastructure.persistence.entity.SalesOrderJpaEntity;

/**
 * Spring Data repository for sales orders — internal to the infrastructure
 * layer; the application talks to {@code SalesOrderRepositoryPort} instead.
 */
public interface SpringDataSalesOrderRepository extends JpaRepository<SalesOrderJpaEntity, Long> {

    Optional<SalesOrderJpaEntity> findBySoNumber(String soNumber);

    @Query("select so from SalesOrderJpaEntity so"
            + " where (:status is null or so.status = :status)"
            + " and (:customerId is null or so.customer.id = :customerId)")
    Page<SalesOrderJpaEntity> findPage(@Param("status") DocumentStatus status,
            @Param("customerId") Long customerId, Pageable pageable);
}
