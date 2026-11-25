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
import com.minierp.domain.model.supplier.Supplier;
import com.minierp.domain.port.in.SupplierCommand;
import com.minierp.domain.port.in.SupplierUseCase;
import com.minierp.domain.port.out.SupplierRepositoryPort;

/**
 * Master data CRUD for suppliers.
 */
@Service
public class SupplierService implements SupplierUseCase {

    private static final Set<String> SORT_PROPERTIES =
            Set.of("id", "code", "name", "address", "phone", "email", "active");

    private static final SortField DEFAULT_SORT = SortField.asc("code");

    private final SupplierRepositoryPort supplierRepository;

    public SupplierService(SupplierRepositoryPort supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public PageResult<Supplier> list(AuthenticatedUser actor, Boolean active, PageQuery query) {
        return supplierRepository.findPage(active, Pagination.normalize(query, SORT_PROPERTIES, DEFAULT_SORT));
    }

    @Transactional(readOnly = true)
    @Override
    public Supplier get(AuthenticatedUser actor, Long id) {
        return findOrThrow(id);
    }

    @Transactional
    @Override
    public Supplier create(AuthenticatedUser actor, SupplierCommand command) {
        ensureCodeAvailable(command.code());
        Supplier supplier = Supplier.create(command.code(), command.name(), command.address(),
                command.phone(), command.email());
        return supplierRepository.save(supplier);
    }

    @Transactional
    @Override
    public Supplier update(AuthenticatedUser actor, Long id, SupplierCommand command) {
        Supplier supplier = findOrThrow(id);
        if (!supplier.getCode().equals(command.code())) {
            ensureCodeAvailable(command.code());
        }
        supplier.update(command.code(), command.name(), command.address(), command.phone(), command.email());
        return supplierRepository.save(supplier);
    }

    /**
     * Soft delete: the supplier stays referenced by documents but is deactivated.
     */
    @Transactional
    @Override
    public void delete(AuthenticatedUser actor, Long id) {
        Supplier supplier = findOrThrow(id);
        supplier.deactivate();
        supplierRepository.save(supplier);
    }

    private Supplier findOrThrow(Long id) {
        return supplierRepository.findById(id).orElseThrow(() -> new NotFoundException("Supplier", id));
    }

    private void ensureCodeAvailable(String code) {
        if (supplierRepository.existsByCode(code)) {
            throw new DuplicateResourceException("Kode %s sudah digunakan".formatted(code));
        }
    }
}
