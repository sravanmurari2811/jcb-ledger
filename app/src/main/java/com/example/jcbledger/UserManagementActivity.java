package com.example.jcbledger;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.jcbledger.adapter.UserManagementAdapter;
import com.example.jcbledger.databinding.ActivityUserManagementBinding;
import com.example.jcbledger.model.User;
import com.example.jcbledger.network.ApiService;
import com.example.jcbledger.network.RetrofitClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserManagementActivity extends AppCompatActivity {

    private ActivityUserManagementBinding binding;
    private ApiService apiService;
    private UserManagementAdapter adapter;
    private final List<User> userList = new ArrayList<>();
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("JCBPrefs", MODE_PRIVATE);
        userRole = prefs.getString("userRole", "OPERATOR");

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        apiService = RetrofitClient.getClient().create(ApiService.class);

        setupRecyclerView();
        fetchAllUsers();
    }

    private void setupRecyclerView() {
        adapter = new UserManagementAdapter(userList, this::showStatusUpdateDialog);
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUsers.setAdapter(adapter);
    }

    private void fetchAllUsers() {
        binding.progressBar.setVisibility(View.VISIBLE);
        apiService.getAllUsers(userRole).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    userList.clear();
                    userList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(UserManagementActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(UserManagementActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showStatusUpdateDialog(User user) {
        String[] statuses = {"ACTIVE", "PENDING", "BLOCKED"};
        int currentSelection = 0;
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(user.getStatus())) {
                currentSelection = i;
                break;
            }
        }

        final int[] selectedIndex = {currentSelection};

        new AlertDialog.Builder(this)
                .setTitle("Update Status for " + user.getName())
                .setSingleChoiceItems(statuses, currentSelection, (dialog, which) -> selectedIndex[0] = which)
                .setPositiveButton("Update", (dialog, which) -> updateUserStatus(user.getId(), statuses[selectedIndex[0]]))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateUserStatus(Long userId, String newStatus) {
        binding.progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("status", newStatus);

        apiService.updateUserStatus(payload).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(UserManagementActivity.this, "Status updated successfully", Toast.LENGTH_SHORT).show();
                    fetchAllUsers(); // Refresh list
                } else {
                    Toast.makeText(UserManagementActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(UserManagementActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
