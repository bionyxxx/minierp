package com.minierp.domain.port.in;

import com.minierp.domain.model.purchase.PurchaseOrder;
import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;

/**
 * Purchase Order use cases offered by the application.
 */
public interface PurchaseOrderUseCase {

    PageResult<PurchaseOrder> list(AuthenticatedUser actor, DocumentStatus status, Long supplierId, PageQuery query);

    PurchaseOrder get(AuthenticatedUser actor, Long id);

    PurchaseOrder create(AuthenticatedUser actor, PurchaseOrderCommand command);

    PurchaseOrder approve(AuthenticatedUser actor, Long id);

    PurchaseOrder reject(AuthenticatedUser actor, Long id, String reason);
}
