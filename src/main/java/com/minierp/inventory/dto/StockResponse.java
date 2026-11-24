package com.minierp.inventory.dto;

import java.math.BigDecimal;

import com.minierp.inventory.Stock;
import com.minierp.item.Item;
import com.minierp.warehouse.Warehouse;

/**
 * Stock balance of one item in one warehouse.
 */
public record StockResponse(Long itemId, String sku, String itemName,
        Long warehouseId, String warehouseCode, String warehouseName, BigDecimal qty) {

    public static StockResponse from(Stock stock) {
        Item item = stock.getItem();
        Warehouse warehouse = stock.getWarehouse();
        return new StockResponse(item.getId(), item.getSku(), item.getName(),
                warehouse.getId(), warehouse.getCode(), warehouse.getName(), stock.getQty());
    }
}
