package com.minierp.infrastructure.persistence.mapper;

import com.minierp.domain.model.inventory.StockMovement;
import com.minierp.infrastructure.persistence.entity.StockMovementJpaEntity;

/**
 * Domain ⇄ JPA mapping for stock card entries. Every column is copied so a
 * merge can never wipe state Hibernate has already persisted.
 */
public final class StockMovementMapper {

    private StockMovementMapper() {
    }

    public static StockMovementJpaEntity toEntity(StockMovement movement) {
        StockMovementJpaEntity entity = new StockMovementJpaEntity();
        entity.setId(movement.getId());
        entity.setItem(ItemMapper.toEntity(movement.getItem()));
        entity.setWarehouse(WarehouseMapper.toEntity(movement.getWarehouse()));
        entity.setMovementType(movement.getMovementType());
        entity.setQty(movement.getQty());
        entity.setRefType(movement.getRefType());
        entity.setRefId(movement.getRefId());
        entity.setRefNumber(movement.getRefNumber());
        entity.setRemark(movement.getRemark());
        entity.setCreatedBy(movement.getCreatedBy());
        entity.setCreatedAt(movement.getCreatedAt());
        return entity;
    }

    public static StockMovement toDomain(StockMovementJpaEntity entity) {
        return StockMovement.reconstitute(entity.getId(), ItemMapper.toDomain(entity.getItem()),
                WarehouseMapper.toDomain(entity.getWarehouse()), entity.getMovementType(), entity.getQty(),
                entity.getRefType(), entity.getRefId(), entity.getRefNumber(), entity.getRemark(),
                entity.getCreatedBy(), entity.getCreatedAt());
    }
}
