package com.minierp.infrastructure.web.purchase;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.purchase.GoodsReceipt;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.port.in.GoodsReceiptUseCase;
import com.minierp.infrastructure.security.CurrentUser;
import com.minierp.infrastructure.web.purchase.dto.GoodsReceiptRequest;
import com.minierp.infrastructure.web.purchase.dto.GoodsReceiptResponse;
import com.minierp.infrastructure.web.purchase.dto.RejectRequest;
import com.minierp.infrastructure.web.support.PageResponse;

import jakarta.validation.Valid;

/**
 * HTTP adapter for Goods Receipt (penerimaan barang) use cases.
 */
@RestController
@RequestMapping("/api/goods-receipts")
public class GoodsReceiptController {

    private final GoodsReceiptUseCase goodsReceiptUseCase;
    private final CurrentUser currentUser;

    public GoodsReceiptController(GoodsReceiptUseCase goodsReceiptUseCase, CurrentUser currentUser) {
        this.goodsReceiptUseCase = goodsReceiptUseCase;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<GoodsReceiptResponse> create(@Valid @RequestBody GoodsReceiptRequest request) {
        GoodsReceiptResponse response = GoodsReceiptResponse.from(
                goodsReceiptUseCase.create(currentUser.get(), request.toCommand()));
        return ResponseEntity.created(URI.create("/api/goods-receipts/" + response.id())).body(response);
    }

    @GetMapping
    public PageResponse<GoodsReceiptResponse> list(
            @RequestParam(required = false) Long purchaseOrderId,
            @RequestParam(required = false) DocumentStatus status, Pageable pageable) {
        PageResult<GoodsReceipt> page = goodsReceiptUseCase.list(currentUser.get(), purchaseOrderId, status,
                Pagination.toQuery(pageable));
        return PageResponse.from(page.map(GoodsReceiptResponse::from));
    }

    @GetMapping("/{id}")
    public GoodsReceiptResponse get(@PathVariable Long id) {
        return GoodsReceiptResponse.from(goodsReceiptUseCase.get(currentUser.get(), id));
    }

    @PostMapping("/{id}/approve")
    public GoodsReceiptResponse approve(@PathVariable Long id) {
        return GoodsReceiptResponse.from(goodsReceiptUseCase.approve(currentUser.get(), id));
    }

    @PostMapping("/{id}/reject")
    public GoodsReceiptResponse reject(@PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        return GoodsReceiptResponse.from(goodsReceiptUseCase.reject(currentUser.get(), id, request.reason()));
    }
}
