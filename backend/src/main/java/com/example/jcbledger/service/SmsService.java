package com.example.jcbledger.service;

import org.springframework.stereotype.Service;

@Service
public class SmsService {
    // SMS service disabled as per requirements.
    public void sendOtp(String toPhone, String otp) {
        System.out.println("OTP for " + toPhone + " is: " + otp);
    }
}
