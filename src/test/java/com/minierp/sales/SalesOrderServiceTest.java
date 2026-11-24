package com.minierp.sales;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.minierp.approval.ApprovalPolicy;
import com.minierp.common.AuthenticatedUser;
import com.minierp.common.BusinessRuleException;
import com.minierp.common.DocumentStatus;
import com.minierp.common.InvalidStateException;
import com.minierp.common.NotFoundException;
import com.minierp.config.MinierpProperties;
import com.minierp.customer.Customer;
import com.minierp.customer.CustomerRepository;
import com.minierp.item.Item;
import com.minierp.item.ItemRepository;
import com.minierp.sales.dto.RejectRequest;
import com.minierp.sales.dto.SalesOrderLineRequest;
import com.minierp.sales.dto.SalesOrderRequest;
import com.minierp.sales.dto.SalesOrderResponse;
import com.minierp.user.Role;

@ExtendWith(MockitoExtension.class)
class SalesOrderServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ItemRepository itemRepository;

    private SalesOrderService service;

    private final AuthenticatedUser admin = new AuthenticatedUser("admin1", Role.ADMIN);
    private final AuthenticatedUser staff = new AuthenticatedUser("staff1", Role.STAFF);

    @BeforeEach
    void setUp() {
        MinierpProperties properties = new MinierpProperties(
                new MinierpProperties.Security(new MinierpProperties.Jwt("secret", Duration.ofHours(8))),
                new MinierpProperties.Approval(10000000),
                new MinierpProperties.Seed(false),
                new MinierpProperties.Api("https://minierp.local/problems/"));
        service = new SalesOrderService(salesOrderRepository, customerRepository, itemRepository,
                new ApprovalPolicy(properties));
    }

    @Test
    void createComputesTotalAmountAndDocumentNumber() {
        Customer customer = newCustomer(10L, "C-001", "PT Maju");
        Item item = newItem(5L, "SKU-1", "Widget");
        when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(salesOrderRepository.nextNumberValue()).thenReturn(1L);
        when(salesOrderRepository.save(any(SalesOrder.class))).thenAnswer(invocation -> {
            SalesOrder saved = invocation.getArgument(0);
            setId(saved, 1L);
            return saved;
        });

        SalesOrderResponse response = service.create(staff, new SalesOrderRequest(
                10L, LocalDate.of(2026, 1, 15), "catatan",
                List.of(new SalesOrderLineRequest(5L, new BigDecimal("2"), new BigDecimal("1500.50")),
                        new SalesOrderLineRequest(5L, new BigDecimal("1"), new BigDecimal("500")))));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.soNumber()).isEqualTo("SO-000001");
        assertThat(response.status()).isEqualTo(DocumentStatus.PENDING);
        assertThat(response.createdBy()).isEqualTo("staff1");
        assertThat(response.customerName()).isEqualTo("PT Maju");
        // 2 * 1500.50 + 1 * 500 = 3501.00
        assertThat(response.totalAmount()).isEqualByComparingTo("3501.00");
        assertThat(response.lines()).hasSize(2);
        assertThat(response.lines().get(0).lineTotal()).isEqualByComparingTo("3001.00");
        assertThat(response.lines().get(1).lineTotal()).isEqualByComparingTo("500");
    }

    @Test
    void createRejectsEmptyLines() {
        SalesOrderRequest request = new SalesOrderRequest(10L, LocalDate.of(2026, 1, 15), null, List.of());

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.create(staff, request));

        assertThat(ex.getMessage()).isEqualTo("Sales Order minimal memiliki 1 line");
        verifyNoInteractions(salesOrderRepository, customerRepository, itemRepository);
    }

    @Test
    void createRejectsInactiveCustomer() {
        Customer customer = newCustomer(10L, "C-001", "PT Maju");
        customer.setActive(false);
        when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));

        SalesOrderRequest request = new SalesOrderRequest(10L, LocalDate.of(2026, 1, 15), null,
                List.of(new SalesOrderLineRequest(5L, BigDecimal.ONE, BigDecimal.TEN)));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.create(staff, request));

        assertThat(ex.getMessage()).contains("tidak aktif");
    }

    @Test
    void createRejectsUnknownItem() {
        Customer customer = newCustomer(10L, "C-001", "PT Maju");
        when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(itemRepository.findById(5L)).thenReturn(Optional.empty());

        SalesOrderRequest request = new SalesOrderRequest(10L, LocalDate.of(2026, 1, 15), null,
                List.of(new SalesOrderLineRequest(5L, BigDecimal.ONE, BigDecimal.TEN)));

        assertThrows(NotFoundException.class, () -> service.create(staff, request));
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

        SalesOrderResponse response = service.approve(admin, 42L);

        assertThat(response.status()).isEqualTo(DocumentStatus.APPROVED);
        assertThat(response.approvedBy()).isEqualTo("admin1");
        assertThat(response.approvedAt()).isNotNull();
    }

    @Test
    void approveRejectsDocumentThatIsNotPending() {
        SalesOrder so = newSalesOrderWithTotal(42L, "SO-000042", BigDecimal.ONE);
        so.setStatus(DocumentStatus.APPROVED);
        when(salesOrderRepository.findById(42L)).thenReturn(Optional.of(so));

        assertThrows(InvalidStateException.class, () -> service.approve(admin, 42L));
    }

    @Test
    void rejectSetsRejectedStatusAndReason() {
        SalesOrder so = newSalesOrderWithTotal(42L, "SO-000042", BigDecimal.ONE);
        when(salesOrderRepository.findById(42L)).thenReturn(Optional.of(so));

        SalesOrderResponse response = service.reject(admin, 42L, new RejectRequest("data salah"));

        assertThat(response.status()).isEqualTo(DocumentStatus.REJECTED);
        assertThat(response.rejectReason()).isEqualTo("data salah");
    }

    @Test
    void rejectRequiresReason() {
        SalesOrder so = newSalesOrderWithTotal(42L, "SO-000042", BigDecimal.ONE);
        when(salesOrderRepository.findById(42L)).thenReturn(Optional.of(so));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.reject(admin, 42L, new RejectRequest("  ")));

        assertThat(ex.getMessage()).isEqualTo("reason wajib diisi");
        assertThat(so.getStatus()).isEqualTo(DocumentStatus.PENDING);
    }

    @Test
    void getByIdReturnsLines() {
        Customer customer = newCustomer(10L, "C-001", "PT Maju");
        Item item = newItem(5L, "SKU-1", "Widget");
        SalesOrder so = new SalesOrder(customer, LocalDate.of(2026, 1, 15));
        setId(so, 1L);
        so.setSoNumber("SO-000001");
        so.addLine(new SalesOrderLine(item, new BigDecimal("2"), new BigDecimal("1500.50")));
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(so));

        SalesOrderResponse response = service.getById(1L);

        assertThat(response.soNumber()).isEqualTo("SO-000001");
        assertThat(response.lines()).hasSize(1);
        assertThat(response.lines().get(0).sku()).isEqualTo("SKU-1");
        assertThat(response.lines().get(0).itemName()).isEqualTo("Widget");
    }

    private static SalesOrder newSalesOrderWithTotal(long id, String soNumber, BigDecimal lineTotal) {
        Customer customer = newCustomer(10L, "C-001", "PT Maju");
        Item item = newItem(5L, "SKU-1", "Widget");
        SalesOrder so = new SalesOrder(customer, LocalDate.of(2026, 1, 15));
        setId(so, id);
        so.setSoNumber(soNumber);
        so.addLine(new SalesOrderLine(item, BigDecimal.ONE, lineTotal));
        return so;
    }

    private static Customer newCustomer(long id, String code, String name) {
        Customer customer = new Customer();
        customer.setCode(code);
        customer.setName(name);
        setId(customer, id);
        return customer;
    }

    private static Item newItem(long id, String sku, String name) {
        Item item = new Item();
        item.setSku(sku);
        item.setName(name);
        setId(item, id);
        return item;
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
