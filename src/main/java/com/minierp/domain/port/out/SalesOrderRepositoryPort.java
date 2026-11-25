package com.minierp.domain.port.out;

import java.util.Optional;

import com.minierp.domain.model.sales.SalesOrder;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;

/**
 * Driven port: Sales Order persistence needed by the application.
 */
public interface SalesOrderRepositoryPort {

    Optional<SalesOrder> findById(Long id);

    SalesOrder save(SalesOrder salesOrder);

    PageResult<SalesOrder> findPage(DocumentStatus status, Long customerId, PageQuery query);
}
