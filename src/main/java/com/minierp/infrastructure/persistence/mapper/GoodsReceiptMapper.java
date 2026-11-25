package com.minierp.infrastructure.persistence.mapper;

import java.util.ArrayList;
import java.util.List;

import com.minierp.domain.model.purchase.GoodsReceipt;
import com.minierp.domain.model.purchase.GoodsReceiptLine;
import com.minierp.infrastructure.persistence.entity.GoodsReceiptJpaEntity;
import com.minierp.infrastructure.persistence.entity.GoodsReceiptLineJpaEntity;

/**
 * Domain ⇄ JPA mapping for the Goods Receipt aggregate (header and lines).
 * Every column is copied so a merge can never wipe state Hibernate has already
 * persisted, and both directions of the parent ↔ line relation are mapped.
 */
public final class GoodsReceiptMapper {

    private GoodsReceiptMapper() {
    }

    public static GoodsReceiptJpaEntity toEntity(GoodsReceipt grn) {
        GoodsReceiptJpaEntity entity = new GoodsReceiptJpaEntity();
        entity.setId(grn.getId());
        entity.setGrnNumber(grn.getGrnNumber());
        entity.setPurchaseOrder(PurchaseOrderMapper.toEntity(grn.getPurchaseOrder()));
        entity.setWarehouse(WarehouseMapper.toEntity(grn.getWarehouse()));
        entity.setReceiptDate(grn.getReceiptDate());
        entity.setStatus(grn.getStatus());
        entity.setNotes(grn.getNotes());
        entity.setCreatedBy(grn.getCreatedBy());
        entity.setCreatedAt(grn.getCreatedAt());
        entity.setApprovedBy(grn.getApprovedBy());
        entity.setApprovedAt(grn.getApprovedAt());
        entity.setRejectReason(grn.getRejectReason());
        entity.getLines().addAll(grn.getLines().stream().map(line -> toLineEntity(entity, line)).toList());
        return entity;
    }

    public static GoodsReceipt toDomain(GoodsReceiptJpaEntity entity) {
        List<GoodsReceiptLine> lines = entity.getLines().stream()
                .map(GoodsReceiptMapper::toLineDomain)
                .toList();
        return GoodsReceipt.reconstitute(entity.getId(), entity.getGrnNumber(),
                PurchaseOrderMapper.toDomain(entity.getPurchaseOrder()),
                WarehouseMapper.toDomain(entity.getWarehouse()), entity.getReceiptDate(), entity.getStatus(),
                entity.getNotes(), entity.getCreatedBy(), entity.getCreatedAt(), entity.getApprovedBy(),
                entity.getApprovedAt(), entity.getRejectReason(), new ArrayList<>(lines));
    }

    private static GoodsReceiptLineJpaEntity toLineEntity(GoodsReceiptJpaEntity parent, GoodsReceiptLine line) {
        GoodsReceiptLineJpaEntity entity = new GoodsReceiptLineJpaEntity();
        entity.setId(line.getId());
        entity.setGoodsReceipt(parent);
        entity.setItem(ItemMapper.toEntity(line.getItem()));
        entity.setQty(line.getQty());
        return entity;
    }

    private static GoodsReceiptLine toLineDomain(GoodsReceiptLineJpaEntity entity) {
        return GoodsReceiptLine.reconstitute(entity.getId(), ItemMapper.toDomain(entity.getItem()), entity.getQty());
    }
}
