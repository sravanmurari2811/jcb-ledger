package com.example.jcbledger.controller;

import com.example.jcbledger.entity.DriverExpense;
import com.example.jcbledger.repository.DriverExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/driver-expenses")
public class DriverExpenseController {

    @Autowired
    private DriverExpenseRepository expenseRepository;

    @PostMapping
    public ResponseEntity<?> addExpense(@RequestBody DriverExpense expense) {
        return ResponseEntity.ok(expenseRepository.save(expense));
    }

    @GetMapping("/operator/{operatorId}")
    public ResponseEntity<List<DriverExpense>> getExpensesByOperator(@PathVariable String operatorId) {
        return ResponseEntity.ok(expenseRepository.findByOperatorId(operatorId));
    }

    @GetMapping("/total/{operatorId}")
    public ResponseEntity<Map<String, Double>> getTotalByOperator(@PathVariable String operatorId) {
        List<DriverExpense> expenses = expenseRepository.findByOperatorId(operatorId);
        double total = expenses.stream().mapToDouble(DriverExpense::getAmount).sum();
        return ResponseEntity.ok(Map.of("total", total));
    }
}
