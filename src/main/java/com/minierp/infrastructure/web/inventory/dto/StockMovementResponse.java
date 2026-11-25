package com.minierp.infrastructure.web.inventory.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.minierp.domain.model.inventory.StockMovement;
import com.minierp.domain.model.inventory.StockMovementRef;
import com.minierp.domain.model.inventory.StockMovementType;

/**
 * Stock card entry: one goods in/out movement.
 */
public record StockMovementResponse(Long id, Long itemId, String sku, String itemName,
        Long warehouseId, String warehouseCode, StockMovementType movementType, BigDecimal qty,
        StockMovementRef refType, String refNumber, String remark, String createdBy, Instant createdAt) {

    public static StockMovementResponse from(StockMovement movement) {
        return new StockMovementResponse(movement.getId(), movement.getItem().getId(), movement.getItem().getSku(),
                movement.getItem().getName(), movement.getWarehouse().getId(), movement.getWarehouse().getCode(),
                movement.getMovementType(), movement.getQty(), movement.getRefType(), movement.getRefNumber(),
                movement.getRemark(), movement.getCreatedBy(), movement.getCreatedAt());
    }
}
