package com.minierp.domain.port.in;

import com.minierp.domain.model.customer.Customer;
import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;

/**
 * Customer master data use cases offered by the application.
 */
public interface CustomerUseCase {

    PageResult<Customer> list(AuthenticatedUser actor, Boolean active, PageQuery query);

    Customer get(AuthenticatedUser actor, Long id);

    Customer create(AuthenticatedUser actor, CustomerCommand command);

    Customer update(AuthenticatedUser actor, Long id, CustomerCommand command);

    void delete(AuthenticatedUser actor, Long id);
}
