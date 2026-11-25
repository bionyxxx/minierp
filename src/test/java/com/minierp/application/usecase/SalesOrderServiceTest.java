package com.minierp.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.minierp.domain.model.customer.Customer;
import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.sales.SalesOrder;
import com.minierp.domain.model.sales.SalesOrderLine;
import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.BusinessRuleException;
import com.minierp.domain.model.shared.DocumentNumberType;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.InvalidStateException;
import com.minierp.domain.model.shared.NotFoundException;
import com.minierp.domain.model.user.Role;
import com.minierp.domain.port.in.SalesOrderCommand;
import com.minierp.domain.port.in.SalesOrderLineCommand;
import com.minierp.domain.port.out.CustomerRepositoryPort;
import com.minierp.domain.port.out.DocumentNumberPort;
import com.minierp.domain.port.out.ItemRepositoryPort;
import com.minierp.domain.port.out.SalesOrderRepositoryPort;
import com.minierp.domain.service.ApprovalPolicy;

@ExtendWith(MockitoExtension.class)
class SalesOrderServiceTest {

    @Mock
    private SalesOrderRepositoryPort salesOrderRepository;

    @Mock
    private CustomerRepositoryPort customerRepository;

    @Mock
    private ItemRepositoryPort itemRepository;

    @Mock
    private DocumentNumberPort documentNumbers;

    private SalesOrderService service;

    private final AuthenticatedUser admin = new AuthenticatedUser("admin1", Role.ADMIN);
    private final AuthenticatedUser staff = new AuthenticatedUser("staff1", Role.STAFF);

    @BeforeEach
    void setUp() {
        service = new SalesOrderService(salesOrderRepository, customerRepository, itemRepository,
                documentNumbers, new ApprovalPolicy(new BigDecimal("10000000")));
    }

    @Test
    void createComputesTotalAmountAndDocumentNumber() {
        Customer customer = newCustomer(10L, "C-001", "PT Maju");
        Item item = newItem(5L, "SKU-1", "Widget");
        when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(documentNumbers.nextNumber(DocumentNumberType.SALES_ORDER)).thenReturn("SO-000001");
        when(salesOrderRepository.save(any(SalesOrder.class))).thenAnswer(invocation -> {
            SalesOrder saved = invocation.getArgument(0);
            setId(saved, 1L);
            return saved;
        });

        SalesOrder so = service.create(staff, new SalesOrderCommand(
                10L, LocalDate.of(2026, 1, 15), "catatan",
                List.of(new SalesOrderLineCommand(5L, new BigDecimal("2"), new BigDecimal("1500.50")),
                        new SalesOrderLineCommand(5L, new BigDecimal("1"), new BigDecimal("500")))));

        assertThat(so.getId()).isEqualTo(1L);
        assertThat(so.getSoNumber()).isEqualTo("SO-000001");
        assertThat(so.getStatus()).isEqualTo(DocumentStatus.PENDING);
        assertThat(so.getCreatedBy()).isEqualTo("staff1");
        assertThat(so.getCustomer().getName()).isEqualTo("PT Maju");
        // 2 * 1500.50 + 1 * 500 = 3501.00
        assertThat(so.getTotalAmount()).isEqualByComparingTo("3501.00");
        assertThat(so.getLines()).hasSize(2);
        assertThat(so.getLines().get(0).getLineTotal()).isEqualByComparingTo("3001.00");
        assertThat(so.getLines().get(1).getLineTotal()).isEqualByComparingTo("500");
    }

    @Test
    void createRejectsEmptyLines() {
        SalesOrderCommand command = new SalesOrderCommand(10L, LocalDate.of(2026, 1, 15), null, List.of());

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.create(staff, command));

