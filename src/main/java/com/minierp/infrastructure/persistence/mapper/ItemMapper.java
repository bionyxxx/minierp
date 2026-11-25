package com.minierp.infrastructure.persistence.mapper;

import com.minierp.domain.model.item.Item;
import com.minierp.infrastructure.persistence.entity.ItemJpaEntity;

/**
 * Domain ⇄ JPA mapping for the item aggregate. Every column is copied so a
 * merge can never wipe state Hibernate has already persisted.
 */
public final class ItemMapper {

    private ItemMapper() {
    }

    public static ItemJpaEntity toEntity(Item item) {
        ItemJpaEntity entity = new ItemJpaEntity();
        entity.setId(item.getId());
        entity.setSku(item.getSku());
        entity.setName(item.getName());
        entity.setUom(item.getUom());
        entity.setMinStock(item.getMinStock());
        entity.setPurchasePrice(item.getPurchasePrice());
        entity.setSellingPrice(item.getSellingPrice());
        entity.setActive(item.isActive());
        entity.setCreatedAt(item.getCreatedAt());
        entity.setUpdatedAt(item.getUpdatedAt());
        return entity;
    }

    public static Item toDomain(ItemJpaEntity entity) {
        return Item.reconstitute(entity.getId(), entity.getSku(), entity.getName(), entity.getUom(),
                entity.getMinStock(), entity.getPurchasePrice(), entity.getSellingPrice(),
                entity.isActive(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
