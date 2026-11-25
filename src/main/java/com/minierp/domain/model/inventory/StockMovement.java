package com.minierp.domain.model.inventory;

import java.math.BigDecimal;
import java.time.Instant;

import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.warehouse.Warehouse;

/**
 * Immutable stock card entry (goods in/out history). Pure domain model; the
 * created timestamp is filled by persistence.
 */
public class StockMovement {

    private Long id;
    private final Item item;
    private final Warehouse warehouse;
    private final StockMovementType movementType;
    private final BigDecimal qty;
    private final StockMovementRef refType;
    private final Long refId;
    private final String refNumber;
    private final String remark;
    private final String createdBy;
    private final Instant createdAt;

    private StockMovement(Item item, Warehouse warehouse, StockMovementType movementType, BigDecimal qty,
            StockMovementRef refType, Long refId, String refNumber, String remark, String createdBy,
            Instant createdAt) {
        this.item = item;
        this.warehouse = warehouse;
        this.movementType = movementType;
        this.qty = qty;
        this.refType = refType;
        this.refId = refId;
        this.refNumber = refNumber;
        this.remark = remark;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    /**
     * Records a new stock card entry for a goods in/out movement.
     */
    public static StockMovement create(Item item, Warehouse warehouse, StockMovementType movementType, BigDecimal qty,
            StockMovementRef refType, Long refId, String refNumber, String remark, String createdBy) {
        return new StockMovement(item, warehouse, movementType, qty, refType, refId, refNumber, remark, createdBy,
                null);
    }

    /**
     * Reconstitutes an existing stock card entry loaded from persistence.
     */
    public static StockMovement reconstitute(Long id, Item item, Warehouse warehouse, StockMovementType movementType,
            BigDecimal qty, StockMovementRef refType, Long refId, String refNumber, String remark, String createdBy,
            Instant createdAt) {
        StockMovement movement = new StockMovement(item, warehouse, movementType, qty, refType, refId, refNumber,
                remark, createdBy, createdAt);
        movement.id = id;
        return movement;
    }

    public Long getId() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public StockMovementType getMovementType() {
        return movementType;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public StockMovementRef getRefType() {
        return refType;
    }

    public Long getRefId() {
        return refId;
    }

    public String getRefNumber() {
        return refNumber;
    }

    public String getRemark() {
        return remark;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
