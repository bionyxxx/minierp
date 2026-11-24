package com.minierp.sales;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minierp.approval.ApprovalPolicy;
import com.minierp.common.AuthenticatedUser;
import com.minierp.common.BusinessRuleException;
import com.minierp.common.DocumentNumbers;
import com.minierp.common.DocumentStatus;
import com.minierp.common.InvalidStateException;
import com.minierp.common.NotFoundException;
import com.minierp.common.PageResponse;
import com.minierp.common.Pagination;
import com.minierp.inventory.StockMovementRef;
import com.minierp.inventory.StockService;
import com.minierp.item.Item;
import com.minierp.item.ItemRepository;
import com.minierp.sales.dto.DeliveryLineRequest;
import com.minierp.sales.dto.DeliveryRequest;
import com.minierp.sales.dto.DeliveryResponse;
import com.minierp.sales.dto.RejectRequest;
import com.minierp.warehouse.Warehouse;
import com.minierp.warehouse.WarehouseRepository;

/**
 * Delivery use cases: create (against an approved Sales Order), approve
 * (decreases stock), reject (no stock change), list and detail.
 */
@Service
public class DeliveryService {

    private static final Set<String> SORT_PROPERTIES =
            Set.of("id", "deliveryNumber", "deliveryDate", "status", "createdAt");

    private final DeliveryRepository deliveryRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final WarehouseRepository warehouseRepository;
    private final ItemRepository itemRepository;
    private final StockService stockService;
    private final ApprovalPolicy approvalPolicy;

    public DeliveryService(DeliveryRepository deliveryRepository, SalesOrderRepository salesOrderRepository,
            WarehouseRepository warehouseRepository, ItemRepository itemRepository, StockService stockService,
            ApprovalPolicy approvalPolicy) {
        this.deliveryRepository = deliveryRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.warehouseRepository = warehouseRepository;
        this.itemRepository = itemRepository;
        this.stockService = stockService;
        this.approvalPolicy = approvalPolicy;
    }

    @Transactional
    public DeliveryResponse create(AuthenticatedUser actor, DeliveryRequest request) {
        if (request.lines() == null || request.lines().isEmpty()) {
            throw new BusinessRuleException("Delivery minimal memiliki 1 line");
        }
        SalesOrder so = salesOrderRepository.findById(request.salesOrderId())
                .orElseThrow(() -> new NotFoundException("Sales Order", request.salesOrderId()));
        if (so.getStatus() != DocumentStatus.APPROVED) {
            throw new InvalidStateException("Delivery hanya dapat dibuat untuk Sales Order yang sudah disetujui");
        }
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new NotFoundException("Gudang", request.warehouseId()));
        if (!warehouse.isActive()) {
            throw new BusinessRuleException("Gudang %s tidak aktif".formatted(warehouse.getName()));
        }

        // Qty pesanan per item (line dengan item yang sama dijumlahkan)
        Map<Long, BigDecimal> orderedQty = new LinkedHashMap<>();
        for (SalesOrderLine soLine : so.getLines()) {
            orderedQty.merge(soLine.getItem().getId(), soLine.getQty(), BigDecimal::add);
        }

        // Qty yang diminta pada pengiriman ini, per item
        Map<Long, BigDecimal> requestedQty = new LinkedHashMap<>();
        Map<Long, Item> itemById = new LinkedHashMap<>();
        List<RequestedLine> requestedLines = new ArrayList<>();
        for (DeliveryLineRequest line : request.lines()) {
            if (line.qty() == null || line.qty().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessRuleException("Qty line Delivery harus lebih besar dari 0");
            }
            Item item = itemRepository.findById(line.itemId())
                    .orElseThrow(() -> new NotFoundException("Item", line.itemId()));
            if (!orderedQty.containsKey(item.getId())) {
                throw new BusinessRuleException("Item %s tidak termasuk dalam Sales Order %s"
                        .formatted(item.getSku(), so.getSoNumber()));
            }
            itemById.put(item.getId(), item);
            requestedQty.merge(item.getId(), line.qty(), BigDecimal::add);
            requestedLines.add(new RequestedLine(item, line.qty()));
        }

        // Qty yang sudah dikirim pada pengiriman PENDING/APPROVED sebelumnya
        Map<Long, BigDecimal> deliveredQty = new HashMap<>();
        for (Delivery previous : deliveryRepository.findBySalesOrderId(so.getId())) {
            if (previous.getStatus() == DocumentStatus.PENDING || previous.getStatus() == DocumentStatus.APPROVED) {
                for (DeliveryLine previousLine : previous.getLines()) {
                    deliveredQty.merge(previousLine.getItem().getId(), previousLine.getQty(), BigDecimal::add);
                }
            }
        }

