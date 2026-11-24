package com.minierp.inventory;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minierp.common.BusinessRuleException;
import com.minierp.item.Item;
import com.minierp.warehouse.Warehouse;

/**
 * Single place where stock balances change. Every change is recorded as a
 * {@link StockMovement} so the stock card always reflects in/out history.
 */
@Service
public class StockService {

    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;

    public StockService(StockRepository stockRepository, StockMovementRepository stockMovementRepository) {
        this.stockRepository = stockRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    /**
     * Increases stock (goods receipt) and records an IN movement.
     */
    @Transactional
    public void increase(Item item, Warehouse warehouse, BigDecimal qty,
            StockMovementRef refType, Long refId, String refNumber, String remark, String actor) {
        Stock stock = stockRepository.findByItemAndWarehouse(item, warehouse)
                .orElseGet(() -> stockRepository.save(new Stock(item, warehouse, BigDecimal.ZERO)));
        stock.setQty(stock.getQty().add(qty));
        stockMovementRepository.save(new StockMovement(item, warehouse, StockMovementType.IN, qty,
                refType, refId, refNumber, remark, actor));
    }

    /**
     * Decreases stock (delivery) and records an OUT movement.
     *
     * @throws BusinessRuleException when stock in the warehouse is insufficient
     */
    @Transactional
    public void decrease(Item item, Warehouse warehouse, BigDecimal qty,
            StockMovementRef refType, Long refId, String refNumber, String remark, String actor) {
        Stock stock = stockRepository.findByItemAndWarehouse(item, warehouse)
                .orElseThrow(() -> new BusinessRuleException(insufficientMessage(item, warehouse, qty, BigDecimal.ZERO)));
        if (stock.getQty().compareTo(qty) < 0) {
            throw new BusinessRuleException(insufficientMessage(item, warehouse, qty, stock.getQty()));
        }
        stock.setQty(stock.getQty().subtract(qty));
        stockMovementRepository.save(new StockMovement(item, warehouse, StockMovementType.OUT, qty,
                refType, refId, refNumber, remark, actor));
    }

    /**
     * Current stock of an item in a warehouse (zero when never stocked).
     */
    @Transactional(readOnly = true)
    public BigDecimal availableQty(Item item, Warehouse warehouse) {
        return stockRepository.findByItemAndWarehouse(item, warehouse)
                .map(Stock::getQty)
                .orElse(BigDecimal.ZERO);
    }

    private String insufficientMessage(Item item, Warehouse warehouse, BigDecimal requested, BigDecimal available) {
        return "Stok tidak mencukupi: item %s di gudang %s tersedia %s, diminta %s"
                .formatted(item.getSku(), warehouse.getCode(), available.toPlainString(), requested.toPlainString());
    }
}
