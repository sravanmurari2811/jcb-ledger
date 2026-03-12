package com.example.jcbledger;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jcbledger.databinding.ActivityAddDriverEntryBinding;
import com.example.jcbledger.model.DriverExpense;
import com.example.jcbledger.network.ApiService;
import com.example.jcbledger.network.RetrofitClient;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddDriverEntryActivity extends AppCompatActivity {

    private ActivityAddDriverEntryBinding binding;
    private String type; // SALARY or EXPENSE
    private String driverId;
    private String driverName;
    private String machineNumber;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddDriverEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        type = getIntent().getStringExtra("type");
        driverId = getIntent().getStringExtra("driverId");
        driverName = getIntent().getStringExtra("driverName");
        
        SharedPreferences prefs = getSharedPreferences("JCBPrefs", MODE_PRIVATE);
        machineNumber = prefs.getString("machineNumber", "");

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(type.equals("SALARY") ? "Add Salary" : "Add Expense");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.tvEntryTypeTitle.setText("New " + (type.equals("SALARY") ? "Salary" : "Expense") + " for " + driverName);
        binding.etDate.setText(dateFormat.format(calendar.getTime()));
        binding.etDate.setOnClickListener(v -> showDatePicker());

        binding.btnSubmit.setOnClickListener(v -> saveEntry());
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            binding.etDate.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveEntry() {
        String amountStr = binding.etAmount.getText().toString();
        if (amountStr.isEmpty()) {
            binding.etAmount.setError("Required");
            return;
        }

        DriverExpense expense = new DriverExpense();
        expense.setOperatorId(driverId);
        expense.setOperatorName(driverName);
        expense.setVehicleNumber(machineNumber);
        expense.setDate(binding.etDate.getText().toString());
        expense.setAmount(Double.parseDouble(amountStr));
        expense.setType(type);
        expense.setNote(binding.etNote.getText().toString());

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.addDriverExpense(expense).enqueue(new Callback<DriverExpense>() {
            @Override
            public void onResponse(Call<DriverExpense> call, Response<DriverExpense> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddDriverEntryActivity.this, "Entry Saved Successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(AddDriverEntryActivity.this, "Failed to save", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DriverExpense> call, Throwable t) {
                Toast.makeText(AddDriverEntryActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
