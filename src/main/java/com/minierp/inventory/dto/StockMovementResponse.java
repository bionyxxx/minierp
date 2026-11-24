package com.minierp.inventory.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.minierp.inventory.StockMovement;
import com.minierp.inventory.StockMovementRef;
import com.minierp.inventory.StockMovementType;
import com.minierp.item.Item;
import com.minierp.warehouse.Warehouse;

/**
 * Stock card entry: one goods in/out movement.
 */
public record StockMovementResponse(Long id, Long itemId, String sku, String itemName,
        Long warehouseId, String warehouseCode, StockMovementType movementType, BigDecimal qty,
        StockMovementRef refType, String refNumber, String remark, String createdBy, Instant createdAt) {

    public static StockMovementResponse from(StockMovement movement) {
        Item item = movement.getItem();
        Warehouse warehouse = movement.getWarehouse();
        return new StockMovementResponse(movement.getId(), item.getId(), item.getSku(), item.getName(),
                warehouse.getId(), warehouse.getCode(), movement.getMovementType(), movement.getQty(),
                movement.getRefType(), movement.getRefNumber(), movement.getRemark(),
                movement.getCreatedBy(), movement.getCreatedAt());
    }
}
