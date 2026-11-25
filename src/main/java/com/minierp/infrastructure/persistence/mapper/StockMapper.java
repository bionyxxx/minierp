package com.minierp.infrastructure.persistence.mapper;

import com.minierp.domain.model.inventory.Stock;
import com.minierp.infrastructure.persistence.entity.StockJpaEntity;

/**
 * Domain ⇄ JPA mapping for stock balances. Every column is copied so a merge
 * can never wipe state Hibernate has already persisted.
 */
public final class StockMapper {

    private StockMapper() {
    }

    public static StockJpaEntity toEntity(Stock stock) {
        StockJpaEntity entity = new StockJpaEntity();
        entity.setId(stock.getId());
        entity.setItem(ItemMapper.toEntity(stock.getItem()));
        entity.setWarehouse(WarehouseMapper.toEntity(stock.getWarehouse()));
        entity.setQty(stock.getQty());
        return entity;
    }

    public static Stock toDomain(StockJpaEntity entity) {
        return Stock.reconstitute(entity.getId(), ItemMapper.toDomain(entity.getItem()),
                WarehouseMapper.toDomain(entity.getWarehouse()), entity.getQty());
    }
}
