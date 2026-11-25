package com.minierp.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.inventory.Stock;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.port.out.StockRepositoryPort;
import com.minierp.infrastructure.persistence.entity.StockJpaEntity;
import com.minierp.infrastructure.persistence.mapper.StockMapper;

/**
 * Persistence adapter — implements the stock balance port using JPA.
 * The domain layer never sees JPA, only this adapter does.
 */
@Component
public class JpaStockRepository implements StockRepositoryPort {

    private final SpringDataStockRepository repository;

    public JpaStockRepository(SpringDataStockRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Stock> findByItemIdAndWarehouseId(Long itemId, Long warehouseId) {
        return repository.findByItemIdAndWarehouseId(itemId, warehouseId).map(StockMapper::toDomain);
    }

    @Override
    public Stock save(Stock stock) {
        StockJpaEntity saved = repository.save(StockMapper.toEntity(stock));
        return StockMapper.toDomain(saved);
    }

    @Override
    public List<Stock> findAll(Long itemId, Long warehouseId) {
        return repository.findAllWithItemAndWarehouse(itemId, warehouseId).stream()
                .map(StockMapper::toDomain)
                .toList();
    }

    @Override
    public PageResult<Stock> findPage(Long itemId, Long warehouseId, PageQuery query) {
        Page<StockJpaEntity> page = repository.findPageWithItemAndWarehouse(itemId, warehouseId,
                Pagination.toPageable(query));
        return new PageResult<>(page.getContent().stream().map(StockMapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
