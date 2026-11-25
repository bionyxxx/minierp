package com.minierp.infrastructure.persistence.mapper;

import com.minierp.domain.model.supplier.Supplier;
import com.minierp.infrastructure.persistence.entity.SupplierJpaEntity;

/**
 * Domain ⇄ JPA mapping for the supplier aggregate. Every column is copied so a
 * merge can never wipe state Hibernate has already persisted.
 */
public final class SupplierMapper {

    private SupplierMapper() {
    }

    public static SupplierJpaEntity toEntity(Supplier supplier) {
        SupplierJpaEntity entity = new SupplierJpaEntity();
        entity.setId(supplier.getId());
        entity.setCode(supplier.getCode());
        entity.setName(supplier.getName());
        entity.setAddress(supplier.getAddress());
        entity.setPhone(supplier.getPhone());
        entity.setEmail(supplier.getEmail());
        entity.setActive(supplier.isActive());
        entity.setCreatedAt(supplier.getCreatedAt());
        entity.setUpdatedAt(supplier.getUpdatedAt());
        return entity;
    }

    public static Supplier toDomain(SupplierJpaEntity entity) {
        return Supplier.reconstitute(entity.getId(), entity.getCode(), entity.getName(), entity.getAddress(),
                entity.getPhone(), entity.getEmail(), entity.isActive(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
