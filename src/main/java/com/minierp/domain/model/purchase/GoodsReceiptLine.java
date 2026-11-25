package com.minierp.domain.model.purchase;

import java.math.BigDecimal;

import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.shared.BusinessRuleException;

/**
 * One received item on a Goods Receipt.
 */
public class GoodsReceiptLine {

    private Long id;
    private Item item;
    private BigDecimal qty;

    private GoodsReceiptLine(Item item, BigDecimal qty) {
        this.item = item;
        this.qty = qty;
    }

    /**
     * Creates a received line.
     *
     * @throws BusinessRuleException when qty is not positive
     */
    public static GoodsReceiptLine create(Item item, BigDecimal qty) {
        if (qty == null || qty.signum() <= 0) {
            throw new BusinessRuleException("qty line Goods Receipt harus lebih dari 0");
        }
        return new GoodsReceiptLine(item, qty);
    }

    /**
     * Reconstitutes an existing line loaded from persistence.
     */
    public static GoodsReceiptLine reconstitute(Long id, Item item, BigDecimal qty) {
        GoodsReceiptLine line = new GoodsReceiptLine(item, qty);
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
}
