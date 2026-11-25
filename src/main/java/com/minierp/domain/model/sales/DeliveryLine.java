package com.minierp.domain.model.sales;

import java.math.BigDecimal;

import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.shared.BusinessRuleException;

/**
 * One delivered item on a Delivery.
 */
public class DeliveryLine {

    private Long id;
    private Item item;
    private BigDecimal qty;

    private DeliveryLine(Item item, BigDecimal qty) {
        this.item = item;
        this.qty = qty;
    }

    /**
     * Creates a delivered line.
     *
     * @throws BusinessRuleException when qty is not positive
     */
    public static DeliveryLine create(Item item, BigDecimal qty) {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Qty line Delivery harus lebih besar dari 0");
        }
        return new DeliveryLine(item, qty);
    }

    /**
     * Reconstitutes an existing line loaded from persistence.
     */
    public static DeliveryLine reconstitute(Long id, Item item, BigDecimal qty) {
        DeliveryLine line = new DeliveryLine(item, qty);
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
