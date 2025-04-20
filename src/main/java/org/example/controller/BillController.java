package org.example.controller;

import org.example.model.Bill;
import org.example.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/bill")
@Tag(name = "Bill", description = "Endpoints for managing bills")
public class BillController {
    @Autowired
    private BillService billService;

    @Operation(summary = "Create a new bill", description = "Creates a new bill with the provided details.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bill created successfully")
    })
    @PostMapping
    public ResponseEntity<Bill> createBill(@RequestBody Bill bill) {
        bill.setId(UUID.randomUUID().toString());
        bill.setStatus("OPEN");
        bill.setCreatedAt(LocalDateTime.now());
        Bill savedBill = billService.createBill(bill);
        return ResponseEntity.ok(savedBill);
    }

    @Operation(summary = "Get all bills", description = "Retrieves a list of all bills.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of bills returned successfully")
    })
    @GetMapping
    public ResponseEntity<List<Bill>> getAllBills() {
        List<Bill> bills = billService.getAllBills();
        return ResponseEntity.ok(bills);
    }

    @Operation(summary = "Get bill by ID", description = "Retrieves a bill by its unique identifier.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bill found"),
        @ApiResponse(responseCode = "404", description = "Bill not found")
    })
    @GetMapping("/{billId}")
    public ResponseEntity<Bill> getBill(@PathVariable String billId) {
        return billService.getBillById(billId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update bill", description = "Updates an existing bill by its unique identifier.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bill updated successfully"),
        @ApiResponse(responseCode = "404", description = "Bill not found")
    })
    @PutMapping("/{billId}")
    public ResponseEntity<Bill> updateBill(@PathVariable String billId, @RequestBody Bill billDetails) {
        try {
            Bill updatedBill = billService.updateBill(billId, billDetails);
            return ResponseEntity.ok(updatedBill);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete bill", description = "Deletes a bill by its unique identifier.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Bill deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Bill not found")
    })
    @DeleteMapping("/{billId}")
    public ResponseEntity<Void> deleteBill(@PathVariable String billId) {
        try {
            billService.deleteBill(billId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
