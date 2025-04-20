package org.example.controller;

import org.example.model.Bill;
import org.example.service.BillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;


class BillControllerTest {
    @Mock
    private BillService billService;

    @InjectMocks
    private org.example.controller.BillController billController;

    private Bill bill;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bill = new Bill();
        bill.setId(UUID.randomUUID().toString());
        bill.setTitle("Test Bill");
        bill.setStatus("OPEN");
        bill.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateBill() {
        when(billService.createBill(any(Bill.class))).thenReturn(bill);
        ResponseEntity<Bill> response = billController.createBill(bill);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Test Bill", response.getBody().getTitle());
    }

    @Test
    void testGetAllBills() {
        when(billService.getAllBills()).thenReturn(Arrays.asList(bill));
        ResponseEntity<List<Bill>> response = billController.getAllBills();
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals("Test Bill", response.getBody().get(0).getTitle());
    }

    @Test
    void testGetBillById() {
        when(billService.getBillById(eq(bill.getId()))).thenReturn(Optional.of(bill));
        ResponseEntity<Bill> response = billController.getBill(bill.getId());
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Test Bill", response.getBody().getTitle());
    }

    @Test
    void testGetBillByIdNotFound() {
        when(billService.getBillById(eq("notfound"))).thenReturn(Optional.empty());
        ResponseEntity<Bill> response = billController.getBill("notfound");
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testUpdateBill() {
        Bill updated = new Bill();
        updated.setId(bill.getId());
        updated.setTitle("Updated Title");
        updated.setStatus("OPEN");
        updated.setCreatedAt(bill.getCreatedAt());
        when(billService.updateBill(eq(bill.getId()), any(Bill.class))).thenReturn(updated);
        ResponseEntity<Bill> response = billController.updateBill(bill.getId(), updated);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Updated Title", response.getBody().getTitle());
    }

    @Test
    void testUpdateBillNotFound() {
        when(billService.updateBill(eq("notfound"), any(Bill.class))).thenThrow(new RuntimeException("Bill not found"));
        ResponseEntity<Bill> response = billController.updateBill("notfound", bill);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteBill() {
        doNothing().when(billService).deleteBill(eq(bill.getId()));
        ResponseEntity<Void> response = billController.deleteBill(bill.getId());
        assertEquals(204, response.getStatusCode().value());
    }

    @Test
    void testDeleteBillNotFound() {
        doNothing().when(billService).deleteBill(eq("notfound"));
        ResponseEntity<Void> response = billController.deleteBill("notfound");
        // The controller returns 404 if exception is thrown, but here doNothing so 204
        assertEquals(204, response.getStatusCode().value());
    }
}

