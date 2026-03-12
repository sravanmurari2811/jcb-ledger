package com.example.jcbledger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.jcbledger.adapter.WorkEntryAdapter;
import com.example.jcbledger.databinding.ActivityPendingBillsBinding;
import com.example.jcbledger.model.WorkEntry;
import com.example.jcbledger.network.ApiService;
import com.example.jcbledger.network.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingBillsActivity extends AppCompatActivity {

    private ActivityPendingBillsBinding binding;
    private ApiService apiService;
    private WorkEntryAdapter adapter;
    private List<WorkEntry> pendingBills = new ArrayList<>();
    private String machineNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPendingBillsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("JCBPrefs", MODE_PRIVATE);
        machineNumber = prefs.getString("machineNumber", "");

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        apiService = RetrofitClient.getClient().create(ApiService.class);

        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchPendingBills();
    }

    private void setupRecyclerView() {
        adapter = new WorkEntryAdapter(pendingBills, entry -> {
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
        binding.rvPendingBills.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPendingBills.setAdapter(adapter);
    }

    private void fetchPendingBills() {
        apiService.getPendingBills(machineNumber).enqueue(new Callback<List<WorkEntry>>() {
            @Override
            public void onResponse(Call<List<WorkEntry>> call, Response<List<WorkEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    pendingBills.clear();
                    pendingBills.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<WorkEntry>> call, Throwable t) {
                Toast.makeText(PendingBillsActivity.this, "Failed to fetch bills", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
