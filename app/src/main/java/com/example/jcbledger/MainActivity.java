package com.example.jcbledger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("JCBPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        long lastLoginTime = prefs.getLong("lastLoginTime", 0);
        long currentTime = System.currentTimeMillis();
        
        // 12 hours in milliseconds: 12 * 60 * 60 * 1000 = 43,200,000
        long twelveHoursInMillis = 12 * 60 * 60 * 1000;

        if (isLoggedIn && (currentTime - lastLoginTime < twelveHoursInMillis)) {
            // Keep user logged in and go to Dashboard
            startActivity(new Intent(this, DashboardActivity.class));
        } else {
            // Either not logged in or session expired (over 12 hours)
            prefs.edit().putBoolean("isLoggedIn", false).apply();
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}
