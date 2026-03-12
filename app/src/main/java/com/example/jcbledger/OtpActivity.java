package com.example.jcbledger;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jcbledger.databinding.ActivityOtpBinding;

public class OtpActivity extends AppCompatActivity {

    private ActivityOtpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnVerifyOtp.setOnClickListener(v -> {
            String otp = binding.etOtp.getText().toString();

            if (otp.isEmpty() || otp.length() < 4) {
                Toast.makeText(this, "Enter any 4-6 digit code to continue", Toast.LENGTH_SHORT).show();
                return;
            }

            // Demo Mode: Allow any OTP
            Toast.makeText(this, "Login Successful (Demo Mode)", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, DashboardActivity.class));
            finishAffinity();
        });
    }
}
