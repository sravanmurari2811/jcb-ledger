package com.example.jcbledger.service;

import com.example.jcbledger.entity.User;
import com.example.jcbledger.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User register(User user) throws Exception {
        if (userRepository.findByPhone(user.getPhone()).isPresent()) {
            throw new Exception("Phone number already registered");
        }
        
        if (user.getVehicleNumber() != null) {
            user.setVehicleNumber(user.getVehicleNumber().toUpperCase());
        }

        // Logic: No operator should be allowed for a specific vehicle if respective owner is not found
        if ("OPERATOR".equalsIgnoreCase(user.getRole())) {
            String vehicleNumber = user.getVehicleNumber();
            if (vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
                throw new Exception("Vehicle number is required for Operator registration");
            }
            
            List<User> owners = userRepository.findByRoleAndVehicleNumber("OWNER", vehicleNumber);
            if (owners.isEmpty()) {
                throw new Exception("No Owner found for vehicle: " + vehicleNumber + ". Operator cannot register.");
            }
        }
        
        user.setStatus("ACTIVE");
        return userRepository.save(user);
    }

    public Optional<User> login(String phone, String password) {
        return userRepository.findByPhoneAndPassword(phone, password);
    }
}
