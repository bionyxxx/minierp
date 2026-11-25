package com.minierp.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.sales.Delivery;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.port.out.DeliveryRepositoryPort;
import com.minierp.infrastructure.persistence.entity.DeliveryJpaEntity;
import com.minierp.infrastructure.persistence.mapper.DeliveryMapper;

/**
 * Persistence adapter — implements the Delivery port using JPA.
 * The domain layer never sees JPA, only this adapter does.
 */
@Component
public class JpaDeliveryRepository implements DeliveryRepositoryPort {

    private final SpringDataDeliveryRepository repository;

    public JpaDeliveryRepository(SpringDataDeliveryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Delivery> findById(Long id) {
        return repository.findById(id).map(DeliveryMapper::toDomain);
    }

    @Override
    public List<Delivery> findBySalesOrderId(Long salesOrderId) {
        return repository.findBySalesOrderId(salesOrderId).stream()
                .map(DeliveryMapper::toDomain)
                .toList();
    }

    @Override
    public Delivery save(Delivery delivery) {
        DeliveryJpaEntity saved = repository.save(DeliveryMapper.toEntity(delivery));
        return DeliveryMapper.toDomain(saved);
    }

    @Override
    public PageResult<Delivery> findPage(Long salesOrderId, DocumentStatus status, PageQuery query) {
        Page<DeliveryJpaEntity> page = repository.findPage(salesOrderId, status, Pagination.toPageable(query));
        return new PageResult<>(page.getContent().stream().map(DeliveryMapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
