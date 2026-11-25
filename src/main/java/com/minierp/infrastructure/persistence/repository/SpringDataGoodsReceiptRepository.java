package com.minierp.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.infrastructure.persistence.entity.GoodsReceiptJpaEntity;

/**
 * Spring Data repository for goods receipts — internal to the infrastructure
 * layer; the application talks to {@code GoodsReceiptRepositoryPort} instead.
 */
public interface SpringDataGoodsReceiptRepository extends JpaRepository<GoodsReceiptJpaEntity, Long> {

    List<GoodsReceiptJpaEntity> findByPurchaseOrderId(Long purchaseOrderId);

    @Query("select gr from GoodsReceiptJpaEntity gr"
            + " where (:purchaseOrderId is null or gr.purchaseOrder.id = :purchaseOrderId)"
            + " and (:status is null or gr.status = :status)")
    Page<GoodsReceiptJpaEntity> findPage(@Param("purchaseOrderId") Long purchaseOrderId,
            @Param("status") DocumentStatus status, Pageable pageable);
}
