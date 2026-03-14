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

public class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.ViewHolder> {

    private List<User> userList;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onChangeStatus(User user);
    }

    public UserManagementAdapter(List<User> userList, OnUserActionListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_management, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvName.setText(user.getName());
        holder.tvPhone.setText("Phone: " + user.getPhone());
        holder.tvVehicle.setText("Vehicle: " + user.getVehicleNumber());
        holder.tvRole.setText("Role: " + user.getRole());
        holder.tvStatus.setText("Status: " + user.getStatus());

        holder.btnChangeStatus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChangeStatus(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvVehicle, tvRole, tvStatus;
        MaterialButton btnChangeStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvPhone = itemView.findViewById(R.id.tvUserPhone);
            tvVehicle = itemView.findViewById(R.id.tvUserVehicle);
            tvRole = itemView.findViewById(R.id.tvUserRole);
            tvStatus = itemView.findViewById(R.id.tvUserStatus);
            btnChangeStatus = itemView.findViewById(R.id.btnChangeStatus);
        }
    }
}
