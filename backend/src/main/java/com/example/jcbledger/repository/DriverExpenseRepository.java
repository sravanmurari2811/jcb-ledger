package com.example.jcbledger.repository;

import com.example.jcbledger.entity.DriverExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DriverExpenseRepository extends JpaRepository<DriverExpense, Long> {
    List<DriverExpense> findByOperatorId(String operatorId);
    List<DriverExpense> findByVehicleNumber(String vehicleNumber);
}
