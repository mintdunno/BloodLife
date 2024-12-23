package com.minh.bloodlife.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.minh.bloodlife.R;

public class SuperUserDashboardFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_super_user_dashboard, container, false);

        Button generateReportsButton = view.findViewById(R.id.generateReportsButton);
        generateReportsButton.setOnClickListener(v -> {
            ReportsFragment reportsFragment = new ReportsFragment();
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, reportsFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }
}