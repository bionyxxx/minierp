package com.minierp.customer;

import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minierp.common.AuthenticatedUser;
import com.minierp.common.DuplicateResourceException;
import com.minierp.common.NotFoundException;
import com.minierp.common.PageResponse;
import com.minierp.common.Pagination;
import com.minierp.customer.dto.CustomerRequest;
import com.minierp.customer.dto.CustomerResponse;

/**
 * Master data CRUD for customers.
 */
@Service
public class CustomerService {

    private static final Set<String> SORT_PROPERTIES =
            Set.of("id", "code", "name", "address", "phone", "email", "active");

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> list(AuthenticatedUser actor, Boolean active, Pageable pageable) {
        Pageable effective = Pagination.normalize(pageable, SORT_PROPERTIES, Sort.by("code"));
        return PageResponse.from(customerRepository.findPage(active, effective).map(CustomerResponse::from));
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(AuthenticatedUser actor, Long id) {
        return CustomerResponse.from(findOrThrow(id));
    }

    @Transactional
    public CustomerResponse create(AuthenticatedUser actor, CustomerRequest request) {
        ensureCodeAvailable(request.code());
        Customer customer = new Customer();
        apply(customer, request);
        return CustomerResponse.from(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse update(AuthenticatedUser actor, Long id, CustomerRequest request) {
        Customer customer = findOrThrow(id);
        if (!customer.getCode().equals(request.code())) {
            ensureCodeAvailable(request.code());
        }
        apply(customer, request);
        return CustomerResponse.from(customerRepository.save(customer));
    }

    /**
     * Soft delete: the customer stays referenced by documents but is deactivated.
     */
    @Transactional
    public void delete(AuthenticatedUser actor, Long id) {
        Customer customer = findOrThrow(id);
        customer.setActive(false);
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

    private void apply(Customer customer, CustomerRequest request) {
        customer.setCode(request.code());
        customer.setName(request.name());
        customer.setAddress(request.address());
        customer.setPhone(request.phone());
        customer.setEmail(request.email());
    }
}
