package com.minierp.sales;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minierp.common.DocumentStatus;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    Optional<SalesOrder> findBySoNumber(String soNumber);

    @Query(value = "select nextval('sales_order_number_seq')", nativeQuery = true)
    Long nextNumberValue();

    @Query("select so from SalesOrder so"
            + " where (:status is null or so.status = :status)"
            + " and (:customerId is null or so.customer.id = :customerId)")
    Page<SalesOrder> findPage(@Param("status") DocumentStatus status, @Param("customerId") Long customerId,
            Pageable pageable);
}
