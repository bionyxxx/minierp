package com.minierp.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.infrastructure.persistence.entity.DeliveryJpaEntity;

/**
 * Spring Data repository for deliveries — internal to the infrastructure
 * layer; the application talks to {@code DeliveryRepositoryPort} instead.
 */
public interface SpringDataDeliveryRepository extends JpaRepository<DeliveryJpaEntity, Long> {

    List<DeliveryJpaEntity> findBySalesOrderId(Long salesOrderId);

    @Query("select d from DeliveryJpaEntity d"
            + " where (:salesOrderId is null or d.salesOrder.id = :salesOrderId)"
            + " and (:status is null or d.status = :status)")
    Page<DeliveryJpaEntity> findPage(@Param("salesOrderId") Long salesOrderId,
            @Param("status") DocumentStatus status, Pageable pageable);
}
