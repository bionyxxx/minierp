package com.minierp.inventory;

import java.math.BigDecimal;
import java.time.Instant;

import com.minierp.item.Item;
import com.minierp.warehouse.Warehouse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Immutable stock card entry (goods in/out history).
 */
@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 10)
    private StockMovementType movementType;

    @Column(name = "qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal qty;

    @Enumerated(EnumType.STRING)
    @Column(name = "ref_type", nullable = false, length = 20)
    private StockMovementRef refType;

    @Column(name = "ref_id", nullable = false)
    private Long refId;

    @Column(name = "ref_number", nullable = false, length = 30)
    private String refNumber;

    @Column(name = "remark", length = 255)
    private String remark;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected StockMovement() {
    }

    public StockMovement(Item item, Warehouse warehouse, StockMovementType movementType, BigDecimal qty,
            StockMovementRef refType, Long refId, String refNumber, String remark, String createdBy) {
        this.item = item;
        this.warehouse = warehouse;
        this.movementType = movementType;
        this.qty = qty;
        this.refType = refType;
        this.refId = refId;
        this.refNumber = refNumber;
        this.remark = remark;
        this.createdBy = createdBy;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
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
