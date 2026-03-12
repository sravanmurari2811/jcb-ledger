package com.example.jcbledger.controller;

import com.example.jcbledger.dto.WorkEntryRequest;
import com.example.jcbledger.entity.Customer;
import com.example.jcbledger.entity.WorkEntry;
import com.example.jcbledger.entity.TransactionHistory;
import com.example.jcbledger.repository.CustomerRepository;
import com.example.jcbledger.repository.WorkEntryRepository;
import com.example.jcbledger.repository.TransactionHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/work-entries")
public class WorkEntryController {

    @Autowired
    private WorkEntryRepository workEntryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionHistoryRepository transactionRepository;

    @PostMapping
    public ResponseEntity<WorkEntry> createWorkEntry(@RequestBody WorkEntryRequest request, @RequestParam String machineNumber) {
        Customer customer = customerRepository.findByMobile(request.getCustomerMobile())
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setMobile(request.getCustomerMobile());
                    newCustomer.setName(request.getCustomerName());
                    return customerRepository.save(newCustomer);
                });

        WorkEntry entry = new WorkEntry();
        entry.setCustomer(customer);
        entry.setWorkDate(LocalDate.parse(request.getWorkDate()));
        entry.setWorkType(request.getWorkType());
        entry.setMachineNumber(machineNumber);
        
        if ("EARTH_WORK".equals(request.getWorkType())) {
            entry.setStartTime(LocalTime.parse(request.getStartTime()));
            entry.setEndTime(LocalTime.parse(request.getEndTime()));
            entry.setTotalHours(request.getTotalHours());
            entry.setRate(request.getRate());
        } else {
            entry.setTrips(request.getTrips());
            entry.setChargePerTrip(request.getChargePerTrip());
            entry.setTractorNumber(request.getTractorNumber());
            entry.setTotalHours(0);
        }
        
        entry.setTravelCost(request.getTravelCost());
        entry.setTotalAmount(request.getTotalAmount());
        entry.setAmountPaid(request.getAmountPaid());
        entry.setPendingAmount(request.getPendingAmount());
        entry.setPaymentMethod(request.getPaymentMethod());
        entry.setPlace(request.getPlace());

        updateStatus(entry);
        WorkEntry savedEntry = workEntryRepository.save(entry);

        if (request.getAmountPaid() > 0) {
            TransactionHistory tx = new TransactionHistory();
            tx.setCustomer(customer);
            tx.setAmount(request.getAmountPaid());
            tx.setTransactionDate(LocalDate.parse(request.getWorkDate()).atStartOfDay());
            tx.setPaymentMethod(request.getPaymentMethod());
            tx.setNote("Initial payment for work on " + request.getWorkDate());
            tx.setMachineNumber(machineNumber);
            transactionRepository.save(tx);
        }

        return ResponseEntity.ok(savedEntry);
    }

    private void updateStatus(WorkEntry entry) {
        if (entry.getPendingAmount() <= 0) {
            entry.setStatus("cleared");
        } else if (entry.getAmountPaid() == 0) {
            entry.setStatus("pending");
        } else {
            entry.setStatus("partial");
        }
    }

    @PostMapping("/update-payment/{id}")
    public ResponseEntity<WorkEntry> updatePayment(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        WorkEntry entry = workEntryRepository.findById(id).orElseThrow();
        double additionalAmount = Double.parseDouble(payload.get("amount").toString());
        String method = (String) payload.get("paymentMethod");

        entry.setAmountPaid(entry.getAmountPaid() + additionalAmount);
        entry.setPendingAmount(entry.getTotalAmount() - entry.getAmountPaid());
        updateStatus(entry);
        workEntryRepository.save(entry);

        TransactionHistory tx = new TransactionHistory();
        tx.setCustomer(entry.getCustomer());
        tx.setAmount(additionalAmount);
        tx.setTransactionDate(LocalDateTime.now());
        tx.setPaymentMethod(method);
        tx.setNote("Payment received for bill ID: " + id);
        tx.setMachineNumber(entry.getMachineNumber());
        transactionRepository.save(tx);

        return ResponseEntity.ok(entry);
    }

    @GetMapping("/total-pending")
    public ResponseEntity<Map<String, Double>> getTotalPending(@RequestParam String machineNumber) {
        List<WorkEntry> entries = workEntryRepository.findByMachineNumber(machineNumber);
        double total = entries.stream()
                .mapToDouble(WorkEntry::getPendingAmount)
                .sum();
        return ResponseEntity.ok(Map.of("totalPending", total));
    }

    @GetMapping("/pending-bills")
    public ResponseEntity<List<WorkEntry>> getPendingBills(@RequestParam String machineNumber) {
        return ResponseEntity.ok(workEntryRepository.findByStatusNotAndMachineNumber("cleared", machineNumber));
    }

    @GetMapping("/reports")
    public ResponseEntity<List<WorkEntry>> getReports(
            @RequestParam String filter,
            @RequestParam(required = false, defaultValue = "ALL") String statusFilter,
            @RequestParam String machineNumber,
            @RequestParam(required = false) String date) {
        
        LocalDate clientDate = (date != null && !date.isEmpty()) ? LocalDate.parse(date) : LocalDate.now();
        List<WorkEntry> allEntriesForMachine = workEntryRepository.findByMachineNumber(machineNumber);
        
        List<WorkEntry> filteredEntries;

        if ("ALL".equals(filter)) {
            filteredEntries = allEntriesForMachine;
        } else {
            LocalDate start;
            LocalDate end = clientDate;

            switch (filter) {
                case "WEEK":
                    start = clientDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    break;
                case "MONTH":
                    start = clientDate.with(TemporalAdjusters.firstDayOfMonth());
                    break;
                case "TODAY":
                default:
                    start = clientDate;
                    break;
            }

            filteredEntries = allEntriesForMachine.stream()
                .filter(e -> e.getWorkDate() != null)
                .filter(e -> (e.getWorkDate().isEqual(start) || e.getWorkDate().isAfter(start)) && 
                             (e.getWorkDate().isEqual(end) || e.getWorkDate().isBefore(end)))
                .collect(Collectors.toList());
        }

        if ("PENDING".equals(statusFilter)) {
            filteredEntries = filteredEntries.stream()
                    .filter(e -> !"cleared".equals(e.getStatus()))
                    .collect(Collectors.toList());
        }
        
        filteredEntries.sort(Comparator.comparing(WorkEntry::getWorkDate).reversed());
        return ResponseEntity.ok(filteredEntries);
    }

    @GetMapping("/customer/{mobile}")
    public ResponseEntity<List<WorkEntry>> getCustomerWorkEntries(
            @PathVariable("mobile") String mobile,
            @RequestParam(required = false) boolean pendingOnly,
            @RequestParam String machineNumber) {
        List<WorkEntry> entries = workEntryRepository.findByCustomer_MobileAndMachineNumber(mobile, machineNumber);
        if (pendingOnly) {
            entries = entries.stream()
                    .filter(e -> !"cleared".equals(e.getStatus()))
                    .collect(Collectors.toList());
        }
        entries.sort(Comparator.comparing(WorkEntry::getWorkDate).reversed());
        return ResponseEntity.ok(entries);
    }

    @PostMapping("/receive-payment/{mobile}")
    public ResponseEntity<?> receivePayment(@PathVariable String mobile, @RequestBody Map<String, Object> payload) {
        Double paymentAmount = Double.parseDouble(payload.get("amount").toString());
        String method = payload.get("paymentMethod").toString();
        String dateStr = payload.get("date").toString();
        String machineNumber = payload.get("machineNumber").toString();
        
        LocalDate paymentDate = LocalDate.parse(dateStr);
        Customer customer = customerRepository.findByMobile(mobile).orElseThrow();
        
        List<WorkEntry> pendingEntries = workEntryRepository.findByCustomer_MobileAndMachineNumber(mobile, machineNumber).stream()
                .filter(e -> e.getPendingAmount() > 0)
                .sorted(Comparator.comparing(WorkEntry::getWorkDate))
                .collect(Collectors.toList());

        double remainingPayment = paymentAmount;
        for (WorkEntry entry : pendingEntries) {
            if (remainingPayment <= 0) break;

            double entryPending = entry.getPendingAmount();
            if (remainingPayment >= entryPending) {
                entry.setAmountPaid(entry.getAmountPaid() + entryPending);
                entry.setPendingAmount(0.0);
                remainingPayment -= entryPending;
            } else {
                entry.setAmountPaid(entry.getAmountPaid() + remainingPayment);
                entry.setPendingAmount(entryPending - remainingPayment);
                remainingPayment = 0;
            }
            updateStatus(entry);
            workEntryRepository.save(entry);
        }

        TransactionHistory tx = new TransactionHistory();
        tx.setCustomer(customer);
        tx.setAmount(paymentAmount);
        tx.setTransactionDate(paymentDate.atStartOfDay());
        tx.setPaymentMethod(method);
        tx.setNote("Customer Payment");
        tx.setMachineNumber(machineNumber);
        transactionRepository.save(tx);

        return ResponseEntity.ok(Map.of("message", "Payment processed", "remainingBalance", remainingPayment));
    }
}
