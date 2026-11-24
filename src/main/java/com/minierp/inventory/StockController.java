package com.minierp.inventory;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minierp.common.CurrentUser;
import com.minierp.common.PageResponse;
import com.minierp.inventory.dto.ItemStockResponse;
import com.minierp.inventory.dto.StockMovementResponse;
import com.minierp.inventory.dto.StockResponse;

/**
 * Read-only stock adapter: balances and stock card. Available to all roles.
 */
@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockQueryService stockQueryService;
    private final CurrentUser currentUser;

    public StockController(StockQueryService stockQueryService, CurrentUser currentUser) {
        this.stockQueryService = stockQueryService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public PageResponse<StockResponse> list(@RequestParam(required = false) Long itemId,
                                            @RequestParam(required = false) Long warehouseId, Pageable pageable) {
        return stockQueryService.listStock(currentUser.get(), itemId, warehouseId, pageable);
    }

    @GetMapping("/movements")
    public PageResponse<StockMovementResponse> movements(@RequestParam(required = false) Long itemId,
                                                         @RequestParam(required = false) Long warehouseId,
                                                         Pageable pageable) {
        return stockQueryService.listMovements(currentUser.get(), itemId, warehouseId, pageable);
    }

    @GetMapping("/{itemId}")
    public ItemStockResponse byItem(@PathVariable Long itemId) {
        return stockQueryService.getItemStock(currentUser.get(), itemId);
    }
}
