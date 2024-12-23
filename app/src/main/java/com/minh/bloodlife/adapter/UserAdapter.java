package com.minh.bloodlife.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.minh.bloodlife.R;
import com.minh.bloodlife.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;

    public UserAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userNameTextView.setText(String.format("%s %s", user.getFirstName(), user.getLastName()));
        holder.userEmailTextView.setText(user.getEmail());
        holder.userPhoneTextView.setText(user.getPhoneNumber()); // Make sure this field exists in your User model
        // Set other fields as necessary
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView, userEmailTextView, userPhoneTextView;

        UserViewHolder(View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.userName);
            userEmailTextView = itemView.findViewById(R.id.userEmail);
            userPhoneTextView = itemView.findViewById(R.id.userPhone);
            // Initialize other views as necessary
        }
    }
}