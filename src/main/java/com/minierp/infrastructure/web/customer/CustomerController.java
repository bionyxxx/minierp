package com.minierp.infrastructure.web.customer;

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
import com.minierp.domain.model.customer.Customer;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.port.in.CustomerUseCase;
import com.minierp.infrastructure.security.CurrentUser;
import com.minierp.infrastructure.web.customer.dto.CustomerRequest;
import com.minierp.infrastructure.web.customer.dto.CustomerResponse;
import com.minierp.infrastructure.web.support.PageResponse;

import jakarta.validation.Valid;

/**
 * Customer master data adapter.
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerUseCase customerUseCase;
    private final CurrentUser currentUser;

    public CustomerController(CustomerUseCase customerUseCase, CurrentUser currentUser) {
        this.customerUseCase = customerUseCase;
        this.currentUser = currentUser;
    }

    @GetMapping
    public PageResponse<CustomerResponse> list(@RequestParam(required = false) Boolean active, Pageable pageable) {
        PageResult<Customer> page = customerUseCase.list(currentUser.get(), active, Pagination.toQuery(pageable));
        return PageResponse.from(page.map(CustomerResponse::from));
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable Long id) {
        return CustomerResponse.from(customerUseCase.get(currentUser.get(), id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = CustomerResponse.from(customerUseCase.create(currentUser.get(), request.toCommand()));
        return ResponseEntity.created(URI.create("/api/customers/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return CustomerResponse.from(customerUseCase.update(currentUser.get(), id, request.toCommand()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        customerUseCase.delete(currentUser.get(), id);
    }
}
