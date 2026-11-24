package com.minierp.supplier;

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
import com.minierp.supplier.dto.SupplierRequest;
import com.minierp.supplier.dto.SupplierResponse;

/**
 * Master data CRUD for suppliers.
 */
@Service
public class SupplierService {

    private static final Set<String> SORT_PROPERTIES =
            Set.of("id", "code", "name", "address", "phone", "email", "active");

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<SupplierResponse> list(AuthenticatedUser actor, Boolean active, Pageable pageable) {
        Pageable effective = Pagination.normalize(pageable, SORT_PROPERTIES, Sort.by("code"));
        return PageResponse.from(supplierRepository.findPage(active, effective).map(SupplierResponse::from));
    }

    @Transactional(readOnly = true)
    public SupplierResponse get(AuthenticatedUser actor, Long id) {
        return SupplierResponse.from(findOrThrow(id));
    }

    @Transactional
    public SupplierResponse create(AuthenticatedUser actor, SupplierRequest request) {
        ensureCodeAvailable(request.code());
        Supplier supplier = new Supplier();
        apply(supplier, request);
        return SupplierResponse.from(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse update(AuthenticatedUser actor, Long id, SupplierRequest request) {
        Supplier supplier = findOrThrow(id);
        if (!supplier.getCode().equals(request.code())) {
            ensureCodeAvailable(request.code());
        }
        apply(supplier, request);
        return SupplierResponse.from(supplierRepository.save(supplier));
    }

    /**
     * Soft delete: the supplier stays referenced by documents but is deactivated.
     */
    @Transactional
    public void delete(AuthenticatedUser actor, Long id) {
        Supplier supplier = findOrThrow(id);
        supplier.setActive(false);
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

    private void apply(Supplier supplier, SupplierRequest request) {
        supplier.setCode(request.code());
        supplier.setName(request.name());
        supplier.setAddress(request.address());
        supplier.setPhone(request.phone());
        supplier.setEmail(request.email());
    }
}
