package com.minierp.purchase;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minierp.common.DocumentStatus;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {

    List<GoodsReceipt> findByPurchaseOrderId(Long purchaseOrderId);

    @Query(value = "select nextval('goods_receipt_number_seq')", nativeQuery = true)
    Long nextNumberValue();

    @Query("select gr from GoodsReceipt gr"
            + " where (:purchaseOrderId is null or gr.purchaseOrder.id = :purchaseOrderId)"
            + " and (:status is null or gr.status = :status)")
    Page<GoodsReceipt> findPage(@Param("purchaseOrderId") Long purchaseOrderId,
            @Param("status") DocumentStatus status, Pageable pageable);
}
