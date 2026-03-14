package com.example.jcbledger;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jcbledger.databinding.ActivityRegisterBinding;
import com.example.jcbledger.network.ApiService;
import com.example.jcbledger.network.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getClient().create(ApiService.class);

        binding.etRegVehicle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (!input.equals(input.toUpperCase())) {
                    binding.etRegVehicle.setText(input.toUpperCase());
                    binding.etRegVehicle.setSelection(binding.etRegVehicle.length());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnSubmitReg.setOnClickListener(v -> {
            String name = binding.etRegName.getText().toString().trim();
            String phone = binding.etRegPhone.getText().toString().trim();
            String password = binding.etRegPassword.getText().toString().trim();
            String vehicle = binding.etRegVehicle.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty() || password.isEmpty() || vehicle.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String role = binding.rgRole.getCheckedRadioButtonId() == R.id.rbOwner ? "OWNER" : "OPERATOR";

            Map<String, Object> body = new HashMap<>();
            body.put("name", name);
            body.put("phone", phone);
            body.put("password", password);
            body.put("vehicleNumber", vehicle);
            body.put("role", role);

            apiService.register(body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        String successMsg = role.equals("OWNER") ? 
                            "Request sent to Admin for approval." : 
                            "Request sent to Owner for approval.";
                        Toast.makeText(RegisterActivity.this, successMsg, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String errorMessage = "Registration failed";
                        try {
                            if (response.errorBody() != null) {
                                String errorJson = response.errorBody().string();
                                Map<String, String> errorData = new Gson().fromJson(errorJson, new TypeToken<Map<String, String>>(){}.getType());
                                if (errorData != null && errorData.containsKey("message")) {
                                    errorMessage = errorData.get("message");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Toast.makeText(RegisterActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.btnBackLogin.setOnClickListener(v -> finish());
    }
}
