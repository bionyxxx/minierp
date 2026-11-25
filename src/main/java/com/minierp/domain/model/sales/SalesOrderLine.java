package com.minierp.domain.model.sales;

import java.math.BigDecimal;

import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.shared.BusinessRuleException;

/**
 * One ordered item on a Sales Order. The line total is derived from qty and
 * unit price at creation time.
 */
public class SalesOrderLine {

    private Long id;
    private Item item;
    private BigDecimal qty;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    private SalesOrderLine(Item item, BigDecimal qty, BigDecimal unitPrice, BigDecimal lineTotal) {
        this.item = item;
        this.qty = qty;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    /**
     * Creates a line and computes its total.
     *
     * @throws BusinessRuleException when qty is not positive or the selling price is negative
     */
    public static SalesOrderLine create(Item item, BigDecimal qty, BigDecimal unitPrice) {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Qty line Sales Order harus lebih besar dari 0");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Harga jual line Sales Order tidak boleh negatif");
        }
        return new SalesOrderLine(item, qty, unitPrice, qty.multiply(unitPrice));
    }

    /**
     * Reconstitutes an existing line loaded from persistence.
     */
    public static SalesOrderLine reconstitute(Long id, Item item, BigDecimal qty, BigDecimal unitPrice,
            BigDecimal lineTotal) {
        SalesOrderLine line = new SalesOrderLine(item, qty, unitPrice, lineTotal);
        line.id = id;
        return line;
    }

    public Long getId() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
