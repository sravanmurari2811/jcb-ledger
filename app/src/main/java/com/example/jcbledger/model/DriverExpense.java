package com.example.jcbledger.model;

public class DriverExpense {
    private Long id;
    private String operatorId;
    private String operatorName;
    private String vehicleNumber;
    private String date;
    private double amount;
    private String type; // EXPENSE or SALARY
    private String note;

    public DriverExpense() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
