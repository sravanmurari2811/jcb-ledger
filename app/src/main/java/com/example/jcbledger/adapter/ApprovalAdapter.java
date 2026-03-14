package com.example.jcbledger.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jcbledger.R;
import com.example.jcbledger.model.User;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class ApprovalAdapter extends RecyclerView.Adapter<ApprovalAdapter.ViewHolder> {

    private List<User> pendingUsers;
    private OnApproveClickListener listener;

    public interface OnApproveClickListener {
        void onApproveClick(User user);
    }

    public ApprovalAdapter(List<User> pendingUsers, OnApproveClickListener listener) {
        this.pendingUsers = pendingUsers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_approval, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = pendingUsers.get(position);
        holder.tvName.setText(user.getName());
        holder.tvPhone.setText("Phone: " + user.getPhone());
        holder.tvVehicle.setText("Vehicle: " + user.getVehicleNumber());
        holder.tvRole.setText("Role: " + user.getRole());

        holder.btnApprove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onApproveClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pendingUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvVehicle, tvRole;
        MaterialButton btnApprove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvPhone = itemView.findViewById(R.id.tvUserPhone);
            tvVehicle = itemView.findViewById(R.id.tvUserVehicle);
            tvRole = itemView.findViewById(R.id.tvUserRole);
            btnApprove = itemView.findViewById(R.id.btnApprove);
        }
    }
}
