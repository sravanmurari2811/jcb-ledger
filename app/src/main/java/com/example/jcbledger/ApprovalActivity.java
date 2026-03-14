package com.example.jcbledger;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.jcbledger.adapter.ApprovalAdapter;
import com.example.jcbledger.databinding.ActivityApprovalBinding;
import com.example.jcbledger.model.User;
import com.example.jcbledger.network.ApiService;
import com.example.jcbledger.network.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApprovalActivity extends AppCompatActivity {

    private ActivityApprovalBinding binding;
    private ApiService apiService;
    private ApprovalAdapter adapter;
    private final List<User> pendingUsers = new ArrayList<>();
    private String userRole;
    private String machineNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityApprovalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("JCBPrefs", MODE_PRIVATE);
        userRole = prefs.getString("userRole", "OPERATOR");
        machineNumber = prefs.getString("machineNumber", "");

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        apiService = RetrofitClient.getClient().create(ApiService.class);

        setupRecyclerView();
        fetchPendingApprovals();
    }

    private void setupRecyclerView() {
        adapter = new ApprovalAdapter(pendingUsers, this::approveUser);
        binding.rvApprovals.setLayoutManager(new LinearLayoutManager(this));
        binding.rvApprovals.setAdapter(adapter);
    }

    private void fetchPendingApprovals() {
        binding.progressBar.setVisibility(View.VISIBLE);
        apiService.getPendingApprovals(userRole, machineNumber).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    pendingUsers.clear();
                    pendingUsers.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    if (pendingUsers.isEmpty()) {
                        Toast.makeText(ApprovalActivity.this, "No pending requests found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ApprovalActivity.this, "Failed to fetch requests", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ApprovalActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void approveUser(User user) {
        binding.progressBar.setVisibility(View.VISIBLE);
        apiService.approveUser(user.getId()).enqueue(new Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(Call<java.util.Map<String, Object>> call, Response<java.util.Map<String, Object>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(ApprovalActivity.this, "User approved successfully", Toast.LENGTH_SHORT).show();
                    fetchPendingApprovals(); // Refresh list
                } else {
                    Toast.makeText(ApprovalActivity.this, "Approval failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<java.util.Map<String, Object>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ApprovalActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
