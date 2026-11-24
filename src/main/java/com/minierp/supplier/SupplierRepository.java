package com.minierp.supplier;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByCode(String code);

    boolean existsByCode(String code);

    @Query("select s from Supplier s where (:active is null or s.active = :active)")
    Page<Supplier> findPage(@Param("active") Boolean active, Pageable pageable);
}
