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

        // Wake up the Render backend as soon as the app starts
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
        // Silent call to wake up Render
        apiService.getTotalPending("WAKEUP").enqueue(new Callback<Map<String, Double>>() {
            @Override
            public void onResponse(Call<Map<String, Double>> call, Response<Map<String, Double>> response) {}
            @Override
            public void onFailure(Call<Map<String, Double>> call, Throwable t) {}
        });
    }
}
