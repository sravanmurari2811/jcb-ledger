package com.example.jcbledger.service;

import com.example.jcbledger.entity.WorkEntry;
import com.example.jcbledger.repository.WorkEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WorkEntryService {

    @Autowired
    private WorkEntryRepository workEntryRepository;

    public WorkEntry saveWorkEntry(WorkEntry entry) {
        return workEntryRepository.save(entry);
    }

    public List<WorkEntry> getEntriesByCustomer(String mobile, String machineNumber) {
        return workEntryRepository.findByCustomer_MobileAndMachineNumber(mobile, machineNumber);
    }

    public Double getTotalPendingAmount(String machineNumber) {
        return workEntryRepository.findByMachineNumber(machineNumber).stream()
                .mapToDouble(WorkEntry::getPendingAmount)
                .sum();
    }

    public List<WorkEntry> getPendingBills(String machineNumber) {
        return workEntryRepository.findByStatusNotAndMachineNumber("cleared", machineNumber);
    }

    public List<WorkEntry> getReports(String filter, String statusFilter, String machineNumber) {
        return workEntryRepository.findByMachineNumber(machineNumber);
    }
}
