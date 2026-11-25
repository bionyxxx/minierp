package com.minierp.domain.port.out;

import java.util.Optional;

import com.minierp.domain.model.purchase.PurchaseOrder;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;

/**
 * Driven port: Purchase Order persistence needed by the application.
 */
public interface PurchaseOrderRepositoryPort {

    Optional<PurchaseOrder> findById(Long id);

    PurchaseOrder save(PurchaseOrder purchaseOrder);

    PageResult<PurchaseOrder> findPage(DocumentStatus status, Long supplierId, PageQuery query);
}
