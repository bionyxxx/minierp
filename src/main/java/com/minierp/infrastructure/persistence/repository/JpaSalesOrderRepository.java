package com.minierp.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.sales.SalesOrder;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.port.out.SalesOrderRepositoryPort;
import com.minierp.infrastructure.persistence.entity.SalesOrderJpaEntity;
import com.minierp.infrastructure.persistence.mapper.SalesOrderMapper;

/**
 * Persistence adapter — implements the Sales Order port using JPA.
 * The domain layer never sees JPA, only this adapter does.
 */
@Component
public class JpaSalesOrderRepository implements SalesOrderRepositoryPort {

    private final SpringDataSalesOrderRepository repository;

    public JpaSalesOrderRepository(SpringDataSalesOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<SalesOrder> findById(Long id) {
        return repository.findById(id).map(SalesOrderMapper::toDomain);
    }

    @Override
    public SalesOrder save(SalesOrder salesOrder) {
        SalesOrderJpaEntity saved = repository.save(SalesOrderMapper.toEntity(salesOrder));
        return SalesOrderMapper.toDomain(saved);
    }

    @Override
    public PageResult<SalesOrder> findPage(DocumentStatus status, Long customerId, PageQuery query) {
        Page<SalesOrderJpaEntity> page = repository.findPage(status, customerId, Pagination.toPageable(query));
        return new PageResult<>(page.getContent().stream().map(SalesOrderMapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
