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

        // Default all new registrations to PENDING
        user.setStatus("PENDING");

        if ("OPERATOR".equalsIgnoreCase(user.getRole())) {
            String vehicleNumber = user.getVehicleNumber();
            if (vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
                throw new Exception("Vehicle number is required for Operator registration");
            }
            
            List<User> owners = userRepository.findByRoleAndVehicleNumber("OWNER", vehicleNumber);
            if (owners.isEmpty()) {
                throw new Exception("No Owner found for vehicle: " + vehicleNumber + ". Please ask your Owner to register first.");
            }
        }
        
        return userRepository.save(user);
    }

    public Optional<User> login(String phone, String password) throws Exception {
        Optional<User> userOpt = userRepository.findByPhoneAndPassword(phone, password);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Strict check: Only ACTIVE users can log in
            if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
                if ("OWNER".equalsIgnoreCase(user.getRole())) {
                    throw new Exception("Waiting for Admin approval. Please contact support.");
                } else {
                    throw new Exception("Waiting for Owner approval. Please contact your machine owner.");
                }
            }
            return Optional.of(user);
        }
        return Optional.empty();
    }
}
