package com.minierp.infrastructure.persistence.mapper;

import java.util.ArrayList;
import java.util.List;

import com.minierp.domain.model.sales.SalesOrder;
import com.minierp.domain.model.sales.SalesOrderLine;
import com.minierp.infrastructure.persistence.entity.SalesOrderJpaEntity;
import com.minierp.infrastructure.persistence.entity.SalesOrderLineJpaEntity;

/**
 * Domain ⇄ JPA mapping for the Sales Order aggregate (header and lines).
 * Every column is copied so a merge can never wipe state Hibernate has already
 * persisted, and both directions of the parent ↔ line relation are mapped.
 */
public final class SalesOrderMapper {

    private SalesOrderMapper() {
    }

    public static SalesOrderJpaEntity toEntity(SalesOrder so) {
        SalesOrderJpaEntity entity = new SalesOrderJpaEntity();
        entity.setId(so.getId());
        entity.setSoNumber(so.getSoNumber());
        entity.setCustomer(CustomerMapper.toEntity(so.getCustomer()));
        entity.setOrderDate(so.getOrderDate());
        entity.setTotalAmount(so.getTotalAmount());
        entity.setStatus(so.getStatus());
        entity.setNotes(so.getNotes());
        entity.setCreatedBy(so.getCreatedBy());
        entity.setCreatedAt(so.getCreatedAt());
        entity.setApprovedBy(so.getApprovedBy());
        entity.setApprovedAt(so.getApprovedAt());
        entity.setRejectReason(so.getRejectReason());
        entity.getLines().addAll(so.getLines().stream().map(line -> toLineEntity(entity, line)).toList());
        return entity;
    }

    public static SalesOrder toDomain(SalesOrderJpaEntity entity) {
        List<SalesOrderLine> lines = entity.getLines().stream()
                .map(SalesOrderMapper::toLineDomain)
                .toList();
        return SalesOrder.reconstitute(entity.getId(), entity.getSoNumber(),
                CustomerMapper.toDomain(entity.getCustomer()), entity.getOrderDate(), entity.getTotalAmount(),
                entity.getStatus(), entity.getNotes(), entity.getCreatedBy(), entity.getCreatedAt(),
                entity.getApprovedBy(), entity.getApprovedAt(), entity.getRejectReason(),
                new ArrayList<>(lines));
    }

    private static SalesOrderLineJpaEntity toLineEntity(SalesOrderJpaEntity parent, SalesOrderLine line) {
        SalesOrderLineJpaEntity entity = new SalesOrderLineJpaEntity();
        entity.setId(line.getId());
        entity.setSalesOrder(parent);
        entity.setItem(ItemMapper.toEntity(line.getItem()));
        entity.setQty(line.getQty());
        entity.setUnitPrice(line.getUnitPrice());
        entity.setLineTotal(line.getLineTotal());
        return entity;
    }

    private static SalesOrderLine toLineDomain(SalesOrderLineJpaEntity entity) {
        return SalesOrderLine.reconstitute(entity.getId(), ItemMapper.toDomain(entity.getItem()),
                entity.getQty(), entity.getUnitPrice(), entity.getLineTotal());
    }
}
