package com.minierp.warehouse;

import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minierp.common.AuthenticatedUser;
import com.minierp.common.DuplicateResourceException;
import com.minierp.common.NotFoundException;
import com.minierp.common.PageResponse;
import com.minierp.common.Pagination;
import com.minierp.warehouse.dto.WarehouseRequest;
import com.minierp.warehouse.dto.WarehouseResponse;

/**
 * Master data CRUD for warehouses.
 */
@Service
public class WarehouseService {

    private static final Set<String> SORT_PROPERTIES =
            Set.of("id", "code", "name", "location", "active");

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<WarehouseResponse> list(AuthenticatedUser actor, Boolean active, Pageable pageable) {
        Pageable effective = Pagination.normalize(pageable, SORT_PROPERTIES, Sort.by("code"));
        return PageResponse.from(warehouseRepository.findPage(active, effective).map(WarehouseResponse::from));
    }

    @Transactional(readOnly = true)
    public WarehouseResponse get(AuthenticatedUser actor, Long id) {
        return WarehouseResponse.from(findOrThrow(id));
    }

    @Transactional
    public WarehouseResponse create(AuthenticatedUser actor, WarehouseRequest request) {
        ensureCodeAvailable(request.code());
        Warehouse warehouse = new Warehouse();
        apply(warehouse, request);
        return WarehouseResponse.from(warehouseRepository.save(warehouse));
    }

    @Transactional
    public WarehouseResponse update(AuthenticatedUser actor, Long id, WarehouseRequest request) {
        Warehouse warehouse = findOrThrow(id);
        if (!warehouse.getCode().equals(request.code())) {
            ensureCodeAvailable(request.code());
        }
        apply(warehouse, request);
        return WarehouseResponse.from(warehouseRepository.save(warehouse));
    }

    /**
     * Soft delete: the warehouse stays referenced by stock/documents but is deactivated.
     */
    @Transactional
    public void delete(AuthenticatedUser actor, Long id) {
        Warehouse warehouse = findOrThrow(id);
        warehouse.setActive(false);
        warehouseRepository.save(warehouse);
    }

    private Warehouse findOrThrow(Long id) {
        return warehouseRepository.findById(id).orElseThrow(() -> new NotFoundException("Warehouse", id));
    }

    private void ensureCodeAvailable(String code) {
        if (warehouseRepository.existsByCode(code)) {
            throw new DuplicateResourceException("Kode %s sudah digunakan".formatted(code));
        }
    }

    private void apply(Warehouse warehouse, WarehouseRequest request) {
        warehouse.setCode(request.code());
        warehouse.setName(request.name());
        warehouse.setLocation(request.location());
    }
}
