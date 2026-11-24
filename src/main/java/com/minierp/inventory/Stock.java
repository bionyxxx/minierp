package com.minierp.inventory;

import java.math.BigDecimal;

import com.minierp.item.Item;
import com.minierp.warehouse.Warehouse;

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
 * Stock balance of an item in a specific warehouse (multi-warehouse support).
 */
@Entity
@Table(name = "stocks", uniqueConstraints = @UniqueConstraint(columnNames = { "item_id", "warehouse_id" }))
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal qty = BigDecimal.ZERO;

    protected Stock() {
    }

    public Stock(Item item, Warehouse warehouse, BigDecimal qty) {
        this.item = item;
        this.warehouse = warehouse;
        this.qty = qty;
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

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }
}
