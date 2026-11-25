package com.minierp.application.usecase;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minierp.domain.model.inventory.Stock;
import com.minierp.domain.model.inventory.StockMovement;
import com.minierp.domain.model.inventory.StockMovementRef;
import com.minierp.domain.model.inventory.StockMovementType;
import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.warehouse.Warehouse;
import com.minierp.domain.port.out.StockMovementRepositoryPort;
import com.minierp.domain.port.out.StockPort;
import com.minierp.domain.port.out.StockRepositoryPort;

/**
 * Single place where stock balances change (implements {@link StockPort}).
 * Every change is recorded as a {@link StockMovement} so the stock card always
 * reflects in/out history.
 */
@Service
public class StockService implements StockPort {

    private final StockRepositoryPort stockRepository;
    private final StockMovementRepositoryPort stockMovementRepository;

    public StockService(StockRepositoryPort stockRepository, StockMovementRepositoryPort stockMovementRepository) {
        this.stockRepository = stockRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    /**
     * Increases stock (goods receipt) and records an IN movement.
     */
    @Transactional
    @Override
    public void increase(Item item, Warehouse warehouse, BigDecimal qty,
            StockMovementRef refType, Long refId, String refNumber, String remark, String actor) {
        Stock stock = stockRepository.findByItemIdAndWarehouseId(item.getId(), warehouse.getId())
                .orElseGet(() -> Stock.create(item, warehouse));
        stock.increase(qty);
        stockRepository.save(stock);
        stockMovementRepository.save(StockMovement.create(item, warehouse, StockMovementType.IN, qty,
                refType, refId, refNumber, remark, actor));
    }

    /**
     * Decreases stock (delivery) and records an OUT movement.
     */
    @Transactional
    @Override
    public void decrease(Item item, Warehouse warehouse, BigDecimal qty,
            StockMovementRef refType, Long refId, String refNumber, String remark, String actor) {
        Stock stock = stockRepository.findByItemIdAndWarehouseId(item.getId(), warehouse.getId())
                .orElseGet(() -> Stock.create(item, warehouse));
        stock.decrease(qty);
        stockRepository.save(stock);
        stockMovementRepository.save(StockMovement.create(item, warehouse, StockMovementType.OUT, qty,
                refType, refId, refNumber, remark, actor));
    }

    /**
     * Current stock of an item in a warehouse (zero when never stocked).
     */
    @Transactional(readOnly = true)
    @Override
    public BigDecimal availableQty(Item item, Warehouse warehouse) {
        return stockRepository.findByItemIdAndWarehouseId(item.getId(), warehouse.getId())
                .map(Stock::getQty)
                .orElse(BigDecimal.ZERO);
    }
}
