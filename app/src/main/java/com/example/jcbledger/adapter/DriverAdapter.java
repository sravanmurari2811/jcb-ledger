package com.example.jcbledger.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jcbledger.R;
import java.util.List;
import java.util.Map;

public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.ViewHolder> {

    private List<Map<String, Object>> drivers;
    private OnDriverClickListener listener;

    public interface OnDriverClickListener {
        void onDriverClick(Map<String, Object> driver);
    }

    public DriverAdapter(List<Map<String, Object>> drivers, OnDriverClickListener listener) {
        this.drivers = drivers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_driver, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> driver = drivers.get(position);
        holder.tvName.setText((String) driver.get("name"));
        holder.tvPhone.setText((String) driver.get("phone"));
        holder.itemView.setOnClickListener(v -> listener.onDriverClick(driver));
    }

    @Override
    public int getItemCount() {
        return drivers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDriverName);
            tvPhone = itemView.findViewById(R.id.tvDriverPhone);
        }
    }
}
