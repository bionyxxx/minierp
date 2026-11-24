package com.minierp.inventory.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Stock of one item across all warehouses.
 */
public record ItemStockResponse(Long itemId, String sku, String name, String uom,
        BigDecimal totalQty, List<StockResponse> warehouseStocks) {
}