        assertThat(ex.getMessage()).isEqualTo("Sales Order minimal memiliki 1 line");
        verifyNoInteractions(salesOrderRepository, customerRepository, itemRepository);
    }

    @Test
    void createRejectsInactiveCustomer() {
        Customer customer = newCustomer(10L, "C-001", "PT Maju");
        customer.deactivate();
        when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));

        SalesOrderCommand command = new SalesOrderCommand(10L, LocalDate.of(2026, 1, 15), null,
                List.of(new SalesOrderLineCommand(5L, BigDecimal.ONE, BigDecimal.TEN)));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.create(staff, command));

        assertThat(ex.getMessage()).contains("tidak aktif");
    }

    @Test
    void createRejectsUnknownItem() {
        Customer customer = newCustomer(10L, "C-001", "PT Maju");
        when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(itemRepository.findById(5L)).thenReturn(Optional.empty());

        SalesOrderCommand command = new SalesOrderCommand(10L, LocalDate.of(2026, 1, 15), null,
                List.of(new SalesOrderLineCommand(5L, BigDecimal.ONE, BigDecimal.TEN)));

        assertThrows(NotFoundException.class, () -> service.create(staff, command));
    }

    @Test
    void staffCannotApproveSalesOrderAtOrAboveThreshold() {
        SalesOrder so = newSalesOrderWithTotal(42L, "SO-000042", new BigDecimal("10000000"));
        when(salesOrderRepository.findById(42L)).thenReturn(Optional.of(so));

        assertThrows(BusinessRuleException.class, () -> service.approve(staff, 42L));

        assertThat(so.getStatus()).isEqualTo(DocumentStatus.PENDING);
        assertThat(so.getApprovedBy()).isNull();
    }

    @Test
    void adminCanApproveSalesOrderAboveThreshold() {
        SalesOrder so = newSalesOrderWithTotal(42L, "SO-000042", new BigDecimal("15000000"));
        when(salesOrderRepository.findById(42L)).thenReturn(Optional.of(so));

        SalesOrder approved = service.approve(admin, 42L);

        assertThat(approved.getStatus()).isEqualTo(DocumentStatus.APPROVED);
        assertThat(approved.getApprovedBy()).isEqualTo("admin1");
        assertThat(approved.getApprovedAt()).isNotNull();
    }

    @Test
    void approveRejectsDocumentThatIsNotPending() {
        SalesOrder so = newSalesOrderWithTotal(42L, "SO-000042", BigDecimal.ONE);
        so.approve("admin1", Instant.now());
        when(salesOrderRepository.findById(42L)).thenReturn(Optional.of(so));

        assertThrows(InvalidStateException.class, () -> service.approve(admin, 42L));
    }

    @Test
    void rejectSetsRejectedStatusAndReason() {
        SalesOrder so = newSalesOrderWithTotal(42L, "SO-000042", BigDecimal.ONE);
        when(salesOrderRepository.findById(42L)).thenReturn(Optional.of(so));

        SalesOrder rejected = service.reject(admin, 42L, "data salah");

        assertThat(rejected.getStatus()).isEqualTo(DocumentStatus.REJECTED);
        assertThat(rejected.getRejectReason()).isEqualTo("data salah");
    }

    @Test
    void rejectRequiresReason() {
        SalesOrder so = newSalesOrderWithTotal(42L, "SO-000042", BigDecimal.ONE);
        when(salesOrderRepository.findById(42L)).thenReturn(Optional.of(so));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.reject(admin, 42L, "  "));

        assertThat(ex.getMessage()).isEqualTo("reason wajib diisi");
        assertThat(so.getStatus()).isEqualTo(DocumentStatus.PENDING);
    }

    @Test
    void getByIdReturnsLines() {
        Customer customer = newCustomer(10L, "C-001", "PT Maju");
        Item item = newItem(5L, "SKU-1", "Widget");
        SalesOrder so = SalesOrder.create(customer, LocalDate.of(2026, 1, 15), null, "staff1");
        setId(so, 1L);
        so.assignNumber("SO-000001");
        so.addLine(SalesOrderLine.create(item, new BigDecimal("2"), new BigDecimal("1500.50")));
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(so));

        SalesOrder found = service.get(admin, 1L);

        assertThat(found.getSoNumber()).isEqualTo("SO-000001");
        assertThat(found.getLines()).hasSize(1);
        assertThat(found.getLines().get(0).getItem().getSku()).isEqualTo("SKU-1");
        assertThat(found.getLines().get(0).getItem().getName()).isEqualTo("Widget");
    }

    private static SalesOrder newSalesOrderWithTotal(long id, String soNumber, BigDecimal lineTotal) {
        Customer customer = newCustomer(10L, "C-001", "PT Maju");
        Item item = newItem(5L, "SKU-1", "Widget");
        SalesOrder so = SalesOrder.create(customer, LocalDate.of(2026, 1, 15), null, "staff1");
        setId(so, id);
        so.assignNumber(soNumber);
        so.addLine(SalesOrderLine.create(item, BigDecimal.ONE, lineTotal));
        return so;
    }

    private static Customer newCustomer(long id, String code, String name) {
        return Customer.reconstitute(id, code, name, null, null, null, true, null, null);
    }

    private static Item newItem(long id, String sku, String name) {
        return Item.reconstitute(id, sku, name, "PCS", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                true, null, null);
    }

    private static void setId(Object target, long id) {
        setField(target, "id", id);
    }

    private static void setField(Object target, String fieldName, Object value) {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException ex) {
                type = type.getSuperclass();
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
        }
        throw new IllegalStateException("Field %s tidak ditemukan pada %s".formatted(fieldName, target.getClass()));
    }
}
