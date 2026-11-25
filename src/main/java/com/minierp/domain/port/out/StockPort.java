package com.minierp.domain.port.out;

import com.minierp.domain.model.inventory.StockMovementRef;
import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.warehouse.Warehouse;

import java.math.BigDecimal;

/**
 * Driven port: stock balance changes needed by the document use cases
 * (goods receipt increases stock, delivery decreases it).
 */
public interface StockPort {

    /**
     * Increases stock and records an IN movement.
     */
    void increase(Item item, Warehouse warehouse, BigDecimal qty,
            StockMovementRef refType, Long refId, String refNumber, String remark, String actor);

    /**
     * Decreases stock and records an OUT movement.
     */
    void decrease(Item item, Warehouse warehouse, BigDecimal qty,
            StockMovementRef refType, Long refId, String refNumber, String remark, String actor);

    /**
     * Current stock of an item in a warehouse (zero when never stocked).
     */
    BigDecimal availableQty(Item item, Warehouse warehouse);
}
