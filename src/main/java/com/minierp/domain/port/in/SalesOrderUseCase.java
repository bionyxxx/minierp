package com.minierp.domain.port.in;

import com.minierp.domain.model.sales.SalesOrder;
import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;

/**
 * Sales Order use cases offered by the application.
 */
public interface SalesOrderUseCase {

    PageResult<SalesOrder> list(AuthenticatedUser actor, DocumentStatus status, Long customerId, PageQuery query);

    SalesOrder get(AuthenticatedUser actor, Long id);

    SalesOrder create(AuthenticatedUser actor, SalesOrderCommand command);

    SalesOrder approve(AuthenticatedUser actor, Long id);

    SalesOrder reject(AuthenticatedUser actor, Long id, String reason);
}
