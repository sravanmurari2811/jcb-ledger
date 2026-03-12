package com.example.jcbledger;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jcbledger.databinding.ActivityWorkEntryDetailsBinding;
import com.example.jcbledger.model.WorkEntry;
import com.example.jcbledger.network.ApiService;
import com.example.jcbledger.network.RetrofitClient;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkEntryDetailsActivity extends AppCompatActivity {

    private ActivityWorkEntryDetailsBinding binding;
    private ApiService apiService;
    private long entryId;
    private double currentPending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWorkEntryDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getClient().create(ApiService.class);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        entryId = getIntent().getLongExtra("id", -1L);
        displayDetails();

        binding.btnUpdatePayment.setOnClickListener(v -> showUpdatePaymentDialog());
    }

    private void displayDetails() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) return;

        String workType = extras.getString("workType", "EARTH_WORK");
        currentPending = extras.getDouble("pending");
        
        binding.tvDetCustomerName.setText(extras.getString("customerName"));
        binding.tvDetMobile.setText(extras.getString("mobile"));
        binding.tvDetStatus.setText(extras.getString("status", "").toUpperCase());

        if (currentPending <= 0) {
            binding.btnUpdatePayment.setVisibility(View.GONE);
        }

        setRow(binding.rowWorkType.getRoot(), "Work Type", workType.replace("_", " "));
        setRow(binding.rowDate.getRoot(), "Work Date", extras.getString("date"));
        setRow(binding.rowPlace.getRoot(), "Place", extras.getString("place"));

        if ("EARTH_WORK".equals(workType)) {
            binding.rowTime.getRoot().setVisibility(View.VISIBLE);
            binding.rowDuration.getRoot().setVisibility(View.VISIBLE);
            binding.rowRate.getRoot().setVisibility(View.VISIBLE);
            
            binding.rowTrips.getRoot().setVisibility(View.GONE);
            binding.rowChargePerTrip.getRoot().setVisibility(View.GONE);
            binding.rowTractor.getRoot().setVisibility(View.GONE);

            setRow(binding.rowTime.getRoot(), "Work Time", extras.getString("startTime") + " - " + extras.getString("endTime"));
            
            double rate = extras.getDouble("rate");
            double total = extras.getDouble("total");
            double travel = extras.getDouble("travel");
            if (rate > 0) {
                double duration = (total - travel) / rate;
                setRow(binding.rowDuration.getRoot(), "Total Duration", String.format(Locale.getDefault(), "%.2f hrs", duration));
            }
            setRow(binding.rowRate.getRoot(), "Rate", String.format(Locale.getDefault(), "₹ %.2f / hr", rate));
            
        } else {
            binding.rowTime.getRoot().setVisibility(View.GONE);
            binding.rowDuration.getRoot().setVisibility(View.GONE);
            binding.rowRate.getRoot().setVisibility(View.GONE);
            
            binding.rowTrips.getRoot().setVisibility(View.VISIBLE);
            binding.rowChargePerTrip.getRoot().setVisibility(View.VISIBLE);
            binding.rowTractor.getRoot().setVisibility(View.VISIBLE);

            setRow(binding.rowTrips.getRoot(), "Total Trips", String.valueOf(extras.getInt("trips")));
            setRow(binding.rowChargePerTrip.getRoot(), "Charge / Trip", String.format(Locale.getDefault(), "₹ %.2f", extras.getDouble("chargePerTrip")));
            setRow(binding.rowTractor.getRoot(), "Tractor(s)", extras.getString("tractorNumber"));
        }

        setRow(binding.rowTravel.getRoot(), "Travel Cost", String.format(Locale.getDefault(), "₹ %.2f", extras.getDouble("travel")));
        setRow(binding.rowTotal.getRoot(), "Total Amount", String.format(Locale.getDefault(), "₹ %.2f", extras.getDouble("total")));
        setRow(binding.rowPaid.getRoot(), "Amount Paid", String.format(Locale.getDefault(), "₹ %.2f", extras.getDouble("paid")));
        setRow(binding.rowPending.getRoot(), "Pending Amount", String.format(Locale.getDefault(), "₹ %.2f", currentPending));
        setRow(binding.rowMethod.getRoot(), "Payment Method", extras.getString("method"));
    }

    private void setRow(android.view.View view, String label, String value) {
        TextView tvLabel = view.findViewById(R.id.tvLabel);
        TextView tvValue = view.findViewById(R.id.tvValue);
        tvLabel.setText(label);
        tvValue.setText(value);
    }

    private void showUpdatePaymentDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_payment, null);
        TextView tvPending = dialogView.findViewById(R.id.tvRemainingPending);
        EditText etAmount = dialogView.findViewById(R.id.etUpdateAmount);
        RadioGroup rgMethod = dialogView.findViewById(R.id.rgUpdatePaymentMethod);

        tvPending.setText(String.format(Locale.getDefault(), "Pending: ₹ %.2f", currentPending));
        etAmount.setText(String.valueOf(currentPending));

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Receive", (dialog, which) -> {
                    String amountStr = etAmount.getText().toString();
                    if (amountStr.isEmpty()) return;
                    double amount = Double.parseDouble(amountStr);
                    String method = rgMethod.getCheckedRadioButtonId() == R.id.rbUpdateCash ? "Cash" : "UPI";
                    performUpdatePayment(amount, method);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performUpdatePayment(double amount, String method) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", amount);
        payload.put("paymentMethod", method);

        apiService.updatePayment(entryId, payload).enqueue(new Callback<WorkEntry>() {
            @Override
            public void onResponse(Call<WorkEntry> call, Response<WorkEntry> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(WorkEntryDetailsActivity.this, "Payment Updated", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to refresh list
                }
            }
            @Override
            public void onFailure(Call<WorkEntry> call, Throwable t) {
                Toast.makeText(WorkEntryDetailsActivity.this, "Update Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
