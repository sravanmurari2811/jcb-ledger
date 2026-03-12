package com.example.jcbledger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.jcbledger.adapter.DriverExpenseAdapter;
import com.example.jcbledger.databinding.ActivityDriverExpenseBinding;
import com.example.jcbledger.model.DriverExpense;
import com.example.jcbledger.network.ApiService;
import com.example.jcbledger.network.RetrofitClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverExpenseActivity extends AppCompatActivity {

    private ActivityDriverExpenseBinding binding;
    private ApiService apiService;
    private String machineNumber;
    private String driverId;
    private String driverName;
    private List<DriverExpense> expenseHistory = new ArrayList<>();
    private List<DriverExpense> displayHistory = new ArrayList<>();
    private DriverExpenseAdapter adapter;

    private final ActivityResultLauncher<Intent> entryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    fetchExpenseHistory();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("JCBPrefs", MODE_PRIVATE);
        machineNumber = prefs.getString("machineNumber", "");

        driverId = getIntent().getStringExtra("driverId");
        driverName = getIntent().getStringExtra("driverName");

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.tvDriverNameHeader.setText("Driver :- " + driverName);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        setupRecyclerView();
        fetchExpenseHistory();
        
        binding.btnAddExpenseAction.setOnClickListener(v -> openAddEntry("EXPENSE"));
        binding.btnAddSalaryAction.setOnClickListener(v -> openAddEntry("SALARY"));

        binding.btnTransactionHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransactionHistoryActivity.class);
            intent.putExtra("driverId", driverId);
            intent.putExtra("driverName", driverName);
            intent.putExtra("isOperatorHistory", true);
            startActivity(intent);
        });

        binding.tvViewAllExpenses.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransactionHistoryActivity.class);
            intent.putExtra("driverId", driverId);
            intent.putExtra("driverName", driverName);
            intent.putExtra("isOperatorHistory", true);
            startActivity(intent);
        });
    }

    private void openAddEntry(String type) {
        Intent intent = new Intent(this, AddDriverEntryActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("driverId", driverId);
        intent.putExtra("driverName", driverName);
        entryLauncher.launch(intent);
    }

    private void setupRecyclerView() {
        adapter = new DriverExpenseAdapter(displayHistory);
        binding.rvDriverExpenses.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDriverExpenses.setAdapter(adapter);
    }

    private void fetchExpenseHistory() {
        apiService.getDriverExpenses(driverId).enqueue(new Callback<List<DriverExpense>>() {
            @Override
            public void onResponse(Call<List<DriverExpense>> call, Response<List<DriverExpense>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    expenseHistory.clear();
                    List<DriverExpense> list = response.body();
                    
                    // Robust sorting by date, then by ID as a tie-breaker
                    Collections.sort(list, (o1, o2) -> {
                        int dateCmp = o2.getDate().compareTo(o1.getDate());
                        if (dateCmp != 0) return dateCmp;
                        if (o1.getId() != null && o2.getId() != null) {
                            return o2.getId().compareTo(o1.getId());
                        }
                        return 0;
                    });

                    expenseHistory.addAll(list);
                    
                    displayHistory.clear();
                    for(int i=0; i < Math.min(5, expenseHistory.size()); i++){
                        displayHistory.add(expenseHistory.get(i));
                    }
                    
                    adapter.notifyDataSetChanged();
                    calculateNetBalance();
                }
            }
            @Override
            public void onFailure(Call<List<DriverExpense>> call, Throwable t) {
                Toast.makeText(DriverExpenseActivity.this, "Error fetching history", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateNetBalance() {
        double net = 0;
        for (DriverExpense exp : expenseHistory) {
            if ("SALARY".equals(exp.getType())) {
                net -= exp.getAmount();
            } else {
                net += exp.getAmount();
            }
        }
        binding.tvDriverTotalAmount.setText(String.format(Locale.getDefault(), "₹ %.2f", net));
    }
}
