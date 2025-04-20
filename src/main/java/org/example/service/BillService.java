package org.example.service;

import org.example.model.Bill;
import org.example.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BillService {
    @Autowired
    private BillRepository billRepository;

    public Bill createBill(Bill bill) {
        return billRepository.save(bill);
    }

    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    public Optional<Bill> getBillById(String id) {
        return billRepository.findById(id);
    }

    public Bill updateBill(String id, Bill billDetails) {
        return billRepository.findById(id)
                .map(bill -> {
                    bill.setTitle(billDetails.getTitle());
                    bill.setCreatorName(billDetails.getCreatorName());
                    bill.setItems(billDetails.getItems());
                    bill.setParticipants(billDetails.getParticipants());
                    bill.setTax(billDetails.getTax());
                    bill.setServiceCharge(billDetails.getServiceCharge());
                    bill.setStatus(billDetails.getStatus());
                    bill.setCreatedAt(billDetails.getCreatedAt());
                    return billRepository.save(bill);
                })
                .orElseThrow(() -> new RuntimeException("Bill not found with id " + id));
    }

    public void deleteBill(String id) {
        billRepository.deleteById(id);
    }
}
