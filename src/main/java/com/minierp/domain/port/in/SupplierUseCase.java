package com.minierp.domain.port.in;

import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.model.supplier.Supplier;

/**
 * Supplier master data use cases offered by the application.
 */
public interface SupplierUseCase {

    PageResult<Supplier> list(AuthenticatedUser actor, Boolean active, PageQuery query);

    Supplier get(AuthenticatedUser actor, Long id);

    Supplier create(AuthenticatedUser actor, SupplierCommand command);

    Supplier update(AuthenticatedUser actor, Long id, SupplierCommand command);

    void delete(AuthenticatedUser actor, Long id);
}
