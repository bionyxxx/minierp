package com.minierp.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minierp.infrastructure.persistence.entity.StockJpaEntity;

/**
 * Spring Data repository for stock balances — internal to the infrastructure
 * layer; the application talks to {@code StockRepositoryPort} instead.
 */
public interface SpringDataStockRepository extends JpaRepository<StockJpaEntity, Long> {

    Optional<StockJpaEntity> findByItemIdAndWarehouseId(Long itemId, Long warehouseId);

    /**
     * Stock balances with item and warehouse fetched, optionally filtered by
     * item and/or warehouse. Ordered by SKU then warehouse code.
     */
    @Query("select s from StockJpaEntity s"
            + " join fetch s.item i"
            + " join fetch s.warehouse w"
            + " where (:itemId is null or i.id = :itemId)"
            + " and (:warehouseId is null or w.id = :warehouseId)"
            + " order by i.sku, w.code")
    List<StockJpaEntity> findAllWithItemAndWarehouse(@Param("itemId") Long itemId,
            @Param("warehouseId") Long warehouseId);

    /**
     * Paginated variant of {@link #findAllWithItemAndWarehouse(Long, Long)}.
     * The count query is explicit because a fetch join cannot be counted as-is.
     */
    @Query(value = "select s from StockJpaEntity s"
            + " join fetch s.item i"
            + " join fetch s.warehouse w"
            + " where (:itemId is null or i.id = :itemId)"
            + " and (:warehouseId is null or w.id = :warehouseId)",
            countQuery = "select count(s) from StockJpaEntity s"
            + " join s.item i"
            + " join s.warehouse w"
            + " where (:itemId is null or i.id = :itemId)"
            + " and (:warehouseId is null or w.id = :warehouseId)")
    Page<StockJpaEntity> findPageWithItemAndWarehouse(@Param("itemId") Long itemId,
            @Param("warehouseId") Long warehouseId, Pageable pageable);
}
