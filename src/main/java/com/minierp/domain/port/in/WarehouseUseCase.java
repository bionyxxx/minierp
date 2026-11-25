package com.minierp.domain.port.in;

import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.model.warehouse.Warehouse;

/**
 * Warehouse master data use cases offered by the application.
 */
public interface WarehouseUseCase {

    PageResult<Warehouse> list(AuthenticatedUser actor, Boolean active, PageQuery query);

    Warehouse get(AuthenticatedUser actor, Long id);

    Warehouse create(AuthenticatedUser actor, WarehouseCommand command);

    Warehouse update(AuthenticatedUser actor, Long id, WarehouseCommand command);

    void delete(AuthenticatedUser actor, Long id);
}
