package com.minierp.infrastructure.persistence.mapper;

import java.util.ArrayList;
import java.util.List;

import com.minierp.domain.model.purchase.PurchaseOrder;
import com.minierp.domain.model.purchase.PurchaseOrderLine;
import com.minierp.infrastructure.persistence.entity.PurchaseOrderJpaEntity;
import com.minierp.infrastructure.persistence.entity.PurchaseOrderLineJpaEntity;

/**
 * Domain ⇄ JPA mapping for the Purchase Order aggregate (header and lines).
 * Every column is copied so a merge can never wipe state Hibernate has already
 * persisted, and both directions of the parent ↔ line relation are mapped.
 */
public final class PurchaseOrderMapper {

    private PurchaseOrderMapper() {
    }

    public static PurchaseOrderJpaEntity toEntity(PurchaseOrder po) {
        PurchaseOrderJpaEntity entity = new PurchaseOrderJpaEntity();
        entity.setId(po.getId());
        entity.setPoNumber(po.getPoNumber());
        entity.setSupplier(SupplierMapper.toEntity(po.getSupplier()));
        entity.setOrderDate(po.getOrderDate());
        entity.setTotalAmount(po.getTotalAmount());
        entity.setStatus(po.getStatus());
        entity.setNotes(po.getNotes());
        entity.setCreatedBy(po.getCreatedBy());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setApprovedBy(po.getApprovedBy());
        entity.setApprovedAt(po.getApprovedAt());
        entity.setRejectReason(po.getRejectReason());
        entity.getLines().addAll(po.getLines().stream().map(line -> toLineEntity(entity, line)).toList());
        return entity;
    }

    public static PurchaseOrder toDomain(PurchaseOrderJpaEntity entity) {
        List<PurchaseOrderLine> lines = entity.getLines().stream()
                .map(PurchaseOrderMapper::toLineDomain)
                .toList();
        return PurchaseOrder.reconstitute(entity.getId(), entity.getPoNumber(),
                SupplierMapper.toDomain(entity.getSupplier()), entity.getOrderDate(), entity.getTotalAmount(),
                entity.getStatus(), entity.getNotes(), entity.getCreatedBy(), entity.getCreatedAt(),
                entity.getApprovedBy(), entity.getApprovedAt(), entity.getRejectReason(),
                new ArrayList<>(lines));
    }

    private static PurchaseOrderLineJpaEntity toLineEntity(PurchaseOrderJpaEntity parent, PurchaseOrderLine line) {
        PurchaseOrderLineJpaEntity entity = new PurchaseOrderLineJpaEntity();
        entity.setId(line.getId());
        entity.setPurchaseOrder(parent);
        entity.setItem(ItemMapper.toEntity(line.getItem()));
        entity.setQty(line.getQty());
        entity.setUnitPrice(line.getUnitPrice());
        entity.setLineTotal(line.getLineTotal());
        return entity;
    }

    private static PurchaseOrderLine toLineDomain(PurchaseOrderLineJpaEntity entity) {
        return PurchaseOrderLine.reconstitute(entity.getId(), ItemMapper.toDomain(entity.getItem()),
                entity.getQty(), entity.getUnitPrice(), entity.getLineTotal());
    }
}
