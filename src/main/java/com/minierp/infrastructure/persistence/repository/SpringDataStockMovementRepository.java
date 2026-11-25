package com.minierp.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minierp.infrastructure.persistence.entity.StockMovementJpaEntity;

/**
 * Spring Data repository for stock card entries — internal to the
 * infrastructure layer; the application talks to
 * {@code StockMovementRepositoryPort} instead.
 */
public interface SpringDataStockMovementRepository extends JpaRepository<StockMovementJpaEntity, Long> {

    /**
     * Stock card movements with item and warehouse fetched, optionally filtered
     * by item and/or warehouse. Newest first.
     */
    @Query("select m from StockMovementJpaEntity m"
            + " join fetch m.item i"
            + " join fetch m.warehouse w"
            + " where (:itemId is null or i.id = :itemId)"
            + " and (:warehouseId is null or w.id = :warehouseId)"
            + " order by m.createdAt desc, m.id desc")
    List<StockMovementJpaEntity> findAllWithItemAndWarehouse(@Param("itemId") Long itemId,
            @Param("warehouseId") Long warehouseId);

    /**
     * Paginated variant of {@link #findAllWithItemAndWarehouse(Long, Long)}.
     * The count query is explicit because a fetch join cannot be counted as-is.
     */
    @Query(value = "select m from StockMovementJpaEntity m"
            + " join fetch m.item i"
            + " join fetch m.warehouse w"
            + " where (:itemId is null or i.id = :itemId)"
            + " and (:warehouseId is null or w.id = :warehouseId)",
            countQuery = "select count(m) from StockMovementJpaEntity m"
            + " join m.item i"
            + " join m.warehouse w"
            + " where (:itemId is null or i.id = :itemId)"
            + " and (:warehouseId is null or w.id = :warehouseId)")
    Page<StockMovementJpaEntity> findPageWithItemAndWarehouse(@Param("itemId") Long itemId,
            @Param("warehouseId") Long warehouseId, Pageable pageable);
}
