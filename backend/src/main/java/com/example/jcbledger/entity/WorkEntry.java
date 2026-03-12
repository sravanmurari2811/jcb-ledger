package com.example.jcbledger.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "work_entries")
public class WorkEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private double totalHours;
    private double rate;
    private int trips;
    private double chargePerTrip;
    private String tractorNumber;
    private String workType;
    private double travelCost;
    private double totalAmount;
    private double amountPaid;
    private double pendingAmount;
    private String paymentMethod;
    private String status;
    private String place;
    private String machineNumber;

    public WorkEntry() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public double getTotalHours() { return totalHours; }
    public void setTotalHours(double totalHours) { this.totalHours = totalHours; }
    public double getRate() { return rate; }
    public void setRate(double rate) { this.rate = rate; }
    public int getTrips() { return trips; }
    public void setTrips(int trips) { this.trips = trips; }
    public double getChargePerTrip() { return chargePerTrip; }
    public void setChargePerTrip(double chargePerTrip) { this.chargePerTrip = chargePerTrip; }
    public String getTractorNumber() { return tractorNumber; }
    public void setTractorNumber(String tractorNumber) { this.tractorNumber = tractorNumber; }
    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }
    public double getTravelCost() { return travelCost; }
    public void setTravelCost(double travelCost) { this.travelCost = travelCost; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }
    public double getPendingAmount() { return pendingAmount; }
    public void setPendingAmount(double pendingAmount) { this.pendingAmount = pendingAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }
    public String getMachineNumber() { return machineNumber; }
    public void setMachineNumber(String machineNumber) { this.machineNumber = machineNumber; }
}
