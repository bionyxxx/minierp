package com.minierp.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.inventory.StockMovementRef;
import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.sales.Delivery;
import com.minierp.domain.model.sales.DeliveryLine;
import com.minierp.domain.model.sales.SalesOrder;
import com.minierp.domain.model.sales.SalesOrderLine;
import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.BusinessRuleException;
import com.minierp.domain.model.shared.DocumentNumberType;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.InvalidStateException;
import com.minierp.domain.model.shared.NotFoundException;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.model.shared.SortField;
import com.minierp.domain.model.warehouse.Warehouse;
import com.minierp.domain.port.in.DeliveryCommand;
import com.minierp.domain.port.in.DeliveryLineCommand;
import com.minierp.domain.port.in.DeliveryUseCase;
import com.minierp.domain.port.out.DeliveryRepositoryPort;
import com.minierp.domain.port.out.DocumentNumberPort;
import com.minierp.domain.port.out.ItemRepositoryPort;
import com.minierp.domain.port.out.SalesOrderRepositoryPort;
import com.minierp.domain.port.out.StockPort;
import com.minierp.domain.port.out.WarehouseRepositoryPort;
import com.minierp.domain.service.ApprovalPolicy;

/**
 * Delivery use cases: create (against an approved Sales Order), approve
 * (decreases stock), reject (no stock change), list and detail.
 */
@Service
public class DeliveryService implements DeliveryUseCase {

    private static final Set<String> SORT_PROPERTIES =
            Set.of("id", "deliveryNumber", "deliveryDate", "status", "createdAt");

    private static final SortField DEFAULT_SORT = SortField.desc("id");

    private final DeliveryRepositoryPort deliveryRepository;
    private final SalesOrderRepositoryPort salesOrderRepository;
    private final WarehouseRepositoryPort warehouseRepository;
    private final ItemRepositoryPort itemRepository;
    private final StockPort stockPort;
    private final DocumentNumberPort documentNumbers;
    private final ApprovalPolicy approvalPolicy;

    public DeliveryService(DeliveryRepositoryPort deliveryRepository,
            SalesOrderRepositoryPort salesOrderRepository, WarehouseRepositoryPort warehouseRepository,
            ItemRepositoryPort itemRepository, StockPort stockPort, DocumentNumberPort documentNumbers,
            ApprovalPolicy approvalPolicy) {
        this.deliveryRepository = deliveryRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.warehouseRepository = warehouseRepository;
        this.itemRepository = itemRepository;
        this.stockPort = stockPort;
        this.documentNumbers = documentNumbers;
        this.approvalPolicy = approvalPolicy;
    }

    @Transactional
    @Override
    public Delivery create(AuthenticatedUser actor, DeliveryCommand command) {
        if (command.lines().isEmpty()) {
            throw new BusinessRuleException("Delivery minimal memiliki 1 line");
        }
        SalesOrder so = salesOrderRepository.findById(command.salesOrderId())
                .orElseThrow(() -> new NotFoundException("Sales Order", command.salesOrderId()));
        if (so.getStatus() != DocumentStatus.APPROVED) {
            throw new InvalidStateException("Delivery hanya dapat dibuat untuk Sales Order yang sudah disetujui");
        }
        Warehouse warehouse = warehouseRepository.findById(command.warehouseId())
                .orElseThrow(() -> new NotFoundException("Gudang", command.warehouseId()));
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
        List<DeliveryLine> requestedLines = new ArrayList<>();
        for (DeliveryLineCommand line : command.lines()) {
            Item item = itemRepository.findById(line.itemId())
                    .orElseThrow(() -> new NotFoundException("Item", line.itemId()));
            DeliveryLine deliveryLine = DeliveryLine.create(item, line.qty());
            if (!orderedQty.containsKey(item.getId())) {
                throw new BusinessRuleException("Item %s tidak termasuk dalam Sales Order %s"
                        .formatted(item.getSku(), so.getSoNumber()));
            }
            itemById.put(item.getId(), item);
            requestedQty.merge(item.getId(), line.qty(), BigDecimal::add);
            requestedLines.add(deliveryLine);
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
            BigDecimal available = stockPort.availableQty(item, warehouse);
            if (available.compareTo(requested) < 0) {
                throw new BusinessRuleException("Stok tidak mencukupi: item %s di gudang %s tersedia %s, diminta %s"
                        .formatted(item.getSku(), warehouse.getCode(),
                                available.toPlainString(), requested.toPlainString()));
            }
        }

        Delivery delivery = Delivery.create(so, warehouse, command.deliveryDate(), command.notes(), actor.username());
        for (DeliveryLine line : requestedLines) {
            delivery.addLine(line);
        }

        // The number must be assigned before save: Hibernate snapshots entity state
        // at persist time, so post-save changes are not part of the initial INSERT.
        delivery.assignNumber(documentNumbers.nextNumber(DocumentNumberType.DELIVERY));
        return deliveryRepository.save(delivery);
    }

    @Transactional(readOnly = true)
    @Override
    public Delivery get(AuthenticatedUser actor, Long id) {
        return findOrThrow(id);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResult<Delivery> list(AuthenticatedUser actor, Long salesOrderId, DocumentStatus status,
            PageQuery query) {
        return deliveryRepository.findPage(salesOrderId, status,
                Pagination.normalize(query, SORT_PROPERTIES, DEFAULT_SORT));
    }

    @Transactional
    @Override
    public Delivery approve(AuthenticatedUser actor, Long id) {
        Delivery delivery = findOrThrow(id);
        // Basis persetujuan adalah nilai Sales Order, bukan nilai delivery
        approvalPolicy.checkCanApprove(actor, delivery.getSalesOrder().getTotalAmount(),
                "Delivery " + delivery.getDeliveryNumber());
        delivery.approve(actor.username(), Instant.now());
        deliveryRepository.save(delivery);
        for (DeliveryLine line : delivery.getLines()) {
            stockPort.decrease(line.getItem(), delivery.getWarehouse(), line.getQty(),
                    StockMovementRef.DELIVERY, delivery.getId(), delivery.getDeliveryNumber(),
                    "Pengiriman barang", actor.username());
        }
        return delivery;
    }

    @Transactional
    @Override
    public Delivery reject(AuthenticatedUser actor, Long id, String reason) {
        Delivery delivery = findOrThrow(id);
        // Basis persetujuan adalah nilai Sales Order, bukan nilai delivery
        approvalPolicy.checkCanApprove(actor, delivery.getSalesOrder().getTotalAmount(),
                "Delivery " + delivery.getDeliveryNumber());
        delivery.reject(reason);
        deliveryRepository.save(delivery);
        return delivery;
    }

    private Delivery findOrThrow(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Delivery", id));
    }
}
