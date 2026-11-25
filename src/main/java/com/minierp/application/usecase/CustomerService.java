package com.minierp.application.usecase;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.customer.Customer;
import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.DuplicateResourceException;
import com.minierp.domain.model.shared.NotFoundException;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.model.shared.SortField;
import com.minierp.domain.port.in.CustomerCommand;
import com.minierp.domain.port.in.CustomerUseCase;
import com.minierp.domain.port.out.CustomerRepositoryPort;

/**
 * Master data CRUD for customers.
 */
@Service
public class CustomerService implements CustomerUseCase {

    private static final Set<String> SORT_PROPERTIES =
            Set.of("id", "code", "name", "address", "phone", "email", "active");

    private static final SortField DEFAULT_SORT = SortField.asc("code");

    private final CustomerRepositoryPort customerRepository;

    public CustomerService(CustomerRepositoryPort customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public PageResult<Customer> list(AuthenticatedUser actor, Boolean active, PageQuery query) {
        return customerRepository.findPage(active, Pagination.normalize(query, SORT_PROPERTIES, DEFAULT_SORT));
    }

    @Transactional(readOnly = true)
    @Override
    public Customer get(AuthenticatedUser actor, Long id) {
        return findOrThrow(id);
    }

    @Transactional
    @Override
    public Customer create(AuthenticatedUser actor, CustomerCommand command) {
        ensureCodeAvailable(command.code());
        Customer customer = Customer.create(command.code(), command.name(), command.address(),
                command.phone(), command.email());
        return customerRepository.save(customer);
    }

    @Transactional
    @Override
    public Customer update(AuthenticatedUser actor, Long id, CustomerCommand command) {
        Customer customer = findOrThrow(id);
        if (!customer.getCode().equals(command.code())) {
            ensureCodeAvailable(command.code());
        }
        customer.update(command.code(), command.name(), command.address(), command.phone(), command.email());
        return customerRepository.save(customer);
    }

    /**
     * Soft delete: the customer stays referenced by documents but is deactivated.
     */
    @Transactional
    @Override
    public void delete(AuthenticatedUser actor, Long id) {
        Customer customer = findOrThrow(id);
        customer.deactivate();
        customerRepository.save(customer);
    }

    private Customer findOrThrow(Long id) {
        return customerRepository.findById(id).orElseThrow(() -> new NotFoundException("Customer", id));
    }

    private void ensureCodeAvailable(String code) {
        if (customerRepository.existsByCode(code)) {
            throw new DuplicateResourceException("Kode %s sudah digunakan".formatted(code));
        }
    }
}
