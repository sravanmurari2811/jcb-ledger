package com.example.jcbledger.repository;

import com.example.jcbledger.entity.WorkEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface WorkEntryRepository extends JpaRepository<WorkEntry, Long> {

    List<WorkEntry> findByCustomer_MobileAndMachineNumber(String mobile, String machineNumber);
    
    List<WorkEntry> findByStatusNotAndMachineNumber(String status, String machineNumber);

    List<WorkEntry> findByWorkDateBetweenAndMachineNumber(LocalDate start, LocalDate end, String machineNumber);

    List<WorkEntry> findByMachineNumber(String machineNumber);
    
    List<WorkEntry> findByCustomer_Mobile(String mobile);
}
