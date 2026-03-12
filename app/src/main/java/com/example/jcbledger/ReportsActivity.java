package com.example.jcbledger;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.jcbledger.adapter.WorkEntryAdapter;
import com.example.jcbledger.databinding.ActivityReportsBinding;
import com.example.jcbledger.model.WorkEntry;
import com.example.jcbledger.network.ApiService;
import com.example.jcbledger.network.RetrofitClient;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsActivity extends AppCompatActivity {

    private ActivityReportsBinding binding;
    private ApiService apiService;
    private WorkEntryAdapter adapter;
    private List<WorkEntry> reportList = new ArrayList<>();
    private String machineNumber;
    private static final String TAG = "ReportsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("JCBPrefs", MODE_PRIVATE);
        machineNumber = prefs.getString("machineNumber", "");
        Log.d(TAG, "Machine Number: " + machineNumber);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        apiService = RetrofitClient.getClient().create(ApiService.class);

        setupFilters();
        setupRecyclerView();
    }

    private void setupFilters() {
        String[] filters = {"All Work", "Today", "This Week", "This Month"};
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filters);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFilter.setAdapter(timeAdapter);

        String[] statuses = {"All Status", "Pending Only"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerStatus.setAdapter(statusAdapter);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        binding.spinnerFilter.setOnItemSelectedListener(listener);
        binding.spinnerStatus.setOnItemSelectedListener(listener);
        
        binding.spinnerFilter.setSelection(0);
    }

    private void applyFilters() {
        int timePos = binding.spinnerFilter.getSelectedItemPosition();
        String timeFilter = "ALL";
        if (timePos == 1) timeFilter = "TODAY";
        else if (timePos == 2) timeFilter = "WEEK";
        else if (timePos == 3) timeFilter = "MONTH";

        String statusFilter = binding.spinnerStatus.getSelectedItemPosition() == 1 ? "PENDING" : "ALL";

        fetchReports(timeFilter, statusFilter);
    }

    private void setupRecyclerView() {
        adapter = new WorkEntryAdapter(reportList, entry -> {
            // Details logic - can navigate to details if needed
        });
        binding.rvReports.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReports.setAdapter(adapter);
    }

    private void fetchReports(String timeFilter, String statusFilter) {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Log.d(TAG, "Fetching reports: Filter=" + timeFilter + ", Status=" + statusFilter + ", Machine=" + machineNumber + ", Date=" + currentDate);
        
        apiService.getReports(timeFilter, statusFilter, machineNumber, currentDate).enqueue(new Callback<List<WorkEntry>>() {
            @Override
            public void onResponse(Call<List<WorkEntry>> call, Response<List<WorkEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    reportList.clear();
                    reportList.addAll(response.body());
                    Log.d(TAG, "Reports received: " + reportList.size());
                    adapter.notifyDataSetChanged();
                    calculateSummary();
                } else {
                    Log.e(TAG, "Error fetching reports: " + response.code());
                    Toast.makeText(ReportsActivity.this, "Error fetching reports: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<WorkEntry>> call, Throwable t) {
                Log.e(TAG, "Failed to load reports", t);
                Toast.makeText(ReportsActivity.this, "Failed to load reports: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateSummary() {
        double totalHours = 0;
        double totalReceived = 0;
        double totalPending = 0;

        for (WorkEntry entry : reportList) {
            if ("EARTH_WORK".equals(entry.getWorkType())) {
                totalHours += entry.getTotalHours();
            }
            totalReceived += entry.getAmountPaid();
            totalPending += entry.getPendingAmount();
        }

        updateSummaryCard(binding.cardHours.getRoot(), "Hours Worked", String.format(Locale.getDefault(), "%.2f", totalHours));
        updateSummaryCard(binding.cardReceived.getRoot(), "Amount Received", String.format(Locale.getDefault(), "₹ %.0f", totalReceived));
        updateSummaryCard(binding.cardPending.getRoot(), "Pending Amount", String.format(Locale.getDefault(), "₹ %.0f", totalPending));
    }

    private void updateSummaryCard(View view, String label, String value) {
        ((android.widget.TextView)view.findViewById(R.id.tvSummaryLabel)).setText(label);
        ((android.widget.TextView)view.findViewById(R.id.tvSummaryValue)).setText(value);
    }
}
