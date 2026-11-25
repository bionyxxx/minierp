package com.minierp.domain.port.out;

import java.util.Optional;

import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.model.supplier.Supplier;

/**
 * Driven port: supplier persistence needed by the application.
 */
public interface SupplierRepositoryPort {

    Optional<Supplier> findById(Long id);

    Optional<Supplier> findByCode(String code);

    boolean existsByCode(String code);

    Supplier save(Supplier supplier);

    PageResult<Supplier> findPage(Boolean active, PageQuery query);
}
