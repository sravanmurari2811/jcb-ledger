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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private double rawTotalAmount = 0;
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
        binding.etStartTime.setText("");
        binding.etEndTime.setText("");
        binding.tvTotalTimeDisplay.setText("Total Duration: 0 hrs 0 mins");
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
        TimePickerDialog tpd = new TimePickerDialog(this, 
            android.R.style.Theme_Holo_Light_Dialog_NoActionBar, 
            (view, hourOfDay, minute) -> {
                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c.set(Calendar.MINUTE, minute);
                c.set(Calendar.SECOND, 0);
                if (isStart) binding.etStartTime.setText(timeFormat.format(c.getTime()));
                else binding.etEndTime.setText(timeFormat.format(c.getTime()));
                updateCalculatedTime();
                calculateTotals();
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
        tpd.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        tpd.show();
    }

    private void updateCalculatedTime() {
        String startStr = binding.etStartTime.getText().toString();
        String endStr = binding.etEndTime.getText().toString();
        if (!startStr.isEmpty() && !endStr.isEmpty()) {
            if (startStr.equals(endStr)) {
                binding.etEndTime.setError("Start and End time cannot be the same");
                calculatedTotalHours = 0;
                binding.tvTotalTimeDisplay.setText("Total Time: 0.00 hrs");
                return;
            } else {
                binding.etEndTime.setError(null);
            }
            
            long diff = endTimeCalendar.getTimeInMillis() - startTimeCalendar.getTimeInMillis();
            if (diff < 0) diff += 24 * 60 * 60 * 1000;
            
            long totalMinutes = diff / (1000 * 60);
            calculatedTotalHours = totalMinutes / 60.0;
            
            long h = totalMinutes / 60;
            long m = totalMinutes % 60;
            binding.tvTotalTimeDisplay.setText(String.format(Locale.getDefault(), "Total Duration: %d hrs %d mins", h, m));
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

            rawTotalAmount = total;
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
        if (val == null || val.isEmpty()) return 0;
        try { return Double.parseDouble(val); } catch (Exception e) { return 0; }
    }

    private void saveEntry() {
        if (!validateForm()) return;

        binding.btnSave.setEnabled(false);
        binding.btnSave.setText("Saving...");

        WorkEntryRequest request = new WorkEntryRequest();
        request.setCustomerMobile(binding.etMobile.getText().toString());
        request.setCustomerName(binding.etName.getText().toString());
        request.setWorkDate(binding.etDate.getText().toString());
        request.setPlace(binding.etPlace.getText().toString());
        request.setTravelCost(getDoubleValue(binding.etTravel.getText().toString()));
        request.setTotalAmount(rawTotalAmount);
        double amountPaid = getDoubleValue(binding.etAmountPaid.getText().toString());
        request.setAmountPaid(amountPaid);
        request.setPendingAmount(rawTotalAmount - amountPaid);
        request.setPaymentMethod(binding.rbCash.isChecked() ? "Cash" : "UPI");

        if (binding.rbEarthWork.isChecked()) {
            request.setWorkType("EARTH_WORK");
            request.setStartTime(binding.etStartTime.getText().toString());
            request.setEndTime(binding.etEndTime.getText().toString());
            request.setTotalHours(calculatedTotalHours);
            request.setRate(getDoubleValue(binding.etRate.getText().toString()));
        } else {
            request.setWorkType("LOADING");
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
                    String errorMsg = "Failed to save entry";
                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            Map<String, String> errorMap = new Gson().fromJson(errorJson, new TypeToken<Map<String, String>>(){}.getType());
                            if (errorMap != null && errorMap.containsKey("message")) {
                                errorMsg = errorMap.get("message");
                            }
                        }
                    } catch (Exception e) {}
                    Toast.makeText(WorkEntryActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    binding.btnSave.setEnabled(true);
                    binding.btnSave.setText("SAVE WORK ENTRY");
                }
            }
            @Override
            public void onFailure(Call<WorkEntry> call, Throwable t) {
                Toast.makeText(WorkEntryActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                binding.btnSave.setEnabled(true);
                binding.btnSave.setText("SAVE WORK ENTRY");
            }
        });
    }

    private boolean validateForm() {
        if (binding.etMobile.getText().toString().length() != 10) {
            Toast.makeText(this, "Enter valid 10-digit mobile number", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.etName.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Customer name is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.etPlace.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Location/Place is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (binding.rbEarthWork.isChecked()) {
            if (binding.etStartTime.getText().toString().isEmpty() || binding.etEndTime.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please select Start and End time", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (calculatedTotalHours <= 0) {
                Toast.makeText(this, "Work duration must be greater than zero", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (getDoubleValue(binding.etRate.getText().toString()) <= 0) {
                Toast.makeText(this, "Please enter hourly rate", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            for (ItemTractorRowBinding row : tractorRows) {
                if (row.etTractorName.getText().toString().trim().isEmpty() || getDoubleValue(row.etTractorTrips.getText().toString()) <= 0) {
                    Toast.makeText(this, "Enter valid tractor details and trips", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            if (getDoubleValue(binding.etChargePerTrip.getText().toString()) <= 0) {
                Toast.makeText(this, "Please enter charge per trip", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (getDoubleValue(binding.etAmountPaid.getText().toString()) > rawTotalAmount) {
            Toast.makeText(this, "Paid amount cannot exceed total amount", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
