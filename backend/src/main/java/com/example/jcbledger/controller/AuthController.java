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
        
        Optional<User> user = userService.login(phone, password);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid phone number or password"));
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
}
