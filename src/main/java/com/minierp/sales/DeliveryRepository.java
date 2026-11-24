package com.minierp.sales;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minierp.common.DocumentStatus;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    List<Delivery> findBySalesOrderId(Long salesOrderId);

    @Query(value = "select nextval('delivery_number_seq')", nativeQuery = true)
    Long nextNumberValue();

    @Query("select d from Delivery d"
            + " where (:salesOrderId is null or d.salesOrder.id = :salesOrderId)"
            + " and (:status is null or d.status = :status)")
    Page<Delivery> findPage(@Param("salesOrderId") Long salesOrderId, @Param("status") DocumentStatus status,
            Pageable pageable);
}
