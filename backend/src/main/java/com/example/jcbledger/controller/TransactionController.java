package com.example.jcbledger.controller;

import com.example.jcbledger.entity.TransactionHistory;
import com.example.jcbledger.repository.TransactionHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    @Autowired
    private TransactionHistoryRepository transactionRepository;

    @GetMapping("/customer/{mobile}")
    public ResponseEntity<List<TransactionHistory>> getCustomerTransactions(
            @PathVariable String mobile,
            @RequestParam String machineNumber) {
        return ResponseEntity.ok(transactionRepository.findByCustomer_MobileAndMachineNumber(mobile, machineNumber));
    }
}
