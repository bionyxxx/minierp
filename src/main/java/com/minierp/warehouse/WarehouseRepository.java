package com.minierp.warehouse;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByCode(String code);

    boolean existsByCode(String code);

    @Query("select w from Warehouse w where (:active is null or w.active = :active)")
    Page<Warehouse> findPage(@Param("active") Boolean active, Pageable pageable);
}
