package com.example.jcbledger.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jcbledger.R;
import com.example.jcbledger.model.WorkEntry;
import java.util.List;
import java.util.Locale;

public class WorkEntryAdapter extends RecyclerView.Adapter<WorkEntryAdapter.ViewHolder> {

    private List<WorkEntry> workEntries;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(WorkEntry workEntry);
    }

    public WorkEntryAdapter(List<WorkEntry> workEntries, OnItemClickListener listener) {
        this.workEntries = workEntries;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_work_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkEntry entry = workEntries.get(position);
        
        String name = "Unknown";
        String mobile = "";
        if (entry.getCustomer() != null) {
            if (entry.getCustomer().getName() != null) name = entry.getCustomer().getName();
            if (entry.getCustomer().getMobile() != null) mobile = entry.getCustomer().getMobile();
        }
        
        holder.tvCustomerName.setText(name);
        holder.tvMobile.setText(mobile);
        holder.tvDate.setText(entry.getWorkDate() != null ? entry.getWorkDate() : "");
        holder.tvPlace.setText(entry.getPlace() != null ? entry.getPlace() : "");
        holder.tvPendingAmount.setText(String.format(Locale.getDefault(), "₹ %.2f", entry.getPendingAmount()));
        
        String status = entry.getStatus() != null ? entry.getStatus() : "pending";
        holder.tvStatus.setText(status.toUpperCase());
        
        String type = entry.getWorkType() != null ? entry.getWorkType().replace("_", " ") : "EARTH WORK";
        holder.tvWorkType.setText(type);
        
        if ("cleared".equalsIgnoreCase(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_cleared);
        } else if ("partial".equalsIgnoreCase(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_partial);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(entry);
        });
    }

    @Override
    public int getItemCount() {
        return workEntries != null ? workEntries.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvMobile, tvDate, tvPlace, tvPendingAmount, tvStatus, tvWorkType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvMobile = itemView.findViewById(R.id.tvMobile);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPlace = itemView.findViewById(R.id.tvPlace);
            tvPendingAmount = itemView.findViewById(R.id.tvPendingAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvWorkType = itemView.findViewById(R.id.tvWorkType);
        }
    }
}
