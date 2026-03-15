package com.example.jcbledger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jcbledger.databinding.ActivityLoginBinding;
import com.example.jcbledger.network.ApiService;
import com.example.jcbledger.network.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.net.UnknownHostException;
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

        binding.btnLoginMenu.setOnClickListener(this::showLoginMenu);
    }

    private void showLoginMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenu().add(0, 1, 0, "About Us");
        popup.getMenu().add(0, 2, 1, "Contact Admin");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                showAboutUsDialog();
                return true;
            } else if (item.getItemId() == 2) {
                showContactInfoDialog();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showAboutUsDialog() {
        new AlertDialog.Builder(this)
            .setTitle("About JCBLedger")
            .setMessage("JCBLedger v1.0.0\n\nA professional solution for JCB machine owners and operators to manage work entries, payments, and expenses efficiently.\n\nDeveloped for modern field operations.")
            .setPositiveButton("Close", null)
            .show();
    }

    private void showContactInfoDialog() {
        String adminPhone = "7780240688";
        String adminEmail = "support@jcbledger.com"; 

        new AlertDialog.Builder(this)
            .setTitle("Contact Admin")
            .setMessage("For approvals or support, please contact us:\n\n" +
                    "Phone: " + adminPhone + "\n" +
                    "Email: " + adminEmail)
            .setPositiveButton("Call Now", (dialog, which) -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + adminPhone));
                startActivity(intent);
            })
            .setNegativeButton("Close", null)
            .show();
    }

    private void pingServer() {
        apiService.ping().enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {}
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {}
        });
    }

    private void performLogin(String phone, String password) {
        binding.btnLogin.setEnabled(false);
        binding.btnLogin.setText("Logging in...");

        Map<String, String> body = new HashMap<>();
        body.put("phone", phone);
        body.put("password", password);

        apiService.login(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> user = response.body();
                    
                    String machineNumber = (String) user.get("vehicleNumber");
                    String role = (String) user.get("role");
                    String name = (String) user.get("name");
                    
                    SharedPreferences prefs = getSharedPreferences("JCBPrefs", MODE_PRIVATE);
                    prefs.edit()
                        .putString("machineNumber", machineNumber)
                        .putString("userRole", role)
                        .putString("userName", name)
                        .putBoolean("isLoggedIn", true)
                        .putLong("lastLoginTime", System.currentTimeMillis())
                        .apply();

                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    finishAffinity();
                } else {
                    String errorMsg = "Invalid credentials";
                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            Map<String, String> errorMap = new Gson().fromJson(errorJson, new TypeToken<Map<String, String>>(){}.getType());
                            if (errorMap != null && errorMap.containsKey("message")) {
                                errorMsg = errorMap.get("message");
                            }
                        }
                    } catch (Exception e) {}
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    binding.btnLogin.setEnabled(true);
                    binding.btnLogin.setText("LOGIN");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                String errorMsg = "Connection error";
                if (t instanceof UnknownHostException) {
                    errorMsg = "No internet connection";
                } else if (t instanceof IOException) {
                    errorMsg = "Server unreachable. Please try again later.";
                } else {
                    errorMsg = "Error: " + t.getMessage();
                }
                
                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setText("LOGIN");
            }
        });
    }
}
