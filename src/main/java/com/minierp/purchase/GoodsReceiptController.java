package com.minierp.purchase;

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

import com.minierp.common.AuthenticatedUser;
import com.minierp.common.CurrentUser;
import com.minierp.common.DocumentStatus;
import com.minierp.common.PageResponse;
import com.minierp.purchase.dto.GoodsReceiptRequest;
import com.minierp.purchase.dto.GoodsReceiptResponse;
import com.minierp.purchase.dto.RejectRequest;

import jakarta.validation.Valid;

/**
 * HTTP adapter for Goods Receipt (penerimaan barang) use cases.
 */
@RestController
@RequestMapping("/api/goods-receipts")
public class GoodsReceiptController {

    private final GoodsReceiptService goodsReceiptService;
    private final CurrentUser currentUser;

    public GoodsReceiptController(GoodsReceiptService goodsReceiptService, CurrentUser currentUser) {
        this.goodsReceiptService = goodsReceiptService;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<GoodsReceiptResponse> create(@Valid @RequestBody GoodsReceiptRequest request) {
        AuthenticatedUser actor = currentUser.get();
        GoodsReceiptResponse response = goodsReceiptService.create(actor, request);
        return ResponseEntity.created(URI.create("/api/goods-receipts/" + response.id())).body(response);
    }

    @GetMapping
    public PageResponse<GoodsReceiptResponse> list(
            @RequestParam(required = false) Long purchaseOrderId,
            @RequestParam(required = false) DocumentStatus status, Pageable pageable) {
        return goodsReceiptService.list(currentUser.get(), purchaseOrderId, status, pageable);
    }

    @GetMapping("/{id}")
    public GoodsReceiptResponse get(@PathVariable Long id) {
        return goodsReceiptService.get(currentUser.get(), id);
    }

    @PostMapping("/{id}/approve")
    public GoodsReceiptResponse approve(@PathVariable Long id) {
        return goodsReceiptService.approve(currentUser.get(), id);
    }

    @PostMapping("/{id}/reject")
    public GoodsReceiptResponse reject(@PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        return goodsReceiptService.reject(currentUser.get(), id, request);
    }
}
