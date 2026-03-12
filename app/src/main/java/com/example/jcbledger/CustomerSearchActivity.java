package com.example.jcbledger;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.jcbledger.adapter.WorkEntryAdapter;
import com.example.jcbledger.databinding.ActivityCustomerSearchBinding;
import com.example.jcbledger.model.WorkEntry;
import com.example.jcbledger.network.ApiService;
import com.example.jcbledger.network.RetrofitClient;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerSearchActivity extends AppCompatActivity {

    private ActivityCustomerSearchBinding binding;
    private ApiService apiService;
    private WorkEntryAdapter adapter;
    private final List<WorkEntry> workEntries = new ArrayList<>();
    private String mobile;
    private String machineNumber;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("JCBPrefs", MODE_PRIVATE);
        machineNumber = prefs.getString("machineNumber", "");

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        apiService = RetrofitClient.getClient().create(ApiService.class);
        mobile = getIntent().getStringExtra("mobile");
        String customerName = getIntent().getStringExtra("customerName");

        // Set Header details for confirmation with explicit labels
        binding.tvHeaderCustomerName.setText("Name :- " + (customerName != null ? customerName : "Unknown Customer"));
        binding.tvHeaderCustomerMobile.setText("Mobile :- " + (mobile != null ? mobile : ""));

        setupRecyclerView();
        
        binding.cbPendingOnly.setOnCheckedChangeListener((buttonView, isChecked) -> fetchHistory());
        binding.btnReceivePayment.setOnClickListener(v -> showPaymentDialog());
        binding.btnViewAllTransactions.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransactionHistoryActivity.class);
            intent.putExtra("mobile", mobile);
            startActivity(intent);
        });
        
        fetchHistory();
    }

    private void setupRecyclerView() {
        adapter = new WorkEntryAdapter(workEntries, entry -> {
            Intent intent = new Intent(this, WorkEntryDetailsActivity.class);
            intent.putExtra("id", entry.getId());
            intent.putExtra("customerName", entry.getCustomer().getName());
            intent.putExtra("mobile", entry.getCustomer().getMobile());
            intent.putExtra("date", entry.getWorkDate());
            intent.putExtra("startTime", entry.getStartTime());
            intent.putExtra("endTime", entry.getEndTime());
            intent.putExtra("rate", entry.getRate());
            intent.putExtra("trips", entry.getTrips());
            intent.putExtra("chargePerTrip", entry.getChargePerTrip());
            intent.putExtra("tractorNumber", entry.getTractorNumber());
            intent.putExtra("workType", entry.getWorkType());
            intent.putExtra("travel", entry.getTravelCost());
            intent.putExtra("total", entry.getTotalAmount());
            intent.putExtra("paid", entry.getAmountPaid());
            intent.putExtra("pending", entry.getPendingAmount());
            intent.putExtra("method", entry.getPaymentMethod());
            intent.putExtra("status", entry.getStatus());
            intent.putExtra("place", entry.getPlace());
            startActivity(intent);
        });
        binding.rvCustomerHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCustomerHistory.setAdapter(adapter);
    }

    private void fetchHistory() {
        boolean pendingOnly = binding.cbPendingOnly.isChecked();
        apiService.getCustomerWorkEntries(mobile, pendingOnly, machineNumber).enqueue(new Callback<List<WorkEntry>>() {
            @Override
            public void onResponse(Call<List<WorkEntry>> call, Response<List<WorkEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    workEntries.clear();
                    workEntries.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    calculateSummaries();
                }
            }

            @Override
            public void onFailure(Call<List<WorkEntry>> call, Throwable t) {
                Toast.makeText(CustomerSearchActivity.this, "Search failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateSummaries() {
        double totalPending = 0;
        double totalHours = 0;
        for (WorkEntry entry : workEntries) {
            totalPending += entry.getPendingAmount();
            if ("EARTH_WORK".equals(entry.getWorkType())) {
                totalHours += entry.getTotalHours();
            }
        }
        binding.tvCustomerTotalPending.setText(String.format(Locale.getDefault(), "₹ %.2f", totalPending));
        binding.tvCustomerTotalHours.setText(String.format(Locale.getDefault(), "%.2f hrs", totalHours));
        
        binding.btnReceivePayment.setVisibility(totalPending > 0 ? View.VISIBLE : View.GONE);
    }

    private void showPaymentDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_receive_payment, null);
        EditText etAmount = dialogView.findViewById(R.id.etPaymentAmount);
        EditText etDate = dialogView.findViewById(R.id.etPaymentDate);
        RadioGroup rgMethod = dialogView.findViewById(R.id.rgPaymentMethod);

        Calendar cal = Calendar.getInstance();
        etDate.setText(dateFormat.format(cal.getTime()));
        etDate.setOnClickListener(v -> new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            cal.set(year, month, dayOfMonth);
            etDate.setText(dateFormat.format(cal.getTime()));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show());
        
        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("PAY", (dialog, which) -> {
                    String amountStr = etAmount.getText().toString();
                    if (!amountStr.isEmpty()) {
                        String method = rgMethod.getCheckedRadioButtonId() == R.id.rbCash ? "Cash" : "UPI";
                        processPayment(Double.parseDouble(amountStr), method, etDate.getText().toString());
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void processPayment(double amount, String method, String date) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", amount);
        payload.put("paymentMethod", method);
        payload.put("date", date);
        payload.put("machineNumber", machineNumber);
        
        apiService.receivePayment(mobile, payload).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CustomerSearchActivity.this, "Payment Recorded Successfully", Toast.LENGTH_LONG).show();
                    fetchHistory();
                } else {
                    Toast.makeText(CustomerSearchActivity.this, "Payment Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(CustomerSearchActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
