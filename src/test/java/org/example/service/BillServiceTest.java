package org.example.service;

import org.example.model.Bill;
import org.example.repository.BillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillServiceTest {
    @Mock
    private BillRepository billRepository;

    @InjectMocks
    private BillService billService;

    private Bill bill;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bill = new Bill();
        bill.setId("1");
        bill.setTitle("Test Bill");
    }

    @Test
    void testCreateBill() {
        when(billRepository.save(bill)).thenReturn(bill);
        Bill created = billService.createBill(bill);
        assertEquals(bill, created);
    }

    @Test
    void testGetAllBills() {
        when(billRepository.findAll()).thenReturn(Collections.singletonList(bill));
        List<Bill> bills = billService.getAllBills();
        assertEquals(1, bills.size());
        assertEquals(bill, bills.getFirst());
    }

    @Test
    void testGetBillById() {
        when(billRepository.findById("1")).thenReturn(Optional.of(bill));
        Optional<Bill> found = billService.getBillById("1");
        assertTrue(found.isPresent());
        assertEquals(bill, found.get());
    }

    @Test
    void testUpdateBill() {
        Bill updated = new Bill();
        updated.setTitle("Updated Title");
        when(billRepository.findById("1")).thenReturn(Optional.of(bill));
        when(billRepository.save(any(Bill.class))).thenReturn(updated);
        Bill result = billService.updateBill("1", updated);
        assertEquals("Updated Title", result.getTitle());
    }

    @Test
    void testDeleteBill() {
        doNothing().when(billRepository).deleteById("1");
        billService.deleteBill("1");
        verify(billRepository, times(1)).deleteById("1");
    }
}
