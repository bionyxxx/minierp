package com.minierp.sales;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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
import com.minierp.customer.Customer;
import com.minierp.customer.CustomerRepository;
import com.minierp.item.Item;
import com.minierp.item.ItemRepository;
import com.minierp.sales.dto.RejectRequest;
import com.minierp.sales.dto.SalesOrderLineRequest;
import com.minierp.sales.dto.SalesOrderRequest;
import com.minierp.sales.dto.SalesOrderResponse;

/**
 * Sales Order use cases: create, approve, reject, list and detail.
 */
@Service
public class SalesOrderService {

    private static final Set<String> SORT_PROPERTIES =
            Set.of("id", "soNumber", "orderDate", "status", "totalAmount", "createdAt");

    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;
    private final ItemRepository itemRepository;
    private final ApprovalPolicy approvalPolicy;

    public SalesOrderService(SalesOrderRepository salesOrderRepository, CustomerRepository customerRepository,
            ItemRepository itemRepository, ApprovalPolicy approvalPolicy) {
        this.salesOrderRepository = salesOrderRepository;
        this.customerRepository = customerRepository;
        this.itemRepository = itemRepository;
        this.approvalPolicy = approvalPolicy;
    }

    @Transactional
    public SalesOrderResponse create(AuthenticatedUser actor, SalesOrderRequest request) {
        if (request.lines() == null || request.lines().isEmpty()) {
            throw new BusinessRuleException("Sales Order minimal memiliki 1 line");
        }
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new NotFoundException("Customer", request.customerId()));
        if (!customer.isActive()) {
            throw new BusinessRuleException("Customer %s tidak aktif".formatted(customer.getName()));
        }

        SalesOrder so = new SalesOrder(customer, request.orderDate());
        so.setNotes(request.notes());
        so.setCreatedBy(actor.username());
        for (SalesOrderLineRequest line : request.lines()) {
            if (line.qty() == null || line.qty().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessRuleException("Qty line Sales Order harus lebih besar dari 0");
            }
            if (line.unitPrice() == null || line.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessRuleException("Harga jual line Sales Order tidak boleh negatif");
            }
            so.addLine(new SalesOrderLine(resolveItem(line.itemId()), line.qty(), line.unitPrice()));
        }

        // The number must be set before save: Hibernate snapshots entity state at
        // persist time, so post-save changes are not part of the initial INSERT.
        so.setSoNumber(DocumentNumbers.format("SO", salesOrderRepository.nextNumberValue()));
        salesOrderRepository.save(so);
        return SalesOrderMapper.toResponse(so);
    }

    @Transactional
    public SalesOrderResponse approve(AuthenticatedUser actor, Long id) {
        SalesOrder so = getSalesOrder(id);
        approvalPolicy.checkCanApprove(actor, so.getTotalAmount(), "Sales Order " + so.getSoNumber());
        if (so.getStatus() != DocumentStatus.PENDING) {
            throw new InvalidStateException(
                    "Sales Order %s hanya dapat disetujui/ditolak saat berstatus PENDING, status saat ini %s"
                            .formatted(so.getSoNumber(), so.getStatus()));
        }
        so.setStatus(DocumentStatus.APPROVED);
        so.setApprovedBy(actor.username());
        so.setApprovedAt(Instant.now());
        return SalesOrderMapper.toResponse(so);
    }

    @Transactional
    public SalesOrderResponse reject(AuthenticatedUser actor, Long id, RejectRequest request) {
        SalesOrder so = getSalesOrder(id);
        approvalPolicy.checkCanApprove(actor, so.getTotalAmount(), "Sales Order " + so.getSoNumber());
        if (so.getStatus() != DocumentStatus.PENDING) {
            throw new InvalidStateException(
                    "Sales Order %s hanya dapat disetujui/ditolak saat berstatus PENDING, status saat ini %s"
                            .formatted(so.getSoNumber(), so.getStatus()));
        }
        if (request == null || request.reason() == null || request.reason().isBlank()) {
            throw new BusinessRuleException("reason wajib diisi");
        }
        so.setStatus(DocumentStatus.REJECTED);
        so.setRejectReason(request.reason());
        return SalesOrderMapper.toResponse(so);
    }

    @Transactional(readOnly = true)
    public PageResponse<SalesOrderResponse> list(DocumentStatus status, Long customerId, Pageable pageable) {
        Pageable effective = Pagination.normalize(pageable, SORT_PROPERTIES, Sort.by(Sort.Order.desc("id")));
        return PageResponse.from(
                salesOrderRepository.findPage(status, customerId, effective).map(SalesOrderMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public SalesOrderResponse getById(Long id) {
        return SalesOrderMapper.toResponse(getSalesOrder(id));
    }

    private SalesOrder getSalesOrder(Long id) {
        return salesOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sales Order", id));
    }

    private Item resolveItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item", itemId));
        if (!item.isActive()) {
            throw new BusinessRuleException("Item %s tidak aktif".formatted(item.getSku()));
        }
        return item;
    }
}
