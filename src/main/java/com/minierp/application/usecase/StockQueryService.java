package com.minierp.application.usecase;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.inventory.ItemStock;
import com.minierp.domain.model.inventory.Stock;
import com.minierp.domain.model.inventory.StockMovement;
import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.NotFoundException;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.model.shared.SortField;
import com.minierp.domain.port.in.StockQueryUseCase;
import com.minierp.domain.port.out.ItemRepositoryPort;
import com.minierp.domain.port.out.StockMovementRepositoryPort;
import com.minierp.domain.port.out.StockRepositoryPort;

/**
 * Read-only stock queries: current balances and the stock card (movement history).
 */
@Service
public class StockQueryService implements StockQueryUseCase {

    private static final Set<String> STOCK_SORT_PROPERTIES =
            Set.of("qty", "item.sku", "item.name", "warehouse.code", "warehouse.name");
    private static final List<SortField> STOCK_DEFAULT_SORT =
            List.of(SortField.asc("item.sku"), SortField.asc("warehouse.code"));

    private static final Set<String> MOVEMENT_SORT_PROPERTIES =
            Set.of("id", "createdAt", "movementType", "qty", "refType", "refNumber");
    private static final List<SortField> MOVEMENT_DEFAULT_SORT =
            List.of(SortField.desc("createdAt"), SortField.desc("id"));

    private final StockRepositoryPort stockRepository;
    private final StockMovementRepositoryPort stockMovementRepository;
    private final ItemRepositoryPort itemRepository;

    public StockQueryService(StockRepositoryPort stockRepository,
            StockMovementRepositoryPort stockMovementRepository, ItemRepositoryPort itemRepository) {
        this.stockRepository = stockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.itemRepository = itemRepository;
    }

    /**
     * Stock balances, optionally filtered by item and/or warehouse.
     */
    @Transactional(readOnly = true)
    @Override
    public PageResult<Stock> listStock(AuthenticatedUser actor, Long itemId, Long warehouseId, PageQuery query) {
        return stockRepository.findPage(itemId, warehouseId,
                Pagination.normalize(query, STOCK_SORT_PROPERTIES, STOCK_DEFAULT_SORT));
    }

    /**
     * Stock of one item in every warehouse that holds it, with the total quantity.
     */
    @Transactional(readOnly = true)
    @Override
    public ItemStock getItemStock(AuthenticatedUser actor, Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Item", itemId));
        List<Stock> warehouseStocks = stockRepository.findAll(itemId, null);
        return new ItemStock(item, warehouseStocks);
    }

    /**
     * Stock card movements (newest first), optionally filtered by item and/or warehouse.
     */
    @Transactional(readOnly = true)
    @Override
    public PageResult<StockMovement> listMovements(AuthenticatedUser actor, Long itemId, Long warehouseId,
            PageQuery query) {
        return stockMovementRepository.findPage(itemId, warehouseId,
                Pagination.normalize(query, MOVEMENT_SORT_PROPERTIES, MOVEMENT_DEFAULT_SORT));
    }
}
