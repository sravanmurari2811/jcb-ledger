package com.example.jcbledger.repository;

import com.example.jcbledger.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {
    List<TransactionHistory> findByCustomer_MobileAndMachineNumber(String mobile, String machineNumber);
}
