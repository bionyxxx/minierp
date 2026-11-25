package com.minierp.infrastructure.web.inventory;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.inventory.Stock;
import com.minierp.domain.model.inventory.StockMovement;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.port.in.StockQueryUseCase;
import com.minierp.infrastructure.security.CurrentUser;
import com.minierp.infrastructure.web.inventory.dto.ItemStockResponse;
import com.minierp.infrastructure.web.inventory.dto.StockMovementResponse;
import com.minierp.infrastructure.web.inventory.dto.StockResponse;
import com.minierp.infrastructure.web.support.PageResponse;

/**
 * Read-only stock adapter: balances and stock card. Available to all roles.
 */
@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockQueryUseCase stockQueryUseCase;
    private final CurrentUser currentUser;

    public StockController(StockQueryUseCase stockQueryUseCase, CurrentUser currentUser) {
        this.stockQueryUseCase = stockQueryUseCase;
        this.currentUser = currentUser;
    }

    @GetMapping
    public PageResponse<StockResponse> list(@RequestParam(required = false) Long itemId,
                                            @RequestParam(required = false) Long warehouseId, Pageable pageable) {
        PageResult<Stock> page = stockQueryUseCase.listStock(currentUser.get(), itemId, warehouseId,
                Pagination.toQuery(pageable));
        return PageResponse.from(page.map(StockResponse::from));
    }

    @GetMapping("/movements")
    public PageResponse<StockMovementResponse> movements(@RequestParam(required = false) Long itemId,
                                                         @RequestParam(required = false) Long warehouseId,
                                                         Pageable pageable) {
        PageResult<StockMovement> page = stockQueryUseCase.listMovements(currentUser.get(), itemId, warehouseId,
                Pagination.toQuery(pageable));
        return PageResponse.from(page.map(StockMovementResponse::from));
    }

    @GetMapping("/{itemId}")
    public ItemStockResponse byItem(@PathVariable Long itemId) {
        return ItemStockResponse.from(stockQueryUseCase.getItemStock(currentUser.get(), itemId));
    }
}
