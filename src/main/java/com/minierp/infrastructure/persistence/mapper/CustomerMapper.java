package com.minierp.infrastructure.persistence.mapper;

import com.minierp.domain.model.customer.Customer;
import com.minierp.infrastructure.persistence.entity.CustomerJpaEntity;

/**
 * Domain ⇄ JPA mapping for the customer aggregate. Every column is copied so a
 * merge can never wipe state Hibernate has already persisted.
 */
public final class CustomerMapper {

    private CustomerMapper() {
    }

    public static CustomerJpaEntity toEntity(Customer customer) {
        CustomerJpaEntity entity = new CustomerJpaEntity();
        entity.setId(customer.getId());
        entity.setCode(customer.getCode());
        entity.setName(customer.getName());
        entity.setAddress(customer.getAddress());
        entity.setPhone(customer.getPhone());
        entity.setEmail(customer.getEmail());
        entity.setActive(customer.isActive());
        entity.setCreatedAt(customer.getCreatedAt());
        entity.setUpdatedAt(customer.getUpdatedAt());
        return entity;
    }

    public static Customer toDomain(CustomerJpaEntity entity) {
        return Customer.reconstitute(entity.getId(), entity.getCode(), entity.getName(), entity.getAddress(),
                entity.getPhone(), entity.getEmail(), entity.isActive(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
