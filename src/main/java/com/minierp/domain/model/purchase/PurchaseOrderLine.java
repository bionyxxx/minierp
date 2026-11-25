package com.minierp.domain.model.purchase;

import java.math.BigDecimal;

import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.shared.BusinessRuleException;

/**
 * One ordered item on a Purchase Order. The line total is derived from qty and
 * unit price at creation time.
 */
public class PurchaseOrderLine {

    private Long id;
    private Item item;
    private BigDecimal qty;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    private PurchaseOrderLine(Item item, BigDecimal qty, BigDecimal unitPrice, BigDecimal lineTotal) {
        this.item = item;
        this.qty = qty;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    /**
     * Creates a line and computes its total.
     *
     * @throws BusinessRuleException when qty is not positive or the unit price is negative
     */
    public static PurchaseOrderLine create(Item item, BigDecimal qty, BigDecimal unitPrice) {
        if (qty == null || qty.signum() <= 0) {
            throw new BusinessRuleException("qty line Purchase Order harus lebih dari 0");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new BusinessRuleException("unitPrice line Purchase Order tidak boleh negatif");
        }
        return new PurchaseOrderLine(item, qty, unitPrice, qty.multiply(unitPrice));
    }

    /**
     * Reconstitutes an existing line loaded from persistence.
     */
    public static PurchaseOrderLine reconstitute(Long id, Item item, BigDecimal qty, BigDecimal unitPrice,
            BigDecimal lineTotal) {
        PurchaseOrderLine line = new PurchaseOrderLine(item, qty, unitPrice, lineTotal);
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
