package com.minierp.infrastructure.persistence.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * JPA mapping of the {@code stocks} table. Persistence detail of the inventory
 * aggregate; the domain model lives in {@code domain.model.inventory}.
 */
@Entity
@Table(name = "stocks", uniqueConstraints = @UniqueConstraint(columnNames = { "item_id", "warehouse_id" }))
public class StockJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private ItemJpaEntity item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private WarehouseJpaEntity warehouse;

    @Column(name = "qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal qty = BigDecimal.ZERO;

    public StockJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    /** Used by the persistence mapper only. */
    public void setId(Long id) {
        this.id = id;
    }

    public ItemJpaEntity getItem() {
        return item;
    }

    /** Used by the persistence mapper only. */
    public void setItem(ItemJpaEntity item) {
        this.item = item;
    }

    public WarehouseJpaEntity getWarehouse() {
        return warehouse;
    }

    /** Used by the persistence mapper only. */
    public void setWarehouse(WarehouseJpaEntity warehouse) {
        this.warehouse = warehouse;
    }

    public BigDecimal getQty() {
        return qty;
    }

    /** Used by the persistence mapper only. */
    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }
}
