package org.example.controller;

import org.example.model.Bill;
import org.example.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/bill")
public class BillController {
    @Autowired
    private BillRepository billRepository;

    @PostMapping
    public ResponseEntity<Bill> createBill(@RequestBody Bill bill) {
        String id = UUID.randomUUID().toString();
        bill.setId(id);
        bill.setStatus("OPEN");
        bill.setCreatedAt(LocalDateTime.now());
        Bill savedBill = billRepository.save(bill);
        return ResponseEntity.ok(savedBill);
    }

    @GetMapping("/{billId}")
    public ResponseEntity<Bill> getBill(@PathVariable String billId) {
        return billRepository.findById(billId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
