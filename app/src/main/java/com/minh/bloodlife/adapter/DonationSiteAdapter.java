package com.minh.bloodlife.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.minh.bloodlife.R;
import com.minh.bloodlife.model.DonationSite;

import java.util.List;

public class DonationSiteAdapter extends RecyclerView.Adapter<DonationSiteAdapter.ViewHolder> {

    private List<DonationSite> donationSites;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(DonationSite site);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public DonationSiteAdapter(List<DonationSite> donationSites) {
        this.donationSites = donationSites;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_donation_site, parent, false);
        return new ViewHolder(view, listener, donationSites);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DonationSite site = donationSites.get(position);
        holder.siteNameTextView.setText(site.getSiteName());
        holder.siteAddressTextView.setText(site.getAddress());
        // Set other fields as needed
    }

    @Override
    public int getItemCount() {
        return donationSites.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView siteNameTextView;
        TextView siteAddressTextView;

        public ViewHolder(View itemView, OnItemClickListener listener, List<DonationSite> donationSites) {
            super(itemView);
            siteNameTextView = itemView.findViewById(R.id.siteNameTextView);
            siteAddressTextView = itemView.findViewById(R.id.siteAddressTextView);
            itemView.setOnClickListener(view -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(donationSites.get(position));
                    }
                }
            });
        }
    }
}