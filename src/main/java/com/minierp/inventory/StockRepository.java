package com.minierp.inventory;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minierp.item.Item;
import com.minierp.warehouse.Warehouse;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByItemAndWarehouse(Item item, Warehouse warehouse);

    /**
     * Stock balances with item and warehouse fetched, optionally filtered by
     * item and/or warehouse. Ordered by SKU then warehouse code.
     */
    @Query("select s from Stock s"
            + " join fetch s.item i"
            + " join fetch s.warehouse w"
            + " where (:itemId is null or i.id = :itemId)"
            + " and (:warehouseId is null or w.id = :warehouseId)"
            + " order by i.sku, w.code")
    List<Stock> findAllWithItemAndWarehouse(@Param("itemId") Long itemId, @Param("warehouseId") Long warehouseId);

    /**
     * Paginated variant of {@link #findAllWithItemAndWarehouse(Long, Long)}.
     * The count query is explicit because a fetch join cannot be counted as-is.
     */
    @Query(value = "select s from Stock s"
            + " join fetch s.item i"
            + " join fetch s.warehouse w"
            + " where (:itemId is null or i.id = :itemId)"
            + " and (:warehouseId is null or w.id = :warehouseId)",
            countQuery = "select count(s) from Stock s"
            + " join s.item i"
            + " join s.warehouse w"
            + " where (:itemId is null or i.id = :itemId)"
            + " and (:warehouseId is null or w.id = :warehouseId)")
    Page<Stock> findPageWithItemAndWarehouse(@Param("itemId") Long itemId, @Param("warehouseId") Long warehouseId,
            Pageable pageable);
}
