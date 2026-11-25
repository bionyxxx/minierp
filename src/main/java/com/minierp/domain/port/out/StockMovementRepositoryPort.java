package com.minierp.domain.port.out;

import java.util.List;

import com.minierp.domain.model.inventory.StockMovement;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;

/**
 * Driven port: stock card (movement history) persistence needed by the
 * application.
 */
public interface StockMovementRepositoryPort {

    StockMovement save(StockMovement movement);

    /**
     * Stock card movements with item and warehouse, optionally filtered by
     * item and/or warehouse. Newest first.
     */
    List<StockMovement> findAll(Long itemId, Long warehouseId);

    PageResult<StockMovement> findPage(Long itemId, Long warehouseId, PageQuery query);
}
