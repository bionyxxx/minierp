package com.minierp.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.DuplicateResourceException;
import com.minierp.domain.model.user.Role;
import com.minierp.domain.port.in.ItemCommand;
import com.minierp.domain.port.out.ItemRepositoryPort;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepositoryPort itemRepository;

    @InjectMocks
    private ItemService itemService;

    private final AuthenticatedUser actor = new AuthenticatedUser("admin", Role.ADMIN);

    @Test
    void createWithDuplicateSkuThrowsDuplicateResourceException() {
        ItemCommand command = new ItemCommand("SKU-001", "Widget", "PCS",
                BigDecimal.ONE, BigDecimal.TEN, new BigDecimal("15.50"));
        when(itemRepository.existsBySku("SKU-001")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> itemService.create(actor, command));

        assertEquals("SKU SKU-001 sudah digunakan", exception.getMessage());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void createNormalizesNullNumericFieldsToZero() {
        ItemCommand command = new ItemCommand("SKU-001", "Widget", "PCS", null, null, null);
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Item item = itemService.create(actor, command);

        assertEquals("SKU-001", item.getSku());
        assertEquals("Widget", item.getName());
        assertEquals("PCS", item.getUom());
        assertEquals(0, BigDecimal.ZERO.compareTo(item.getMinStock()));
        assertEquals(0, BigDecimal.ZERO.compareTo(item.getPurchasePrice()));
        assertEquals(0, BigDecimal.ZERO.compareTo(item.getSellingPrice()));
    }

    @Test
    void deleteSoftDeletesItem() {
        Item item = Item.create("SKU-001", "Widget", "PCS", null, null, null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        itemService.delete(actor, 1L);

        assertFalse(item.isActive());
        verify(itemRepository).save(item);
    }

    @Test
    void updateWithUnchangedSkuSkipsUniquenessCheck() {
        Item item = Item.create("SKU-001", "Widget", "PCS", null, null, null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Item updated = itemService.update(actor, 1L,
                new ItemCommand("SKU-001", "Nama baru", "PCS", null, null, null));

        assertEquals("Nama baru", updated.getName());
        verify(itemRepository, never()).existsBySku(anyString());
    }

    @Test
    void updateWithChangedSkuChecksUniqueness() {
        Item item = Item.create("SKU-001", "Widget", "PCS", null, null, null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.existsBySku("SKU-002")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> itemService.update(actor, 1L,
                        new ItemCommand("SKU-002", "Widget", "PCS", null, null, null)));

        assertEquals("SKU SKU-002 sudah digunakan", exception.getMessage());
        verify(itemRepository, never()).save(any(Item.class));
    }
}
