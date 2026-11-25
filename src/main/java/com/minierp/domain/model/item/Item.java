package com.minierp.domain.model.item;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Item master data. Pure domain model: creation rules, updates, and the soft
 * delete lifecycle live here; persistence details stay in the infrastructure
 * mappers.
 */
public class Item {

    private Long id;
    private String sku;
    private String name;
    private String uom;
    private BigDecimal minStock;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    private Item() {
    }

    /**
     * Creates a new item. Nullable numeric fields default to zero.
     */
    public static Item create(String sku, String name, String uom,
            BigDecimal minStock, BigDecimal purchasePrice, BigDecimal sellingPrice) {
        Item item = new Item();
        item.sku = sku;
        item.name = name;
        item.uom = uom;
        item.minStock = orZero(minStock);
        item.purchasePrice = orZero(purchasePrice);
        item.sellingPrice = orZero(sellingPrice);
        item.active = true;
        return item;
    }

    /**
     * Reconstitutes an existing item loaded from persistence.
     */
    public static Item reconstitute(Long id, String sku, String name, String uom,
            BigDecimal minStock, BigDecimal purchasePrice, BigDecimal sellingPrice,
            boolean active, Instant createdAt, Instant updatedAt) {
        Item item = new Item();
        item.id = id;
        item.sku = sku;
        item.name = name;
        item.uom = uom;
        item.minStock = minStock;
        item.purchasePrice = purchasePrice;
        item.sellingPrice = sellingPrice;
        item.active = active;
        item.createdAt = createdAt;
        item.updatedAt = updatedAt;
        return item;
    }

    /**
     * Applies a full update of the item details. Nullable numeric fields
     * default to zero.
     */
    public void update(String sku, String name, String uom,
            BigDecimal minStock, BigDecimal purchasePrice, BigDecimal sellingPrice) {
        this.sku = sku;
        this.name = name;
        this.uom = uom;
        this.minStock = orZero(minStock);
        this.purchasePrice = orZero(purchasePrice);
        this.sellingPrice = orZero(sellingPrice);
    }

    /**
     * Soft delete: the item stays referenced by documents but is deactivated.
     */
    public void deactivate() {
        this.active = false;
    }

    private static BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getUom() {
        return uom;
    }

    public BigDecimal getMinStock() {
        return minStock;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
