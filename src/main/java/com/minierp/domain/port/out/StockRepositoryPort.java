package com.minierp.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.minierp.domain.model.inventory.Stock;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;

/**
 * Driven port: stock balance persistence needed by the application.
 */
public interface StockRepositoryPort {

    Optional<Stock> findByItemIdAndWarehouseId(Long itemId, Long warehouseId);

    Stock save(Stock stock);

    /**
     * Stock balances with item and warehouse, optionally filtered by item
     * and/or warehouse. Ordered by SKU then warehouse code.
     */
    List<Stock> findAll(Long itemId, Long warehouseId);

    PageResult<Stock> findPage(Long itemId, Long warehouseId, PageQuery query);
}
