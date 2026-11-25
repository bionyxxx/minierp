package com.minierp.infrastructure.persistence.mapper;

import com.minierp.domain.model.warehouse.Warehouse;
import com.minierp.infrastructure.persistence.entity.WarehouseJpaEntity;

/**
 * Domain ⇄ JPA mapping for the warehouse aggregate. Every column is copied so a
 * merge can never wipe state Hibernate has already persisted.
 */
public final class WarehouseMapper {

    private WarehouseMapper() {
    }

    public static WarehouseJpaEntity toEntity(Warehouse warehouse) {
        WarehouseJpaEntity entity = new WarehouseJpaEntity();
        entity.setId(warehouse.getId());
        entity.setCode(warehouse.getCode());
        entity.setName(warehouse.getName());
        entity.setLocation(warehouse.getLocation());
        entity.setActive(warehouse.isActive());
        entity.setCreatedAt(warehouse.getCreatedAt());
        entity.setUpdatedAt(warehouse.getUpdatedAt());
        return entity;
    }

    public static Warehouse toDomain(WarehouseJpaEntity entity) {
        return Warehouse.reconstitute(entity.getId(), entity.getCode(), entity.getName(), entity.getLocation(),
                entity.isActive(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
