package com.minierp.domain.port.in;

import com.minierp.domain.model.inventory.ItemStock;
import com.minierp.domain.model.inventory.Stock;
import com.minierp.domain.model.inventory.StockMovement;
import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;

/**
 * Read-only stock queries offered by the application: current balances and the
 * stock card (movement history).
 */
public interface StockQueryUseCase {

    /**
     * Stock balances, optionally filtered by item and/or warehouse.
     */
    PageResult<Stock> listStock(AuthenticatedUser actor, Long itemId, Long warehouseId, PageQuery query);

    /**
     * Stock of one item in every warehouse that holds it, with the total quantity.
     */
    ItemStock getItemStock(AuthenticatedUser actor, Long itemId);

    /**
     * Stock card movements (newest first), optionally filtered by item and/or warehouse.
     */
    PageResult<StockMovement> listMovements(AuthenticatedUser actor, Long itemId, Long warehouseId, PageQuery query);
}
