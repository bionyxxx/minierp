package com.minierp.infrastructure.persistence.mapper;

import java.util.ArrayList;
import java.util.List;

import com.minierp.domain.model.sales.Delivery;
import com.minierp.domain.model.sales.DeliveryLine;
import com.minierp.infrastructure.persistence.entity.DeliveryJpaEntity;
import com.minierp.infrastructure.persistence.entity.DeliveryLineJpaEntity;

/**
 * Domain ⇄ JPA mapping for the Delivery aggregate (header and lines).
 * Every column is copied so a merge can never wipe state Hibernate has already
 * persisted, and both directions of the parent ↔ line relation are mapped.
 */
public final class DeliveryMapper {

    private DeliveryMapper() {
    }

    public static DeliveryJpaEntity toEntity(Delivery delivery) {
        DeliveryJpaEntity entity = new DeliveryJpaEntity();
        entity.setId(delivery.getId());
        entity.setDeliveryNumber(delivery.getDeliveryNumber());
        entity.setSalesOrder(SalesOrderMapper.toEntity(delivery.getSalesOrder()));
        entity.setWarehouse(WarehouseMapper.toEntity(delivery.getWarehouse()));
        entity.setDeliveryDate(delivery.getDeliveryDate());
        entity.setStatus(delivery.getStatus());
        entity.setNotes(delivery.getNotes());
        entity.setCreatedBy(delivery.getCreatedBy());
        entity.setCreatedAt(delivery.getCreatedAt());
        entity.setApprovedBy(delivery.getApprovedBy());
        entity.setApprovedAt(delivery.getApprovedAt());
        entity.setRejectReason(delivery.getRejectReason());
        entity.getLines().addAll(delivery.getLines().stream().map(line -> toLineEntity(entity, line)).toList());
        return entity;
    }

    public static Delivery toDomain(DeliveryJpaEntity entity) {
        List<DeliveryLine> lines = entity.getLines().stream()
                .map(DeliveryMapper::toLineDomain)
                .toList();
        return Delivery.reconstitute(entity.getId(), entity.getDeliveryNumber(),
                SalesOrderMapper.toDomain(entity.getSalesOrder()),
                WarehouseMapper.toDomain(entity.getWarehouse()), entity.getDeliveryDate(), entity.getStatus(),
                entity.getNotes(), entity.getCreatedBy(), entity.getCreatedAt(), entity.getApprovedBy(),
                entity.getApprovedAt(), entity.getRejectReason(), new ArrayList<>(lines));
    }

    private static DeliveryLineJpaEntity toLineEntity(DeliveryJpaEntity parent, DeliveryLine line) {
        DeliveryLineJpaEntity entity = new DeliveryLineJpaEntity();
        entity.setId(line.getId());
        entity.setDelivery(parent);
        entity.setItem(ItemMapper.toEntity(line.getItem()));
        entity.setQty(line.getQty());
        return entity;
    }

    private static DeliveryLine toLineDomain(DeliveryLineJpaEntity entity) {
        return DeliveryLine.reconstitute(entity.getId(), ItemMapper.toDomain(entity.getItem()), entity.getQty());
    }
}
