package com.minierp.infrastructure.web.inventory.dto;

import java.math.BigDecimal;
import java.util.List;

import com.minierp.domain.model.inventory.ItemStock;

/**
 * Stock of one item across all warehouses.
 */
public record ItemStockResponse(Long itemId, String sku, String name, String uom,
        BigDecimal totalQty, List<StockResponse> warehouseStocks) {

    public static ItemStockResponse from(ItemStock itemStock) {
        return new ItemStockResponse(itemStock.item().getId(), itemStock.item().getSku(),
                itemStock.item().getName(), itemStock.item().getUom(), itemStock.totalQty(),
                itemStock.warehouseStocks().stream().map(StockResponse::from).toList());
    }
}
