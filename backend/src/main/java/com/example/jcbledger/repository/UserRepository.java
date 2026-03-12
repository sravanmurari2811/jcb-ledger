package com.example.jcbledger.repository;

import com.example.jcbledger.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    Optional<User> findByPhoneAndPassword(String phone, String password);
    List<User> findByRoleAndVehicleNumber(String role, String vehicleNumber);
}
