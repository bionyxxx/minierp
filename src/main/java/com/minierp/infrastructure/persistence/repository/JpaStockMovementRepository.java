package com.minierp.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.inventory.StockMovement;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.port.out.StockMovementRepositoryPort;
import com.minierp.infrastructure.persistence.entity.StockMovementJpaEntity;
import com.minierp.infrastructure.persistence.mapper.StockMovementMapper;

/**
 * Persistence adapter — implements the stock card port using JPA.
 * The domain layer never sees JPA, only this adapter does.
 */
@Component
public class JpaStockMovementRepository implements StockMovementRepositoryPort {

    private final SpringDataStockMovementRepository repository;

    public JpaStockMovementRepository(SpringDataStockMovementRepository repository) {
        this.repository = repository;
    }

    @Override
    public StockMovement save(StockMovement movement) {
        StockMovementJpaEntity saved = repository.save(StockMovementMapper.toEntity(movement));
        return StockMovementMapper.toDomain(saved);
    }

    @Override
    public List<StockMovement> findAll(Long itemId, Long warehouseId) {
        return repository.findAllWithItemAndWarehouse(itemId, warehouseId).stream()
                .map(StockMovementMapper::toDomain)
                .toList();
    }

    @Override
    public PageResult<StockMovement> findPage(Long itemId, Long warehouseId, PageQuery query) {
        Page<StockMovementJpaEntity> page = repository.findPageWithItemAndWarehouse(itemId, warehouseId,
                Pagination.toPageable(query));
        return new PageResult<>(page.getContent().stream().map(StockMovementMapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
