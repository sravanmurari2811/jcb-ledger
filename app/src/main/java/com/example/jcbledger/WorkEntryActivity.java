package com.example.jcbledger;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jcbledger.databinding.ActivityWorkEntryBinding;
import com.example.jcbledger.databinding.ItemTractorRowBinding;
import com.example.jcbledger.dto.WorkEntryRequest;
import com.example.jcbledger.model.Customer;
import com.example.jcbledger.model.WorkEntry;
import com.example.jcbledger.network.ApiService;
import com.example.jcbledger.network.RetrofitClient;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkEntryActivity extends AppCompatActivity {

    private ActivityWorkEntryBinding binding;
    private ApiService apiService;
    private Calendar workCalendar = Calendar.getInstance();
    private Calendar startTimeCalendar = Calendar.getInstance();
    private Calendar endTimeCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    
    private List<ItemTractorRowBinding> tractorRows = new ArrayList<>();
    private double calculatedTotalHours = 0;
    private String machineNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWorkEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("JCBPrefs", MODE_PRIVATE);
        machineNumber = prefs.getString("machineNumber", "");

        apiService = RetrofitClient.getClient().create(ApiService.class);

        initViews();
        setupListeners();
        addTractorRow();
    }

    private void initViews() {
        binding.etDate.setText(dateFormat.format(workCalendar.getTime()));
    }

    private void setupListeners() {
        binding.rgWorkType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbEarthWork) {
                binding.layoutEarthWork.setVisibility(View.VISIBLE);
                binding.layoutLoading.setVisibility(View.GONE);
            } else {
                binding.layoutEarthWork.setVisibility(View.GONE);
                binding.layoutLoading.setVisibility(View.VISIBLE);
            }
            calculateTotals();
        });

        binding.etDate.setOnClickListener(v -> showDatePicker());
        binding.etStartTime.setOnClickListener(v -> showTimePicker(true));
        binding.etEndTime.setOnClickListener(v -> showTimePicker(false));

        binding.btnAddNewTractor.setOnClickListener(v -> addTractorRow());

        binding.etMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 10) {
                    binding.etMobile.setError(null);
                    fetchCustomer(s.toString());
                } else if (s.length() > 0 && s.length() < 10) {
                    binding.etMobile.setError("Mobile must be 10 digits");
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        TextWatcher calculationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotals();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.etRate.addTextChangedListener(calculationWatcher);
        binding.etTravel.addTextChangedListener(calculationWatcher);
        binding.etAmountPaid.addTextChangedListener(calculationWatcher);
        binding.etChargePerTrip.addTextChangedListener(calculationWatcher);

        binding.btnSave.setOnClickListener(v -> saveEntry());
    }

    private void addTractorRow() {
        ItemTractorRowBinding rowBinding = ItemTractorRowBinding.inflate(LayoutInflater.from(this), binding.containerTractors, false);
        binding.containerTractors.addView(rowBinding.getRoot());
        tractorRows.add(rowBinding);

        rowBinding.etTractorTrips.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotals();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        rowBinding.btnRemoveTractor.setOnClickListener(v -> {
            if (tractorRows.size() > 1) {
                binding.containerTractors.removeView(rowBinding.getRoot());
                tractorRows.remove(rowBinding);
                calculateTotals();
            } else {
                Toast.makeText(this, "At least one tractor is required", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            workCalendar.set(Calendar.YEAR, year);
            workCalendar.set(Calendar.MONTH, month);
            workCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            binding.etDate.setText(dateFormat.format(workCalendar.getTime()));
        }, workCalendar.get(Calendar.YEAR), workCalendar.get(Calendar.MONTH), workCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(boolean isStart) {
        Calendar c = isStart ? startTimeCalendar : endTimeCalendar;
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);
            c.set(Calendar.SECOND, 0);
            
            if (isStart) {
                binding.etStartTime.setText(timeFormat.format(c.getTime()));
            } else {
                binding.etEndTime.setText(timeFormat.format(c.getTime()));
            }
            
            updateCalculatedTime();
            calculateTotals();
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void updateCalculatedTime() {
        String startStr = binding.etStartTime.getText().toString();
        String endStr = binding.etEndTime.getText().toString();
        if (!startStr.isEmpty() && !endStr.isEmpty()) {
            if (startStr.equals(endStr)) {
                binding.etEndTime.setError("Start and End time cannot be the same");
                calculatedTotalHours = 0;
                binding.tvTotalTimeDisplay.setText("Total Time: 0 hours 0 minutes");
                return;
            } else {
                binding.etEndTime.setError(null);
            }
            
            long diff = endTimeCalendar.getTimeInMillis() - startTimeCalendar.getTimeInMillis();
            if (diff < 0) diff += 24 * 60 * 60 * 1000;
            
            long totalMinutes = diff / (1000 * 60);
            long h = totalMinutes / 60;
            long m = totalMinutes % 60;
            
            calculatedTotalHours = h + (m / 60.0);
            binding.tvTotalTimeDisplay.setText("Total Time: " + h + " hours " + m + " minutes");
        } else {
            calculatedTotalHours = 0;
            binding.tvTotalTimeDisplay.setText("Total Time: 0 hours 0 minutes");
        }
    }

    private void fetchCustomer(String mobile) {
        apiService.getCustomerByMobile(mobile).enqueue(new Callback<Customer>() {
            @Override
            public void onResponse(Call<Customer> call, Response<Customer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    binding.etName.setText(response.body().getName());
                    binding.etName.setEnabled(false);
                }
            }
            @Override
            public void onFailure(Call<Customer> call, Throwable t) {}
        });
    }

    private void calculateTotals() {
        try {
            double total = 0;
            double travel = getDoubleValue(binding.etTravel.getText().toString());

            if (binding.rbEarthWork.isChecked()) {
                double rate = getDoubleValue(binding.etRate.getText().toString());
                total = (calculatedTotalHours * rate) + travel;
            } else {
                int totalTrips = 0;
                for (ItemTractorRowBinding row : tractorRows) {
                    totalTrips += (int) getDoubleValue(row.etTractorTrips.getText().toString());
                }
                binding.tvTotalTripsLabel.setText("Total Trips: " + totalTrips);
                double charge = getDoubleValue(binding.etChargePerTrip.getText().toString());
                total = (totalTrips * charge) + travel;
            }

            double amountPaid = getDoubleValue(binding.etAmountPaid.getText().toString());
            double pending = total - amountPaid;

            binding.tvTotal.setText(String.format(Locale.getDefault(), "₹ %.2f", total));
            binding.tvPending.setText(String.format(Locale.getDefault(), "₹ %.2f", pending));
            
            if (pending < 0) {
                binding.etAmountPaid.setError("Amount paid exceeds total");
            } else {
                binding.etAmountPaid.setError(null);
            }
        } catch (Exception e) {}
    }

    private double getDoubleValue(String val) {
        if (val == null || val.isEmpty() || val.equals("0")) return 0;
        try { return Double.parseDouble(val); } catch (Exception e) { return 0; }
    }

    private void saveEntry() {
        if (!validateForm()) return;

        String mobile = binding.etMobile.getText().toString();
        String name = binding.etName.getText().toString();
        String date = binding.etDate.getText().toString();
        double travel = getDoubleValue(binding.etTravel.getText().toString());
        double total = getDoubleValue(binding.tvTotal.getText().toString().replace("₹ ", "").replace(",", ""));
        double amountPaid = getDoubleValue(binding.etAmountPaid.getText().toString());
        double pending = getDoubleValue(binding.tvPending.getText().toString().replace("₹ ", "").replace(",", ""));
        String place = binding.etPlace.getText().toString();
        String paymentMethod = binding.rbCash.isChecked() ? "Cash" : "UPI";
        String workType = binding.rbEarthWork.isChecked() ? "EARTH_WORK" : "LOADING";

        WorkEntryRequest request = new WorkEntryRequest();
        request.setCustomerMobile(mobile);
        request.setCustomerName(name);
        request.setWorkDate(date);
        request.setWorkType(workType);
        request.setTravelCost(travel);
        request.setTotalAmount(total);
        request.setAmountPaid(amountPaid);
        request.setPendingAmount(pending);
        request.setPaymentMethod(paymentMethod);
        request.setPlace(place);

        if (workType.equals("EARTH_WORK")) {
            request.setStartTime(binding.etStartTime.getText().toString());
            request.setEndTime(binding.etEndTime.getText().toString());
            request.setTotalHours(calculatedTotalHours);
            request.setRate(getDoubleValue(binding.etRate.getText().toString()));
        } else {
            int totalTrips = 0;
            StringBuilder tractors = new StringBuilder();
            for (int i = 0; i < tractorRows.size(); i++) {
                String tName = tractorRows.get(i).etTractorName.getText().toString().trim();
                int trips = (int) getDoubleValue(tractorRows.get(i).etTractorTrips.getText().toString());
                totalTrips += trips;
                tractors.append(tName).append(" (").append(trips).append(")");
                if (i < tractorRows.size() - 1) tractors.append(", ");
            }
            request.setTrips(totalTrips);
            request.setChargePerTrip(getDoubleValue(binding.etChargePerTrip.getText().toString()));
            request.setTractorNumber(tractors.toString());
        }

        apiService.createWorkEntry(request, machineNumber).enqueue(new Callback<WorkEntry>() {
            @Override
            public void onResponse(Call<WorkEntry> call, Response<WorkEntry> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(WorkEntryActivity.this, "Entry Saved Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(WorkEntryActivity.this, "Failed to save entry. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<WorkEntry> call, Throwable t) {
                Toast.makeText(WorkEntryActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (binding.etMobile.getText().toString().length() != 10) {
            binding.etMobile.setError("Enter 10-digit mobile number");
            isValid = false;
        }

        if (binding.etName.getText().toString().trim().isEmpty()) {
            binding.etName.setError("Customer name is required");
            isValid = false;
        }

        if (binding.rbEarthWork.isChecked()) {
            if (binding.etStartTime.getText().toString().isEmpty()) {
                binding.etStartTime.setError("Required");
                isValid = false;
            }
            if (binding.etEndTime.getText().toString().isEmpty()) {
                binding.etEndTime.setError("Required");
                isValid = false;
            }
            if (calculatedTotalHours <= 0 && !binding.etStartTime.getText().toString().isEmpty() && !binding.etEndTime.getText().toString().isEmpty()) {
                Toast.makeText(this, "Work duration must be greater than zero", Toast.LENGTH_SHORT).show();
                isValid = false;
            }
            if (getDoubleValue(binding.etRate.getText().toString()) <= 0) {
                binding.etRate.setError("Rate must be greater than zero");
                isValid = false;
            }
        } else {
            boolean tractorError = false;
            for (int i = 0; i < tractorRows.size(); i++) {
                ItemTractorRowBinding row = tractorRows.get(i);
                if (row.etTractorName.getText().toString().trim().isEmpty()) {
                    row.etTractorName.setError("Please enter tractor name");
                    isValid = false;
                    tractorError = true;
                }
                if (getDoubleValue(row.etTractorTrips.getText().toString()) <= 0) {
                    row.etTractorTrips.setError("Enter trips");
                    isValid = false;
                }
            }
            if (tractorError) {
                Toast.makeText(this, "Please enter tractor name", Toast.LENGTH_SHORT).show();
            }
            if (getDoubleValue(binding.etChargePerTrip.getText().toString()) <= 0) {
                binding.etChargePerTrip.setError("Charge must be greater than zero");
                isValid = false;
            }
        }

        double total = getDoubleValue(binding.tvTotal.getText().toString().replace("₹ ", "").replace(",", ""));
        double paid = getDoubleValue(binding.etAmountPaid.getText().toString());
        if (paid > total) {
            binding.etAmountPaid.setError("Cannot pay more than total amount");
            isValid = false;
        }

        return isValid;
    }
}
