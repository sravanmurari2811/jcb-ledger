package com.example.jcbledger;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.jcbledger.adapter.TransactionAdapter;
import com.example.jcbledger.adapter.DriverExpenseAdapter;
import com.example.jcbledger.databinding.ActivityTransactionHistoryBinding;
import com.example.jcbledger.model.DriverExpense;
import com.example.jcbledger.network.ApiService;
import com.example.jcbledger.network.RetrofitClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionHistoryActivity extends AppCompatActivity {

    private ActivityTransactionHistoryBinding binding;
    private ApiService apiService;
    private List<Map<String, Object>> dataList = new ArrayList<>();
    private List<DriverExpense> expenseList = new ArrayList<>();
    private String machineNumber;
    private boolean isOperatorHistory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("JCBPrefs", MODE_PRIVATE);
        machineNumber = prefs.getString("machineNumber", "");

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        apiService = RetrofitClient.getClient().create(ApiService.class);
        
        isOperatorHistory = getIntent().getBooleanExtra("isOperatorHistory", false);
        
        if (isOperatorHistory) {
            String driverId = getIntent().getStringExtra("driverId");
            String driverName = getIntent().getStringExtra("driverName");
            getSupportActionBar().setTitle(driverName + " - Expenses");
            setupDriverAdapter();
            fetchDriverExpenses(driverId);
        } else {
            String mobile = getIntent().getStringExtra("mobile");
            getSupportActionBar().setTitle("Transaction History");
            setupTransactionAdapter();
            if (mobile != null) {
                fetchTransactions(mobile);
            }
        }
    }

    private void setupTransactionAdapter() {
        TransactionAdapter adapter = new TransactionAdapter(dataList);
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTransactions.setAdapter(adapter);
    }

    private void setupDriverAdapter() {
        DriverExpenseAdapter adapter = new DriverExpenseAdapter(expenseList);
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTransactions.setAdapter(adapter);
    }

    private void fetchTransactions(String mobile) {
        apiService.getCustomerTransactions(mobile, machineNumber).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dataList.clear();
                    List<Map<String, Object>> list = response.body();
                    // Sort: Latest first (Descending order)
                    Collections.sort(list, (o1, o2) -> {
                        Object d1 = o1.get("transactionDate");
                        Object d2 = o2.get("transactionDate");
                        if (d1 == null) return 1;
                        if (d2 == null) return -1;
                        // Assuming transactionDate is in ISO-8601 format string
                        return String.valueOf(d2).compareTo(String.valueOf(d1));
                    });
                    dataList.addAll(list);
                    if (binding.rvTransactions.getAdapter() != null) {
                        binding.rvTransactions.getAdapter().notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(TransactionHistoryActivity.this, "Failed to fetch history", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchDriverExpenses(String driverId) {
        apiService.getDriverExpenses(driverId).enqueue(new Callback<List<DriverExpense>>() {
            @Override
            public void onResponse(Call<List<DriverExpense>> call, Response<List<DriverExpense>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    expenseList.clear();
                    List<DriverExpense> list = response.body();
                    // Sort: Latest first (Descending order)
                    Collections.sort(list, (o1, o2) -> {
                        String d1 = o1.getDate();
                        String d2 = o2.getDate();
                        if (d1 == null) return 1;
                        if (d2 == null) return -1;
                        
                        // Primary sort: Date
                        int dateCmp = d2.compareTo(d1);
                        if (dateCmp != 0) return dateCmp;
                        
                        // Secondary sort: ID (if available) as tie-breaker for same day entries
                        if (o1.getId() != null && o2.getId() != null) {
                            return o2.getId().compareTo(o1.getId());
                        }
                        return 0;
                    });
                    expenseList.addAll(list);
                    if (binding.rvTransactions.getAdapter() != null) {
                        binding.rvTransactions.getAdapter().notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<DriverExpense>> call, Throwable t) {
                Toast.makeText(TransactionHistoryActivity.this, "Failed to fetch history", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
