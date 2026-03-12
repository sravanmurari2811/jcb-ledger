package com.example.jcbledger.model;

public class Customer {
    private Long id;
    private String name;
    private String mobile;

    public Customer() {}

    public Customer(String mobile, String name) {
        this.mobile = mobile;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
