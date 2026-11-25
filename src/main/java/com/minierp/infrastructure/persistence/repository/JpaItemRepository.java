package com.minierp.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.port.out.ItemRepositoryPort;
import com.minierp.infrastructure.persistence.entity.ItemJpaEntity;
import com.minierp.infrastructure.persistence.mapper.ItemMapper;

/**
 * Persistence adapter — implements the item port using JPA.
 * The domain layer never sees JPA, only this adapter does.
 */
@Component
public class JpaItemRepository implements ItemRepositoryPort {

    private final SpringDataItemRepository repository;

    public JpaItemRepository(SpringDataItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return repository.findById(id).map(ItemMapper::toDomain);
    }

    @Override
    public Optional<Item> findBySku(String sku) {
        return repository.findBySku(sku).map(ItemMapper::toDomain);
    }

    @Override
    public boolean existsBySku(String sku) {
        return repository.existsBySku(sku);
    }

    @Override
    public Item save(Item item) {
        ItemJpaEntity saved = repository.save(ItemMapper.toEntity(item));
        return ItemMapper.toDomain(saved);
    }

    @Override
    public PageResult<Item> findPage(Boolean active, PageQuery query) {
        Page<ItemJpaEntity> page = repository.findPage(active, Pagination.toPageable(query));
        return new PageResult<>(page.getContent().stream().map(ItemMapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