        for (Map.Entry<Long, BigDecimal> entry : requestedQty.entrySet()) {
            BigDecimal alreadyDelivered = deliveredQty.getOrDefault(entry.getKey(), BigDecimal.ZERO);
            BigDecimal ordered = orderedQty.get(entry.getKey());
            if (alreadyDelivered.add(entry.getValue()).compareTo(ordered) > 0) {
                throw new BusinessRuleException("Qty pengiriman untuk item %s melebihi qty pesanan"
                        .formatted(itemById.get(entry.getKey()).getSku()));
            }
        }

        for (Map.Entry<Long, BigDecimal> entry : requestedQty.entrySet()) {
            Item item = itemById.get(entry.getKey());
            BigDecimal requested = entry.getValue();
            BigDecimal available = stockService.availableQty(item, warehouse);
            if (available.compareTo(requested) < 0) {
                throw new BusinessRuleException("Stok tidak mencukupi: item %s di gudang %s tersedia %s, diminta %s"
                        .formatted(item.getSku(), warehouse.getCode(),
                                available.toPlainString(), requested.toPlainString()));
            }
        }

        Delivery delivery = new Delivery(so, warehouse, request.deliveryDate());
        delivery.setNotes(request.notes());
        delivery.setCreatedBy(actor.username());
        for (RequestedLine line : requestedLines) {
            delivery.addLine(new DeliveryLine(line.item(), line.qty()));
        }

        // The number must be set before save: Hibernate snapshots entity state at
        // persist time, so post-save changes are not part of the initial INSERT.
        delivery.setDeliveryNumber(DocumentNumbers.format("DEL", deliveryRepository.nextNumberValue()));
        deliveryRepository.save(delivery);
        return DeliveryMapper.toResponse(delivery);
    }

    @Transactional
    public DeliveryResponse approve(AuthenticatedUser actor, Long id) {
        Delivery delivery = getDelivery(id);
        // Basis persetujuan adalah nilai Sales Order, bukan nilai delivery
        approvalPolicy.checkCanApprove(actor, delivery.getSalesOrder().getTotalAmount(),
                "Delivery " + delivery.getDeliveryNumber());
        if (delivery.getStatus() != DocumentStatus.PENDING) {
            throw new InvalidStateException(
                    "Delivery %s hanya dapat disetujui/ditolak saat berstatus PENDING, status saat ini %s"
                            .formatted(delivery.getDeliveryNumber(), delivery.getStatus()));
        }
        delivery.setStatus(DocumentStatus.APPROVED);
        delivery.setApprovedBy(actor.username());
        delivery.setApprovedAt(Instant.now());
        for (DeliveryLine line : delivery.getLines()) {
            stockService.decrease(line.getItem(), delivery.getWarehouse(), line.getQty(),
                    StockMovementRef.DELIVERY, delivery.getId(), delivery.getDeliveryNumber(),
                    "Pengiriman barang", actor.username());
        }
        return DeliveryMapper.toResponse(delivery);
    }

    @Transactional
    public DeliveryResponse reject(AuthenticatedUser actor, Long id, RejectRequest request) {
        Delivery delivery = getDelivery(id);
        approvalPolicy.checkCanApprove(actor, delivery.getSalesOrder().getTotalAmount(),
                "Delivery " + delivery.getDeliveryNumber());
        if (delivery.getStatus() != DocumentStatus.PENDING) {
            throw new InvalidStateException(
                    "Delivery %s hanya dapat disetujui/ditolak saat berstatus PENDING, status saat ini %s"
                            .formatted(delivery.getDeliveryNumber(), delivery.getStatus()));
        }
        if (request == null || request.reason() == null || request.reason().isBlank()) {
            throw new BusinessRuleException("reason wajib diisi");
        }
        delivery.setStatus(DocumentStatus.REJECTED);
        delivery.setRejectReason(request.reason());
        return DeliveryMapper.toResponse(delivery);
    }

    @Transactional(readOnly = true)
    public PageResponse<DeliveryResponse> list(Long salesOrderId, DocumentStatus status, Pageable pageable) {
        Pageable effective = Pagination.normalize(pageable, SORT_PROPERTIES, Sort.by(Sort.Order.desc("id")));
        return PageResponse.from(
                deliveryRepository.findPage(salesOrderId, status, effective).map(DeliveryMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getById(Long id) {
        return DeliveryMapper.toResponse(getDelivery(id));
    }

    private Delivery getDelivery(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Delivery", id));
    }

    private record RequestedLine(Item item, BigDecimal qty) {
    }
}
