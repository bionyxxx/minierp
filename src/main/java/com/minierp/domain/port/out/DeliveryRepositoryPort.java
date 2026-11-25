package com.minierp.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.minierp.domain.model.sales.Delivery;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;

/**
 * Driven port: Delivery persistence needed by the application.
 */
public interface DeliveryRepositoryPort {

    Optional<Delivery> findById(Long id);

    List<Delivery> findBySalesOrderId(Long salesOrderId);

    Delivery save(Delivery delivery);

    PageResult<Delivery> findPage(Long salesOrderId, DocumentStatus status, PageQuery query);
}
