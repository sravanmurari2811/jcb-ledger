package com.example.jcbledger.controller;

import com.example.jcbledger.entity.User;
import com.example.jcbledger.repository.UserRepository;
import com.example.jcbledger.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String password = request.get("password");
        
        try {
            Optional<User> user = userService.login(phone, password);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.status(401).body(Map.of("message", "Invalid phone number or password"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User registeredUser = userService.register(user);
            return ResponseEntity.ok(registeredUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/operators")
    public ResponseEntity<List<User>> getOperatorsByVehicle(@RequestParam String vehicleNumber) {
        return ResponseEntity.ok(userRepository.findByRoleAndVehicleNumber("OPERATOR", vehicleNumber));
    }

    @GetMapping("/pending-approvals")
    public ResponseEntity<List<User>> getPendingApprovals(@RequestParam String role, @RequestParam(required = false) String vehicleNumber) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.ok(userRepository.findByStatusAndRole("PENDING", "OWNER"));
        } else if ("OWNER".equalsIgnoreCase(role)) {
            return ResponseEntity.ok(userRepository.findByStatusAndRoleAndVehicleNumber("PENDING", "OPERATOR", vehicleNumber));
        }
        return ResponseEntity.ok(List.of());
    }

    @PostMapping("/approve-user/{id}")
    public ResponseEntity<?> approveUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setStatus("ACTIVE");
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User approved successfully"));
    }

    @GetMapping("/all-users")
    public ResponseEntity<List<User>> getAllUsers(@RequestParam String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            // Admin can see all users
            return ResponseEntity.ok(userRepository.findAll());
        }
        return ResponseEntity.ok(List.of());
    }

    @PostMapping("/update-user-status")
    public ResponseEntity<?> updateUserStatus(@RequestBody Map<String, Object> payload) {
        Long id = Long.valueOf(payload.get("userId").toString());
        String status = (String) payload.get("status");
        User user = userRepository.findById(id).orElseThrow();
        user.setStatus(status);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User status updated to " + status));
    }
}
