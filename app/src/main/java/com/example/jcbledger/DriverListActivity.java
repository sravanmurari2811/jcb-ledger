package com.example.jcbledger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.jcbledger.adapter.DriverAdapter;
import com.example.jcbledger.databinding.ActivityDriverListBinding;
import com.example.jcbledger.network.ApiService;
import com.example.jcbledger.network.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverListActivity extends AppCompatActivity {

    private ActivityDriverListBinding binding;
    private ApiService apiService;
    private String machineNumber;
    private List<Map<String, Object>> drivers = new ArrayList<>();
    private DriverAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("JCBPrefs", MODE_PRIVATE);
        machineNumber = prefs.getString("machineNumber", "");

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        apiService = RetrofitClient.getClient().create(ApiService.class);

        setupRecyclerView();
        fetchDrivers();
    }

    private void setupRecyclerView() {
        adapter = new DriverAdapter(drivers, driver -> {
            Intent intent = new Intent(this, DriverExpenseActivity.class);
            // Safely convert ID to String to avoid casting issues
            Object idObj = driver.get("id");
            String idStr = (idObj != null) ? String.valueOf(idObj) : "";
            
            // If it's a double from Gson (e.g. 1.0), remove the .0
            if (idStr.endsWith(".0")) {
                idStr = idStr.substring(0, idStr.length() - 2);
            }

            intent.putExtra("driverId", idStr);
            intent.putExtra("driverName", (String) driver.get("name"));
            intent.putExtra("driverPhone", (String) driver.get("phone"));
            startActivity(intent);
        });
        binding.rvDrivers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDrivers.setAdapter(adapter);
    }

    private void fetchDrivers() {
        apiService.getOperators(machineNumber).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    drivers.clear();
                    drivers.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    if (drivers.isEmpty()) {
                        Toast.makeText(DriverListActivity.this, "No drivers found for this machine", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(DriverListActivity.this, "Error fetching drivers", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
