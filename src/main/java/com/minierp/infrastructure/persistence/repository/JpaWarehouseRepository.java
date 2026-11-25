package com.minierp.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.model.warehouse.Warehouse;
import com.minierp.domain.port.out.WarehouseRepositoryPort;
import com.minierp.infrastructure.persistence.entity.WarehouseJpaEntity;
import com.minierp.infrastructure.persistence.mapper.WarehouseMapper;

/**
 * Persistence adapter — implements the warehouse port using JPA.
 * The domain layer never sees JPA, only this adapter does.
 */
@Component
public class JpaWarehouseRepository implements WarehouseRepositoryPort {

    private final SpringDataWarehouseRepository repository;

    public JpaWarehouseRepository(SpringDataWarehouseRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Warehouse> findById(Long id) {
        return repository.findById(id).map(WarehouseMapper::toDomain);
    }

    @Override
    public Optional<Warehouse> findByCode(String code) {
        return repository.findByCode(code).map(WarehouseMapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return repository.existsByCode(code);
    }

    @Override
    public Warehouse save(Warehouse warehouse) {
        WarehouseJpaEntity saved = repository.save(WarehouseMapper.toEntity(warehouse));
        return WarehouseMapper.toDomain(saved);
    }

    @Override
    public PageResult<Warehouse> findPage(Boolean active, PageQuery query) {
        Page<WarehouseJpaEntity> page = repository.findPage(active, Pagination.toPageable(query));
        return new PageResult<>(page.getContent().stream().map(WarehouseMapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
