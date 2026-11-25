package com.minierp.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.purchase.PurchaseOrder;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.port.out.PurchaseOrderRepositoryPort;
import com.minierp.infrastructure.persistence.entity.PurchaseOrderJpaEntity;
import com.minierp.infrastructure.persistence.mapper.PurchaseOrderMapper;

/**
 * Persistence adapter — implements the Purchase Order port using JPA.
 * The domain layer never sees JPA, only this adapter does.
 */
@Component
public class JpaPurchaseOrderRepository implements PurchaseOrderRepositoryPort {

    private final SpringDataPurchaseOrderRepository repository;

    public JpaPurchaseOrderRepository(SpringDataPurchaseOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PurchaseOrder> findById(Long id) {
        return repository.findById(id).map(PurchaseOrderMapper::toDomain);
    }

    @Override
    public PurchaseOrder save(PurchaseOrder purchaseOrder) {
        PurchaseOrderJpaEntity saved = repository.save(PurchaseOrderMapper.toEntity(purchaseOrder));
        return PurchaseOrderMapper.toDomain(saved);
    }

    @Override
    public PageResult<PurchaseOrder> findPage(DocumentStatus status, Long supplierId, PageQuery query) {
        Page<PurchaseOrderJpaEntity> page = repository.findPage(status, supplierId, Pagination.toPageable(query));
        return new PageResult<>(page.getContent().stream().map(PurchaseOrderMapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
