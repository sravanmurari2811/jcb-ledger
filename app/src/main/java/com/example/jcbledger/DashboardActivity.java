package com.example.jcbledger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
    private String userRole;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getGetSharedPreferences();
        machineNumber = prefs.getString("machineNumber", "");
        userRole = prefs.getString("userRole", "OPERATOR");
        userName = prefs.getString("userName", "User");

        apiService = RetrofitClient.getClient().create(ApiService.class);

        setupMenu();
        setupSearch();
        
        binding.btnMenu.setOnClickListener(this::showSideMenu);
    }

    private SharedPreferences getGetSharedPreferences() {
        return getSharedPreferences("JCBPrefs", MODE_PRIVATE);
    }

    private void showSideMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        
        int order = 0;
        if ("ADMIN".equalsIgnoreCase(userRole) || "OWNER".equalsIgnoreCase(userRole)) {
            popup.getMenu().add(0, 1, order++, "User Requests");
        }
        
        if ("ADMIN".equalsIgnoreCase(userRole)) {
            popup.getMenu().add(0, 6, order++, "User Management");
        }
        
        popup.getMenu().add(0, 2, order++, "My Profile");
        popup.getMenu().add(0, 5, order++, "Logout");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    startActivity(new Intent(this, ApprovalActivity.class));
                    return true;
                case 6:
                    startActivity(new Intent(this, UserManagementActivity.class));
                    return true;
                case 2:
                    showProfileDialog();
                    return true;
                case 5:
                    showLogoutDialog();
                    return true;
                default:
                    return false;
            }
        });
        popup.show();
    }

    private void showProfileDialog() {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        
        appendStyled(builder, "Name: ", userName);
        builder.append("\n\n");
        appendStyled(builder, "Role: ", userRole);
        builder.append("\n\n");
        appendStyled(builder, "Vehicle: ", machineNumber);

        new AlertDialog.Builder(this)
            .setTitle("User Profile")
            .setMessage(builder)
            .setPositiveButton("OK", null)
            .show();
    }

    private void appendStyled(SpannableStringBuilder builder, String key, String value) {
        int start = builder.length();
        builder.append(key);
        int end = builder.length();
        
        // Color for Key
        builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary)), 
                start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Bold for Key
        builder.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        builder.append(value);
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
        SharedPreferences prefs = getGetSharedPreferences();
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
