package com.minierp.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.model.supplier.Supplier;
import com.minierp.domain.port.out.SupplierRepositoryPort;
import com.minierp.infrastructure.persistence.entity.SupplierJpaEntity;
import com.minierp.infrastructure.persistence.mapper.SupplierMapper;

/**
 * Persistence adapter — implements the supplier port using JPA.
 * The domain layer never sees JPA, only this adapter does.
 */
@Component
public class JpaSupplierRepository implements SupplierRepositoryPort {

    private final SpringDataSupplierRepository repository;

    public JpaSupplierRepository(SpringDataSupplierRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Supplier> findById(Long id) {
        return repository.findById(id).map(SupplierMapper::toDomain);
    }

    @Override
    public Optional<Supplier> findByCode(String code) {
        return repository.findByCode(code).map(SupplierMapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return repository.existsByCode(code);
    }

    @Override
    public Supplier save(Supplier supplier) {
        SupplierJpaEntity saved = repository.save(SupplierMapper.toEntity(supplier));
        return SupplierMapper.toDomain(saved);
    }

    @Override
    public PageResult<Supplier> findPage(Boolean active, PageQuery query) {
        Page<SupplierJpaEntity> page = repository.findPage(active, Pagination.toPageable(query));
        return new PageResult<>(page.getContent().stream().map(SupplierMapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
