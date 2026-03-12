package com.example.jcbledger.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jcbledger.R;
import com.example.jcbledger.model.DriverExpense;
import java.util.List;
import java.util.Locale;

public class DriverExpenseAdapter extends RecyclerView.Adapter<DriverExpenseAdapter.ViewHolder> {

    private List<DriverExpense> expenses;

    public DriverExpenseAdapter(List<DriverExpense> expenses) {
        this.expenses = expenses;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_driver_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DriverExpense expense = expenses.get(position);
        holder.tvDate.setText(expense.getDate());
        
        double amount = expense.getAmount();
        String type = expense.getType();
        
        if ("SALARY".equals(type)) {
            holder.tvAmount.setText(String.format(Locale.getDefault(), "- ₹ %.2f", amount));
            holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark));
        } else {
            holder.tvAmount.setText(String.format(Locale.getDefault(), "₹ %.2f", amount));
            holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.error_red));
        }

        holder.tvNote.setText(expense.getNote());
        
        if (expense.getNote() == null || expense.getNote().isEmpty()) {
            holder.tvNote.setVisibility(View.GONE);
        } else {
            holder.tvNote.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvAmount, tvNote;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvExpenseDate);
            tvAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvNote = itemView.findViewById(R.id.tvExpenseNote);
        }
    }
}
