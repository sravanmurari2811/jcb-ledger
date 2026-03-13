package com.example.jcbledger.repository;

import com.example.jcbledger.entity.DriverExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DriverExpenseRepository extends JpaRepository<DriverExpense, Long> {
    List<DriverExpense> findByOperatorId(String operatorId);
    List<DriverExpense> findByVehicleNumber(String vehicleNumber);
    
    Optional<DriverExpense> findByOperatorIdAndDateAndAmountAndTypeAndNote(
            String operatorId, LocalDate date, double amount, String type, String note);
}
