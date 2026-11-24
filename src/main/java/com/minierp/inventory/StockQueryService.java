package com.minierp.inventory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minierp.common.AuthenticatedUser;
import com.minierp.common.NotFoundException;
import com.minierp.common.PageResponse;
import com.minierp.common.Pagination;
import com.minierp.inventory.dto.ItemStockResponse;
import com.minierp.inventory.dto.StockMovementResponse;
import com.minierp.inventory.dto.StockResponse;
import com.minierp.item.Item;
import com.minierp.item.ItemRepository;

/**
 * Read-only stock queries: current balances and the stock card (movement history).
 */
@Service
public class StockQueryService {

    private static final Set<String> STOCK_SORT_PROPERTIES =
            Set.of("qty", "item.sku", "item.name", "warehouse.code", "warehouse.name");
    private static final Sort STOCK_DEFAULT_SORT = Sort.by("item.sku", "warehouse.code");

    private static final Set<String> MOVEMENT_SORT_PROPERTIES =
            Set.of("id", "createdAt", "movementType", "qty", "refType", "refNumber");
    private static final Sort MOVEMENT_DEFAULT_SORT =
            Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ItemRepository itemRepository;

    public StockQueryService(StockRepository stockRepository, StockMovementRepository stockMovementRepository,
            ItemRepository itemRepository) {
        this.stockRepository = stockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.itemRepository = itemRepository;
    }

    /**
     * Stock balances, optionally filtered by item and/or warehouse.
     */
    @Transactional(readOnly = true)
    public PageResponse<StockResponse> listStock(AuthenticatedUser actor, Long itemId, Long warehouseId,
            Pageable pageable) {
        Pageable effective = Pagination.normalize(pageable, STOCK_SORT_PROPERTIES, STOCK_DEFAULT_SORT);
        return PageResponse.from(stockRepository.findPageWithItemAndWarehouse(itemId, warehouseId, effective)
                .map(StockResponse::from));
    }

    /**
     * Stock of one item in every warehouse that holds it, with the total quantity.
     */
    @Transactional(readOnly = true)
    public ItemStockResponse getItemStock(AuthenticatedUser actor, Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Item", itemId));
        List<StockResponse> warehouseStocks = stockRepository.findAllWithItemAndWarehouse(itemId, null).stream()
                .map(StockResponse::from)
                .toList();
        BigDecimal totalQty = warehouseStocks.stream()
                .map(StockResponse::qty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ItemStockResponse(item.getId(), item.getSku(), item.getName(), item.getUom(),
                totalQty, warehouseStocks);
    }

    /**
     * Stock card movements (newest first), optionally filtered by item and/or warehouse.
     */
    @Transactional(readOnly = true)
    public PageResponse<StockMovementResponse> listMovements(AuthenticatedUser actor, Long itemId, Long warehouseId,
            Pageable pageable) {
        Pageable effective = Pagination.normalize(pageable, MOVEMENT_SORT_PROPERTIES, MOVEMENT_DEFAULT_SORT);
        return PageResponse.from(stockMovementRepository.findPageWithItemAndWarehouse(itemId, warehouseId, effective)
                .map(StockMovementResponse::from));
    }
}
