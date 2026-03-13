package com.example.jcbledger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jcbledger.network.ApiService;
import com.example.jcbledger.network.RetrofitClient;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Wake up the Render backend using the new lightweight health check
        pingServer();

        SharedPreferences prefs = getSharedPreferences("JCBPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        long lastLoginTime = prefs.getLong("lastLoginTime", 0);
        long currentTime = System.currentTimeMillis();
        
        // 12 hours session validity
        long twelveHoursInMillis = 12 * 60 * 60 * 1000;

        if (isLoggedIn && (currentTime - lastLoginTime < twelveHoursInMillis)) {
            startActivity(new Intent(this, DashboardActivity.class));
        } else {
            prefs.edit().putBoolean("isLoggedIn", false).apply();
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }

    private void pingServer() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        // Use the dedicated lightweight ping endpoint
        apiService.ping().enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {}
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {}
        });
    }
}
