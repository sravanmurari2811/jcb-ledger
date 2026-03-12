package com.example.jcbledger.dto;

public class WorkEntryRequest {
    private String customerMobile;
    private String customerName;
    private String workDate;
    private String workType;
    private String startTime;
    private String endTime;
    private double totalHours;
    private double rate;
    private int trips;
    private double chargePerTrip;
    private String tractorNumber;
    private double travelCost;
    private double totalAmount;
    private double amountPaid;
    private double pendingAmount;
    private String paymentMethod;
    private String place;

    public WorkEntryRequest() {}

    public String getCustomerMobile() { return customerMobile; }
    public void setCustomerMobile(String customerMobile) { this.customerMobile = customerMobile; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getWorkDate() { return workDate; }
    public void setWorkDate(String workDate) { this.workDate = workDate; }
    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
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
    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }
}
