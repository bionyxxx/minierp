package com.minierp.domain.model.inventory;

import java.math.BigDecimal;
import java.util.List;

import com.minierp.domain.model.item.Item;

/**
 * Stock of one item across all warehouses, with the total quantity.
 */
public record ItemStock(Item item, List<Stock> warehouseStocks) {

    public ItemStock {
        warehouseStocks = List.copyOf(warehouseStocks);
    }

    public BigDecimal totalQty() {
        return warehouseStocks.stream()
                .map(Stock::getQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
