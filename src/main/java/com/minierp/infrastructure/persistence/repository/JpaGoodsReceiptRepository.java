package com.minierp.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.purchase.GoodsReceipt;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.port.out.GoodsReceiptRepositoryPort;
import com.minierp.infrastructure.persistence.entity.GoodsReceiptJpaEntity;
import com.minierp.infrastructure.persistence.mapper.GoodsReceiptMapper;

/**
 * Persistence adapter — implements the Goods Receipt port using JPA.
 * The domain layer never sees JPA, only this adapter does.
 */
@Component
public class JpaGoodsReceiptRepository implements GoodsReceiptRepositoryPort {

    private final SpringDataGoodsReceiptRepository repository;

    public JpaGoodsReceiptRepository(SpringDataGoodsReceiptRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<GoodsReceipt> findById(Long id) {
        return repository.findById(id).map(GoodsReceiptMapper::toDomain);
    }

    @Override
    public List<GoodsReceipt> findByPurchaseOrderId(Long purchaseOrderId) {
        return repository.findByPurchaseOrderId(purchaseOrderId).stream()
                .map(GoodsReceiptMapper::toDomain)
                .toList();
    }

    @Override
    public GoodsReceipt save(GoodsReceipt goodsReceipt) {
        GoodsReceiptJpaEntity saved = repository.save(GoodsReceiptMapper.toEntity(goodsReceipt));
        return GoodsReceiptMapper.toDomain(saved);
    }

    @Override
    public PageResult<GoodsReceipt> findPage(Long purchaseOrderId, DocumentStatus status, PageQuery query) {
        Page<GoodsReceiptJpaEntity> page = repository.findPage(purchaseOrderId, status,
                Pagination.toPageable(query));
        return new PageResult<>(page.getContent().stream().map(GoodsReceiptMapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
