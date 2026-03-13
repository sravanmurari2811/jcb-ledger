package com.example.jcbledger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jcbledger.databinding.ActivityLoginBinding;
import com.example.jcbledger.network.ApiService;
import com.example.jcbledger.network.RetrofitClient;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Warm up the Render backend immediately when the login screen opens.
        // This pings the server while the user is typing, significantly reducing
        // the wait time when they finally click Login.
        pingServer();

        binding.etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 10) {
                    binding.tilPhone.setError(null);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnLogin.setOnClickListener(v -> {
            String phone = binding.etPhone.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (phone.isEmpty() || phone.length() != 10) {
                binding.tilPhone.setError("Enter a valid 10-digit phone number");
                return;
            }
            if (password.isEmpty()) {
                binding.tilPassword.setError("Password is required");
                return;
            }
            
            performLogin(phone, password);
        });

        binding.btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void pingServer() {
        // Simple dummy call just to wake up the server.
        // Using a non-existent mobile number or just any GET request.
        apiService.getTotalPending("WAKEUP").enqueue(new Callback<Map<String, Double>>() {
            @Override
            public void onResponse(Call<Map<String, Double>> call, Response<Map<String, Double>> response) {
                // Ignore result, purpose is just to wake the JVM on Render.
            }
            @Override
            public void onFailure(Call<Map<String, Double>> call, Throwable t) {
                // Ignore failure
            }
        });
    }

    private void performLogin(String phone, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("phone", phone);
        body.put("password", password);

        apiService.login(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> user = response.body();
                    
                    String machineNumber = (String) user.get("vehicleNumber");
                    
                    // Store machine number and login metadata
                    SharedPreferences prefs = getSharedPreferences("JCBPrefs", MODE_PRIVATE);
                    prefs.edit()
                        .putString("machineNumber", machineNumber)
                        .putBoolean("isLoggedIn", true)
                        .putLong("lastLoginTime", System.currentTimeMillis())
                        .apply();

                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    finishAffinity();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Login error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
