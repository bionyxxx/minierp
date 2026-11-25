package com.minierp.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.customer.Customer;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.port.out.CustomerRepositoryPort;
import com.minierp.infrastructure.persistence.entity.CustomerJpaEntity;
import com.minierp.infrastructure.persistence.mapper.CustomerMapper;

/**
 * Persistence adapter — implements the customer port using JPA.
 * The domain layer never sees JPA, only this adapter does.
 */
@Component
public class JpaCustomerRepository implements CustomerRepositoryPort {

    private final SpringDataCustomerRepository repository;

    public JpaCustomerRepository(SpringDataCustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return repository.findById(id).map(CustomerMapper::toDomain);
    }

    @Override
    public Optional<Customer> findByCode(String code) {
        return repository.findByCode(code).map(CustomerMapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return repository.existsByCode(code);
    }

    @Override
    public Customer save(Customer customer) {
        CustomerJpaEntity saved = repository.save(CustomerMapper.toEntity(customer));
        return CustomerMapper.toDomain(saved);
    }

    @Override
    public PageResult<Customer> findPage(Boolean active, PageQuery query) {
        Page<CustomerJpaEntity> page = repository.findPage(active, Pagination.toPageable(query));
        return new PageResult<>(page.getContent().stream().map(CustomerMapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
