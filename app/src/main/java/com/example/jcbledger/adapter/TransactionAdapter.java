package com.example.jcbledger.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jcbledger.R;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Map<String, Object>> transactions;

    public TransactionAdapter(List<Map<String, Object>> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> tx = transactions.get(position);
        
        // Note: Backend returns LocalDateTime which usually serializes to a list or string depending on Gson config
        // For simplicity, we assume it's handled as a string or we'll get it from the map
        Object dateObj = tx.get("transactionDate");
        holder.tvDate.setText(String.valueOf(dateObj).split("T")[0]);
        
        double amount = (double) tx.get("amount");
        holder.tvAmount.setText(String.format(Locale.getDefault(), "₹ %.2f", amount));
        
        holder.tvMethod.setText("Method: " + tx.get("paymentMethod"));
        holder.tvNote.setText(String.valueOf(tx.get("note")));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvAmount, tvMethod, tvNote;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvTxDate);
            tvAmount = itemView.findViewById(R.id.tvTxAmount);
            tvMethod = itemView.findViewById(R.id.tvTxMethod);
            tvNote = itemView.findViewById(R.id.tvTxNote);
        }
    }
}
