package com.minierp.infrastructure.web.supplier;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.model.supplier.Supplier;
import com.minierp.domain.port.in.SupplierUseCase;
import com.minierp.infrastructure.security.CurrentUser;
import com.minierp.infrastructure.web.supplier.dto.SupplierRequest;
import com.minierp.infrastructure.web.supplier.dto.SupplierResponse;
import com.minierp.infrastructure.web.support.PageResponse;

import jakarta.validation.Valid;

/**
 * Supplier master data adapter.
 */
@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierUseCase supplierUseCase;
    private final CurrentUser currentUser;

    public SupplierController(SupplierUseCase supplierUseCase, CurrentUser currentUser) {
        this.supplierUseCase = supplierUseCase;
        this.currentUser = currentUser;
    }

    @GetMapping
    public PageResponse<SupplierResponse> list(@RequestParam(required = false) Boolean active, Pageable pageable) {
        PageResult<Supplier> page = supplierUseCase.list(currentUser.get(), active, Pagination.toQuery(pageable));
        return PageResponse.from(page.map(SupplierResponse::from));
    }

    @GetMapping("/{id}")
    public SupplierResponse get(@PathVariable Long id) {
        return SupplierResponse.from(supplierUseCase.get(currentUser.get(), id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierRequest request) {
        SupplierResponse response = SupplierResponse.from(supplierUseCase.create(currentUser.get(), request.toCommand()));
        return ResponseEntity.created(URI.create("/api/suppliers/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public SupplierResponse update(@PathVariable Long id, @Valid @RequestBody SupplierRequest request) {
        return SupplierResponse.from(supplierUseCase.update(currentUser.get(), id, request.toCommand()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        supplierUseCase.delete(currentUser.get(), id);
    }
}
