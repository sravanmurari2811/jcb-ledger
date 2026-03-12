package com.example.jcbledger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jcbledger.databinding.ActivityDashboardBinding;
import com.example.jcbledger.model.Customer;
import com.example.jcbledger.network.ApiService;
import com.example.jcbledger.network.RetrofitClient;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private ApiService apiService;
    private String machineNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("JCBPrefs", MODE_PRIVATE);
        machineNumber = prefs.getString("machineNumber", "");

        apiService = RetrofitClient.getClient().create(ApiService.class);

        setupMenu();
        fetchStats();
        setupSearch();
        
        binding.btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (dialog, which) -> performLogout())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void performLogout() {
        SharedPreferences prefs = getSharedPreferences("JCBPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
        
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchStats();
    }

    private void fetchStats() {
        apiService.getTotalPending(machineNumber).enqueue(new Callback<Map<String, Double>>() {
            @Override
            public void onResponse(Call<Map<String, Double>> call, Response<Map<String, Double>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Double totalPending = response.body().get("totalPending");
                    if (totalPending != null) {
                        binding.tvTotalPending.setText(String.format(Locale.getDefault(), "₹ %.2f", totalPending));
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Double>> call, Throwable t) {
                // Silent failure or log
            }
        });
    }

    private void setupMenu() {
        binding.btnNewEntry.setOnClickListener(v -> {
            startActivity(new Intent(this, WorkEntryActivity.class));
        });

        binding.btnPendingBills.setOnClickListener(v -> {
            startActivity(new Intent(this, PendingBillsActivity.class));
        });

        binding.btnDriverExpense.setOnClickListener(v -> {
            startActivity(new Intent(this, DriverListActivity.class));
        });

        binding.btnReports.setOnClickListener(v -> {
            startActivity(new Intent(this, ReportsActivity.class));
        });
    }

    private void setupSearch() {
        binding.tilSearchMobile.setEndIconOnClickListener(v -> {
            String mobile = binding.etSearchMobile.getText().toString().trim();
            if (mobile.length() == 10) {
                apiService.getCustomerByMobile(mobile).enqueue(new Callback<Customer>() {
                    @Override
                    public void onResponse(Call<Customer> call, Response<Customer> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Intent intent = new Intent(DashboardActivity.this, CustomerSearchActivity.class);
                            intent.putExtra("mobile", mobile);
                            intent.putExtra("customerName", response.body().getName());
                            startActivity(intent);
                        } else {
                            Toast.makeText(DashboardActivity.this, "Customer not found with this mobile number", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Customer> call, Throwable t) {
                        Toast.makeText(DashboardActivity.this, "Network Error. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                binding.etSearchMobile.setError("Enter a valid 10-digit number");
            }
        });
    }
}
