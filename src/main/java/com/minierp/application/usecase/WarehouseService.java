package com.minierp.application.usecase;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.DuplicateResourceException;
import com.minierp.domain.model.shared.NotFoundException;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.model.shared.SortField;
import com.minierp.domain.model.warehouse.Warehouse;
import com.minierp.domain.port.in.WarehouseCommand;
import com.minierp.domain.port.in.WarehouseUseCase;
import com.minierp.domain.port.out.WarehouseRepositoryPort;

/**
 * Master data CRUD for warehouses.
 */
@Service
public class WarehouseService implements WarehouseUseCase {

    private static final Set<String> SORT_PROPERTIES =
            Set.of("id", "code", "name", "location", "active");

    private static final SortField DEFAULT_SORT = SortField.asc("code");

    private final WarehouseRepositoryPort warehouseRepository;

    public WarehouseService(WarehouseRepositoryPort warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public PageResult<Warehouse> list(AuthenticatedUser actor, Boolean active, PageQuery query) {
        return warehouseRepository.findPage(active, Pagination.normalize(query, SORT_PROPERTIES, DEFAULT_SORT));
    }

    @Transactional(readOnly = true)
    @Override
    public Warehouse get(AuthenticatedUser actor, Long id) {
        return findOrThrow(id);
    }

    @Transactional
    @Override
    public Warehouse create(AuthenticatedUser actor, WarehouseCommand command) {
        ensureCodeAvailable(command.code());
        Warehouse warehouse = Warehouse.create(command.code(), command.name(), command.location());
        return warehouseRepository.save(warehouse);
    }

    @Transactional
    @Override
    public Warehouse update(AuthenticatedUser actor, Long id, WarehouseCommand command) {
        Warehouse warehouse = findOrThrow(id);
        if (!warehouse.getCode().equals(command.code())) {
            ensureCodeAvailable(command.code());
        }
        warehouse.update(command.code(), command.name(), command.location());
        return warehouseRepository.save(warehouse);
    }

    /**
     * Soft delete: the warehouse stays referenced by stock/documents but is deactivated.
     */
    @Transactional
    @Override
    public void delete(AuthenticatedUser actor, Long id) {
        Warehouse warehouse = findOrThrow(id);
        warehouse.deactivate();
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
}
