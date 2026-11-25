package com.minierp.domain.port.out;

import java.util.Optional;

import com.minierp.domain.model.customer.Customer;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;

/**
 * Driven port: customer persistence needed by the application.
 */
public interface CustomerRepositoryPort {

    Optional<Customer> findById(Long id);

    Optional<Customer> findByCode(String code);

    boolean existsByCode(String code);

    Customer save(Customer customer);

    PageResult<Customer> findPage(Boolean active, PageQuery query);
}
