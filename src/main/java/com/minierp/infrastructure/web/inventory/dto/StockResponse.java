package com.minierp.infrastructure.web.inventory.dto;

import java.math.BigDecimal;

import com.minierp.domain.model.inventory.Stock;

/**
 * Stock balance of one item in one warehouse.
 */
public record StockResponse(Long itemId, String sku, String itemName,
        Long warehouseId, String warehouseCode, String warehouseName, BigDecimal qty) {

    public static StockResponse from(Stock stock) {
        return new StockResponse(stock.getItem().getId(), stock.getItem().getSku(), stock.getItem().getName(),
                stock.getWarehouse().getId(), stock.getWarehouse().getCode(), stock.getWarehouse().getName(),
                stock.getQty());
    }
}
