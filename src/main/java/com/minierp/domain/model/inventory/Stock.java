package com.minierp.domain.model.inventory;

import java.math.BigDecimal;

import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.shared.BusinessRuleException;
import com.minierp.domain.model.warehouse.Warehouse;

/**
 * Stock balance of an item in a specific warehouse (multi-warehouse support).
 * Pure domain model: balance changes and their guard live here.
 */
public class Stock {

    private Long id;
    private Item item;
    private Warehouse warehouse;
    private BigDecimal qty;

    private Stock(Item item, Warehouse warehouse, BigDecimal qty) {
        this.item = item;
        this.warehouse = warehouse;
        this.qty = qty;
    }

    /**
     * New balance for an item that has never been stocked in the warehouse.
     */
    public static Stock create(Item item, Warehouse warehouse) {
        return new Stock(item, warehouse, BigDecimal.ZERO);
    }

    /**
     * Reconstitutes an existing balance loaded from persistence.
     */
    public static Stock reconstitute(Long id, Item item, Warehouse warehouse, BigDecimal qty) {
        Stock stock = new Stock(item, warehouse, qty);
        stock.id = id;
        return stock;
    }

    /**
     * Increases the balance (goods receipt).
     */
    public void increase(BigDecimal qty) {
        this.qty = this.qty.add(qty);
    }

    /**
     * Decreases the balance (delivery).
     *
     * @throws BusinessRuleException when the warehouse holds less than requested
     */
    public void decrease(BigDecimal qty) {
        if (this.qty.compareTo(qty) < 0) {
            throw new BusinessRuleException(insufficientMessage(qty));
        }
        this.qty = this.qty.subtract(qty);
    }

    private String insufficientMessage(BigDecimal requested) {
        return "Stok tidak mencukupi: item %s di gudang %s tersedia %s, diminta %s"
                .formatted(item.getSku(), warehouse.getCode(), qty.toPlainString(), requested.toPlainString());
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
}
