package com.minierp.purchase;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minierp.common.DocumentStatus;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    @Query(value = "select nextval('purchase_order_number_seq')", nativeQuery = true)
    Long nextNumberValue();

    @Query("select po from PurchaseOrder po"
            + " where (:status is null or po.status = :status)"
            + " and (:supplierId is null or po.supplier.id = :supplierId)")
    Page<PurchaseOrder> findPage(@Param("status") DocumentStatus status, @Param("supplierId") Long supplierId,
            Pageable pageable);
}
