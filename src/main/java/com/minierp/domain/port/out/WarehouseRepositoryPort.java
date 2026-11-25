package com.minierp.domain.port.out;

import java.util.Optional;

import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.model.warehouse.Warehouse;

/**
 * Driven port: warehouse persistence needed by the application.
 */
public interface WarehouseRepositoryPort {

    Optional<Warehouse> findById(Long id);

    Optional<Warehouse> findByCode(String code);

    boolean existsByCode(String code);

    Warehouse save(Warehouse warehouse);

    PageResult<Warehouse> findPage(Boolean active, PageQuery query);
}
