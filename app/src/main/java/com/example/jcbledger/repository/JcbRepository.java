package com.example.jcbledger.repository;

import com.example.jcbledger.model.Customer;
import com.example.jcbledger.model.WorkEntry;
import com.example.jcbledger.model.User;

import java.util.List;
import androidx.lifecycle.LiveData;

public interface JcbRepository{
    // User Management
    LiveData<User> login(String phone, String password);
    LiveData<Boolean> registerOperator(String name, String phone);
    
    // Customer Management
    LiveData<Customer> getCustomerByMobile(String mobile);
    LiveData<Boolean> addCustomer(Customer customer);
    
    // Work Entry
    LiveData<Boolean> addWorkEntry(WorkEntry entry);
    LiveData<List<WorkEntry>> getWorkEntriesForCustomer(String mobile);
    
    // Reports
    LiveData<Double> getPendingBalance(String mobile);
}
